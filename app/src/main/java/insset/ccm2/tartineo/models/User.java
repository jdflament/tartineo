package insset.ccm2.tartineo.models;

public class User {
    public String username;

    /**
     * @param username The username of the User. This field must be unique.
     */
    public User(String username) {
        this.username = username;
    }
}
