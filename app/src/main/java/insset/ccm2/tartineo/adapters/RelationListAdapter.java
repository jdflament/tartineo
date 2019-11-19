package insset.ccm2.tartineo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import insset.ccm2.tartineo.R;
import insset.ccm2.tartineo.models.UserModel;

public class RelationListAdapter extends BaseAdapter {
    private final ArrayList mData;

    public RelationListAdapter(Map<String, UserModel> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
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

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.relation_list_adapter, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<String, UserModel> item = getItem(position);
        UserModel userModel = item.getValue();

        ((TextView) result.findViewById(android.R.id.text1)).setText(item.getKey());
        ((TextView) result.findViewById(android.R.id.text2)).setText(userModel.getUsername());

        return result;
    }
}
