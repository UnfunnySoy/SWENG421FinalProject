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
public class Order extends WarehouseComposite implements ObservableIF {
    public static final int TYPE_RETAIL    = 0;
    public static final int TYPE_WHOLESALE = 1;

    public static final int STATUS_PENDING  = 0;
    public static final int STATUS_PREPARED = 1;
    public static final int STATUS_SHIPPED  = 2;

    private int orderType;

    private List<PackageWrapper> packages;

    public Order(String orderId, int orderType, String deliveryLocation,
                 String deliveryPerson, WarehouseComposite warehouse) {
        this.orderType        = orderType;
        this.status           = STATUS_PENDING;
        this.packages         = new ArrayList<>();
    }

    public void addPackage(PackageWrapper p)    { packages.add(p); }
    public void removePackage(PackageWrapper p) { packages.remove(p); }
    public List<PackageWrapper> getPackages() { return packages; }

    public double getTotalWeight() {
        double total = 0;
        for (PackageWrapper pw : packages) total += pw.getWeight();
        return total;
    }

    public int getOrderType(){ return orderType; }

    public void markPrepared() {
        setStatus(STATUS_PREPARED);
    }

    // ---- ObservableIF ----
    @Override public void addObserver(ObserverIF o)    { multicaster.addObserver(o); }
    @Override public void removeObserver(ObserverIF o) { multicaster.removeObserver(o); }

    @Override
    public void doAction() {
        multicaster.notify();
        //TODO: add action implementation for Order
    }
}
