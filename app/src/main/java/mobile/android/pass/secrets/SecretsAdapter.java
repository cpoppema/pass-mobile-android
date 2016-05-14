package mobile.android.pass.secrets;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import mobile.android.pass.R;
import mobile.android.pass.utils.ImageViewHelper;

public class SecretsAdapter extends CursorAdapter implements SectionIndexer {

    private Context mContext;
    private AlphabetIndexer mAlphabetIndexer;
    private final String ALPHABET = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public SecretsAdapter(Context context) {
        super(context, null, false);

        mContext = context;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = super.swapCursor(newCursor);

        if(newCursor != null) {
            mAlphabetIndexer = new AlphabetIndexer(newCursor, newCursor.getColumnIndex(Secret.DOMAIN), ALPHABET);
        }
        if(mAlphabetIndexer != null) {
            mAlphabetIndexer.setCursor(newCursor);
        }
        return oldCursor;
    }

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
        holder.getActions().setTag(cursor.getPosition());
        holder.getActions().setOnClickListener((View.OnClickListener) mContext);
    }

    @Override
    public Object[] getSections() {
        if(mAlphabetIndexer == null) {
            return new Object[0];
        }
        return mAlphabetIndexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if(mAlphabetIndexer == null) {
            return 0;
        }
        return mAlphabetIndexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        if(mAlphabetIndexer == null) {
            return 0;
        }
        return mAlphabetIndexer.getSectionForPosition(position);
    }

    public static class SecretViewHolder {
        private ImageView mIcon;
        private TextView mIconText;
        private TextView mDomain;
        private TextView mUsername;
        private View mActions;

        public SecretViewHolder(View v) {
            mIcon = (ImageView) v.findViewById(R.id.item_secret_icon);
            mIconText = (TextView) v.findViewById(R.id.item_secret_icon_text);
            mDomain = (TextView) v.findViewById(R.id.item_secret_domain);
            mUsername = (TextView) v.findViewById(R.id.item_secret_username);
            mActions = v.findViewById(R.id.item_secret_actions);

            // Convert mIcon to a circular ImageView.
            ImageViewHelper.convertToCircularImageView(mIcon);
        }

        public View getActions() {
            return mActions;
        }

        public void bindData(Cursor cursor) {
            String domain = cursor.getString(cursor.getColumnIndex(Secret.DOMAIN));
            String username = cursor.getString(cursor.getColumnIndex(Secret.USERNAME));
            String iconText = Character.toString(domain.charAt(0));

            mDomain.setText(domain);
            mUsername.setText(username);
            mIconText.setText(iconText);
        }
    }
}
