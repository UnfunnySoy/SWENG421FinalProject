package ui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * CompositePanel
 * The center column. Shows the selected model.WarehouseComposite's header stats,
 * then scrolls through three sections: Vehicles, Staged Orders, Unassigned Orders.
 */
public class CompositePanel extends JPanel {

    private final AppController ctrl;

    public CompositePanel(AppController ctrl) {
        this.ctrl = ctrl;
        setOpaque(false);
        setLayout(new BorderLayout(0, 10));
    }

    public void refresh(WarehouseComposite wc) {
        removeAll();
        if (wc == null) return;

        add(buildHeader(wc), BorderLayout.NORTH);

        JPanel body = buildBody(wc);
        JScrollPane scroll = new JScrollPane(body);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        Theme.styleScrollBar(scroll.getVerticalScrollBar());
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // ── Header ────────────────────────────────────────────────
    private JPanel buildHeader(WarehouseComposite wc) {
        JPanel panel = Theme.darkPanel();
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));
        panel.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel rootLabel = new JLabel("COMPOSITE ROOT");
        rootLabel.setFont(Theme.FONT_SMALL);
        rootLabel.setForeground(Theme.TEXT_DIM);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        JButton addV = Theme.accentButton("🚚 Add model.Vehicle");
        JButton addO = Theme.filledButton("📦 Add model.Order");
        addV.addActionListener(e -> ctrl.onAddVehicle(wc));
        addO.addActionListener(e -> ctrl.onAddOrder(wc));
        btnRow.add(addV); btnRow.add(addO);

