/*
 */
package dk.cintix.sms.network.protocol;

import dk.cintix.sms.Producer;
import dk.cintix.sms.Subscriber;
import dk.cintix.sms.io.Binary;
import dk.cintix.sms.messages.Message;
import dk.cintix.sms.messages.QueueModel;
import dk.cintix.sms.messages.TextMessage;
import dk.cintix.sms.network.exceptions.ProtocolException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migo
 */
public class Protocol {

    private TextMessage textMessage;
    private QueueModel queueModel;
    
    /**
     * Protocol structure Index
     *
     * 14 Bytes header
     *
     * 0......... (1 byte ) - VERSION
     * 1,2,3,4... (4 bytes) - UNSIGNED Sender ID
     * 5,6,7,8... (4 bytes) - UNSIGNED Reciver ID
     * 9,10,11,12 (4 bytes) - UNSIGNED Content length
     *
     */
    public static final int PROTOCOL_LENGTH = 13;

    /**
     * readHeader
     *
     * @param bytes
     *
     * @return
     */
    public static Header readHeader(byte[] bytes) throws ProtocolException {
        if (bytes == null || bytes.length != PROTOCOL_LENGTH) {
            throw new ProtocolException("Header is not correct length " + PROTOCOL_LENGTH + " was " + bytes.length);
        }
        Header header = new Header();
        header.setVersion(bytes[0]);

        byte[] clientBytes = new byte[4];
        clientBytes[0] = bytes[1];
        clientBytes[1] = bytes[2];
        clientBytes[2] = bytes[3];
        clientBytes[3] = bytes[4];

        header.setClientID(Binary.bytesToLong(clientBytes));
        
        byte[] receiverBytes = new byte[4];
        receiverBytes[0] = bytes[5];
        receiverBytes[1] = bytes[6];
        receiverBytes[2] = bytes[7];
        receiverBytes[3] = bytes[8];

        header.setReceiverID(Binary.bytesToLong(receiverBytes));

        byte[] contentLengthBytes = new byte[4];
        contentLengthBytes[0] = bytes[9];
        contentLengthBytes[1] = bytes[10];
        contentLengthBytes[2] = bytes[11];
        contentLengthBytes[3] = bytes[12];

        header.setContentLength(Binary.bytesToLong(contentLengthBytes));

        return header;
    }

    /**
     * createHeader
     *
     * @param header
     *
     * @return
     */
    public static byte[] createHeader(Header header) {
        byte[] protocolHeader = new byte[PROTOCOL_LENGTH];
        protocolHeader[0] = header.getVersion();

        byte[] clientId = Binary.longToByte(header.getClientID());
        protocolHeader[1] = clientId[0];
        protocolHeader[2] = clientId[1];
        protocolHeader[3] = clientId[2];
        protocolHeader[4] = clientId[3];

        byte[] receiverId = Binary.longToByte(header.getReceiverID());
        protocolHeader[5] = receiverId[0];
        protocolHeader[6] = receiverId[1];
        protocolHeader[7] = receiverId[2];
        protocolHeader[8] = receiverId[3];

        byte[] contentLenght = Binary.longToByte(header.getContentLength());
        protocolHeader[9] = contentLenght[0];
        protocolHeader[10] = contentLenght[1];
        protocolHeader[11] = contentLenght[2];
        protocolHeader[12] = contentLenght[3];

        return protocolHeader;
    }

    /**
     * 
     * @param <T>
     * @param bytes
     * @return 
     */
    public final static <T extends Message> T bytesToMessage(byte[] bytes) {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return ((T) o.readObject());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Subscriber.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Subscriber.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * 
     * @param message
     * @return 
     */
    public final static <T extends Message> byte[] messageToBytes(T message) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(message);
            return b.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
