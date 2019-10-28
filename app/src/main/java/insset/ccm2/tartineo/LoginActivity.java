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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import insset.ccm2.tartineo.services.AuthService;

/**
 * Activité contenant l'ensemble de la logique liée
 * à la connexion de l'utilisateur sur l'application.
 */
public class LoginActivity extends AppCompatActivity {

    private final static String LOGIN_TAG = "LOGIN_ACTIVITY";

    // Composants
    EditText emailText, passwordText;
    Button loginButton, registerPageButton;
    ProgressBar progressBar;

    // Services
    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();

        // Vérifie si l'utilisateur est déjà connecté.
        if (authService.getCurrentUser() != null) {
            Log.i(LOGIN_TAG, getStringRes(R.string.info_user_already_logged_in));

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    /**
     * Connecte un utilisateur sur l'application.
     * Déclanché lors d'un click sur le bouton de validation.
     *
     * @param view RegisterActivity view.
     */
    public void login(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        Boolean isValidForm = checkLoginForm(email, password);

        if (!isValidForm) {
            return;
        }

        authService.loginWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    Log.i(LOGIN_TAG, getStringRes(R.string.info_login_successful));

                    Toast.makeText(getApplicationContext(), getStringRes(R.string.info_login_successful), Toast.LENGTH_LONG).show();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                else {
                    Log.w(LOGIN_TAG, getStringRes(R.string.error_login_failed), task.getException());

                    Toast.makeText(getApplicationContext(), getStringRes(R.string.error_login_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Redirige vers la page d'inscription.
     *
     * @param view LoginActivity view.
     */
    public void registerPage(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    /**
     * Check if the login form is valid.
     *
     * @param email The email in the form.
     * @param password The password in the form.
     *
     * @return a boolean, false if the form is invalid.
     */
    private Boolean checkLoginForm(String email, String password) {
        // Vérification de la présence de l'email
        if (TextUtils.isEmpty(email)) {
            Log.e(LOGIN_TAG, getStringRes(R.string.error_empty_email));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_empty_email), Toast.LENGTH_LONG).show();

            return false;
        }

        // Vérification de la présence du mot de passe
        if (TextUtils.isEmpty(password)) {
            Log.e(LOGIN_TAG, getStringRes(R.string.error_empty_password));

            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_empty_password), Toast.LENGTH_LONG).show();

            return false;
        }

        return true;
    }

    /**
     * Initialise les variables nécessaires.
     */
    private void initialize() {
        Log.i(LOGIN_TAG, getStringRes(R.string.info_login_initialization));

        // Composants
        emailText = findViewById(R.id.login_email_text);
        passwordText = findViewById(R.id.login_password_text);

        loginButton = findViewById(R.id.login_submit_button);
        registerPageButton = findViewById(R.id.login_register_page_button);

        progressBar = findViewById(R.id.login_progress_bar);

        // Services
        authService = AuthService.getInstance();
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
