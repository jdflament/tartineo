package insset.ccm2.tartineo.services;

import android.location.Location;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import insset.ccm2.tartineo.models.LocationModel;

public class GoogleMapService {

    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;

    private GoogleMap map;

    private static final GoogleMapService instance = new GoogleMapService();

    private AuthService authService;
    private UserService userService;

    private Map<String, Marker> markers = new HashMap<>();

    private GoogleMapService() {
        authService = AuthService.getInstance();
        userService = UserService.getInstance();
    }

    public static GoogleMapService getInstance() {
        return instance;
    }

    /**
     * Réinitialise la carte.
     */
    public void reset() {
        for (Map.Entry<String, Marker> markerEntry : markers.entrySet()) {
            Marker marker = markerEntry.getValue();

            marker.remove();
        }

        markers.clear();
        map.resetMinMaxZoomPreference();
    }

    /**
     * Initialise la carte.
     *
     * @param googleMap Google map
     */
    public void setMap(GoogleMap googleMap) {
        map = googleMap;
    }

    /**
     * Récupère l'instance de la carte précedement initialisée.
     *
     * @return GoogleMap
     */
    public GoogleMap getMap() {
        return map;
    }

    /**
     * Déplace la caméra vers la position indiquée avec le zoom.
     *
     * @param latLng Position
     * @param zoom Zoom
     */
    public void animateCamera(LatLng latLng, float zoom) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(zoom).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Ajoute un Marker sur la carte.
     *
     * @param id Identifiant du Marker
     * @param latLng Position du Marker
     *
     * @return Marker
     */
    public Marker addMarker(String id, LatLng latLng) {
        Marker marker = map.addMarker(setMarkerOptions(latLng));

        markers.put(id, marker);

        return marker;
    }

    /**
     * Ajoute un Marker sur la carte avec un titre.
     *
     * @param id Identifiant du Marker
     * @param latLng Position du Marker
     * @param title Titre sur le Marker
     *
     * @return Marker
     */
    public Marker addMarker(String id, LatLng latLng, String title) {
        Marker marker = map.addMarker(setMarkerOptions(latLng, title));

        markers.put(id, marker);

        return marker;
    }

    /**
     * Ajoute un Marker sur la carte avec un titre.
     *
     * @param id Identifiant du Marker
     * @param latLng Position du Marker
     * @param title Titre sur le Marker
     * @param color Couleur du Marker
     *
     * @return Marker
     */
    public Marker addMarker(String id, LatLng latLng, String title, String color) {
        Marker marker = map.addMarker(setMarkerOptions(latLng, title, color));

        markers.put(id, marker);

        return marker;
    }

    /**
     * Récupère la liste des Markers sur la carte.
     *
     * @return La liste des Markers.
     */
    public Map<String, Marker> getMarkers() {
        return markers;
    }

    /**
     * Récupère un Marker existant.
     *
     * @param id L'identifiant du Marker.
     *
     * @return Marker
     */
    public Marker getMarker(String id) {
        return markers.get(id);
    }

    /**
     * Génère une Localisation (LatLng chez Google)
     *
     * @param lat Latitude
     * @param lng Longitude
     *
     * @return LatLng
     */
    public LatLng generateLocation(Double lat, Double lng) {
        return new LatLng(lat, lng);
    }

    /**
     *
     * @return LocationRequest
     */
    public LocationRequest requestLocation() {
        return new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)
        ;
    }

    /**
     * Vérifie si les identifiants Markers sont présent dans la liste indiquée.
     *
     * S'ils ne le sont pas, une suppression est effectuée.
     *
     * @param ids La liste des identifiants.
     */
    public void checkMarkersIdsExistence(ArrayList<String> ids) {
        for (Map.Entry<String, Marker> markerEntry : markers.entrySet()) {
            String key = markerEntry.getKey();
            Marker marker = markerEntry.getValue();

            if (!ids.contains(key) && key != authService.getCurrentUser().getUid()) {
                marker.remove();
            }
        }
    }

    /**
     * Supprime un marker de la carte.
     *
     * @param id Identifiant du Marker
     */
    public void removeMarker(String id) {
        Marker marker = getMarker(id);

        if (marker != null) {
            marker.remove();
            markers.remove(id);
        }
    }

    /**
     * Récupère la distance entre deux points en km.
     *
     * @param sourceLatitude Latitude initiale
     * @param sourceLongitude Longitude initiale
     * @param targetLatitude Latitude cible
     * @param targetLongitude Longitude cible
     *
     * @return La distance en km.
     */
    public float getDistance(double sourceLatitude, double sourceLongitude, double targetLatitude, double targetLongitude) {
        float[] distances = new float[1];

        Location.distanceBetween(
            sourceLatitude,
            sourceLongitude,
            targetLatitude,
            targetLongitude,
            distances
        );

        return distances[0] == 0 ? distances[0] : distances[0]/1000;
    }

    /**
     * Ajoute un Marker sur la carte depuis l'identifiant d'un utilisateur.
     *
     * @param id Identifiant de l'utilisateur.
     */
    public void addMarkerFromUserId(String id, String color) {
        userService
                .get(id)
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.get("username").toString();
                    Map<String, Double> location = (Map<String, Double>) documentSnapshot.get("location");

                    addMarker(id, generateLocation(
                            location.get("latitude"),
                            location.get("longitude")
                    ), username, color);
                })
        ;
    }

    /**
     * Créer l'objet MarkerOptions, permettant de paramétrer un Marker
     *
     * @param latLng Position du marqueur
     *
     * @return MarkerOptions
     */
    private MarkerOptions setMarkerOptions(LatLng latLng) {
        return new MarkerOptions()
            .position(latLng)
            .flat(true)
        ;
    }

    /**
     * Créer l'objet MarkerOptions, permettant de paramétrer un Marker
     *
     * @param latLng Position du marqueur
     * @param title Titre du marqueur
     *
     * @return MarkerOptions
     */
    private MarkerOptions setMarkerOptions(LatLng latLng, String title) {
        return new MarkerOptions()
            .position(latLng)
            .title(title)
            .flat(true)
        ;
    }

    /**
     * Créer l'objet MarkerOptions, permettant de paramétrer un Marker
     *
     * @param latLng Position du marqueur
     * @param title Titre du marqueur
     *
     * @return MarkerOptions
     */
    private MarkerOptions setMarkerOptions(LatLng latLng, String title, String color) {
        float markerColor;

        switch (color) {
            case "orange":
                markerColor = BitmapDescriptorFactory.HUE_ORANGE;
                break;
            case "blue":
                markerColor = BitmapDescriptorFactory.HUE_AZURE;
                break;
            case "red":
            default:
                markerColor = BitmapDescriptorFactory.HUE_RED;
                break;
        }

        return new MarkerOptions()
            .position(latLng)
            .title(title)
            .flat(true)
            .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
        ;
    }
}
