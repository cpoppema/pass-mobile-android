package mobile.android.pass.secrets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mobile.android.pass.R;

public class SecretsAdapter extends ArrayAdapter<Secret> {
    public SecretsAdapter(Context context, ArrayList<Secret> secrets) {
        super(context, 0, secrets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Secret secret = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_secret, parent, false);
        }
        // Lookup view for data population
        TextView domain = (TextView) convertView.findViewById(R.id.item_secret_domain);
        TextView username = (TextView) convertView.findViewById(R.id.item_secret_username);

        // Populate the data into the template view using the data object.
        domain.setText(secret.getDomain());
        username.setText(secret.getUsername());

        // Return the completed view to render on screen.
        return convertView;
    }
}
