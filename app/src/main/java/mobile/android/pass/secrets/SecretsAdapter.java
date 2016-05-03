package mobile.android.pass.secrets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mobile.android.pass.R;

public class SecretsAdapter extends RecyclerView.Adapter<SecretsAdapter.SecretViewHolder> implements Filterable {

    private ArrayList<Secret> mOriginalList;
    private ArrayList<Secret> mFilteredList;

    public SecretsAdapter(Context context, ArrayList<Secret> secrets) {
        mOriginalList = secrets;
        mFilteredList = secrets;
    }

    @Override
    public SecretViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_secret, parent, false);
        SecretViewHolder holder = new SecretViewHolder(view);

        // Convert to a circular ImageView.
        ImageView icon = holder.getIcon();
        int backgroundColor = ((ColorDrawable) icon.getBackground()).getColor();
        Context context = icon.getContext();
        Bitmap bitmap = Bitmap.createBitmap(icon.getLayoutParams().width, icon.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(backgroundColor);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        drawable.setCircular(true);
        icon.setBackground(drawable);

        return holder;
    }

    @Override
    public void onBindViewHolder(SecretViewHolder holder, int position) {
        Secret secret = mFilteredList.get(position);
        holder.setDomain(secret.getDomain());
        holder.setUsername(secret.getUsername());
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    public static class SecretViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private ImageView mIcon;
        private TextView mIconText;
        private TextView mDomain;
        private TextView mUsername;
        private ImageView mActions;

        public SecretViewHolder(View v) {
            super(v); // done this way instead of view tagging

            mIcon = (ImageView) v.findViewById(R.id.item_secret_icon);
            mIconText = (TextView) v.findViewById(R.id.item_secret_icon_text);
            mDomain = (TextView) v.findViewById(R.id.item_secret_domain);
            mUsername = (TextView) v.findViewById(R.id.item_secret_username);
            mActions = (ImageView) v.findViewById(R.id.item_secret_actions);

            mActions.setOnClickListener(this);
        }

        public ImageView getIcon() {
            return mIcon;
        }

        public void setDomain(String domain) {
            mDomain.setText(domain);
            mIconText.setText(Character.toString(domain.charAt(0)));
        }

        public void setUsername(String username) {
            mUsername.setText(username);
        }

        @Override
        public void onClick(View view) {
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

    @Override
    public Filter getFilter() {
        return null;
    }
}
