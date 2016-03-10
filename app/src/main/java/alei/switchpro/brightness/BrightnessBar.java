package alei.switchpro.brightness;

import alei.switchpro.R;
import alei.switchpro.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.util.Timer;
import java.util.TimerTask;

public class BrightnessBar extends Activity {
    public static final int BRIGHT_MODE_MANUAL = 0;
    public static final int BRIGHT_MODE_AUTO = 1;
    public static final String BRIGHT_MODE = "screen_brightness_mode";
    private Timer mTimer;
    private NewTask mTimerTask;
    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View brightnessView = getLayoutInflater().inflate(R.layout.view_setting_brightness, null);
        final CheckBox toggleAuto = (CheckBox) brightnessView.findViewById(R.id.toggleAuto);
        final SeekBar seekBar = (SeekBar) brightnessView.findViewById(R.id.seekBar);
        seekBar.setMax(255);

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopTimer();

                if (toggleAuto.isChecked()) {
                    toggleAuto.setChecked(false);
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setBrightness(progress);
            }
        });

        toggleAuto.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stopTimer();
                setBrightness(isChecked ? -1 : seekBar.getProgress());
                startTimer();
            }
        });

        int val = getBrightness();

        if (val == -1) {
            toggleAuto.setChecked(true);
        } else {
            toggleAuto.setChecked(false);
            seekBar.setProgress(val);
        }

        dialog = new AlertDialog.Builder(BrightnessBar.this).setView(brightnessView).create();
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Utils.updateWidget(BrightnessBar.this);
                finish();
            }
        });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTimer();
    }

    public int getBrightness() {
        int brightness = Settings.System.getInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, 255);
        int mode = Settings.System.getInt(getContentResolver(), BRIGHT_MODE, 1);

        if (mode == BRIGHT_MODE_AUTO) {
            return -1;
        } else {
            return brightness;
        }
    }

    public void setBrightness(int val) {
        if (val == -1) {
            Settings.System.putInt(getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_AUTO);
        } else {
            Settings.System.putInt(getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_MANUAL);
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, val);

            LayoutParams attributes = getWindow().getAttributes();
            float tmp = Float.valueOf(val == 0 ? 1 : val) / 255f;
            attributes.screenBrightness = tmp;
            getWindow().setAttributes(attributes);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new NewTask();
        }

        mTimer.schedule(new NewTask(), 3000);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    class NewTask extends TimerTask {
        @Override
        public void run() {
            this.cancel();

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
