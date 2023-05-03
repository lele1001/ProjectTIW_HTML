package beans;

public class Article {
    private final int articleID;
    private final int ownerID;
    private final String name;
    private final String description;
    private String image;
    private final String imagePath;
    private final float price;
    private int auctionID;

    public Article(int articleID, int ownerID, String name, String description, String imagePath, float price) {
        this.articleID = articleID;
        this.ownerID = ownerID;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.price = price;
        this.image = null;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setAuctionID(int auctionID) {
        this.auctionID = auctionID;
    }

    public int getArticleID() {
        return articleID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getImage() {
        return image;
    }

    public float getPrice() {
        return price;
    }

    public int getAuctionID() {
        return auctionID;
    }
}
