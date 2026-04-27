import java.util.ArrayList;

public class WarehouseMulticaster {
    private ArrayList<ObserverIF> observers = new ArrayList<>();

    public void tellObservers(){
        for(ObserverIF observer : observers){
            observer.tell();
        }
    }
}
