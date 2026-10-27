package logic;

import model.Transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Gợi ý ai chuyển cho ai để cả nhóm về 0 (tối đa n − 1 giao dịch). */
public final class SettlementCalculator {
    private SettlementCalculator() {
    }

    /** Lấy người nợ đầu tiên ghép với người được nhận đầu tiên, chuyển số nhỏ hơn, lặp đến hết. */
    public static List<Transfer> suggest(Map<Integer, Long> balances) {
        List<long[]> debtors = new ArrayList<>();   // {id, số tiền còn nợ}
        List<long[]> creditors = new ArrayList<>(); // {id, số tiền còn được nhận}
        for (Map.Entry<Integer, Long> e : balances.entrySet()) {
            long v = e.getValue();
            if (v < 0) debtors.add(new long[]{e.getKey(), -v});
            else if (v > 0) creditors.add(new long[]{e.getKey(), v});
        }

        List<Transfer> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            long[] d = debtors.get(i);
            long[] c = creditors.get(j);
            long pay = Math.min(d[1], c[1]);
            result.add(new Transfer((int) d[0], (int) c[0], pay));
            d[1] -= pay;
            c[1] -= pay;
            if (d[1] == 0) i++;
            if (c[1] == 0) j++;
        }
        return result;
    }
}
