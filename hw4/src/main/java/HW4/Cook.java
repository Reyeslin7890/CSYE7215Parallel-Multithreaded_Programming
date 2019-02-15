package HW4;


import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
    private final String name;
    public static LinkedList<Customer> placeOrder = new LinkedList<Customer>();
    public static LinkedList<Customer> completeOrder = new LinkedList<Customer>();

    /**
     * You can feel free modify this constructor.  It must
     * take at least the name, but may take other parameters
     * if you would find adding them useful.
     *
     * @param: the name of the cook
     */
    public Cook(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    /**
     * This method executes as follows.  The cook tries to retrieve
     * orders placed by Customers.  For each order, a List<Food>, the
     * cook submits each Food item in the List to an appropriate
     * Machine, by calling makeFood().  Once all machines have
     * produced the desired Food, the order is complete, and the Customer
     * is notified.  The cook can then go to process the next order.
     * If during its execution the cook is interrupted (i.e., some
     * other thread calls the interrupt() method on it, which could
     * raise InterruptedException if the cook is blocking), then it
     * terminates.
     */


    /**
     * Invarient:	Cooks' names should be different.
     * Precondition:  Cook receives an order from a customer.
     * Postcondition: Cook places the order to corresponding machines, and give the food to the customer after finishing cooking.
     * Exception:  Cook waits for a new customer.
     */
    private boolean allMachinesAreFull() {
        if (Simulation.grill.numFood < Simulation.grill.machineCapacity) return false;
        if (Simulation.frier.numFood < Simulation.frier.machineCapacity) return false;
        if (Simulation.star.numFood < Simulation.star.machineCapacity) return false;
        return true;
    }

    public void run() {

        Simulation.logEvent(SimulationEvent.cookStarting(this));
        try {
            while (true) {
                Customer c;
                synchronized (placeOrder) {
                    while (placeOrder.size() == 0) placeOrder.wait();
                    if (!allMachinesAreFull()) c = placeOrder.remove(0);
                    else {
                        c = placeOrder.get(0);
                        int index = 0;
                        for (Customer customer : placeOrder)
                            if (customer.getPriorty() < c.getPriorty()) {
                                c = customer;
                                index = placeOrder.indexOf(customer);
                            }
                        c = placeOrder.remove(index);
                    }
                }
                Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, c.getOrder(), c.getOrderNum()));

                int numburger = 0;
                int numfries = 0;
                int numcoffee = 0;
                FutureTask<Boolean> burger = null;
                FutureTask<Boolean> fries = null;
                FutureTask<Boolean> coffee = null;

                for (Food f : c.getOrder()) {
                    if (f.name.equals("burger")) numburger++;
                    if (f.name.equals("fries")) numfries++;
                    if (f.name.equals("coffee")) numcoffee++;
                }

                if (numburger > 0) {
                    //Simulation.logEvent(SimulationEvent.cookStartedFood(this, FoodType.burger, c.getOrderNum()));
                    final int finalNumburger = numburger;
                    Callable<Boolean> cburger = new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            return Simulation.grill.makeFood(finalNumburger);
                        }
                    };
                    burger = new FutureTask<Boolean>(cburger);
                    new Thread(burger).start();
                }
                if (numfries > 0) {

                    //Simulation.logEvent(SimulationEvent.cookStartedFood(this, FoodType.fries, c.getOrderNum()));
                    final int finalNumfries = numfries;
                    Callable<Boolean> cfries = new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            return Simulation.frier.makeFood(finalNumfries);
                        }
                    };
                    fries = new FutureTask<Boolean>(cfries);
                    new Thread(fries).start();
                }
                if (numcoffee > 0) {
                    //Simulation.logEvent(SimulationEvent.cookStartedFood(this, FoodType.coffee, c.getOrderNum()));
                    final int finalNumcoffee = numcoffee;
                    Callable<Boolean> ccoffee = new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            return Simulation.star.makeFood(finalNumcoffee);
                        }
                    };
                    coffee = new FutureTask<Boolean>(ccoffee);
                    new Thread(coffee).start();
                }

                if (numburger > 0 && burger.get()) {
                    //Simulation.logEvent(SimulationEvent.cookFinishedFood(this, FoodType.burger, c.getOrderNum()));
                }
                if (numfries > 0 && fries.get()) {
                    //Simulation.logEvent(SimulationEvent.cookFinishedFood(this, FoodType.fries, c.getOrderNum()));
                }
                if (numcoffee > 0 && coffee.get()) {
                    //Simulation.logEvent(SimulationEvent.cookFinishedFood(this, FoodType.coffee, c.getOrderNum()));
                }

                synchronized (completeOrder) {
                    completeOrder.add(c);
                    completeOrder.notifyAll();
                }
                Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, c.getOrderNum()));


            }
        } catch (InterruptedException e) {
            // This code assumes the provided code in the Simulation class
            // that interrupts each cook thread when all customers are done.
            // You might need to change this if you change how things are
            // done in the Simulation class.
            Simulation.logEvent(SimulationEvent.cookEnding(this));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}