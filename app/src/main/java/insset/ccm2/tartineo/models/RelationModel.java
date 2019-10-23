package insset.ccm2.tartineo.models;

import java.util.ArrayList;

public class RelationModel {

    public ArrayList<String> friendList;
    public ArrayList<String> enemyList;

    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<String> friendList) {
        this.friendList = friendList;
    }

    public ArrayList<String> getEnemyList() {
        return enemyList;
    }

    public void setEnemyList(ArrayList<String> enemyList) {
        this.enemyList = enemyList;
    }
}
