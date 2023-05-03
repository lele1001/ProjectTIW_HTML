package dao;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import beans.Article;

public class ArticleDAO {
    private final Connection connection;
    String query = "";
    PreparedStatement statement = null;
    ResultSet result = null;

    public ArticleDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Loads an image from file and encodes it into a base64 string
     */
    private static String encodeImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // this is the basePath that can be found in web.xml
        final String path = "C:\\Users\\eleon\\Documents\\universita\\TIW\\projectImages\\";
        String base64Head = "data:image/";
        final String[] splitExtension = fileName.split("\\.", 2);

        if (splitExtension.length != 2) {
            return "";
        }

        final String extension = splitExtension[1].toLowerCase();
        String base64Extension = "";

        switch (extension) {
            case "jpg":
            case "jpeg":
                base64Extension = "jpeg";
                break;
            case "png":
                base64Extension = "png";
                break;
            case "gif":
                base64Extension = "gif";
                break;
        }

        if (base64Extension.isEmpty()) {
            return "";
        }

        base64Head += base64Extension + ";base64,";
        File imageFile = new File(path + fileName);

        if (!imageFile.exists()) {
            return "";
        }

        //loads bytes, encodes the image and returns
        byte[] imageBytes = new byte[(int) imageFile.length()];

        try (FileInputStream imageInFile = new FileInputStream(imageFile)) {
            imageInFile.read(imageBytes);
            return base64Head + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Looks for the corresponding articleID in the database
     */
    public Article getArticleByID(int articleID) throws SQLException {
        Article article = null;
        query = "SELECT * FROM article WHERE articleID = ?";

        try {
            // the query contains parameters, so we use a prepared statement
            statement = connection.prepareStatement(query);

            // sets articleID as the first parameter of the query
            statement.setInt(1, articleID);
            result = statement.executeQuery();

            // returns the entire row if there is a match in the DB
            if (result.next()) {
                // creates an Article object and sets the attributes obtained from the DB
                String imageName = result.getString("image");
                article = new Article(result.getInt("articleID"), result.getInt("ownerID"), result.getString("name"),
                        result.getString("description"), imageName, result.getFloat("price"));
                article.setImage(encodeImage(imageName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            try {
                result.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
                throw new SQLException(e1);
            }

            try {
                statement.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
                throw new SQLException(e2);
            }
        }

        return article;
    }

    /**
     * Looks for the article associated to the auctionID in the database
     */
    public List<Article> getArticlesByAuctionID(int auctionID) throws SQLException {
        Article article;
        List<Article> articlesList = new ArrayList<>();
        query = "SELECT * FROM article WHERE auctionID = ?";

        try {
            // the query contains parameters, so we use a prepared statement
            statement = connection.prepareStatement(query);

            // sets articleID as the first parameter of the query
            statement.setInt(1, auctionID);
            result = statement.executeQuery();

            // returns the entire row if there is a match in the DB
            while (result.next()) {
                // creates an Article object and sets the attributes obtained from the DB
                String imageName = result.getString("image");
                article = new Article(result.getInt("articleID"), result.getInt("ownerID"), result.getString("name"),
                        result.getString("description"), imageName, result.getFloat("price"));
                article.setImage(encodeImage(imageName));
                article.setAuctionID(auctionID);

                articlesList.add(article);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            try {
                result.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
                throw new SQLException(e2);
            }

            try {
                statement.close();
            } catch (SQLException e3) {
                e3.printStackTrace();
                throw new SQLException(e3);
            }
        }

        return articlesList;
    }

    /**
     * Looks for the article associated to the userID in the database and are not
     * associated to an auction
     */
    public List<Article> getArticlesByUserIDNotInAuctions(int userID) throws SQLException {
        Article article;
        List<Article> articlesList = new ArrayList<>();
        query = "SELECT * FROM article WHERE ownerID = ? AND auctionID = 0";

        try {
            // the query contains parameters, so we use a prepared statement
            statement = connection.prepareStatement(query);

            // sets articleID as the first parameter of the query
            statement.setInt(1, userID);
            result = statement.executeQuery();

            // returns the entire row if there is a match in the DB
            while (result.next()) {
                // creates an Article object and sets the attributes obtained from the DB
                String imageName = result.getString("image");
                article = new Article(result.getInt("articleID"), result.getInt("ownerID"), result.getString("name"),
                        result.getString("description"), imageName, result.getFloat("price"));
                article.setImage(encodeImage(imageName));

                articlesList.add(article);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            try {
                result.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
                throw new SQLException(e2);
            }

            try {
                statement.close();
            } catch (SQLException e3) {
                e3.printStackTrace();
                throw new SQLException(e3);
            }
        }

        return articlesList;
    }

    /**
     * Looks for the price of an article and associates it to an auction
     */
    public float getPriceByID(int articleID, int auctionID) throws SQLException {
        query = "SELECT price FROM article WHERE articleID = ?";

        try {
            // the query contains parameters, so we use a prepared statement
            statement = connection.prepareStatement(query);

            // sets articleID as the first parameter of the query
            statement.setInt(1, articleID);
            result = statement.executeQuery();

            // it looks if the article is in the DB and returns its price
            if (result.next()) {

                // associates the article to the auction
                String query2 = "UPDATE article SET auctionID = ? WHERE articleID = ?";
                int result2;
                PreparedStatement statement2 = null;

                try {
                    statement2 = connection.prepareStatement(query2);
                    statement2.setInt(1, auctionID);
                    statement2.setInt(2, articleID);

                    result2 = statement2.executeUpdate();

                    // if a row was updated, it means the article has been added
                    if (result2 > 0) {
                        System.out.println("Article " + articleID + " associated to auction " + auctionID);
                    }
                } catch (SQLException e) {
                    try {
                        connection.rollback();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        throw new SQLException(e1);
                    }

                    e.printStackTrace();
                    throw new SQLException(e);
                } finally {
                    try {
                        assert statement2 != null;
                        statement2.close();
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                        throw new SQLException(e2);
                    }
                }

                return result.getFloat("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            try {
                result.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
                throw new SQLException(e2);
            }

            try {
                statement.close();
            } catch (SQLException e3) {
                e3.printStackTrace();
                throw new SQLException(e3);
            }
        }

        // the article is not in the DB
        return 0;
    }

    /**
     * Creates an article to insert in the database
     */
    public boolean createArticle(int articleID, int ownerID, String name, String description, String imageName,
                                 float price) throws SQLException {
        query = "INSERT INTO article (articleID, ownerID, name, description, image, price) VALUES(?, ?, ?, ?, ?, ?)";
        int result;

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, articleID);
            statement.setInt(2, ownerID);
            statement.setString(3, name);
            statement.setString(4, description);
            statement.setString(5, imageName);
            statement.setFloat(6, price);

            result = statement.executeUpdate();

            // if a row was updated, it means the article has been added
            if (result > 0) {
                return true;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
                throw new SQLException(e1);
            }
            throw new SQLException(e);
        } finally {
            try {
                statement.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
                throw new SQLException(e2);
            }
        }

        return false;
    }

    /**
     * closes an open auction
     */
    public void associateToAuction(int articleID, int auctionID) throws SQLException {
        // associates the winner to the auction
        query = "UPDATE article SET auctionID = ? WHERE articleID = ?";
        int result2;

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, auctionID);
            statement.setInt(2, articleID);
            result2 = statement.executeUpdate();

            // if a row was updated, it means the article has been added
            if (result2 > 0) {
                System.out.println("Auction " + auctionID + " associated to article " + articleID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            try {
                statement.close();
            } catch (Exception e2) {
                e2.printStackTrace();
                throw new SQLException(e2);
            }
        }
    }
}
