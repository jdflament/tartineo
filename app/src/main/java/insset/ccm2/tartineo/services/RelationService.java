package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import insset.ccm2.tartineo.models.RelationModel;

public class RelationService {

    private static final String collectionPath = "relations";

    private static final RelationService instance = new RelationService();

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
    public Task<Void> addFriend(String documentId, String userId) {
        final Map<String, Object> addUserToFriendList= new HashMap<>();
        addUserToFriendList.put("friendList", FieldValue.arrayUnion(userId));

        return firestoreService.update(
                collectionPath,
                documentId,
                addUserToFriendList
        );
    }
}
