/*
 */
package dk.cintix.sms;

import dk.cintix.sms.messages.Message;
import dk.cintix.sms.network.exceptions.ProtocolException;
import dk.cintix.sms.network.protocol.Header;
import dk.cintix.sms.network.protocol.Protocol;
import dk.cintix.sms.network.sockets.ServerSocket;
import dk.cintix.sms.network.sockets.Socket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migo
 */
public abstract class Producer<T extends Message> {

    private static final Map<Long, dk.cintix.sms.network.sockets.Socket> SUBSCRIBER_MAP = new HashMap<>();
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
                for (Long id : SUBSCRIBER_MAP.keySet()) {
                    if (receiverID.equals(id)) {
                        SUBSCRIBER_MAP.get(id).sendMessagePart(message);
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
            validateSubscribers();
            synchronized (SUBSCRIBER_MAP) {
                for (Socket socket : SUBSCRIBER_MAP.values()) {
                    try {
                        socket.sendMessage(message);
                    } catch (SocketException socketException) {
                        System.out.println("disconnecting " + socket.getInetAddress().toString());
                        socket.close();
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
                for (Socket socket : SUBSCRIBER_MAP.values()) {
                    try {
                        socket.sendMessagePart(clientID, message);
                    } catch (SocketException socketException) {
                        socket.close();
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
                for (Socket socket : SUBSCRIBER_MAP.values()) {
                    try {
                        socket.sendMessagePart(message);
                    } catch (SocketException socketException) {
                        socket.close();
                    }
                }
            }
        } catch (IOException iOException) {
            return false;
        }
        return true;
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
                            Socket clientConnection = SUBSCRIBER_MAP.get(id);
                            if (clientConnection.getInputStream() != null && clientConnection.getInputStream().available() >= Protocol.PROTOCOL_LENGTH) {
                                requestList.add(id);
                            }
                        }
                    }
                    // loop clients having requests
                    for (Long id : requestList) {
                        if (SUBSCRIBER_MAP.containsKey(id)) {
                            Socket socket = SUBSCRIBER_MAP.get(id);
                            Header header = socket.readHeader();
                            ByteArrayOutputStream messagePart = socket.readMessagePart(header.getContentLength());

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
                            SUBSCRIBER_MAP.put(client.getClientID(), client);
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
                if (SUBSCRIBER_MAP.get(id) == null || SUBSCRIBER_MAP.get(id).isClosed() || !SUBSCRIBER_MAP.get(id).isConnected()) {
                    itemsToRemove.add(id);
                }
            }
            for (Long id : itemsToRemove) {
                SUBSCRIBER_MAP.remove(id);
            }
        }
    }

}
