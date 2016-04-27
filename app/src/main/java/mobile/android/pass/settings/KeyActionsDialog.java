package mobile.android.pass.settings;

import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import mobile.android.pass.R;

public class KeyActionsDialog extends DialogPreference {

    private static Context mContext;

    // TODO: replace with http://developer.android.com/guide/topics/ui/menus.html maybe ?
    public KeyActionsDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_public_key);
        mContext = context;

        // This dialog does not store preferences.
        setPersistent(false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // Listen to click events in this layout.
        ListView listView = (ListView) view.findViewById(R.id.publicKeyActions);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item.toString().equals(mContext.getString(R.string.pref_public_key_action_copy))) {
                    // TODO: copy key
                    Log.i("pass", "Public key action: " + item.toString());
                } else if (item.toString().equals(mContext.getString(R.string.pref_public_key_action_copy_id))) {
                    // TODO: copy key id
                    Log.i("pass", "Public key action: " + item.toString());
                } else if (item.toString().equals(mContext.getString(R.string.pref_public_key_action_show))) {
                    // TODO: show key somehow
                    Log.i("pass", "Public key action: " + item.toString());
                } else if (item.toString().equals(mContext.getString(R.string.pref_public_key_action_create))) {
                    Log.i("pass", "Public key action: " + item.toString());
                    mContext.startActivity(new Intent(mContext, CreateKeyActivity.class));
                }
                KeyActionsDialog.this.getDialog().cancel();
            }
        });
    }
}
