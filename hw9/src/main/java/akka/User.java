package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for your estimation actor system.
 *
 * @author akashnagesh
 */
public class User {
    static String dir = "D:/NEU/7215 Parallel & Multithreaded Prog/akka/Akka_Text/";
    static ArrayList<String> filenames;

    public static String readfile(String filename) {
        String txt = "";
        String line = null;
        try {
            BufferedReader in;
            in = new BufferedReader(new FileReader(dir + filename));
            line = in.readLine();
            txt += line;
            while (line != null) {
                line = in.readLine();
                txt += line;
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return txt;
    }

    public static void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                filenames.add(fileEntry.getName());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create("EstimationSystem");
        Props estimator1Props = Props.create(Estimator.class);
        Props estimator2Props = Props.create(Estimator.class);
        Props counterProps = Props.create(FirstCounter.class);
        ActorRef estimator1 = system.actorOf(estimator1Props, "Estimator1");
        ActorRef estimator2 = system.actorOf(estimator2Props, "Estimator2");
        ActorRef counter = system.actorOf(counterProps, "Counter");

        try {
            Future<Object> f = Patterns.ask(estimator1, new Messages("Start"), 5000);
            Timeout timeout = new Timeout(Duration.create(10, "seconds"));
            Messages s = (Messages) Await.result(f, timeout.duration());
            System.out.println(s.message);
            f = Patterns.ask(estimator2, new Messages("Start"), 5000);
            s = (Messages) Await.result(f, timeout.duration());
            System.out.println(s.message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        counter.tell(new Messages("Start", estimator1, estimator2), null);

        int num = 10;
        List<Future<Object>> f1 = new ArrayList<Future<Object>>();
        List<Future<Object>> f2 = new ArrayList<Future<Object>>();

        //read all filenames in directory "dir"
        filenames = new ArrayList<String>();
        final File folder = new File(dir);
        listFilesForFolder(folder);

        //read files end with txt
        for (String filename : filenames)
            if (filename.endsWith("txt")) {
                String txt = readfile(filename);
                counter.tell(new Messages(txt, filename), null);
                Future<Object> future1 = Patterns.ask(estimator1, new Messages(txt, filename), 10000);
                Future<Object> future2 = Patterns.ask(estimator2, new Messages(txt, filename), 5000);
                f1.add(future1);
                f2.add(future2);
            }

        int count1 = 0;
        int count2 = 0;
        while (count1 < num || count2 < num) {
            try {
                if (count1 < f1.size() && f1.get(count1).isCompleted()) {
                    Future<Object> f = f1.get(count1);
                    Timeout timeout = new Timeout(Duration.create(10, "seconds"));
                    Messages m = (Messages) Await.result(f, timeout.duration());
                    System.out.println("Round " + (count1+1) + " Estimator1: s = " + m.value);
                    count1++;
                }
                if (count2 < f2.size() && f2.get(count2).isCompleted()) {
                    Future<Object> f = f2.get(count2);
                    Timeout timeout = new Timeout(Duration.create(10, "seconds"));
                    Messages m = (Messages) Await.result(f, timeout.duration());
                    System.out.println("Round " + (count2+1) + " Estimator2: s = " + m.value);
                    count2++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        estimator1.tell(new Messages("Stop"), null);
        estimator2.tell(new Messages("Stop"), null);
        system.terminate();
        /*
         * Create the Estimator Actor and send it the StartProcessingFolder
         * message. Once you get back the response, use it to print the result.
         * Remember, there is only one actor directly under the ActorSystem.
         * Also, do not forget to shutdown the actorsystem
         */

    }

}