        top.add(rootLabel, BorderLayout.WEST);
        top.add(btnRow,    BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        JPanel nameBlock = new JPanel(new BorderLayout());
        nameBlock.setOpaque(false);
        JLabel nameL = new JLabel(wc.getName());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 26));
        nameL.setForeground(Theme.TEXT_PRI);
        JLabel locL = new JLabel(wc.getLocation());
        locL.setFont(Theme.FONT_BODY);
        locL.setForeground(Theme.TEXT_SEC);
        nameBlock.add(nameL, BorderLayout.NORTH);
        nameBlock.add(locL,  BorderLayout.CENTER);
        panel.add(nameBlock, BorderLayout.CENTER);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 10, 0));
        statsRow.setOpaque(false);
        statsRow.add(Theme.statTile("TOTAL WEIGHT",
            String.format("%.1f kg", wc.getTotalWeightKg())));
        statsRow.add(Theme.statTile("ACTIVE VEHICLES",
            String.valueOf(wc.getActiveVehicleCount())));
        statsRow.add(Theme.statTile("STAGED ORDERS",
            String.valueOf(wc.getStagedOrderCount())));
        panel.add(statsRow, BorderLayout.SOUTH);

        return panel;
    }

    // ── Scrollable body ───────────────────────────────────────
    private JPanel buildBody(WarehouseComposite wc) {
        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));

        // ── Vehicles ──
        outer.add(Theme.sectionDivider("🚚  Vehicles", "COMPOSITE + OBSERVER"));
        outer.add(Box.createVerticalStrut(8));

        JPanel vGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        vGrid.setOpaque(false);
        for (Vehicle v : wc.getVehicles()) {
            vGrid.add(buildVehicleCard(v, wc));
        }
        if (wc.getVehicles().isEmpty()) {
            vGrid.add(Theme.emptyNotice("No vehicles docked. Use 'Add model.Vehicle' above."));
        }
        outer.add(vGrid);
        outer.add(Box.createVerticalStrut(18));

        // ── Staged Orders ──
        List<Order> staged = collectStaged(wc);
        outer.add(Theme.sectionDivider("📦  Staged Orders",
            "READY TO DELIVER  —  " + staged.size()));
        outer.add(Box.createVerticalStrut(8));
        if (staged.isEmpty()) {
            outer.add(Theme.emptyNotice("No staged orders. Mark an order as Prepared to stage it."));
        } else {
            JPanel sGrid = new JPanel(new GridLayout(0, 2, 10, 8));
            sGrid.setOpaque(false);
            for (Order o : staged) sGrid.add(buildStagedCard(o, wc));
            outer.add(sGrid);
        }
        outer.add(Box.createVerticalStrut(18));

        // ── Unassigned Orders ──
        List<Order> unassigned = collectUnassigned(wc);
        outer.add(Theme.sectionDivider("📋  Unassigned Orders",
            "PENDING — NOT ON A VEHICLE  —  " + unassigned.size()));
        outer.add(Box.createVerticalStrut(8));
        if (unassigned.isEmpty()) {
            outer.add(Theme.emptyNotice("All orders are assigned to vehicles."));
        } else {
            JPanel uGrid = new JPanel(new GridLayout(0, 2, 10, 8));
            uGrid.setOpaque(false);
            for (Order o : unassigned) uGrid.add(buildUnassignedCard(o, wc));
            outer.add(uGrid);
        }
        outer.add(Box.createVerticalStrut(14));

        return outer;
    }

    // ── model.Vehicle card ──────────────────────────────────────────
    private JPanel buildVehicleCard(Vehicle v, WarehouseComposite wc) {
        JPanel card = Theme.darkPanel();
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        card.setLayout(new BorderLayout(0, 8));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel idLabel = new JLabel("🚚 " + v.getId());
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        idLabel.setForeground(Theme.ACCENT);
        head.add(idLabel, BorderLayout.WEST);
        head.add(Theme.statusBadge(v.statusLabel()), BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JLabel driver = new JLabel("Driver: " + v.getDriverName());
        driver.setFont(Theme.FONT_SMALL);
        driver.setForeground(Theme.TEXT_SEC);
        card.add(driver, BorderLayout.CENTER);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Capacity bar
        double loaded = v.getLoadedWeightKg(), cap = v.getCapacityKg();
        JPanel capRow = new JPanel(new BorderLayout(6, 0));
        capRow.setOpaque(false);
        JLabel capLabel = new JLabel("Capacity");
        capLabel.setFont(Theme.FONT_SMALL);
        capLabel.setForeground(Theme.TEXT_DIM);
        JLabel capVal = new JLabel(String.format("%.1f / %.0f kg", loaded, cap));
        capVal.setFont(Theme.FONT_SMALL);
        capVal.setForeground(Theme.TEXT_PRI);
        capRow.add(capLabel, BorderLayout.WEST);
        capRow.add(capVal,   BorderLayout.EAST);
        content.add(capRow);
        content.add(Box.createVerticalStrut(4));
        content.add(Theme.capacityBar(loaded, cap));
        content.add(Box.createVerticalStrut(8));

        // Orders on this vehicle
        for (Order o : v.getOrders()) {
            content.add(buildOrderEntry(o, wc));
            content.add(Box.createVerticalStrut(4));
        }
        if (v.getOrders().isEmpty()) {
            JLabel empty = new JLabel("empty");
            empty.setFont(Theme.FONT_SMALL);
            empty.setForeground(Theme.TEXT_DIM);
            content.add(empty);
        }
        content.add(Box.createVerticalStrut(8));

        // Status toggle buttons
        content.add(buildStatusButtons(v, wc));
        card.add(content, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildStatusButtons(Vehicle v, WarehouseComposite wc) {
        JPanel row = new JPanel(new Theme.WrapLayout(FlowLayout.LEFT, 4, 3));
        row.setOpaque(false);
        int cur = v.getStatus();
        row.add(statusBtn("Docked",      Vehicle.STATUS_DOCKED,      cur, v, wc));
        row.add(statusBtn("Loading",     Vehicle.STATUS_LOADING,     cur, v, wc));
        row.add(statusBtn("Departed",    Vehicle.STATUS_DEPARTED,    cur, v, wc));
        row.add(statusBtn("Maintenance", Vehicle.STATUS_MAINTENANCE, cur, v, wc));
        return row;
    }

    private JButton statusBtn(String label, int status, int current,
                               Vehicle v, WarehouseComposite wc) {
        boolean active = (status == current);
        JButton btn = new JButton(label.toUpperCase());
        btn.setFont(new Font("SansSerif", Font.BOLD, 9));
        btn.setMargin(new Insets(3, 7, 3, 7));
        Color activeBg = status == Vehicle.STATUS_LOADING     ? Theme.ORANGE
                       : status == Vehicle.STATUS_DEPARTED    ? Theme.BLUE
                       : status == Vehicle.STATUS_MAINTENANCE ? Theme.RED
                       : Theme.GREEN;
        btn.setBackground(active ? activeBg : Theme.CARD_BG);
        btn.setForeground(active ? (activeBg == Theme.ORANGE ? Color.BLACK : Color.WHITE)
                                 : Theme.TEXT_DIM);
        btn.setBorderPainted(!active);
        btn.setBorder(active
            ? new EmptyBorder(3, 7, 3, 7)
            : BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(2, 6, 2, 6)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> ctrl.onVehicleStatusChange(v, status, wc));
        return btn;
    }

    private JPanel buildOrderEntry(Order o, WarehouseComposite wc) {
        JPanel p = new JPanel(new BorderLayout(6, 2));
        p.setBackground(new Color(0x14, 0x17, 0x22));
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        String type = o.getOrderType() == Order.TYPE_RETAIL ? "Retail model.Order" : "Wholesale model.Order";
        JLabel nameL = new JLabel(type + " #" + o.getId());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 11));
        nameL.setForeground(Theme.TEXT_PRI);
        JLabel wt = new JLabel(String.format("%.1fkg", o.getTotalWeight()));
        wt.setFont(Theme.FONT_SMALL);
        wt.setForeground(Theme.TEXT_SEC);
        top.add(nameL, BorderLayout.WEST);
        top.add(wt,    BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        JPanel pkgList = new JPanel();
        pkgList.setOpaque(false);
        pkgList.setLayout(new BoxLayout(pkgList, BoxLayout.Y_AXIS));
        for (PackageWrapper pw : o.getPackages()) {
            for (model.Package pkg : pw.getPackages()) {
                JLabel pkgL = new JLabel("  📦 " + pkg.getItem().getName()
                    + " (" + pkg.getItem().getWeight() + "kg)");
                pkgL.setFont(Theme.FONT_SMALL);
                pkgL.setForeground(Theme.TEXT_SEC);
                pkgList.add(pkgL);
            }
            if (pw.getShippingInfo() != null) {
                JLabel ship = new JLabel("  ↪ " + pw.getShippingInfo().toString());
                ship.setFont(Theme.FONT_SMALL);
                ship.setForeground(Theme.TEXT_DIM);
                pkgList.add(ship);
            }
        }
        p.add(pkgList, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        actions.setOpaque(false);
        if (o.checkStatus() == Order.STATUS_PENDING) {
            JButton prep = Theme.smallTextButton("✓ Mark Prepared");
            prep.setForeground(Theme.GREEN);
            prep.addActionListener(e -> ctrl.onMarkPrepared(o));
            actions.add(prep);
        }
        JButton move = Theme.smallTextButton("⇄ Move to model.Vehicle");
        move.setForeground(Theme.ACCENT2);
        move.addActionListener(e -> ctrl.onMoveOrder(o, wc));
        actions.add(move);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    // ── Staged order card ─────────────────────────────────────
    private JPanel buildStagedCard(Order o, WarehouseComposite wc) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(0x16, 0x1E, 0x18));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x22, 0x55, 0x33), 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        String type = o.getOrderType() == Order.TYPE_RETAIL ? "Retail" : "Wholesale";
        JLabel nameL = new JLabel(type + " #" + o.getOrderId());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(Theme.GREEN);
        JLabel badge = new JLabel("STAGED");
        badge.setFont(Theme.FONT_BADGE);
        badge.setForeground(Color.BLACK);
        badge.setBackground(Theme.GREEN);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
        head.add(nameL, BorderLayout.WEST);
        head.add(badge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        if (!o.getDeliveryLocation().isEmpty()) {
            JLabel dest = new JLabel("→ " + o.getDeliveryLocation());
            dest.setFont(Theme.FONT_SMALL); dest.setForeground(Theme.TEXT_SEC);
            details.add(dest);
        }
        if (!o.getDeliveryPerson().isEmpty()) {
            JLabel person = new JLabel("Driver: " + o.getDeliveryPerson());
            person.setFont(Theme.FONT_SMALL); person.setForeground(Theme.TEXT_DIM);
            details.add(person);
        }
        String vLabel = "Unassigned";
        for (Vehicle v : wc.getVehicles()) {
            if (v.getOrders().contains(o)) { vLabel = "On " + v.getVehicleId(); break; }
        }
        JLabel veh = new JLabel("model.Vehicle: " + vLabel);
        veh.setFont(Theme.FONT_SMALL); veh.setForeground(Theme.TEXT_DIM);
        details.add(veh);

        int itemCount = 0;
        for (PackageWrapper pw : o.getPackages()) itemCount += pw.getPackages().size();
        JLabel items = new JLabel(itemCount + " item" + (itemCount != 1 ? "s" : "")
            + "  —  " + String.format("%.1f kg", o.getTotalWeight()));
        items.setFont(Theme.FONT_SMALL); items.setForeground(Theme.TEXT_SEC);
        details.add(Box.createVerticalStrut(4));
        details.add(items);
        card.add(details, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);

        JButton deliverBtn = new JButton("✈ Deliver");
        deliverBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        deliverBtn.setBackground(Theme.GREEN);
        deliverBtn.setForeground(Color.BLACK);
        deliverBtn.setBorderPainted(false);
        deliverBtn.setFocusPainted(false);
        deliverBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        deliverBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deliverBtn.addActionListener(e -> ctrl.onDeliverOrder(o, wc));

        JButton moveBtn = Theme.smallTextButton("⇄ Move");
        moveBtn.setForeground(Theme.ACCENT2);
        moveBtn.addActionListener(e -> ctrl.onMoveOrder(o, wc));

        btnRow.add(deliverBtn);
        btnRow.add(moveBtn);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ── Unassigned order card ─────────────────────────────────
    private JPanel buildUnassignedCard(Order o, WarehouseComposite wc) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        String type = o.getOrderType() == Order.TYPE_RETAIL ? "Retail" : "Wholesale";
        JLabel nameL = new JLabel(type + " #" + o.getOrderId());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameL.setForeground(Theme.TEXT_PRI);
        JLabel badge = new JLabel("PENDING");
        badge.setFont(Theme.FONT_BADGE);
        badge.setForeground(Color.BLACK);
        badge.setBackground(Theme.ORANGE);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));
        head.add(nameL, BorderLayout.WEST);
        head.add(badge, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        if (!o.getDeliveryLocation().isEmpty()) {
            JLabel dest = new JLabel("→ " + o.getDeliveryLocation());
            dest.setFont(Theme.FONT_SMALL); dest.setForeground(Theme.TEXT_SEC);
            details.add(dest);
        }
        int itemCount = 0;
        for (PackageWrapper pw : o.getPackages()) itemCount += pw.getPackages().size();
        JLabel items = new JLabel(itemCount + " item" + (itemCount != 1 ? "s" : "")
            + "  —  " + String.format("%.1f kg", o.getTotalWeight()));
        items.setFont(Theme.FONT_SMALL); items.setForeground(Theme.TEXT_SEC);
        details.add(Box.createVerticalStrut(4));
        details.add(items);
        card.add(details, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);

        JButton stageBtn = new JButton("✓ Mark Prepared");
        stageBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        stageBtn.setBackground(new Color(0x1A, 0x40, 0x28));
        stageBtn.setForeground(Theme.GREEN);
        stageBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0x22, 0x55, 0x33), 1),
            new EmptyBorder(4, 8, 4, 8)));
        stageBtn.setFocusPainted(false);
        stageBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        stageBtn.addActionListener(e -> ctrl.onMarkPrepared(o));

        JButton moveBtn = Theme.smallTextButton("⇄ Assign model.Vehicle");
        moveBtn.setForeground(Theme.ACCENT2);
        moveBtn.addActionListener(e -> ctrl.onMoveOrder(o, wc));

        JButton delBtn = Theme.smallTextButton("✕ Remove");
        delBtn.setForeground(Theme.RED);
        delBtn.addActionListener(e -> ctrl.onRemoveOrder(o, wc));

        btnRow.add(stageBtn);
        btnRow.add(moveBtn);
        btnRow.add(delBtn);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────
    private List<Order> collectStaged(WarehouseComposite wc) {
        List<Order> list = new ArrayList<>();
        Set<Order> seen  = new HashSet<>();
        for (Order o : wc.getOrders()) {
            if (o.checkStatus() == Order.STATUS_PREPARED) { list.add(o); seen.add(o); }
        }
        for (Vehicle v : wc.getVehicles()) {
            for (Order o : v.getOrders()) {
                if (!seen.contains(o) && o.checkStatus() == Order.STATUS_PREPARED) {
                    list.add(o); seen.add(o);
                }
            }
        }
        return list;
    }

    private List<Order> collectUnassigned(WarehouseComposite wc) {
        Set<Order> onVehicle = new HashSet<>();
        for (Vehicle v : wc.getVehicles()) onVehicle.addAll(v.getOrders());
        List<Order> list = new ArrayList<>();
        for (Order o : wc.getOrders()) {
            if (!onVehicle.contains(o) && o.checkStatus() != Order.STATUS_SHIPPED) list.add(o);
        }
        return list;
    }
}
