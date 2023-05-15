package controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
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
import beans.User;
import dao.ArticleDAO;
import dao.AuctionDAO;
import dao.OfferDAO;
import dao.UserDAO;
import utilities.ConnectionHandler;

@WebServlet("/ClosedAuctionDetails")
public class ClosedAuctionDetails extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public ClosedAuctionDetails() {
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

	private void setupPage(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		int auctionID;
		
		try {
			auctionID = Integer.parseInt(request.getParameter("auctionID"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: inserire un intero!");
			return;
		}
		
		if (auctionID < 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: inserire un intero positivo!");
			return;
		}

		User winner = null;
		Auction auction;
		List<Article> articlesList;
		Offer maxOffer;

		// checks if the connection is active
		if (checkConnection(connection)) {
			AuctionDAO auc = new AuctionDAO(connection);

			try {
				// retrieves the auction
				auction = auc.getClosedAuctionByID(auctionID);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			if (auction != null) {
				OfferDAO off = new OfferDAO(connection);

				try {
					// retrieves the maximum offer for the auction
					maxOffer = off.getMaxOffer(auctionID);
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Errore: accesso al database fallito!");
					return;
				}

				UserDAO us = new UserDAO(connection);

				if (maxOffer != null) {
					try {
						// retrieves the user that won the auction
						winner = us.getUserByID(maxOffer.getUserID());
					} catch (SQLException e) {
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"Errore: accesso al database fallito!");
						return;
					}
				}

				ArticleDAO art = new ArticleDAO(connection);

				try {
					// retrieves the articles in the auction
					articlesList = art.getArticlesByAuctionID(auctionID);
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Errore: accesso al database fallito!");
					return;
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Errore: nessuna asta trovata!");
				return;
			}

			// redirects to closedAuctionDetails.html and add missions to the parameters
			String path = "/closedAuctionDetails.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

			// creates and sets the variables to use inside the template page
			ctx.setVariable("closedAuction", auction);
			ctx.setVariable("articlesList", articlesList);
			ctx.setVariable("winner", winner);

			templateEngine.process(path, ctx, response.getWriter());
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
