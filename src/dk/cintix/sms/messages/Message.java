/*
 */
package dk.cintix.sms.messages;

import dk.cintix.sms.network.protocol.Header;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migo
 */
public abstract class Message implements Serializable {

    private Header header;
    private Date sent = new Date();
    private boolean confirmed;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public final Message reply() {
        try {
            Message message = this.getClass().newInstance();
            Header localHeader = this.getHeader();
            Header outgoingHeader = new Header();

            outgoingHeader.setReceiverID(localHeader.getClientID());
            outgoingHeader.setVersion(localHeader.getVersion());

            message.setHeader(outgoingHeader);
            return message;
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Message{" + "header=" + header + ", sent=" + sent + ", confirmed=" + confirmed + '}';
    }

}
