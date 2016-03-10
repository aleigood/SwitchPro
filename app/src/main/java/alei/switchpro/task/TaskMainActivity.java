package alei.switchpro.task;

import alei.switchpro.DatabaseOper;
import alei.switchpro.MyApplication;
import alei.switchpro.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class TaskMainActivity extends PreferenceActivity implements OnItemClickListener {
    // This string is used to identify the alarm id passed to SetAlarm from the
    // list of alarms.
    public static final String ALARM_ID = "alarm_id";
    final static int MAX_ALARM_COUNT = 12;
    private Preference newTaskPref;
    private LayoutInflater mFactory;
    private ListView mAlarmsList;
    private Cursor mCursor;
    private String mAm, mPm;
    private DatabaseOper dbOper;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_task_main);
        addPreferencesFromResource(R.xml.pref_new_task);
        dbOper = MyApplication.getInstance().getDataOper();
        newTaskPref = findPreference("new_task");

        String[] ampm = new DateFormatSymbols().getAmPmStrings();
        mAm = ampm[0];
        mPm = ampm[1];

        mFactory = LayoutInflater.from(this);
        mCursor = dbOper.queryAllTask();
        updateLayout();
    }

    private void updateLayout() {
        TimerAdapter timerAdapter = new TimerAdapter(this, mCursor);
        mAlarmsList = (ListView) findViewById(R.id.alarms_list);
        mAlarmsList.setAdapter(timerAdapter);
        mAlarmsList.setVerticalScrollBarEnabled(true);
        mAlarmsList.setOnItemClickListener(this);
        mAlarmsList.setOnCreateContextMenuListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == newTaskPref) {
            int newId = TaskUtil.addAlarm(dbOper);
            Intent intent = new Intent(this, TaskModifyActivity.class);
            intent.putExtra(ALARM_ID, newId);
            startActivity(intent);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout();
        inflateClock();
    }

    /**
     * ��ӵ����˵�
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate our menu.
        getMenuInflater().inflate(R.menu.task_pop_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * ��ʼ���˵���״̬
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_add).setVisible(mAlarmsList.getAdapter().getCount() < MAX_ALARM_COUNT);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * �����˵�����Ӧ�¼�
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            int newId = TaskUtil.addAlarm(dbOper);
            Intent intent = new Intent(this, TaskModifyActivity.class);
            intent.putExtra(ALARM_ID, newId);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * ��ʼ������ĳһ��ʱ�ĵ����˵�
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.task_context_menu, menu);
    }

    /**
     * ��ʼ������ĳһ��ʱ���¼�
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;

        if (item.getItemId() == R.id.delete_alarm) {
            // Confirm that the alarm will be deleted.
            new AlertDialog.Builder(this).setTitle(getString(R.string.delete_alarm))
                    .setMessage(getString(R.string.delete_alarm_confirm))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            TaskUtil.deleteAlarm(dbOper, id);
                            requery();
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    /**
     * ���ĳһ�˵���ʱ�����޸Ľ���
     */
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        Intent intent = new Intent(this, TaskModifyActivity.class);
        intent.putExtra(TaskUtil.ALARM_ID, (int) id);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCursor.deactivate();
        mCursor.close();
    }

    ;

    @Override
    protected void onResume() {
        super.onResume();
        inflateClock();
        // ���޸�,����Ǵ����ý��淵�غ���ô˷���ˢ���б�
        requery();
    }

    protected void inflateClock() {
        TextView am = (TextView) findViewById(R.id.am);
        TextView pm = (TextView) findViewById(R.id.pm);
        TextView am2 = (TextView) findViewById(R.id.am2);
        TextView pm2 = (TextView) findViewById(R.id.pm2);

        if (am != null) {
            am.setText(mAm);
            am2.setText(mAm);
        }

        if (pm != null) {
            pm.setText(mPm);
            pm2.setText(mPm);
        }
    }

    /**
     * ��ɾ��,����,�޸Ļ������½���˽���ʱˢ���б�����ʱ����
     */
    public void requery() {
        mCursor.requery();
    }

    private class TimerAdapter extends CursorAdapter {
        public TimerAdapter(Context context, Cursor cursor) {
            super(context, cursor, false);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View ret = mFactory.inflate(R.layout.item_task, parent, false);

            ((TextView) ret.findViewById(R.id.am)).setText(mAm);
            ((TextView) ret.findViewById(R.id.pm)).setText(mPm);
            // ???????????
            ((TextView) ret.findViewById(R.id.am2)).setText(mAm);
            ((TextView) ret.findViewById(R.id.pm2)).setText(mPm);

            DigitalClock digitalClock = (DigitalClock) ret.findViewById(R.id.digitalClock);
            digitalClock.setContext(context);
            digitalClock.setLive(false);
            return ret;
        }

        public void bindView(View view, Context context, Cursor cursor) {
            final Task alarm = new Task(cursor);

            CheckBox onButton = (CheckBox) view.findViewById(R.id.alarmButton);
            onButton.setChecked(alarm.enabled);
            onButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox) v).isChecked();
                    TaskUtil.enableAlarm(dbOper, alarm.id, isChecked);
                }
            });

            DigitalClock digitalClock = (DigitalClock) view.findViewById(R.id.digitalClock);

            // set the alarm text
            final Calendar c1 = Calendar.getInstance();
            c1.set(Calendar.HOUR_OF_DAY, alarm.startHour);
            c1.set(Calendar.MINUTE, alarm.startMinutes);
            final Calendar c2 = Calendar.getInstance();
            c2.set(Calendar.HOUR_OF_DAY, alarm.endHour);
            c2.set(Calendar.MINUTE, alarm.endMinutes);
            digitalClock.updateTime(c1, c2);

            // Set the repeat text or leave it blank if it does not repeat.
            TextView daysOfWeekView = (TextView) digitalClock.findViewById(R.id.daysOfWeek);
            final String daysOfWeekStr = alarm.daysOfWeek.toString(TaskMainActivity.this, false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
                daysOfWeekView.setText(daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }

            // Display the label
            TextView labelView = (TextView) digitalClock.findViewById(R.id.label);
            if (alarm.message != null && alarm.message.length() != 0) {
                labelView.setText(alarm.message);
                labelView.setVisibility(View.VISIBLE);
            } else {
                labelView.setVisibility(View.GONE);
            }
        }
    }

}
