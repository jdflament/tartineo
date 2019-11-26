package insset.ccm2.tartineo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import insset.ccm2.tartineo.models.RelationModel;
import insset.ccm2.tartineo.models.UserModel;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.RelationService;
import insset.ccm2.tartineo.services.UserService;

/**
 * Activité contenant l'ensemble de la logique liée
 * à l'inscription de l'utilisateur sur l'application.
 */
public class RegisterActivity extends AppCompatActivity {

    private final static String REGISTER_TAG = "REGISTER_ACTIVITY";

    // Composants
    private EditText usernameText, emailText, passwordText, passwordConfirmationText;
    private ProgressBar progressBar;

    // Services
    private AuthService authService;
    private UserService userService;
    private RelationService relationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initialize();

        // Vérifie si l'utilisateur est déjà connecté.
        if (authService.getCurrentUser() != null) {
            Log.i(REGISTER_TAG, getStringRes(R.string.info_user_already_logged_in));

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    /**
     * Inscrit un nouvel utilisateur sur l'application.
     * Déclanché lors d'un click sur le bouton de validation.
     *
     * @param view RegisterActivity view.
     */
    public void register(View view) {
        Log.i(REGISTER_TAG, getStringRes(R.string.info_register_button_fired));

        progressBar.setVisibility(View.VISIBLE);

        final String username = usernameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirmation = passwordConfirmationText.getText().toString();

        Boolean isValidForm = checkRegisterForm(username, email, password, passwordConfirmation);

        if (!isValidForm) {
            return;
        }

        /*
         * Création d'un Utilisateur Firebase.
         */
        authService.registerWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);

                // Redirection sur l'activité principale et enregistrement en base en cas de succès.
                if (task.isSuccessful()) {
                    Log.i(REGISTER_TAG, getStringRes(R.string.info_registration_successful));

                    // Ajoute le nom d'utilisateur au nouvel utilisateur enregistré.
                    FirebaseUser firebaseUser = Objects.requireNonNull(task.getResult()).getUser();
                    storeUsername(firebaseUser, username);

                    // Ajoute une collection "relations" au nouvel utilisateur enregistré.
                    createUserRelations(firebaseUser);

                    // Envoi l'email de vérification au nouvel utilisateur enregistré.
                    Objects.requireNonNull(firebaseUser).sendEmailVerification();

                    Toast.makeText(getApplicationContext(), getStringRes(R.string.info_registration_successful), Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    finish();
                }
                else {
                    Log.w(REGISTER_TAG, getStringRes(R.string.error_registration_failed), task.getException());

                    Toast.makeText(getApplicationContext(), getStringRes(R.string.error_registration_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Redirige vers la page de login.
     *
     * @param view RegisterActivity view.
     */
    public void loginPage(View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    /**
     * Check if the register form is valid.
     *
     * @param username The username in the form.
     * @param email The email in the form.
     * @param password The password in the form.
     * @param passwordConfirmation The password confirmation in the form.
     *
     * @return a boolean, false if the form is invalid.
     */
    private Boolean checkRegisterForm(String username, String email, String password, String passwordConfirmation) {
        // Vérification si les champs sont remplis.
        if (TextUtils.isEmpty(username)|| TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_empty_field));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_empty_field), Toast.LENGTH_SHORT).show();

            return false;
        }

        // Vérification de la taille du mot de passe.
        if (password.length() < 6) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_password_length));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_password_length), Toast.LENGTH_SHORT).show();

            return false;
        }

        // Vérification de l'égalité des champs mot de passe.
        if (!TextUtils.equals(password, passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_different_passwords));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_different_passwords), Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    /**
     * Enregistre un nom d'utilisateur en base de données.
     *
     * @param firebaseUser The Firebase UserModel.
     * @param username The unique Username to store.
     */
    private void storeUsername(FirebaseUser firebaseUser, String username) {
        UserModel newUser = new UserModel(username);

        userService
                .set(firebaseUser.getUid(), newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(REGISTER_TAG, getStringRes(R.string.info_username_storage));
                    } else {
                        Log.w(REGISTER_TAG, getStringRes(R.string.error_username_storage), task.getException());

                        Toast.makeText(getApplicationContext(), getStringRes(R.string.error_has_occurred), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Créer un collection "relations" pour l'utilisateur.
     * Cette collection comprend une liste d'amis et d'ennemis.
     *
     * @param firebaseUser The Firebase UserModel.
     */
    private void createUserRelations(FirebaseUser firebaseUser) {
        RelationModel relation = new RelationModel();

        relationService
                .set(firebaseUser.getUid(), relation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(REGISTER_TAG, getStringRes(R.string.info_user_relations_storage));
                    } else {
                        Log.w(REGISTER_TAG, getStringRes(R.string.error_user_relations_storage), task.getException());

                        Toast.makeText(getApplicationContext(), getStringRes(R.string.error_has_occurred), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Initialise les variables nécessaires.
     */
    private void initialize() {
        Log.i(REGISTER_TAG, getStringRes(R.string.info_register_initialization));

        // Components
        usernameText = findViewById(R.id.register_username_text);
        emailText = findViewById(R.id.register_email_text);
        passwordText = findViewById(R.id.register_password_text);
        passwordConfirmationText = findViewById(R.id.register_password_confirmation_text);

        progressBar = findViewById(R.id.register_progress_bar);

        // Services
        authService = AuthService.getInstance();
        userService = UserService.getInstance();
        relationService = RelationService.getInstance();
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
