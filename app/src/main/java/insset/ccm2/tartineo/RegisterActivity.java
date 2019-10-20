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

/**
 * Activité contenant l'ensemble de la logique liée
 * à l'inscription de l'utilisateur sur l'application.
 */
public class RegisterActivity extends AppCompatActivity {

    private final static String REGISTER_TAG = "REGISTER_ACTIVITY";

    EditText usernameText, emailText, passwordText, passwordConfirmationText;

    Button registerButton, backButton;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.i(REGISTER_TAG, getStringRes(R.string.info_register_initialization));

        // Champs
        usernameText = findViewById(R.id.register_username_text);
        emailText = findViewById(R.id.register_email_text);
        passwordText = findViewById(R.id.register_password_text);
        passwordConfirmationText = findViewById(R.id.register_password_confirmation_text);

        // Boutons
        registerButton = findViewById(R.id.register_submit_button);
        backButton = findViewById(R.id.register_back_button);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Vérifie si l'utilisateur est déjà connecté.
        if (firebaseAuth.getCurrentUser() != null) {
            Log.i(REGISTER_TAG, getStringRes(R.string.info_user_already_logged_in));

            // TODO : Rediriger vers MapActivity si l'Utilisateur est déjà connecté.
            //startActivity(new Intent(getApplicationContext(), MapActivity.class));
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

        String username = usernameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirmation = passwordConfirmationText.getText().toString();

        // Vérification si les champs sont remplis.
        if (TextUtils.isEmpty(username)|| TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_empty_field));
            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_empty_field), Toast.LENGTH_SHORT).show();
        }

        // Vérification de la taille du mot de passe.
        if (password.length() < 6) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_password_length));
            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_password_length), Toast.LENGTH_SHORT).show();
        }

        // Vérification de l'égalité des champs mot de passe.
        if (!TextUtils.equals(password, passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_different_passwords));
            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_different_passwords), Toast.LENGTH_SHORT).show();
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

                    // TODO : Create MapActivity, then redirect to this one (and remove the Toast BTW).
                    Toast.makeText(getApplicationContext(), getStringRes(R.string.info_registration_successful), Toast.LENGTH_SHORT).show();

                    finish();
                }
                else {
                    Log.w(REGISTER_TAG, getStringRes(R.string.error_incorrect_email_or_password), task.getException());

                    Toast.makeText(getApplicationContext(), getStringRes(R.string.error_incorrect_email_or_password), Toast.LENGTH_SHORT).show();
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
     * @param id Id of the string resource.
     *
     * @return String resource.
     */
    private String getStringRes(int id) {
        return getResources().getString(id);
    }
}
