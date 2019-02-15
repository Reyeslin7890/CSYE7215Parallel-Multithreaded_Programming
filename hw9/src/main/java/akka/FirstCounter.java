package akka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * this actor reads the file, counts the vowels and sends the result to
 * Estimator.
 *
 * @author akashnagesh
 */
public class FirstCounter extends UntypedActor {

    private int round;
    private List<Double> p;

    public FirstCounter() {
        // TODO Auto-generated constructor stub
        p = new ArrayList<Double>();
    }

    private int vowelCounter(String txt) {
        int tot = 0;
        txt = txt.toLowerCase();
        for (int i = 0; i < txt.length(); i++) {
            char asc = txt.charAt(i);
            if (asc == 'a' || asc == 'e' || asc == 'i' || asc == 'o' || asc == 'u' || asc == 'y') tot++;
        }
        return tot;
    }

    private int letterCounter(String txt) {
        int tot = 0;
        txt = txt.toUpperCase();
        for (int i = 0; i < txt.length(); i++) {
            int asc = (int) txt.charAt(i);
            if (asc >= 65 && asc <= 90) tot++;
        }
        return tot;
    }

    @Override
    public void onReceive(Object msg) throws Throwable {
        Messages m = (Messages) msg;
        if (getSender().path().name().equals("deadLetters")) {
            if (m.message.equals("Start")) {
                Random r = new Random();
                m.a.tell(new Messages("Set c value", (double)0.401), getSelf());
                m.b.tell(new Messages("Set c value", (double)0.400), getSelf());
            } else {
                String txt = m.message;
                int l = letterCounter(txt);
                int v = vowelCounter(txt);
                p.add((double) v / (double) l);

            }
        } else {
            int index = m.round;
            if (index < p.size() && index>=0) {
                double getp = p.get(index);
                double c = Double.parseDouble(m.message);
                double u = getp - c;
                getSender().tell(new Messages(String.valueOf(u)), getSelf());
            }
        }
    }

}
