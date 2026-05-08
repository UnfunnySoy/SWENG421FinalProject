import model.*;
import ui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class Main extends JFrame implements AppController {
    private final Warehouse          appWarehouse;
    private       WarehouseComposite selectedComposite;

    private final LeftPanel      leftPanel;
    private final CompositePanel compositePanel;
    private final FeedPanel      feedPanel;

    private final javax.swing.Timer refreshTimer;

    public Main() {
        super("model.Warehouse Storage Manager");
        appWarehouse = SampleData.build();
        selectedComposite = appWarehouse.getElements().isEmpty()
                ? null : appWarehouse.getElements().get(0);

        leftPanel      = new LeftPanel(this);
        compositePanel = new CompositePanel(this);
        feedPanel      = new FeedPanel();

        buildFrame();
        refreshTimer = new javax.swing.Timer(800, e -> refresh());
        refreshTimer.start();
        refresh();
    }

    // ── AppController ─────────────────────────────────────────
    @Override public void onSelectComposite(WarehouseComposite wc) {
        selectedComposite = wc;
        appWarehouse.readScan(wc);
        refresh();
    }
    @Override public void onReadScan(WarehouseComposite wc) {
        appWarehouse.readScan(wc); refresh();
    }
    @Override public void onAddWarehouse() {
        Dialogs.showAddWarehouse(this, this, appWarehouse); refresh();
    }
    @Override public void onAddVehicle(WarehouseComposite wc) {
        Dialogs.showAddVehicle(this, this, appWarehouse, wc); refresh();
    }
    @Override public void onAddOrder(WarehouseComposite wc) {
        Dialogs.showAddOrder(this, this, appWarehouse, wc); refresh();
    }
    @Override public void onMoveOrder(Order o, WarehouseComposite wc) {
        Dialogs.showMoveOrder(this, this, appWarehouse, o, wc); refresh();
    }
    @Override public void onDeliverOrder(Order o, WarehouseComposite wc) {
        appWarehouse.writeOperation(() -> wc.deliverOrder(o), wc,
                "Deliver order " + o.getOrderId());
        refresh();
    }
    @Override public void onRemoveOrder(Order o, WarehouseComposite wc) {
        appWarehouse.writeOperation(() -> wc.removeOrder(o), wc,
                "Remove order " + o.getOrderId());
        refresh();
    }
    @Override public void onVehicleStatusChange(Vehicle v, int newStatus, WarehouseComposite wc) {
        appWarehouse.writeOperation(() -> v.setStatus(newStatus), wc,
                v.getId() + " → " + v.statusLabel());
        refresh();
    }
    @Override public void onMarkPrepared(Order o) { o.markPrepared(); refresh(); }
    @Override public JFrame getOwnerFrame() { return this; }

    // ── Refresh ───────────────────────────────────────────────
    private void refresh() {
        leftPanel.refresh(appWarehouse.getElements(), selectedComposite, appWarehouse.getLock());
        compositePanel.refresh(selectedComposite);
        feedPanel.refresh(appWarehouse.getEventLog());
        revalidate();
        repaint();
    }

    // ── Frame construction ────────────────────────────────────
    private void buildFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1400, 860));
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PANEL_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(10, 18, 10, 18)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel("⬡");
        logo.setFont(new Font("SansSerif", Font.BOLD, 26));
        logo.setForeground(Theme.ACCENT);
        JLabel title = new JLabel("model.Warehouse Storage Manager");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRI);
        JLabel sub = new JLabel("  Inventory · Vehicles · Orders");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SEC);
        left.add(logo); left.add(title); left.add(sub);
        JLabel ver = new JLabel("v1.0");
        ver.setFont(Theme.FONT_BADGE);
        ver.setForeground(Theme.TEXT_SEC);
        ver.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
        bar.add(left, BorderLayout.WEST);
        bar.add(ver,  BorderLayout.EAST);
        return bar;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(Theme.BG);
        main.setBorder(new EmptyBorder(14, 14, 14, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH; g.weighty = 1.0;
        g.gridx = 0; g.weightx = 0.22; g.insets = new Insets(0, 0, 0, 10);
        main.add(leftPanel, g);
        g.gridx = 1; g.weightx = 0.50; g.insets = new Insets(0, 0, 0, 10);
        main.add(compositePanel, g);
        g.gridx = 2; g.weightx = 0.28; g.insets = new Insets(0, 0, 0, 0);
        main.add(feedPanel, g);
        return main;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(Main::new);
    }
}
