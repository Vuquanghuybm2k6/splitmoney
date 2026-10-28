package ui;

import logic.BalanceCalculator;
import logic.SettlementCalculator;
import model.Expense;
import model.GroupData;
import model.Member;
import model.Settlement;
import model.Transfer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(Theme.cardBorder());

        // ----- Bảng số dư -----
        Theme.styleTable(balanceTable);
        DefaultTableCellRenderer balanceRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(new javax.swing.border.EmptyBorder(0, 8, 0, 12));
                if (!sel) {
                    String s = String.valueOf(v);
                    if (s.startsWith("+")) comp.setForeground(Theme.SUCCESS);
                    else if (s.startsWith("\u2212") || s.startsWith("-")) comp.setForeground(Theme.DANGER);
                    else comp.setForeground(Theme.TEXT_PRIMARY);
                }
                return comp;
            }
        };
        balanceTable.getColumnModel().getColumn(3).setCellRenderer(balanceRenderer);
        for (int i = 0; i < 3; i++) {
            if (i == 0) continue;
            balanceTable.getColumnModel().getColumn(i).setCellRenderer(Theme.rightRenderer());
        }
        JPanel balancePanel = titledCard("Bảng số dư (xanh: được nhận, đỏ: đang nợ)", new JScrollPane(balanceTable), null);

        // ----- Gợi ý thanh toán -----
        Theme.styleTable(suggestTable);
        for (int i = 1; i < suggestTable.getColumnCount(); i++) {
            suggestTable.getColumnModel().getColumn(i).setCellRenderer(Theme.rightRenderer());
        }
        JButton paidButton = Theme.primaryButton("Đã thanh toán");
        paidButton.addActionListener(e -> markPaid());
        JPanel suggestPanel = titledCard("Gợi ý thanh toán", new JScrollPane(suggestTable), paidButton);

        // ----- Lịch sử thanh toán -----
        Theme.styleTable(historyTable);
        for (int i = 1; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(Theme.rightRenderer());
        }
        JButton cancelButton = Theme.secondaryButton("Hủy");
        cancelButton.addActionListener(e -> cancelSettlement());
        JPanel historyPanel = titledCard("Lịch sử thanh toán", new JScrollPane(historyTable), cancelButton);

        JPanel center = new JPanel(new GridLayout(3, 1, 0, 10));
        center.setOpaque(false);
        center.add(balancePanel);
        center.add(suggestPanel);
        center.add(historyPanel);
        add(center, BorderLayout.CENTER);

        // ----- Thanh dưới -----
        JPanel bottomCard = new JPanel(new BorderLayout(8, 0));
        bottomCard.setBackground(Theme.CARD_BG);
        bottomCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));
        totalLabel.setForeground(Theme.TEXT_SECONDARY);
        bottomCard.add(totalLabel, BorderLayout.WEST);
        JButton copyButton = Theme.secondaryButton("Sao chép tổng kết");
        copyButton.addActionListener(e -> copySummary());
        bottomCard.add(copyButton, BorderLayout.EAST);
        add(bottomCard, BorderLayout.SOUTH);
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

    private static JPanel titledCard(String title, JComponent content, JButton button) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(Theme.CARD_BG);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                title, 0, 0, Theme.FONT_BOLD, Theme.TEXT_PRIMARY
        ));
        JScrollPane scroll = content instanceof JScrollPane ? (JScrollPane) content : new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.CARD_BG);
        p.add(scroll, BorderLayout.CENTER);
        if (button != null) {
            JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            south.setOpaque(false);
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
