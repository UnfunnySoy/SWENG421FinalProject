import java.util.ArrayList;
import java.util.List;

/**
 * Vehicle (Composite leaf / Observable)
 * Represents a shipping vehicle (truck, van, etc.) docked at a warehouse.
 * Implements ObservableIF so the warehouse can track its status changes.
 *
 * Status codes:
 *   STATUS_DOCKED      = 0
 *   STATUS_LOADING     = 1
 *   STATUS_DEPARTED    = 2
 *   STATUS_MAINTENANCE = 3
 */
public class Vehicle implements ObservableIF {
    public static final int STATUS_DOCKED      = 0;
    public static final int STATUS_LOADING     = 1;
    public static final int STATUS_DEPARTED    = 2;
    public static final int STATUS_MAINTENANCE = 3;

    private int    status;
    private String vehicleId;
    private String driverName;
    private double capacityKg;
    private int    condition; // editable condition value (0-100)

    private List<Order> orders;
    private ArrayList<ObserverIF> observers;

    public Vehicle(String vehicleId, String driverName, double capacityKg) {
        this.vehicleId   = vehicleId;
        this.driverName  = driverName;
        this.capacityKg  = capacityKg;
        this.condition   = 100;
        this.status      = STATUS_DOCKED;
        this.orders      = new ArrayList<>();
        this.observers   = new ArrayList<>();
    }

    // ---- Editable fields ----
    public int  getCondition()           { return condition; }
    public void setCondition(int cond)   { this.condition = cond; }

    public String getDriverName()        { return driverName; }
    public void   setDriverName(String d){ this.driverName = d; }

    // ---- Orders ----
    public void addOrder(Order o)    { orders.add(o); o.setShippingVehicle(this); }
    public void removeOrder(Order o) { orders.remove(o); o.setShippingVehicle(null); }
    public List<Order> getOrders()   { return orders; }

    public double getLoadedWeightKg() {
        double total = 0;
        for (Order o : orders) total += o.getTotalWeight();
        return total;
    }

    // ---- Identifiers ----
    public String getVehicleId()  { return vehicleId; }
    public double getCapacityKg() { return capacityKg; }

    // ---- ObservableIF ----
    @Override public void addObserver(ObserverIF o)    { observers.add(o); }
    @Override public void removeObserver(ObserverIF o) { observers.remove(o); }

    @Override
    public void doAction() {
        String label = statusLabel();
        for (ObserverIF o : observers) {
            o.notify("Observer: " + vehicleId + " status → " + label, "Vehicle");
        }
    }

    @Override public int checkStatus() { return status; }

    @Override
    public void updateStatus(int status) {
        this.status = status;
        doAction();
    }

    public String statusLabel() {
        switch (status) {
            case STATUS_DOCKED:      return "DOCKED";
            case STATUS_LOADING:     return "LOADING";
            case STATUS_DEPARTED:    return "DEPARTED";
            case STATUS_MAINTENANCE: return "MAINTENANCE";
            default:                 return "UNKNOWN";
        }
    }

    @Override
    public String toString() {
        return vehicleId + " [" + statusLabel() + "] driver=" + driverName;
    }
}
