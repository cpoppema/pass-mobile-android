package mobile.android.pass.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import mobile.android.pass.R;

public class PublicKeyDialogPreference extends DialogPreference {

    public PublicKeyDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_public_key);

        // This dialog does not store preferences.
        setPersistent(false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

//        final PublicKeyDialogPreference that = this;
        // Listen to click events in this layout.
        ListView listView = (ListView) view.findViewById(R.id.publicKeyActions);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                Log.d("clicked", item.toString());
                Log.d("clicked", getContext().getResources().getStringArray(R.array.pref_public_key_entry_values)[position]);
//                that.getDialog().cancel();
            }
        });
    }
}
