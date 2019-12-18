package insset.ccm2.tartineo.models;

import android.location.Location;

public class LocationModel {
    private double latitude;

    private double longitude;

    public LocationModel() { }

    /**
     * @param latitude
     * @param longitude
     */
    public LocationModel(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
