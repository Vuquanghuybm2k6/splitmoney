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

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildForm(), BorderLayout.NORTH);

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton deleteButton = new JButton("Xóa khoản chi đã chọn");
        deleteButton.addActionListener(e -> deleteExpense());
        bottom.add(deleteButton);
        bottom.add(new JLabel("(Muốn sửa thì xóa rồi thêm lại)"));
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thêm khoản chi"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Mô tả:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        form.add(titleField, c);
        c.gridx = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        form.add(new JLabel("Số tiền (đ):"), c);
        c.gridx = 3;
        form.add(amountField, c);

        c.gridx = 0; c.gridy = 1;
        form.add(new JLabel("Người trả:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(payerBox, c);

        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Người tham gia:"), c);
        c.gridx = 1; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane sp = new JScrollPane(participantsPanel);
        sp.setPreferredSize(new Dimension(400, 70));
        form.add(sp, c);

        c.gridx = 1; c.gridy = 3; c.gridwidth = 3; c.fill = GridBagConstraints.NONE;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton allButton = new JButton("Chọn tất cả");
        JButton noneButton = new JButton("Bỏ chọn hết");
        JButton addButton = new JButton("Thêm");
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
