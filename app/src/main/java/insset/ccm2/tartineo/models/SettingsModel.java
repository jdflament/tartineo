package insset.ccm2.tartineo.models;

public class SettingsModel {
    private int radius;

    private String phoneNumber;

    public SettingsModel() { }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
