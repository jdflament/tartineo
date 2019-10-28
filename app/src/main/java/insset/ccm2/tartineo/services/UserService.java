package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import insset.ccm2.tartineo.models.User;

public class UserService {

    private static final String collectionPath = "users";

    private static final UserService instance = new UserService();

    private FirestoreService firestoreService;

    private UserService() {
        firestoreService = FirestoreService.getInstance();
    }

    public static UserService getInstance() {
        return instance;
    }

    /**
     * Set a new User in database by document id.
     *
     * @param documentId The document ID.
     * @param user The User object to add.
     *
     * @return Task
     */
    public Task<Void> set(String documentId, User user) {
        return firestoreService.set(collectionPath, documentId, user);
    }

    /**
     * Get a User by documentId.
     *
     * @param documentId The document ID.
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<DocumentSnapshot> get(String documentId) {
        return firestoreService.getDocument(collectionPath, documentId);
    }


    /**
     * Search a user by username value.
     *
     * @param username The value to search on.
     *
     * @return Task with QuerySnapshot response.
     */
    public Task<QuerySnapshot> searchByUsername(String username) {
        return firestoreService.whereEqualTo(collectionPath, "username", username);
    }
}