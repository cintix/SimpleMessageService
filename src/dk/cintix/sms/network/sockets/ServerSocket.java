/*
 */
package dk.cintix.sms.network.sockets;

import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author migo
 */
public class ServerSocket extends java.net.ServerSocket implements java.io.Closeable {
    private final int SOCKET_TIMEOUT = 2000;
    private static long clientSequence = 1L;

    public ServerSocket() throws IOException {
    }

    public ServerSocket(int i) throws IOException {
        super(i);
    }

    public ServerSocket(int i, int i1) throws IOException {
        super(i, i1);
    }

    public ServerSocket(int i, int i1, InetAddress ia) throws IOException {
        super(i, i1, ia);
    }

    @Override
    public dk.cintix.sms.network.sockets.Socket accept() throws IOException {
        dk.cintix.sms.network.sockets.Socket incomingClient = new dk.cintix.sms.network.sockets.Socket(clientSequence++);
        incomingClient.setSoTimeout(SOCKET_TIMEOUT);        
        implAccept(incomingClient);
        return incomingClient;
    }

}
