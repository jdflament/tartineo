package insset.ccm2.tartineo.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import insset.ccm2.tartineo.R;
import insset.ccm2.tartineo.models.SettingsModel;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.SettingsService;
import insset.ccm2.tartineo.services.UserService;

public class SettingsFragment extends Fragment {

    private final static String SETTINGS_TAG = "SETTINGS_FRAGMENT";

    // Composants
    private TextView welcomeUserText;
    private SeekBar radiusSeekbar;
    private TextView radiusSeekbarValue;

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

        displayCurrentUsername();

        loadUserSettings();
    }

    /**
     * Récupère le nom d'utilisateur de l'utilisateur Firebase actuellement connecté.
     */
    private void displayCurrentUsername() {
        userService
            .get(authService.getCurrentUser().getUid())
            .addOnSuccessListener(documentSnapshot -> {
                Log.i(SETTINGS_TAG, getStringRes(R.string.info_username_displayed));
                welcomeUserText.setText(getResources().getString(R.string.logged_in, documentSnapshot.get("username")));
            })
        ;
    }

    /**
     * Charge les paramètres de l'utilisateur.
     */
    private void loadUserSettings() {
        settingsService
            .get(authService.getCurrentUser().getUid())
            .addOnSuccessListener(documentSnapshot -> {
                Log.d(SETTINGS_TAG, getStringRes(R.string.info_settings_loaded));
                SettingsModel settings = documentSnapshot.toObject(SettingsModel.class);

                if (settings != null) {
                    radiusSeekbar.setProgress(settings.getRadius());
                }
            })
            .addOnFailureListener(e -> Log.w(SETTINGS_TAG, getStringRes(R.string.error_settings_loading), e));
        ;
    }

    /**
     * Enregistre les paramètres de l'utilisateur.
     */
    private void saveSettings() {
        SettingsModel settings = new SettingsModel();

        int radius = radiusSeekbar.getProgress();

        settings.setRadius(radius);

        settingsService
            .update(authService.getCurrentUser().getUid(), settings)
            .addOnSuccessListener(aVoid -> {
                Log.i(SETTINGS_TAG, getStringRes(R.string.info_settings_updated));
                Toast.makeText(getActivity(), getStringRes(R.string.info_settings_updated),Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(SETTINGS_TAG, getStringRes(R.string.error_settings_storage), e);
                Toast.makeText(getActivity(), getStringRes(R.string.error_settings_storage),Toast.LENGTH_SHORT).show();
            })
        ;
    }

    /**
     * Affiche la valeur du radius au changement.
     */
    private OnSeekBarChangeListener updateRadiusSeekbarValue = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            radiusSeekbarValue.setText(getResources().getString(R.string.radius_value, progress, SettingsService.USER_MAX_RADIUS));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * Initialise les composants de la vue.
     */
    private void initialize(View view) {
        // Composants
        welcomeUserText = view.findViewById(R.id.welcome_user_text);

        radiusSeekbar = view.findViewById(R.id.radius_seekbar);
        radiusSeekbarValue = view.findViewById(R.id.radius_seekbar_value);

        radiusSeekbar.setOnSeekBarChangeListener(updateRadiusSeekbarValue);
        radiusSeekbar.setMin(SettingsService.USER_MIN_RADIUS);
        radiusSeekbar.setMax(SettingsService.USER_MAX_RADIUS);
        radiusSeekbarValue.setText(getResources().getString(R.string.radius_value, radiusSeekbar.getProgress(), SettingsService.USER_MAX_RADIUS));

        Button saveSettingsButton = view.findViewById(R.id.save_settings_button);
        saveSettingsButton.setOnClickListener(v -> saveSettings());

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
