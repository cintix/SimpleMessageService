/*
 */
package dk.cintix.sms.messages;

/**
 *
 * @author migo
 */
public class TextMessage extends Message{
    
    private String message;

    public TextMessage() {
    }

    public TextMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TextMessage{" + "message=" + message + '}';
    }    
    
}
