package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * FeedPanel
 * The rightmost column: live Observer Feed showing timestamped events
 * from the warehouse's event log.
 */
public class FeedPanel extends JPanel {

    private JPanel entriesPanel;

    public FeedPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(Theme.PANEL_BG);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(Theme.sectionLabel("OBSERVER FEED"), BorderLayout.WEST);
        JLabel pulse = new JLabel("↯");
        pulse.setFont(new Font("SansSerif", Font.BOLD, 16));
        pulse.setForeground(Theme.ACCENT2);
        head.add(pulse, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        // Scrollable entries
        entriesPanel = new JPanel();
        entriesPanel.setOpaque(false);
        entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(entriesPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Theme.styleScrollBar(scroll.getVerticalScrollBar());
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh(List<String[]> log) {
        entriesPanel.removeAll();
        int max = Math.min(log.size(), 40);
        for (int i = 0; i < max; i++) {
            String[] entry = log.get(i);
            entriesPanel.add(buildEntry(entry[0], entry[1], entry[2]));
            entriesPanel.add(Box.createVerticalStrut(2));
        }
        entriesPanel.revalidate();
        entriesPanel.repaint();
    }

    private JPanel buildEntry(String time, String source, String message) {
        JPanel entry = new JPanel(new BorderLayout(6, 2));
        entry.setOpaque(false);
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        boolean isWrite = message.startsWith("Write") || message.contains("docked")
                        || message.contains("added")  || message.contains("registered")
                        || message.contains("delivered");
        boolean isRead  = message.startsWith("Read lock");

        JLabel icon = new JLabel(isRead ? "ℹ" : "●");
        icon.setFont(new Font("SansSerif", Font.BOLD, 10));
        icon.setForeground(isWrite ? Theme.GREEN : isRead ? Theme.BLUE : Theme.ORANGE);
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel right = new JPanel(new BorderLayout(0, 1));
        right.setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel timeL = new JLabel(time);
        timeL.setFont(Theme.FONT_SMALL);
        timeL.setForeground(Theme.TEXT_DIM);
        JLabel srcL = new JLabel(source);
        srcL.setFont(new Font("SansSerif", Font.BOLD, 10));
        srcL.setForeground(Theme.ACCENT);
        topRow.add(timeL, BorderLayout.WEST);
        topRow.add(srcL,  BorderLayout.EAST);

        JLabel msgL = new JLabel("<html><body style='width:160px'>" + message + "</body></html>");
        msgL.setFont(Theme.FONT_SMALL);
        msgL.setForeground(Theme.TEXT_SEC);

        right.add(topRow, BorderLayout.NORTH);
        right.add(msgL,   BorderLayout.CENTER);

        entry.add(icon,  BorderLayout.WEST);
        entry.add(right, BorderLayout.CENTER);

        MatteBorder bottom = new MatteBorder(0, 0, 1, 0, Theme.BORDER);
        entry.setBorder(BorderFactory.createCompoundBorder(
            bottom, new EmptyBorder(5, 6, 5, 6)
        ));
        return entry;
    }
}
