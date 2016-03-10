package alei.switchpro.sync;

import alei.switchpro.Constants;
import alei.switchpro.R;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SyncUtils {
    public static boolean getSync(Context context) {
        boolean backgroundData = getBackgroundDataState(context);
        boolean sync = getMasterSyncAutomatically();
        return backgroundData && sync;
    }

    /**
     * Toggle auto-sync
     *
     * @param context
     */
    public static void toggleSync(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundData = getBackgroundDataState(context);
        boolean sync = getMasterSyncAutomatically();

        // need open background data
        if (!backgroundData) {
            Toast.makeText(context, R.string.need_open_backdata, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sync) {
            if (config.getBoolean(Constants.PREFS_SYNC_NOW, false)) {
                Bundle extras = new Bundle();
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
                context.getContentResolver().startSync(null, extras);
            }

            setMasterSyncAutomatically(true);
        } else {
            setMasterSyncAutomatically(false);
        }
    }

    /**
     * Gets the state of background data.
     *
     * @param context
     * @return true if enabled
     */
    public static boolean getBackgroundDataState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getBackgroundDataSetting();
    }

    private static boolean getMasterSyncAutomatically() {
        try {
            return ((Boolean) (ContentResolver.class.getMethod("getMasterSyncAutomatically", new Class[]{}).invoke(
                    null, new Object[]{}))).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void setMasterSyncAutomatically(boolean b) {
        try {
            ContentResolver.class.getMethod("setMasterSyncAutomatically", new Class[]{boolean.class}).invoke(null,
                    new Object[]{b});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
