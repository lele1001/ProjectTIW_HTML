package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import beans.Auction;

public class AuctionDAO {
	private final Connection connection;
	String query = "";
	PreparedStatement statement = null;
	ResultSet result = null;

	public AuctionDAO(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Finds the open auctions owned by the user
	 */
	public List<Auction> getOpenAuctionsByUser(int userID) throws SQLException {
		query = "SELECT * FROM auction WHERE ownerID = ? AND isClosed = 0 ORDER BY expiryDate DESC";
		List<Auction> openAuctions = new ArrayList<>();

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, userID);
			result = statement.executeQuery();

			while (result.next()) {
				Auction opA = new Auction(result.getInt("auctionID"), result.getInt("ownerID"),
						result.getString("title"), result.getFloat("startingPrice"), result.getFloat("minIncrease"),
						result.getTimestamp("expiryDate").toLocalDateTime(), result.getFloat("actualPrice"),
						result.getInt("isClosed"));
				openAuctions.add(opA);
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

		return openAuctions;
	}

	/**
	 * Finds the closed auctions owned by the user
	 */
	public List<Auction> getClosedAuctionsByUser(int userID) throws SQLException {
		query = "SELECT * FROM auction WHERE ownerID = ? AND isClosed = 1 ORDER BY expiryDate DESC";
		List<Auction> closedAuctions = new ArrayList<>();

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, userID);
			result = statement.executeQuery();

			while (result.next()) {
				Auction opA = new Auction(result.getInt("auctionID"), result.getInt("ownerID"),
						result.getString("title"), result.getFloat("startingPrice"), result.getFloat("minIncrease"),
						result.getTimestamp("expiryDate").toLocalDateTime(), result.getFloat("actualPrice"),
						result.getInt("isClosed"));
				opA.setWinnerID(result.getInt("winnerID"));
				closedAuctions.add(opA);
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

		return closedAuctions;
	}

	/**
	 * Creates an auction, that is open by default
	 */
	public boolean createAuction(int auctionID, int ownerID, String title, float startingPrice, float minIncrease,
			LocalDateTime expiryDate) throws SQLException, ParseException {
		query = "INSERT INTO auction (auctionID, ownerID, title, startingPrice, minIncrease, expiryDate, actualPrice, isClosed) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		int result;

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, auctionID);
			statement.setInt(2, ownerID);
			statement.setString(3, title);
			statement.setFloat(4, startingPrice);
			statement.setFloat(5, minIncrease);
			statement.setObject(6, expiryDate);
			// at the beginning startingPrice = actualPrice
			statement.setFloat(7, startingPrice);
			statement.setInt(8, 0);
			result = statement.executeUpdate();

			if (result > 0) {
				return true;
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
		return false;
	}

	/**
	 * Looks for the corresponding open auction in the database
	 */
	public Auction getOpenAuctionByID(int auctionID) throws SQLException {
		Auction openAuction = null;
		query = "SELECT * FROM auction WHERE auctionID = ? AND isClosed = 0";

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, auctionID);
			result = statement.executeQuery();

			if (result.next()) {
				openAuction = new Auction(result.getInt("auctionID"), result.getInt("ownerID"),
						result.getString("title"), result.getFloat("startingPrice"), result.getFloat("minIncrease"),
						result.getTimestamp("expiryDate").toLocalDateTime(), result.getFloat("actualPrice"),
						result.getInt("isClosed"));
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

		return openAuction;
	}

	/**
	 * Looks for the corresponding closed auctions in the database
	 */
	public Auction getClosedAuctionByID(int auctionID) throws SQLException {
		Auction closedAuction = null;
		query = "SELECT * FROM auction WHERE auctionID = ? AND isClosed = 1";

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, auctionID);
			result = statement.executeQuery();

			if (result.next()) {
				closedAuction = new Auction(result.getInt("auctionID"), result.getInt("ownerID"),
						result.getString("title"), result.getFloat("startingPrice"), result.getFloat("minIncrease"),
						result.getTimestamp("expiryDate").toLocalDateTime(), result.getFloat("actualPrice"),
						result.getInt("isClosed"));
				closedAuction.setWinnerID(result.getInt("winnerID"));
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

		return closedAuction;
	}

	/**
	 * Looks for the corresponding closed auctions in the database
	 */
	public boolean checkIDAvailability(int auctionID) throws SQLException {
		query = "SELECT * FROM auction WHERE auctionID = ?";

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, auctionID);
			result = statement.executeQuery();

			if (result.next()) {
				return false;
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

		return true;
	}

	/**
	 * Looks for auctions in which the title, or the description of at least an
	 * article matches the keyword
	 */
	public List<Auction> searchByKeyword(String keyword) throws SQLException {
		List<Integer> auctionIDsList = new ArrayList<>();
		List<Auction> auctions = new ArrayList<>();
		query = "SELECT * FROM auction WHERE isClosed = 0 AND auctionID = (SELECT auctionID as aID FROM article WHERE auctionID <> 0 AND (name LIKE ? OR description LIKE ?) GROUP BY auctionID) ORDER BY expiryDate DESC";

		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, "%" + keyword + "%");
			statement.setString(2, "%" + keyword + "%");
			result = statement.executeQuery();

			while (result.next()) {
				int auctionID = result.getInt("auctionID");

				// inserting only valid IDs that are not on the list (avoiding duplicates)
				if (auctionID > 0 && !auctionIDsList.contains(auctionID)) {
					Auction a = new Auction(auctionID, result.getInt("ownerID"), result.getString("title"),
							result.getFloat("startingPrice"), result.getFloat("minIncrease"),
							result.getTimestamp("expiryDate").toLocalDateTime(), result.getFloat("actualPrice"),
							result.getInt("isClosed"));
					auctionIDsList.add(auctionID);
					auctions.add(a);
				}
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

		return auctions;
	}

	/**
	 * closes an open auction
	 */
	public void closeAuction(int auctionID, int winnerID) throws SQLException {
		// associates the winner to the auction
		query = "UPDATE auction SET winnerID = ?, isClosed = 1 WHERE auctionID = ?";
		int result2;

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, winnerID);
			statement.setInt(2, auctionID);
			result2 = statement.executeUpdate();

			// if a row was updated, it means the article has been added
			if (result2 > 0) {
				System.out.println("Winner " + winnerID + " associated to auction " + auctionID);
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

	/**
	 * Finds all the auctions won by the user
	 */
	public List<Auction> getWonAuctions(int userID) throws SQLException {
		query = "SELECT * FROM auction WHERE winnerID = ?";
		List<Auction> auctions = new ArrayList<>();

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, userID);
			result = statement.executeQuery();

			while (result.next()) {
				Auction opA = new Auction(result.getInt("auctionID"), result.getInt("ownerID"),
						result.getString("title"), result.getFloat("startingPrice"), result.getFloat("minIncrease"),
						result.getTimestamp("expiryDate").toLocalDateTime(), result.getFloat("actualPrice"),
						result.getInt("isClosed"));
				opA.setWinnerID(result.getInt("winnerID"));
				auctions.add(opA);
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

		return auctions;
	}
	
	/**
	 * updates an open auction price after an offer
	 */
	public void updatePrice(int auctionID, float price) throws SQLException {
		// associates the winner to the auction
		query = "UPDATE auction SET actualPrice = ? WHERE auctionID = ?";
		int result2;

		try {
			statement = connection.prepareStatement(query);
			statement.setFloat(1, price);
			statement.setInt(2, auctionID);
			result2 = statement.executeUpdate();

			// if a row was updated, it means the article has been added
			if (result2 > 0) {
				return;
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
