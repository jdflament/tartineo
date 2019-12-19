package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import insset.ccm2.tartineo.models.SettingsModel;

public class SettingsService {
    public final static Integer USER_DEFAULT_RADIUS = 15;

    public final static Integer USER_MIN_RADIUS = 1;
    public final static Integer USER_MAX_RADIUS = 30;

    private static final String collectionPath = "settings";

    private static final SettingsService instance = new SettingsService();

    private FirestoreService firestoreService;

    private SettingsService() { firestoreService = FirestoreService.getInstance(); }

    public static SettingsService getInstance() {
        return instance;
    }

    /**
     * Créé un document "settings" pour l'utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param settings Les paramètres de l'utilisateur
     *
     * @return Task
     */
    public Task<Void> set(String userId, SettingsModel settings) {
        return firestoreService.set(collectionPath, userId, settings);
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
     * Modifie les paramètres d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param settings Les paramètres
     *
     * @return Task
     */
    public Task<Void> update(String userId, SettingsModel settings) {
        Map<String,Object> settingsMap = new HashMap<>();

        settingsMap.put("radius", settings.getRadius());

        return firestoreService.update(collectionPath, userId, settingsMap);
    }
}
