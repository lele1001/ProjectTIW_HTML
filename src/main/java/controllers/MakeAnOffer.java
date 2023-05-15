package controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

@WebServlet("/MakeAnOffer")
public class MakeAnOffer extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public MakeAnOffer() {
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

	private void setupPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User user = (User) request.getSession(false).getAttribute("user");
		int auctionID;
		
		try {
			auctionID = Integer.parseInt(request.getParameter("auctionID"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: inserire un numero!");
			return;
		}
		
		if (auctionID < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: inserire un intero positivo!");
			return;
		}
		
		float newPrice;
		
		try {
			newPrice = Float.parseFloat(request.getParameter("price"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: inserire un numero!");
			return;
		}

		LocalDateTime offerDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		Auction auction;
		Offer maxOffer;

		// checks if the connection is active
		if (checkConnection(connection)) {
			AuctionDAO auc = new AuctionDAO(connection);

			try {
				// retrieves the auction, checks if it is owned by the user if it is expired
				// and if the new price fulfills the requirements
				auction = auc.getOpenAuctionByID(auctionID);

				if (auction == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Errore: nessuna asta trovata!");
					return;
				} else if (auction.getOwnerID() == user.getUserID()) {
					response.sendError(HttpServletResponse.SC_CONFLICT,
							"Errore: non puoi fare un'offerta per una tua asta!");
					return;
				} else if (offerDate.isAfter(auction.getExpiryDate())) {
					response.sendError(HttpServletResponse.SC_CONFLICT,
							"Errore: non puoi fare un'offerta per un'asta scaduta!");
					return;
				} else if (newPrice < auction.getMinIncrease() + auction.getActualPrice()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: prezzo inserito troppo basso!");
					return;
				}
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			OfferDAO off = new OfferDAO(connection);

			try {
				// retrieves the maximum offer and checks if it belongs to the current user
				maxOffer = off.getMaxOffer(auctionID);

				if (maxOffer != null && maxOffer.getUserID() == user.getUserID()) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Errore: prima di poter fare una nuova offerta attendi che qualcun altro ne faccia una!");
					return;
				}
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			try {
				// creates the new offer
				off.createOffer(user.getUserID(), auctionID, offerDate, newPrice);

				// updates the price
				auc.updatePrice(auctionID, newPrice);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			// redirects to offer.html with the offer list updated
			String path = getServletContext().getContextPath() + "/GoToOfferPage?auctionID=" + auctionID;
			response.sendRedirect(path);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// checks if the session does not exist or is expired
		if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
		} else {
			setupPage(request, response);
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