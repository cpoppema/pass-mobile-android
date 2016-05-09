package mobile.android.pass.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

public class CircularImageView {
    public static void convertToCircularImageView(ImageView v) {
        int backgroundColor = ((ColorDrawable) v.getBackground()).getColor();
        Bitmap bitmap = Bitmap.createBitmap(v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(backgroundColor);

        // Replace drawable.
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(v.getResources(), bitmap);
        drawable.setCircular(true);
        v.setBackground(drawable);

        // Causing these log entries in 6.0:
        // Called getWidth() on a recycle()'d bitmap! This is undefined behavior!
        // Called getHeight() on a recycle()'d bitmap! This is undefined behavior!
//        bitmap.recycle();
    }
}