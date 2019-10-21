package insset.ccm2.tartineo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ParametersFragment extends Fragment {

    private final static String PARAMETERS_TAG = "PARAMETERS_FRAGMENT";

    TextView welcomeUserText;

    FirebaseUser firebaseUser;

    FirebaseAuth firebaseAuth;

    FirebaseFirestore database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_parameters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(PARAMETERS_TAG, getStringRes(R.string.info_parameters_initialization));

        initializeUI(view);

        initializeFirebase();

        // Récupère le nom d'utilisateur de l'utilisateur Firebase actuellement connecté.
        database.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    welcomeUserText.setText("Welcome, " + doc.get("username"));
                }
            }
        });
    }

    /**
     * Initialise les composants de la vue.
     */
    private void initializeUI(View view) {
        welcomeUserText = view.findViewById(R.id.welcome_user_text);
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
