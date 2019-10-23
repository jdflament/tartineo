package insset.ccm2.tartineo.models;

import java.util.ArrayList;

public class RelationModel {

    private ArrayList<String> friendList;

    private ArrayList<String> enemyList;

    public RelationModel() {
        this.friendList = new ArrayList<String>();
        this.enemyList = new ArrayList<String>();
    }

    /**
     * @return FriendList
     */
    public ArrayList<String> getFriendList() {
        return friendList;
    }

    /**
     * @param friendList
     */
    public void setFriendList(ArrayList<String> friendList) {
        this.friendList = friendList;
    }

    /**
     * @return EnemyList
     */
    public ArrayList<String> getEnemyList() {
        return enemyList;
    }

    /**
     * @param enemyList
     */
    public void setEnemyList(ArrayList<String> enemyList) {
        this.enemyList = enemyList;
    }
}
