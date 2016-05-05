package mobile.android.pass.secrets;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mobile.android.pass.R;

public class SecretsAdapter extends RecyclerView.Adapter<SecretsAdapter.SecretViewHolder> {

    private boolean mDataValid = false;
    private Context mContext;
    private MatrixCursor mCursor;

    public SecretsAdapter(Context context) {
        mContext = context;
    }

    public void swapCursor(MatrixCursor cursor)
    {
        mCursor = cursor;
        mDataValid = mCursor != null;
        notifyDataSetChanged();
    }

    @Override
    public SecretViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // TODO parent.getContext() or mContext ?
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_secret, parent, false);
        SecretViewHolder holder = new SecretViewHolder(view);

        // Convert to a circular ImageView.
        ImageView icon = holder.getIcon();

        // Maintain current color.
        int backgroundColor = ((ColorDrawable) icon.getBackground()).getColor();
        Bitmap bitmap = Bitmap.createBitmap(icon.getLayoutParams().width, icon.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(backgroundColor);

        // Replace drawable.
        // TODO icon.getContext() or mContext or parent ?
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(parent.getResources(), bitmap);
        drawable.setCircular(true);
        icon.setBackground(drawable);
        bitmap.recycle();

        return holder;
    }

    public Cursor getItem(int position) {
        mCursor.moveToPosition(position);
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(SecretViewHolder holder, int position) {
        Cursor cursor = getItem(position);
        holder.bindData(cursor);

        // Hide divider for the last item.
        int visible = (position == getItemCount() - 1) ? View.INVISIBLE : View.VISIBLE;
        holder.getDivider().setVisibility(visible);
    }

    public static class SecretViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private ImageView mIcon;
        private TextView mIconText;
        private TextView mDomain;
        private TextView mUsername;
        private View mActions;
        private ImageView mDivider;

        public SecretViewHolder(View v) {
            super(v); // done this way instead of view tagging

            mIcon = (ImageView) v.findViewById(R.id.item_secret_icon);
            mIconText = (TextView) v.findViewById(R.id.item_secret_icon_text);
            mDomain = (TextView) v.findViewById(R.id.item_secret_domain);
            mUsername = (TextView) v.findViewById(R.id.item_secret_username);
            mActions = v.findViewById(R.id.item_secret_actions);
            mDivider = (ImageView) v.findViewById(R.id.item_secret_divider);

            mActions.setOnClickListener(this);
        }

        public void bindData(Cursor cursor)
        {
            String domain = cursor.getString(cursor.getColumnIndex("domain"));
            String username = cursor.getString(cursor.getColumnIndex("username"));
            String iconText = Character.toString(domain.charAt(0));

            mDomain.setText(domain);
            mUsername.setText(username);
            mIconText.setText(iconText);
        }

        public ImageView getDivider() {
            return mDivider;
        }

        public ImageView getIcon() {
            return mIcon;
        }

        @Override
        public void onClick(View view) {
            // TODO: block showing popupMenu while refresh is happening
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

            // Inflating the Popup using xml file.
            popupMenu.getMenuInflater().inflate(R.menu.menu_item_secret, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(this);

            // Show menu.
            popupMenu.show();

            // Position menu over button.
            ListPopupWindow.ForwardingListener listener = (ListPopupWindow.ForwardingListener) popupMenu.getDragToOpenListener();
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            listener.getPopup().setVerticalOffset(-view.getHeight()-lp.topMargin);

            // Redraw on the new position.
            listener.getPopup().show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Log.i("pass", "Clicked on " + item.getTitle());
            return true;
        }
    }
}
