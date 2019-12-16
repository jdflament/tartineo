package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class SettingsService {
    private static final String collectionPath = "settings";

    private static final SettingsService instance = new SettingsService();

    private FirestoreService firestoreService;

    private SettingsService() { firestoreService = FirestoreService.getInstance(); }

    public static SettingsService getInstance() {
        return instance;
    }

    /**
     * Récupère les paramètres d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     *
     * @return Task
     */
    public Task<DocumentSnapshot> get(String userId) {
        return firestoreService.getDocument(collectionPath, userId);
    }

    /**
     * Modifie le radius d'un utilisateur
     *
     * @param userId L'identifiant de l'utilisateur
     * @param radius Le radius
     *
     * @return Task
     */
    public Task<Void> updateRadius(String userId, int radius) {
        Map<String,Object> radiusMap = new HashMap<>();
        radiusMap.put("radius", radius);

        return firestoreService.update(collectionPath, userId, radiusMap);
    }
}
