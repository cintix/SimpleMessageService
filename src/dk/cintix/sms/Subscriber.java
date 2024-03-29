/*
 */
package dk.cintix.sms;

import dk.cintix.sms.messages.Message;
import dk.cintix.sms.messages.MessageType;
import dk.cintix.sms.network.exceptions.ProtocolException;
import dk.cintix.sms.network.protocol.Protocol;
import dk.cintix.sms.network.sockets.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migo
 */
public abstract class Subscriber<T extends Message> {

    private final Thread boardcastService;
    private volatile boolean running = false;
    private List<MessageType> filterOn;
    private final String host;
    private final int port;

    private final ConcurrentLinkedQueue<T> messageQueue = new ConcurrentLinkedQueue<>();

    public Subscriber(String host, int port) {
        this.host = host;
        this.port = port;
        this.boardcastService = new Thread(new BroadcastService(host, port));
    }

    public Subscriber(String host, int port, MessageType... filter) {
        this.host = host;
        this.port = port;
        this.boardcastService = new Thread(new BroadcastService(host, port));
        filterOn = Arrays.asList(filter);
    }

    public void registereFilter(MessageType... filter) {
        filterOn = Arrays.asList(filter);
    }

    public final void sendMessage(T message) {
        synchronized (messageQueue) {
            messageQueue.add(message);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public final void startService() {
        running = true;
        boardcastService.start();
    }

    public final void stopService() {
        try {
            running = false;
            boardcastService.interrupt();
        } catch (Exception e) {
        }
    }

    public abstract void onMessage(T message);

    /**
     * BroadcastService
     */
    private class BroadcastService implements Runnable {

        public final int CONNECTION_TIMEOUT = 15000;
        private final String host;
        private final int port;

        public BroadcastService(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            Socket clientConnection = new Socket();
            try {
                System.out.println("Starting subscriber broadcasting service");
                clientConnection.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
                clientConnection.setSoTimeout(CONNECTION_TIMEOUT);
            } catch (IOException ex) {
            }

            while (running) {
                try {

                    if (clientConnection.isClosed() || !clientConnection.isConnected()) {
                        clientConnection = new Socket();
                        clientConnection.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
                        clientConnection.setSoTimeout(CONNECTION_TIMEOUT);
                    } else {

                        if (clientConnection.getInputStream() != null && clientConnection.getInputStream().available() >= Protocol.PROTOCOL_LENGTH) {
                            T readMessage = clientConnection.readMessage();

                            if (filterOn == null || filterOn.contains(readMessage.getType())) {
                                onMessage(readMessage);
                            }
                        }

                        synchronized (messageQueue) {
                            if (messageQueue.size() > 0) {
                                for (T message : messageQueue) {
                                    clientConnection.sendMessage(message);
                                }
                                messageQueue.clear();
                            }
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(100);

                } catch (SocketException socketException) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Subscriber.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException | ProtocolException ex) {
                    Logger.getLogger(Subscriber.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }
}
