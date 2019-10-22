package insset.ccm2.tartineo;

import android.location.Location;

import androidx.lifecycle.LiveData;

import com.google.android.gms.location.FusedLocationProviderClient;

import insset.ccm2.tartineo.models.LocationModel;

public class LocationLiveData extends LiveData {

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onActive() {
        super.onActive();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
    }

    /**
     *
     *
     * @param location
     *
     * @return LocationModel
     */
    private LocationModel setLocationData(Location location) {
        return new LocationModel(
                location.getLatitude(),
                location.getLongitude()
        );
    }
}
