package mobile.android.pass.secrets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by marcov on 13-4-16.
 */
public class SecretsAdapter extends ArrayAdapter<Secret> {
    private static LayoutInflater sLayoutInflater;

    private List<Secret> mSecrets;
    private int mResource;

    public SecretsAdapter(Context context, int resource, List<Secret> objects) {
        super(context, resource, objects);

        mResource = resource;
        mSecrets = objects;

        sLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private static class ViewHolder {
        public TextView domain;
        public TextView username;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = sLayoutInflater.inflate(mResource, null);
                holder = new ViewHolder();

                holder.domain = (TextView) vi.findViewById(android.R.id.text1);
                holder.username = (TextView) vi.findViewById(android.R.id.text2);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.domain.setText(mSecrets.get(position).getDomain());
            holder.username.setText(mSecrets.get(position).getUsername());
        } catch (Exception e) {

        }
        return vi;
    }

}
