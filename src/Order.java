import java.util.ArrayList;
import java.util.List;

/**
 * Order (Composite leaf / Observable)
 * Represents a customer order containing one or more PackageWrappers.
 * Implements ObservableIF so the warehouse can observe its status.
 *
 * Order types (set at creation by Factory):
 *   TYPE_RETAIL    = 0
 *   TYPE_WHOLESALE = 1
 *
 * Status codes:
 *   STATUS_PENDING  = 0
 *   STATUS_PREPARED = 1  (marked prepared for shipping)
 *   STATUS_SHIPPED  = 2
 */
public class Order implements ObservableIF {
    public static final int TYPE_RETAIL    = 0;
    public static final int TYPE_WHOLESALE = 1;

    public static final int STATUS_PENDING  = 0;
    public static final int STATUS_PREPARED = 1;
    public static final int STATUS_SHIPPED  = 2;

    private int    status;
    private int    orderType;
    private String orderId;
    private String deliveryLocation;
    private String deliveryPerson;
    private Vehicle shippingVehicle;       // editable
    private final WarehouseComposite warehouse; // unchangeable

    private List<PackageWrapper> packages;
    private ArrayList<ObserverIF> observers;

    public Order(String orderId, int orderType, String deliveryLocation,
                 String deliveryPerson, WarehouseComposite warehouse) {
        this.orderId          = orderId;
        this.orderType        = orderType;
        this.deliveryLocation = deliveryLocation;
        this.deliveryPerson   = deliveryPerson;
        this.warehouse        = warehouse;
        this.status           = STATUS_PENDING;
        this.packages         = new ArrayList<>();
        this.observers        = new ArrayList<>();
    }

    // ---- Editable fields ----
    public String getDeliveryLocation()               { return deliveryLocation; }
    public void   setDeliveryLocation(String loc)     { this.deliveryLocation = loc; }

    public String getDeliveryPerson()                 { return deliveryPerson; }
    public void   setDeliveryPerson(String person)    { this.deliveryPerson = person; }

    public Vehicle getShippingVehicle()               { return shippingVehicle; }
    public void    setShippingVehicle(Vehicle v)      { this.shippingVehicle = v; }

    // ---- Package list ----
    public void addPackageWrapper(PackageWrapper pw)    { packages.add(pw); }
    public void removePackageWrapper(PackageWrapper pw) { packages.remove(pw); }
    public List<PackageWrapper> getPackages()           { return packages; }

    public double getTotalWeight() {
        double total = 0;
        for (PackageWrapper pw : packages) total += pw.getWeight();
        return total;
    }

    // ---- Immutable field ----
    public WarehouseComposite getWarehouse() { return warehouse; }

    // ---- Identifiers ----
    public String getOrderId()  { return orderId; }
    public int    getOrderType(){ return orderType; }

    /** Mark the order as prepared for shipping (status → PREPARED). */
    public void markPrepared() {
        updateStatus(STATUS_PREPARED);
    }

    // ---- ObservableIF ----
    @Override public void addObserver(ObserverIF o)    { observers.add(o); }
    @Override public void removeObserver(ObserverIF o) { observers.remove(o); }

    @Override
    public void doAction() {
        String statusLabel = status == STATUS_PENDING  ? "PENDING"
                           : status == STATUS_PREPARED ? "PREPARED"
                           : "SHIPPED";
        for (ObserverIF o : observers) {
            o.notify("Order " + orderId + " status → " + statusLabel, "Order");
        }
    }

    @Override public int checkStatus() { return status; }

    @Override
    public void updateStatus(int status) {
        this.status = status;
        doAction();
    }

    @Override
    public String toString() {
        String type = (orderType == TYPE_RETAIL) ? "Retail Order" : "Wholesale Order";
        return type + " #" + orderId;
    }
}
