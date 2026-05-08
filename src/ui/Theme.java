package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;

/**
 * Theme
 * Central registry for the dark industrial color palette, fonts,
 * and factory methods for consistently-styled widgets.
 */
public final class Theme {

    private Theme() {}

    // ── Palette ──────────────────────────────────────────────
    public static final Color BG       = new Color(0x12, 0x14, 0x1A);
    public static final Color PANEL_BG = new Color(0x1A, 0x1D, 0x27);
    public static final Color CARD_BG  = new Color(0x1F, 0x22, 0x30);
    public static final Color CARD_SEL = new Color(0x26, 0x2B, 0x3E);
    public static final Color ACCENT   = new Color(0xFF, 0xB3, 0x00);
    public static final Color ACCENT2  = new Color(0x00, 0xD4, 0xFF);
    public static final Color TEXT_PRI = new Color(0xF0, 0xF0, 0xF8);
    public static final Color TEXT_SEC = new Color(0x8A, 0x8E, 0xA8);
    public static final Color TEXT_DIM = new Color(0x50, 0x55, 0x70);
    public static final Color GREEN    = new Color(0x22, 0xC5, 0x5E);
    public static final Color ORANGE   = new Color(0xFF, 0xB3, 0x00);
    public static final Color RED      = new Color(0xFF, 0x4D, 0x6D);
    public static final Color BLUE     = new Color(0x00, 0xD4, 0xFF);
    public static final Color BORDER   = new Color(0x2A, 0x2F, 0x45);

    // ── Fonts ────────────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_BODY  = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 10);
    public static final Font FONT_BADGE = new Font("SansSerif", Font.BOLD,   9);

    // ── Panel / card helpers ──────────────────────────────────

    public static JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        p.setBorder(new LineBorder(BORDER, 1, true));
        return p;
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT_DIM);
        return l;
    }

    public static JPanel emptyNotice(String msg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel l = new JLabel(msg);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_DIM);
        p.add(l);
        return p;
    }

    public static JPanel sectionDivider(String title, String meta) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRI);
        JLabel metaL = new JLabel(meta);
        metaL.setFont(FONT_BADGE);
        metaL.setForeground(TEXT_DIM);
        row.add(lbl,   BorderLayout.WEST);
        row.add(metaL, BorderLayout.EAST);
        return row;
    }

    public static JPanel statTile(String label, String value) {
        JPanel tile = new JPanel(new BorderLayout(0, 4));
        tile.setBackground(new Color(0x14, 0x17, 0x22));
        tile.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_DIM);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 22));
        val.setForeground(TEXT_PRI);
        tile.add(lbl, BorderLayout.NORTH);
        tile.add(val, BorderLayout.CENTER);
        return tile;
    }

    public static JLabel statusBadge(String label) {
        JLabel l = new JLabel(label);
        l.setFont(FONT_BADGE);
        Color bg = label.equals("LOADING")     ? ORANGE
                 : label.equals("DEPARTED")    ? BLUE
                 : label.equals("MAINTENANCE") ? RED
                 : GREEN;
        l.setBackground(bg);
        l.setForeground(bg == ORANGE ? Color.BLACK : Color.WHITE);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(3, 7, 3, 7));
        return l;
    }

    public static JPanel capacityBar(double loaded, double cap) {
        double ratio = cap > 0 ? Math.min(1.0, loaded / cap) : 0;
        JPanel bar = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                Color fill = ratio > 0.8 ? RED : ratio > 0.5 ? ORANGE : GREEN;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, (int)(getWidth() * ratio), getHeight(), 4, 4);
            }
        };
        bar.setPreferredSize(new Dimension(100, 5));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        bar.setOpaque(false);
        return bar;
    }

    // ── Button helpers ────────────────────────────────────────

    public static JButton iconButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setForeground(TEXT_SEC);
        b.setBackground(CARD_BG);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(26, 26));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_SMALL);
        b.setForeground(TEXT_PRI);
        b.setBackground(CARD_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton filledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(Color.BLACK);
        b.setBackground(ACCENT);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton smallTextButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_SMALL);
        b.setForeground(TEXT_SEC);
        b.setBackground(new Color(0, 0, 0, 0));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Form helpers ──────────────────────────────────────────

    public static JPanel formPanel() {
        JPanel p = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        return p;
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_SEC);
        return l;
    }

    public static JTextField styledField(String placeholder) {
        return styledFieldWithPlaceholder(placeholder, null);
    }

    public static JTextField styledFieldWithPlaceholder(String placeholder, String suffixHint) {
        JTextField f = new JTextField(18) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && placeholder != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(TEXT_DIM);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left + 2,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1);
                }
            }
        };
        f.setBackground(CARD_BG);
        f.setForeground(TEXT_PRI);
        f.setCaretColor(ACCENT);
        f.setFont(FONT_BODY);
        f.setOpaque(true);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return f;
    }

    public static void styleCombo(JComboBox<String> combo) {
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT_PRI);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(2, 4, 2, 4)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? CARD_SEL : CARD_BG);
                lbl.setForeground(TEXT_PRI);
                lbl.setFont(FONT_BODY);
                lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
                lbl.setOpaque(true);
                return lbl;
            }
        });
        combo.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("v");
                btn.setBackground(CARD_BG);
                btn.setForeground(TEXT_SEC);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 10));
                return btn;
            }
            @Override public void paintCurrentValueBackground(Graphics g,
                    Rectangle bounds, boolean hasFocus) {
                g.setColor(CARD_BG);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
    }

    public static JRadioButton styledRadio(String label, Color fg, ButtonGroup group) {
        JRadioButton rb = new JRadioButton(label);
        rb.setFont(FONT_BODY);
        rb.setForeground(fg != null ? fg : TEXT_PRI);
        rb.setBackground(PANEL_BG);
        rb.setFocusPainted(false);
        group.add(rb);
        return rb;
    }

    public static void styleScrollBar(JScrollBar sb) {
        sb.setBackground(PANEL_BG);
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = BORDER;
                trackColor = PANEL_BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    public static JDialog styledDialog(JFrame owner, String title) {
        JDialog d = new JDialog(owner, title, true);
        d.getContentPane().setBackground(PANEL_BG);
        d.setLayout(new BorderLayout());
        d.getRootPane().setBorder(new LineBorder(BORDER, 1));
        return d;
    }

    // ── WrapLayout ────────────────────────────────────────────
    /** FlowLayout that wraps onto the next line inside narrow containers. */
    public static class WrapLayout extends java.awt.FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container t) { return layoutSize(t, true); }
        @Override public Dimension minimumLayoutSize(Container t)   { return layoutSize(t, false); }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int maxW  = target.getWidth();
                if (maxW == 0) maxW = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                int hg = getHgap(), vg = getVgap();
                int x = ins.left + hg, y = ins.top + vg, rowH = 0, maxX = ins.left;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width + hg > maxW && x > ins.left + hg) {
                        x = ins.left + hg; y += rowH + vg; rowH = 0;
                    }
                    maxX = Math.max(maxX, x + d.width);
                    x += d.width + hg;
                    rowH = Math.max(rowH, d.height);
                }
                return new Dimension(maxX + ins.right, y + rowH + vg + ins.bottom);
            }
        }
    }
}
