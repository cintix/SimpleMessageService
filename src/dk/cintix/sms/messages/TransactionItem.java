package dk.cintix.sms.messages;

/**
 *
 * @author migo
 */
public class TransactionItem {

    private final long ID;
    private final Object obj;

    public TransactionItem(Object obj) {
        this.ID = System.nanoTime();
        this.obj = obj;
    }

    public long getID() {
        return ID;
    }

    public <T> T item() {        
        T returnValue = (T) obj;
        return returnValue;
    }

    @Override
    public String toString() {
        return "TransactionItem{" + "ID=" + ID + ", obj=" + obj + '}';
    }

}
