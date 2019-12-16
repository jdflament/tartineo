package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import insset.ccm2.tartineo.models.RelationModel;
import insset.ccm2.tartineo.models.UserModel;

public class RelationService {

    private static final String collectionPath = "relations";

    private static final RelationService instance = new RelationService();

    // Composants
    private HashMap<String, UserModel> friendList = new HashMap<>();
    private HashMap<String, UserModel> enemyList = new HashMap<>();

    // Services
    private FirestoreService firestoreService;

    private RelationService() {
        firestoreService = FirestoreService.getInstance();
    }

    public static RelationService getInstance() {
        return instance;
    }

    /**
     * Set a new Relation in database by UserModel ID.
     *
     * @param userId The UserModel ID.
     * @param relation The relation object.
     *
     * @return Task
     */
    public Task<Void> set(String userId, RelationModel relation) {
        return firestoreService.set(collectionPath, userId, relation);
    }

    /**
     * Get UserModel relations.
     *
     * @param userId The UserModel ID.
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<DocumentSnapshot> get(String userId) {
        return firestoreService.getDocument(collectionPath, userId);
    }

    /**
     * Update the friend list of the given document ID.
     *
     * @param documentId The UserModel ID.
     * @param userId The userId to add in friend list.
     *
     * @return Task
     */
    public Task<Void> storeFriend(String documentId, String userId) {
        final Map<String, Object> addUserToFriendList= new HashMap<>();
        addUserToFriendList.put("friendList", FieldValue.arrayUnion(userId));

        return firestoreService.update(
                collectionPath,
                documentId,
                addUserToFriendList
        );
    }

    /**
     * Remove the friend list of the given document ID.
     *
     * @param documentId The UserModel ID.
     * @param userId The userId to add in friend list.
     *
     * @return Task
     */
    public Task<Void> removeFriend(String documentId, String userId) {
        final Map<String, Object> removeUserToFriendList= new HashMap<>();
        removeUserToFriendList.put("friendList", FieldValue.arrayRemove(userId));

        return firestoreService.update(
                collectionPath,
                documentId,
                removeUserToFriendList
        );
    }

    /**
     * Ajoute un Utilisateur dans la liste d'ami.
     *
     * Ne pas oublier de mettre à jour la listView (adapter) au changement.
     *
     * @param key  Position in the list.
     * @param user The user ID.
     */
    public void addInFriendList(String key, UserModel user) {
        friendList.put(key, user);
    }

    /**
     * Supprime un Utilisateur de la liste d'ami.
     *
     * Ne pas oublier de mettre à jour la listView (adapter) au changement.
     *
     * @param targetUserId The target user ID.
     */
    public void removeFromFriendList(String targetUserId) {
        friendList.remove(targetUserId);
    }

    /**
     * Retourne la liste d'ami.
     *
     * @return FriendList
     */
    public Map<String, UserModel> getFriendList() {
        return friendList;
    }

    /**
     * Créer une relation entre deux utilisateurs.
     *
     * @param sourceUserId The source user ID.
     * @param targetUserId The target user ID.
     *
     * @return Tasks
     */
    public Task<Void> createFriendRelation(String sourceUserId, String targetUserId) {
        // Ajoute l'utilisateur cible dans la liste d'ami de l'utilisateur source
        final Task<Void> firstFriendshipTask = storeFriend(sourceUserId, targetUserId);

        // Ajoute l'utilisateur source dans la liste d'ami de l'utilisateur cible
        final Task<Void> secondFriendshipTask = storeFriend(targetUserId, sourceUserId);

        return firstFriendshipTask.continueWithTask(task -> secondFriendshipTask);
    }

    /**
     * Supprime une relation entre deux utilisateurs.
     *
     * @param sourceUserId The source user ID.
     * @param targetUserId The target user ID.
     */
    public Task<Void> removeFriendRelation(String sourceUserId, String targetUserId) {
        // Supprime l'utilisateur cible dans la liste d'ami de l'utilisateur source
        final Task<Void> firstFriendshipTask = removeFriend(sourceUserId, targetUserId);

        // Supprime l'utilisateur source dans la liste d'ami de l'utilisateur cible
        final Task<Void> secondFriendshipTask = removeFriend(targetUserId, sourceUserId);

        return firstFriendshipTask.continueWithTask(task -> secondFriendshipTask);
    }

    /**
     * Update the enemy list of the given document ID.
     *
     * @param documentId The UserModel ID.
     * @param userId The userId to add in friend list.
     *
     * @return Task
     */
    public Task<Void> storeEnemy(String documentId, String userId) {
        final Map<String, Object> addUserToEnemyList= new HashMap<>();
        addUserToEnemyList.put("enemyList", FieldValue.arrayUnion(userId));

        return firestoreService.update(
                collectionPath,
                documentId,
                addUserToEnemyList
        );
    }

    /**
     * Remove the enemy list of the given document ID.
     *
     * @param documentId The UserModel ID.
     * @param userId The userId to add in friend list.
     *
     * @return Task
     */
    public Task<Void> removeEnemy(String documentId, String userId) {
        final Map<String, Object> removeUserToEnemyList= new HashMap<>();
        removeUserToEnemyList.put("enemyList", FieldValue.arrayRemove(userId));

        return firestoreService.update(
                collectionPath,
                documentId,
                removeUserToEnemyList
        );
    }

    /**
     * Ajoute un Utilisateur dans la liste d'ennemi.
     *
     * Ne pas oublier de mettre à jour la listView (adapter) au changement.
     *
     * @param key  Position in the list.
     * @param user The user ID.
     */
    public void addInEnemyList(String key, UserModel user) {
        enemyList.put(key, user);
    }

    /**
     * Supprime un Utilisateur de la liste d'ennemi.
     *
     * Ne pas oublier de mettre à jour la listView (adapter) au changement.
     *
     * @param targetUserId The target user ID.
     */
    public void removeFromEnemyList(String targetUserId) {
        enemyList.remove(targetUserId);
    }

    /**
     * Retourne la liste d'ennemi.
     *
     * @return EnemyList
     */
    public Map<String, UserModel> getEnemyList() {
        return enemyList;
    }

    /**
     * Créer une relation entre deux utilisateurs.
     *
     * @param sourceUserId The source user ID.
     * @param targetUserId The target user ID.
     *
     * @return Tasks
     */
    public Task<Void> createUnFriendRelation(String sourceUserId, String targetUserId) {
        // Ajoute l'utilisateur cible dans la liste d'ami de l'utilisateur source
        final Task<Void> firstUnFriendshipTask = storeEnemy(sourceUserId, targetUserId);

        // Ajoute l'utilisateur source dans la liste d'ami de l'utilisateur cible
        final Task<Void> secondUnFriendshipTask = storeEnemy(targetUserId, sourceUserId);

        return firstUnFriendshipTask.continueWithTask(task -> secondUnFriendshipTask);
    }

    /**
     * Supprime une relation entre deux utilisateurs.
     *
     * @param sourceUserId The source user ID.
     * @param targetUserId The target user ID.
     */
    public Task<Void> removeUnFriendRelation(String sourceUserId, String targetUserId) {
        // Supprime l'utilisateur cible dans la liste d'ami de l'utilisateur source
        final Task<Void> firstUnFriendshipTask = removeEnemy(sourceUserId, targetUserId);

        // Supprime l'utilisateur source dans la liste d'ami de l'utilisateur cible
        final Task<Void> secondUnFriendshipTask = removeEnemy(targetUserId, sourceUserId);

        return firstUnFriendshipTask.continueWithTask(task -> secondUnFriendshipTask);
    }
}
