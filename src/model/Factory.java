package model;

import java.util.HashMap;
import java.util.Map;

public class Factory {
    private Map<String, Vehicle>            vehiclePool;
    private Map<String, WarehouseComposite> compositePool;

    public Map<String, Vehicle>            getVehiclePool()    { return vehiclePool; }
    public Map<String, WarehouseComposite> getCompositePool()  { return compositePool; }

    public Factory() {
        vehiclePool = new HashMap<>();
        compositePool = new HashMap<>();
    }

    public Vehicle getOrCreateVehicle(String vehicleId, double capacityKg) {
        return vehiclePool.computeIfAbsent(vehicleId,
                id -> new Vehicle(capacityKg));
    }

    /* //TODO: this need to be changed to return a concrete object
    public model.WarehouseComposite getOrCreateComposite(String name, String location) {
        return compositePool.computeIfAbsent(name,
                n -> new model.WarehouseComposite());
    }
     */
}
