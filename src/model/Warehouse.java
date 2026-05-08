package model;

import java.util.ArrayList;
import java.util.List;

/**
 * model.Warehouse (Structural - Composite Root)
 * The top-level container. Holds multiple model.WarehouseComposite nodes.
 * Owns the model.WarehouseLock and the model.Factory (both shared across composites).
 * Implements model.ObserverIF so it can log events from its composites.
 */
public class Warehouse implements ObserverIF {
    private String name;
    private String city;
    private String state;

    private ArrayList<WarehouseComposite> elements;
    private ArrayList<ObservableIF>       observed;

    private WarehouseLock lock;
    private Factory       factory;

    // Observer event log
    private List<String[]> eventLog; // [timestamp, source, message]

    public Warehouse(String name, String city, String state) {
        this.name     = name;
        this.city     = city;
        this.state    = state;
        this.elements = new ArrayList<>();
        this.observed = new ArrayList<>();
        this.lock     = new WarehouseLock();
        this.factory  = new Factory();
        this.eventLog = new ArrayList<>();
    }

    // ---- Composite management ----
    public void add(WarehouseComposite w) {
        elements.add(w);
        w.addObserver(this);   // warehouse observes the composite
        observed.add(w);
        logEvent(w.getName(), "model.WarehouseComposite '" + w.getName() + "' registered");
    }

    public void remove(WarehouseComposite w) {
        elements.remove(w);
        w.removeObserver(this);
        observed.remove(w);
    }

    public List<WarehouseComposite> getElements() { return elements; }

    // ---- Read/Write lock helpers ----

    /**
     * Perform a read scan of the given composite under a read lock.
     * Multiple threads can read simultaneously.
     */
    public double readScan(WarehouseComposite wc) {
        lock.readLock();
        try {
            wc.fireReadSnapshot(lock);
            return wc.getTotalWeightKg();
        } finally {
            lock.done(false);
        }
    }

    /**
     * Perform a write operation (add/remove vehicle or order) under a write lock.
     */
    public void writeOperation(Runnable operation, WarehouseComposite wc, String description) {
        lock.writeLock();
        try {
            operation.run();
            logEvent(wc.getName(), "Write: " + description);
        } finally {
            lock.done(true);
        }
    }

    // ---- model.ObserverIF ----
    @Override
    public void notify(String event, String source) {
        logEvent(source, event);
    }

    // ---- Event log ----
    private void logEvent(String source, String message) {
        String timestamp = new java.text.SimpleDateFormat("hh:mm:ss a")
                               .format(new java.util.Date());
        eventLog.add(0, new String[]{ timestamp, source, message });
        if (eventLog.size() > 200) eventLog.remove(eventLog.size() - 1);
    }

    public List<String[]> getEventLog() { return eventLog; }

    // ---- Accessors ----
    public String        getName()    { return name; }
    public String        getCity()    { return city; }
    public String        getState()   { return state; }
    public WarehouseLock getLock()    { return lock; }
    public Factory getFactory() { return factory; }

    public double getTotalWeight() {
        double t = 0;
        for (WarehouseComposite wc : elements) t += wc.getTotalWeightKg();
        return t;
    }

    @Override
    public String toString() {
        return name + " (" + city + ", " + state + ")";
    }
}
