package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import insset.ccm2.tartineo.models.RelationModel;

public class RelationService {

    private static final RelationService instance = new RelationService();

    private static final String collectionPath = "relations";

    public static RelationService getInstance() {
        return instance;
    }

    /**
     * Set a new Relation in database by User ID.
     *
     * @param userId The User ID.
     * @param relation The relation object.
     *
     * @return Task
     */
    public Task<Void> set(String userId, RelationModel relation) {
        return FirestoreService.getInstance().set(collectionPath, userId, relation);
    }

    /**
     * Get User relations.
     *
     * @param userId The User ID.
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<DocumentSnapshot> get(String userId) {
        return FirestoreService.getInstance().getDocument(collectionPath, userId);
    }

    /**
     * Update the friend list of the given document ID.
     *
     * @param documentId The User ID.
     * @param userId The userId to add in friend list.
     *
     * @return Task
     */
    public Task<Void> addFriend(String documentId, String userId) {
        final Map<String, Object> addUserToFriendList= new HashMap<>();
        addUserToFriendList.put("friendList", FieldValue.arrayUnion(userId));

        return FirestoreService.getInstance().update(
                collectionPath,
                documentId,
                addUserToFriendList
        );
    }
}
