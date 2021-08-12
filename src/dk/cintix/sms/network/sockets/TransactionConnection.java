package dk.cintix.sms.network.sockets;

/**
 *
 * @author migo
 */
public class TransactionConnection {

    private final dk.cintix.sms.network.sockets.Socket socket;
    private long lastTransaction = 0L;

    public TransactionConnection(dk.cintix.sms.network.sockets.Socket socket) {
        this.socket = socket;
    }

    public void transaction(long l) {
        lastTransaction = l;
    }

    public long getLastTransaction() {
        return lastTransaction;
    }

    public Socket socket() {
        return socket;
    }

}
