public class ShippingInfo extends Package {
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
    public double getWeight() {
        return 0;
    }

    @Override
    public void addItem(Item item) {
        return;
    }

    @Override
    public ShippingInfo getShippingInfo(){
        return this;
    }

    @Override
    public void updateShippingInfo(ShippingInfo info){
        this.name = info.name;
        this.address = info.address;
    }
}
