package hw2;

/**
 * @Jin Lin
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AuctionServer {
    /**
     * Singleton: the following code makes the server a Singleton. You should
     * not edit the code in the following noted section.
     * <p>
     * For test purposes, we made the constructor protected.
     */

    /* Singleton: Begin code that you SHOULD NOT CHANGE! */
    protected AuctionServer() {
    }

    private static AuctionServer instance = new AuctionServer();

    public static AuctionServer getInstance() {
        return instance;
    }

    /* Singleton: End code that you SHOULD NOT CHANGE! */





    /* Statistic variables and server constants: Begin code you should likely leave alone. */


    /**
     * Server statistic variables and access methods:
     */
    private int soldItemsCount = 0;
    private int revenue = 0;
    protected double bias = 2;
    public int soldItemsCount() {
        return this.soldItemsCount;
    }
    public int revenue() {
        return this.revenue;
    }


    /**
     * Server restriction constants:
     */
    public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
    public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
    public static final int serverCapacity = 80; // The maximum number of active items at a given time.


    /* Statistic variables and server constants: End code you should likely leave alone. */


    /**
     * Some variables we think will be of potential use as you implement the server...
     */

    // List of items currently up for bidding (will eventually remove things that have expired).
    private List<Item> itemsUpForBidding = new ArrayList<Item>();


    // The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
    private int lastListingID = -1;

    // List of item IDs and actual items.  This is a running list with everything ever added to the auction.
    private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

    // List of itemIDs and the highest bid for each item.  This is a running list with everything ever added to the auction.
    private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

    // List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
    private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>();


    // List of sellers and how many items they have currently up for bidding.
    private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

    // List of buyers and how many items on which they are currently bidding.
    private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();


    // Object used for instance synchronization if you need to do it at some point
    // since as a good practice we don't use synchronized (this) if we are doing internal
    // synchronization.
    //
    // private Object instanceLock = new Object();









    /*
     *  The code from this point forward can and should be changed to correctly and safely
     *  implement the methods as needed to create a working multi-threaded server for the
     *  system.  If you need to add Object instances here to use for locking, place a comment
     *  with them saying what they represent.  Note that if they just represent one structure
     *  then you should probably be using that structure's intrinsic lock.
     */


    /**
     * Attempt to submit an <code>Item</code> to the auction
     *
     * @param sellerName         Name of the <code>Seller</code>
     * @param itemName           Name of the <code>Item</code>
     * @param lowestBiddingPrice Opening price
     * @param biddingDurationMs  Bidding duration in milliseconds
     * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
     */


    // Invarient:  The item must be a new item;
    //				biddingDuration	must not be a negative integer;
    // Precondition:	The seller and the item can be identified;
    //					Currently item total does not exceed the serverCapacity;
    //					The item has never been added to the auction;
    //					The number of items the seller adds on the server does not exceed the maximum number of items;
    //					lowestBiddingPrice does not exceed 100;
    //	Postcondition:  when satisfied the precondition, auction server will return the item's ID; otherwise, return -1.
    synchronized public int submitItem(String sellerName, String itemName, int lowestBiddingPrice, int biddingDurationMs) {
        // TODO: IMPLEMENT CODE HERE
        // Some reminders:
        //   Make sure there's room in the auction site.
        //   If the seller is a new one, add them to the list of sellers.
        //   If the seller has too many items up for bidding, don't let them add this one.
        //   Don't forget to increment the number of things the seller has currently listed.

        synchronized (itemsUpForBidding) {
            if (itemsUpForBidding.size() >= serverCapacity) return -1;
            for (Item i: itemsUpForBidding)
                if (i.name().equals(itemName)) return -1;
        }

        synchronized ((itemsPerSeller)) {
            if (!itemsPerSeller.containsKey(sellerName))
                itemsPerSeller.put(sellerName, 0);
            else if (itemsPerSeller.get(sellerName) >= maxSellerItems) return -1;
        }
        if (biddingDurationMs <= 0) return -1;
        if (lowestBiddingPrice > 100) return -1;

        synchronized (itemsUpForBidding) {
            synchronized (itemsAndIDs) {
                synchronized (itemsPerSeller) {
                    lastListingID++;
                    Item newitem = new Item(sellerName, itemName, lastListingID, lowestBiddingPrice, biddingDurationMs);
                    itemsAndIDs.put(lastListingID, newitem);
                    itemsUpForBidding.add(newitem);
                    itemsPerSeller.put(sellerName, itemsPerSeller.get(sellerName) + 1);
                }
            }
        }
        //System.out.println(sellerName + " submit Item " + lastListingID);
        return lastListingID;
    }


    /**
     * Get all <code>Items</code> active in the auction
     * @return A copy of the <code>List</code> of <code>Items</code>
     */

    /**
     * Invarient: Items for bidding currently must be on the list.
     * Precondition: Items cannot be modified by others;
     * Postcondition:Return the list of the items now for bidding.
     */
    synchronized public List<Item> getItems() {
        // TODO: IMPLEMENT CODE HERE
        // Some reminders:
        //    Don't forget that whatever you return is now outside of your control.
        List<Item> items = new ArrayList<Item>();

        synchronized (itemsUpForBidding) {
            int index = 0;
            while (itemsUpForBidding.size() > 0 && index < itemsUpForBidding.size()) {
                Item i = itemsUpForBidding.get(index);
                if (!i.biddingOpen()) {
                    if (!itemUnbid(i.listingID())) soldItemsCount++;
                    itemsPerSeller.put(i.seller(), itemsPerSeller.get(i.seller()) - 1);
                    itemsUpForBidding.remove(index);
                    index--;
                }
                index++;
            }

            items.addAll(itemsUpForBidding);
        }

        return items;
    }


    /**
     * Attempt to submit a bid for an <code>Item</code>
     * @param bidderName Name of the <code>Bidder</code>
     * @param listingID Unique ID of the <code>Item</code>
     * @param biddingAmount Total amount to bid
     * @return True if successfully bid, false otherwise
     */

    /**
     * Buyer Bias Strategy: if the buyer has a valid bid on an item with triple price or higher of the highest bid,
     * the buyer can buy the item immediately and the item closed.
     */
    // invarient:	the item of the bid must be on the itemlist.
    //	precondition:	the bidder is valid;
    //					the bidder has enough resource to bid;
    //					the item can be bid on;
    //					the highest bidder of the item is not the bidder;
    //					the biddingAmount is greater than the highestBid of the item;
    // postcondition:	if the bid satisfies the precondition, return true; else return false;
    synchronized public boolean submitBid(String bidderName, int listingID, int biddingAmount) {
        // TODO: IMPLEMENT CODE HERE
        // Some reminders:
        //   See if the item exists.
        //   See if it can be bid upon.
        //   See if this bidder has too many items in their bidding list.
        //   Get current bidding info.
        //   See if they already hold the highest bid.
        //   See if the new bid isn't better than the existing/opening bid floor.
        //   Decrement the former winning bidder's count
        //   Put your bid in place
        // if (item not in items)
        Item i = null;

        synchronized (itemsUpForBidding) {
            for (Item item : itemsUpForBidding)
                if (item.equals(itemsAndIDs.get(listingID))) {
                    i = item;
                    break;
                }
            if (i == null) return false;
        }


        // if (bidder.money<biddingAmount || itemsPerBuyer.get(bidderName) >= maxBidCount)
        synchronized (itemsPerBuyer) {
            if (!itemsPerBuyer.containsKey(bidderName)) itemsPerBuyer.put(bidderName, 0);
            if (itemsPerBuyer.get(bidderName) >= maxBidCount) return false;
        }

        // if (highestBidders.get(listingID).equals(bidderName))
        //		return false;
        synchronized (highestBids) {
            synchronized (highestBidders) {
                if (highestBidders.containsKey(listingID))
                    if (highestBidders.get(listingID).equals(bidderName)) return false;
                if (highestBids.containsKey(listingID))
                    if (highestBids.get(listingID) >= biddingAmount) return false;
            }
        }

        //itemsPerBuyer.get(bidderName)++;
        //itemsPerBuyer.get(highestBidders.get(listingID))--;
        synchronized (highestBids) {
            synchronized (highestBidders) {
                synchronized (itemsPerBuyer) {
                    itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) + 1);
                    if (highestBidders.containsKey(listingID))
                        itemsPerBuyer.put(highestBidders.get(listingID), itemsPerBuyer.get(highestBidders.get(listingID)) - 1);
                    int oldHighestBid = itemsAndIDs.get(listingID).lowestBiddingPrice();
                    if (highestBids.get(listingID) != null) oldHighestBid = highestBids.get(listingID);
                    highestBids.put(listingID, biddingAmount);
                    highestBidders.put(listingID, bidderName);
                    if (itemsAndIDs.get(listingID).lowestBiddingPrice() + bias <= biddingAmount) {
                        itemsPerSeller.put(i.seller(),itemsPerSeller.get(i.seller())-1);
                        itemsUpForBidding.remove(i);
                        soldItemsCount++;
                        itemsPerBuyer.put(bidderName, itemsPerBuyer.get(bidderName) - 1);
                    }
                    revenue += biddingAmount - oldHighestBid;
                }
            }
        }
        //oldhighestbid = highestbid.get(listingID);
        //highestBid.put(listingID, biddingAmount);
        //highestBidder.put(listingID, bidderName);
        //if (biddingAmount >= oldhighestBid)
        //	{items.remove(item);
        //	itemsPerBuyer.get(bidderName)--;
        //	serverHoldingItems--;}
        //System.out.println(bidderName + " submit Bid " + listingID + " Revenue: " + revenue);
        return true;
    }

    /**
     * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
     *
     * @param bidderName Name of <code>Bidder</code>
     * @param listingID  Unique ID of the <code>Item</code>
     * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
     * 2 (open) if this <code>Item</code> is still up for auction<br>
     * 3 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
     */

    // 	invarient:	item must exist in itemsAndIDs.
    public int checkBidStatus(String bidderName, int listingID) {
        // TODO: IMPLEMENT CODE HERE

        // Some reminders:
        //   If the bidding is closed, clean up for that item.
        //     Remove item from the list of things up for bidding.
        //     Decrease the count of items being bid on by the winning bidder if there was any...
        //     Update the number of open bids for this seller

        //System.out.println(bidderName + " in checkBidStatus");
        if (itemsAndIDs.get(listingID) == null) return 3;

        // if (item not in items)
        synchronized (itemsUpForBidding) {
            synchronized (itemsAndIDs) {
                synchronized (itemsPerBuyer) {
                    synchronized (itemsPerSeller) {
                        Item i = itemsAndIDs.get(listingID);
                        if (itemsUpForBidding.contains(i))
                            if (i.biddingOpen()) return 2;
                            else {
                                itemsUpForBidding.remove(i);
                                if (highestBids.get(listingID) != null) {
                                    //revenue += highestBids.get(listingID) - i.lowestBiddingPrice();
                                    itemsPerBuyer.put(highestBidders.get(listingID), itemsPerBuyer.get(highestBidders.get(listingID)) - 1);
                                }
                                itemsPerSeller.put(i.seller(), itemsPerSeller.get(i.seller()) - 1);
                            }
                    }
                }
            }
        }
        if (highestBidders.get(listingID).equals(bidderName)) return 1;
        return 3;
    }

    /**
     * Check the current bid for an <code>Item</code>
     *
     * @param listingID Unique ID of the <code>Item</code>
     * @return The highest bid so far or the opening price if no bid has been made,
     * -1 if no <code>Item</code> exists
     */

    //	Invarient:	item must be in the itemList;
    synchronized public int itemPrice(int listingID) {
        // TODO: IMPLEMENT CODE HERE
        //boolean flag = itemsAndIDs.get(listingID)==null;
        //if (flag) return -1;
        //if (itemUnbid()) return item.lowestBiddingPrice;
        //else return highestBid.get(listingID)
        //System.out.println(" in itemPrice");
        if (itemsAndIDs.get(listingID) == null) return -1;
        if (highestBidders.get(listingID) == null) return itemsAndIDs.get(listingID).lowestBiddingPrice();
        return highestBids.get(listingID);
    }

    /**
     * Check whether an <code>Item</code> has been bid upon yet
     *
     * @param listingID Unique ID of the <code>Item</code>
     * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
     */

    //Invarient:	itemsUpForBidding is not empty
    //Precondition:	listingId is not in the highestBid
    //					while checking itemUnbid(), there is no other bids for the item.
    //Postcondition:	if listingId is not in the highestBid then return true; otherwise false.
    public Boolean itemUnbid(int listingID) {
        // TODO: IMPLEMENT CODE HERE
        //System.out.println(" in itemUnbid");
        synchronized (itemsUpForBidding) {
            synchronized (highestBids) {
                if (!itemsUpForBidding.contains(itemsAndIDs.get(listingID))) return false;
                if (highestBids.get(listingID) != null) return false;
            }
        }
        return true;
    }


}
 