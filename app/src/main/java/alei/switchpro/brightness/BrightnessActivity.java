package alei.switchpro.brightness;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import alei.switchpro.Constants;
import alei.switchpro.Utils;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.WindowManager.LayoutParams;

public class BrightnessActivity extends Activity
{
    // 亮度,界面上能调整的最低亮度为30
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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // 获取亮度级别配置
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        List<Integer> lastLevel = LevelPreference.getLevelRealList(config.getString(Constants.PREFS_BRIGHT_LEVEL,
                LevelPreference.DEFAULT_LEVEL));

        int brightness = Settings.System.getInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, BRIGHT_LEVEL_10);
        int mode = Settings.System.getInt(getContentResolver(), BRIGHT_MODE, 1);
        int curLevel = 0;

        if (mode == BRIGHT_MODE_AUTO)
        {
            curLevel = -1;
        }
        else
        {
            if (brightness == BRIGHT_LEVEL_0)
            {
                curLevel = 0;
            }
            if (brightness > BRIGHT_LEVEL_0 && brightness <= BRIGHT_LEVEL_10)
            {
                curLevel = 10;
            }
            else if (brightness > BRIGHT_LEVEL_10 && brightness <= BRIGHT_LEVEL_20)
            {
                curLevel = 20;
            }
            else if (brightness > BRIGHT_LEVEL_20 && brightness <= BRIGHT_LEVEL_30)
            {
                curLevel = 30;
            }
            else if (brightness > BRIGHT_LEVEL_30 && brightness <= BRIGHT_LEVEL_40)
            {
                curLevel = 40;
            }
            else if (brightness > BRIGHT_LEVEL_40 && brightness <= BRIGHT_LEVEL_50)
            {
                curLevel = 50;
            }
            else if (brightness > BRIGHT_LEVEL_50 && brightness <= BRIGHT_LEVEL_60)
            {
                curLevel = 60;
            }
            else if (brightness > BRIGHT_LEVEL_60 && brightness <= BRIGHT_LEVEL_70)
            {
                curLevel = 70;
            }
            else if (brightness > BRIGHT_LEVEL_70 && brightness <= BRIGHT_LEVEL_80)
            {
                curLevel = 80;
            }
            else if (brightness > BRIGHT_LEVEL_80 && brightness <= BRIGHT_LEVEL_90)
            {
                curLevel = 90;
            }
            else if (brightness > BRIGHT_LEVEL_90 && brightness <= BRIGHT_LEVEL_100)
            {
                curLevel = 100;
            }
        }

        int nextLevel = 0;

        if (lastLevel.contains(curLevel))
        {
            int index = lastLevel.indexOf(curLevel);

            if (index + 1 <= lastLevel.size() - 1)
            {
                nextLevel = lastLevel.get(index + 1);
            }
            else
            {
                nextLevel = lastLevel.get(0);
            }
        }
        else
        {
            nextLevel = lastLevel.get(0);
        }

        // 如果值为-1，要设置成自动调整
        if (nextLevel == -1)
        {
            Settings.System.putInt(getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_AUTO);
        }
        else
        {
            // 如果当前是自动调整的应先关闭
            if (mode == BRIGHT_MODE_AUTO)
            {
                Settings.System.putInt(getContentResolver(), BRIGHT_MODE, BRIGHT_MODE_MANUAL);
            }

            int setVal = BRIGHT_LEVEL_0;

            if (nextLevel == 10)
            {
                setVal = BRIGHT_LEVEL_10;
            }
            else if (nextLevel == 20)
            {
                setVal = BRIGHT_LEVEL_20;
            }
            else if (nextLevel == 30)
            {
                setVal = BRIGHT_LEVEL_30;
            }
            else if (nextLevel == 40)
            {
                setVal = BRIGHT_LEVEL_40;
            }
            else if (nextLevel == 50)
            {
                setVal = BRIGHT_LEVEL_50;
            }
            else if (nextLevel == 60)
            {
                setVal = BRIGHT_LEVEL_60;
            }
            else if (nextLevel == 70)
            {
                setVal = BRIGHT_LEVEL_70;
            }
            else if (nextLevel == 80)
            {
                setVal = BRIGHT_LEVEL_80;
            }
            else if (nextLevel == 90)
            {
                setVal = BRIGHT_LEVEL_90;
            }
            else if (nextLevel == 100)
            {
                setVal = BRIGHT_LEVEL_100;
            }

            // 设置系统的亮度(0-255)
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, setVal);

            // 设置Layout的亮度
            LayoutParams attributes = getWindow().getAttributes();
            float tmp = Float.valueOf(nextLevel) / 100f;
            // 这里的最小值不能为0，否则会出现锁屏
            // 在galaxy nexus中0.04f与系统最低亮度差不多，不会出现明显的亮度跳动的现象
            attributes.screenBrightness = tmp == 0 ? 0.04f : tmp;
            getWindow().setAttributes(attributes);
        }

        // 3. 需要设置延迟关闭窗体,因为条件亮度有延迟
        Timer timer = new Timer();
        timer.schedule(new NewTask(), 500);

        // 通知widget按钮进行更新
        Utils.updateWidget(this);
    }

    class NewTask extends TimerTask
    {
        @Override
        public void run()
        {
            this.cancel();
            finish();
        }
    }
}
