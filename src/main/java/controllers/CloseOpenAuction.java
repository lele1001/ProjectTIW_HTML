package controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.Auction;
import beans.Offer;
import beans.User;
import dao.AuctionDAO;
import dao.OfferDAO;
import utilities.ConnectionHandler;

@WebServlet("/CloseOpenAuction")
public class CloseOpenAuction extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CloseOpenAuction() {
		super();
	}

	/**
	 * Initializes the configuration of the servlet and connects to the database
	 */
	public void init() throws ServletException {
		ServletContext context = getServletContext();
		connection = ConnectionHandler.getConnection(context);
	}

	/**
	 * Checks if the connection is active
	 */
	private boolean checkConnection(Connection connection) {
		try {
			if (connection != null && !connection.isClosed())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Before closing the auction, the method checks if the current user is the
	 * owner
	 */
	private void closeAuction(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		User user = (User) request.getSession(false).getAttribute("user");
		int auctionID = Integer.parseInt(request.getParameter("auctionID"));

		Auction auction;
		Offer maxOffer;
		int winnerID = 0;
		boolean isExpired;

		// checks if the connection is active
		if (checkConnection(connection)) {
			AuctionDAO auc = new AuctionDAO(connection);

			try {
				// retrieves the auction, checks if it is owned by the user, and if the actual
				// time is after the deadline
				auction = auc.getOpenAuctionByID(auctionID);
				isExpired = LocalDateTime.now().isAfter(auction.getExpiryDate());

				if (auction.getOwnerID() != user.getUserID()) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN,
							"Errore: non puoi chiudere un'asta che non ti appartiene!");
					return;
				} else if (!isExpired) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN,
							"Errore: non puoi chiudere un'asta che non è scaduta!");
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			OfferDAO offerDAO = new OfferDAO(connection);

			try {
				// retrieves the max offer, if exists
				maxOffer = offerDAO.getMaxOffer(auctionID);

				if (maxOffer != null) {
					winnerID = maxOffer.getUserID();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			try {
				auc.closeAuction(auctionID, winnerID);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			// redirects to offer.html with the offer list updated
			String path = getServletContext().getContextPath() + "/GoToSellPage";
			response.sendRedirect(path);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// checks if the session does not exist or is expired
		if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
		} else if (Integer.parseInt(request.getParameter("auctionID")) > 0) {
			closeAuction(request, response);
		}
	}

	/**
	 * Called when the servlet is destroyed
	 */
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}