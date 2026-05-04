/**
 * Observer Interface (Behavioral - Observer Pattern)
 * Implemented by any class that wants to be notified of observable changes.
 */
public interface ObserverIF {
    void notify(String event, String source);
}
