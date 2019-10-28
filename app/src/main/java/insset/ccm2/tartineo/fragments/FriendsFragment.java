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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.RelationService;
import insset.ccm2.tartineo.services.UserService;

public class FriendsFragment extends Fragment {
    private final static String FRIENDS_TAG = "FRIENDS_FRAGMENT";

    // Composants
    // ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> friendList;
    private Dialog addfriendsDialog;
    private EditText friendsUsernameText;
    // ListView friendsListView;

    // Services
    private AuthService authService;
    private UserService userService;
    private RelationService relationService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(FRIENDS_TAG, getStringRes(R.string.info_friends_initialization));

        initialize(view);

        // TODO : au chargement de la vue, remplir la liste avec les données enregistrées.
        // arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, friendList);
        // friendsListView.setAdapter(arrayAdapter);
    }

    /**
     * Ajoute un ami à la liste de l'utilisateur courant.
     * Déclanché lors d'un click sur le bouton de validation.
     */
    private void addFriend() {
        Log.i(FRIENDS_TAG, getStringRes(R.string.info_add_friend_button_fired));

        final String username = friendsUsernameText.getText().toString();

        Boolean isValidForm = checkAddFriendForm(username);

        if (!isValidForm) {
            return;
        }

        userService.searchByUsername(username).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Si la requête est bonne mais qu'aucun utilisateur n'est retourné
                    if (task.getResult().isEmpty()) {
                        Log.e(FRIENDS_TAG, getStringRes(R.string.info_user_not_found));

                        Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_user_not_found), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    // Sinon, parcours des utilisateurs trouvés
                    for (final QueryDocumentSnapshot userByUsername : task.getResult()) {
                        Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_search_successful));

                        relationService.get(authService.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                friendList = (ArrayList<String>) doc.get("friendList");

                                // Vérifie si l'utilisateur trouvé est déjà dans la liste d'ami de l'utilisateur courant
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
     * Vérifie si le formulaire d'ajout d'ami est valide.
     *
     * @param username The username in the form.
     *
     * @return a boolean, false if the form is invalid.
     */
    private Boolean checkAddFriendForm(String username) {
        // Vérification si les champs sont remplis.
        if (TextUtils.isEmpty(username)) {
            Log.e(FRIENDS_TAG, getStringRes(R.string.error_empty_field));

            Toast.makeText(getActivity().getApplicationContext(), getStringRes(R.string.error_empty_field), Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    /**
     * Enregistre un ami en base de données.
     *
     * @param userId The User ID to store.
     */
    private void storeFriend(String userId) {
        relationService.addFriend(authService.getCurrentUser().getUid(), userId).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    private void initialize(View view) {
        // Composants
        Button friendsModalButton = view.findViewById(R.id.friends_modal_button);
        friendsModalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFriendModal();
            }
        });

        // friendsListView = view.findViewById(R.id.friends_list_view);

        addfriendsDialog = new Dialog(getContext());
        addfriendsDialog.setContentView(R.layout.add_friends_dialog);

        friendsUsernameText = addfriendsDialog.findViewById(R.id.friends_username_text);
        Button friendsSubmitButton = addfriendsDialog.findViewById(R.id.friends_submit_button);
        friendsSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });

        // Services
        authService = AuthService.getInstance();
        userService = UserService.getInstance();
        relationService = RelationService.getInstance();
    }

    /**
     * Affiche le dialogue addFriends.
     */
    private void showAddFriendModal() {
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
