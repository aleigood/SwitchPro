package alei.switchpro.brightness;

import alei.switchpro.Constants;
import alei.switchpro.Utils;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.WindowManager.LayoutParams;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BrightnessActivity extends Activity {
    // ����,�������ܵ������������Ϊ30
    public static final int BRIGHT_LEVEL_0 = 0;
    public static final int BRIGHT_LEVEL_10 = 25;
    public static final int BRIGHT_LEVEL_20 = 51;
    public static final int BRIGHT_LEVEL_30 = 77;
    public static final int BRIGHT_LEVEL_40 = 102;
    public static final int BRIGHT_LEVEL_50 = 128;
    public static final int BRIGHT_LEVEL_60 = 153;
    public static final int BRIGHT_LEVEL_70 = 179;
    public static final int BRIGHT_LEVEL_80 = 204;
    public static final int BRIGHT_LEVEL_90 = 230;
    public static final int BRIGHT_LEVEL_100 = 255;

    public static final int BRIGHT_MODE_MANUAL = 0;
    public static final int BRIGHT_MODE_AUTO = 1;
    public static final String BRIGHT_MODE = "screen_brightness_mode";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ��ȡ���ȼ�������
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        List<Integer> lastLevel = LevelPreference.getLevelRealList(config.getString(Constants.PREFS_BRIGHT_LEVEL,
                LevelPreference.DEFAULT_LEVEL));

        int brightness = Settings.System.getInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, BRIGHT_LEVEL_10);
        int mode = Settings.System.getInt(getContentResolver(), BRIGHT_MODE, 1);
        int curLevel = 0;

        if (mode == BRIGHT_MODE_AUTO) {
            curLevel = -1;
        } else {
            if (brightness == BRIGHT_LEVEL_0) {
                curLevel = 0;
            }
            if (brightness > BRIGHT_LEVEL_0 && brightness <= BRIGHT_LEVEL_10) {
                curLevel = 10;
            } else if (brightness > BRIGHT_LEVEL_10 && brightness <= BRIGHT_LEVEL_20) {
                curLevel = 20;
            } else if (brightness > BRIGHT_LEVEL_20 && brightness <= BRIGHT_LEVEL_30) {
                curLevel = 30;
            } else if (brightness > BRIGHT_LEVEL_30 && brightness <= BRIGHT_LEVEL_40) {
                curLevel = 40;
            } else if (brightness > BRIGHT_LEVEL_40 && brightness <= BRIGHT_LEVEL_50) {
                curLevel = 50;
            } else if (brightness > BRIGHT_LEVEL_50 && brightness <= BRIGHT_LEVEL_60) {
                curLevel = 60;
            } else if (brightness > BRIGHT_LEVEL_60 && brightness <= BRIGHT_LEVEL_70) {
                curLevel = 70;
            } else if (brightness > BRIGHT_LEVEL_70 && brightness <= BRIGHT_LEVEL_80) {
                curLevel = 80;
            } else if (brightness > BRIGHT_LEVEL_80 && brightness <= BRIGHT_LEVEL_90) {
                curLevel = 90;
            } else if (brightness > BRIGHT_LEVEL_90 && brightness <= BRIGHT_LEVEL_100) {
                curLevel = 100;
            }
        }

        int nextLevel = 0;

        if (lastLevel.contains(curLevel)) {
            int index = lastLevel.indexOf(curLevel);

            if (index + 1 <= lastLevel.size() - 1) {
                nextLevel = lastLevel.get(index + 1);
            } else {
                nextLevel = lastLevel.get(0);
            }
        } else {
            nextLevel = lastLevel.get(0);
        }

        // ���ֵΪ-1��Ҫ���ó��Զ�����
        if (nextLevel == -1) {
            Settings.System.putInt(getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_AUTO);
        } else {
            // �����ǰ���Զ�������Ӧ�ȹر�
            if (mode == BRIGHT_MODE_AUTO) {
                Settings.System.putInt(getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_MANUAL);
            }

            int setVal = BRIGHT_LEVEL_0;

            if (nextLevel == 10) {
                setVal = BRIGHT_LEVEL_10;
            } else if (nextLevel == 20) {
                setVal = BRIGHT_LEVEL_20;
            } else if (nextLevel == 30) {
                setVal = BRIGHT_LEVEL_30;
            } else if (nextLevel == 40) {
                setVal = BRIGHT_LEVEL_40;
            } else if (nextLevel == 50) {
                setVal = BRIGHT_LEVEL_50;
            } else if (nextLevel == 60) {
                setVal = BRIGHT_LEVEL_60;
            } else if (nextLevel == 70) {
                setVal = BRIGHT_LEVEL_70;
            } else if (nextLevel == 80) {
                setVal = BRIGHT_LEVEL_80;
            } else if (nextLevel == 90) {
                setVal = BRIGHT_LEVEL_90;
            } else if (nextLevel == 100) {
                setVal = BRIGHT_LEVEL_100;
            }

            // ����ϵͳ������(0-255)
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, setVal);

            // ����Layout������
            LayoutParams attributes = getWindow().getAttributes();
            float tmp = Float.valueOf(nextLevel) / 100f;
            // �������Сֵ����Ϊ0��������������
            // ��galaxy nexus��0.04f��ϵͳ������Ȳ�࣬����������Ե���������������
            attributes.screenBrightness = tmp == 0 ? 0.04f : tmp;
            getWindow().setAttributes(attributes);
        }

        // 3. ��Ҫ�����ӳٹرմ���,��Ϊ�����������ӳ�
        Timer timer = new Timer();
        timer.schedule(new NewTask(), 500);

        // ֪ͨwidget��ť���и���
        Utils.updateWidget(this);
    }

    class NewTask extends TimerTask {
        @Override
        public void run() {
            this.cancel();
            finish();
        }
    }
}
