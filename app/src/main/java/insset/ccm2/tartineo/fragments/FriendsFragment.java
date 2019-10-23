package insset.ccm2.tartineo.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import insset.ccm2.tartineo.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import insset.ccm2.tartineo.models.RelationModel;
import insset.ccm2.tartineo.services.RelationService;

public class FriendsFragment extends Fragment {
    private final static String FRIENDS_TAG = "FRIENDS_FRAGMENT";

    Dialog addfriendsDialog;

    EditText usernameText;

    Button friendsModalButton, friendsSubmitButton;

    FirebaseAuth firebaseAuth;

    FirebaseFirestore database;

    RelationModel userRelations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(FRIENDS_TAG, getStringRes(R.string.info_friends_initialization));

        initializeUI(view);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }

    /**
     * Ajoute un ami à l'utilisateur courant.
     * Déclanché lors d'un click sur le bouton de validation.
     *
     * @param view
     */
    private void addFriend(View view) {
        Log.i(FRIENDS_TAG, getStringRes(R.string.info_add_friend_button_fired));

        final String username = usernameText.getText().toString();

        // Vérification si les champs sont remplis.
        if (TextUtils.isEmpty(username)) {
            Log.e(FRIENDS_TAG, getStringRes(R.string.error_empty_field));

            Toast.makeText(getActivity().getApplicationContext(), getStringRes(R.string.error_empty_field), Toast.LENGTH_SHORT).show();

            return;
        }

        database
                .collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.i(FRIENDS_TAG, document.getId());

                                getUserRelations();
                                storeFriend(document.getId());
                            }
                        } else {
                            Log.w(FRIENDS_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Enregistre un ami en base de données.
     *
     * @param userId
     */
    private void  storeFriend(String userId) {
        RelationService.getInstance().addFriend(
                firebaseAuth.getCurrentUser().getUid(),
                userId
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_list_storage));
                } else {
                    Log.w(FRIENDS_TAG, getStringRes(R.string.error_friend_list_storage), task.getException());
                }
            }
        });
    }

    /**
     * Récupère les relations de l'utilisateur courant.
     */
    private void getUserRelations() {
        database
                .collection("relations")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();

                            userRelations = new RelationModel();
                            userRelations.setFriendList((ArrayList<String>) doc.get("friendList"));
                            userRelations.setEnemyList((ArrayList<String>) doc.get("enemyList"));
                        } else {

                        }
                    }
                });
    }

    /**
     * Initialise les composants des différentes vues.
     */
    private void initializeUI(View view) {
        // Boutons
        friendsModalButton = view.findViewById(R.id.friends_modal_button);

        friendsModalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFriendModal();
            }
        });

        // Dialogue
        addfriendsDialog = new Dialog(getContext());
        addfriendsDialog.setContentView(R.layout.add_friends_dialog);

        // Champs Dialogue
        usernameText = addfriendsDialog.findViewById(R.id.friends_username_text);

        // Boutons Dialogue
        friendsSubmitButton = addfriendsDialog.findViewById(R.id.friends_submit_button);

        friendsSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(v);
            }
        });
    }

    /**
     * Affiche le dialogue addFriends.
     */
    public void showAddFriendModal() {
        addfriendsDialog.show();
    }

    /**
     * @param id Id of the string resource.
     *
     * @return String resource.
     */
    private String getStringRes(int id) {
        return getResources().getString(id);
    }
}
