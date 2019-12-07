package insset.ccm2.tartineo.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Map;

import insset.ccm2.tartineo.R;
import insset.ccm2.tartineo.fragments.EnemiesFragment;
import insset.ccm2.tartineo.fragments.FriendsFragment;
import insset.ccm2.tartineo.models.UserModel;
import insset.ccm2.tartineo.services.AuthService;
import insset.ccm2.tartineo.services.MapService;

public class RelationListAdapter extends BaseAdapter {
    private final ArrayList mData;
    private FriendsFragment friendsFragment;
    private EnemiesFragment enemiesFragment;

    // Services
    private MapService mapService;
    private AuthService authService;

    public RelationListAdapter(Map<String, UserModel> map, FriendsFragment friendsFragment) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());

        this.friendsFragment = friendsFragment;
    }

    public RelationListAdapter(Map<String, UserModel> map, EnemiesFragment enemiesFragment) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());

        this.enemiesFragment = enemiesFragment;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, UserModel> getItem(int position) {
        return (Map.Entry) mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        result = (convertView == null) ? LayoutInflater.from(parent.getContext()).inflate(R.layout.relation_list_adapter, parent, false) : convertView;

        // Composants
        Map.Entry<String, UserModel> item = getItem(position);
        UserModel userModel = item.getValue();

        ((TextView) result.findViewById(android.R.id.text1)).setText(userModel.getUsername());

        Button deleteButton = result.findViewById(R.id.relation_list_delete_button);
        
        deleteButton.setOnClickListener(v -> {
            if (friendsFragment != null) {
                removeFriend(item);
            }

            if (enemiesFragment != null) {
                removeEnemy(item);
            }
        });

        // Services
        authService = AuthService.getInstance();
        mapService = MapService.getInstance();

        return result;
    }

    /**
     * Supprime un ami à la liste de l'utilisateur courant.
     * Déclanché lors d'un click sur le bouton de suppression.
     *
     * @param item The user ID and username of UserModel.
     */
    private void removeFriend(Map.Entry<String, UserModel> item) {
        String targetUserId = String.valueOf(item.getKey());

        friendsFragment.removeFriendship(authService.getCurrentUser().getUid(), targetUserId);
        mapService.removeMarker(targetUserId);
    }

    /**
     * Supprime un ennemi à la liste de l'utilisateur courant.
     * Déclanché lors d'un click sur le bouton de suppression.
     *
     * @param item The user ID and username of UserModel.
     */
    private void removeEnemy(Map.Entry<String, UserModel> item) {
        enemiesFragment.removeUnfriendlyRelationship(authService.getCurrentUser().getUid(), String.valueOf(item.getKey()));
    }
}
