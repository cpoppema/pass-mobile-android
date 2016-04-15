package mobile.android.pass.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import mobile.android.pass.R;

/**
 * Helper for using the clipboard.
 */
public class ClipboardHelper {

    /**
     * Function to add text to the clipboard with the app name as key.
     * @param context
     * @param text
     */
    public static void addToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name),
                text);
        clipboard.setPrimaryClip(clip);
    }
}
