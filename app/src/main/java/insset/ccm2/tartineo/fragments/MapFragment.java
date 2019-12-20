package insset.ccm2.tartineo.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Map;

import insset.ccm2.tartineo.models.LocationModel;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.GoogleMapService;
import insset.ccm2.tartineo.services.NotificationService;
import insset.ccm2.tartineo.services.RelationService;
import insset.ccm2.tartineo.services.SettingsService;
import insset.ccm2.tartineo.services.UserService;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final static String MAP_TAG = "MAP_FRAGMENT";

    private static final int LOCATION_REQUEST_CODE = 101;

    // Component (Dialog)
    private Dialog contactFriendModal;
    private TextView contactFriendDialogTitle;
    private Button contactFriendDialogPhoneButton;
    private Button contactFriendDialogSmsButton;

    // Location
    private LocationModel currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // Map
    private SupportMapFragment supportMapFragment;
    private Marker currentUserMarker;

    // Services
    private GoogleMapService googleMapService;
    private AuthService authService;
    private UserService userService;
    private RelationService relationService;
    private SettingsService settingsService;
    private NotificationService notificationService;

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
    public void onDestroy() {
        super.onDestroy();

        googleMapService.reset();
        contactFriendModal.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(onMarkerClickListener);
        googleMapService.setMap(googleMap);

        setCurrentUserMarker();

        setCurrentUserRelationsMarkers();
    }

    /**
     * Récupère la position de l'utilisateur courant et affiche son marker avec son nom
     */
    private void setCurrentUserMarker() {
        LatLng latLng = googleMapService.generateLocation(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );

        String currentUserId = authService.getCurrentUser().getUid();

        userService.get(currentUserId)
            .addOnSuccessListener(documentSnapshot -> {
                // Déplace la caméra vers la position de l'utilisateur courant
                googleMapService.animateCamera(latLng, 15);

                String username = documentSnapshot.get("username").toString().concat(" " + getStringRes(R.string.self_marker_helper));

                // Ajoute le marker de l'utilisateur courant sur la carte
                currentUserMarker = googleMapService.addMarker(currentUserId, latLng, username, "red");
            }
        );
    }

    private void setCurrentUserRelationsMarkers() {
        relationService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(relationDocumentSnapshot -> {
            relationDocumentSnapshot.getReference().addSnapshotListener(((documentSnapshot, e) -> {
                Log.i(MAP_TAG, getStringRes(R.string.info_get_friend_list));
                Log.i(MAP_TAG, getStringRes(R.string.info_get_enemy_list));

                setMarkersByList(
                        (ArrayList<String>) documentSnapshot.get("friendList"),
                        "blue"
                );

                setMarkersByList(
                        (ArrayList<String>) documentSnapshot.get("enemyList"),
                        "orange"
                );
            }));
        });
    }

    /**
     * Récupère la position des relations de l'utilisateur courant et affiche les markers avec leur nom
     */
    private void setMarkersByList(ArrayList<String> relationListIds, String markerColor) {
        googleMapService.checkMarkersIdsExistence(relationListIds);

        settingsService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(settingsDocumentSnapshot -> {
            settingsDocumentSnapshot.getReference().addSnapshotListener(((settingsSnapshotListener, e) -> {
                if (e != null) {
                    Log.w(MAP_TAG, getStringRes(R.string.error_document_event_listening), e);

                    return;
                }

                long radius = (Long) settingsSnapshotListener.get("radius");

                for (int i = 0; i < relationListIds.size(); i++) {
                    int index = i;

                    String relationId = relationListIds.get(index);

                    userService.get(relationId).addOnSuccessListener(userDocumentSnapshot -> {
                        // Ecoute les évènements sur les relations de l'utilisateur
                        userDocumentSnapshot.getReference().addSnapshotListener((documentSnapshot, exception) -> {
                            if (exception != null) {
                                Log.w(MAP_TAG, getStringRes(R.string.error_document_event_listening), exception);

                                return;
                            }

                            if (documentSnapshot.getId() == authService.getCurrentUser().getUid()) {
                                Log.w(MAP_TAG, getStringRes(R.string.error_display_yourself_as_friend_marker));

                                return;
                            }

                            Map<String, Double> location = (Map<String, Double>) documentSnapshot.get("location");
                            Marker currentRelationMarker = googleMapService.getMarker(relationId);

                            if (location.isEmpty() || location.get("latitude") == null || location.get("longitude") == null) {
                                Log.e(MAP_TAG, getStringRes(R.string.error_location_not_found));

                                if (currentRelationMarker != null) {
                                    Log.i(MAP_TAG, getStringRes(R.string.info_relation_marker_removal));

                                    googleMapService.removeMarker(relationId);
                                }

                                return;
                            }

                            LatLng latLng = googleMapService.generateLocation(
                                    location.get("latitude"),
                                    location.get("longitude")
                            );

                            String username = documentSnapshot.get("username").toString();

                            if (currentRelationMarker != null) {
                                Log.i(MAP_TAG, getStringRes(R.string.info_relation_marker_removal));
                                googleMapService.removeMarker(relationId);
                            }

                            float distance = googleMapService.getDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), latLng.latitude, latLng.longitude);

                            if (distance > radius) {
                                return;
                            }

                            if (currentRelationMarker == null) {
                                String notificationTitle = getStringRes(R.string.notification_title_friend_is_close_to_you);
                                String notificationDescription = getResources().getString(R.string.notification_description_friend_is_close_to_you, username, String.format("%.1f", distance));

                                if (markerColor.equals("orange")) {
                                    notificationTitle = getStringRes(R.string.notification_title_enemy_is_close_to_you);
                                    notificationDescription = getResources().getString(R.string.notification_description_enemy_is_close_to_you, username, String.format("%.1f", distance));
                                }

                                notificationService.createNotification(
                                        NotificationService.MARKERS_CHANNEL_ID,
                                        MapFragment.this.getActivity(),
                                        R.drawable.ic_notifications_black_24dp,
                                        notificationTitle,
                                        notificationDescription
                                );
                            }

                            googleMapService.addMarker(relationId, latLng, username, markerColor);

                            Log.i(MAP_TAG, getStringRes(R.string.info_relation_marker_added));
                        });
                    });
                }
            }));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        fetchLastLocation();
    }

    /**
     * Au click sur un Marker (ami), l'utilisateur peut le contacter par téléphone ou SMS
     * si l'utilisateur sélectionné a indiqué son numéro de téléphone.
     */
    private GoogleMap.OnMarkerClickListener onMarkerClickListener = marker -> {
        String markerId = googleMapService.getMarkerIdByMarker(marker);

        if (markerId != null && !markerId.equals(authService.getCurrentUser().getUid()) && relationService.getFriendList().containsKey(markerId)) {
            userService
                .get(markerId)
                .addOnSuccessListener(documentSnapshot -> {
                    String relationUsername = (String) documentSnapshot.get("username");

                    settingsService
                            .get(markerId)
                            .addOnSuccessListener(settingsDocumentSnapshot -> {
                                String relationPhoneNumber = (String) settingsDocumentSnapshot.get("phoneNumber");

                                if (relationUsername != null && relationPhoneNumber != null) {
                                    showContactFriendModal(relationUsername);
                                    contactFriendDialogPhoneButton.setOnClickListener(v -> callFriend(relationPhoneNumber));
                                    contactFriendDialogSmsButton.setOnClickListener(v -> sendMessage(relationPhoneNumber));
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(MAP_TAG, getStringRes(R.string.error_has_occurred));
                })
            ;
        }

        return false;
    };

    /**
     * Appel un utilisateur ayant saisi son numéro de téléphone.
     * Demande les permissions si elles ne sont pas données.
     *
     * @param phoneNumber
     */
    private void callFriend(String phoneNumber) {
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 101);

                return;
            }

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Ouvre l'application de message préremplie avec le numéro indiqué.
     * Demande les permissions si elles ne sont pas données.
     *
     * @param phoneNumber Le numéro de téléphone
     */
    private void sendMessage(String phoneNumber) {
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 101);

                return;
            }

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("sms:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    /**
     * Affiche le dialogue d'ajout d'ami.
     */
    private void showContactFriendModal(String username) {
        contactFriendModal.show();
        contactFriendDialogTitle.setText(getResources().getString(R.string.contact_user, username));
    }

    /**
     * Demande de mettre à jour la localisation de l'utilisateur courant toutes les 5 secondes.
     */
    private void requestLocationUpdates() {
        LocationRequest locationRequest = googleMapService.requestLocation();

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
                        currentUserMarker.setPosition(
                            googleMapService.generateLocation(
                                location.getLatitude(),
                                location.getLongitude()
                            )
                        );

                        LocationModel locationModel = new LocationModel(location.getLatitude(), location.getLongitude());
                        storeCurrentUserLocation(locationModel);
                        currentLocation = locationModel;

                        Log.d(MAP_TAG, getStringRes(R.string.info_location_updated));

                        setCurrentUserRelationsMarkers();
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
                LocationModel locationModel = new LocationModel(location.getLatitude(), location.getLongitude());
                currentLocation = locationModel;
                supportMapFragment.getMapAsync(MapFragment.this);

                storeCurrentUserLocation(currentLocation);
            })
            .addOnFailureListener(e -> {
                Log.w(MAP_TAG, getStringRes(R.string.error_location_not_found), e);

                Toast.makeText(getActivity(), getStringRes(R.string.error_location_not_found),Toast.LENGTH_SHORT).show();
            })
        ;
    }

    /**
     * Enregistre la localisation de l'utilisateur courant en base de données.
     *
     * @param location A given location.
     */
    private void storeCurrentUserLocation(LocationModel location) {
        userService.updateLocation(authService.getCurrentUser().getUid(), location)
            .addOnSuccessListener(aVoid -> {
                Log.i(MAP_TAG, getStringRes(R.string.info_location_storage));
            })
            .addOnFailureListener(e -> {
                Log.w(MAP_TAG, getStringRes(R.string.error_location_storage), e);
            })
        ;
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

        // Composants (dialog)
        contactFriendModal = new Dialog(getContext());
        contactFriendModal.setContentView(R.layout.contact_relation_dialog);

        contactFriendDialogTitle = contactFriendModal.findViewById(R.id.contact_friend_dialog_title);
        contactFriendDialogPhoneButton = contactFriendModal.findViewById(R.id.contact_friend_dialog_phone);
        contactFriendDialogSmsButton = contactFriendModal.findViewById(R.id.contact_friend_dialog_sms);

        // Services
        googleMapService = GoogleMapService.getInstance();
        authService = AuthService.getInstance();
        userService = UserService.getInstance();
        relationService = RelationService.getInstance();
        settingsService = SettingsService.getInstance();
        notificationService = NotificationService.getInstance();
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
