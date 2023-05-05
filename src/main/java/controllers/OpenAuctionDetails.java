package controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import beans.Article;
import beans.Auction;
import beans.Offer;
import dao.ArticleDAO;
import dao.AuctionDAO;
import dao.OfferDAO;
import utilities.ConnectionHandler;

@WebServlet("/OpenAuctionDetails")
public class OpenAuctionDetails extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public OpenAuctionDetails() {
		super();
	}

	/**
	 * Initializes the configuration of the servlet, of the thymeleaf engine and
	 * connects to the database
	 */
	public void init() throws ServletException {
		ServletContext context = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);

		templateResolver.setTemplateMode(TemplateMode.HTML);

		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);

		templateResolver.setCharacterEncoding("ISO-8859-1");
		templateResolver.setSuffix(".html");

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
		int auctionID = Integer.parseInt(request.getParameter("auctionID"));

		Auction auction;
		List<Offer> offers;
		List<Article> articlesList;
		boolean isExpired;

		// used to print the date on the screen in the easiest way
		String formatDate;
		// contains all the offers with their creationTimes formatted as strings
		LinkedHashMap<Offer, String> formatOffers = null;

		// checks if the connection is active
		if (checkConnection(connection)) {
			AuctionDAO auc = new AuctionDAO(connection);

			try {
				// retrieves the auction
				auction = auc.getOpenAuctionByID(auctionID);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			if (auction != null) {
				OfferDAO off = new OfferDAO(connection);

				try {
					// retrieves the offers related to the auction
					offers = off.getOffersByAuctionID(auctionID);

					// reformat the offerDate to a string if there is at least one offer
					if (offers != null) {
						formatOffers = new LinkedHashMap<>();

						for (Offer o : offers) {
							String formatOfferDate = o.getDate()
									.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
							formatOffers.put(o, formatOfferDate);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Errore: accesso al database fallito!");
					return;
				}

				ArticleDAO art = new ArticleDAO(connection);

				try {
					// retrieves the articles in the auction
					articlesList = art.getArticlesByAuctionID(auctionID);
				} catch (SQLException e) {
					e.printStackTrace();
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Errore: accesso al database fallito!");
					return;
				}

				// checks if the auction is expired
				LocalDateTime curr = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
				isExpired = curr.isAfter(auction.getExpiryDate());

				// reformat the expiryDate and makes it more readable
				formatDate = auction.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Errore: nessuna asta trovata!");
				return;
			}

			// redirects to openAuctionDetails.html and add missions to the parameters
			String path = "/openAuctionDetails.html";

			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

			// creates and sets the variables to use inside the template page
			ctx.setVariable("openAuction", auction);
			ctx.setVariable("expiryDate", formatDate);
			ctx.setVariable("articlesList", articlesList);
			ctx.setVariable("offersList", formatOffers);
			ctx.setVariable("isExpired", isExpired);

			templateEngine.process(path, ctx, response.getWriter());
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// checks if the session does not exist or is expired
		if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
		} else if (Integer.parseInt(request.getParameter("auctionID")) > 0) {
			setupPage(request, response);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doPost(request, response);
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
