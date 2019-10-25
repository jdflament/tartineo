package insset.ccm2.tartineo.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import insset.ccm2.tartineo.services.FirestoreService;
import insset.ccm2.tartineo.services.RelationService;

public class FriendsFragment extends Fragment {
    private final static String FRIENDS_TAG = "FRIENDS_FRAGMENT";

    // ArrayAdapter<String> arrayAdapter;

    ArrayList<String> friendList;

    Button friendsModalButton, friendsSubmitButton;

    Dialog addfriendsDialog;

    EditText friendsUsernameText;

    FirebaseAuth firebaseAuth;

    FirebaseFirestore database;

    // ListView friendsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(FRIENDS_TAG, getStringRes(R.string.info_friends_initialization));

        initializeUI(view);

        // TODO : au chargement de la vue, remplir la liste avec les données enregistrées.
        // arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, friendList);
        // friendsListView.setAdapter(arrayAdapter);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }

    /**
     * Ajoute un ami à la liste de l'utilisateur courant.
     * Déclanché lors d'un click sur le bouton de validation.
     */
    private void addFriend() {
        Log.i(FRIENDS_TAG, getStringRes(R.string.info_add_friend_button_fired));

        final String username = friendsUsernameText.getText().toString();

        // Vérification si les champs sont remplis.
        if (TextUtils.isEmpty(username)) {
            Log.e(FRIENDS_TAG, getStringRes(R.string.error_empty_field));

            Toast.makeText(getActivity().getApplicationContext(), getStringRes(R.string.error_empty_field), Toast.LENGTH_SHORT).show();

            return;
        }

        FirestoreService.getInstance().whereEqualTo(
                "users",
                "username",
                username
        ).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().isEmpty()) {
                        Log.e(FRIENDS_TAG, getStringRes(R.string.info_user_not_found));

                        Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_user_not_found), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    for (final QueryDocumentSnapshot userByUsername : task.getResult()) {
                        Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_search_successful));

                        RelationService.getInstance().get(
                                firebaseAuth.getCurrentUser().getUid()
                        ).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    friendList = (ArrayList<String>) doc.get("friendList");

                                    if (friendList.indexOf(userByUsername.getId()) != -1) {
                                        Log.e(FRIENDS_TAG, getStringRes(R.string.error_users_already_friend));

                                        Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_users_already_friend), Toast.LENGTH_SHORT).show();

                                        return;
                                    }

                                    storeFriend(userByUsername.getId());
                                }
                            }
                        });
                    }
                } else {
                    Log.w(FRIENDS_TAG, getStringRes(R.string.error_friend_search_failed), task.getException());

                    Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_friend_search_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Enregistre un ami en base de données.
     *
     * @param userId The User ID to store.
     */
    private void storeFriend(String userId) {
        RelationService.getInstance().addFriend(
                firebaseAuth.getCurrentUser().getUid(),
                userId
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_storage));

                    // TODO : rafraîchir la liste d'ami.

                    Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_friend_storage), Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(FRIENDS_TAG, getStringRes(R.string.error_friend_storage), task.getException());

                    Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_friend_storage), Toast.LENGTH_SHORT).show();
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

        // Listes
        // friendsListView = view.findViewById(R.id.friends_list_view);

        // Dialogue
        addfriendsDialog = new Dialog(getContext());
        addfriendsDialog.setContentView(R.layout.add_friends_dialog);

        // Champs Dialogue
        friendsUsernameText = addfriendsDialog.findViewById(R.id.friends_username_text);

        // Boutons Dialogue
        friendsSubmitButton = addfriendsDialog.findViewById(R.id.friends_submit_button);

        friendsSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
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
