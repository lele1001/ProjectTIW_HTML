package controllers;

import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
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

import beans.Auction;
import beans.User;
import dao.AuctionDAO;
import utilities.ConnectionHandler;

@WebServlet("/GoToBuyPage")
public class GoToBuyPage extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public GoToBuyPage() {
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

	private boolean validateKey(String key) {
		// checks if the key contains only letters and its length is between 3 and 20
		// characters
		return key.matches("[a-zA-Z]+") && key.length() > 2 && key.length() < 21;
	}

	private String getRemainingTime(LocalDateTime from, LocalDateTime to) {
		long diffDays = ChronoUnit.DAYS.between(from, to);
		long diffHours = ChronoUnit.HOURS.between(from, to);
		long hoursBetween = diffHours - (diffDays * 24);

		return diffDays + " days and " + hoursBetween + " hours";
	}

	private void setupPage(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		User user = (User) request.getSession(false).getAttribute("user");
		LocalDateTime loginTime = (LocalDateTime) request.getSession(false).getAttribute("loginTime");
		String key = request.getParameter("key");

		List<Auction> wonAuctions;
		List<Auction> keyAuctionsList = null;
		// contains all the auctions with their remaining times formatted as strings
		LinkedHashMap<Auction, String> keyAuctions = null;

		// checks if the connection is active
		if (checkConnection(connection)) {
			AuctionDAO auc = new AuctionDAO(connection);

			if (key != null) {
				if (validateKey(key)) {
					try {
						// retrieves all the auctions with articles that contain the keyword
						keyAuctionsList = auc.searchByKeyword(key);

						// creates and sets the variables to use inside the template page
						if (keyAuctionsList != null && !keyAuctionsList.isEmpty()) {
							keyAuctions = new LinkedHashMap<>();

							for (Auction a : keyAuctionsList) {
								String remainingTime = getRemainingTime(loginTime, a.getExpiryDate());

								keyAuctions.put(a, remainingTime);
							}
						} else {
							keyAuctions = null;
						}
					} catch (SQLException e) {
						e.printStackTrace();
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"Errore: accesso al database fallito!");
						return;
					}
				} else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Errore: la chiave deve essere lunga tra 3 e 20 caratteri e può contenere solo lettere non accentate!");
					return;
				}
			}

			try {
				// retrieves all the auctions won by the user
				wonAuctions = auc.getWonAuctions(user.getUserID());
			} catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Errore: accesso al database fallito!");
				return;
			}

			// redirects to buy.html and add missions to the parameters
			String path = "/buy.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

			// creates and sets the variables to use inside the template page
			ctx.setVariable("wonAuctions", wonAuctions);
			ctx.setVariable("auctionsByKeyword", keyAuctions);
			if (keyAuctions == null || keyAuctions.isEmpty()) {
				ctx.setVariable("noAuctionsMsg", "Non ci sono aste aperte per la parola " + key);
			}

			templateEngine.process(path, ctx, response.getWriter());
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// checks if the session does not exist or is expired
		if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
		} else {
			try {
				setupPage(request, response);
			} catch (IOException | ServletException e) {
				e.printStackTrace();
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
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