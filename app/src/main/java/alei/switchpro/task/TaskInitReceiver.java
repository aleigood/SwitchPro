package alei.switchpro.task;

import alei.switchpro.DatabaseOper;
import alei.switchpro.MyApplication;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TaskInitReceiver extends BroadcastReceiver {

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED. Resets alarm on TIME_SET,
     * TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseOper dbOper = MyApplication.getInstance().getDataOper();
        String action = intent.getAction();

        if (context.getContentResolver() == null) {
            return;
        }

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            TaskUtil.disableExpiredAlarms(dbOper);
        }

        TaskUtil.setNextAlert(dbOper);
    }
}
