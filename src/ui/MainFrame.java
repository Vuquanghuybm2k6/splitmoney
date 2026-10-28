package ui;

import model.GroupData;
import storage.DataStore;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Cửa sổ chính: thanh trên cùng (tên nhóm, Nhóm mới) và 3 tab. */
public class MainFrame extends JFrame {
    private final GroupData data;
    private final DataStore store;

    private final JTextField groupNameField = new JTextField(24);
    private final MembersPanel membersPanel;
    private final ExpensesPanel expensesPanel;
    private final SummaryPanel summaryPanel;
    private boolean settingName = false;

    public MainFrame(GroupData data, DataStore store) {
        super("SplitMoney - Chia tiền nhóm");
        this.data = data;
        this.store = store;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ----- Thanh trên cùng -----
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.add(new JLabel("Tên nhóm:"));
        left.add(groupNameField);
        top.add(left, BorderLayout.CENTER);
        JButton newGroupButton = new JButton("Nhóm mới");
        newGroupButton.addActionListener(e -> newGroup());
        top.add(newGroupButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        groupNameField.setText(data.getGroupName());
        groupNameField.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                if (settingName) return;
                MainFrame.this.data.setGroupName(groupNameField.getText().trim());
                save();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });

        // ----- 3 tab -----
        membersPanel = new MembersPanel(this, data);
        expensesPanel = new ExpensesPanel(this, data);
        summaryPanel = new SummaryPanel(this, data);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Thành viên", membersPanel);
        tabs.addTab("Khoản chi", expensesPanel);
        tabs.addTab("Tổng kết", summaryPanel);
        add(tabs, BorderLayout.CENTER);

        setSize(900, 650);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
        refreshAll();
    }

    /** Cho các tab cập nhật lại sau mỗi thay đổi. */
    public void refreshAll() {
        membersPanel.refresh();
        expensesPanel.refresh();
        summaryPanel.refresh();
    }

    /** Lưu dữ liệu ra file. */
    public void save() {
        try {
            store.save(data);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không lưu được dữ liệu: " + ex.getMessage(),
                    "Lỗi lưu file", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveAndRefresh() {
        save();
        refreshAll();
    }

    private void newGroup() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa toàn bộ dữ liệu (thành viên, khoản chi, thanh toán) để bắt đầu nhóm mới?\n"
                        + "Nhóm hiện tại sẽ được sao lưu tự động trước khi xóa.",
                "Nhóm mới", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        // Sao lưu dữ liệu nhóm hiện tại trước khi tạo nhóm mới
        try {
            Path currentFile = store.getFile();
            if (Files.exists(currentFile)) {
                String groupName = data.getGroupName();
                if (groupName == null || groupName.trim().isEmpty()) {
                    groupName = "nhom";
                }
                // Làm sạch tên file
                String safeName = groupName.trim().replaceAll("[^a-zA-Z0-9\\-_]", "_");
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Path backupFile = currentFile.resolveSibling("splitmoney-backup-" + safeName + "-" + timeStamp + ".txt");
                Files.copy(currentFile, backupFile);
                JOptionPane.showMessageDialog(this,
                        "Đã sao lưu nhóm cũ vào:\n" + backupFile.getFileName(),
                        "Đã sao lưu", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            // Không chặn việc tạo nhóm mới nếu không sao lưu được
            JOptionPane.showMessageDialog(this,
                    "Không thể sao lưu tự động nhóm cũ: " + ex.getMessage()
                            + "\nVẫn tiếp tục tạo nhóm mới?",
                    "Cảnh báo sao lưu", JOptionPane.WARNING_MESSAGE);
        }

        data.clear();
        settingName = true;
        groupNameField.setText("");
        settingName = false;
        saveAndRefresh();
    }
}
