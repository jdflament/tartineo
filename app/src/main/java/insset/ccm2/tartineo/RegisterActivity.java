package insset.ccm2.tartineo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import insset.ccm2.tartineo.models.User;

/**
 * Activité contenant l'ensemble de la logique liée
 * à l'inscription de l'utilisateur sur l'application.
 */
public class RegisterActivity extends AppCompatActivity {

    private final static String REGISTER_TAG = "REGISTER_ACTIVITY";

    EditText usernameText, emailText, passwordText, passwordConfirmationText;

    Button registerButton, backButton;

    FirebaseAuth firebaseAuth;

    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.i(REGISTER_TAG, getStringRes(R.string.info_register_initialization));

        initializeUI();

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        // Vérifie si l'utilisateur est déjà connecté.
        if (firebaseAuth.getCurrentUser() != null) {
            Log.i(REGISTER_TAG, getStringRes(R.string.info_user_already_logged_in));

            startActivity(new Intent(getApplicationContext(), MapActivity.class));
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

        final String username = usernameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirmation = passwordConfirmationText.getText().toString();

        // Vérification si les champs sont remplis.
        if (TextUtils.isEmpty(username)|| TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_empty_field));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_empty_field), Toast.LENGTH_SHORT).show();

            return;
        }

        // Vérification de la taille du mot de passe.
        if (password.length() < 6) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_password_length));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_password_length), Toast.LENGTH_SHORT).show();

            return;
        }

        // Vérification de l'égalité des champs mot de passe.
        if (!TextUtils.equals(password, passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_different_passwords));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_different_passwords), Toast.LENGTH_SHORT).show();

            return;
        }

        /*
         * Création d'un Utilisateur Firebase.
         */
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Redirection sur l'activité principale et enregistrement en base en cas de succès.
                if (task.isSuccessful()) {
                    Log.i(REGISTER_TAG, getStringRes(R.string.info_registration_successful));

                    // Ajoute le nom d'utilisateur au nouvel utilisateur enregistré.
                    FirebaseUser firebaseUser = Objects.requireNonNull(task.getResult()).getUser();
                    storeUsername(firebaseUser, username);

                    // Envoi l'email de vérification au nouvel utilisateur enregistré.
                    Objects.requireNonNull(firebaseUser).sendEmailVerification();

                    Toast.makeText(getApplicationContext(), getStringRes(R.string.info_registration_successful), Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(getApplicationContext(), MapActivity.class));

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
     * Enregistre un nom d'utilisateur en base de données.
     *
     * @param firebaseUser The Firebase User.
     * @param username The unique Username to store.
     */
    private void storeUsername(FirebaseUser firebaseUser, String username) {
        User newUser = new User(username);

        database
                .collection("users")
                .document(firebaseUser.getUid())
                .set(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(REGISTER_TAG, getStringRes(R.string.info_username_storage));
                        } else {
                            Log.w(REGISTER_TAG, getStringRes(R.string.error_username_storage), task.getException());
                        }
                    }
                });
    }

    /**
     * Initialise les composants de la vue.
     */
    private void initializeUI() {
        // Champs
        usernameText = findViewById(R.id.register_username_text);
        emailText = findViewById(R.id.register_email_text);
        passwordText = findViewById(R.id.register_password_text);
        passwordConfirmationText = findViewById(R.id.register_password_confirmation_text);

        // Boutons
        registerButton = findViewById(R.id.register_submit_button);
        backButton = findViewById(R.id.register_back_button);
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
