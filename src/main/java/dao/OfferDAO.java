package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import beans.Offer;

public class OfferDAO {
	private final Connection connection;
	String query = "";
	PreparedStatement statement = null;
	ResultSet result = null;

	public OfferDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Offer> getOffersByAuctionID(int auctionID) throws SQLException {
		query = "SELECT * from offer WHERE auctionID = ? ORDER BY offerDate DESC, price DESC";
		List<Offer> offersList = new ArrayList<>();

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, auctionID);
			result = statement.executeQuery();

			while (result.next()) {
				Offer o = new Offer(result.getInt("userID"), auctionID,
						result.getTimestamp("offerDate").toLocalDateTime(), result.getFloat("price"));
				offersList.add(o);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch (SQLException e1) {
				throw new SQLException(e1);
			}

			try {
				statement.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}

		return offersList;
	}

	public boolean createOffer(int userID, int auctionID, LocalDateTime date, float price)
			throws SQLException, ParseException {
		query = "INSERT INTO offer (userID, auctionID, offerDate, price) VALUES(?, ?, ?, ?)";
		int result;

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, userID);
			statement.setInt(2, auctionID);
			statement.setObject(3, date);
			statement.setFloat(4, price);
			result = statement.executeUpdate();

			// if a row was updated, the offer has been added
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

	public Offer getMaxOffer(int auctionID) throws SQLException {
		query = "SELECT * FROM offer WHERE price = (SELECT MAX(price) FROM offer WHERE auctionID = ?)";
		Offer offer = null;

		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, auctionID);
			result = statement.executeQuery();

			if (result.next()) {
				offer = new Offer(result.getInt("userID"), result.getInt("auctionID"),
						result.getTimestamp("offerDate").toLocalDateTime(), result.getFloat("price"));
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

		return offer;
	}
}
