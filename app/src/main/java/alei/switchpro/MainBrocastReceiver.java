package alei.switchpro;

import alei.switchpro.bt.BluetoothTeUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MainBrocastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MainBrocastReceiver", "onReceive:" + intent.getAction());
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor configEditor = config.edit();

        // ����֪ͨ���а�ť����Ӧ�¼���widget�İ�ť��Ӧ�¼�ֻ�ᱻ����׽
        WidgetProviderUtil.performButtonEvent(context, intent);

        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                int currentBatteryLevel = intent.getIntExtra("level", -1);

                if (currentBatteryLevel != -1) {
                    configEditor.putInt(Constants.PREFS_BATTERY_LEVEL, currentBatteryLevel).commit();
                }
            }
            // ɨ��SD������¼���ֻ��ʾ�����ʾ
            else if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)
                    && WidgetProviderUtil.scanMediaFlag) {
                Toast.makeText(context, R.string.media_scanner_finished, Toast.LENGTH_SHORT).show();
                WidgetProviderUtil.scanMediaFlag = false;
            }
            // �����Ļ�ر��˾͹ر������
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                boolean savedState = config.getBoolean(Constants.PREFS_FLASH_STATE, false);

                if (savedState) {
                    configEditor.putBoolean(Constants.PREFS_FLASH_STATE, false);
                    SwitchUtils.setCameraFlashState(context, false);
                    configEditor.commit();
                }
            }
            // �������������������ƺ���Ļ����״̬
            else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                configEditor.putBoolean(Constants.PREFS_FLASH_STATE, false);
                configEditor.putBoolean(Constants.PREF_AUTOLOCK_STATE, true);
                configEditor.commit();

                // ���4G���ر�ϵͳ�Զ��򿪣���ص���
                // ���һ������
                int state = config.getInt(Constants.PREF_4G_STATE, WidgetProviderUtil.STATE_ENABLED);

                // ������һ�������ǹرյģ��ҵ�ǰ�Ѿ����ˣ��Ͱ����ر�
                if (state == WidgetProviderUtil.STATE_DISABLED
                        && SwitchUtils.getWimaxState(context) == WidgetProviderUtil.STATE_ENABLED) {
                    SwitchUtils.toggleWimax(context);
                }
            } else if (("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction()) || "android.bluetooth.intent.action.BLUETOOTH_STATE_CHANGED"
                    .equals(intent.getAction())) && SwitchUtils.toggle_bluetooth_te) {
                if (SwitchUtils.getBluetoothState(context) == WidgetProviderUtil.STATE_ENABLED) {
                    BluetoothTeUtil.toggleBluetoothTe();
                    // �����һ���Եģ�ֻҪ������������
                    SwitchUtils.toggle_bluetooth_te = false;
                }
            }
        }

        // ֪ͨwidget����
        Utils.updateWidget(context);
    }
}
