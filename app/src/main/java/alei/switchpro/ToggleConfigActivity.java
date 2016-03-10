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

public class ToggleConfigActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static String[] silentSummary;
    private static String[] silentValues = new String[]{"0", "1", "2"};
    private static String[] deviceSummary;
    private static String[] deviceValues = new String[]{Constants.DEVICE_TYPE1, Constants.DEVICE_TYPE2,
            Constants.DEVICE_TYPE3, Constants.DEVICE_TYPE4, Constants.DEVICE_TYPE5, Constants.DEVICE_TYPE6,
            Constants.DEVICE_TYPE7};
    private CheckBoxPreference aireplaneWifi;
    private CheckBoxPreference toggleFlash;
    private CheckBoxPreference useApn;
    private CheckBoxPreference aireplaneRadio;
    private LevelPreference brightLevel;
    private ListPreference silentBtn;
    private ListPreference deviceType;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        deviceSummary = new String[]{getResources().getString(R.string.type1),
                getResources().getString(R.string.type2), getResources().getString(R.string.type3),
                getResources().getString(R.string.type4), getResources().getString(R.string.type5),
                getResources().getString(R.string.type6), getResources().getString(R.string.type7)};
        silentSummary = new String[]{getResources().getString(R.string.vsmode),
                getResources().getString(R.string.vibrate), getResources().getString(R.string.silent)};
        addPreferencesFromResource(R.xml.pref_toggle_conf);
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUI();
    }

    /**
     * ��Ϊ��onCreate������onResume����ֱ����onResume�г�ʼ��
     */
    private void initUI() {
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

        // ������Ѿ����õĲ���Ҫ���������õĳ�ʼ��
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        String silentValue = config.getString(Constants.PREFS_SILENT_BTN, Constants.BTN_VS);
        silentBtn.setValue(silentValue);
        silentBtn.setSummary(getSilentSummary(silentValue));

        // ���radioѡ���ˣ���wifi�Ͳ�����
        aireplaneWifi.setEnabled(!config.getBoolean(Constants.PREFS_AIRPLANE_RADIO, false));

        // �����豸���
        deviceType.setEnabled(config.getBoolean(Constants.PREFS_TOGGLE_FLASH, true));
        String deviceValue = config.getString(Constants.PREFS_DEVICE_TYPE, Constants.DEVICE_TYPE1);
        deviceType.setValue(deviceValue);
        deviceType.setSummary(getDeviceSummary(deviceValue));

        // ��ʾ��������������ʱ���������ò�����
        brightLevel.setActivity(this);

        // ע��ѡ��仯������
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * ���任ѡ��ʱҪ��̬���������ؼ��п�ѡ����Ŀ
     *
     * @param btnName
     * @return
     */
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(Constants.PREFS_AIRPLANE_RADIO)) {
            // ���ѡ����ֻ�ر��ƶ�����
            if (aireplaneRadio.isChecked()) {
                aireplaneWifi.setEnabled(false);
            } else {
                aireplaneWifi.setEnabled(true);
            }

            // �����ǰ�İ�ť�ǿ��ŵ�
            if (SwitchUtils.getNetworkState(this)) {
                // �������ģʽ�Ѿ���
                if (SwitchUtils.getAirplaneState(this)) {
                    // �ȹرշ���ģʽ
                    SwitchUtils.setAirplaneState(this, false);
                } else {
                    // �ȴ��ֻ�����
                    NetUtils.setSignalState(this, true);
                }
            }
        } else if (key.equals(Constants.PREFS_TOGGLE_FLASH)) {
            if (SwitchUtils.getFlashlight(this)) {
                SwitchUtils.setCameraFlashState(this, false);
                Utils.updateWidget(this);
            }

            deviceType.setEnabled(toggleFlash.isChecked());
        } else if (key.equals(Constants.PREFS_TOGGLE_TIMEOUT)) {
            if (SwitchUtils.getScreenTimeoutState(this)) {
                SwitchUtils.toggleScreenTimeout(this);
                Utils.updateWidget(this);
            }
        } else if (key.equals(Constants.PREFS_USE_APN)) {
            // �����ǰѡ����ʹ��APN
            if (useApn.isChecked()) {
                // �����ǰ������������û�򿪵�
                if (!NetUtils.getMobileNetworkState(this)) {
                    // �ȴ��������ӣ��ڹر�APN
                    NetUtils.setMobileNetworkState(this, true);
                    ApnUtils.setApnState(this, false);
                }
                // ����Ǵ򿪵ģ�ֱ��Ҳ��APN
                else {
                    ApnUtils.setApnState(this, true);
                }

                useApn.setSummary(R.string.use_apn_summary);
            }
            // ����õ�����������
            else {
                // �����ǰAPNû��
                if (!ApnUtils.getApnState(this)) {
                    // �ȴ�APN���ڹر���������
                    ApnUtils.setApnState(this, true);
                    NetUtils.setMobileNetworkState(this, false);
                } else {
                    // ���APN�Ѿ����ˣ���ֱ�Ӵ���������
                    NetUtils.setMobileNetworkState(this, true);
                }
            }
        } else if (key.equals(Constants.PREFS_SILENT_BTN)) {
            silentBtn.setSummary(silentBtn.getEntry());
        } else if (key.equals(Constants.PREFS_DEVICE_TYPE)) {
            deviceType.setSummary(deviceType.getEntry());
        }
    }

    private String getSilentSummary(String value) {
        if (value.equals(Constants.BTN_ONLY_SILENT)) {
            return getResources().getString(R.string.silent);
        } else if (value.equals(Constants.BTN_ONLY_VIVERATE)) {
            return getResources().getString(R.string.vibrate);
        } else {
            return getResources().getString(R.string.vsmode);
        }
    }

    private String getDeviceSummary(String value) {
        if (value.equals(Constants.DEVICE_TYPE1)) {
            return getResources().getString(R.string.type1);
        } else if (value.equals(Constants.DEVICE_TYPE2)) {
            return getResources().getString(R.string.type2);
        } else if (value.equals(Constants.DEVICE_TYPE3)) {
            return getResources().getString(R.string.type3);
        } else if (value.equals(Constants.DEVICE_TYPE4)) {
            return getResources().getString(R.string.type4);
        } else if (value.equals(Constants.DEVICE_TYPE5)) {
            return getResources().getString(R.string.type5);
        } else if (value.equals(Constants.DEVICE_TYPE6)) {
            return getResources().getString(R.string.type6);
        } else {
            return getResources().getString(R.string.type7);
        }
    }
}
