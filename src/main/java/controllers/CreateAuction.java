package controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.Article;
import beans.User;
import dao.ArticleDAO;
import dao.AuctionDAO;
import utilities.ConnectionHandler;

@WebServlet("/CreateAuction")
@MultipartConfig
public class CreateAuction extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreateAuction() {
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

	private int generateAuctionID() throws SQLException {
		Random rand = new Random();
		int code = rand.nextInt(1, 9999);
		AuctionDAO auc = new AuctionDAO(connection);

		// checks if the generated code is available for a new auction or if it is too
		// big
		if (!auc.checkIDAvailability(code) || code > 9999) {
			// generates a new code because the previous one was already in use
			code = generateAuctionID();
		}

		// the code is available, so it can be returned
		return code;
	}

	private void createAuction(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		User user = (User) request.getSession(false).getAttribute("user");
		String[] articlesIDs = request.getParameterValues("articleIDs");

		int auctionID;

		try {
			auctionID = generateAuctionID();
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore: accesso al database fallito!");
			return;
		}

		Article a;
		float price = 0;
		LocalDateTime expiryDate = LocalDateTime.parse(request.getParameter("expiryDate"))
				.truncatedTo(ChronoUnit.MINUTES);
		String title = request.getParameter("title");

		float minIncrease;

		try {
			minIncrease = Float.parseFloat(request.getParameter("minIncrease"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: inserire un numero!");
			return;
		}

		// checks if the connection is active
		if (checkConnection(connection)) {
			if (title == null || title.isBlank() || title.length() < 3 || title.length() > 20) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Errore: il titolo non rispetta i vincoli richiesti!");
				return;
			}

			if (minIncrease < 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Errore: l'incremento minimo deve essere un numero positivo!");
				return;
			}

			if (!expiryDate.isAfter(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Errore: devi inserire una scadenza posteriore alla data attuale!");
				return;
			}

			ArticleDAO art = new ArticleDAO(connection);

			for (String s : articlesIDs) {
				int articleID;

				try {
					articleID = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: inserire un numero!");
					return;
				}

				try {
					// retrieves each selected article and checks if its owner is the current user
					// and if it is associated to another auction
					a = art.getArticleByID(articleID);

					if (a.getOwnerID() != user.getUserID()) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN,
								"Errore: non puoi inserire un'articolo che non ti appartiene!");
						return;
					} else if (a.getAuctionID() != 0) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN,
								"Errore: non puoi inserire un'articolo presente in un'altra asta!");
						return;
					}
				} catch (SQLException | IOException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Errore: accesso al database fallito!");
					return;
				}

				price += a.getPrice();

				// associates the article to the auction
				try {
					art.associateToAuction(articleID, auctionID);
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Errore: accesso al database fallito!");
					return;
				}
			}

			AuctionDAO auc = new AuctionDAO(connection);

			try {
				auc.createAuction(auctionID, user.getUserID(), title, price, minIncrease, expiryDate);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			// redirects to sell.html
			String path = getServletContext().getContextPath() + "/GoToSellPage";
			response.sendRedirect(path);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// checks if the session does not exist or is expired
		if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
		} else {
			createAuction(request, response);
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