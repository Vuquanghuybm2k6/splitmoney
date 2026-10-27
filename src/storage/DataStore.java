package storage;

import model.Expense;
import model.GroupData;
import model.Member;
import model.Settlement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu/đọc dữ liệu nhóm ra file văn bản (UTF-8). Mỗi dòng là một bản ghi, các trường cách nhau bằng Tab.
 * G = tên nhóm, N = id kế tiếp, M = thành viên, E = khoản chi, S = thanh toán.
 */
public class DataStore {
    public static final String DEFAULT_FILE = "splitmoney-data.txt";

    private final Path file;

    public DataStore() {
        this(Paths.get(DEFAULT_FILE));
    }

    public DataStore(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public void save(GroupData data) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("G\t" + clean(data.getGroupName()));
        lines.add("N\t" + data.getNextId());
        for (Member m : data.getMembers()) {
            lines.add("M\t" + m.id() + "\t" + clean(m.name()));
        }
        for (Expense e : data.getExpenses()) {
            StringBuilder ids = new StringBuilder();
            for (int pid : e.participantIds()) {
                if (ids.length() > 0) ids.append(',');
                ids.append(pid);
            }
            lines.add("E\t" + e.id() + "\t" + clean(e.title()) + "\t" + e.amount()
                    + "\t" + e.paidBy() + "\t" + ids);
        }
        for (Settlement s : data.getSettlements()) {
            lines.add("S\t" + s.id() + "\t" + s.fromId() + "\t" + s.toId() + "\t" + s.amount());
        }

        // Ghi ra file tạm rồi đổi tên để tránh hỏng file nếu bị ngắt giữa chừng.
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Đọc dữ liệu; file chưa có thì trả về dữ liệu rỗng. Các dòng hỏng sẽ bị bỏ qua. */
    public GroupData load() throws IOException {
        GroupData data = new GroupData();
        if (!Files.exists(file)) return data;

        int maxId = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] p = line.split("\t", -1);
            try {
                switch (p[0]) {
                    case "G" -> data.setGroupName(p.length > 1 ? p[1] : "");
                    case "N" -> data.setNextId(Integer.parseInt(p[1]));
                    case "M" -> {
                        int id = Integer.parseInt(p[1]);
                        data.addMember(new Member(id, p[2]));
                        maxId = Math.max(maxId, id);
                    }
                    case "E" -> {
                        int id = Integer.parseInt(p[1]);
                        List<Integer> ids = new ArrayList<>();
                        for (String s : p[5].split(",")) {
                            if (!s.isBlank()) ids.add(Integer.parseInt(s.trim()));
                        }
                        data.addExpense(new Expense(id, p[2], Long.parseLong(p[3]), Integer.parseInt(p[4]), ids));
                        maxId = Math.max(maxId, id);
                    }
                    case "S" -> {
                        int id = Integer.parseInt(p[1]);
                        data.addSettlement(new Settlement(id, Integer.parseInt(p[2]),
                                Integer.parseInt(p[3]), Long.parseLong(p[4])));
                        maxId = Math.max(maxId, id);
                    }
                    default -> { /* bỏ qua dòng lạ */ }
                }
            } catch (RuntimeException ex) {
                // Dòng hỏng định dạng: bỏ qua.
            }
        }
        // Đảm bảo bộ đếm id luôn lớn hơn mọi id đã dùng.
        if (data.getNextId() <= maxId) data.setNextId(maxId + 1);
        return data;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }
}
