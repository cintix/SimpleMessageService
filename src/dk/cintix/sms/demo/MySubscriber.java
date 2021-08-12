/*
 */
package dk.cintix.sms.demo;

import dk.cintix.sms.Subscriber;
import dk.cintix.sms.messages.DomainModelA;

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

    public MySubscriber(String host, int port, Integer... filter) {
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
