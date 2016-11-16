/*
 */
package dk.cintix.sms.demo;

import dk.cintix.sms.Subscriber;
import dk.cintix.sms.messages.DomainModelA;
import dk.cintix.sms.messages.MessageType;

/**
 *
 * @author migo
 */
public class MySubscriber extends Subscriber<DomainModelA> {

    private int randomNr = (int) (Math.random()  * 400);
    private int counter = 0;
    public MySubscriber(String host, int port) {
        super(host, port);
    }

    public MySubscriber(String host, int port, MessageType... filter) {
        super(host, port, filter);
    }

    @Override
    public void onMessage(DomainModelA message) {
        
        counter++;
        
        if (counter == randomNr) {
            DomainModelA domainModelA = new DomainModelA();
            domainModelA.setTitle("Thank you jerkoff");
            sendMessage(domainModelA);
        }
        System.out.println("incoming textmessage " + message.toString()+ " created " + message.getSent() + " - " + this.toString());
    }

}
