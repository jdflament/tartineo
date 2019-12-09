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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.RelationService;
import insset.ccm2.tartineo.services.UserService;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final static String MAP_TAG = "MAP_FRAGMENT";

    private static final int LOCATION_REQUEST_CODE = 101;

    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;

    // Location
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // Map
    private GoogleMap map;
    private SupportMapFragment supportMapFragment;
    private Marker currentUserMarker;
    private Map<String, Marker> relationsMarkers = new HashMap<>();

    // Services
    private AuthService authService;
    private UserService userService;
    private RelationService relationService;

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
        map = googleMap;
        setCurrentUserMarker();
        setCurrentUserRelationsMarkers();
    }

    /**
     * Récupère la position de l'utilisateur courant et affiche son marker avec son nom
     */
    private void setCurrentUserMarker() {
        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

        userService.get(authService.getCurrentUser().getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    // Déplace la caméra vers la position de l'utilisateur courant
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                    // Ajoute le marker de l'utilisateur courant sur la carte
                    currentUserMarker = map.addMarker(getMarkerOptions(
                            documentSnapshot.get("username").toString().concat(" " + getStringRes(R.string.self_marker_helper)),
                            latLng
                    ));
                });
    }

    /**
     * Récupère la position des relations de l'utilisateur courant et affiche les markers avec leur nom
     */
    private void setCurrentUserRelationsMarkers() {
        relationService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(relationDocumentSnapshot -> {
            Log.i(MAP_TAG, getStringRes(R.string.info_get_friend_list));

            ArrayList<String> friendListIds = (ArrayList<String>) relationDocumentSnapshot.get("friendList");

            for (int i = 0; i < friendListIds.size(); i++) {
                int index = i;

                String friendId = friendListIds.get(index);

                // TODO : Add Friend/Enemy marker style
                userService.get(friendId)
                        .addOnSuccessListener(userDocumentSnapshot -> {

                            // Ecoute les évènements sur les relations de l'utilisateur
                            userDocumentSnapshot.getReference().addSnapshotListener((documentSnapshot, e) -> {
                                if (e != null) {
                                    Log.w(MAP_TAG, getStringRes(R.string.error_document_event_listening), e);
                                    return;
                                }

                                Map<String, Double> location = (Map<String, Double>) documentSnapshot.get("location");
                                Marker currentFriendMarker = relationsMarkers.get(friendId);

                                if (location.isEmpty() || location.get("latitude") != null || location.get("longitude") != null) {
                                    if (currentFriendMarker != null) {
                                        currentFriendMarker.remove();
                                    }

                                    return;
                                }

                                LatLng friendLatLng = new LatLng(location.get("latitude"), location.get("longitude"));

                                if (currentFriendMarker != null) {
                                    currentFriendMarker.setPosition(friendLatLng);
                                    return;
                                }

                                Marker friendMarker = map.addMarker(getMarkerOptions(
                                        documentSnapshot.get("username").toString(),
                                        friendLatLng)
                                );

                                relationsMarkers.put(friendListIds.get(index), friendMarker);
                            });
                        });
            }
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
     * Créer l'objet MarkerOptions, permettant de paramétrer un Marker
     *
     * @param title Titre du marqueur
     * @param latLng Position du marqueur
     *
     * @return MarkerOptions
     */
    private MarkerOptions getMarkerOptions(String title, LatLng latLng) {
        return new MarkerOptions().position(latLng).title(title);
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
        relationService = RelationService.getInstance();
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
