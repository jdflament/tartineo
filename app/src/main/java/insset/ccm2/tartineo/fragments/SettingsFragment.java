package insset.ccm2.tartineo.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import insset.ccm2.tartineo.R;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.SettingsService;
import insset.ccm2.tartineo.services.UserService;

public class SettingsFragment extends Fragment {

    private final static String SETTINGS_TAG = "SETTINGS_FRAGMENT";

    // Composants
    private TextView welcomeUserText;

    // Services
    private AuthService authService;
    private UserService userService;
    private SettingsService settingsService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(SETTINGS_TAG, getStringRes(R.string.info_settings_initialization));

        initialize(view);

        // Récupère le nom d'utilisateur de l'utilisateur Firebase actuellement connecté.
        userService.get(authService.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
            welcomeUserText.setText(getResources().getString(R.string.logged_in, documentSnapshot.get("username")));
        });
    }

    /**
     * Initialise les composants de la vue.
     */
    private void initialize(View view) {
        // Composants
        welcomeUserText = view.findViewById(R.id.welcome_user_text);

        // Services
        authService = AuthService.getInstance();
        userService = UserService.getInstance();
        settingsService = SettingsService.getInstance();
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