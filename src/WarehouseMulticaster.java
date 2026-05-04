import java.util.ArrayList;

/**
 * WarehouseMulticaster (Behavioral - Observer Pattern)
 * Manages a list of observers and multicasts notifications to all of them.
 */
public class WarehouseMulticaster implements ObserverIF {
    private ArrayList<ObserverIF> observers;

    public WarehouseMulticaster() {
        observers = new ArrayList<>();
    }

    public void addObserver(ObserverIF o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public void removeObserver(ObserverIF o) {
        observers.remove(o);
    }

    @Override
    public void notify(String event, String source) {
        for (ObserverIF observer : new ArrayList<>(observers)) {
            observer.notify(event, source);
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}
