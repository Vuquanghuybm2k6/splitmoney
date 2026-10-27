package ui;

import logic.BalanceCalculator;
import logic.SettlementCalculator;
import model.Expense;
import model.GroupData;
import model.Member;
import model.Settlement;
import model.Transfer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Map;

/** Tab "Tổng kết": bảng số dư, gợi ý thanh toán, lịch sử thanh toán, sao chép tổng kết. */
public class SummaryPanel extends JPanel {
    private static final Color GREEN = new Color(0, 130, 0);
    private static final Color RED = new Color(200, 0, 0);

    private final MainFrame frame;
    private final GroupData data;

    private final DefaultTableModel balanceModel = readOnlyModel("Thành viên", "Đã trả", "Phải chịu", "Số dư");
    private final DefaultTableModel suggestModel = readOnlyModel("Người chuyển", "Người nhận", "Số tiền");
    private final DefaultTableModel historyModel = readOnlyModel("Người chuyển", "Người nhận", "Số tiền");
    private final JTable balanceTable = new JTable(balanceModel);
    private final JTable suggestTable = new JTable(suggestModel);
    private final JTable historyTable = new JTable(historyModel);
    private final JLabel totalLabel = new JLabel(" ");

    private List<Transfer> transfers = List.of();

    public SummaryPanel(MainFrame frame, GroupData data) {
        this.frame = frame;
        this.data = data;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- Bảng số dư -----
        styleTable(balanceTable);
        DefaultTableCellRenderer balanceRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (!sel) {
                    String s = String.valueOf(v);
                    if (s.startsWith("+")) comp.setForeground(GREEN);
                    else if (s.startsWith("\u2212")) comp.setForeground(RED);
                    else comp.setForeground(t.getForeground());
                }
                return comp;
            }
        };
        balanceTable.getColumnModel().getColumn(3).setCellRenderer(balanceRenderer);
        JPanel balancePanel = titled("Bảng số dư (xanh: được nhận, đỏ: đang nợ)", new JScrollPane(balanceTable), null);

        // ----- Gợi ý thanh toán -----
        styleTable(suggestTable);
        JButton paidButton = new JButton("Đã thanh toán");
        paidButton.addActionListener(e -> markPaid());
        JPanel suggestPanel = titled("Gợi ý thanh toán", new JScrollPane(suggestTable), paidButton);

        // ----- Lịch sử thanh toán -----
        styleTable(historyTable);
        JButton cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(e -> cancelSettlement());
        JPanel historyPanel = titled("Lịch sử thanh toán", new JScrollPane(historyTable), cancelButton);

        JPanel center = new JPanel(new GridLayout(3, 1, 0, 8));
        center.add(balancePanel);
        center.add(suggestPanel);
        center.add(historyPanel);
        add(center, BorderLayout.CENTER);

        // ----- Thanh dưới -----
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(totalLabel, BorderLayout.WEST);
        JButton copyButton = new JButton("Sao chép tổng kết");
        copyButton.addActionListener(e -> copySummary());
        bottom.add(copyButton, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 1; i < table.getColumnCount(); i++) {
            // Cột tiền căn phải (với bảng gợi ý/lịch sử chỉ cột cuối là tiền)
            boolean isMoney = table.getColumnCount() == 4 || i == table.getColumnCount() - 1;
            if (isMoney) table.getColumnModel().getColumn(i).setCellRenderer(right);
        }
    }

    private static JPanel titled(String title, JComponent content, JButton button) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(content, BorderLayout.CENTER);
        if (button != null) {
            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            south.add(button);
            p.add(south, BorderLayout.SOUTH);
        }
        return p;
    }

    // ---------- Hành động ----------
    private void markPaid() {
        int row = suggestTable.getSelectedRow();
        if (row < 0 || row >= transfers.size()) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một gợi ý thanh toán.");
            return;
        }
        Transfer t = transfers.get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                data.nameOf(t.fromId()) + " đã chuyển cho " + data.nameOf(t.toId()) + " "
                        + Money.format(t.amount()) + "?",
                "Ghi nhận đã thanh toán", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            data.createSettlement(t.fromId(), t.toId(), t.amount());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        frame.saveAndRefresh();
    }

    private void cancelSettlement() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trong lịch sử để hủy.");
            return;
        }
        Settlement s = data.getSettlements().get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "Hủy ghi nhận: " + data.nameOf(s.fromId()) + " → " + data.nameOf(s.toId())
                        + " " + Money.format(s.amount()) + "?",
                "Hủy thanh toán", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        data.removeSettlement(s.id());
        frame.saveAndRefresh();
    }

    private void copySummary() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(buildSummaryText()), null);
        JOptionPane.showMessageDialog(this, "Đã sao chép tổng kết. Bạn có thể dán vào Zalo/Messenger.");
    }

    // ---------- Nội dung tổng kết ----------
    private long totalSpending() {
        long total = 0;
        for (Expense e : data.getExpenses()) total += e.amount();
        return total;
    }

    private String buildSummaryText() {
        Map<Integer, Long> balances = BalanceCalculator.calculate(data);
        List<Transfer> list = SettlementCalculator.suggest(balances);
        String name = data.getGroupName().isBlank() ? "Nhóm" : data.getGroupName();

        StringBuilder sb = new StringBuilder();
        sb.append("TỔNG KẾT: ").append(name).append('\n');
        sb.append("Tổng chi tiêu: ").append(Money.format(totalSpending())).append("\n\n");
        sb.append("Số dư:\n");
        for (Member m : data.getMembers()) {
            sb.append("- ").append(m.name()).append(": ").append(Money.signed(balances.getOrDefault(m.id(), 0L))).append('\n');
        }
        sb.append("\nCần chuyển tiền:\n");
        if (list.isEmpty()) {
            sb.append("Mọi người đã cân bằng, không cần chuyển thêm.\n");
        } else {
            for (Transfer t : list) {
                sb.append("- ").append(data.nameOf(t.fromId())).append(" chuyển cho ")
                        .append(data.nameOf(t.toId())).append(": ").append(Money.format(t.amount())).append('\n');
            }
        }
        return sb.toString();
    }

    // ---------- Vẽ lại ----------
    public void refresh() {
        Map<Integer, Long> paid = BalanceCalculator.paid(data);
        Map<Integer, Long> owed = BalanceCalculator.owed(data);
        Map<Integer, Long> balances = BalanceCalculator.calculate(data);

        balanceModel.setRowCount(0);
        for (Member m : data.getMembers()) {
            balanceModel.addRow(new Object[]{
                    m.name(),
                    Money.format(paid.getOrDefault(m.id(), 0L)),
                    Money.format(owed.getOrDefault(m.id(), 0L)),
                    Money.signed(balances.getOrDefault(m.id(), 0L))});
        }

        transfers = SettlementCalculator.suggest(balances);
        suggestModel.setRowCount(0);
        for (Transfer t : transfers) {
            suggestModel.addRow(new Object[]{data.nameOf(t.fromId()), data.nameOf(t.toId()), Money.format(t.amount())});
        }

        historyModel.setRowCount(0);
        for (Settlement s : data.getSettlements()) {
            historyModel.addRow(new Object[]{data.nameOf(s.fromId()), data.nameOf(s.toId()), Money.format(s.amount())});
        }

        totalLabel.setText("Tổng chi tiêu của nhóm: " + Money.format(totalSpending()));
    }
}
