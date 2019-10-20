package insset.ccm2.tartineo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Activité contenant l'ensemble de la logique liée
 * à la connexion de l'utilisateur sur l'application.
 */
public class LoginActivity extends AppCompatActivity {

    private final static String LOGIN_TAG = "LOGIN_ACTIVITY";

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Vérifie si l'utilisateur est déjà connecté.
        if (firebaseAuth.getCurrentUser() != null) {
            Log.i(LOGIN_TAG, getStringRes(R.string.info_user_already_logged_in));

            // TODO : Rediriger vers MapActivity si l'Utilisateur est déjà connecté.
            //startActivity(new Intent(getApplicationContext(), MapActivity.class));
        }
    }

    /**
     * Connecte un utilisateur sur l'application.
     * Déclanché lors d'un click sur le bouton de validation.
     *
     * @param view RegisterActivity view.
     */
    public void login(View view) {

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
     * @param id Id of the string resource.
     *
     * @return String resource.
     */
    private String getStringRes(int id) {
        return getResources().getString(id);
    }
}
