package logic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Chia đều tiền cho những người tham gia (Java thuần). */
public final class SplitCalculator {
    private SplitCalculator() {
    }

    /**
     * Mỗi người chịu amount / n (chia nguyên). Phần dư r = amount % n được chia thêm
     * 1 đồng cho r người đầu tiên trong danh sách. Tổng luôn bằng amount.
     */
    public static Map<Integer, Long> splitEqually(long amount, List<Integer> participantIds) {
        if (participantIds == null || participantIds.isEmpty())
            throw new IllegalArgumentException("Phải có ít nhất 1 người tham gia.");
        if (amount < 0) throw new IllegalArgumentException("Số tiền không được âm.");

        int n = participantIds.size();
        long base = amount / n;
        long remainder = amount % n;

        Map<Integer, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            long share = base + (i < remainder ? 1 : 0);
            result.merge(participantIds.get(i), share, Long::sum);
        }
        return result;
    }
}
