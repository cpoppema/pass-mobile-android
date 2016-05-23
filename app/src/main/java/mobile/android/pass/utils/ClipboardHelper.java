package mobile.android.pass.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Helper for using the clipboard.
 */
public class ClipboardHelper {

    private static final String TAG = ClipboardHelper.class.toString();

    /**
     * Function to add text to the clipboard4.
     */
    public static void copy(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(TAG, text);
        clipboard.setPrimaryClip(clip);
    }
}
