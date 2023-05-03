package dao;

import beans.User;

import java.sql.*;

public class UserDAO {
    private Connection connection;
    String query = "";
    PreparedStatement statement = null;
    ResultSet result = null;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Checks the user credentials
     */
    public User checkCredentials(String username, String password) throws SQLException {
        User user = null;
        query = "SELECT * FROM persons WHERE username = ? and password = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            result = statement.executeQuery();

            if (result.next()) {
                user = new User(result.getInt("userID"), result.getString("username"), result.getString("password"),
                        result.getString("address"));
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

        return user;
    }

    /**
     * Looks for the corresponding userID in the database
     */
    public User getUserByID(int userID) throws SQLException {
        User user = null;
        query = "SELECT * FROM persons WHERE userID = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, userID);
            result = statement.executeQuery();

            if (result.next()) {
                user = new User(result.getInt("userID"), result.getString("username"), result.getString("password"),
                        result.getString("address"));
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

        return user;
    }

    /**
     * Looks for the corresponding username in the database
     */
    public boolean getUserByUsername(String username) throws SQLException {
        query = "SELECT * FROM persons WHERE username = ?";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            result = statement.executeQuery();

            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                    throw new SQLException(e2);
                }
            }
            try {
                statement.close();
            } catch (SQLException e3) {
                e3.printStackTrace();
                throw new SQLException(e3);
            }
        }

        return false;
    }
}
