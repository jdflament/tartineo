package insset.ccm2.tartineo.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import insset.ccm2.tartineo.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final static String MAP_TAG = "MAP_FRAGMENT";

    private static final int LOCATION_REQUEST_CODE = 101;

    private Location currentLocation;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private SupportMapFragment supportMapFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(MAP_TAG, getStringRes(R.string.info_map_initialization));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        supportMapFragment = (SupportMapFragment) MapFragment.this.getChildFragmentManager().findFragmentById(R.id.map);

        checkLocationPermission();

        fetchLastLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

        //MarkerOptions are used to create a new Marker.You can specify location, title etc with MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are Here");

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        //Adding the created the marker on the map
        googleMap.addMarker(markerOptions);
    }

    /**
     * Récupère la dernière localisation de l'utilisateur.
     */
    private void fetchLastLocation(){
        final Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    Log.i(MAP_TAG, getStringRes(R.string.info_current_location) + " " + currentLocation.getLatitude() + "/" + currentLocation.getLongitude());

                    supportMapFragment.getMapAsync(MapFragment.this);
                } else{
                    Log.w(MAP_TAG, getStringRes(R.string.error_location_not_found), task.getException());

                    Toast.makeText(getActivity(), getStringRes(R.string.error_location_not_found),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Demande l'accès à la position de l'utilisateur s'il ne l'a pas encore accordé.
     */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
            return;
        }
    }

    /**
     * @param id Id of the string resource.
     *
     * @return String resource.
     */
    private String getStringRes(int id) {
        return getResources().getString(id);
    }
}
