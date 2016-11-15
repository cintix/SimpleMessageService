/*
 */
package dk.cintix.sms.demo;

import dk.cintix.sms.Producer;
import dk.cintix.sms.messages.DomainModelA;
import java.io.IOException;

/**
 *
 * @author migo
 */
public class MyProducer extends Producer<DomainModelA> {

    public MyProducer(int port) throws IOException {
        super(port);
    }


}
