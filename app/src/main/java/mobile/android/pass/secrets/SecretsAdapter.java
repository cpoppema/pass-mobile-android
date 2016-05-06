package mobile.android.pass.secrets;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mobile.android.pass.R;
import mobile.android.pass.utils.CircularImageView;

public class SecretsAdapter extends CursorAdapter {

    private Context mContext;
//    private Cursor mOriginalCursor;
//    private Cursor mFilteredCursor;

    public SecretsAdapter(Context context) {
        super(context, null, false);

        mContext = context;
    }

//    @Override
//    public Cursor swapCursor(Cursor newCursor) {
//        Cursor oldCursor = super.swapCursor(newCursor);
//
//        // Set original cursor.
//        if (mOriginalCursor == null) {
//            mOriginalCursor = newCursor;
//        }
//        return oldCursor;
//    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_secret, parent, false);
        SecretViewHolder holder = new SecretViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SecretViewHolder holder = (SecretViewHolder) view.getTag();
        holder.bindData(cursor);
//        holder.setPosition(cursor.getPosition());
//        view.setOnClickListener((View.OnClickListener) mContext);
        holder.getActions().setTag(cursor.getPosition());
        holder.getActions().setOnClickListener((View.OnClickListener) mContext);
    }

    public static class SecretViewHolder {
        private ImageView mIcon;
        private TextView mIconText;
        private TextView mDomain;
        private TextView mUsername;
        private View mActions;
        private int mPosition;

        public SecretViewHolder(View v) {
            mIcon = (ImageView) v.findViewById(R.id.item_secret_icon);
            mIconText = (TextView) v.findViewById(R.id.item_secret_icon_text);
            mDomain = (TextView) v.findViewById(R.id.item_secret_domain);
            mUsername = (TextView) v.findViewById(R.id.item_secret_username);
            mActions = v.findViewById(R.id.item_secret_actions);

            // Convert mIcon to a circular ImageView.
            CircularImageView.convertToCircularImageView(mIcon);
        }

        public void bindData(Cursor cursor) {
            String domain = cursor.getString(cursor.getColumnIndex(Secret.DOMAIN));
            String username = cursor.getString(cursor.getColumnIndex(Secret.USERNAME));
            String iconText = Character.toString(domain.charAt(0));

            mDomain.setText(domain);
            mUsername.setText(username);
            mIconText.setText(iconText);
        }

        public View getActions() {
            return mActions;
        }

        public void setPosition(int position) {
            mPosition = position;
        }

        public int getPosition() {
            return mPosition;
        }
    }
}
