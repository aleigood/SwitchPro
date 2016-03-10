package alei.switchpro.task.pref;

import alei.switchpro.R;
import alei.switchpro.task.TaskUtil;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

/**
 * The RingtonePreference does not have a way to get/set the current ringtone so
 * we override onSaveRingtone and onRestoreRingtone to get the same behavior.
 */
public class MyRingtonePreference extends RingtonePreference implements OnClickListener {
    private Uri mAlert;
    private boolean isChecked;

    public MyRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSaveRingtone(Uri ringtoneUri) {
        setAlert(ringtoneUri);
    }

    @Override
    protected Uri onRestoreRingtone() {
        return mAlert;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.pref_check);

        if (checkBox != null) {
            checkBox.setChecked(isChecked);
            checkBox.setOnClickListener(this);
        }
    }

    public void setAlert(Uri alert) {
        mAlert = alert;

        if (alert != null) {
            final Ringtone r = RingtoneManager.getRingtone(getContext(), alert);

            if (r != null) {
                setSummary(r.getTitle(getContext()));
            }
        } else {
            Uri defaultUri = RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE);

            if (defaultUri != null) {
                Ringtone ringstone = RingtoneManager.getRingtone(getContext(), defaultUri);

                if (ringstone != null) {
                    setSummary(ringstone.getTitle(getContext()));
                }
            }
        }
    }

    public String getAlertString() {
        if (mAlert != null) {
            return mAlert.toString();
        }

        return TaskUtil.ALARM_ALERT_SILENT;
    }

    public String getDefaultAlertString() {
        Uri defaultAlert = RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE);

        if (defaultAlert != null) {
            return defaultAlert.toString();
        }

        return TaskUtil.ALARM_ALERT_SILENT;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean b) {
        isChecked = b;
    }

    public void onClick(View v) {
        isChecked = ((CheckBox) v).isChecked();
    }
}
