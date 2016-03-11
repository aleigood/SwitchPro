package alei.switchpro.task;

import alei.switchpro.DatabaseOper;
import alei.switchpro.MyApplication;
import alei.switchpro.R;
import alei.switchpro.task.pref.BaseTogglePreference;
import alei.switchpro.task.pref.MyRingtonePreference;
import alei.switchpro.task.pref.VolumePreference;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.List;

/**
 * Manages each alarm
 */
public class TaskModifyActivity extends PreferenceActivity {
    public DatabaseOper dbOper;
    public Task alarm;
    public List<Toggle> toggles;
    private EditTextPreference mLabel;
    private Preference mTimePref;
    private Preference mTimePref2;
    private RepeatPreference mRepeatPref;
    private MenuItem mDeleteAlarmItem;
    private MyRingtonePreference ringtonePref;
    private BaseTogglePreference radioPref;
    private BaseTogglePreference dataPref;
    private BaseTogglePreference wifiPref;
    private BaseTogglePreference syncPref;
    private BaseTogglePreference silentPref;
    private VolumePreference volumePref;
    private AudioManager mAudioManager;
    private int currentVolume;
    private int maxVolume;
    private int mId;
    private boolean mEnabled;
    private int mHour;
    private int mMinutes;
    private int mHour2;
    private int mMinutes2;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

        dbOper = MyApplication.getInstance().getDataOper();
        setContentView(R.layout.activity_task_conf);
        addPreferencesFromResource(R.xml.pref_task_conf);

