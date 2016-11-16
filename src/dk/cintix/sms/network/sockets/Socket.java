/*
 */
package dk.cintix.sms.network.sockets;

import dk.cintix.sms.messages.Message;
import dk.cintix.sms.network.exceptions.ProtocolException;
import dk.cintix.sms.network.protocol.Header;
import dk.cintix.sms.network.protocol.Protocol;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 *
 * @author migo
 */
public class Socket extends java.net.Socket {

    private Header header;
    private long clientID;
    private ByteArrayOutputStream backBuffer = new ByteArrayOutputStream();

    public Socket(long clientID) {
        this.clientID = clientID;
    }

    public Socket(Proxy proxy) {
        super(proxy);
    }

    public Socket(Header header) {
        this.header = header;
    }

    public Socket(InetAddress ia, int i) throws IOException {
        super(ia, i);
    }

    public Socket(String string, int i) throws UnknownHostException, IOException {
        super(string, i);
    }

    public Socket(String string, int i, boolean bln) throws IOException {
        super(string, i, bln);
    }

    public Socket(String string, int i, InetAddress ia, int i1) throws IOException {
        super(string, i, ia, i1);
    }

    public Socket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
        super(ia, i, ia1, i1);
    }

    public Socket(InetAddress ia, int i, boolean bln) throws IOException {
        super(ia, i, bln);
    }

    public Socket() {
        super();
    }

    public void sendHeader(Header _header) throws IOException {
        getOutputStream().write(Protocol.createHeader(_header));
        //getOutputStream().flush();
    }

    public long getClientID() {
        return clientID;
    }

    public Header getHeader() {
        return header;
    }

    /**
     * Send Message
     *
     * @param <T>
     * @param message
     *
     * @throws IOException
     */
    public <T extends Message> void sendMessage(T message) throws IOException {
        header = message.getHeader();
        if (header == null) {
            header = new Header();
        }

        header.setClientID(clientID);
        byte[] bytes = Protocol.messageToBytes(message);
        header.setContentLength(bytes.length);

        sendHeader(header);

        getOutputStream().write(bytes);
        getOutputStream().flush();
    }

    /**
     * sendMessagePart
     *
     * @param message
     *
     * @throws IOException
     */
    public void sendMessagePart(ByteArrayOutputStream message) throws IOException {

        Header _header = new Header();
        _header.setClientID(clientID);
        _header.setContentLength(message.size());

        sendHeader(_header);

        getOutputStream().write(message.toByteArray());
        getOutputStream().flush();

    }

    /**
     * sendMessagePart
     *
     * @param message
     *
     * @throws IOException
     */
    public void sendMessagePart(long _clientID, ByteArrayOutputStream message) throws IOException {

        Header _header = new Header();
        _header.setClientID(_clientID);
        _header.setContentLength(message.size());

        sendHeader(_header);

        getOutputStream().write(message.toByteArray());
        getOutputStream().flush();

    }

    /**
     * Read the header
     *
     * @return
     *
     * @throws IOException
     * @throws ProtocolException
     */
    public Header readHeader() throws IOException, ProtocolException {
        byte[] headerBytes = readBytesFromStreamOrMemory(getInputStream(), (int) Protocol.PROTOCOL_LENGTH).toByteArray();
        header = Protocol.readHeader(headerBytes);
        return header;
    }

    /**
     * readMessagePart
     *
     * @param contentLength
     *
     * @return
     *
     * @throws IOException
     */
    public ByteArrayOutputStream readMessagePart(long contentLength) throws IOException {
        return readBytesFromStreamOrMemory(getInputStream(), (int) contentLength);

    }

    private ByteArrayOutputStream readBytesFromStreamOrMemory(InputStream in, int length) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int readLength = length;
                
        if (backBuffer.size() > 0) {
            if (backBuffer.size() > readLength) {
                byte[] byteArray = backBuffer.toByteArray();
                byte[] byteReturn = Arrays.copyOfRange(byteArray, 0, length - 1);
                byte[] byteRemaining = Arrays.copyOfRange(byteArray, length - 1, backBuffer.size() - length);

                backBuffer.reset();
                backBuffer.write(byteRemaining);

                byteArrayOutputStream.write(byteReturn);
                return byteArrayOutputStream;
            } else {
                readLength -= backBuffer.size();
                backBuffer.write(readBytesFromStream(in, readLength));
                byteArrayOutputStream.write(backBuffer.toByteArray());
                backBuffer.reset();
                return byteArrayOutputStream;
            }
        } else {
            byteArrayOutputStream.write(readBytesFromStream(in, length));
            return byteArrayOutputStream;
        }

    }

    public byte[] readBytesFromStream(InputStream in, int length) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int readLength = length;
        byte[] byteArray;


        if (readLength > 2048) {
            byteArray = new byte[2048];
        } else {
            byteArray = new byte[readLength];
        }
        int read = in.read(byteArray);
        if (read != -1) {
            byteArrayOutputStream.write(byteArray, 0, read);
        }

        while (read != -1) {
            readLength -= read;

            if (readLength < 1 || read == -1) {
                break;
            }

            byteArrayOutputStream.write(byteArray, 0, read);

            if (readLength > 2048) {
                byteArray = new byte[2048];
            } else {
                byteArray = new byte[readLength];
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Read the Message
     *
     * @param <T>
     *
     * @return
     *
     * @throws IOException
     */
    public <T extends Message> T readMessage() throws IOException, ProtocolException {
        Header internal = readHeader();
        ByteArrayOutputStream byteArrayOutputStream = readMessagePart(internal.getContentLength());
        T message = Protocol.bytesToMessage(byteArrayOutputStream.toByteArray());
        message.setHeader(internal);
        return message;
    }

}
