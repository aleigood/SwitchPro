package alei.switchpro.task;

import alei.switchpro.Constants;
import android.database.Cursor;

public final class Toggle {
    public static final int SWITCH_RINGTONE = 0;
    public static final int SWITCH_RADIO = 1;
    public static final int SWITCH_DATA_CONN = 2;
    public static final int SWITCH_WIFI = 3;
    public static final int SWITCH_SYNC = 4;
    public static final int SWITCH_VOLUME = 5;
    public static final int SWITCH_SILENT = 6;

    public int id;
    public int taskId;
    public int switchId;
    public String param1;
    public String param2;

    public Toggle(Cursor c) {
        id = c.getInt(Constants.SWITCH.INDEX_ID);
        taskId = c.getInt(Constants.SWITCH.INDEX_TASK_ID);
        switchId = c.getInt(Constants.SWITCH.INDEX_SWITCH_ID);
        param1 = c.getString(Constants.SWITCH.INDEX_PARAM1);
        param2 = c.getString(Constants.SWITCH.INDEX_PARAM2);
    }

}
