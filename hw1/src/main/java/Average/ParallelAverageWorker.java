package Average;

import java.util.LinkedList;

public class ParallelAverageWorker extends Thread {
    protected LinkedList<Integer> list;
    static public int elementsNum;
    protected long partialTotal = 0;
    public ParallelAverageWorker(){}
    public ParallelAverageWorker(LinkedList<Integer> list) {
        this.list = list;
    }


    public void run() {
        while (true) {
            int number;

            synchronized(list) {
                if (list.isEmpty())
                    return;
                number = list.remove();
                elementsNum++;
            }

            partialTotal+= number;
        }
    }

    public double getAverage() {
        return (double)partialTotal/(double)elementsNum;
    }

}
