import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Main – Swing UI for Warehouse Storage Manager
 * Dark industrial theme matching the reference screenshots.
 * Three-column layout: Warehouses | Composite Detail | Observer Feed
 */
public class Main extends JFrame {

    // ── Palette ──────────────────────────────────────────────
    static final Color BG          = new Color(0x12, 0x14, 0x1A);
    static final Color PANEL_BG    = new Color(0x1A, 0x1D, 0x27);
    static final Color CARD_BG     = new Color(0x1F, 0x22, 0x30);
    static final Color CARD_SEL    = new Color(0x26, 0x2B, 0x3E);
    static final Color ACCENT      = new Color(0xFF, 0xB3, 0x00);
    static final Color ACCENT2     = new Color(0x00, 0xD4, 0xFF);
    static final Color TEXT_PRI    = new Color(0xF0, 0xF0, 0xF8);
    static final Color TEXT_SEC    = new Color(0x8A, 0x8E, 0xA8);
    static final Color TEXT_DIM    = new Color(0x50, 0x55, 0x70);
    static final Color GREEN       = new Color(0x22, 0xC5, 0x5E);
    static final Color ORANGE      = new Color(0xFF, 0xB3, 0x00);
    static final Color RED         = new Color(0xFF, 0x4D, 0x6D);
    static final Color BLUE        = new Color(0x00, 0xD4, 0xFF);
    static final Color BORDER      = new Color(0x2A, 0x2F, 0x45);

    // ── Fonts ────────────────────────────────────────────────
    static Font FONT_TITLE, FONT_MONO, FONT_BODY, FONT_SMALL, FONT_BADGE;
    static {
        FONT_TITLE = new Font("SansSerif", Font.BOLD,  22);
        FONT_MONO  = new Font("Monospaced", Font.PLAIN, 11);
        FONT_BODY  = new Font("SansSerif", Font.PLAIN, 12);
        FONT_SMALL = new Font("SansSerif", Font.PLAIN, 10);
        FONT_BADGE = new Font("SansSerif", Font.BOLD,  9);
    }

    // ── App state ────────────────────────────────────────────
    private Warehouse          appWarehouse;
    private WarehouseComposite selectedComposite;

    // ── UI panels ────────────────────────────────────────────
    private JPanel   warehouseListPanel;
    private JPanel   compositeDetailPanel;
    private JPanel   observerFeedPanel;
    private JScrollPane feedScroll;
    private JLabel   lockStatusLabel;
    private JLabel   readersLabel, writerLabel, waitingLabel;

    // ── Refresh timer ────────────────────────────────────────
    private javax.swing.Timer refreshTimer;

    // ─────────────────────────────────────────────────────────
    public Main() {
        super("Warehouse Storage Manager");
        setupData();
        buildUI();
        startRefresh();
    }

