package insset.ccm2.tartineo.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import insset.ccm2.tartineo.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.UserService;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final static String MAP_TAG = "MAP_FRAGMENT";

    private static final int LOCATION_REQUEST_CODE = 101;

    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;

    // Location
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // Map
    private SupportMapFragment supportMapFragment;
    private Marker currentUserMarker;

    // Services
    private AuthService authService;
    private UserService userService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkLocationPermission();

        initialize();

        fetchLastLocation();

        requestLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

        //MarkerOptions are used to create a new Marker.You can specify location, title etc with MarkerOptions
        userService.get(authService.getCurrentUser().getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    String currentUserMarkerTitle = documentSnapshot.get("username").toString().concat(" " + getStringRes(R.string.self_marker_helper));
                    MarkerOptions marker = new MarkerOptions().position(latLng).title(currentUserMarkerTitle);

                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                    //Adding the created the marker on the map
                    currentUserMarker = googleMap.addMarker(marker);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        fetchLastLocation();
    }

    /**
     * Demande de mettre à jour la localisation de l'utilisateur courant toutes les 5 secondes.
     */
    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        ;

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e(MAP_TAG, getStringRes(R.string.error_location_not_found));

                    return;
                }
                
                for (Location location : locationResult.getLocations()) {
                    // Met à jour le marker et la base de données uniquement si la nouvelle localisation est différente de l'ancienne.
                    if (currentLocation.getLongitude() != location.getLongitude() || currentLocation.getLatitude() != location.getLatitude()) {
                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                        currentUserMarker.setPosition(latLng);
                        storeLocation(location);
                        currentLocation = location;

                        Log.d(MAP_TAG, getStringRes(R.string.info_location_updated));
                    }
                }
            };
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    /**
     * Récupère la dernière localisation de l'utilisateur.
     */
    private void fetchLastLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    currentLocation = location;
                    supportMapFragment.getMapAsync(MapFragment.this);

                    storeLocation(currentLocation);
                })
                .addOnFailureListener(e -> {
                    Log.w(MAP_TAG, getStringRes(R.string.error_location_not_found), e);

                    Toast.makeText(getActivity(), getStringRes(R.string.error_location_not_found),Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Enregistre la localisation de l'utilisateur courant en base de données.
     *
     * @param location A given location.
     */
    private void storeLocation(Location location) {
        userService.updateLocation(authService.getCurrentUser().getUid(), location)
                .addOnSuccessListener(aVoid -> {
                    Log.i(MAP_TAG, getStringRes(R.string.info_location_storage));
                })
                .addOnFailureListener(e -> {
                    Log.w(MAP_TAG, getStringRes(R.string.error_location_storage), e);
                });
    }

    /**
     * Demande l'accès à la localisation de l'utilisateur s'il ne l'a pas encore accordé.
     */
    private void checkLocationPermission() {
        if (!hasLocationPermission()) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        }
    }

    /**
     * Vérifie si l'application a les permissions de localisation nécessaires.
     *
     * @return Boolean
     */
    private Boolean hasLocationPermission() {
        return (ActivityCompat.checkSelfPermission(
                getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        );
    }

    /**
     * Initialise les composants de la vue.
     */
    private void initialize() {
        Log.i(MAP_TAG, getStringRes(R.string.info_map_initialization));

        // Composants
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        supportMapFragment = (SupportMapFragment) MapFragment.this.getChildFragmentManager().findFragmentById(R.id.map);

        // Services
        authService = AuthService.getInstance();
        userService = UserService.getInstance();
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