        // Get each preference so we can retrieve the value later.
        mLabel = (EditTextPreference) findPreference("label");
        mLabel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference p, Object newValue) {
                // Set the summary based on the new label.
                p.setSummary((String) newValue);
                return true;
            }
        });
        mTimePref = findPreference("time");
        mTimePref2 = findPreference("time2");
        mRepeatPref = (RepeatPreference) findPreference("setRepeat");
        mId = getIntent().getIntExtra(TaskUtil.ALARM_ID, -1);

        /* load alarm details from database */
        alarm = TaskUtil.getAlarmById(dbOper, mId);
        toggles = TaskUtil.getSwitchesByTaskId(dbOper, alarm.id);
        mEnabled = alarm.enabled;
        mLabel.setText(alarm.message);
        mLabel.setSummary(alarm.message);
        mHour = alarm.startHour;
        mMinutes = alarm.startMinutes;
        mHour2 = alarm.endHour;
        mMinutes2 = alarm.endMinutes;
        mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
        updateTime();
        initToggleState();

        // Attach actions to each button.
        Button b = (Button) findViewById(R.id.button_apply);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // 先校验，是否开始时间小于结束时间
                if (mHour > mHour2 || (mHour == mHour2 && mMinutes >= mMinutes2)) {
                    Toast.makeText(TaskModifyActivity.this, "Start time must be less than the End time.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // 先删除全部的开关
                TaskUtil.deleteSwitch(dbOper, mId);

                // 如果选中了就新增开关,当前的铃声在触发任务时设置
                if (ringtonePref.isChecked()) {
                    TaskUtil.addSwitch(dbOper, mId, Toggle.SWITCH_RINGTONE, ringtonePref.getAlertString(), "");
                }

                if (radioPref.isChecked()) {
                    TaskUtil.addSwitch(dbOper, mId, Toggle.SWITCH_RADIO, radioPref.getValue(), "");
                }

                if (dataPref.isChecked()) {
                    TaskUtil.addSwitch(dbOper, mId, Toggle.SWITCH_DATA_CONN, dataPref.getValue(), "");
                }

                if (wifiPref.isChecked()) {
                    TaskUtil.addSwitch(dbOper, mId, Toggle.SWITCH_WIFI, wifiPref.getValue(), "");
                }

                if (syncPref.isChecked()) {
                    TaskUtil.addSwitch(dbOper, mId, Toggle.SWITCH_SYNC, syncPref.getValue(), "");
                }

                if (silentPref.isChecked()) {
                    TaskUtil.addSwitch(dbOper, mId, Toggle.SWITCH_SILENT, silentPref.getValue(), "");
                }

                // 存音量，第一个参数只存百分比，第二个参数存之前的值,这个值在触发任务是再保存
                if (volumePref.isChecked()) {
                    TaskUtil.addSwitch(dbOper, mId, Toggle.SWITCH_VOLUME, volumePref.getPercent() + "", "");
                }

                TaskUtil.setAlarm(dbOper, mId, mHour, mMinutes, mHour2, mMinutes2, mRepeatPref.getDaysOfWeek(),
                        mEnabled, mLabel.getText());
                finish();
            }
        });
        b = (Button) findViewById(R.id.button_cancel);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initToggleState() {
        ringtonePref = (MyRingtonePreference) findPreference("ringtone");
        ringtonePref.setAlert(null);

        String trunOn = getResources().getString(R.string.turn_on);
        String trunOff = getResources().getString(R.string.turn_off);
        radioPref = (BaseTogglePreference) findPreference("radio");
        radioPref.setEntries(new String[]{trunOff, trunOn});
        radioPref.setEntryValues(new String[]{"0", "1"});
        radioPref.setValue("0");
        radioPref.setSummary(trunOff);
        radioPref.initData();

        dataPref = (BaseTogglePreference) findPreference("data");
        dataPref.setEntries(new String[]{trunOff, trunOn});
        dataPref.setEntryValues(new String[]{"0", "1"});
        dataPref.setValue("0");
        dataPref.setSummary(trunOff);
        dataPref.initData();

        wifiPref = (BaseTogglePreference) findPreference("wifi");
        wifiPref.setEntries(new String[]{trunOff, trunOn});
        wifiPref.setEntryValues(new String[]{"0", "1"});
        wifiPref.setValue("0");
        wifiPref.setSummary(trunOff);
        wifiPref.initData();

        syncPref = (BaseTogglePreference) findPreference("sync");
        syncPref.setEntries(new String[]{trunOff, trunOn});
        syncPref.setEntryValues(new String[]{"0", "1"});
        syncPref.setValue("0");
        syncPref.setSummary(trunOff);
        syncPref.initData();

        silentPref = (BaseTogglePreference) findPreference("silent");
        silentPref.setEntries(new String[]{getResources().getString(R.string.silent),
                getResources().getString(R.string.vibrate)});
        silentPref.setEntryValues(new String[]{"0", "1"});
        silentPref.setValue("0");
        silentPref.setSummary(getResources().getString(R.string.silent));
        silentPref.initData();

        // 初始化成当前的百分比
        int initPercent = (int) (((float) currentVolume / (float) maxVolume) * 100);
        volumePref = (VolumePreference) findPreference("volume");
        volumePref.setPercent(initPercent);
        volumePref.setSummary(initPercent + " %");
        volumePref.initData(this);

        for (int i = 0; i < toggles.size(); i++) {
            Toggle tmp = (Toggle) toggles.get(i);

            if (tmp.switchId == Toggle.SWITCH_RINGTONE) {
                ringtonePref.setChecked(true);

                if (tmp.param1 != null && tmp.param1.length() != 0) {
                    ringtonePref.setAlert(Uri.parse(tmp.param1));
                } else {
                    ringtonePref.setAlert(null);
                }
            }

            if (tmp.switchId == Toggle.SWITCH_RADIO) {
                radioPref.setChecked(true);

                if (tmp.param1.equals("1")) {
                    radioPref.setValue("1");
                    radioPref.setSummary(trunOn);
                }
            }

            if (tmp.switchId == Toggle.SWITCH_DATA_CONN) {
                dataPref.setChecked(true);

                if (tmp.param1.equals("1")) {
                    dataPref.setValue("1");
                    dataPref.setSummary(trunOn);
                }
            }

            if (tmp.switchId == Toggle.SWITCH_WIFI) {
                wifiPref.setChecked(true);

                if (tmp.param1.equals("1")) {
                    wifiPref.setValue("1");
                    wifiPref.setSummary(trunOn);
                }
            }

            if (tmp.switchId == Toggle.SWITCH_SYNC) {
                syncPref.setChecked(true);

                if (tmp.param1.equals("1")) {
                    syncPref.setValue("1");
                    syncPref.setSummary(trunOn);
                }
            }

            if (tmp.switchId == Toggle.SWITCH_SILENT) {
                silentPref.setChecked(true);

                if (tmp.param1.equals("1")) {
                    silentPref.setValue("1");
                    silentPref.setSummary(getResources().getString(R.string.vibrate));
                }
            }

            if (tmp.switchId == Toggle.SWITCH_VOLUME) {
                // 初始化成实际的百分比
                int actual = Integer.parseInt(tmp.param1);
                volumePref.setChecked(true);
                volumePref.setSummary(actual + " %");
                volumePref.setPercent(actual);
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTimePref) {
            new TimePickerDialog(this, new OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour = hourOfDay;
                    mMinutes = minute;
                    onTimeSet2(view, hourOfDay, minute);
                }
            }, mHour, mMinutes, DateFormat.is24HourFormat(this)).show();
        }

        if (preference == mTimePref2) {
            new TimePickerDialog(this, new OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHour2 = hourOfDay;
                    mMinutes2 = minute;
                    onTimeSet2(view, hourOfDay, minute);
                }
            }, mHour2, mMinutes2, DateFormat.is24HourFormat(this)).show();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onTimeSet2(TimePicker view, int hourOfDay, int minute) {
        updateTime();
        // If the time has been changed, enable the alarm.
        mEnabled = true;
    }

    private void updateTime() {
        mTimePref.setSummary(TaskUtil.formatTime(this, mHour, mMinutes, mRepeatPref.getDaysOfWeek()));
        mTimePref2.setSummary(TaskUtil.formatTime(this, mHour2, mMinutes2, mRepeatPref.getDaysOfWeek()));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        mDeleteAlarmItem = menu.add(0, 0, 0, R.string.delete_alarm);
        mDeleteAlarmItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mDeleteAlarmItem) {
            TaskUtil.deleteAlarm(dbOper, mId);
            finish();
            return true;
        }

        return false;
    }
}
