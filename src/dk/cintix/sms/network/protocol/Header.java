/*
 */
package dk.cintix.sms.network.protocol;

/**
 * Class Header
 *
 * @author migo
 */
public class Header {

    private byte version = 1;
    private long clientID = 0;
    private long receiverID = 0; // Default means the Producer
    private long contentLength;

    public Header() {
    }

    /**
     * Header(long clientID, long contentLength)
     *
     * @param clientID
     * @param contentLength
     */
    public Header(long clientID, long receiverID, long contentLength) {
        this.clientID = clientID;
        this.contentLength = contentLength;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public long getClientID() {
        return clientID;
    }

    public void setClientID(long clientID) {
        this.clientID = clientID;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(long receiverID) {
        this.receiverID = receiverID;
    }

    @Override
    public Header clone() throws CloneNotSupportedException {        
        Header header =  (Header) super.clone();
        header.setReceiverID(0);
        header.setContentLength(0);
        return header;
    }
    
    

    @Override
    public String toString() {
        return "Header{" + "version=" + version + ", clientID=" + clientID + ", receiverID=" + receiverID + ", contentLength=" + contentLength + '}';
    }

}
