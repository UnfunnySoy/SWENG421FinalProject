package model;

public class Item extends WarehouseComposite {
    public static final int STATUS_IN_STORAGE  = 0;
    public static final int STATUS_IN_TRANSIT  = 1;
    public static final int STATUS_DELIVERED   = 2;

    private String name;
    private double weight;

    public Item(String name, double weight) {
        super();
        this.name    = name;
        this.weight  = weight;
        this.status  = STATUS_IN_STORAGE;
    }

    @Override
    protected String generateId(){
        String baseID = super.generateId();
        return "I" + baseID;
    }

    public String getName()  { return name; }
    public void setName(String name) { this.name = name; }

    public double getWeight() { return weight; }

    @Override public void addObserver(ObserverIF o)    { multicaster.addObserver(o); }
    @Override public void removeObserver(ObserverIF o) { multicaster.addObserver(o); }

    @Override
    public void doAction() {
        //TODO: add action implementation for model.Item
        multicaster.notify();
    }
}
