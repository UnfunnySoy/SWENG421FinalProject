import java.util.ArrayList;
import java.util.List;

public abstract class WarehouseComposite implements ObservableIF {
    private static int idDisc = 1000;

    protected String id;
    protected int status;
    protected WarehouseMulticaster multicaster;

    public WarehouseComposite() {
        this.id = generateId();
        this.status = 0;
        this.multicaster = new WarehouseMulticaster();
    }

    public abstract void doAction();

    protected String generateId(){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        int n = idDisc++ * 7919 + 13337;
        for (int i = 0; i < 7; i++) {
            sb.append(chars.charAt(Math.abs(n % chars.length())));
            n = n / chars.length() + idDisc;
        }
        return sb.toString();
    }

    public String getId(){
        return id;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    @Override
    public void addObserver(ObserverIF o) {
        multicaster.addObserver(o);
    }

    @Override
    public void removeObserver(ObserverIF o) {
        multicaster.removeObserver(o);
    }
}
