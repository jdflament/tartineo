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

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

import insset.ccm2.tartineo.adapters.RelationListAdapter;
import insset.ccm2.tartineo.models.UserModel;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.GoogleMapService;
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
    private GoogleMapService googleMapService;

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        addfriendsDialog.dismiss();
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
                    ArrayList<String> enemyListIds = (ArrayList<String>) documentSnapshot.get("enemyList");

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

                    // Vérifie si l'utilisateur trouvé est dans la liste d'ennemi de l'utilisateur courant
                    if (enemyListIds.indexOf(userByUsername.getId()) != -1) {
                        Log.e(FRIENDS_TAG, getStringRes(R.string.error_user_remove_relations));

                        relationService.removeUnfriendRelation(authService.getCurrentUser().getUid(), userByUsername.getId());
                    }

                    createFriendship(authService.getCurrentUser().getUid(), userByUsername.getId());
                    googleMapService.addMarkerFromUserId(userByUsername.getId(), "blue");
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
        relationService.createFriendRelation(sourceUserId, targetUserId).addOnSuccessListener(aVoid -> {
            Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_storage));

            friendsUsernameText.setText("");
            addfriendsDialog.hide();

            // Ajoute un utilisateur et met à jour la listView
            userService.get(targetUserId).addOnSuccessListener(documentSnapshot -> {
               relationService.addInFriendList(targetUserId, documentSnapshot.toObject(UserModel.class));
               relationService.removeFromEnemyList(targetUserId);
               updateFriendListView(relationService.getFriendList());
            });

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
        relationService.removeFriendRelation(sourceUserId, targetUserId).addOnSuccessListener(aVoid -> {
            Log.i(FRIENDS_TAG, getStringRes(R.string.info_friend_removal));

            relationService.removeFromFriendList(targetUserId);
            googleMapService.removeMarker(targetUserId);
            updateFriendListView(relationService.getFriendList());

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_friend_removal), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Log.w(FRIENDS_TAG, getStringRes(R.string.error_friend_removal), exception);

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_friend_removal), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Récupère la liste d'amis.
     */
    private void getFriendList() {
        relationService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(relationDocumentSnapshot -> {
            relationDocumentSnapshot.getReference().addSnapshotListener((documentSnapshot, e) -> {

                ArrayList<String> friendListIds = (ArrayList<String>) documentSnapshot.get("friendList");

                relationService.clearFriendList();

                if (friendListIds.isEmpty()) {
                    updateFriendListView(relationService.getFriendList());
                }

                for (int i = 0; i < friendListIds.size(); i++) {
                    int index = i;

                    userService.get(friendListIds.get(index)).addOnSuccessListener(userDocumentSnapshot -> {
                        relationService.addInFriendList(friendListIds.get(index), userDocumentSnapshot.toObject(UserModel.class));
                        updateFriendListView(relationService.getFriendList());
                    });
                }
            });
        });
    }

    /**
     * Met à jour la listView.
     *
     * @param friendList
     */
    public void updateFriendListView(Map<String, UserModel> friendList) {
        RelationListAdapter adapter = new RelationListAdapter(friendList, FriendsFragment.this);
        friendsListView.setAdapter(adapter);
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
        googleMapService = GoogleMapService.getInstance();
    }

    /**
     * Affiche le dialogue d'ajout d'ami.
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
