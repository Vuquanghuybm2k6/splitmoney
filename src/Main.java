import model.GroupData;
import storage.DataStore;
import ui.MainFrame;

import javax.swing.*;
import java.io.IOException;

/** Điểm khởi động ứng dụng SplitMoney. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Dùng giao diện mặc định nếu không đặt được.
        }

        DataStore store = new DataStore();
        GroupData data;
        String loadError = null;
        try {
            data = store.load();
        } catch (IOException ex) {
            data = new GroupData();
            loadError = ex.getMessage();
        }

        final GroupData finalData = data;
        final String error = loadError;
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(finalData, store);
            frame.setVisible(true);
            if (error != null) {
                JOptionPane.showMessageDialog(frame,
                        "Không đọc được file dữ liệu: " + error + "\nỨng dụng sẽ bắt đầu với dữ liệu rỗng.",
                        "Lỗi đọc file", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}
