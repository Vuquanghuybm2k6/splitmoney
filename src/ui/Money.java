package ui;

/** Tiện ích định dạng và đọc số tiền (đơn vị: đồng, số nguyên). */
public final class Money {
    private static final String MINUS = "\u2212"; // dấu trừ đẹp

    private Money() {
    }

    /** 150000 → "150.000đ" */
    public static String format(long value) {
        String digits = Long.toString(Math.abs(value));
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            sb.append(digits.charAt(i));
            if (++count % 3 == 0 && i > 0) sb.append('.');
        }
        sb.reverse().append('đ');
        return (value < 0 ? "-" : "") + sb;
    }

    /** 250000 → "+250.000đ", -150000 → "−150.000đ", 0 → "0đ" */
    public static String signed(long value) {
        if (value > 0) return "+" + format(value);
        if (value < 0) return MINUS + format(-value);
        return format(0);
    }

    /**
     * Đọc số tiền người dùng gõ ("100.000", "100000", "100,000", "100.000đ").
     * Ném IllegalArgumentException (kèm thông báo tiếng Việt) nếu không hợp lệ.
     */
    public static long parse(String text) {
        if (text == null) throw new IllegalArgumentException("Vui lòng nhập số tiền.");
        String s = text.trim().replace(".", "").replace(",", "")
                .replace(" ", "").replace("\u00a0", "");
        if (s.endsWith("đ") || s.endsWith("Đ")) s = s.substring(0, s.length() - 1);
        if (s.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập số tiền.");
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)) || s.charAt(i) > '9')
                throw new IllegalArgumentException("Số tiền chỉ được chứa chữ số (ví dụ 100.000).");
        }
        if (s.length() > 15) throw new IllegalArgumentException("Số tiền quá lớn.");
        long v = Long.parseLong(s);
        if (v <= 0) throw new IllegalArgumentException("Số tiền phải lớn hơn 0.");
        return v;
    }
}
