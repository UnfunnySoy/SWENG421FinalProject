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
    private List<Package> packages;
    private String wrapperId;

    public PackageWrapper(String wrapperId) {
        this.wrapperId = wrapperId;
        this.packages  = new ArrayList<>();
    }

    public void addPackage(Package p) {
        packages.add(p);
    }

    public void removePackage(Package p) {
        packages.remove(p);
    }

    public List<Package> getPackages() {
        return packages;
    }

    /** Total weight of all wrapped packages. */
    public double getWeight() {
        double total = 0;
        for (Package p : packages) {
            total += p.getWeight();
        }
        return total;
    }

    public String getWrapperId() { return wrapperId; }

    /** Convenience: shipping info of the first package, if any. */
    public ShippingInfo getShippingInfo() {
        if (packages.isEmpty()) return null;
        return packages.get(0).getShippingInfo();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Wrapper[").append(wrapperId).append(": ");
        for (int i = 0; i < packages.size(); i++) {
            sb.append(packages.get(i).getItem().getName());
            if (i < packages.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
