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

        // 处理通知栏中按钮的响应事件，widget的按钮响应事件只会被自身捕捉
        WidgetProviderUtil.performButtonEvent(context, intent);

        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                int currentBatteryLevel = intent.getIntExtra("level", -1);

                if (currentBatteryLevel != -1) {
                    configEditor.putInt(Constants.PREFS_BATTERY_LEVEL, currentBatteryLevel).commit();
                }
            }
            // 扫描SD卡完成事件，只显示完成提示
            else if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)
                    && WidgetProviderUtil.scanMediaFlag) {
                Toast.makeText(context, R.string.media_scanner_finished, Toast.LENGTH_SHORT).show();
                WidgetProviderUtil.scanMediaFlag = false;
            }
            // 如果屏幕关闭了就关闭闪光灯
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                boolean savedState = config.getBoolean(Constants.PREFS_FLASH_STATE, false);

                if (savedState) {
                    configEditor.putBoolean(Constants.PREFS_FLASH_STATE, false);
                    SwitchUtils.setCameraFlashState(context, false);
                    configEditor.commit();
                }
            }
            // 如果刚启动则重置闪光灯和屏幕锁的状态
            else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                configEditor.putBoolean(Constants.PREFS_FLASH_STATE, false);
                configEditor.putBoolean(Constants.PREF_AUTOLOCK_STATE, true);
                configEditor.commit();

                // 如果4G开关被系统自动打开，则关掉它
                // 最后一次配置
                int state = config.getInt(Constants.PREF_4G_STATE, WidgetProviderUtil.STATE_ENABLED);

                // 如果最后一次配置是关闭的，且当前已经打开了，就把它关闭
                if (state == WidgetProviderUtil.STATE_DISABLED
                        && SwitchUtils.getWimaxState(context) == WidgetProviderUtil.STATE_ENABLED) {
                    SwitchUtils.toggleWimax(context);
                }
            } else if (("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction()) || "android.bluetooth.intent.action.BLUETOOTH_STATE_CHANGED"
                    .equals(intent.getAction())) && SwitchUtils.toggle_bluetooth_te) {
                if (SwitchUtils.getBluetoothState(context) == WidgetProviderUtil.STATE_ENABLED) {
                    BluetoothTeUtil.toggleBluetoothTe();
                    // 标记是一次性的，只要打开了蓝牙就行
                    SwitchUtils.toggle_bluetooth_te = false;
                }
            }
        }

        // 通知widget更新
        Utils.updateWidget(context);
    }
}
