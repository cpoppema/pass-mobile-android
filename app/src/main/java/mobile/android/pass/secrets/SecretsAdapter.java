package mobile.android.pass.secrets;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import mobile.android.pass.R;
import mobile.android.pass.utils.ImageViewHelper;

/** Creates views for showing a secret's information in a ListView. **/

public class SecretsAdapter extends CursorAdapter implements SectionIndexer {

    // Sections for fast scroll.
    private final String ALPHABET = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // SectionIndexer using the Latin alphabet.
    private AlphabetIndexer mAlphabetIndexer;
    // Context reference.
    private Context mContext;

    public SecretsAdapter(Context context) {
        super(context, null, false);

        mContext = context;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = super.swapCursor(newCursor);

        // Create an indexer for the first time, indexing on domain.
        if (newCursor != null) {
            mAlphabetIndexer = new AlphabetIndexer(newCursor, newCursor.getColumnIndex(Secret.DOMAIN), ALPHABET);
        }
        // Update an existing indexer with the latest cursor.
        if (mAlphabetIndexer != null) {
            mAlphabetIndexer.setCursor(newCursor);
        }

        return oldCursor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate from XML resource.
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_secret, parent, false);

        // Use the ViewHolder-pattern for better use of resources.
        SecretViewHolder holder = new SecretViewHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SecretViewHolder holder = (SecretViewHolder) view.getTag();

        // Setup the ViewHolder.
        holder.bindData(cursor);
        holder.getActions().setOnClickListener((View.OnClickListener) mContext);
        holder.getItem().setOnClickListener((View.OnClickListener) mContext);

        // Set the position as a tag so a PopUpMenu can easily be anchored to/restored for this
        // specific item.
        holder.getActions().setTag(cursor.getPosition());
        // Set the position as a tag so a SecretDialogHelper can easily be displayed/restored for
        // this specific item.
        holder.getItem().setTag(cursor.getPosition());
    }

    @Override
    public Object[] getSections() {
        if (mAlphabetIndexer == null) {
            return new Object[0];
        }

        return mAlphabetIndexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (mAlphabetIndexer == null) {
            return 0;
        }

        return mAlphabetIndexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        if (mAlphabetIndexer == null) {
            return 0;
        }

        return mAlphabetIndexer.getSectionForPosition(position);
    }

    /** Display a single secret's information, used inside a ListView **/
    public static class SecretViewHolder {
        // Background view.
        private RelativeLayout mItem;

        // Left-hand icon with the first letter of the domain.
        private ImageView mIcon;
        private TextView mIconText;

        // Full length version of the domain (as long as it fits).
        private TextView mDomain;

        // Full length version of the username (as long as it fits).
        private TextView mUsername;

        // Icon that triggers a PopupMenu with actions (copy/show/etc.).
        private View mActions;

        public SecretViewHolder(View v) {
            mItem = (RelativeLayout) v.findViewById(R.id.item_secret);
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

        public View getItem() {
            return mItem;
        }

        /** Copy text from Cursor object to all the TextViews **/
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
