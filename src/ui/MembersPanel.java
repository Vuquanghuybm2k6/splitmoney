package ui;

import model.GroupData;
import model.Member;

import javax.swing.*;
import java.awt.*;

/** Tab "Thành viên": thêm / xóa thành viên. */
public class MembersPanel extends JPanel {
    private final MainFrame frame;
    private final GroupData data;

    private final DefaultListModel<Member> listModel = new DefaultListModel<>();
    private final JList<Member> list = new JList<>(listModel);
    private final JTextField nameField = new JTextField(20);

    public MembersPanel(MainFrame frame, GroupData data) {
        this.frame = frame;
        this.data = data;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form nhập
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        form.add(new JLabel("Tên thành viên:"));
        form.add(nameField);
        JButton addButton = new JButton("Thêm");
        form.add(addButton);
        add(form, BorderLayout.NORTH);

        // Danh sách
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(list.getFont().deriveFont(15f));
        add(new JScrollPane(list), BorderLayout.CENTER);

        // Nút xóa
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton removeButton = new JButton("Xóa thành viên đã chọn");
        bottom.add(removeButton);
        add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addMember());
        nameField.addActionListener(e -> addMember());
        removeButton.addActionListener(e -> removeMember());
    }

    private void addMember() {
        try {
            data.createMember(nameField.getText());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thêm được", JOptionPane.WARNING_MESSAGE);
            return;
        }
        nameField.setText("");
        nameField.requestFocusInWindow();
        frame.saveAndRefresh();
    }

    private void removeMember() {
        Member m = list.getSelectedValue();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một thành viên để xóa.");
            return;
        }
        if (data.isMemberUsed(m.id())) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xóa \"" + m.name() + "\" vì đã tham gia khoản chi hoặc thanh toán.\n"
                            + "Hãy xóa các khoản đó trước.",
                    "Không xóa được", JOptionPane.WARNING_MESSAGE);
            return;
        }
        data.removeMember(m.id());
        frame.saveAndRefresh();
    }

    public void refresh() {
        listModel.clear();
        for (Member m : data.getMembers()) listModel.addElement(m);
    }
}
