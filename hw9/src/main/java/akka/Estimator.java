package akka;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * This is the main actor and the only actor that is created directly under the
 * {@code ActorSystem} This actor creates more child actors
 * {@code WordCountInAFileActor} depending upon the number of files in the given
 * directory structure
 *
 * @author akashnagesh
 */
public class Estimator extends UntypedActor {

    private static double g;
    private double c, c_, u, s;
    private int count, rec;
    private ActorRef counter;
    private List<ActorRef> user = new ArrayList<ActorRef>();
    public Estimator() {
        count = 0;
        rec = 0;
        c = 0;
        c_ = 0;
        u = 0;
        s = 0;
        g = 0.2;
    }

    @Override
    public void onReceive(Object msg) throws Throwable {
        Messages m = (Messages) msg;
        if (getSender().path().name().equals("Counter")) {
            if (m.message.equals("Set c value")) {
                c = m.value;
                counter = getSender();
                System.out.println(getSelf().path().name() + "'s c value has been set to " + c);
            } else {
                u = Double.parseDouble(m.message);
                c_ = c + u;
                c = g * c + (1 - g) * c_;
                s = s + u;
                count++;
                user.get(count-1).tell(new Messages(getSelf().path().name(),(double)s/(double)count), getSelf());

                //System.out.println(getSelf().path().name() + " at " + count + ": s = " + s / count);
            }
        } else {
            if (m.message.equals("Start")) {
                //System.out.println(getSelf().path().name() + " is ready to go!");
                getSender().tell(new Messages(getSelf().path().name()+" is started"), getSelf());
            } else if (m.message.equals("Stop")) {
                System.out.println(getSelf().path().name() + " has stopped");
            } else {
                if (getSelf().path().name().endsWith("1")) Thread.sleep(2);
                else Thread.sleep(3);
                if (m.filename!=null) user.add(getSender());
                if (rec==count){
                    rec++;
                    counter.tell(new Messages(String.valueOf(c), count), getSelf());}
                else
                    getSelf().tell(new Messages("wait"),getSelf());
            }
        }
    }

}
