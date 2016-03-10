package alei.switchpro.task;

import alei.switchpro.DatabaseOper;
import alei.switchpro.MyApplication;
import alei.switchpro.SwitchUtils;
import alei.switchpro.WidgetProviderUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;

import java.util.List;

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert activity. Passes
 * through Alarm ID.
 */
public class TaskReceiver extends BroadcastReceiver {
    /**
     * If the alarm is older than STALE_WINDOW seconds, ignore. It is probably
     * the result of a time or timezone change
     */
    private final static int STALE_WINDOW = 60 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseOper dbOper = MyApplication.getInstance().getDataOper();
        Task alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(TaskUtil.ALARM_RAW_DATA);

        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Task.CREATOR.createFromParcel(in);
        }

        if (alarm == null) {
            return;
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();

        // �մ�������ʱ��͹����ˣ�����Ĭ�ϵ��ӳ�Ϊ30�룬������ǹ���˵��ʱ���ʱ��������
        List<Toggle> switches = TaskUtil.getSwitchesByTaskId(dbOper, alarm.id);

        for (int i = 0; i < switches.size(); i++) {
            Toggle toggle = (Toggle) switches.get(i);

            switch (toggle.switchId) {
                case Toggle.SWITCH_RINGTONE:

                    Uri defaultAlert = RingtoneManager.getActualDefaultRingtoneUri(context,
                            RingtoneManager.TYPE_RINGTONE);
                    Uri ringtone = null;

                    if (alarm.type == 0) {
                        if (alarm.startTime + STALE_WINDOW * 1000 < now) {
                            return;
                        }

                        if (toggle.param1 != null && toggle.param1.length() != 0) {
                            ringtone = Uri.parse(toggle.param1);
                        }

                        // ��ʱ���浱ǰ���������ã��Ա�ָ�
                        TaskUtil.updateSwitch(dbOper, toggle.taskId, toggle.switchId, toggle.param1,
                                defaultAlert.toString());
                    } else {
                        if (alarm.endTime + STALE_WINDOW * 1000 < now) {
                            return;
                        }

                        if (toggle.param2 != null && toggle.param2.length() != 0) {
                            ringtone = Uri.parse(toggle.param2);
                        }
                    }

                    if (ringtone == null) {
                        ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }

                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, ringtone);
                    break;
                case Toggle.SWITCH_RADIO:

                    boolean radioState = SwitchUtils.getNetworkState(context);
                    if (alarm.type == 0) {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǿ�����
                        if ((toggle.param1.equals("0") && radioState) || (toggle.param1.equals("1") && !radioState)) {
                            SwitchUtils.toggleNetwork(context);
                        }
                    } else {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǹرյĻ���Ҫ�����ģ����Ѿ�������
                        if ((toggle.param1.equals("0") && !radioState) || (toggle.param1.equals("1") && radioState)) {
                            SwitchUtils.toggleNetwork(context);
                        }
                    }

                    break;
                case Toggle.SWITCH_WIFI:

                    int wifiState = SwitchUtils.getWifiState(context);
                    if (alarm.type == 0) {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǿ�����
                        if ((toggle.param1.equals("0") && wifiState == WidgetProviderUtil.STATE_ENABLED)
                                || (toggle.param1.equals("1") && wifiState == WidgetProviderUtil.STATE_DISABLED)) {
                            SwitchUtils.toggleWifi(context);
                        }
                    } else {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǹرյ�
                        if ((toggle.param1.equals("0") && wifiState == WidgetProviderUtil.STATE_DISABLED)
                                || (toggle.param1.equals("1") && wifiState == WidgetProviderUtil.STATE_ENABLED)) {
                            SwitchUtils.toggleWifi(context);
                        }
                    }
                    break;
                case Toggle.SWITCH_DATA_CONN:

                    boolean dataState = SwitchUtils.getApnState(context);
                    if (alarm.type == 0) {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǿ�����
                        if ((toggle.param1.equals("0") && dataState) || (toggle.param1.equals("1") && !dataState)) {
                            SwitchUtils.toggleApn(context);
                        }
                    } else {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǹرյ�
                        if ((toggle.param1.equals("0") && !dataState) || (toggle.param1.equals("1") && dataState)) {
                            SwitchUtils.toggleApn(context);
                        }
                    }
                    break;
                case Toggle.SWITCH_SYNC:

                    boolean syncState = SwitchUtils.getSync(context);
                    if (alarm.type == 0) {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǿ�����
                        if ((toggle.param1.equals("0") && syncState) || (toggle.param1.equals("1") && !syncState)) {
                            SwitchUtils.toggleSync(context);
                        }
                    } else {
                        // ��Ҫ�رյģ��ҵ�ǰ�ǹرյ�
                        if ((toggle.param1.equals("0") && !syncState) || (toggle.param1.equals("1") && syncState)) {
                            SwitchUtils.toggleSync(context);
                        }
                    }
                    break;
                case Toggle.SWITCH_SILENT:

                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                    // ����ǿ�ʼ����
                    if (alarm.type == 0) {
                        // ����Ǿ������ҵ�ǰ������ģʽ
                        if ((toggle.param1.equals("0") && audioManager.getMode() == AudioManager.MODE_NORMAL)) {
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        } else if ((toggle.param1.equals("1") && audioManager.getMode() == AudioManager.MODE_NORMAL)) {
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        }
                    } else {
                        // ����ǽ��������ֱ�ӻָ�Normalģʽ
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }
                    break;
                case Toggle.SWITCH_VOLUME:

                    AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                    if (alarm.type == 0) {
                        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                                (int) ((Float.parseFloat(toggle.param1) / 100f) * (float) max),
                                AudioManager.FLAG_SHOW_UI);
                        // ���浱ǰ���������Ա�ָ�
                        TaskUtil.updateSwitch(dbOper, toggle.taskId, toggle.switchId, toggle.param1, currentVolume + "");
                    } else {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, Integer.parseInt(toggle.param2),
                                AudioManager.FLAG_SHOW_UI);
                    }

                    break;
                default:
                    break;
            }
        }

        // ��������ظ��������ҽ��������Ѿ���������������ʧЧ
        if (!alarm.daysOfWeek.isRepeatSet() && alarm.type == 1) {
            TaskUtil.enableAlarm(dbOper, alarm.id, false);
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            TaskUtil.setNextAlert(dbOper);
        }
    }

}
