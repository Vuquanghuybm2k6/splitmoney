package ui;

import model.Expense;
import model.GroupData;
import model.Member;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Tab "Khoản chi": thêm khoản chi (chia đều), danh sách, xóa. */
public class ExpensesPanel extends JPanel {
    private final MainFrame frame;
    private final GroupData data;

    private final JTextField titleField = new JTextField(18);
    private final JTextField amountField = new JTextField(10);
    private final JComboBox<Member> payerBox = new JComboBox<>();
    private final JPanel participantsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
    private final Map<Integer, JCheckBox> checkBoxes = new LinkedHashMap<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Mô tả", "Người trả", "Số tiền", "Người tham gia"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public ExpensesPanel(MainFrame frame, GroupData data) {
        this.frame = frame;
        this.data = data;

        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(Theme.cardBorder());

        add(buildForm(), BorderLayout.NORTH);

        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.HEADER_BG);
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
        header.setBorder(new javax.swing.border.MatteBorder(0, 0, 1, 0, Theme.BORDER));
        DefaultTableCellRenderer right = Theme.rightRenderer();
        table.getColumnModel().getColumn(2).setCellRenderer(right);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "Danh sách khoản chi", 0, 0, Theme.FONT_BOLD, Theme.TEXT_PRIMARY
        ));
        tableScroll.getViewport().setBackground(Theme.CARD_BG);
        add(tableScroll, BorderLayout.CENTER);

        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottomCard.setBackground(Theme.CARD_BG);
        bottomCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "Thao tác", 0, 0, Theme.FONT_BOLD, Theme.TEXT_PRIMARY
        ));
        JButton deleteButton = Theme.secondaryButton("Xóa khoản chi đã chọn");
        deleteButton.addActionListener(e -> deleteExpense());
        bottomCard.add(deleteButton);
        JLabel hint = new JLabel("(Muốn sửa thì xóa rồi thêm lại)");
        hint.setForeground(Theme.TEXT_SECONDARY);
        bottomCard.add(hint);
        add(bottomCard, BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD_BG);
        form.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "Thêm khoản chi", 0, 0, Theme.FONT_BOLD, Theme.TEXT_PRIMARY
        ));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        JLabel descLabel = new JLabel("Mô tả:");
        descLabel.setForeground(Theme.TEXT_SECONDARY);
        form.add(descLabel, c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        titleField.setPreferredSize(new Dimension(200, 32));
        form.add(titleField, c);
        c.gridx = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        JLabel amountLabel = new JLabel("Số tiền (đ):");
        amountLabel.setForeground(Theme.TEXT_SECONDARY);
        form.add(amountLabel, c);
        c.gridx = 3;
        amountField.setPreferredSize(new Dimension(120, 32));
        form.add(amountField, c);

        c.gridx = 0; c.gridy = 1;
        JLabel payerLabel = new JLabel("Người trả:");
        payerLabel.setForeground(Theme.TEXT_SECONDARY);
        form.add(payerLabel, c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        payerBox.setPreferredSize(new Dimension(200, 32));
        form.add(payerBox, c);

        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.NORTHWEST;
        JLabel participantsLabel = new JLabel("Người tham gia:");
        participantsLabel.setForeground(Theme.TEXT_SECONDARY);
        form.add(participantsLabel, c);
        c.gridx = 1; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        participantsPanel.setBackground(Theme.CARD_BG);
        JScrollPane sp = new JScrollPane(participantsPanel);
        sp.setPreferredSize(new Dimension(400, 76));
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        form.add(sp, c);

        c.gridx = 1; c.gridy = 3; c.gridwidth = 3; c.fill = GridBagConstraints.NONE;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        JButton allButton = Theme.secondaryButton("Chọn tất cả");
        JButton noneButton = Theme.secondaryButton("Bỏ chọn hết");
        JButton addButton = Theme.primaryButton("Thêm");
        buttons.add(allButton);
        buttons.add(noneButton);
        buttons.add(addButton);
        form.add(buttons, c);

        allButton.addActionListener(e -> checkBoxes.values().forEach(cb -> cb.setSelected(true)));
        noneButton.addActionListener(e -> checkBoxes.values().forEach(cb -> cb.setSelected(false)));
        addButton.addActionListener(e -> addExpense());
        amountField.addActionListener(e -> addExpense());
        return form;
    }

    private void addExpense() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            warn("Vui lòng nhập mô tả khoản chi.");
            return;
        }
        long amount;
        try {
            amount = Money.parse(amountField.getText());
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
            return;
        }
        Member payer = (Member) payerBox.getSelectedItem();
        if (payer == null) {
            warn("Hãy thêm thành viên ở tab \"Thành viên\" trước.");
            return;
        }
        List<Integer> participants = new ArrayList<>();
        for (Map.Entry<Integer, JCheckBox> e : checkBoxes.entrySet()) {
            if (e.getValue().isSelected()) participants.add(e.getKey());
        }
        if (participants.isEmpty()) {
            warn("Phải chọn ít nhất 1 người tham gia.");
            return;
        }
        try {
            data.createExpense(title, amount, payer.id(), participants);
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
            return;
        }
        titleField.setText("");
        amountField.setText("");
        titleField.requestFocusInWindow();
        frame.saveAndRefresh();
    }

    private void deleteExpense() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một khoản chi để xóa.");
            return;
        }
        Expense e = data.getExpenses().get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa khoản chi \"" + e.title() + "\" (" + Money.format(e.amount()) + ")?",
                "Xóa khoản chi", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        data.removeExpense(e.id());
        frame.saveAndRefresh();
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    public void refresh() {
        // --- Người trả: giữ lại lựa chọn cũ ---
        Member previousPayer = (Member) payerBox.getSelectedItem();
        payerBox.removeAllItems();
        Member toSelect = null;
        for (Member m : data.getMembers()) {
            payerBox.addItem(m);
            if (previousPayer != null && previousPayer.id() == m.id()) toSelect = m;
        }
        if (toSelect != null) payerBox.setSelectedItem(toSelect);

        // --- Ô tick người tham gia: giữ trạng thái cũ, người mới mặc định được tick ---
        Set<Integer> known = new HashSet<>(checkBoxes.keySet());
        Set<Integer> checked = new HashSet<>();
        checkBoxes.forEach((id, cb) -> {
            if (cb.isSelected()) checked.add(id);
        });
        checkBoxes.clear();
        participantsPanel.removeAll();
        for (Member m : data.getMembers()) {
            JCheckBox cb = new JCheckBox(m.name(), !known.contains(m.id()) || checked.contains(m.id()));
            checkBoxes.put(m.id(), cb);
            participantsPanel.add(cb);
        }
        participantsPanel.revalidate();
        participantsPanel.repaint();

        // --- Bảng ---
        tableModel.setRowCount(0);
        for (Expense e : data.getExpenses()) {
            StringJoiner names = new StringJoiner(", ");
            for (int id : e.participantIds()) names.add(data.nameOf(id));
            tableModel.addRow(new Object[]{e.title(), data.nameOf(e.paidBy()), Money.format(e.amount()), names.toString()});
        }
    }
}
