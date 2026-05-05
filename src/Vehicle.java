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
public class Vehicle extends WarehouseComposite {
    public static final int STATUS_DOCKED      = 0;
    public static final int STATUS_LOADING     = 1;
    public static final int STATUS_DEPARTED    = 2;
    public static final int STATUS_MAINTENANCE = 3;

    private int    status;
    private double capacityKg;
    private int    condition; // editable condition value (0-100)

    private List<Order> orders;

    public Vehicle(double capacityKg) {
        this.id = generateId();
        this.capacityKg  = capacityKg;
        this.condition   = 100;
        this.status      = STATUS_DOCKED;
        this.orders      = new ArrayList<>();
    }

    @Override
    protected String generateId(){
        String baseID = super.generateId();
        return "V" + baseID;
    }

    public int  getCondition()           { return condition; }
    public void setCondition(int cond)   { this.condition = cond; }

    public void addOrder(Order o)    { orders.add(o); }
    public void removeOrder(Order o) { orders.remove(o); }
    public List<Order> getOrders()   { return orders; }

    public double getCapacityKg() { return capacityKg; }

    public double getLoadedWeightKg() {
        double total = 0;
        for (Order o : orders) total += o.getTotalWeight();
        return total;
    }

    @Override
    public void doAction() {
        //TODO: add action implementation for Vehicle
        multicaster.notify();
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
}
