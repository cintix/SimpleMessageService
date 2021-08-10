/*
 */
package dk.cintix.sms.messages;

import java.io.Serializable;

/**
 *
 * @author migo
 */
public enum MessageType implements Serializable {
    PROGRAM, IMAGE, SCHEDULE,
    PROGRAMRESPONSE, IMAGERESPONSE, SCHEDULERESPONSE
}
