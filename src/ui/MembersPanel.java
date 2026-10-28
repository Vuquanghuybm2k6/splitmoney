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

        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(Theme.cardBorder());

        // Form nhập
        JPanel formCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formCard.setBackground(Theme.CARD_BG);
        formCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "Thêm thành viên", 0, 0, Theme.FONT_BOLD, Theme.TEXT_PRIMARY
        ));
        JLabel nameLabel = new JLabel("Tên thành viên:");
        nameLabel.setForeground(Theme.TEXT_SECONDARY);
        formCard.add(nameLabel);
        nameField.setPreferredSize(new Dimension(220, 32));
        formCard.add(nameField);
        JButton addButton = Theme.primaryButton("Thêm");
        formCard.add(addButton);
        add(formCard, BorderLayout.NORTH);

        // Danh sách
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(Theme.FONT_REGULAR);
        list.setFixedCellHeight(28);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "Danh sách thành viên", 0, 0, Theme.FONT_BOLD, Theme.TEXT_PRIMARY
        ));
        scroll.getViewport().setBackground(Theme.CARD_BG);
        add(scroll, BorderLayout.CENTER);

        // Nút xóa
        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottomCard.setBackground(Theme.CARD_BG);
        bottomCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                "Thao tác", 0, 0, Theme.FONT_BOLD, Theme.TEXT_PRIMARY
        ));
        JButton removeButton = Theme.secondaryButton("Xóa thành viên đã chọn");
        bottomCard.add(removeButton);
        add(bottomCard, BorderLayout.SOUTH);

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
