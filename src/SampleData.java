import model.*;

public class SampleData {

    public static Warehouse build() {
        Warehouse warehouse = new Warehouse("System", "N/A", "N/A");
        Factory   f         = warehouse.getFactory();

        WarehouseComposite northHub = f.getOrCreateComposite("North Hub", "Seattle, WA");
        warehouse.add(northHub);

        Vehicle truck01 = f.getOrCreateVehicle("Truck-NH-01", "M. Myers", 5000);
        northHub.addVehicle(truck01);
        truck01.updateStatus(Vehicle.STATUS_LOADING);

        Vehicle van02 = f.getOrCreateVehicle("Van-NH-02", "A. Kuntz", 1500);
        northHub.addVehicle(van02);

        Vehicle carrr = f.getOrCreateVehicle("carrr", "ayoob", 3000);
        northHub.addVehicle(carrr);
        carrr.updateStatus(Vehicle.STATUS_LOADING);

        Order retailOrder = f.createRetailOrder("FedEx #FX1029", "A. Kuntz", northHub);
        Item monitor  = new Item("Monitor",  6.5);
        Item keyboard = new Item("Keyboard", 1.2);
        model.Package pkgMon = new model.Package(monitor,  new ShippingInfo("FedEx", "#FX1029"));
        model.Package pkgKbd = new model.Package(keyboard, new ShippingInfo("FedEx", "#FX1029"));
        PackageWrapper wrapper = new PackageWrapper("QVR6UTB");
        wrapper.addPackage(pkgMon);
        wrapper.addPackage(pkgKbd);
        retailOrder.addPackageWrapper(wrapper);
        northHub.addOrder(retailOrder);
        truck01.addOrder(retailOrder);

        WarehouseComposite centralDepot = f.getOrCreateComposite("Central Depot", "Dallas, TX");
        warehouse.add(centralDepot);

        Vehicle vanD1 = f.getOrCreateVehicle("Van-CD-01", "B. Smith", 2000);
        centralDepot.addVehicle(vanD1);

        for (WarehouseComposite wc : warehouse.getElements()) {
            warehouse.readScan(wc);
        }

        return warehouse;
    }
}
