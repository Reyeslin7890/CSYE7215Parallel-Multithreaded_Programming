package Average;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;

public class TestAverage {

    private int	threadCount = 9;
    private ParallelAverage ave = new ParallelAverage(threadCount);

    @Test
    public void compareAve() {
        int size = 100001;
        LinkedList<Integer> list = new LinkedList<Integer>();
        Random rand = new Random();
        double total = 0;
        double paralleltotal = 0;
        for (int i=0; i<size; i++) {
            int next = rand.nextInt();
            list.add(next);
            total += next;
        }
        total/=size;
        try {
            paralleltotal = ave.getAverage(list);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("The test failed because the max procedure was interrupted unexpectedly.");
        } catch (Exception e) {
            e.printStackTrace();
            fail("The test failed because the max procedure encountered a runtime error: " + e.getMessage());
        }
        assertEquals("The serial total doesn't match the parallel total", total, paralleltotal, 0.001);
    }
}
