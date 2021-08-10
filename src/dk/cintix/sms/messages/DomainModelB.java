/*
 */
package dk.cintix.sms.messages;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author migo
 */
public class DomainModelB extends Message {
    private String entity;

    public DomainModelB(String entity) {
        this.entity = entity;
    }

    public DomainModelB(String entity, MessageType type) {
        this.entity = entity;
        setType(type);
    }

    public DomainModelB() {
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
            return new String(hash);
        } catch (Exception ex) {
            Logger.getLogger(DomainModelB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "DomainModelB{" + "entity=" + entity + '}';
    }
    
}
