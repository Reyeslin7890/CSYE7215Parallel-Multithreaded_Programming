package HW4;

//@Author : Jin Lin


/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeMS milliseconds to
 * produce.
 */
public class Machine {
    public final String machineName;
    public final Food machineFoodType;
    public final int machineCapacity;
    public volatile static int numFood;
    //YOUR CODE GOES HERE...


    /**
     * The constructor takes at least the name of the machine,
     * the Food item it makes, and its capacity.  You may extend
     * it with other arguments, if you wish.  Notice that the
     * constructor currently does nothing with the capacity; you
     * must add code to make use of this field (and do whatever
     * initialization etc. you need).
     */
    public Machine(String nameIn, Food foodIn, int capacityIn) {
        this.machineName = nameIn;
        this.machineFoodType = foodIn;
        this.numFood = 0;
        this.machineCapacity = capacityIn;
        Simulation.logEvent(SimulationEvent.machineStarting(this, foodIn, capacityIn));
        //YOUR CODE GOES HERE...

    }


    /**
     * This method is called by a Cook in order to make the Machine's
     * food item.  You can extend this method however you like, e.g.,
     * you can have it take extra parameters or return something other
     * than Object.  It should block if the machine is currently at full
     * capacity.  If not, the method should return, so the Cook making
     * the call can proceed.  You will need to implement some means to
     * notify the calling Cook when the food item is finished.
     */


    /**
     * Invarient: The number of food that machine is dealing with cannot exceed the capacity of machine
     * Precondition: create a CookAnItem thread and wait until it is finished
     * Postcondition: return "success"
     * InterruptedException: current CookAnItem thread was interrupted by higher priority thread, wait for space and cook again.
     * Exception: return "fail"
     */
    public boolean makeFood(int count) throws InterruptedException {
        //YOUR CODE GOES HERE...
        try {
            synchronized (Machine.class) {
                while (numFood + count > machineCapacity) {
                    Machine.class.wait();
                }
                numFood += count;
            }
            Simulation.logEvent(SimulationEvent.machineCookingFood(this, machineFoodType));

            Thread t = new Thread(new CookAnItem(count));
            t.start();
            t.join();

            synchronized (Machine.class){
                numFood -= count;
                Machine.class.notifyAll();
            }
            Simulation.logEvent(SimulationEvent.machineDoneFood(this, machineFoodType));
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }


    //THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
    private class CookAnItem implements Runnable {
        private int count;

        CookAnItem(int count) {
            this.count = count;
        }

        /***
         Precondition: each food items is limited to 3 at most and wait for certain time.
         Postcondition: return "success".
         Exception: return "fail".
         InterruptedException: When there is a higher priority food item and the machine is full, lower priority item thread should be
         interrupted and throw an InterruptedException.
         */
        public void run() {
            try {
                Thread.sleep(machineFoodType.cookTimeMS);
            } catch (InterruptedException e) {

            }
        }
    }


    public String toString() {
        return machineName;
    }
}