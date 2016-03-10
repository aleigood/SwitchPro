package alei.switchpro;

import alei.switchpro.apn.ApnUtils;
import alei.switchpro.brightness.LevelPreference;
import alei.switchpro.net.NetUtils;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ToggleConfigActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    private CheckBoxPreference aireplaneWifi;
    private CheckBoxPreference toggleFlash;
    private CheckBoxPreference useApn;
    private CheckBoxPreference aireplaneRadio;
    private LevelPreference brightLevel;
    private ListPreference silentBtn;
    private ListPreference deviceType;

    private static String[] silentSummary;
    private static String[] silentValues = new String[] { "0", "1", "2" };

    private static String[] deviceSummary;
    private static String[] deviceValues = new String[] { Constants.DEVICE_TYPE1, Constants.DEVICE_TYPE2,
            Constants.DEVICE_TYPE3, Constants.DEVICE_TYPE4, Constants.DEVICE_TYPE5, Constants.DEVICE_TYPE6,
            Constants.DEVICE_TYPE7 };

    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        deviceSummary = new String[] { getResources().getString(R.string.type1),
                getResources().getString(R.string.type2), getResources().getString(R.string.type3),
                getResources().getString(R.string.type4), getResources().getString(R.string.type5),
                getResources().getString(R.string.type6), getResources().getString(R.string.type7) };
        silentSummary = new String[] { getResources().getString(R.string.vsmode),
                getResources().getString(R.string.vibrate), getResources().getString(R.string.silent) };
        addPreferencesFromResource(R.xml.pref_toggle_conf);
        initUI();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initUI();
    }

    /**
     * 因为在onCreate后会调用onResume所以直接在onResume中初始化
     */
    private void initUI()
    {
        aireplaneWifi = (CheckBoxPreference) findPreference(Constants.PREFS_AIRPLANE_WIFI);
        aireplaneRadio = (CheckBoxPreference) findPreference(Constants.PREFS_AIRPLANE_RADIO);
        toggleFlash = (CheckBoxPreference) findPreference(Constants.PREFS_TOGGLE_FLASH);
        useApn = (CheckBoxPreference) findPreference(Constants.PREFS_USE_APN);
        brightLevel = (LevelPreference) findPreference(Constants.PREFS_BRIGHT_LEVEL);

        silentBtn = (ListPreference) findPreference(Constants.PREFS_SILENT_BTN);
        silentBtn.setEntries(silentSummary);
        silentBtn.setEntryValues(silentValues);

        deviceType = (ListPreference) findPreference(Constants.PREFS_DEVICE_TYPE);
        deviceType.setEntries(deviceSummary);
        deviceType.setEntryValues(deviceValues);

        // 如果有已经设置的参数要根据已设置的初始化
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        String silentValue = config.getString(Constants.PREFS_SILENT_BTN, Constants.BTN_VS);
        silentBtn.setValue(silentValue);
        silentBtn.setSummary(getSilentSummary(silentValue));

        // 如果radio选中了，则wifi就不可用
        aireplaneWifi.setEnabled(!config.getBoolean(Constants.PREFS_AIRPLANE_RADIO, false));

        // 设置设备类别
        deviceType.setEnabled(config.getBoolean(Constants.PREFS_TOGGLE_FLASH, true));
        String deviceValue = config.getString(Constants.PREFS_DEVICE_TYPE, Constants.DEVICE_TYPE1);
        deviceType.setValue(deviceValue);
        deviceType.setSummary(getDeviceSummary(deviceValue));

        // 显示亮度条件进度条时，级别设置不可用
        brightLevel.setActivity(this);

        // 注册选项变化监听器
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * 当变换选项时要动态更新其他控件中可选的条目
     * 
     * @param btnName
     * @return
     */
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        if (key.equals(Constants.PREFS_AIRPLANE_RADIO))
        {
            // 如果选中了只关闭移动网络
            if (aireplaneRadio.isChecked())
            {
                aireplaneWifi.setEnabled(false);
            }
            else
            {
                aireplaneWifi.setEnabled(true);
            }

            // 如果当前的按钮是开着的
            if (SwitchUtils.getNetworkState(this))
            {
                // 如果飞行模式已经打开
                if (SwitchUtils.getAirplaneState(this))
                {
                    // 先关闭飞行模式
                    SwitchUtils.setAirplaneState(this, false);
                }
                else
                {
                    // 先打开手机网络
                    NetUtils.setSignalState(this, true);
                }
            }
        }
        else if (key.equals(Constants.PREFS_TOGGLE_FLASH))
        {
            if (SwitchUtils.getFlashlight(this))
            {
                SwitchUtils.setCameraFlashState(this, false);
                Utils.updateWidget(this);
            }

            deviceType.setEnabled(toggleFlash.isChecked());
        }
        else if (key.equals(Constants.PREFS_TOGGLE_TIMEOUT))
        {
            if (SwitchUtils.getScreenTimeoutState(this))
            {
                SwitchUtils.toggleScreenTimeout(this);
                Utils.updateWidget(this);
            }
        }
        else if (key.equals(Constants.PREFS_USE_APN))
        {
            // 如果当前选择了使用APN
            if (useApn.isChecked())
            {
                // 如果当前的数据连接是没打开的
                if (!NetUtils.getMobileNetworkState(this))
                {
                    // 先打开数据连接，在关闭APN
                    NetUtils.setMobileNetworkState(this, true);
                    ApnUtils.setApnState(this, false);
                }
                // 如果是打开的，直接也打开APN
                else
                {
                    ApnUtils.setApnState(this, true);
                }

                useApn.setSummary(R.string.use_apn_summary);
            }
            // 如果用的是数据连接
            else
            {
                // 如果当前APN没打开
                if (!ApnUtils.getApnState(this))
                {
                    // 先打开APN，在关闭数据连接
                    ApnUtils.setApnState(this, true);
                    NetUtils.setMobileNetworkState(this, false);
                }
                else
                {
                    // 如果APN已经打开了，就直接打开数据连接
                    NetUtils.setMobileNetworkState(this, true);
                }
            }
        }
        else if (key.equals(Constants.PREFS_SILENT_BTN))
        {
            silentBtn.setSummary(silentBtn.getEntry());
        }
        else if (key.equals(Constants.PREFS_DEVICE_TYPE))
        {
            deviceType.setSummary(deviceType.getEntry());
        }
    }

    private String getSilentSummary(String value)
    {
        if (value.equals(Constants.BTN_ONLY_SILENT))
        {
            return getResources().getString(R.string.silent);
        }
        else if (value.equals(Constants.BTN_ONLY_VIVERATE))
        {
            return getResources().getString(R.string.vibrate);
        }
        else
        {
            return getResources().getString(R.string.vsmode);
        }
    }

    private String getDeviceSummary(String value)
    {
        if (value.equals(Constants.DEVICE_TYPE1))
        {
            return getResources().getString(R.string.type1);
        }
        else if (value.equals(Constants.DEVICE_TYPE2))
        {
            return getResources().getString(R.string.type2);
        }
        else if (value.equals(Constants.DEVICE_TYPE3))
        {
            return getResources().getString(R.string.type3);
        }
        else if (value.equals(Constants.DEVICE_TYPE4))
        {
            return getResources().getString(R.string.type4);
        }
        else if (value.equals(Constants.DEVICE_TYPE5))
        {
            return getResources().getString(R.string.type5);
        }
        else if (value.equals(Constants.DEVICE_TYPE6))
        {
            return getResources().getString(R.string.type6);
        }
        else
        {
            return getResources().getString(R.string.type7);
        }
    }
}
