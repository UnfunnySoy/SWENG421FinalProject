/**
 * ShippingInfo
 * Holds shipping destination details for a package.
 */
public class ShippingInfo {
    private String name;
    private String address;

    public ShippingInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return name + " @ " + address;
    }
}
