package beans;

import java.time.LocalDateTime;

public class Offer {
    private int userID;
    private final int auctionID;
    private LocalDateTime offerDate;
    private final float price;

    public Offer(int userID, int auctionID, LocalDateTime date, float price) {
        this.userID = userID;
        this.auctionID = auctionID;
        this.offerDate = date;
        this.price = price;
    }

    public Offer(int auctionID, float price) {
        super();
        this.auctionID = auctionID;
        this.price = price;
    }

    public int getUserID() {
        return userID;
    }

    public int getAuctionID() {
        return auctionID;
    }

    public LocalDateTime getDate() {
        return offerDate;
    }

    public float getPrice() {
        return price;
    }

}
