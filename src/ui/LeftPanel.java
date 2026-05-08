package ui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * LeftPanel
 * The leftmost column: scrollable warehouse list + R/W lock status panel.
 */
public class LeftPanel extends JPanel {

    private final AppController ctrl;
    private JPanel warehouseListPanel;
    private JLabel lockStatusLabel;
    private JLabel readersLabel, writerLabel, waitingLabel;

    public LeftPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setOpaque(false);
        setLayout(new BorderLayout(0, 10));
        add(buildWarehouseSection(), BorderLayout.CENTER);
        add(buildLockPanel(),        BorderLayout.SOUTH);
    }

    private JPanel buildWarehouseSection() {
        JPanel section = Theme.darkPanel();
        section.setLayout(new BorderLayout(0, 8));
        section.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(Theme.sectionLabel("WAREHOUSES"), BorderLayout.WEST);
        JButton addBtn = Theme.iconButton("+");
        addBtn.addActionListener(e -> ctrl.onAddWarehouse());
        head.add(addBtn, BorderLayout.EAST);
        section.add(head, BorderLayout.NORTH);

        warehouseListPanel = new JPanel();
        warehouseListPanel.setOpaque(false);
        warehouseListPanel.setLayout(new BoxLayout(warehouseListPanel, BoxLayout.Y_AXIS));
        section.add(warehouseListPanel, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildLockPanel() {
        JPanel panel = Theme.darkPanel();
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));
        panel.setLayout(new BorderLayout(0, 8));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(Theme.sectionLabel("R/W LOCK"), BorderLayout.WEST);
        JLabel shield = new JLabel("🛡");
        shield.setFont(new Font("SansSerif", Font.PLAIN, 14));
        head.add(shield, BorderLayout.EAST);
        panel.add(head, BorderLayout.NORTH);

        lockStatusLabel = new JLabel("● IDLE");
        lockStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        lockStatusLabel.setForeground(Theme.GREEN);
        panel.add(lockStatusLabel, BorderLayout.CENTER);

        JPanel counts = new JPanel(new GridLayout(1, 3, 8, 0));
        counts.setOpaque(false);
        readersLabel = lockStatLabel("0", "READERS");
        writerLabel  = lockStatLabel("0", "WRITER");
        waitingLabel = lockStatLabel("0", "WAITING");
        counts.add(readersLabel);
        counts.add(writerLabel);
        counts.add(waitingLabel);
        panel.add(counts, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel lockStatLabel(String num, String label) {
        return new JLabel(lockHtml(Integer.parseInt(num), label), SwingConstants.CENTER);
    }

    public void refresh(List<WarehouseComposite> composites,
                        WarehouseComposite selected,
                        WarehouseLock lock) {
        refreshWarehouseList(composites, selected);
        refreshLock(lock);
    }

    private void refreshWarehouseList(List<WarehouseComposite> composites,
                                      WarehouseComposite selected) {
        warehouseListPanel.removeAll();
        for (WarehouseComposite wc : composites) {
            warehouseListPanel.add(buildWarehouseCard(wc, wc == selected));
            warehouseListPanel.add(Box.createVerticalStrut(6));
        }
        warehouseListPanel.revalidate();
        warehouseListPanel.repaint();
    }

    private JPanel buildWarehouseCard(WarehouseComposite wc, boolean selected) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(selected ? Theme.CARD_SEL : Theme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(selected ? Theme.ACCENT : Theme.BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel nameRow = new JPanel(new BorderLayout());
        nameRow.setOpaque(false);
        JLabel name = new JLabel(wc.getName());
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        name.setForeground(selected ? Theme.ACCENT : Theme.TEXT_PRI);
        JLabel icon = new JLabel("⬡");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 16));
        icon.setForeground(selected ? Theme.ACCENT : Theme.TEXT_DIM);
        nameRow.add(name, BorderLayout.WEST);
        nameRow.add(icon, BorderLayout.EAST);
        card.add(nameRow, BorderLayout.NORTH);

        JLabel loc = new JLabel(wc.getLocation());
        loc.setFont(Theme.FONT_SMALL);
        loc.setForeground(Theme.TEXT_SEC);
        card.add(loc, BorderLayout.CENTER);

        JPanel stats = new JPanel(new BorderLayout());
        stats.setOpaque(false);
        JLabel vCount = new JLabel("🚚 " + wc.getVehicles().size()
            + "   📦 " + wc.getStagedOrderCount() + " staged");
        vCount.setFont(Theme.FONT_SMALL);
        vCount.setForeground(Theme.TEXT_SEC);
        JLabel weight = new JLabel(String.format("%.1fkg", wc.getTotalWeightKg()));
        weight.setFont(new Font("SansSerif", Font.BOLD, 11));
        weight.setForeground(Theme.TEXT_PRI);
        stats.add(vCount, BorderLayout.WEST);
        stats.add(weight, BorderLayout.EAST);

        JButton scan = Theme.smallTextButton("👁 Read scan");
        scan.addActionListener(e -> ctrl.onReadScan(wc));
        JPanel scanRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        scanRow.setOpaque(false);
        scanRow.add(scan);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(stats,   BorderLayout.NORTH);
        south.add(scanRow, BorderLayout.SOUTH);
        card.add(south, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { ctrl.onSelectComposite(wc); }
        });
        return card;
    }

    private void refreshLock(WarehouseLock lock) {
        boolean idle = lock.isIdle();
        lockStatusLabel.setText(idle ? "● IDLE" : "● ACTIVE");
        lockStatusLabel.setForeground(idle ? Theme.GREEN : Theme.ORANGE);
        readersLabel.setText(lockHtml(lock.getReaderCount(),  "READERS"));
        writerLabel.setText(lockHtml(lock.getWriterCount(),   "WRITER"));
        waitingLabel.setText(lockHtml(lock.getWaitingCount(), "WAITING"));
    }

    private String lockHtml(int n, String label) {
        return "<html><div style='text-align:center'>"
            + "<span style='font-size:18px;color:#F0F0F8;font-weight:bold'>" + n + "</span>"
            + "<br><span style='font-size:9px;color:#8A8EA8'>" + label + "</span>"
            + "</div></html>";
    }
}
