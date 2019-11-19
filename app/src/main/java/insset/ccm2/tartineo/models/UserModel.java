package insset.ccm2.tartineo.models;

public class UserModel {
    private String username;

    public UserModel() { }
    /**
     * @param username The username of the UserModel. This field must be unique.
     */
    public UserModel(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
