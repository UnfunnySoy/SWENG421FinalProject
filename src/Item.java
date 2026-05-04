import java.util.ArrayList;

/**
 * Item (Structural - Decorator base component)
 * Represents a single storable item. Also implements ObservableIF so its
 * status can be tracked.
 */
public class Item implements ObservableIF {
    // Status constants
    public static final int STATUS_IN_STORAGE  = 0;
    public static final int STATUS_IN_TRANSIT  = 1;
    public static final int STATUS_DELIVERED   = 2;

    private int status;
    private String name;
    private double weight;
    private Package pack; // the package this item belongs to (optional)

    private ArrayList<ObserverIF> observers;

    public Item(String name, double weight) {
        this.name    = name;
        this.weight  = weight;
        this.status  = STATUS_IN_STORAGE;
        this.observers = new ArrayList<>();
    }

    // ---- Getters / Setters ----
    public String getName()  { return name; }
    public void setName(String name) { this.name = name; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public Package getPackage() { return pack; }
    public void setPackage(Package p) { this.pack = p; }

    // ---- ObservableIF ----
    @Override public void addObserver(ObserverIF o)    { observers.add(o); }
    @Override public void removeObserver(ObserverIF o) { observers.remove(o); }

    @Override
    public void doAction() {
        for (ObserverIF o : observers) {
            o.notify("Item action: " + name, "Item");
        }
    }

    @Override
    public int checkStatus() { return status; }

    @Override
    public void updateStatus(int status) {
        this.status = status;
        doAction();
    }

    @Override
    public String toString() {
        return name + " (" + weight + "kg)";
    }
}
