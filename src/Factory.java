import java.util.HashMap;
import java.util.Map;

/**
 * Factory (Creational - Factory Method Pattern)
 * Creates Order and Vehicle objects. Reuses Vehicle and Warehouse objects
 * by id when possible (Flyweight-like reuse to save memory per requirements).
 */
public class Factory {
    // Reuse pools
    private Map<String, Vehicle>            vehiclePool;
    private Map<String, WarehouseComposite> compositePool;

    private int orderSeq = 1000;

    public Factory() {
        vehiclePool    = new HashMap<>();
        compositePool  = new HashMap<>();
    }

    // =========================================================
    //  Factory Method – Orders
    // =========================================================

    /** Create a Retail Order fully initialized and ready to use. */
    public Order createRetailOrder(String deliveryLocation, String deliveryPerson,
                                   WarehouseComposite warehouse) {
        String id = "R" + generateId();
        return new Order(id, Order.TYPE_RETAIL, deliveryLocation, deliveryPerson, warehouse);
    }

    /** Create a Wholesale Order fully initialized and ready to use. */
    public Order createWholesaleOrder(String deliveryLocation, String deliveryPerson,
                                      WarehouseComposite warehouse) {
        String id = "W" + generateId();
        return new Order(id, Order.TYPE_WHOLESALE, deliveryLocation, deliveryPerson, warehouse);
    }

    // =========================================================
    //  Factory Method – Vehicles  (reuse by vehicleId)
    // =========================================================

    /**
     * Returns an existing Vehicle if vehicleId is already known,
     * otherwise creates and registers a new one.
     */
    public Vehicle getOrCreateVehicle(String vehicleId, String driverName, double capacityKg) {
        return vehiclePool.computeIfAbsent(vehicleId,
                id -> new Vehicle(id, driverName, capacityKg));
    }

    // =========================================================
    //  Factory Method – WarehouseComposites (reuse by name)
    // =========================================================

    /**
     * Returns an existing WarehouseComposite if name is already known,
     * otherwise creates and registers a new one.
     */
    public WarehouseComposite getOrCreateComposite(String name, String location) {
        return compositePool.computeIfAbsent(name,
                n -> new WarehouseComposite(n, location));
    }

    /**
     * getElement() — convenience alias used in UML diagram.
     * Returns the first composite or creates a default one.
     */
    public WarehouseComposite getElement() {
        if (compositePool.isEmpty()) {
            return getOrCreateComposite("Default", "Unknown");
        }
        return compositePool.values().iterator().next();
    }

    // =========================================================
    //  Helpers
    // =========================================================

    private String generateId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        int n = orderSeq++ * 7919 + 13337; // simple hash
        for (int i = 0; i < 7; i++) {
            sb.append(chars.charAt(Math.abs(n % chars.length())));
            n = n / chars.length() + orderSeq;
        }
        return sb.toString();
    }

    public Map<String, Vehicle>            getVehiclePool()    { return vehiclePool; }
    public Map<String, WarehouseComposite> getCompositePool()  { return compositePool; }
}
