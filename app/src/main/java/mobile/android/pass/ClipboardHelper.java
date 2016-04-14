package mobile.android.pass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created by marcov on 14-4-16.
 */
public class ClipboardHelper {

    public static void addToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name),
                text);
        clipboard.setPrimaryClip(clip);
    }
}
