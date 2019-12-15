package insset.ccm2.tartineo.services;

import java.util.Map;

public class MapService {
    private GoogleMapService googleMapService;
    private UserService userService;

    private static final MapService instance = new MapService();

    private MapService() {
        googleMapService = GoogleMapService.getInstance();
        userService = UserService.getInstance();
    }

    public static MapService getInstance() {
        return instance;
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

                googleMapService.addMarker(id, googleMapService.generateLocation(
                        location.get("latitude"),
                        location.get("longitude")
                ), username, color);
            })
        ;
    }

    /**
     * Supprime un Marker de la carte.
     *
     * @param id L'identifiant du Marker.
     */
    public void removeMarker(String id) {
        googleMapService.getMarker(id).remove();
    }
}
