/**
 * Package
 * Wraps an Item with ShippingInfo.
 * This is the "component" layer between Item and PackageWrapper.
 */
public class Package {
    private Item item;
    private ShippingInfo shippingInfo;

    public Package(Item item, ShippingInfo shippingInfo) {
        this.item         = item;
        this.shippingInfo = shippingInfo;
        item.setPackage(this);
    }

    public Item getItem() { return item; }

    public ShippingInfo getShippingInfo() { return shippingInfo; }
    public void setShippingInfo(ShippingInfo si) { this.shippingInfo = si; }

    public double getWeight() { return item.getWeight(); }

    @Override
    public String toString() {
        return "Pkg[" + item.getName() + " → " + shippingInfo + "]";
    }
}
