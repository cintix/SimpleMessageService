package dk.cintix.sms.messages;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migo
 */
public class QueueModel extends Message {

    private String entity;

    public QueueModel(String entity) {
        this.entity = entity;
    }

    public QueueModel(String entity, int type) {
        this.entity = entity;
        setType(type);
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(entity.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            Logger.getLogger(QueueModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
   

    @Override
    public String toString() {
        return "QueueModel{" + "entity=" + entity + '}';
    }

}
