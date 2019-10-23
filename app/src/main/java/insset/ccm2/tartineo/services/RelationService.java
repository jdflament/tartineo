package insset.ccm2.tartineo.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

import insset.ccm2.tartineo.R;
import insset.ccm2.tartineo.models.RelationModel;

public class RelationService {

    private static final RelationService instance = new RelationService();

    private static final String collectionPath = "relations";

    public static RelationService getInstance() {
        return instance;
    }

    /**
     * Set a new Relation in database by document ID.
     *
     * @param documentId The document ID.
     * @param relation The relation object.
     *
     * @return Task
     */
    public Task<Void> set(String documentId, RelationModel relation) {
        return FirestoreService.getInstance().set(collectionPath, documentId, relation);
    }

    /**
     * Get a Relation by documentId.
     *
     * @param documentId The document ID.
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<DocumentSnapshot> get(String documentId) {
        return FirestoreService.getInstance().getDocument(collectionPath, documentId);
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
        RelationModel relation = new RelationModel();
        relation.getFriendList().add(userId);

        return FirestoreService.getInstance().updateByKeyAndValue(
                collectionPath,
                documentId,
                "friendList",
                relation.getFriendList()
        );
    }
}