    // ─────────────────────────────────────────────────────────
    //  Sample data bootstrap
    // ─────────────────────────────────────────────────────────
    private void setupData() {
        appWarehouse = new Warehouse("System", "N/A", "N/A");
        Factory f    = appWarehouse.getFactory();

        // ── North Hub ───────────────────────────────────────
        WarehouseComposite northHub = f.getOrCreateComposite("North Hub", "Seattle, WA");
        appWarehouse.add(northHub);

        Vehicle truck01 = f.getOrCreateVehicle("Truck-NH-01", "M. Myers", 5000);
        northHub.addVehicle(truck01);
        truck01.updateStatus(Vehicle.STATUS_LOADING);

        Vehicle van02 = f.getOrCreateVehicle("Van-NH-02", "A. Kuntz", 1500);
        northHub.addVehicle(van02);

        Vehicle carrr = f.getOrCreateVehicle("carrr", "ayoob", 3000);
        northHub.addVehicle(carrr);
        carrr.updateStatus(Vehicle.STATUS_LOADING);

        // Build a retail order with wrapped packages
        Order retailOrder = f.createRetailOrder("FedEx #FX1029", "A. Kuntz", northHub);

        Item monitor  = new Item("Monitor",  6.5);
        Item keyboard = new Item("Keyboard", 1.2);

        Package pkgMon = new Package(monitor,  new ShippingInfo("FedEx", "#FX1029"));
        Package pkgKbd = new Package(keyboard, new ShippingInfo("FedEx", "#FX1029"));

        PackageWrapper wrapper = new PackageWrapper("QVR6UTB");
        wrapper.addPackage(pkgMon);
        wrapper.addPackage(pkgKbd);
        retailOrder.addPackageWrapper(wrapper);

        northHub.addOrder(retailOrder);
        truck01.addOrder(retailOrder);

        // ── Central Depot ───────────────────────────────────
        WarehouseComposite centralDepot = f.getOrCreateComposite("Central Depot", "Dallas, TX");
        appWarehouse.add(centralDepot);

        Vehicle vanD1 = f.getOrCreateVehicle("Van-CD-01", "B. Smith", 2000);
        centralDepot.addVehicle(vanD1);

        selectedComposite = northHub;

        // Trigger a few read scans so the feed looks populated
        for (WarehouseComposite wc : appWarehouse.getElements()) {
            appWarehouse.readScan(wc);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Build UI
    // ─────────────────────────────────────────────────────────
    private void buildUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1400, 860));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildMainArea(),  BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Top bar ──────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(10, 18, 10, 18)
        ));

        // Left: logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("⬡");
        logo.setFont(new Font("SansSerif", Font.BOLD, 26));
        logo.setForeground(ACCENT);
        JLabel title = new JLabel("Warehouse Storage Manager");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRI);
        JLabel sub = new JLabel("  Inventory · Vehicles · Orders");
        sub.setFont(FONT_SMALL);
        sub.setForeground(TEXT_SEC);

        left.add(logo); left.add(title); left.add(sub);

        // Right: version badge
        JLabel ver = new JLabel("v1.0");
        ver.setFont(FONT_BADGE);
        ver.setForeground(TEXT_SEC);
        ver.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(3, 8, 3, 8)
        ));

        bar.add(left, BorderLayout.WEST);
        bar.add(ver,  BorderLayout.EAST);
        return bar;
    }

    // ── Main three-column layout ─────────────────────────────
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(14, 14, 14, 14));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.BOTH;
        g.insets  = new Insets(0, 0, 0, 10);
        g.weighty = 1.0;

        // Left column: warehouses + lock
        g.gridx = 0; g.weightx = 0.22;
        main.add(buildLeftColumn(), g);

        // Center: composite detail
        g.gridx = 1; g.weightx = 0.50; g.insets = new Insets(0, 0, 0, 10);
        compositeDetailPanel = new JPanel(new BorderLayout(0, 10));
        compositeDetailPanel.setOpaque(false);
        refreshCompositeDetail();
        main.add(compositeDetailPanel, g);

        // Right: observer feed
        g.gridx = 2; g.weightx = 0.28; g.insets = new Insets(0, 0, 0, 0);
        main.add(buildFeedColumn(), g);

        return main;
    }

    // ── Left column ──────────────────────────────────────────
    private JPanel buildLeftColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 10));
        col.setOpaque(false);

        // Warehouse list
        JPanel warehouseSection = darkPanel();
        warehouseSection.setLayout(new BorderLayout(0, 8));
        warehouseSection.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel wHead = new JPanel(new BorderLayout());
        wHead.setOpaque(false);
        JLabel wLabel = sectionLabel("WAREHOUSES");
        JButton addWH = iconButton("+");
        addWH.addActionListener(e -> showAddWarehouseDialog());
        wHead.add(wLabel, BorderLayout.WEST);
        wHead.add(addWH,  BorderLayout.EAST);
        warehouseSection.add(wHead, BorderLayout.NORTH);

        warehouseListPanel = new JPanel();
        warehouseListPanel.setOpaque(false);
        warehouseListPanel.setLayout(new BoxLayout(warehouseListPanel, BoxLayout.Y_AXIS));
        warehouseSection.add(warehouseListPanel, BorderLayout.CENTER);

        // Lock panel
        JPanel lockSection = buildLockPanel();

        col.add(warehouseSection, BorderLayout.CENTER);
        col.add(lockSection,      BorderLayout.SOUTH);
        return col;
    }

    private JPanel buildLockPanel() {
        JPanel panel = darkPanel();
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));
        panel.setLayout(new BorderLayout(0, 8));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(sectionLabel("R/W LOCK"), BorderLayout.WEST);
        JLabel shieldIcon = new JLabel("🛡");
        shieldIcon.setFont(new Font("SansSerif", Font.PLAIN, 14));
        head.add(shieldIcon, BorderLayout.EAST);
        panel.add(head, BorderLayout.NORTH);

        lockStatusLabel = new JLabel("● IDLE");
        lockStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        lockStatusLabel.setForeground(GREEN);
        panel.add(lockStatusLabel, BorderLayout.CENTER);

        JPanel counts = new JPanel(new GridLayout(1, 3, 8, 0));
        counts.setOpaque(false);
        readersLabel  = lockStatLabel("0", "READERS");
        writerLabel   = lockStatLabel("0", "WRITER");
        waitingLabel  = lockStatLabel("0", "WAITING");
        counts.add(readersLabel); counts.add(writerLabel); counts.add(waitingLabel);
        panel.add(counts, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel lockStatLabel(String num, String label) {
        JLabel l = new JLabel("<html><div style='text-align:center'>"
            + "<span style='font-size:18px;color:#F0F0F8;font-weight:bold'>" + num + "</span>"
            + "<br><span style='font-size:9px;color:#8A8EA8'>" + label + "</span></div></html>",
            SwingConstants.CENTER);
        return l;
    }

    // ── Feed column ──────────────────────────────────────────
    private JPanel buildFeedColumn() {
        JPanel col = darkPanel();
        col.setBorder(new EmptyBorder(12, 12, 12, 12));
        col.setLayout(new BorderLayout(0, 8));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(sectionLabel("OBSERVER FEED"), BorderLayout.WEST);
        JLabel pulse = new JLabel("↯");
        pulse.setFont(new Font("SansSerif", Font.BOLD, 16));
        pulse.setForeground(ACCENT2);
        head.add(pulse, BorderLayout.EAST);
        col.add(head, BorderLayout.NORTH);

        observerFeedPanel = new JPanel();
        observerFeedPanel.setOpaque(false);
        observerFeedPanel.setLayout(new BoxLayout(observerFeedPanel, BoxLayout.Y_AXIS));

        feedScroll = new JScrollPane(observerFeedPanel);
        feedScroll.setOpaque(false);
        feedScroll.getViewport().setOpaque(false);
        feedScroll.setBorder(null);
        feedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(feedScroll.getVerticalScrollBar());

        col.add(feedScroll, BorderLayout.CENTER);
        return col;
    }

    // ─────────────────────────────────────────────────────────
    //  Refresh logic
    // ─────────────────────────────────────────────────────────
    private void startRefresh() {
        refreshTimer = new javax.swing.Timer(800, e -> refresh());
        refreshTimer.start();
    }

    private void refresh() {
        refreshWarehouseList();
        refreshCompositeDetail();
        refreshFeed();
        refreshLockPanel();
        revalidate();
        repaint();
    }

    private void refreshWarehouseList() {
        warehouseListPanel.removeAll();
        for (WarehouseComposite wc : appWarehouse.getElements()) {
            warehouseListPanel.add(buildWarehouseCard(wc));
            warehouseListPanel.add(Box.createVerticalStrut(6));
        }
    }

    private void refreshCompositeDetail() {
        compositeDetailPanel.removeAll();
        if (selectedComposite != null) {
            compositeDetailPanel.add(buildCompositeHeader(selectedComposite), BorderLayout.NORTH);
            JPanel content = buildContentArea(selectedComposite);
            JScrollPane scroll = new JScrollPane(content);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            styleScrollBar(scroll.getVerticalScrollBar());
            compositeDetailPanel.add(scroll, BorderLayout.CENTER);
        }
    }

    private void refreshFeed() {
        observerFeedPanel.removeAll();
        List<String[]> log = appWarehouse.getEventLog();
        int max = Math.min(log.size(), 40);
        for (int i = 0; i < max; i++) {
            String[] entry = log.get(i);
            observerFeedPanel.add(buildFeedEntry(entry[0], entry[1], entry[2]));
            observerFeedPanel.add(Box.createVerticalStrut(2));
        }
        observerFeedPanel.revalidate();
    }

    private void refreshLockPanel() {
        WarehouseLock lock = appWarehouse.getLock();
        boolean idle = lock.isIdle();
        lockStatusLabel.setText(idle ? "● IDLE" : "● ACTIVE");
        lockStatusLabel.setForeground(idle ? GREEN : ORANGE);

        readersLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:18px;color:#F0F0F8;font-weight:bold'>" + lock.getReaderCount() + "</span>"
            + "<br><span style='font-size:9px;color:#8A8EA8'>READERS</span></div></html>");
        writerLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:18px;color:#F0F0F8;font-weight:bold'>" + lock.getWriterCount() + "</span>"
            + "<br><span style='font-size:9px;color:#8A8EA8'>WRITER</span></div></html>");
        waitingLabel.setText("<html><div style='text-align:center'>"
            + "<span style='font-size:18px;color:#F0F0F8;font-weight:bold'>" + lock.getWaitingCount() + "</span>"
            + "<br><span style='font-size:9px;color:#8A8EA8'>WAITING</span></div></html>");
    }

    // ─────────────────────────────────────────────────────────
    //  Warehouse card (left column)
    // ─────────────────────────────────────────────────────────
    private JPanel buildWarehouseCard(WarehouseComposite wc) {
        boolean selected = wc == selectedComposite;
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(selected ? CARD_SEL : CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(selected ? ACCENT : BORDER, selected ? 1 : 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Name row
        JPanel nameRow = new JPanel(new BorderLayout());
        nameRow.setOpaque(false);
        JLabel name = new JLabel(wc.getName());
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        name.setForeground(selected ? ACCENT : TEXT_PRI);
        JLabel whIcon = new JLabel("⬡");
        whIcon.setFont(new Font("SansSerif", Font.PLAIN, 16));
        whIcon.setForeground(selected ? ACCENT : TEXT_DIM);
        nameRow.add(name,   BorderLayout.WEST);
        nameRow.add(whIcon, BorderLayout.EAST);
        card.add(nameRow, BorderLayout.NORTH);

        // Location
        JLabel loc = new JLabel(wc.getLocation());
        loc.setFont(FONT_SMALL);
        loc.setForeground(TEXT_SEC);
        card.add(loc, BorderLayout.CENTER);

        // Stats row
        JPanel stats = new JPanel(new BorderLayout());
        stats.setOpaque(false);
        JLabel vCount = new JLabel("🚚 " + wc.getVehicles().size() + "   📦 " + wc.getStagedOrderCount() + " staged");
        vCount.setFont(FONT_SMALL);
        vCount.setForeground(TEXT_SEC);
        JLabel weight = new JLabel(String.format("%.1fkg", wc.getTotalWeightKg()));
        weight.setFont(new Font("SansSerif", Font.BOLD, 11));
        weight.setForeground(TEXT_PRI);
        stats.add(vCount, BorderLayout.WEST);
        stats.add(weight, BorderLayout.EAST);
        card.add(stats, BorderLayout.SOUTH);

        // Click to select + read scan
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedComposite = wc;
                appWarehouse.readScan(wc);
                refresh();
            }
        });

        // Read scan button
        JButton scan = smallTextButton("👁 Read scan");
        scan.addActionListener(e -> {
            appWarehouse.readScan(wc);
            refresh();
        });
        JPanel scanRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        scanRow.setOpaque(false);
        scanRow.add(scan);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(stats,   BorderLayout.NORTH);
        south.add(scanRow, BorderLayout.SOUTH);
        card.add(south, BorderLayout.SOUTH);

        return card;
    }

    // ─────────────────────────────────────────────────────────
    //  Composite header (center top)
    // ─────────────────────────────────────────────────────────
    private JPanel buildCompositeHeader(WarehouseComposite wc) {
        JPanel panel = darkPanel();
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        panel.setLayout(new BorderLayout(0, 10));

        // Top: label + buttons
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel rootLabel = new JLabel("COMPOSITE ROOT");
        rootLabel.setFont(FONT_SMALL);
        rootLabel.setForeground(TEXT_DIM);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        JButton addV = accentButton("🚚 Add Vehicle");
        JButton addO = filledButton("📦 Add Order");
        addV.addActionListener(e -> showAddVehicleDialog(wc));
        addO.addActionListener(e -> showAddOrderDialog(wc));
        btnRow.add(addV); btnRow.add(addO);

        top.add(rootLabel, BorderLayout.WEST);
        top.add(btnRow,    BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        // Name + location
        JPanel nameBlock = new JPanel(new BorderLayout());
        nameBlock.setOpaque(false);
        JLabel nameL = new JLabel(wc.getName());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 26));
        nameL.setForeground(TEXT_PRI);
        JLabel locL  = new JLabel(wc.getLocation());
        locL.setFont(FONT_BODY);
        locL.setForeground(TEXT_SEC);
        nameBlock.add(nameL, BorderLayout.NORTH);
        nameBlock.add(locL,  BorderLayout.CENTER);
        panel.add(nameBlock, BorderLayout.CENTER);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 10, 0));
        statsRow.setOpaque(false);
        statsRow.add(statTile("TOTAL WEIGHT",
            String.format("%.1f kg", wc.getTotalWeightKg())));
        statsRow.add(statTile("ACTIVE VEHICLES",
            String.valueOf(wc.getActiveVehicleCount())));
        statsRow.add(statTile("STAGED ORDERS",
            String.valueOf(wc.getStagedOrderCount())));
        panel.add(statsRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel statTile(String label, String value) {
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

    // ─────────────────────────────────────────────────────────
    //  Content area: Vehicles + Staged Orders + Unassigned Orders
    // ─────────────────────────────────────────────────────────
    private JPanel buildContentArea(WarehouseComposite wc) {
        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

        // ── Vehicles section ──
        outer.add(sectionDivider("🚚  Vehicles", "COMPOSITE + OBSERVER"));
        outer.add(Box.createVerticalStrut(8));

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 10));
        grid.setOpaque(false);
        for (Vehicle v : wc.getVehicles()) {
            grid.add(buildVehicleCard(v, wc));
        }
        if (wc.getVehicles().isEmpty()) {
            grid.add(emptyNotice("No vehicles docked. Use 'Add Vehicle' above."));
        }
        outer.add(grid);
        outer.add(Box.createVerticalStrut(18));

        // ── Staged Orders section (PREPARED, ready to dispatch) ──
        List<Order> staged = new ArrayList<>();
        for (Order o : wc.getOrders()) {
            if (o.checkStatus() == Order.STATUS_PREPARED) staged.add(o);
        }
        // Also pick up prepared orders sitting on vehicles
        for (Vehicle v : wc.getVehicles()) {
            for (Order o : v.getOrders()) {
                if (o.checkStatus() == Order.STATUS_PREPARED && !staged.contains(o)) staged.add(o);
            }
        }

        outer.add(sectionDivider("📦  Staged Orders", "READY TO DELIVER  —  " + staged.size()));
        outer.add(Box.createVerticalStrut(8));

        if (staged.isEmpty()) {
            outer.add(emptyNotice("No orders staged yet. Mark an order as Prepared to stage it."));
        } else {
            JPanel stagedGrid = new JPanel(new GridLayout(0, 2, 10, 8));
            stagedGrid.setOpaque(false);
            for (Order o : staged) {
                stagedGrid.add(buildStagedOrderCard(o, wc));
            }
            outer.add(stagedGrid);
        }
        outer.add(Box.createVerticalStrut(18));

        // ── Unassigned Orders section (PENDING, not on any vehicle) ──
        List<Order> unassigned = new ArrayList<>();
        Set<Order> onVehicle = new HashSet<>();
        for (Vehicle v : wc.getVehicles()) onVehicle.addAll(v.getOrders());
        for (Order o : wc.getOrders()) {
            if (!onVehicle.contains(o) && o.checkStatus() != Order.STATUS_SHIPPED) {
                unassigned.add(o);
            }
        }

        outer.add(sectionDivider("📋  Unassigned Orders", "PENDING — NOT ON A VEHICLE  —  " + unassigned.size()));
        outer.add(Box.createVerticalStrut(8));

        if (unassigned.isEmpty()) {
            outer.add(emptyNotice("All orders are assigned to vehicles."));
        } else {
            JPanel uGrid = new JPanel(new GridLayout(0, 2, 10, 8));
            uGrid.setOpaque(false);
            for (Order o : unassigned) {
                uGrid.add(buildUnassignedOrderCard(o, wc));
            }
            outer.add(uGrid);
        }
        outer.add(Box.createVerticalStrut(14));

        return outer;
    }

    private JPanel sectionDivider(String title, String meta) {
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

    private JPanel emptyNotice(String msg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel l = new JLabel(msg);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_DIM);
        p.add(l);
        return p;
    }

    /** Card for a staged (prepared) order — shows full detail + Deliver button */
    private JPanel buildStagedOrderCard(Order o, WarehouseComposite wc) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(0x16, 0x1E, 0x18)); // subtle green tint
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x22, 0x55, 0x33), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Header row
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        String typeLabel = o.getOrderType() == Order.TYPE_RETAIL ? "Retail" : "Wholesale";
        JLabel nameL = new JLabel(typeLabel + " #" + o.getOrderId());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(GREEN);

        JLabel badge = new JLabel("STAGED");
        badge.setFont(FONT_BADGE);
        badge.setForeground(Color.BLACK);
        badge.setBackground(GREEN);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        head.add(nameL, BorderLayout.WEST);
        head.add(badge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Details
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        if (!o.getDeliveryLocation().isEmpty()) {
            JLabel dest = new JLabel("→ " + o.getDeliveryLocation());
            dest.setFont(FONT_SMALL);
            dest.setForeground(TEXT_SEC);
            details.add(dest);
        }
        if (!o.getDeliveryPerson().isEmpty()) {
            JLabel person = new JLabel("Driver: " + o.getDeliveryPerson());
            person.setFont(FONT_SMALL);
            person.setForeground(TEXT_DIM);
            details.add(person);
        }

        // Vehicle assignment
        String vLabel = "Unassigned";
        for (Vehicle v : wc.getVehicles()) {
            if (v.getOrders().contains(o)) { vLabel = "On " + v.getVehicleId(); break; }
        }
        JLabel veh = new JLabel("Vehicle: " + vLabel);
        veh.setFont(FONT_SMALL);
        veh.setForeground(TEXT_DIM);
        details.add(veh);

        // Items summary
        double totalWt = o.getTotalWeight();
        int itemCount  = 0;
        for (PackageWrapper pw : o.getPackages()) itemCount += pw.getPackages().size();
        JLabel items = new JLabel(itemCount + " item" + (itemCount != 1 ? "s" : "") +
                                  "  —  " + String.format("%.1f kg", totalWt));
        items.setFont(FONT_SMALL);
        items.setForeground(TEXT_SEC);
        details.add(Box.createVerticalStrut(4));
        details.add(items);

        card.add(details, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);

        JButton deliverBtn = new JButton("✈ Deliver");
        deliverBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        deliverBtn.setBackground(GREEN);
        deliverBtn.setForeground(Color.BLACK);
        deliverBtn.setBorderPainted(false);
        deliverBtn.setFocusPainted(false);
        deliverBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        deliverBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deliverBtn.addActionListener(e -> {
            appWarehouse.writeOperation(
                () -> wc.deliverOrder(o),
                wc, "Deliver order " + o.getOrderId()
            );
            refresh();
        });

        JButton moveBtn = smallTextButton("⇄ Move");
        moveBtn.setForeground(ACCENT2);
        moveBtn.addActionListener(e -> showMoveOrderDialog(o, wc));

        btnRow.add(deliverBtn);
        btnRow.add(moveBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    /** Card for an unassigned (pending) order — shows assign + stage actions */
    private JPanel buildUnassignedOrderCard(Order o, WarehouseComposite wc) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        String typeLabel = o.getOrderType() == Order.TYPE_RETAIL ? "Retail" : "Wholesale";
        JLabel nameL = new JLabel(typeLabel + " #" + o.getOrderId());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(TEXT_PRI);

        JLabel badge = new JLabel("PENDING");
        badge.setFont(FONT_BADGE);
        badge.setForeground(Color.BLACK);
        badge.setBackground(ORANGE);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        head.add(nameL, BorderLayout.WEST);
        head.add(badge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Details
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        if (!o.getDeliveryLocation().isEmpty()) {
            JLabel dest = new JLabel("→ " + o.getDeliveryLocation());
            dest.setFont(FONT_SMALL);
            dest.setForeground(TEXT_SEC);
            details.add(dest);
        }

        double totalWt = o.getTotalWeight();
        int itemCount  = 0;
        for (PackageWrapper pw : o.getPackages()) itemCount += pw.getPackages().size();
        JLabel items = new JLabel(itemCount + " item" + (itemCount != 1 ? "s" : "") +
                                  "  —  " + String.format("%.1f kg", totalWt));
        items.setFont(FONT_SMALL);
        items.setForeground(TEXT_SEC);
        details.add(Box.createVerticalStrut(4));
        details.add(items);

        card.add(details, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);

        JButton stageBtn = new JButton("✓ Mark Prepared");
        stageBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        stageBtn.setBackground(new Color(0x1A, 0x40, 0x28));
        stageBtn.setForeground(GREEN);
        stageBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x22, 0x55, 0x33), 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        stageBtn.setFocusPainted(false);
        stageBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stageBtn.addActionListener(e -> { o.markPrepared(); refresh(); });

        JButton moveBtn = smallTextButton("⇄ Assign Vehicle");
        moveBtn.setForeground(ACCENT2);
        moveBtn.addActionListener(e -> showMoveOrderDialog(o, wc));

        JButton deleteBtn = smallTextButton("✕ Remove");
        deleteBtn.setForeground(RED);
        deleteBtn.addActionListener(e -> {
            appWarehouse.writeOperation(
                () -> wc.removeOrder(o),
                wc, "Remove order " + o.getOrderId()
            );
            refresh();
        });

        btnRow.add(stageBtn);
        btnRow.add(moveBtn);
        btnRow.add(deleteBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildVehicleCard(Vehicle v, WarehouseComposite wc) {
        JPanel card = darkPanel();
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        card.setLayout(new BorderLayout(0, 8));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel idLabel = new JLabel("🚚 " + v.getVehicleId());
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        idLabel.setForeground(ACCENT);
        JLabel statusBadge = statusBadge(v.statusLabel());
        head.add(idLabel,     BorderLayout.WEST);
        head.add(statusBadge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        // Driver
        JLabel driver = new JLabel("Driver: " + v.getDriverName());
        driver.setFont(FONT_SMALL);
        driver.setForeground(TEXT_SEC);
        card.add(driver, BorderLayout.CENTER);

        // Content panel
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Capacity bar
        JPanel capRow = new JPanel(new BorderLayout(6, 0));
        capRow.setOpaque(false);
        JLabel capLabel = new JLabel("Capacity");
        capLabel.setFont(FONT_SMALL);
        capLabel.setForeground(TEXT_DIM);
        double loaded = v.getLoadedWeightKg();
        double cap    = v.getCapacityKg();
        JLabel capVal = new JLabel(String.format("%.1f / %.0f kg", loaded, cap));
        capVal.setFont(FONT_SMALL);
        capVal.setForeground(TEXT_PRI);
        capRow.add(capLabel, BorderLayout.WEST);
        capRow.add(capVal,   BorderLayout.EAST);
        content.add(capRow);
        content.add(Box.createVerticalStrut(4));
        content.add(capacityBar(loaded, cap));
        content.add(Box.createVerticalStrut(8));

        // Orders
        for (Order o : v.getOrders()) {
            content.add(buildOrderEntry(o, wc));
            content.add(Box.createVerticalStrut(4));
        }
        if (v.getOrders().isEmpty()) {
            JLabel empty = new JLabel("empty");
            empty.setFont(FONT_SMALL);
            empty.setForeground(TEXT_DIM);
            content.add(empty);
        }
        content.add(Box.createVerticalStrut(8));

        // Status buttons
        JPanel btnRow = buildStatusButtons(v, wc);
        content.add(btnRow);

        card.add(content, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildStatusButtons(Vehicle v, WarehouseComposite wc) {
        JPanel row = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 3));
        row.setOpaque(false);
        int cur = v.checkStatus();
        row.add(statusToggleButton("Docked",      Vehicle.STATUS_DOCKED,      cur, v, wc));
        row.add(statusToggleButton("Loading",     Vehicle.STATUS_LOADING,     cur, v, wc));
        row.add(statusToggleButton("Departed",    Vehicle.STATUS_DEPARTED,    cur, v, wc));
        row.add(statusToggleButton("Maintenance", Vehicle.STATUS_MAINTENANCE, cur, v, wc));
        return row;
    }

    private JButton statusToggleButton(String label, int status, int current,
                                        Vehicle v, WarehouseComposite wc) {
        boolean active = (status == current);
        JButton btn = new JButton(label.toUpperCase());
        btn.setFont(new Font("SansSerif", Font.BOLD, 9));
        btn.setMargin(new Insets(3, 7, 3, 7));

        Color activeBg = status == Vehicle.STATUS_LOADING     ? ORANGE
                       : status == Vehicle.STATUS_DEPARTED    ? BLUE
                       : status == Vehicle.STATUS_MAINTENANCE ? RED
                       : GREEN;
        btn.setBackground(active ? activeBg : CARD_BG);
        btn.setForeground(active ? (activeBg == ORANGE ? Color.BLACK : Color.WHITE) : TEXT_DIM);
        btn.setBorderPainted(!active);
        btn.setBorder(active ? new EmptyBorder(3, 7, 3, 7)
                             : BorderFactory.createCompoundBorder(
                                 new LineBorder(BORDER, 1, true),
                                 new EmptyBorder(2, 6, 2, 6)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            appWarehouse.writeOperation(
                () -> v.updateStatus(status),
                wc,
                v.getVehicleId() + " → " + label.toUpperCase()
            );
            refresh();
        });
        return btn;
    }

    private JPanel buildOrderEntry(Order o, WarehouseComposite wc) {
        JPanel p = new JPanel(new BorderLayout(6, 2));
        p.setBackground(new Color(0x14, 0x17, 0x22));
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        String typeLabel = o.getOrderType() == Order.TYPE_RETAIL ? "Retail Order" : "Wholesale Order";
        JLabel nameL = new JLabel(typeLabel + " #" + o.getOrderId());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 11));
        nameL.setForeground(TEXT_PRI);
        JLabel wt = new JLabel(String.format("%.1fkg", o.getTotalWeight()));
        wt.setFont(FONT_SMALL);
        wt.setForeground(TEXT_SEC);
        top.add(nameL, BorderLayout.WEST);
        top.add(wt,    BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // Packages
        JPanel pkgList = new JPanel();
        pkgList.setOpaque(false);
        pkgList.setLayout(new BoxLayout(pkgList, BoxLayout.Y_AXIS));
        for (PackageWrapper pw : o.getPackages()) {
            for (Package pkg : pw.getPackages()) {
                JLabel pkgL = new JLabel("  📦 " + pkg.getItem().getName()
                    + " (" + pkg.getItem().getWeight() + "kg)");
                pkgL.setFont(FONT_SMALL);
                pkgL.setForeground(TEXT_SEC);
                pkgList.add(pkgL);
            }
            if (pw.getShippingInfo() != null) {
                JLabel ship = new JLabel("  ↪ " + pw.getShippingInfo().toString());
                ship.setFont(FONT_SMALL);
                ship.setForeground(TEXT_DIM);
                pkgList.add(ship);
            }
        }
        p.add(pkgList, BorderLayout.CENTER);

        // Action buttons row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        actionRow.setOpaque(false);

        if (o.checkStatus() == Order.STATUS_PENDING) {
            JButton prep = smallTextButton("✓ Mark Prepared");
            prep.setForeground(GREEN);
            prep.addActionListener(e -> {
                o.markPrepared();
                refresh();
            });
            actionRow.add(prep);
        }

        // "Move to Vehicle" button — shows a picker of vehicles in the composite
        JButton moveBtn = smallTextButton("⇄ Move to Vehicle");
        moveBtn.setForeground(ACCENT2);
        moveBtn.addActionListener(e -> showMoveOrderDialog(o, wc));
        actionRow.add(moveBtn);

        p.add(actionRow, BorderLayout.SOUTH);

        return p;
    }

    // ─────────────────────────────────────────────────────────
    //  Observer feed entry
    // ─────────────────────────────────────────────────────────
    private JPanel buildFeedEntry(String time, String source, String message) {
        JPanel entry = new JPanel(new BorderLayout(6, 2));
        entry.setOpaque(false);
        entry.setBorder(new EmptyBorder(5, 6, 5, 6));
        entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Icon
        boolean isWrite = message.startsWith("Write") || message.contains("docked")
                        || message.contains("added") || message.contains("registered");
        boolean isRead  = message.startsWith("Read lock");
        JLabel icon = new JLabel(isWrite ? "●" : isRead ? "ℹ" : "●");
        icon.setFont(new Font("SansSerif", Font.BOLD, 10));
        icon.setForeground(isWrite ? GREEN : isRead ? BLUE : ORANGE);
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel right = new JPanel(new BorderLayout(0, 1));
        right.setOpaque(false);
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel timeL = new JLabel(time);
        timeL.setFont(FONT_SMALL);
        timeL.setForeground(TEXT_DIM);
        JLabel srcL = new JLabel(source);
        srcL.setFont(new Font("SansSerif", Font.BOLD, 10));
        srcL.setForeground(ACCENT);
        topRow.add(timeL, BorderLayout.WEST);
        topRow.add(srcL,  BorderLayout.EAST);

        JLabel msgL = new JLabel("<html><body style='width:160px'>" + message + "</body></html>");
        msgL.setFont(FONT_SMALL);
        msgL.setForeground(TEXT_SEC);

        right.add(topRow, BorderLayout.NORTH);
        right.add(msgL,   BorderLayout.CENTER);

        entry.add(icon,  BorderLayout.WEST);
        entry.add(right, BorderLayout.CENTER);

        MatteBorder bottom = new MatteBorder(0, 0, 1, 0, BORDER);
        entry.setBorder(BorderFactory.createCompoundBorder(bottom, new EmptyBorder(5, 6, 5, 6)));
        return entry;
    }

    // ─────────────────────────────────────────────────────────
    //  Dialogs
    // ─────────────────────────────────────────────────────────
    private void showMoveOrderDialog(Order o, WarehouseComposite wc) {
        List<Vehicle> vehicles = wc.getVehicles();
        if (vehicles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No vehicles in this composite to assign to.",
                "No Vehicles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog d = styledDialog("Move Order " + o.getOrderId());
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BG);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel info = new JLabel("<html><b style='color:#F0F0F8'>" + o + "</b>"
            + "<br><span style='color:#8A8EA8'>Weight: " + String.format("%.1f", o.getTotalWeight()) + " kg</span></html>");
        info.setFont(FONT_BODY);
        panel.add(info, BorderLayout.NORTH);

        // Vehicle list with radio-style buttons
        JPanel vList = new JPanel();
        vList.setBackground(PANEL_BG);
        vList.setLayout(new BoxLayout(vList, BoxLayout.Y_AXIS));
        ButtonGroup bg = new ButtonGroup();

        // "Unassign" option
        JRadioButton unassign = styledRadio("— Remove from all vehicles (unassign) —", null, bg);
        vList.add(unassign);
        vList.add(Box.createVerticalStrut(4));

        Map<JRadioButton, Vehicle> radioToVehicle = new LinkedHashMap<>();
        Vehicle currentVehicle = null;
        for (Vehicle v : vehicles) {
            // detect current assignment
            boolean isCurrent = v.getOrders().contains(o);
            if (isCurrent) currentVehicle = v;
            double avail = v.getCapacityKg() - v.getLoadedWeightKg();
            boolean fits = avail >= o.getTotalWeight() || isCurrent;
            String label = v.getVehicleId() + "  —  Driver: " + v.getDriverName()
                + "   " + String.format("%.1f / %.0f kg", v.getLoadedWeightKg(), v.getCapacityKg())
                + (isCurrent ? "  ← current" : fits ? "" : "  ⚠ over capacity");
            JRadioButton rb = styledRadio(label, isCurrent ? GREEN : fits ? TEXT_PRI : TEXT_SEC, bg);
            if (isCurrent) rb.setSelected(true);
            if (!fits && !isCurrent) rb.setEnabled(false);
            radioToVehicle.put(rb, v);
            vList.add(rb);
            vList.add(Box.createVerticalStrut(4));
        }
        if (currentVehicle == null) unassign.setSelected(true);

        panel.add(vList, BorderLayout.CENTER);

        JButton ok = filledButton("Assign");
        final Vehicle finalCurrentVehicle = currentVehicle;
        ok.addActionListener(e -> {
            // Remove from current vehicle first
            if (finalCurrentVehicle != null) {
                appWarehouse.writeOperation(
                    () -> finalCurrentVehicle.removeOrder(o),
                    wc, "Unassign order " + o.getOrderId() + " from " + finalCurrentVehicle.getVehicleId()
                );
            }
            // Assign to selected vehicle
            for (Map.Entry<JRadioButton, Vehicle> entry : radioToVehicle.entrySet()) {
                if (entry.getKey().isSelected()) {
                    Vehicle target = entry.getValue();
                    appWarehouse.writeOperation(
                        () -> target.addOrder(o),
                        wc, "Assign order " + o.getOrderId() + " → " + target.getVehicleId()
                    );
                    break;
                }
            }
            refresh();
            d.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(PANEL_BG);
        btnRow.add(ok);
        panel.add(btnRow, BorderLayout.SOUTH);

        d.add(panel);
        d.pack();
        d.setMinimumSize(new Dimension(380, 200));
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private JRadioButton styledRadio(String label, Color fg, ButtonGroup group) {
        JRadioButton rb = new JRadioButton(label);
        rb.setFont(FONT_BODY);
        rb.setForeground(fg != null ? fg : TEXT_PRI);
        rb.setBackground(PANEL_BG);
        rb.setFocusPainted(false);
        group.add(rb);
        return rb;
    }

    private void showAddWarehouseDialog() {
        JDialog d = styledDialog("Add Warehouse");
        JPanel form = formPanel();
        JTextField nameF = styledField("e.g. South Bay");
        JTextField locF  = styledField("e.g. Los Angeles, CA");
        form.add(formLabel("Name:")); form.add(nameF);
        form.add(formLabel("Location:")); form.add(locF);
        JButton ok = filledButton("Create");
        ok.addActionListener(e -> {
            String n = nameF.getText().trim();
            String l = locF.getText().trim();
            if (!n.isEmpty()) {
                WarehouseComposite wc = appWarehouse.getFactory().getOrCreateComposite(n, l.isEmpty() ? "Unknown" : l);
                appWarehouse.add(wc);
                if (selectedComposite == null) selectedComposite = wc;
                refresh();
            }
            d.dispose();
        });
        form.add(new JLabel()); form.add(ok);
        d.add(form);
        d.pack(); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private void showAddVehicleDialog(WarehouseComposite wc) {
        JDialog d = styledDialog("Add Vehicle to " + wc.getName());
        JPanel form = formPanel();
        JTextField idF     = styledField("e.g. Truck-NW-03");
        JTextField driverF = styledField("e.g. J. Smith");
        JTextField capF    = styledField("e.g. 3000");
        form.add(formLabel("Vehicle ID:")); form.add(idF);
        form.add(formLabel("Driver:"));     form.add(driverF);
        form.add(formLabel("Capacity kg:")); form.add(capF);
        JButton ok = filledButton("Add Vehicle");
        ok.addActionListener(e -> {
            try {
                String vid = idF.getText().trim();
                String drv = driverF.getText().trim();
                double cap = Double.parseDouble(capF.getText().trim());
                if (!vid.isEmpty()) {
                    Factory f = appWarehouse.getFactory();
                    Vehicle v = f.getOrCreateVehicle(vid, drv.isEmpty() ? "Unknown" : drv, cap);
                    appWarehouse.writeOperation(() -> wc.addVehicle(v), wc, "Add vehicle " + vid);
                    refresh();
                }
            } catch (NumberFormatException ex) { /* ignore */ }
            d.dispose();
        });
        form.add(new JLabel()); form.add(ok);
        d.add(form);
        d.pack(); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private void showAddOrderDialog(WarehouseComposite wc) {
        JDialog d = styledDialog("Add Order to " + wc.getName());
        d.setMinimumSize(new Dimension(480, 400));

        // ── Main form panel (scrollable) ──
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(PANEL_BG);
        wrapper.setBorder(new EmptyBorder(18, 20, 14, 20));

        // Top fields
        JPanel topForm = new JPanel(new GridLayout(0, 2, 8, 8));
        topForm.setBackground(PANEL_BG);

        JTextField destF   = styledField("e.g. FedEx #12345");
        JTextField personF = styledField("e.g. M. Myers");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Retail", "Wholesale"});
        styleCombo(typeBox);

        // Vehicle dropdown — list vehicles in this composite
        List<Vehicle> vehicles = wc.getVehicles();
        String[] vehicleOptions = new String[vehicles.size() + 1];
        vehicleOptions[0] = "— None (unassigned) —";
        for (int i = 0; i < vehicles.size(); i++) vehicleOptions[i + 1] = vehicles.get(i).getVehicleId();
        JComboBox<String> vehicleBox = new JComboBox<>(vehicleOptions);
        styleCombo(vehicleBox);

        topForm.add(formLabel("Destination:"));   topForm.add(destF);
        topForm.add(formLabel("Delivery Person:")); topForm.add(personF);
        topForm.add(formLabel("Order Type:"));     topForm.add(typeBox);
        topForm.add(formLabel("Assign to Vehicle:")); topForm.add(vehicleBox);

        // ── Dynamic item rows ──
        JLabel itemsHeader = new JLabel("Items / Packages");
        itemsHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
        itemsHeader.setForeground(TEXT_SEC);
        itemsHeader.setBorder(new EmptyBorder(12, 0, 6, 0));

        JPanel itemsPanel = new JPanel();
        itemsPanel.setBackground(PANEL_BG);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));

        // Each row: [Item Name field] [Weight field] [Remove btn]
        List<JTextField[]> itemRows = new ArrayList<>();

        Runnable addItemRow = () -> {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBackground(PANEL_BG);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setBorder(new EmptyBorder(0, 0, 6, 0));

            JTextField nameF = styledField("Item name");
            JTextField wtF   = styledFieldWithPlaceholder("0.0", "kg");
            wtF.setPreferredSize(new Dimension(70, 28));

            // "kg" label attached to right of weight field
            JLabel kgLabel = new JLabel(" kg");
            kgLabel.setFont(FONT_SMALL);
            kgLabel.setForeground(TEXT_SEC);

            JPanel wtWrapper = new JPanel(new BorderLayout(0, 0));
            wtWrapper.setBackground(PANEL_BG);
            wtWrapper.add(wtF,      BorderLayout.CENTER);
            wtWrapper.add(kgLabel,  BorderLayout.EAST);

            JButton rem = new JButton("✕");
            rem.setFont(new Font("SansSerif", Font.BOLD, 10));
            rem.setBackground(new Color(0x3A, 0x1A, 0x1F));
            rem.setForeground(RED);
            rem.setBorderPainted(false);
            rem.setFocusPainted(false);
            rem.setPreferredSize(new Dimension(28, 28));
            rem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel fields = new JPanel(new BorderLayout(6, 0));
            fields.setBackground(PANEL_BG);
            fields.add(nameF,      BorderLayout.CENTER);
            fields.add(wtWrapper,  BorderLayout.EAST);

            row.add(fields, BorderLayout.CENTER);
            row.add(rem,    BorderLayout.EAST);

            JTextField[] pair = {nameF, wtF};
            itemRows.add(pair);

            rem.addActionListener(ev -> {
                itemRows.remove(pair);
                itemsPanel.remove(row);
                itemsPanel.revalidate();
                itemsPanel.repaint();
                d.pack();
            });

            itemsPanel.add(row);
            itemsPanel.revalidate();
            d.pack();
        };

        // Start with one row
        addItemRow.run();

        JButton addItemBtn = smallTextButton("+ Add Item");
        addItemBtn.setForeground(ACCENT);
        addItemBtn.addActionListener(e -> addItemRow.run());

        // ── Bottom: Create button ──
        JButton ok = filledButton("Create Order");

        ok.addActionListener(e -> {
            String dest = destF.getText().trim();
            String per  = personF.getText().trim();
            if (dest.isEmpty()) { destF.setBorder(new LineBorder(RED, 1)); return; }

            int    type = typeBox.getSelectedIndex() == 0 ? Order.TYPE_RETAIL : Order.TYPE_WHOLESALE;
            Factory f   = appWarehouse.getFactory();
            Order order = (type == Order.TYPE_RETAIL)
                ? f.createRetailOrder(dest, per.isEmpty() ? "Unknown" : per, wc)
                : f.createWholesaleOrder(dest, per.isEmpty() ? "Unknown" : per, wc);

            // Build package wrapper from item rows
            PackageWrapper pw = new PackageWrapper(order.getOrderId());
            boolean hasItems = false;
            for (JTextField[] pair : itemRows) {
                String iName = pair[0].getText().trim();
                String iWtStr = pair[1].getText().trim();
                if (iName.isEmpty()) continue;
                double iWt = 1.0;
                try { iWt = Double.parseDouble(iWtStr); } catch (NumberFormatException ignored) {}
                pw.addPackage(new Package(new Item(iName, iWt), new ShippingInfo(per, dest)));
                hasItems = true;
            }
            if (hasItems) order.addPackageWrapper(pw);

            // Add to composite under write lock
            appWarehouse.writeOperation(() -> wc.addOrder(order), wc, "Add order " + order.getOrderId());

            // Assign to vehicle if selected
            int vIdx = vehicleBox.getSelectedIndex();
            if (vIdx > 0) {
                Vehicle chosen = vehicles.get(vIdx - 1);
                appWarehouse.writeOperation(
                    () -> chosen.addOrder(order),
                    wc,
                    "Assign order " + order.getOrderId() + " → " + chosen.getVehicleId()
                );
            }

            refresh();
            d.dispose();
        });

        // ── Assemble ──
        wrapper.add(topForm,     BorderLayout.NORTH);

        JPanel middleBlock = new JPanel(new BorderLayout(0, 4));
        middleBlock.setBackground(PANEL_BG);
        middleBlock.add(itemsHeader, BorderLayout.NORTH);
        middleBlock.add(itemsPanel,  BorderLayout.CENTER);
        middleBlock.add(addItemBtn,  BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(middleBlock);
        scroll.setBackground(PANEL_BG);
        scroll.getViewport().setBackground(PANEL_BG);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(440, 200));
        styleScrollBar(scroll.getVerticalScrollBar());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        bottomRow.setBackground(PANEL_BG);
        bottomRow.add(ok);

        wrapper.add(scroll,    BorderLayout.CENTER);
        wrapper.add(bottomRow, BorderLayout.SOUTH);

        d.add(wrapper);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────
    //  UI helpers
    // ─────────────────────────────────────────────────────────
    private JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        p.setBorder(new LineBorder(BORDER, 1, true));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT_DIM);
        return l;
    }

    private JButton iconButton(String text) {
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

    private JButton accentButton(String text) {
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

    private JButton filledButton(String text) {
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

    private JButton smallTextButton(String text) {
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

    private JLabel statusBadge(String label) {
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

    private JPanel capacityBar(double loaded, double cap) {
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

    private JDialog styledDialog(String title) {
        JDialog d = new JDialog(this, title, true);
        d.getContentPane().setBackground(PANEL_BG);
        d.setLayout(new BorderLayout());
        d.getRootPane().setBorder(new LineBorder(BORDER, 1));
        return d;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBackground(PANEL_BG);
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        return p;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_SEC);
        return l;
    }

    private JTextField styledField(String placeholder) {
        return styledFieldWithPlaceholder(placeholder, null);
    }

    private JTextField styledFieldWithPlaceholder(String placeholder, String suffixHint) {
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

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT_PRI);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(2, 4, 2, 4)
        ));
        // Custom renderer so every item cell is dark-themed regardless of L&F
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
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("v");
                btn.setBackground(CARD_BG);
                btn.setForeground(TEXT_SEC);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 10));
                return btn;
            }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(CARD_BG);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setBackground(PANEL_BG);
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor     = BORDER;
                trackColor     = PANEL_BG;
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

    // ─────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(Main::new);
    }

    // ─────────────────────────────────────────────────────────
    //  WrapLayout — FlowLayout that wraps to next line properly
    //  so status buttons never get clipped in narrow cards.
    // ─────────────────────────────────────────────────────────
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int maxWidth = target.getWidth();
                if (maxWidth == 0) maxWidth = Integer.MAX_VALUE;
                Insets insets = target.getInsets();
                int horizGap  = getHgap();
                int vertGap   = getVgap();
                int x = insets.left + horizGap;
                int y = insets.top  + vertGap;
                int rowHeight = 0;
                int maxX = insets.left;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x + d.width + horizGap > maxWidth && x > insets.left + horizGap) {
                        x = insets.left + horizGap;
                        y += rowHeight + vertGap;
                        rowHeight = 0;
                    }
                    maxX = Math.max(maxX, x + d.width);
                    x += d.width + horizGap;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                return new Dimension(maxX + insets.right,
                                     y + rowHeight + vertGap + insets.bottom);
            }
        }
    }
}
