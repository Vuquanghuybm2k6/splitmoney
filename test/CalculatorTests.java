import logic.BalanceCalculator;
import logic.SettlementCalculator;
import logic.SplitCalculator;
import model.GroupData;
import model.Transfer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Chương trình test thuần Java (không cần JUnit). In PASS/FAIL cho từng ca. */
public class CalculatorTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testSplit100kBy3();
        testSplit5By8();
        testExampleBalances();
        testExampleSuggestions();
        testBalancesSumToZero();
        testApplyingSuggestionsZeroesBalances();
        testAllZeroNoSuggestions();
        testRecordedSettlementUpdatesBalance();

        System.out.println();
        System.out.println("Kết quả: " + passed + " PASS, " + failed + " FAIL");
        if (failed > 0) System.exit(1);
    }

    private static void check(String name, boolean ok) {
        if (ok) passed++;
        else failed++;
        System.out.println((ok ? "PASS" : "FAIL") + " - " + name);
    }

    private static long sum(Map<Integer, Long> m) {
        long s = 0;
        for (long v : m.values()) s += v;
        return s;
    }

    /** Nhóm An(1), Bình(2), Chi(3), Dũng(4); An trả 400k ăn, Bình trả 200k cà phê, chia đều 4 người. */
    private static GroupData exampleGroup() {
        GroupData d = new GroupData();
        int an = d.createMember("An").id();
        int binh = d.createMember("Bình").id();
        int chi = d.createMember("Chi").id();
        int dung = d.createMember("Dũng").id();
        List<Integer> all = List.of(an, binh, chi, dung);
        d.createExpense("Ăn tối", 400_000, an, all);
        d.createExpense("Cà phê", 200_000, binh, all);
        return d;
    }

    private static void testSplit100kBy3() {
        Map<Integer, Long> r = SplitCalculator.splitEqually(100_000, List.of(1, 2, 3));
        check("Chia 100.000đ cho 3 người = 33.334 / 33.333 / 33.333",
                r.get(1) == 33_334 && r.get(2) == 33_333 && r.get(3) == 33_333 && sum(r) == 100_000);
    }

    private static void testSplit5By8() {
        List<Integer> ids = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        Map<Integer, Long> r = SplitCalculator.splitEqually(5, ids);
        boolean ok = sum(r) == 5;
        for (int i = 1; i <= 8; i++) ok &= r.get(i) == (i <= 5 ? 1 : 0);
        check("Chia 5đ cho 8 người: 5 người đầu 1đ, 3 người sau 0đ, tổng = 5", ok);
    }

    private static void testExampleBalances() {
        GroupData d = exampleGroup();
        Map<Integer, Long> b = BalanceCalculator.calculate(d);
        check("Ví dụ An/Bình/Chi/Dũng: số dư +250.000 / +50.000 / −150.000 / −150.000",
                b.get(1) == 250_000 && b.get(2) == 50_000 && b.get(3) == -150_000 && b.get(4) == -150_000);
    }

    private static void testExampleSuggestions() {
        GroupData d = exampleGroup();
        List<Transfer> t = SettlementCalculator.suggest(BalanceCalculator.calculate(d));
        boolean ok = t.size() == 3
                && t.get(0).equals(new Transfer(3, 1, 150_000))
                && t.get(1).equals(new Transfer(4, 1, 100_000))
                && t.get(2).equals(new Transfer(4, 2, 50_000));
        check("Ví dụ: đúng 3 gợi ý (Chi→An 150k; Dũng→An 100k; Dũng→Bình 50k)", ok);
    }

    private static void testBalancesSumToZero() {
        GroupData d = exampleGroup();
        boolean ok = sum(BalanceCalculator.calculate(d)) == 0;
        // Thêm khoản chi gây dư lẻ
        d.createExpense("Taxi", 100_000, 3, List.of(1, 2, 3));
        d.createExpense("Nước", 7, 4, List.of(1, 2, 3, 4));
        ok &= sum(BalanceCalculator.calculate(d)) == 0;
        check("Tổng số dư cả nhóm luôn bằng 0 (kể cả khi có số dư lẻ)", ok);
    }

    private static void testApplyingSuggestionsZeroesBalances() {
        GroupData d = exampleGroup();
        d.createExpense("Taxi", 100_000, 3, List.of(1, 2, 3));
        Map<Integer, Long> b = new LinkedHashMap<>(BalanceCalculator.calculate(d));
        List<Transfer> t = SettlementCalculator.suggest(b);
        for (Transfer x : t) {
            b.merge(x.fromId(), x.amount(), Long::sum);
            b.merge(x.toId(), -x.amount(), Long::sum);
        }
        boolean ok = t.size() <= b.size() - 1;
        for (long v : b.values()) ok &= v == 0;
        check("Áp dụng hết gợi ý thì mọi số dư về 0 (tối đa n−1 giao dịch)", ok);
    }

    private static void testAllZeroNoSuggestions() {
        Map<Integer, Long> b = new LinkedHashMap<>();
        b.put(1, 0L);
        b.put(2, 0L);
        b.put(3, 0L);
        check("Mọi số dư bằng 0 thì không có gợi ý", SettlementCalculator.suggest(b).isEmpty());
    }

    private static void testRecordedSettlementUpdatesBalance() {
        GroupData d = exampleGroup();
        d.createSettlement(3, 1, 150_000); // Chi đã chuyển cho An
        Map<Integer, Long> b = BalanceCalculator.calculate(d);
        List<Transfer> t = new ArrayList<>(SettlementCalculator.suggest(b));
        check("Ghi nhận thanh toán: Chi về 0, An còn +100.000, tổng vẫn 0, còn 2 gợi ý",
                b.get(3) == 0 && b.get(1) == 100_000 && sum(b) == 0 && t.size() == 2);
    }
}
