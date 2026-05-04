import java.util.ArrayList;
import java.util.List;

/**
 * WarehouseComposite (Structural - Composite Pattern)
 * Represents a named location within a warehouse (e.g., a bay or the whole
 * warehouse itself). It is both a composite node (holds Vehicles and Orders)
 * and an ObservableIF so higher-level observers can watch it.
 *
 * The WarehouseMulticaster fans out notifications to all subscribed observers.
 */
public class WarehouseComposite implements ObservableIF {
    private int status;
    private String name;
    private String location;
    private WarehouseMulticaster multicaster;

    private List<Vehicle> vehicles;
    private List<Order>   orders;

    public static final int STATUS_ACTIVE   = 0;
    public static final int STATUS_INACTIVE = 1;

    public WarehouseComposite(String name, String location) {
        this.name        = name;
        this.location    = location;
        this.status      = STATUS_ACTIVE;
        this.multicaster = new WarehouseMulticaster();
        this.vehicles    = new ArrayList<>();
        this.orders      = new ArrayList<>();
    }

    // ---- Composite: Vehicles ----
    public void addVehicle(Vehicle v) {
        vehicles.add(v);
        v.addObserver(multicaster);
        multicaster.notify("Vehicle " + v.getVehicleId() + " docked (driver: " + v.getDriverName() + ")", name);
    }

    public void removeVehicle(Vehicle v) {
        vehicles.remove(v);
        v.removeObserver(multicaster);
        multicaster.notify("Vehicle " + v.getVehicleId() + " removed from composite", name);
    }

    public List<Vehicle> getVehicles() { return vehicles; }

    // ---- Composite: Orders ----
    public void addOrder(Order o) {
        orders.add(o);
        o.addObserver(multicaster);
        multicaster.notify("Order " + o.getOrderId() + " added", name);
    }

    public void removeOrder(Order o) {
        orders.remove(o);
        o.removeObserver(multicaster);
    }

    /**
     * Deliver an order: marks it SHIPPED, removes it from this composite,
     * and removes it from whichever vehicle (if any) was carrying it.
     */
    public void deliverOrder(Order o) {
        o.updateStatus(Order.STATUS_SHIPPED);
        multicaster.notify("Order " + o.getOrderId() + " delivered → removed from system", name);
        // Remove from any vehicle holding it
        for (Vehicle v : new ArrayList<>(vehicles)) {
            if (v.getOrders().contains(o)) {
                v.removeOrder(o);
            }
        }
        removeOrder(o);
    }

    public List<Order> getOrders() { return orders; }

    // ---- Metrics ----
    public double getTotalWeightKg() {
        double total = 0;
        for (Vehicle v : vehicles) total += v.getLoadedWeightKg();
        for (Order   o : orders)   total += o.getTotalWeight();
        return total;
    }

    public int getActiveVehicleCount() {
        int count = 0;
        for (Vehicle v : vehicles) {
            if (v.checkStatus() != Vehicle.STATUS_DEPARTED) count++;
        }
        return count;
    }

    public long getStagedOrderCount() {
        // Count staged orders directly in this composite
        long count = orders.stream()
                           .filter(o -> o.checkStatus() == Order.STATUS_PREPARED)
                           .count();
        // Also count staged orders carried by vehicles (avoiding double-count)
        java.util.Set<Order> seen = new java.util.HashSet<>(orders);
        for (Vehicle v : vehicles) {
            for (Order o : v.getOrders()) {
                if (!seen.contains(o) && o.checkStatus() == Order.STATUS_PREPARED) {
                    count++;
                    seen.add(o);
                }
            }
        }
        return count;
    }

    // ---- Identifiers ----
    public String getName()     { return name; }
    public void   setName(String n) { this.name = n; }
    public String getLocation() { return location; }

    // ---- ObservableIF ----
    @Override
    public void addObserver(ObserverIF o) {
        multicaster.addObserver(o);
    }

    @Override
    public void removeObserver(ObserverIF o) {
        multicaster.removeObserver(o);
    }

    @Override
    public void doAction() {
        multicaster.notify("WarehouseComposite action: " + name, name);
    }

    @Override public int checkStatus() { return status; }

    @Override
    public void updateStatus(int status) {
        this.status = status;
        doAction();
    }

    /** Fire a read-lock inventory snapshot notification. */
    public void fireReadSnapshot(WarehouseLock lock) {
        multicaster.notify("Read lock: inventory snapshot → " + String.format("%.1f", getTotalWeightKg()) + "kg total", name);
    }

    @Override
    public String toString() {
        return name + " [" + location + "] vehicles=" + vehicles.size() + " orders=" + orders.size();
    }
}
