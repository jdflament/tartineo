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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import insset.ccm2.tartineo.R;
import insset.ccm2.tartineo.adapters.RelationListAdapter;
import insset.ccm2.tartineo.models.UserModel;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.RelationService;
import insset.ccm2.tartineo.services.UserService;

public class EnemiesFragment extends Fragment {
    private final static String ENEMIES_TAG = "ENEMIES_FRAGMENT";

    // Composants
    private ArrayList<String> enemyListIds;
    private Dialog addEnemiesDialog;
    private EditText enemiesUsernameText;
    private ListView enemiesListView;

    // Services
    private AuthService authService;
    private UserService userService;
    private RelationService relationService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_enemies, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(ENEMIES_TAG, getStringRes(R.string.info_enemies_initialization));

        initialize(view);

        getEnemyList();
    }

    /**
     * Ajoute un ennemi à la liste de l'utilisateur courant.
     * Déclanché lors d'un click sur le bouton de validation.
     */
    private void addEnemy() {
        Log.i(ENEMIES_TAG, getStringRes(R.string.info_add_enemy_button_fired));

        final String username = enemiesUsernameText.getText().toString();

        Boolean isValidForm = checkAddEnemyForm(username);

        if (!isValidForm) {
            return;
        }

        userService.searchByUsername(username).addOnSuccessListener(queryDocumentSnapshots -> {
            // Si la requête est bonne mais qu'aucun utilisateur n'est retourné
            if (queryDocumentSnapshots.isEmpty()) {
                Log.e(ENEMIES_TAG, getStringRes(R.string.info_user_not_found));

                Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_user_not_found), Toast.LENGTH_SHORT).show();

                return;
            }

            // Sinon, parcours des utilisateurs trouvés
            for (final QueryDocumentSnapshot userByUsername : queryDocumentSnapshots) {
                Log.i(ENEMIES_TAG, getStringRes(R.string.info_enemy_search_successful));

                relationService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
                    enemyListIds = (ArrayList<String>) documentSnapshot.get("enemyList");

                    // Vérifie si l'utilisateur trouvé n'est pas l'utilisateur courant
                    if (userByUsername.getId().equals(authService.getCurrentUser().getUid())) {
                        Log.e(ENEMIES_TAG, getStringRes(R.string.error_add_yourself_as_enemy));

                        Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_add_yourself_as_enemy), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    // Vérifie si l'utilisateur trouvé est déjà dans la liste d'ennemi de l'utilisateur courant
                    if (enemyListIds.indexOf(userByUsername.getId()) != -1) {
                        Log.e(ENEMIES_TAG, getStringRes(R.string.error_users_already_enemy));

                        Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_users_already_enemy), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    createUnfriendlyRelationship(authService.getCurrentUser().getUid(), userByUsername.getId());
                });
            }
        }).addOnFailureListener(exception -> {
            Log.w(ENEMIES_TAG, getStringRes(R.string.error_enemy_search_failed), exception);

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.error_enemy_search_failed), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Vérifie si le formulaire d'ajout d'ennemi est valide.
     *
     * @param username The username in the form.
     *
     * @return a boolean, false if the form is invalid.
     */
    private Boolean checkAddEnemyForm(String username) {
        // Vérification si les champs sont remplis.
        if (TextUtils.isEmpty(username)) {
            Log.e(ENEMIES_TAG, getStringRes(R.string.error_empty_field));

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
    private void createUnfriendlyRelationship(String sourceUserId, String targetUserId) {
        // Ajoute l'utilisateur cible dans la liste d'ennemi de l'utilisateur source
        final Task<Void> unfriendlyRelationshipTask = relationService.addEnemy(sourceUserId, targetUserId);

        // Ajoute l'utilisateur source dans la liste d'ennemi de l'utilisateur cible
        final Task<Void> secondUnfriendlyRelationshipTask = relationService.addEnemy(targetUserId, sourceUserId);

        unfriendlyRelationshipTask.continueWithTask(task -> secondUnfriendlyRelationshipTask).addOnSuccessListener(aVoid -> {
            Log.i(ENEMIES_TAG, getStringRes(R.string.info_enemy_storage));

            getEnemyList();
            enemiesUsernameText.setText("");
            addEnemiesDialog.hide();

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_enemy_storage), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Log.w(ENEMIES_TAG, getStringRes(R.string.info_enemy_storage), exception);

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_enemy_storage), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Supprime une relation entre deux utilisateurs.
     *
     * @param sourceUserId The source user ID.
     * @param targetUserId The target user ID.
     */
    public void removeUnfriendlyRelationship(String sourceUserId, String targetUserId) {
        // Supprime l'utilisateur cible dans la liste d'ennemi de l'utilisateur source
        final Task<Void> unfriendlyRelationshipTask = relationService.removeEnemy(sourceUserId, targetUserId);

        // Supprime l'utilisateur source dans la liste d'ennemi de l'utilisateur cible
        final Task<Void> secondUnfriendlyRelationshipTask = relationService.removeEnemy(targetUserId, sourceUserId);

        unfriendlyRelationshipTask.continueWithTask(task -> secondUnfriendlyRelationshipTask).addOnSuccessListener(aVoid -> {
            Log.i(ENEMIES_TAG, getStringRes(R.string.info_enemy_removal));

            getEnemyList();

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_enemy_removal), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Log.w(ENEMIES_TAG, getStringRes(R.string.info_enemy_removal), exception);

            Toast.makeText(getContext().getApplicationContext(), getStringRes(R.string.info_enemy_removal), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Récupère la liste d'ennemi au chargement de la vue.
     */
    private void getEnemyList() {
        relationService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
            Log.i(ENEMIES_TAG, getStringRes(R.string.info_get_enemy_list));

            enemyListIds = (ArrayList<String>) documentSnapshot.get("enemyList");

            HashMap<String, UserModel> enemyList = new HashMap<>();

            if (enemyListIds.size() == 0) {
                RelationListAdapter adapter = new RelationListAdapter(enemyList, EnemiesFragment.this);
                enemiesListView.setAdapter(adapter);
            }

            for (int i = 0; i < enemyListIds.size(); i++) {
                int index = i;

                userService.get(enemyListIds.get(index)).addOnSuccessListener(userDocumentSnapshot -> {
                    enemyList.put(enemyListIds.get(index), userDocumentSnapshot.toObject(UserModel.class));

                    if (enemyList.size() == enemyListIds.size()) {
                        RelationListAdapter adapter = new RelationListAdapter(enemyList, EnemiesFragment.this);
                        enemiesListView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    /**
     * Initialise les composants des différentes vues.
     */
    private void initialize(View view) {
        //Composants
        ImageButton enemiesModalButton = view.findViewById(R.id.enemies_modal_button);
        enemiesModalButton.setOnClickListener(v -> showAddEnemyModal());

        enemiesListView = view.findViewById(R.id.enemies_list_view);

        addEnemiesDialog = new Dialog(getContext());
        addEnemiesDialog.setContentView(R.layout.add_enemies_dialog);

        enemiesUsernameText = addEnemiesDialog.findViewById(R.id.enemies_username_text);
        Button enemiesSubmitButton = addEnemiesDialog.findViewById(R.id.enemies_submit_button);
        enemiesSubmitButton.setOnClickListener(v -> addEnemy());

        // Services
        authService = AuthService.getInstance();
        userService = UserService.getInstance();
        relationService = RelationService.getInstance();
    }

    /**
     * Affiche le dialogue addEnemies.
     */
    private void showAddEnemyModal() {
        addEnemiesDialog.show();
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
