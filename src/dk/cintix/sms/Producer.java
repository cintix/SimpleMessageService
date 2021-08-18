/*
 */
package dk.cintix.sms;

import dk.cintix.sms.messages.Message;
import dk.cintix.sms.messages.TransactionItem;
import dk.cintix.sms.network.exceptions.ProtocolException;
import dk.cintix.sms.network.protocol.Header;
import dk.cintix.sms.network.protocol.Protocol;
import dk.cintix.sms.network.sockets.ServerSocket;
import dk.cintix.sms.network.sockets.Socket;
import dk.cintix.sms.network.sockets.TransactionConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migo
 * @param <T>
 */
public abstract class Producer<T extends Message> {

    public static final int MAX_BACKLOG_SIZE = 25000;
    private static final List<TransactionItem> BACKLOG = new LinkedList<>();
    private static final Map<Long, TransactionConnection> SUBSCRIBER_MAP = new HashMap<>();
    private static long latestTransmissionId = 0L;
    private final Thread boardcastService;
    private final Thread clientService;
    private int port = 65656;
    private volatile boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Producer(int port) throws IOException {
        this.port = port;
        clientService = new Thread(new SubscriberService(port));
        boardcastService = new Thread(new BroadcastService());
    }

    public Producer() throws IOException {
        clientService = new Thread(new SubscriberService(port));
        boardcastService = new Thread(new BroadcastService());
    }

    public final void startService() {
        running = true;
        boardcastService.start();
        clientService.start();
    }

    public final void stopService() {
        try {
            running = false;
            boardcastService.interrupt();
            clientService.interrupt();
        } catch (Exception e) {
        }
    }

    private boolean broadcastMessageTo(Long receiverID, ByteArrayOutputStream message) {
        try {
            validateSubscribers();

            synchronized (SUBSCRIBER_MAP) {
                if (!SUBSCRIBER_MAP.keySet().contains(receiverID)) {
                    return false;
                }

                for (Long id : SUBSCRIBER_MAP.keySet()) {
                    if (receiverID.equals(id)) {
                        TransactionConnection transaction = SUBSCRIBER_MAP.get(id);
                        transaction.socket().sendMessagePart(message);
                        break;
                    }
                }
            }
        } catch (IOException iOException) {
            return false;
        }
        return true;
    }

    public final boolean broadcastMessage(T message) {
        try {

            if (message != null) {
                addMessageToBackLog(message);
            }

            validateSubscribers();
            synchronized (SUBSCRIBER_MAP) {
                for (TransactionConnection transaction : SUBSCRIBER_MAP.values()) {
                    try {
                        while (transaction.getLastTransaction() < latestTransmissionId) {
                            TransactionItem currentMessage = getCurrentTransactionFromClient(transaction);
                            transaction.socket().sendMessage(currentMessage.item());
                            transaction.transaction(currentMessage.getID());
                        }
                    } catch (SocketException socketException) {
                        System.out.println("disconnecting " + transaction.socket().getInetAddress().toString());
                        transaction.socket().close();
                    }
                }
            }
        } catch (IOException iOException) {
            iOException.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Sending message Parts
     *
     * @param message
     *
     * @return
     */
    private boolean broadcastPartAs(long clientID, ByteArrayOutputStream message) {
        try {
            validateSubscribers();
            synchronized (SUBSCRIBER_MAP) {
                for (TransactionConnection transaction : SUBSCRIBER_MAP.values()) {
                    try {
                        transaction.socket().sendMessagePart(clientID, message);
                    } catch (SocketException socketException) {
                        transaction.socket().close();
                    }
                }
            }
        } catch (IOException iOException) {
            return false;
        }
        return true;
    }

    /**
     * Sending message Parts
     *
     * @param message
     *
     * @return
     */
    private boolean broadcastPart(ByteArrayOutputStream message) {
        try {
            validateSubscribers();
            synchronized (SUBSCRIBER_MAP) {
                for (TransactionConnection transaction : SUBSCRIBER_MAP.values()) {
                    try {
                        transaction.socket().sendMessagePart(message);
                    } catch (SocketException socketException) {
                        transaction.socket().close();
                    }
                }
            }
        } catch (IOException iOException) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param <T>
     * @param obj
     */
    private void addMessageToBackLog(T obj) {
        if (BACKLOG.size() > MAX_BACKLOG_SIZE) {
            BACKLOG.remove(MAX_BACKLOG_SIZE - 1);
        }
        TransactionItem transactionItem = new TransactionItem(obj);
        BACKLOG.add(0, transactionItem);
        latestTransmissionId = transactionItem.getID();
    }

    /**
     *
     * @param TransactionItem
     * @param connection
     * @return
     */
    private TransactionItem getCurrentTransactionFromClient(TransactionConnection connection) {
        long lastIdSent = connection.getLastTransaction();
        int currentIndex = -1;
        if (!BACKLOG.isEmpty()) {
            for (int index = 0; index < BACKLOG.size(); index++) {
                if (BACKLOG.get(index).getID() > lastIdSent) {
                    currentIndex = index;
                } else {
                    break;
                }
            }
        }

        if (currentIndex > -1) {
            return BACKLOG.get(currentIndex);
        }

        return null;
    }

    /**
     * BroadcastService
     */
    private class BroadcastService implements Runnable {

        @Override
        public void run() {
            HashSet<Long> requestList;
            while (running) {
                try {
                    validateSubscribers();
                    requestList = new HashSet<>();
                    synchronized (SUBSCRIBER_MAP) {
                        for (Long id : SUBSCRIBER_MAP.keySet()) {
                            TransactionConnection transaction = SUBSCRIBER_MAP.get(id);
                            if (transaction.socket().getInputStream() != null && transaction.socket().getInputStream().available() >= Protocol.PROTOCOL_LENGTH) {
                                requestList.add(id);
                            }
                        }
                    }
                    // loop clients having requests
                    for (Long id : requestList) {
                        if (SUBSCRIBER_MAP.containsKey(id)) {
                            TransactionConnection transaction = SUBSCRIBER_MAP.get(id);
                            Header header = transaction.socket().readHeader();
                            ByteArrayOutputStream messagePart = transaction.socket().readMessagePart(header.getContentLength());

                            if (header.getReceiverID() > 0) {
                                broadcastMessageTo(header.getReceiverID(), messagePart);
                            } else {
                                broadcastPartAs(id, messagePart);
                            }

                        }
                    }

                    TimeUnit.MILLISECONDS.sleep(100);

                } catch (IOException | ProtocolException | InterruptedException ex) {
                    Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private class SubscriberService implements Runnable {

        private final int port;
        private ServerSocket serverSocket;

        public SubscriberService(int port) throws IOException {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                this.serverSocket = new ServerSocket(port);
                System.out.println("-------------------- Producer is now listing on port " + port);
                while (running) {
                    try {
                        Socket client = serverSocket.accept();
                        synchronized (SUBSCRIBER_MAP) {
                            SUBSCRIBER_MAP.put(client.getClientID(), new TransactionConnection(client));
                            broadcastMessage(null);
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void validateSubscribers() {
        synchronized (SUBSCRIBER_MAP) {
            HashSet<Long> itemsToRemove = new HashSet<>();
            for (Long id : SUBSCRIBER_MAP.keySet()) {
                if (SUBSCRIBER_MAP.get(id) == null || SUBSCRIBER_MAP.get(id).socket().isClosed() || !SUBSCRIBER_MAP.get(id).socket().isConnected()) {
                    itemsToRemove.add(id);
                }
            }
            for (Long id : itemsToRemove) {
                SUBSCRIBER_MAP.remove(id);
            }
        }
    }

}
