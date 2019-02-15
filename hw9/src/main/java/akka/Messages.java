package akka;

import akka.actor.ActorRef;

/**
 * Messages that are passed around the actors are usually immutable classes.
 * Think how you go about creating immutable classes:) Make them all static
 * classes inside the Messages class.
 * <p>
 * This class should have all the immutable messages that you need to pass
 * around actors. You are free to add more classes(Messages) that you think is
 * necessary
 *
 * @author akashnagesh
 */
public class Messages {
    public final String message;
    public int round = -1;
    public String filename = null;
    public double value;
    public ActorRef a ,b;

    public Messages(String message) {
        this.message = message;
    }

    public Messages(String message, int round) {
        this.message = message;
        this.round = round;
    }

    public Messages(String message, String filename) {
        this.message = message;
        this.filename = filename;
    }

    public Messages(String message, double value){
        this.message = message;
        this.value = value;
    }

    public Messages(String message, ActorRef a, ActorRef b){
        this.message = message;
        this.a = a;
        this.b = b;
    }
}