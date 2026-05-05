import java.util.ArrayList;
import java.util.List;

/**
 * PackageWrapper (Structural - Decorator Pattern)
 * Wraps one or more Package objects together into a single shipment unit.
 * Because it aggregates packages it can report total weight and expose
 * individual sub-packages. It also carries the inner-most ShippingInfo of
 * the first package as a convenience accessor.
 */
public class PackageWrapper {
    private Package pack;
    private Item item;

    public PackageWrapper(Item item) {
        this.item = item;
    }

    public void addItem(Item item) {
        if (pack != null) return;
        if (item != null) return;

    }

    public List<Package> getPackages() {
        return packages;
    }

    public double getWeight() {

    }

    public String getWrapperId() { return wrapperId; }

    public ShippingInfo getShippingInfo() {

    }
}
