/*
 */
package dk.cintix.sms;

import dk.cintix.sms.messages.Message;
import dk.cintix.sms.network.exceptions.ProtocolException;
import dk.cintix.sms.network.protocol.Protocol;
import dk.cintix.sms.network.sockets.Socket;
import java.io.IOException;
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
    private final String host;
    private final int port;

    private final ConcurrentLinkedQueue<T> messageQueue = new ConcurrentLinkedQueue<>();

    public Subscriber(String host, int port) {
        this.host = host;
        this.port = port;
        this.boardcastService = new Thread(new BroadcastService(host, port));
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

        private final String host;
        private final int port;

        public BroadcastService(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Socket clientConnection = new Socket(host, port);
                while (running) {
                    try {

                        if (clientConnection.getInputStream() != null && clientConnection.getInputStream().available() >= Protocol.PROTOCOL_LENGTH) {
                            T readMessage = clientConnection.readMessage();
                            onMessage(readMessage);
                        }

                        synchronized (messageQueue) {                            
                            if (messageQueue.size() > 0) {
                                for (T message : messageQueue) {
                                    clientConnection.sendMessage(message);
                                }
                                messageQueue.clear();
                            }
                        }

                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (ProtocolException | InterruptedException ex) {
                        ex.printStackTrace();
                        Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Subscriber.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
