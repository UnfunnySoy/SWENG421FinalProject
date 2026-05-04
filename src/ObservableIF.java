/**
 * Observable Interface (Behavioral - Observer Pattern)
 * Implemented by any class that can be observed.
 */
public interface ObservableIF {
    void addObserver(ObserverIF o);
    void removeObserver(ObserverIF o);
    void doAction();
    int checkStatus();
    void updateStatus(int status);
}
