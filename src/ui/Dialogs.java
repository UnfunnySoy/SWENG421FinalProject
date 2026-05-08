package ui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public final class Dialogs {

    private Dialogs() {}

    // ── Add model.Warehouse ─────────────────────────────────────────
    public static void showAddWarehouse(JFrame owner, AppController ctrl,
                                        Warehouse appWarehouse) {
        JDialog d = Theme.styledDialog(owner, "Add model.Warehouse");
        JPanel form = Theme.formPanel();

        JTextField nameF = Theme.styledField("e.g. South Bay");
        JTextField locF  = Theme.styledField("e.g. Los Angeles, CA");
        form.add(Theme.formLabel("Name:"));     form.add(nameF);
        form.add(Theme.formLabel("Location:")); form.add(locF);

        JButton ok = Theme.filledButton("Create");
        ok.addActionListener(e -> {
            String n = nameF.getText().trim();
            String l = locF.getText().trim();
            if (!n.isEmpty()) {
                WarehouseComposite wc = appWarehouse.getFactory()
                    .getOrCreateComposite(n, l.isEmpty() ? "Unknown" : l);
                appWarehouse.add(wc);
                ctrl.onSelectComposite(wc);
            }
            d.dispose();
        });
        form.add(new JLabel()); form.add(ok);
        d.add(form);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    // ── Add model.Vehicle ───────────────────────────────────────────
    public static void showAddVehicle(JFrame owner, AppController ctrl,
                                      Warehouse appWarehouse, WarehouseComposite wc) {
        JDialog d = Theme.styledDialog(owner, "Add model.Vehicle to " + wc.getName());
        JPanel form = Theme.formPanel();

        JTextField idF     = Theme.styledField("e.g. Truck-NW-03");
        JTextField driverF = Theme.styledField("e.g. J. Smith");
        JTextField capF    = Theme.styledFieldWithPlaceholder("3000", "kg");
        form.add(Theme.formLabel("model.Vehicle ID:"));    form.add(idF);
        form.add(Theme.formLabel("Driver:"));         form.add(driverF);
        form.add(Theme.formLabel("Capacity (kg):")); form.add(capF);

        JButton ok = Theme.filledButton("Add model.Vehicle");
        ok.addActionListener(e -> {
            try {
                String vid = idF.getText().trim();
                String drv = driverF.getText().trim();
                double cap = Double.parseDouble(capF.getText().trim());
                if (!vid.isEmpty()) {
                    Vehicle v = appWarehouse.getFactory()
                        .getOrCreateVehicle(vid, drv.isEmpty() ? "Unknown" : drv, cap);
                    appWarehouse.writeOperation(() -> wc.addVehicle(v), wc,
                        "Add vehicle " + vid);
                }
            } catch (NumberFormatException ignored) {}
            d.dispose();
        });
        form.add(new JLabel()); form.add(ok);
        d.add(form);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    // ── Add model.Order ─────────────────────────────────────────────
    public static void showAddOrder(JFrame owner, AppController ctrl,
                                    Warehouse appWarehouse, WarehouseComposite wc) {
        JDialog d = Theme.styledDialog(owner, "Add model.Order to " + wc.getName());
        d.setMinimumSize(new Dimension(480, 400));

        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(Theme.PANEL_BG);
        wrapper.setBorder(new EmptyBorder(18, 20, 14, 20));

        // Top fields
        JPanel topForm = new JPanel(new GridLayout(0, 2, 8, 8));
        topForm.setBackground(Theme.PANEL_BG);

        JTextField destF   = Theme.styledField("e.g. FedEx #12345");
        JTextField personF = Theme.styledField("e.g. M. Myers");

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Retail", "Wholesale"});
        Theme.styleCombo(typeBox);

        List<Vehicle> vehicles = wc.getVehicles();
        String[] vehicleOptions = new String[vehicles.size() + 1];
        vehicleOptions[0] = "— None (unassigned) —";
        for (int i = 0; i < vehicles.size(); i++) vehicleOptions[i + 1] = vehicles.get(i).getVehicleId();
        JComboBox<String> vehicleBox = new JComboBox<>(vehicleOptions);
        Theme.styleCombo(vehicleBox);

        topForm.add(Theme.formLabel("Destination:"));     topForm.add(destF);
        topForm.add(Theme.formLabel("Delivery Person:")); topForm.add(personF);
        topForm.add(Theme.formLabel("model.Order Type:"));      topForm.add(typeBox);
        topForm.add(Theme.formLabel("Assign to model.Vehicle:")); topForm.add(vehicleBox);

        // Dynamic item rows
        JLabel itemsHeader = new JLabel("Items / Packages");
        itemsHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
        itemsHeader.setForeground(Theme.TEXT_SEC);
        itemsHeader.setBorder(new EmptyBorder(12, 0, 6, 0));

        JPanel itemsPanel = new JPanel();
        itemsPanel.setBackground(Theme.PANEL_BG);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));

        List<JTextField[]> itemRows = new ArrayList<>();

        Runnable addRow = () -> {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBackground(Theme.PANEL_BG);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setBorder(new EmptyBorder(0, 0, 6, 0));

            JTextField nameF = Theme.styledField("model.Item name");
            JTextField wtF   = Theme.styledFieldWithPlaceholder("0.0", "kg");
            wtF.setPreferredSize(new Dimension(70, 28));

            JLabel kgLabel = new JLabel(" kg");
            kgLabel.setFont(Theme.FONT_SMALL);
            kgLabel.setForeground(Theme.TEXT_SEC);

            JPanel wtWrapper = new JPanel(new BorderLayout());
            wtWrapper.setBackground(Theme.PANEL_BG);
            wtWrapper.add(wtF,     BorderLayout.CENTER);
            wtWrapper.add(kgLabel, BorderLayout.EAST);

            JButton rem = new JButton("✕");
            rem.setFont(new Font("SansSerif", Font.BOLD, 10));
            rem.setBackground(new Color(0x3A, 0x1A, 0x1F));
            rem.setForeground(Theme.RED);
            rem.setBorderPainted(false);
            rem.setFocusPainted(false);
            rem.setPreferredSize(new Dimension(28, 28));
            rem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel fields = new JPanel(new BorderLayout(6, 0));
            fields.setBackground(Theme.PANEL_BG);
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

        addRow.run(); // start with one row

        JButton addItemBtn = Theme.smallTextButton("+ Add model.Item");
        addItemBtn.setForeground(Theme.ACCENT);
        addItemBtn.addActionListener(e -> addRow.run());

        JButton ok = Theme.filledButton("Create model.Order");
        ok.addActionListener(e -> {
            String dest = destF.getText().trim();
            String per  = personF.getText().trim();
            if (dest.isEmpty()) { destF.setBorder(new LineBorder(Theme.RED, 1)); return; }

            int type = typeBox.getSelectedIndex() == 0 ? Order.TYPE_RETAIL : Order.TYPE_WHOLESALE;
            Factory f = appWarehouse.getFactory();
            Order order = (type == Order.TYPE_RETAIL)
                ? f.createRetailOrder(dest, per.isEmpty() ? "Unknown" : per, wc)
                : f.createWholesaleOrder(dest, per.isEmpty() ? "Unknown" : per, wc);

            PackageWrapper pw = new PackageWrapper(order.getOrderId());
            boolean hasItems  = false;
            for (JTextField[] pair : itemRows) {
                String iName  = pair[0].getText().trim();
                String iWtStr = pair[1].getText().trim();
                if (iName.isEmpty()) continue;
                double iWt = 1.0;
                try { iWt = Double.parseDouble(iWtStr); } catch (NumberFormatException ignored) {}
                pw.addPackage(new model.Package(new model.Item(iName, iWt), new ShippingInfo(per, dest)));
                hasItems = true;
            }
            if (hasItems) order.addPackageWrapper(pw);

            appWarehouse.writeOperation(() -> wc.addOrder(order), wc,
                "Add order " + order.getOrderId());

            int vIdx = vehicleBox.getSelectedIndex();
            if (vIdx > 0) {
                Vehicle chosen = vehicles.get(vIdx - 1);
                appWarehouse.writeOperation(() -> chosen.addOrder(order), wc,
                    "Assign order " + order.getOrderId() + " → " + chosen.getVehicleId());
            }
            d.dispose();
        });

        JPanel middleBlock = new JPanel(new BorderLayout(0, 4));
        middleBlock.setBackground(Theme.PANEL_BG);
        middleBlock.add(itemsHeader, BorderLayout.NORTH);
        middleBlock.add(itemsPanel,  BorderLayout.CENTER);
        middleBlock.add(addItemBtn,  BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(middleBlock);
        scroll.setBackground(Theme.PANEL_BG);
        scroll.getViewport().setBackground(Theme.PANEL_BG);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(440, 200));
        Theme.styleScrollBar(scroll.getVerticalScrollBar());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        bottomRow.setBackground(Theme.PANEL_BG);
        bottomRow.add(ok);

        wrapper.add(topForm,    BorderLayout.NORTH);
        wrapper.add(scroll,     BorderLayout.CENTER);
        wrapper.add(bottomRow,  BorderLayout.SOUTH);

        d.add(wrapper);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    // ── Move / Assign model.Order to model.Vehicle ────────────────────────
    public static void showMoveOrder(JFrame owner, AppController ctrl,
                                     Warehouse appWarehouse,
                                     Order o, WarehouseComposite wc) {
        List<Vehicle> vehicles = wc.getVehicles();
        if (vehicles.isEmpty()) {
            JOptionPane.showMessageDialog(owner,
                "No vehicles in this composite to assign to.",
                "No Vehicles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog d = Theme.styledDialog(owner, "Move model.Order " + o.getOrderId());
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Theme.PANEL_BG);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel info = new JLabel(
            "<html><b style='color:#F0F0F8'>" + o + "</b>"
            + "<br><span style='color:#8A8EA8'>Weight: "
            + String.format("%.1f", o.getTotalWeight()) + " kg</span></html>");
        info.setFont(Theme.FONT_BODY);
        panel.add(info, BorderLayout.NORTH);

        JPanel vList = new JPanel();
        vList.setBackground(Theme.PANEL_BG);
        vList.setLayout(new BoxLayout(vList, BoxLayout.Y_AXIS));
        ButtonGroup bg = new ButtonGroup();

        JRadioButton unassign = Theme.styledRadio(
            "— Remove from all vehicles (unassign) —", null, bg);
        vList.add(unassign);
        vList.add(Box.createVerticalStrut(4));

        Map<JRadioButton, Vehicle> radioToVehicle = new LinkedHashMap<>();
        Vehicle currentVehicle = null;
        for (Vehicle v : vehicles) {
            boolean isCurrent = v.getOrders().contains(o);
            if (isCurrent) currentVehicle = v;
            double avail = v.getCapacityKg() - v.getLoadedWeightKg();
            boolean fits = avail >= o.getTotalWeight() || isCurrent;
            String label = v.getVehicleId() + "  —  Driver: " + v.getDriverName()
                + "   " + String.format("%.1f / %.0f kg",
                    v.getLoadedWeightKg(), v.getCapacityKg())
                + (isCurrent ? "  ← current" : fits ? "" : "  ⚠ over capacity");
            JRadioButton rb = Theme.styledRadio(label,
                isCurrent ? Theme.GREEN : fits ? Theme.TEXT_PRI : Theme.TEXT_SEC, bg);
            if (isCurrent) rb.setSelected(true);
            if (!fits && !isCurrent) rb.setEnabled(false);
            radioToVehicle.put(rb, v);
            vList.add(rb);
            vList.add(Box.createVerticalStrut(4));
        }
        if (currentVehicle == null) unassign.setSelected(true);
        panel.add(vList, BorderLayout.CENTER);

        JButton ok = Theme.filledButton("Assign");
        final Vehicle finalCurrent = currentVehicle;
        ok.addActionListener(e -> {
            if (finalCurrent != null) {
                appWarehouse.writeOperation(
                    () -> finalCurrent.removeOrder(o), wc,
                    "Unassign order " + o.getOrderId()
                        + " from " + finalCurrent.getVehicleId());
            }
            for (Map.Entry<JRadioButton, Vehicle> entry : radioToVehicle.entrySet()) {
                if (entry.getKey().isSelected()) {
                    Vehicle target = entry.getValue();
                    appWarehouse.writeOperation(
                        () -> target.addOrder(o), wc,
                        "Assign order " + o.getOrderId() + " → " + target.getVehicleId());
                    break;
                }
            }
            d.dispose();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(Theme.PANEL_BG);
        btnRow.add(ok);
        panel.add(btnRow, BorderLayout.SOUTH);

        d.add(panel);
        d.pack();
        d.setMinimumSize(new Dimension(380, 200));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }
}
