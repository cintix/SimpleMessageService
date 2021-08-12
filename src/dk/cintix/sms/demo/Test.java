/*
 */
package dk.cintix.sms.demo;

import dk.cintix.sms.messages.DomainModelA;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author migo
 */
public class Test {

    public static void main(String[] args) throws IOException {

        MyProducer producer = new MyProducer(1122);
        MySubscriber a = new MySubscriber("127.0.0.1", 1122);
        MySubscriber b = new MySubscriber("127.0.0.1", 1122, MessageType.PROGRAM);       
        MySubscriber b1 = new MySubscriber("127.0.0.1", 1122, MessageType.IMAGE);
//        MySubscriber b2 = new MySubscriber("127.0.0.1", 1122);
//        MySubscriber b3 = new MySubscriber("127.0.0.1", 1122);
//        MySubscriber b4 = new MySubscriber("127.0.0.1", 1122);
//        MySubscriber b5 = new MySubscriber("127.0.0.1", 1122);
//        MySubscriber b6 = new MySubscriber("127.0.0.1", 1122);
//        MySubscriber b7 = new MySubscriber("127.0.0.1", 1122);

        boolean sent = false;
        try {
            a.startService();
            b.startService();
            b1.startService();
            TimeUnit.SECONDS.sleep(3);
            producer.startService();

//            b2.startService();
//            b3.startService();
//            b4.startService();
//            b5.startService();
//            b6.startService();
//            b7.startService();
            
            long counter = 1;
            while (1 == 1 && counter < 5000)  {
                //System.out.println("...");
               // TimeUnit.MILLISECONDS.sleep(1300);
                if (!sent) {
                    DomainModelA message = new DomainModelA();
                    message.setId((int) counter);
                    message.setType(MessageType.PROGRAM);
                    boolean broadcastMessage = producer.broadcastMessage(message);
                    //System.out.println("Notifing went " + broadcastMessage);
                    //sent = true;
                    counter++;
                    
                    if (counter == 1000) {
                        System.out.println(" Yup 1000 done....");
                        counter = 1;
                    }
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            a.stopService();
            b.stopService();
            b1.stopService();
//            b2.stopService();
//            b3.stopService();
//            b4.stopService();
//            b5.stopService();
//            b6.stopService();
//            b7.stopService();
            
            producer.stopService();
        }

    }

}
