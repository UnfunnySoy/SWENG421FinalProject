public abstract class Package {
    public abstract double getWeight();
    public abstract void addItem(Item item);
    public abstract ShippingInfo getShippingInfo();
    public abstract void updateShippingInfo(ShippingInfo info);
}
