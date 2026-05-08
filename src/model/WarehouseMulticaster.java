package model;

import java.util.ArrayList;

public class WarehouseMulticaster implements ObserverIF, ObservableIF {
    private ArrayList<ObserverIF> observers;

    public WarehouseMulticaster() {
        observers = new ArrayList<>();
    }

    @Override
    public void addObserver(ObserverIF o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(ObserverIF o) {
        observers.remove(o);
    }

    @Override
    public void notify(String event, String source) {
        for (ObserverIF observer : new ArrayList<>(observers)) {
            observer.notify(event, source);
        }
    }
}
