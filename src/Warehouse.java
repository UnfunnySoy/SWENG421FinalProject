import java.util.ArrayList;

public class Warehouse implements ObserverIF {
    private ArrayList<WarehouseComposite> elements = new ArrayList<>();
    private WarehouseLock lock = new WarehouseLock();
    private Factory factory = new Factory();

    @Override
    public void tell() {
        return;
    }
}
