package alei.switchpro.task;

import alei.switchpro.Constants;
import alei.switchpro.DatabaseOper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.Parcel;
import android.text.format.DateFormat;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The Alarms provider supplies info about Alarm Clock settings
 */
public class TaskUtil {
    // This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    public static final String ALARM_ALERT_ACTION = "alei.switchpro.task.TASK_ACION";

    // This string is used to indicate a silent alarm in the db.
    public static final String ALARM_ALERT_SILENT = "silent";

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    // This string is used to identify the alarm id passed to SetAlarm from the
    // list of alarms.
    public static final String ALARM_ID = "alarm_id";
    // Shared with DigitalClock
    final static String M24 = "kk:mm";
    private final static String M12 = "h:mm aa";
    private static final String TAG_TASK = "task";
    private static final String TAG_SWITCH = "switch";

    /**
     * �����ݿ������һ����¼
     */
    public static int addAlarm(DatabaseOper ap) {
        ContentValues values = new ContentValues(2);
        values.put(Constants.TASK.COLUMN_START_HOUR, 8);
        values.put(Constants.TASK.COLUMN_END_HOUR, 9);
        return ap.insertTask(values);
    }

    /**
     * ��ȡָ����Alarm,�п��ܷ���Null
     */
    public static Task getAlarmById(DatabaseOper ap, int alarmId) {
        Cursor cursor = ap.queryTaskById(alarmId);
        Task alarm = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Task(cursor);
            }

