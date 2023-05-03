package beans;

import java.time.LocalDateTime;

public class Auction {
    private final int auctionID;
    private final int ownerID;
    private final String title;
    private final float startingPrice;
    private final float minIncrease;
    private final LocalDateTime expiryDate; // format: yyyy-MM-dd hh:mm:ss
    private int winnerID;
    private float actualPrice;
    private int isClosed;

    public Auction(int auctionID, int ownerID, String title, float startingPrice, float minIncrease,
                   LocalDateTime expiryDate, float actualPrice, int isClosed) {
        this.auctionID = auctionID;
        this.ownerID = ownerID;
        this.title = title;
        this.startingPrice = startingPrice;
        this.minIncrease = minIncrease;
        this.expiryDate = expiryDate;
        this.actualPrice = actualPrice;
        this.winnerID = 0;
        this.isClosed = isClosed;
    }

    public void setWinnerID(int winnerID) {
        this.winnerID = winnerID;
    }

    public void setActualPrice(float actualPrice) {
        this.actualPrice = actualPrice;
    }

    public int isClosed() {
        return isClosed;
    }

    public void setClosed(int isClosed) {
        this.isClosed = isClosed;
    }

    public int getAuctionID() {
        return auctionID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public String getTitle() {
        return title;
    }

    public float getStartingPrice() {
        return startingPrice;
    }

    public float getMinIncrease() {
        return minIncrease;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public int getWinnerID() {
        return winnerID;
    }

    public float getActualPrice() {
        return actualPrice;
    }
}
