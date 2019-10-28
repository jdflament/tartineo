package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class FirestoreService {

    private static final FirestoreService instance = new FirestoreService();

    private FirebaseFirestore database;

    private FirestoreService() {
        database = FirebaseFirestore.getInstance();
    }

    public static FirestoreService getInstance() {
        return instance;
    }

    /**
     * @param collectionPath The collection path.
     *
     * @return Task with QuerySnapshot response.
     */
    public Task<QuerySnapshot> getCollection(String collectionPath) {
        return database
                .collection(collectionPath)
                .get()
        ;
    }

    /**
     * @param collectionPath The collection path.
     * @param documentId The document ID. (key)
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<DocumentSnapshot> getDocument(String collectionPath, String documentId) {
        return database
                .collection(collectionPath)
                .document(documentId)
                .get()
        ;
    }

    /**
     * @param collectionPath The collection path.
     * @param documentId The document ID. (key)
     * @param data The data to set.
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<Void> set(String collectionPath, String documentId, Object data) {
        return database
                .collection(collectionPath)
                .document(documentId)
                .set(data)
        ;
    }

    /**
     * @param collectionPath The collection path.
     * @param documentId The document ID. (key)
     * @param data The data to set.
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<Void> update(String collectionPath, String documentId, Map<String, Object> data) {
        return database
                .collection(collectionPath)
                .document(documentId)
                .update(data)
        ;
    }

    /**
     * @param collectionPath The collection path.
     * @param documentId The document ID. (key)
     * @param key The key to update.
     * @param value The given value to set.
     *
     * @return Task with DocumentSnapshot response.
     */
    public Task<Void> updateByKeyAndValue(String collectionPath, String documentId, String key, Object value) {
        return database
                .collection(collectionPath)
                .document(documentId)
                .update(key, value)
        ;
    }

    /**
     * @param collectionPath The collection path.
     * @param field The key to search on.
     * @param value The value to search on.
     *
     * @return Task with QuerySnapshot response.
     */
    public Task<QuerySnapshot> whereEqualTo(String collectionPath, String field, String value) {
        return database
                .collection(collectionPath)
                .whereEqualTo(field, value)
                .get()
        ;
    }
}