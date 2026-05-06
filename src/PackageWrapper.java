public class PackageWrapper extends Package {
    private Package pack;
    private Item item;

    public PackageWrapper(Item item) {
        this.pack = null;
        this.item = item;
    }

    @Override
    public double getWeight(){
        return item.getWeight() + pack.getWeight();
    }

    @Override
    public void addItem(Item item) {        //TODO: this needs to preserve ShippingInfo
        if (pack != null) pack.addItem(item);
        pack = new PackageWrapper(item);
    }

    @Override
    public ShippingInfo getShippingInfo(){
        return pack.getShippingInfo();
    }

    @Override
    public void updateShippingInfo(ShippingInfo info){
        if (pack == null) pack = info;
        pack.updateShippingInfo(info);
    }
}
