package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class Theme {
    private Theme() {
    }

    // Màu sắc
    public static final Color BACKGROUND = new Color(248, 249, 251);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color HEADER_BG = new Color(241, 245, 249);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);

    // Font
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);

    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Font mặc định
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("Button.font", FONT_REGULAR);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("ComboBox.font", FONT_REGULAR);
        UIManager.put("List.font", FONT_REGULAR);
        UIManager.put("Table.font", FONT_REGULAR);
        UIManager.put("TableHeader.font", FONT_BOLD);
        UIManager.put("TitledBorder.font", FONT_BOLD);
        UIManager.put("TabbedPane.font", FONT_BOLD);
        UIManager.put("OptionPane.messageFont", FONT_REGULAR);
        UIManager.put("OptionPane.buttonFont", FONT_REGULAR);

        // Màu cơ bản
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
    }

    // Nút chính
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 34));
        return btn;
    }

    // Nút phụ
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(CARD_BG);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 16, 7, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 34));
        return btn;
    }

    // Border cho card
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)
        );
    }

    // Style bảng
    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBorder(new EmptyBorder(4, 8, 4, 8));
    }

    // Căn phải cho cột tiền
    public static DefaultTableCellRenderer rightRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        r.setBorder(new EmptyBorder(0, 8, 0, 12));
        return r;
    }

    // Căn giữa cho header/cột đơn giản
    public static DefaultTableCellRenderer centerRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        r.setBorder(new EmptyBorder(0, 8, 0, 8));
        return r;
    }
}
