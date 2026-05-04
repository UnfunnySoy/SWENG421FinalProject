/**
 * User
 * Represents an operator of the warehouse management system.
 * Has a reference to the Warehouse they manage.
 */
public class User {
    private String   username;
    private Warehouse warehouse;

    public User(String username, Warehouse warehouse) {
        this.username  = username;
        this.warehouse = warehouse;
    }

    public String    getUsername()  { return username; }
    public Warehouse getWarehouse() { return warehouse; }
    public void      setWarehouse(Warehouse w) { this.warehouse = w; }

    @Override
    public String toString() {
        return "User[" + username + "]";
    }
}
