package HW4;

//@Author : Jin Lin

import java.util.LinkedList;
import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the coffee shop (only successful if the
 * coffee shop has a free table), place its order, and then leave the
 * coffee shop when the order is complete.
 */
public class Customer implements Runnable {
    //JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
    private final String name;
    private final List<Food> order;
    private final int orderNum;
    private final int priorty;


    private static int runningCounter = 0;
    private static int numCustomer = 0;
    private static int numTable;
    private static Object lock;
    private static LinkedList<Customer> customers = new LinkedList<Customer>();


    /**
     * You can feel free modify this constructor.  It must take at
     * least the name and order but may take other parameters if you
     * would find adding them useful.
     */
    public Customer(String name, List<Food> order, int priority) {
        this.name = name;
        this.order = order;
        this.orderNum = ++runningCounter;
        this.priorty = priority;
        synchronized (customers) {
            customers.add(this);
        }
    }

    public String toString() {
        return name;
    }

    public static void setNumTable(int numTable) {
        Customer.numTable = numTable;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public List<Food> getOrder() {
        return order;
    }

    public int getPriorty() {
        return priorty;
    }

    public static int getNumTable() {
        return numTable;
    }
    /**
     * This method defines what an Customer does: The customer attempts to
     * enter the coffee shop (only successful when the coffee shop has a
     * free table), place its order, and then leave the coffee shop
     * when the order is complete.
     */

    /**
     * Invarient: The number of customers in the CoffeeShop cannot exceed the number of tables in the CoffeeShop .
     * Precondition: Customers with higher priority should be served first.
     * Postcondition: Customers with higher priority can be served faster than others.
     * Exception: Dectect the exception and make it into correct order.
     */
    public void run() {
        try {
            synchronized (customers) {
                while (numCustomer >= numTable) {
                    customers.wait();
                    //if (customers.get(0) == this) break;
                }

                numCustomer++;
                Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
            }


            synchronized (Cook.placeOrder) {
                Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, order, orderNum));
                Cook.placeOrder.add(this);
                Cook.placeOrder.notifyAll();
            }


            synchronized (Cook.completeOrder) {
                while (!Cook.completeOrder.contains(this)) Cook.completeOrder.wait();
                Cook.completeOrder.remove(this);
            }
            Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, order, orderNum));

            Thread.sleep(200);
            synchronized (customers) {
                Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
                numCustomer--;
                customers.remove(0);
                customers.notifyAll();
            }

        } catch (InterruptedException e) {
            Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
        }


    }
}