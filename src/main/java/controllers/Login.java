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
import javax.servlet.http.HttpSession;

import beans.User;
import dao.UserDAO;
import utilities.ConnectionHandler;

@WebServlet("/Login")
public class Login extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public Login() {
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
	 * Checks the requirements about username and password
	 *
	 * @param username must have length between 5 and 15 characters and must contain
	 *                 ONLY alphanumeric characters
	 * @param password must have length between 5 and 15 characters, must contain
	 *                 ONLY alphanumeric characters, at least 1 number, 1 uppercase
	 *                 letter and 1 lowercase letter
	 */
	private boolean checkCredentialsReq(String username, String password) {
		if (username.matches("[a-zA-Z0-9]+") && username.length() > 4 && username.length() < 16) {
			return password.matches(".*[a-zA-Z0-9].*") && password.length() > 4 && password.length() < 16;
		} else {
			return false;
		}
	}

	/**
	 * Manages the login process
	 */
	private void manageLogin(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		User user = null;

		if (checkConnection(connection)) {
			// checks if the requirements on credentials are met
			if (checkCredentialsReq(username, password)) {
				UserDAO log = new UserDAO(connection);
				try {
					// checks the credentials inserted with the ones on the db only if the username
					// is in the db
					if (log.getUserByUsername(username)) {
						user = log.checkCredentials(username, password);
					}
				} catch (SQLException e) {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Errore: accesso al database fallito!");
					return;
				}

				if (user != null) {
					HttpSession session = request.getSession(true);

					if (session.isNew()) {
						// saves the current userID and the time of the login in the session
						session.setAttribute("user", user);
						session.setAttribute("loginTime", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

						// the session expires after 45 minutes of inactivity
						session.setMaxInactiveInterval(2700);

						response.sendRedirect(getServletContext().getContextPath() + "/home.html");
					}
				} else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore: username o password errati!");
				}
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Errore: username e password devono contenere tra 5 e 15 caratteri alfanumerici. "
								+ "La password deve contenere almeno una lettera maiuscola, una minuscola ed un numero!");
			}
		} else {
			// if the connection is null or closed, it is initialized
			init();
			manageLogin(request, response);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// checks if the session does not exist or is expired
		if (request.getSession(false) == null || request.getSession(false).getAttribute("user") == null) {
			// checks if the user inserted both username and password
			if (request.getParameter("username") == null || request.getParameter("password") == null) {
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
						"Devi inserire sia un username che una password!");
			} else {
				manageLogin(request, response);
			}
		} else {
			response.sendRedirect(getServletContext().getContextPath() + "/index.html");
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
