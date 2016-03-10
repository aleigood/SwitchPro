package alei.switchpro.volume;

import alei.switchpro.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;

public class VolumeActivity extends Activity implements OnCheckedChangeListener {
    private static String NOTIFICATIONS_USE_RING_VOLUME = "notifications_use_ring_volume";
    private CheckBox sameNotificationVolume;
    private TextView notificationVolumeTitle;
    private SeekBar notificationVolumeSeekbar;
    private SeekBar alarmVolumeSeekbar;
    private SeekBar mediaVolumeSeekbar;
    private SeekBar ringVolumeSeekbar;
    private SeekBar systemVolumeSeekbar;
    private SeekBar voiceVolumeSeekbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.volume));
        LayoutInflater inflater = getLayoutInflater();
        View dlgView = inflater.inflate(R.layout.view_pref_ringervolume, null, false);
        alert.setView(dlgView);
        sameNotificationVolume = (CheckBox) dlgView.findViewById(R.id.same_notification_volume);
        notificationVolumeTitle = (TextView) dlgView.findViewById(R.id.notification_volume_title);
        notificationVolumeSeekbar = (SeekBar) dlgView.findViewById(R.id.notification_volume_seekbar);
        alarmVolumeSeekbar = (SeekBar) dlgView.findViewById(R.id.alarm_volume_seekbar);
        mediaVolumeSeekbar = (SeekBar) dlgView.findViewById(R.id.media_volume_seekbar);
        ringVolumeSeekbar = (SeekBar) dlgView.findViewById(R.id.ring_volume_seekbar);
        systemVolumeSeekbar = (SeekBar) dlgView.findViewById(R.id.system_volume_seekbar);
        voiceVolumeSeekbar = (SeekBar) dlgView.findViewById(R.id.voice_volume_seekbar);
        boolean b = Settings.System.getInt(getContentResolver(), NOTIFICATIONS_USE_RING_VOLUME, 0) == 1 ? true : false;
        sameNotificationVolume.setChecked(b);
        setNotificationVolumeVisibility(!b);
        sameNotificationVolume.setOnCheckedChangeListener(this);
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringVolumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        ringVolumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_RING));
        mediaVolumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mediaVolumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        alarmVolumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        alarmVolumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
        systemVolumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        systemVolumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        voiceVolumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        voiceVolumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        notificationVolumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        notificationVolumeSeekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        alert.setPositiveButton(getString(R.string.button_apply), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, ringVolumeSeekbar.getProgress(), 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaVolumeSeekbar.getProgress(), 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolumeSeekbar.getProgress(), 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, systemVolumeSeekbar.getProgress(), 0);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, voiceVolumeSeekbar.getProgress(), 0);
                if (!sameNotificationVolume.isChecked()) {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                            notificationVolumeSeekbar.getProgress(), 0);
                }
                finish();
            }
        });
        alert.setNeutralButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        // һ��Ҫ��ȡ����ʱ��رձ�ACTIVITY��������޷���������,�ڵ����Back����ť�����
        alert.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                finish();
            }
        });
        alert.show();
    }

    // һ��Ҫ��pause��ʱ�������activity
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setNotificationVolumeVisibility(!isChecked);
        Settings.System.putInt(getContentResolver(), NOTIFICATIONS_USE_RING_VOLUME, isChecked ? 1 : 0);
        if (isChecked) {
            // The user wants the notification to be same as ring, so do a
            // one-time sync right now
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, ringVolumeSeekbar.getProgress(), 0);
        }
    }

    private void setNotificationVolumeVisibility(boolean visible) {
        notificationVolumeSeekbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        notificationVolumeTitle.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
