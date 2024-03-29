package insset.ccm2.tartineo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import insset.ccm2.tartineo.fragments.EnemiesFragment;
import insset.ccm2.tartineo.fragments.FriendsFragment;
import insset.ccm2.tartineo.fragments.MapFragment;
import insset.ccm2.tartineo.fragments.SettingsFragment;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.GoogleMapService;
import insset.ccm2.tartineo.services.NotificationService;
import insset.ccm2.tartineo.services.RelationService;

public class MainActivity extends AppCompatActivity {

    private final static String MAIN_TAG = "MAIN_ACTIVITY";

    // Fragments
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private MapFragment mapFragment = new MapFragment();
    private FriendsFragment friendsFragment = new FriendsFragment();
    private EnemiesFragment enemiesFragment = new EnemiesFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();

    // Services
    private GoogleMapService googleMapService;
    private AuthService authService;
    private NotificationService notificationService;
    private RelationService relationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        if (authService.getCurrentUser().isAnonymous()) {
            Log.e(MAIN_TAG, getStringRes(R.string.error_user_not_logged_in));

            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        createNotificationChannel(
                NotificationService.MARKERS_CHANNEL_ID,
                getStringRes(R.string.markers_channel_name),
                getStringRes(R.string.markers_channel_description)
        );
    }

    /**
     * Déconnecte l'utilisateur courant de l'application.
     * Redirige vers la page de connexion.
     *
     * @param view
     */
    public void logout(View view) {
        // Déconnecte l'utilisateur courant.
        authService.logout();

        // Réinitialise la carte
        googleMapService.reset();
        relationService.clearFriendList();
        relationService.clearEnemyList();

        // Redirige vers l'activité de connexion.
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        Toast.makeText(getApplicationContext(), getStringRes(R.string.info_logout_successful), Toast.LENGTH_LONG).show();
    }

    /**
     * Affiche la page qui correspond à l'élément sélectionné dans le menu.
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.action_map :
                        hideShowFragment(currentFragment, mapFragment);
                        currentFragment = mapFragment;
                        break;
                    case R.id.action_friends :
                        friendsFragment.updateFriendListView(RelationService.getInstance().getFriendList());
                        hideShowFragment(currentFragment, friendsFragment);
                        currentFragment = friendsFragment;
                        break;
                    case R.id.action_enemies :
                        enemiesFragment.updateEnemyListView(RelationService.getInstance().getEnemyList());
                        hideShowFragment(currentFragment, enemiesFragment);
                        currentFragment = enemiesFragment;
                        break;
                    case R.id.action_settings :
                        hideShowFragment(currentFragment, settingsFragment);
                        currentFragment = settingsFragment;
                        break;
                }

                return true;
            };

    /**
     * Initialise les composants de la vue.
     */
    private void initialize() {
        Log.i(MAIN_TAG, getStringRes(R.string.info_main_initialization));

        // Composants
        BottomNavigationView bottomNavigationView = findViewById(R.id.activity_main_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        fragmentManager = getSupportFragmentManager();

        createFragments();

        // Services
        authService = AuthService.getInstance();
        googleMapService = GoogleMapService.getInstance();
        notificationService = NotificationService.getInstance();
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

    /**
     * Hide a fragment.
     *
     * @param fragment The fragment to hide
     */
    private void addHideFragment(Fragment fragment) {
        fragmentManager.beginTransaction().add(R.id.activity_main_frame_layout, fragment).hide(fragment).commit();
    }

    /**
     * Hide a fragment and show another one.
     *
     * @param hide The fragment to hide
     * @param show The fragment to show
     */
    private void hideShowFragment(Fragment hide, Fragment show) {
        fragmentManager.beginTransaction().hide(hide).show(show).commit();
    }

    /**
     * Create default (and hide) fragment to the view.
     */
    private void createFragments() {
        addHideFragment(friendsFragment);
        addHideFragment(enemiesFragment);
        addHideFragment(settingsFragment);

        fragmentManager.beginTransaction().add(R.id.activity_main_frame_layout, mapFragment).commit();

        currentFragment = mapFragment;
    }

    /**
     * Create a notification channel.
     */
    private void createNotificationChannel(String id, CharSequence name, String description) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            notificationManager.createNotificationChannel(
                notificationService.createNotificationChannel(id, name, description)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
