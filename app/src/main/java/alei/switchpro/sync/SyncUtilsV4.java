package alei.switchpro.sync;

import alei.switchpro.Utils;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;

public class SyncUtilsV4 {
    /**
     * Gets the state of background data.
     *
     * @param context
     * @return true if enabled
     */
    public static boolean getSync(Context context) {
        return getBackgroundDataState(context);
    }

    public static void toggleSync(Context context) {
        Intent launchIntent = new Intent();
        launchIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        launchIntent.setData(Uri.parse("custom:" + 2));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, launchIntent, 0);
        try {
            pi.send();
        } catch (CanceledException e1) {
            e1.printStackTrace();
        }

        // ֪ͨwidget����
        Utils.updateWidget(context);
    }

    public static boolean getBackgroundDataState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getBackgroundDataSetting();
    }

}
