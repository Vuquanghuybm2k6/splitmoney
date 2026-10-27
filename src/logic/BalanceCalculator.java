package logic;

import model.Expense;
import model.GroupData;
import model.Member;
import model.Settlement;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tính "đã trả", "phải chịu" và số dư của từng người. Luôn tính lại từ dữ liệu gốc. */
public final class BalanceCalculator {
    private BalanceCalculator() {
    }

    private static Map<Integer, Long> zeroMap(GroupData data) {
        Map<Integer, Long> map = new LinkedHashMap<>();
        for (Member m : data.getMembers()) map.put(m.id(), 0L);
        return map;
    }

    /** Tổng số tiền mỗi người đã ứng trả. */
    public static Map<Integer, Long> paid(GroupData data) {
        Map<Integer, Long> map = zeroMap(data);
        for (Expense e : data.getExpenses()) map.merge(e.paidBy(), e.amount(), Long::sum);
        return map;
    }

    /** Tổng số tiền mỗi người phải chịu (sau khi chia đều). */
    public static Map<Integer, Long> owed(GroupData data) {
        Map<Integer, Long> map = zeroMap(data);
        for (Expense e : data.getExpenses()) {
            SplitCalculator.splitEqually(e.amount(), e.participantIds())
                    .forEach((id, share) -> map.merge(id, share, Long::sum));
        }
        return map;
    }

    /** Số dư = đã trả − phải chịu + đã chuyển đi − đã nhận về. Dương: được nhận, âm: đang nợ. */
    public static Map<Integer, Long> calculate(GroupData data) {
        Map<Integer, Long> balance = zeroMap(data);
        paid(data).forEach((id, v) -> balance.merge(id, v, Long::sum));
        owed(data).forEach((id, v) -> balance.merge(id, -v, Long::sum));
        for (Settlement s : data.getSettlements()) {
            balance.merge(s.fromId(), s.amount(), Long::sum);
            balance.merge(s.toId(), -s.amount(), Long::sum);
        }
        return balance;
    }
}
