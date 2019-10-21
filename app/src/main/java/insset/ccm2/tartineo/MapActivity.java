package insset.ccm2.tartineo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapActivity extends AppCompatActivity {

    private final static String MAP_TAG = "MAP_ACTIVITY";

    TextView welcomeUserTextView;

    FirebaseUser firebaseUser;

    FirebaseAuth firebaseAuth;

    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.i(MAP_TAG, getStringRes(R.string.info_map_initialization));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseFirestore.getInstance();

        welcomeUserTextView = findViewById(R.id.map_welcome_user);

        // Récupère le nom d'utilisateur de l'utilisateur Firebase actuellement connecté.
        database.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    welcomeUserTextView.setText("Welcome, " + doc.get("username"));
                }
            }
        });
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

        Log.i(MAP_TAG, getStringRes(R.string.info_logout_successful));

        Toast.makeText(getApplicationContext(), getStringRes(R.string.info_logout_successful), Toast.LENGTH_LONG).show();
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
