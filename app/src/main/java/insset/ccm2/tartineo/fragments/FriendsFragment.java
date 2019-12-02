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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import insset.ccm2.tartineo.R;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import insset.ccm2.tartineo.adapters.RelationListAdapter;
import insset.ccm2.tartineo.models.UserModel;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.RelationService;
import insset.ccm2.tartineo.services.UserService;

public class FriendsFragment extends Fragment {
    private final static String FRIENDS_TAG = "FRIENDS_FRAGMENT";

    // Composants
    private ArrayList<String> friendListIds;
    private Dialog addfriendsDialog;
    private EditText friendsUsernameText;
    private ListView friendsListView;

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

        getFriendList();
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

        userService.searchByUsername(username).addOnSuccessListener(queryDocumentSnapshots -> {
            // Si la requête est bonne mais qu'aucun utilisateur n'est retourné
            if (queryDocumentSnapshots.isEmpty()) {
                Log.e(FRIENDS_TAG, getStringRes(R.string.info_user_not_found));

                Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_user_not_found), Toast.LENGTH_SHORT).show();

                return;
            }

            // Sinon, parcours des utilisateurs trouvés
            for (final QueryDocumentSnapshot userByUsername : queryDocumentSnapshots) {
                Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_search_successful));

                relationService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
                    friendListIds = (ArrayList<String>) documentSnapshot.get("friendList");

                    // Vérifie si l'utilisateur trouvé n'est pas l'utilisateur courant
                    if (userByUsername.getId().equals(authService.getCurrentUser().getUid())) {
                        Log.e(FRIENDS_TAG, getStringRes(R.string.error_add_yourself_as_friend));

                        Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_add_yourself_as_friend), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    // Vérifie si l'utilisateur trouvé est déjà dans la liste d'ami de l'utilisateur courant
                    if (friendListIds.indexOf(userByUsername.getId()) != -1) {
                        Log.e(FRIENDS_TAG, getStringRes(R.string.error_users_already_friend));

                        Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_users_already_friend), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    createFriendship(authService.getCurrentUser().getUid(), userByUsername.getId());
                });
            }
        }).addOnFailureListener(exception -> {
            Log.w(FRIENDS_TAG, getStringRes(R.string.error_friend_search_failed), exception);

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_friend_search_failed), Toast.LENGTH_SHORT).show();
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
     * Créer une relation entre deux utilisateurs.
     *
     * @param sourceUserId The source user ID.
     * @param targetUserId The target user ID.
     */
    private void createFriendship(String sourceUserId, String targetUserId) {
        // Ajoute l'utilisateur cible dans la liste d'ami de l'utilisateur source
        final Task<Void> firstFriendshipTask = relationService.addFriend(sourceUserId, targetUserId);

        // Ajoute l'utilisateur source dans la liste d'ami de l'utilisateur cible
        final Task<Void> secondFriendshipTask = relationService.addFriend(targetUserId, sourceUserId);

        firstFriendshipTask.continueWithTask(task -> secondFriendshipTask).addOnSuccessListener(aVoid -> {
            Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_storage));

            getFriendList();
            friendsUsernameText.setText("");
            addfriendsDialog.hide();

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_friend_storage), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Log.w(FRIENDS_TAG, getStringRes(R.string.error_friend_storage), exception);

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_friend_storage), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Supprime une relation entre deux utilisateurs.
     *
     * @param sourceUserId The source user ID.
     * @param targetUserId The target user ID.
     */
    public void removeFriendship(String sourceUserId, String targetUserId) {
        // Supprime l'utilisateur cible dans la liste d'ami de l'utilisateur source
        final Task<Void> firstFriendshipTask = relationService.removeFriend(sourceUserId, targetUserId);

        // Supprime l'utilisateur source dans la liste d'ami de l'utilisateur cible
        final Task<Void> secondFriendshipTask = relationService.removeFriend(targetUserId, sourceUserId);

        firstFriendshipTask.continueWithTask(task -> secondFriendshipTask).addOnSuccessListener(aVoid -> {
            Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_removal));

            getFriendList();

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_friend_removal), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Log.w(FRIENDS_TAG, getStringRes(R.string.error_friend_removal), exception);

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_friend_removal), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Récupère la liste d'ami au chargement de la vue.
     */
    private void getFriendList() {
        relationService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
            Log.i(FRIENDS_TAG, getStringRes(R.string.info_get_friend_list));

            friendListIds = (ArrayList<String>) documentSnapshot.get("friendList");

            HashMap<String, UserModel> friendList = new HashMap<>();

            if (friendListIds.size() == 0) {
                RelationListAdapter adapter = new RelationListAdapter(friendList, FriendsFragment.this);
                friendsListView.setAdapter(adapter);
            }

            for (int i = 0; i < friendListIds.size(); i++) {
                int index = i;

                userService.get(friendListIds.get(index)).addOnSuccessListener(userDocumentSnapshot -> {
                    friendList.put(friendListIds.get(index), userDocumentSnapshot.toObject(UserModel.class));

                    if (friendList.size() == friendListIds.size()) {
                        RelationListAdapter adapter = new RelationListAdapter(friendList, FriendsFragment.this);
                        friendsListView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    /**
     * Initialise les composants des différentes vues.
     */
    private void initialize(View view) {
        // Composants
        ImageButton friendsModalButton = view.findViewById(R.id.friends_modal_button);
        friendsModalButton.setOnClickListener(v -> showAddFriendModal());

        friendsListView = view.findViewById(R.id.friends_list_view);

        addfriendsDialog = new Dialog(getContext());
        addfriendsDialog.setContentView(R.layout.add_friends_dialog);

        friendsUsernameText = addfriendsDialog.findViewById(R.id.friends_username_text);
        Button friendsSubmitButton = addfriendsDialog.findViewById(R.id.friends_submit_button);
        friendsSubmitButton.setOnClickListener(v -> addFriend());

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
