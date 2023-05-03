package beans;

public class User {
    private final int userID;
    private final String username;
    private final String password;
    private final String address;

    public User(int userID, String username, String password, String address) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.address = address;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

}
