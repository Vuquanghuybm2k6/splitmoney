package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Gói toàn bộ dữ liệu của một nhóm. Chỉ chứa dữ liệu và các hàm tiện ích, không có giao diện. */
public class GroupData {
    private String groupName = "";
    private final List<Member> members = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final List<Settlement> settlements = new ArrayList<>();
    private int nextId = 1;

    // ---------- Tên nhóm & bộ đếm id ----------
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? "" : groupName;
    }

    /** Lấy id mới và tăng bộ đếm. */
    public int takeNextId() {
        return nextId++;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    // ---------- Truy cập danh sách (chỉ đọc) ----------
    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public List<Settlement> getSettlements() {
        return Collections.unmodifiableList(settlements);
    }

    // ---------- Thành viên ----------
    public Member findMember(int id) {
        for (Member m : members) {
            if (m.id() == id) return m;
        }
        return null;
    }

    public String nameOf(int id) {
        Member m = findMember(id);
        return m == null ? "?" : m.name();
    }

    public boolean hasMemberName(String name) {
        for (Member m : members) {
            if (m.name().equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }

    /** Thêm thành viên mới có kiểm tra hợp lệ. */
    public Member createMember(String name) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) throw new IllegalArgumentException("Tên thành viên không được để trống.");
        if (hasMemberName(n)) throw new IllegalArgumentException("Tên \"" + n + "\" đã có trong nhóm.");
        Member m = new Member(takeNextId(), n);
        members.add(m);
        return m;
    }

    /** Thêm trực tiếp (dùng khi đọc file). */
    public void addMember(Member m) {
        members.add(m);
    }

    /** Thành viên đã tham gia khoản chi hoặc thanh toán nào chưa? */
    public boolean isMemberUsed(int id) {
        for (Expense e : expenses) {
            if (e.paidBy() == id || e.participantIds().contains(id)) return true;
        }
        for (Settlement s : settlements) {
            if (s.fromId() == id || s.toId() == id) return true;
        }
        return false;
    }

    /** Xóa thành viên; trả về false nếu thành viên đang được dùng hoặc không tồn tại. */
    public boolean removeMember(int id) {
        if (isMemberUsed(id)) return false;
        return members.removeIf(m -> m.id() == id);
    }

    // ---------- Khoản chi ----------
    public Expense createExpense(String title, long amount, int paidBy, List<Integer> participantIds) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Mô tả khoản chi không được để trống.");
        if (amount <= 0) throw new IllegalArgumentException("Số tiền phải lớn hơn 0.");
        if (findMember(paidBy) == null) throw new IllegalArgumentException("Người trả phải là thành viên của nhóm.");
        if (participantIds == null || participantIds.isEmpty())
            throw new IllegalArgumentException("Phải có ít nhất 1 người tham gia.");
        for (int pid : participantIds) {
            if (findMember(pid) == null) throw new IllegalArgumentException("Người tham gia không thuộc nhóm.");
        }
        Expense e = new Expense(takeNextId(), t, amount, paidBy, participantIds);
        expenses.add(e);
        return e;
    }

    public void addExpense(Expense e) {
        expenses.add(e);
    }

    public boolean removeExpense(int id) {
        return expenses.removeIf(e -> e.id() == id);
    }

    // ---------- Thanh toán ----------
    public Settlement createSettlement(int fromId, int toId, long amount) {
        if (fromId == toId) throw new IllegalArgumentException("Người chuyển và người nhận phải khác nhau.");
        if (amount <= 0) throw new IllegalArgumentException("Số tiền phải lớn hơn 0.");
        if (findMember(fromId) == null || findMember(toId) == null)
            throw new IllegalArgumentException("Người chuyển/nhận phải là thành viên của nhóm.");
        Settlement s = new Settlement(takeNextId(), fromId, toId, amount);
        settlements.add(s);
        return s;
    }

    public void addSettlement(Settlement s) {
        settlements.add(s);
    }

    public boolean removeSettlement(int id) {
        return settlements.removeIf(s -> s.id() == id);
    }

    // ---------- Xóa toàn bộ ----------
    public void clear() {
        groupName = "";
        members.clear();
        expenses.clear();
        settlements.clear();
        nextId = 1;
    }
}
