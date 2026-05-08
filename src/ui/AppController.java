package ui;

import model.*;
import javax.swing.JFrame;

public interface AppController {
    void onSelectComposite(WarehouseComposite wc);
    void onReadScan(WarehouseComposite wc);
    void onAddWarehouse();
    void onAddVehicle(WarehouseComposite wc);
    void onAddOrder(WarehouseComposite wc);
    void onMoveOrder(Order o, WarehouseComposite wc);
    void onDeliverOrder(Order o, WarehouseComposite wc);
    void onRemoveOrder(Order o, WarehouseComposite wc);
    void onVehicleStatusChange(Vehicle v, int newStatus, WarehouseComposite wc);
    void onMarkPrepared(Order o);
    JFrame getOwnerFrame();
}