            cursor.close();
        }

        return alarm;
    }

    public static List<Toggle> getSwitchesByTaskId(DatabaseOper ap, int taskId) {
        Cursor cursor = ap.querySwitchesByTaskId(taskId);
        List<Toggle> list = new ArrayList<Toggle>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Toggle(cursor));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }

        return list;
    }

    public static void deleteSwitch(DatabaseOper ap, int taskId) {
        ap.deleteSwitchById(taskId);
    }

    public static void addSwitch(DatabaseOper ap, int taskid, int switchid, String param1, String param2) {
        ContentValues values = new ContentValues(4);
        values.put(Constants.SWITCH.COLUMN_TASK_ID, taskid);
        values.put(Constants.SWITCH.COLUMN_SWITCH_ID, switchid);
        values.put(Constants.SWITCH.COLUMN_PARAM1, param1);
        values.put(Constants.SWITCH.COLUMN_PARAM2, param2);
        ap.insertSwitch(values);
    }

    public static void updateSwitch(DatabaseOper ap, int taskid, int switchid, String param1, String param2) {
        ContentValues values = new ContentValues();

        if (param1 != null) {
            values.put(Constants.SWITCH.COLUMN_PARAM1, param1);
        }

        if (param2 != null) {
            values.put(Constants.SWITCH.COLUMN_PARAM2, param2);
        }

        ap.updateSwitch(taskid, switchid, values);
    }

    /**
     * ɾ��һ�����񣬵�����Ҫ������һ������
     */
    public static void deleteAlarm(DatabaseOper ap, int alarmId) {
        ap.deleteTask(alarmId);
        setNextAlert(ap);
    }

    // Private method to get a more limited set of alarms from the database.
    private static Cursor getEnabledAlarm(DatabaseOper ap) {
        return ap.queryEnabledTask();
    }

    public static void setAlarm(DatabaseOper ap, int id, int startHour, int startMinutes, int endHour, int endMinutes,
                                Task.DaysOfWeek daysOfWeek, boolean enabled, String message) {
        ContentValues values = new ContentValues(9);
        long startTime = 0;
        long endTime = 0;

        // ֻ���ڲ����ظ������ʱ����������Ĵ���ʱ�䣬�ô��ж���������������ʱ��������������
        // �������ֻ����������󣬼�����һ����������ʱ��֪������������Ѿ���������û����
        if (!daysOfWeek.isRepeatSet()) {
            startTime = calculateAlarm(startHour, startMinutes, daysOfWeek).getTimeInMillis();
            endTime = calculateAlarm(endHour, endMinutes, daysOfWeek).getTimeInMillis();
        }

        values.put(Constants.TASK.COLUMN_START_HOUR, startHour);
        values.put(Constants.TASK.COLUMN_START_MINUTES, startMinutes);
        values.put(Constants.TASK.COLUMN_END_HOUR, endHour);
        values.put(Constants.TASK.COLUMN_END_MINUTES, endMinutes);
        values.put(Constants.TASK.COLUMN_START_TIME, startTime);
        values.put(Constants.TASK.COLUMN_END_TIME, endTime);
        values.put(Constants.TASK.COLUMN_DAYS_OF_WEEK, daysOfWeek.getCoded());
        values.put(Constants.TASK.COLUMN_ENABLED, enabled ? 1 : 0);
        values.put(Constants.TASK.COLUMN_MESSAGE, message);
        ap.updateTask(id, values);

        setNextAlert(ap);
    }

    /**
     * A convenience method to enable or disable an alarm.
     *
     * @param id      corresponds to the _id column
     * @param enabled corresponds to the ENABLED column
     */
    public static void enableAlarm(DatabaseOper ap, int alarmId, boolean enabled) {
        enableAlarmInternal(ap, getAlarmById(ap, alarmId), enabled);
        setNextAlert(ap);
    }

    private static void enableAlarmInternal(DatabaseOper ap, final Task alarm, boolean enabled) {
        ContentValues values = new ContentValues(3);
        values.put(Constants.TASK.COLUMN_ENABLED, enabled ? 1 : 0);

        // ������һ������ʱ��Ҫ�������Ĵ���ʱ��
        if (enabled) {
            long startTime = 0;
            long endTime = 0;

            if (!alarm.daysOfWeek.isRepeatSet()) {
                startTime = calculateAlarm(alarm.startHour, alarm.startMinutes, alarm.daysOfWeek).getTimeInMillis();
                endTime = calculateAlarm(alarm.endHour, alarm.endMinutes, alarm.daysOfWeek).getTimeInMillis();
            }

            values.put(Constants.TASK.COLUMN_START_TIME, startTime);
            values.put(Constants.TASK.COLUMN_END_TIME, endTime);
        }

        ap.updateTask(alarm.id, values);
    }

    /**
     * ����һ����������ڣ���ϵͳ���������
     */
    public static void disableExpiredAlarms(DatabaseOper ap) {
        Cursor cur = getEnabledAlarm(ap);
        long now = System.currentTimeMillis();

        if (cur.moveToFirst()) {
            do {
                Task alarm = new Task(cur);

                // ��ʼ�ͽ���ʱ�䲻Ϊ0��˵�������ظ�����
                // ����ʼ�ͽ���ʱ�䶼����ʱ��˵���������Ѿ�������
                if (alarm.startTime != 0 && alarm.startTime < now && alarm.endTime != 0 && alarm.endTime < now) {
                    enableAlarmInternal(ap, alarm, false);
                }
            }
            while (cur.moveToNext());
        }
        cur.close();
    }

    /**
     * Called at system startup, on time/timezone change, and whenever the user
     * changes alarm settings. Activates snooze if set, otherwise loads all
     * alarms, activates next alert. �����渽��ֵ������ʱ�Եģ�������뵽���ݿ��У����ᴫ�ݵ�Intent��Receiver
     */
    public static void setNextAlert(DatabaseOper ap) {
        Task alarm = null;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        Cursor cursor = getEnabledAlarm(ap);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Task a = new Task(cursor);

                    // �����ظ�������
                    if (a.startTime == 0 && a.endTime == 0) {
                        a.startTime = calculateAlarm(a.startHour, a.startMinutes, a.daysOfWeek).getTimeInMillis();
                        a.endTime = calculateAlarm(a.endHour, a.endMinutes, a.daysOfWeek).getTimeInMillis();
                    }
                    // �����ظ������������ʼʱ��ͽ���ʱ�䶼�����ˣ������������
                    else if (a.startTime < now && a.endTime < now) {
                        enableAlarmInternal(ap, a, false);
                        continue;
                    }

                    // �ߵ�һ�µ����̵����ظ������񣬺Ϳ�ʼʱ������ʱ��û���ڵ�����
                    // �����ظ�������������Ŀ�ʼ�ͽ�������ʱ�䣬���￪ʼʱ���п��ܴ��ڽ���ʱ�䣬��Ϊ�����ʼʱ������ˣ�������ľ�����һ����ʼʱ��
                    // ����һ���Ե����񣬿�ʼʱ��ʼ��С�ڽ���ʱ�䣬���ҿ�ʼʱ���п����Ѿ�����

                    // �����ʼʱ��û���ڣ�����С
                    if (a.startTime > now && a.startTime < minTime) {
                        minTime = a.startTime;
                        alarm = a;
                    }

                    // �п���ĳ����ʼʱ���Ѿ������ˣ���ʼʱ���Ǵ��ڽ���ʱ��ģ��������жϽ���ʱ��
                    if (a.endTime < minTime) {
                        minTime = a.endTime;
                        alarm = a;
                    }
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }

        // �����õ���Alarm������������������ʱ��
        if (alarm != null) {
            // ��ʼʱ���п����Ѿ�������(һ��������)
            if (alarm.startTime < now) {
                // ˵���ǽ�������
                alarm.type = 1;
                enableAlert(ap, alarm, alarm.endTime);
            } else {
                // ���߿�ʼʱ���Ǵ��ڽ���ʱ��ģ���������
                if (alarm.startTime > alarm.endTime) {
                    alarm.type = 1;
                    enableAlert(ap, alarm, alarm.endTime);
                } else {
                    alarm.type = 0;
                    enableAlert(ap, alarm, alarm.startTime);
                }
            }
        } else {
            disableAlert(ap);
        }
    }

    /**
     * Sets alert in AlarmManger and StatusBar. This is what will actually
     * launch the alert when the alarm triggers.
     *
     * @param alarm          Alarm.
     * @param atTimeInMillis milliseconds since epoch
     */
    private static void enableAlert(DatabaseOper ap, final Task alarm, final long atTimeInMillis) {
        AlarmManager am = (AlarmManager) ap.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_ALERT_ACTION);

        // XXX: This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the Alarm class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The AlarmReceiver class knows to build the Alarm
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        alarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent
                .getBroadcast(ap.getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
    }

    /**
     * Disables alert in AlarmManger and StatusBar.
     *
     * @param id Alarm ID.
     */
    static void disableAlert(DatabaseOper ap) {
        AlarmManager am = (AlarmManager) ap.getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(ap.getContext(), 0, new Intent(ALARM_ALERT_ACTION),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }

    /**
     * Given an alarm in hours and minutes, return a time suitable for setting
     * in AlarmManager.
     *
     * @param hour       Always in 24 hour 0-23
     * @param minute     0-59
     * @param daysOfWeek 0-59
     */
    static Calendar calculateAlarm(int hour, int minute, Task.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        /*
         * Log.v("** TIMES * " + c.getTimeInMillis() + " hour " + hour +
         * " minute " + minute + " dow " + c.get(Calendar.DAY_OF_WEEK) +
         * " from now " + addDays);
         */
        if (addDays > 0)
            c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }

    static String formatTime(final Context context, int hour, int minute, Task.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlarm(hour, minute, daysOfWeek);
        return formatTime(context, c);
    }

    /* used by AlarmAlert */
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String) DateFormat.format(format, c);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }

    /**
     * �����ݿ��е����ñ�����xml�ļ���
     *
     * @param ap
     * @return
     */
    public static boolean saveTaskConf(DatabaseOper ap) {
        try {
            // �ڱ���֮ǰ��Ҫ�ж� SDCard �Ƿ����,�����Ƿ���п�дȨ�ޣ�
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // ��ȡSDCardĿ¼,2.2��ʱ��Ϊ:/mnt/sdcart
                // 2.1��ʱ��Ϊ��/sdcard������ʹ�þ�̬�����õ�·�����һ�㡣
                File sdCardDir = Environment.getExternalStorageDirectory();
                File dir = new File(sdCardDir.getPath() + File.separator + Constants.BACK_FILE_PATH);

                if (!dir.exists()) {
                    dir.mkdir();
                }

                File saveFile = new File(dir.getPath(), Constants.TASK.TABLE_TASK);
                FileOutputStream outStream = new FileOutputStream(saveFile);
                outStream.write(writeTaskToXml(ap).getBytes());
                outStream.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String writeTaskToXml(DatabaseOper ap) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.startDocument(Constants.DEFAULT_ENCODING, true);
            serializer.startTag("", Constants.APP_NAME);
            Cursor taskCursor = ap.queryAllTask();

            if (taskCursor.moveToFirst()) {
                do {
                    Task task = new Task(taskCursor);
                    serializer.startTag("", TAG_TASK);
                    serializer.attribute("", Constants.TASK.COLUMN_START_HOUR, task.startHour + "");
                    serializer.attribute("", Constants.TASK.COLUMN_START_MINUTES, task.startMinutes + "");
                    serializer.attribute("", Constants.TASK.COLUMN_END_HOUR, task.endHour + "");
                    serializer.attribute("", Constants.TASK.COLUMN_END_MINUTES, task.endMinutes + "");
                    serializer.attribute("", Constants.TASK.COLUMN_DAYS_OF_WEEK, task.daysOfWeek.getCoded() + "");
                    serializer.attribute("", Constants.TASK.COLUMN_START_TIME, task.startTime + "");
                    serializer.attribute("", Constants.TASK.COLUMN_END_TIME, task.endTime + "");
                    serializer.attribute("", Constants.TASK.COLUMN_ENABLED, task.enabled ? "1" : "0");
                    serializer.attribute("", Constants.TASK.COLUMN_MESSAGE, task.message + "");

                    List<Toggle> switchList = getSwitchesByTaskId(ap, task.id);

                    for (Toggle toggle : switchList) {
                        serializer.startTag("", TAG_SWITCH);
                        serializer.attribute("", Constants.SWITCH.COLUMN_SWITCH_ID, toggle.switchId + "");
                        serializer.attribute("", Constants.SWITCH.COLUMN_PARAM1, toggle.param1 + "");
                        serializer.attribute("", Constants.SWITCH.COLUMN_PARAM2, toggle.param2 + "");
                        serializer.endTag("", TAG_SWITCH);
                    }

                    serializer.endTag("", TAG_TASK);
                }
                while (taskCursor.moveToNext());
            }

            taskCursor.close();
            serializer.endTag("", Constants.APP_NAME);
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static boolean loadTask(DatabaseOper ap) {
        XmlPullParser parser = Xml.newPullParser();
        FileInputStream fis = null;

        try {
            // �ڱ���֮ǰ��Ҫ�ж� SDCard �Ƿ����,�����Ƿ���п�дȨ�ޣ�
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // ��ȡSDCardĿ¼,2.2��ʱ��Ϊ:/mnt/sdcart
                // 2.1��ʱ��Ϊ��/sdcard������ʹ�þ�̬�����õ�·�����һ�㡣
                File sdCardDir = Environment.getExternalStorageDirectory();
                File file = new File(sdCardDir.getPath() + File.separator + Constants.BACK_FILE_PATH + File.separator
                        + Constants.TASK.TABLE_TASK);

                if (file.exists()) {
                    fis = new FileInputStream(file);
                }
            }

            if (fis == null) {
                return false;
            }

            // auto-detect the encoding from the stream
            parser.setInput(fis, null);
            int eventType = parser.getEventType();
            boolean done = false;
            // taskId ÿ��ѭ����Task��ǩʱ�ᱻ���¸�ֵ
            int taskId = -1;

            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                String name = null;

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(TAG_TASK)) {
                            ContentValues values = new ContentValues(9);
                            values.put(Constants.TASK.COLUMN_START_HOUR,
                                    Integer.parseInt(parser.getAttributeValue("", Constants.TASK.COLUMN_START_HOUR)));
                            values.put(Constants.TASK.COLUMN_START_MINUTES,
                                    Integer.parseInt(parser.getAttributeValue("", Constants.TASK.COLUMN_START_MINUTES)));
                            values.put(Constants.TASK.COLUMN_END_HOUR,
                                    Integer.parseInt(parser.getAttributeValue("", Constants.TASK.COLUMN_END_HOUR)));
                            values.put(Constants.TASK.COLUMN_END_MINUTES,
                                    Integer.parseInt(parser.getAttributeValue("", Constants.TASK.COLUMN_END_MINUTES)));
                            values.put(Constants.TASK.COLUMN_START_TIME,
                                    Long.parseLong(parser.getAttributeValue("", Constants.TASK.COLUMN_START_TIME)));
                            values.put(Constants.TASK.COLUMN_END_TIME,
                                    Long.parseLong(parser.getAttributeValue("", Constants.TASK.COLUMN_END_TIME)));
                            values.put(Constants.TASK.COLUMN_DAYS_OF_WEEK,
                                    Integer.parseInt(parser.getAttributeValue("", Constants.TASK.COLUMN_DAYS_OF_WEEK)));
                            values.put(Constants.TASK.COLUMN_ENABLED,
                                    Integer.parseInt(parser.getAttributeValue("", Constants.TASK.COLUMN_ENABLED)));
                            values.put(Constants.TASK.COLUMN_MESSAGE,
                                    parser.getAttributeValue("", Constants.TASK.COLUMN_MESSAGE));
                            taskId = ap.insertTask(values);
                        } else if (name.equalsIgnoreCase(TAG_SWITCH)) {
                            ContentValues values = new ContentValues(4);
                            values.put(Constants.SWITCH.COLUMN_TASK_ID, taskId);
                            values.put(Constants.SWITCH.COLUMN_SWITCH_ID,
                                    Integer.parseInt(parser.getAttributeValue("", Constants.SWITCH.COLUMN_SWITCH_ID)));
                            values.put(Constants.SWITCH.COLUMN_PARAM1,
                                    parser.getAttributeValue("", Constants.SWITCH.COLUMN_PARAM1));
                            values.put(Constants.SWITCH.COLUMN_PARAM2,
                                    parser.getAttributeValue("", Constants.SWITCH.COLUMN_PARAM2));
                            ap.insertSwitch(values);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(Constants.APP_NAME)) {
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
