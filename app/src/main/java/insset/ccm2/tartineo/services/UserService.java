package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import insset.ccm2.tartineo.models.UserModel;

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
     * Set a new UserModel in database by document id.
     *
     * @param documentId The document ID.
     * @param userModel The UserModel object to add.
     *
     * @return Task
     */
    public Task<Void> set(String documentId, UserModel userModel) {
        return firestoreService.set(collectionPath, documentId, userModel);
    }

    /**
     * Get a UserModel by documentId.
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