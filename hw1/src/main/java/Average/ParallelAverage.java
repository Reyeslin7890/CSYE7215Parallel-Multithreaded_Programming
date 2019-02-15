package Average;
import java.util.*;

/**
 * This class runs <code>numThreads</code> instances of
 * <code>ParallelMaximizerWorker</code> in parallel to find the average
 * <code>Integer</code> in a <code>LinkedList</code>.
 */
public class ParallelAverage {

    ArrayList<ParallelAverageWorker> workers;

    public ParallelAverage(int numThreads) {
        workers = new ArrayList<ParallelAverageWorker>();
        for (int i=0; i< numThreads; i++)
            workers.add(new ParallelAverageWorker());
    }



    public static void main(String[] args) {
        int numThreads = 4;
        int numElements = 100;

        ParallelAverage average = new ParallelAverage(numThreads);
        LinkedList<Integer> list = new LinkedList<Integer>();

        for (int i=0; i<numElements; i++)
            list.add(i);

        try {
            System.out.println(average.getAverage(list));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public double getAverage(LinkedList<Integer> list) throws InterruptedException {
        double average = 0;

        System.out.println(workers.size());
        for (int i=0; i < workers.size(); i++) {
            workers.set(i, new ParallelAverageWorker(list));
            workers.get(i).start();
        }
        for (int i=0; i<workers.size(); i++)
            workers.get(i).join();

        for (int i=0; i< workers.size(); i++){
            double partAve = workers.get(i).getAverage();
            average += partAve;
            System.out.println(i + " : " + partAve);
        }
        return average;
    }

}

