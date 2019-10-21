package insset.ccm2.tartineo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private final static String MAIN_TAG = "MAIN_ACTIVITY";

    BottomNavigationView bottomNavigationView;

    FirebaseUser firebaseUser;

    FirebaseAuth firebaseAuth;

    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(MAIN_TAG, getStringRes(R.string.info_main_initialization));

        initializeUI();

        initializeFirebase();

        // Load MapFragment by default
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_layout, new MapFragment()).commit();
    }

    /**
     * Déconnecte l'utilisateur courant de l'application.
     * Redirige vers la page de connexion.
     *
     * @param view
     */
    public void logout(View view) {
        // Déconnecte l'utilisateur de Firebase.
        firebaseAuth.signOut();

        // Redirige vers l'activité de connexion.
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));

        Log.i(MAIN_TAG, getStringRes(R.string.info_logout_successful));

        Toast.makeText(getApplicationContext(), getStringRes(R.string.info_logout_successful), Toast.LENGTH_LONG).show();
    }

    /**
     * Affiche la page qui correspond à l'élément sélectionné dans le menu.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;

                switch (menuItem.getItemId()) {
                    case R.id.action_map :
                        fragment = new MapFragment();
                        break;
                    case R.id.action_friends :
                        fragment = new FriendsFragment();
                        break;
                    case R.id.action_enemies :
                        fragment = new EnemiesFragment();
                        break;
                    case R.id.action_parameters :
                        fragment = new ParametersFragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_layout, fragment).commit();

                return true;
            }
        }
    ;

    /**
     * Initialise les composants de la vue.
     */
    private void initializeUI() {
        bottomNavigationView = findViewById(R.id.activity_main_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    /**
     * Initialise les composants de Firebase.
     */
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseFirestore.getInstance();
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
