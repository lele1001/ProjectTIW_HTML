package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
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
import beans.User;
import dao.*;
import utilities.ConnectionHandler;

@WebServlet("/GoToSellPage")
public class GoToSellPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public GoToSellPage() {
		super();
	}

	/**
	 * initializes the configuration of the servlet, of the thymeleaf engine and
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
	 * checks if the connection is active
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
			throws ServletException, IOException {
		List<Auction> openAuctionsList = null;
		List<Auction> closedAuctions = null;
		List<Article> userArticles = null;
		// contains all the auctions with their expiryDates formatted as strings
		LinkedHashMap<Auction, String> openAuctions = null;

		User user = (User) request.getSession(false).getAttribute("user");
		int userID = user.getUserID();

		// checks if the connection is active
		if (checkConnection(connection)) {
			AuctionDAO auc = new AuctionDAO(connection);

			try {
				// retrieves all user's open auctions ordered by deadline ascending
				openAuctionsList = auc.getOpenAuctionsByUser(userID);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			try {
				// retrieves all user's closed auctions ordered by deadline ascending
				closedAuctions = auc.getClosedAuctionsByUser(userID);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}
			
			ArticleDAO art = new ArticleDAO(connection);

			try {
				// retrieves all user's articles that are not in an auction
				userArticles = art.getArticlesByUserIDNotInAuctions(userID);
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			// redirects to sell.html and add missions to the parameters
			String path = "/sell.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

			// creates and sets the variables to use inside the template page
			if (openAuctionsList != null && !openAuctionsList.isEmpty()) {
				openAuctions = new LinkedHashMap<>();

				for (Auction a : openAuctionsList) {
					String formatOfferDate = a.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
					openAuctions.put(a, formatOfferDate);
				}
			}

			ctx.setVariable("userOpenAuctions", openAuctions);
			ctx.setVariable("userOpenAuctionsList", openAuctionsList);
			ctx.setVariable("userClosedAuctions", closedAuctions);
			ctx.setVariable("userArticles", userArticles);
			
			templateEngine.process(path, ctx, response.getWriter());
		} 
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// checks if the session does not exists or is expired
		if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
		} else {
			setupPage(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//doGet(request, response);
	}

	/**
	 * called when the servlet is destroyed
	 */
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
