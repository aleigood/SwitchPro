package alei.switchpro;

import alei.switchpro.apn.ApnUtils;
import alei.switchpro.brightness.BrightnessActivity;
import alei.switchpro.brightness.BrightnessBar;
import alei.switchpro.bt.BluetoothTeUtil;
import alei.switchpro.bt.BluetoothUtils;
import alei.switchpro.flash.FlashlightActivity;
import alei.switchpro.flash.FlashlightUtils;
import alei.switchpro.lock.DeviceAdminActivity;
import alei.switchpro.net.NetUtils;
import alei.switchpro.nfc.NFCUtil;
import alei.switchpro.process.ProcessUtils;
import alei.switchpro.reboot.RebootActivity;
import alei.switchpro.sync.SyncUtils;
import alei.switchpro.sync.SyncUtilsV4;
import alei.switchpro.timeout.TimeoutSelectorActivity;
import alei.switchpro.usb.MountUtils;
import alei.switchpro.volume.VolumeActivity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import android.widget.Toast;

import java.lang.reflect.Method;

public class SwitchUtils {
    public static final int WIFI_AP_STATE_DISABLED = 1;
    public static final int WIFI_AP_STATE_ENABLED = 3;
    // android sdk version > 14
    public static final int WIFI_AP_STATE_DISABLING_14 = 10;
    public static final int WIFI_AP_STATE_DISABLED_14 = 11;
    public static final int WIFI_AP_STATE_ENABLING_14 = 12;
    public static final int WIFI_AP_STATE_ENABLED_14 = 13;
    public static final int WIFI_AP_STATE_FAILED_14 = 14;
    public static boolean toggle_bluetooth_te = false;
    public static SparseArray<Notification> notifications = new SparseArray<Notification>();
    private static WakeLock wakeLock;
    private static KeyguardLock keyLock;

    /**
     * Gets the state of Wi-Fi
     *
     * @param context
     * @return STATE_ENABLED, STATE_DISABLED, or STATE_INTERMEDIATE
     */
    public static int getWifiState(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();

        if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
            return WidgetProviderUtil.STATE_DISABLED;
        } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            return WidgetProviderUtil.STATE_ENABLED;
        } else {
            return WidgetProviderUtil.STATE_INTERMEDIATE;
        }
    }

    /**
     * Toggles the state of Wi-Fi
     *
     * @param context
     */
    public static void toggleWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = getWifiState(context);

        if (wifiState == WidgetProviderUtil.STATE_ENABLED) {
            wifiManager.setWifiEnabled(false);
        } else if (wifiState == WidgetProviderUtil.STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);

            // 如果配置了需要打开配置界面，在打开后弹出界面
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
            boolean openAction = config.getBoolean(Constants.PREFS_TOGGLE_WIFI, false);

            if (openAction) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                try {
                    pendingIntent.send();
                } catch (CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Toggles of APN
     *
     * @param context
     */
    public static void toggleApn(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

        // 如果是9以上的系统，默认是选中APN开关
        if (config.getBoolean(Constants.PREFS_USE_APN, false)) {
            ApnUtils.switchAndNotify(context);
            Utils.updateWidget(context);
        }
        // 如果没选择APN开关
        else {
            if (VERSION.SDK_INT >= 9) {
                NetUtils.toggleMobileNetwork9(context);
                // 这个状态可以立即更新
                Utils.updateWidget(context);
            } else {
                NetUtils.toggleMobileNetwork(context);
            }
        }
    }

    /**
     * Gets the state of APN.
     *
     * @param context
     * @return true if enabled
     */
    public static boolean getApnState(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

        if (config.getBoolean(Constants.PREFS_USE_APN, false)) {
            return ApnUtils.getApnState(context);
        } else {
            return NetUtils.getMobileNetworkState(context);
        }
    }

    /**
     * Gets the state of auto-sync.
     *
     * @param context
     * @return true if enabled
     */
    public static boolean getSync(Context context) {
        if (VERSION.SDK.equals("4")) {
            return SyncUtilsV4.getSync(context);
        } else {
            return SyncUtils.getSync(context);
        }
    }

    /**
     * Toggle auto-sync
     *
     * @param context
     */
    public static void toggleSync(Context context) {
        // 如果配置了需要打开配置界面，在打开后弹出界面
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean openAction = config.getBoolean(Constants.PREFS_TOGGLE_SYNC, false);

        // 如果需要打开面板，且当前同步是关闭的
        if (openAction) {
            Intent intent = new Intent("android.settings.SYNC_SETTINGS");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            if (VERSION.SDK.equals("4")) {
                SyncUtilsV4.toggleSync(context);
            } else {
                SyncUtils.toggleSync(context);
            }
        }
    }

    /**
     * Toggles the state of GPS.
     *
     * @param context
     */
    public static void toggleGps(Context context) {
        // 如果配置了需要打开配置界面，在打开后弹出界面
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        // 如果SDK大于9，默认是打开面板，除非设置它不打开面板
        boolean openAction = config.getBoolean(Constants.PREFS_TOGGLE_GPS, false);

        // 如果需要打开面板
        if (openAction) {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException e1) {
                e1.printStackTrace();
            }
        } else {
            try {
                ContentResolver resolver = context.getContentResolver();
                boolean enabled = getGpsState(context);
                Settings.Secure.class.getMethod("setLocationProviderEnabled",
                        new Class[]{ContentResolver.class, String.class, boolean.class}).invoke(null,
                        new Object[]{resolver, LocationManager.GPS_PROVIDER, !enabled});
            } catch (Exception e) {
                Intent launchIntent = new Intent();
                launchIntent.setClassName("com.android.settings",
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                launchIntent.setData(Uri.parse("custom:" + 3));
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, launchIntent, 0);

                try {
                    pi.send();
                } catch (CanceledException e1) {
                    e1.printStackTrace();
                }
            }

            // 通知widget更新
            Utils.updateWidget(context);

            if (!config.contains(Constants.PREFS_GPS_FIRST_LAUNCH) && VERSION.SDK_INT >= 9 && VERSION.SDK_INT != 17) {
                config.edit().putBoolean(Constants.PREFS_GPS_FIRST_LAUNCH, false).commit();
                Intent intent = new Intent(context, GoToSettingsActivity.class);
                intent.putExtra("title", context.getString(R.string.is_it_work));
                intent.putExtra("content", context.getString(R.string.goto_settings));
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                try {
                    PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                    pendingIntent.send();
                } catch (CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the state of GPS location.
     *
     * @param context
     * @return true if enabled.
     */
    public static boolean getGpsState(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String gps = Settings.Secure.getString(resolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        return gps.contains(LocationManager.GPS_PROVIDER);
    }

    /**
     * Toggle grayvity
     *
     * @param context
     */
    public static void toggleGrayity(Context context) {
        ContentResolver cr = context.getContentResolver();
        int autoRotate = Settings.System.getInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0);

        if (autoRotate == 1) {
            Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0);
            Toast.makeText(context, R.string.auto_rotate_off, Toast.LENGTH_SHORT).show();
        } else {
            Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 1);
            Toast.makeText(context, R.string.auto_rotate_on, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets state of gravity.
     *
     * @param context
     * @return true if more than moderately bright.
     */
    public static boolean getGravityState(Context context) {
        int autoRotate = Settings.System
                .getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

        if (autoRotate == 1) {
            return true;
        }

        return false;
    }

    public static void toggleNetSwitch(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");

        if (VERSION.SDK_INT >= 16) {
            intent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
        } else {
            intent.setClassName("com.android.phone", "com.android.phone.Settings");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        try {
            // 如果是从通知栏弹出，需要先弹起通知栏
            PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
            pendingIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public static boolean getNetSwitch(Context context) {
        int netType = NetUtils.getNetworkType(context);

        if (netType == TelephonyManager.NETWORK_TYPE_EDGE || netType == TelephonyManager.NETWORK_TYPE_GPRS) {
            return false;
        } else {
            return true;
        }
    }

    public static void toggleBattery(Context context) {
        if (Utils.isAppExist(context, "com.htc.htcpowermanager")) {
            Intent htcIntent = new Intent(Intent.ACTION_MAIN);
            htcIntent.addCategory("com.android.settings.SHORTCUT");
            htcIntent.setClassName("com.htc.htcpowermanager", "com.htc.htcpowermanager.PowerManagerActivity");
            htcIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                PendingIntent.getActivity(context, 0, htcIntent, 0).send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            if (VERSION.SDK_INT >= 11) {
                Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
                shortcutIntent.addCategory("com.android.settings.SHORTCUT");
                shortcutIntent.setClassName("com.android.settings",
                        "com.android.settings.Settings$PowerUsageSummaryActivity");
                shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    // 如果是从通知栏弹出，需要先弹起通知栏
                    PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                    PendingIntent.getActivity(context, 0, shortcutIntent, 0).send();
                } catch (CanceledException e) {
                    e.printStackTrace();
                }

            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.fuelgauge.PowerUsageSummary");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                try {
                    // 如果是从通知栏弹出，需要先弹起通知栏
                    PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                    pendingIntent.send();
                } catch (CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void toggleBirghtness(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showBar = config.getBoolean(Constants.PREFS_SHOW_BRIGHTNESS_BAR, false);

        if (showBar) {
            Intent intent = new Intent(context, BrightnessBar.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            Intent intent = new Intent(context, BrightnessActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Gets state of brightness.
     *
     * @param context
     * @return true if more than moderately bright.
     */
    public static int getBrightness(Context context) {
        int brightness = Settings.System.getInt(context.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, BrightnessActivity.BRIGHT_LEVEL_30);
        int mode = Settings.System.getInt(context.getContentResolver(), BrightnessActivity.BRIGHT_MODE, 1);

        if (mode == BrightnessActivity.BRIGHT_MODE_AUTO) {
            return WidgetProviderUtil.STATE_OTHER;
        } else {
            if (brightness <= BrightnessActivity.BRIGHT_LEVEL_30) {
                return WidgetProviderUtil.STATE_DISABLED;
            } else if (brightness > BrightnessActivity.BRIGHT_LEVEL_30
                    && brightness <= BrightnessActivity.BRIGHT_LEVEL_70) {
                return WidgetProviderUtil.STATE_INTERMEDIATE;
            } else if (brightness > BrightnessActivity.BRIGHT_LEVEL_70
                    && brightness <= BrightnessActivity.BRIGHT_LEVEL_100) {
                return WidgetProviderUtil.STATE_ENABLED;
            }
        }

        return WidgetProviderUtil.STATE_DISABLED;
    }

    /**
     * Gets state of bluetooth
     *
     * @param context
     * @return true if enabled.
     */
    public static int getBluetoothState(Context context) {
        return BluetoothUtils.getBluetoothState(context);
    }

    /**
     * Toggles the state of bluetooth
     *
     * @param context
     */
    public static void toggleBluetooth(Context context) {
        BluetoothUtils.toggleBluetooth(context);
    }

    /**
     * 获取屏幕超时状态
     *
     * @param context
     * @return
     */
    public static boolean getScreenTimeoutState(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0) < 0 ? true
                : false;
    }

    /**
     * 设置屏幕超时
     *
     * @param context
     */
    public static void toggleScreenTimeout(Context context) {
        // 如果配置了需要打开配置界面，在打开后弹出界面
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean openAction = config.getBoolean(Constants.PREFS_TOGGLE_TIMEOUT, false);

        if (openAction) {
            Intent intent = new Intent(context, TimeoutSelectorActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {

            int screenTimeOut = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, 0);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            // 如果不是常亮，就保存这个时间值
            if (screenTimeOut != -1) {
                SharedPreferences.Editor configEditor = sp.edit();
                configEditor.putInt("Timeout", screenTimeOut);
                configEditor.commit();
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
                Toast.makeText(context, R.string.keep_awake_on, Toast.LENGTH_SHORT).show();

                if (VERSION.SDK_INT >= 17) {
                    wakeLock = Utils.getWakeLock(context);
                    wakeLock.acquire();
                }
            }
            // 读出已保存的时间值
            else {
                int time = sp.getInt("Timeout", 30000);
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);
                Toast.makeText(context, R.string.keep_awake_off, Toast.LENGTH_SHORT).show();

                if (VERSION.SDK_INT >= 17) {
                    if (wakeLock != null) {
                        wakeLock.release();
                    }
                }
            }
        }
    }

    public static void toggleNetwork(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean closeRadio = config.getBoolean(Constants.PREFS_AIRPLANE_RADIO, false);

        if (closeRadio) {
            NetUtils.toggleSignal(context);
            Utils.updateWidget(context);
        } else {
            toggleAirPlane(context);
        }
    }

    public static boolean getNetworkState(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean closeRadio = config.getBoolean(Constants.PREFS_AIRPLANE_RADIO, true);

        if (closeRadio) {
            return !NetUtils.getSignalState(context);
        } else {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    /**
     * 飞行模式开关
     *
     * @param context
     */
    public static void toggleAirPlane(Context context) {
        if (VERSION.SDK_INT >= 17) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            if (getAirplaneState(context)) {
                setAirplaneState(context, false);
            } else {
                setAirplaneState(context, true);
            }
        }
    }

    public static void setAirplaneState(Context context, boolean state) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean closeWifi = config.getBoolean(Constants.PREFS_AIRPLANE_WIFI, true);

        if (state) {
            // 开启飞行模式
            if (closeWifi) {
                Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
            } else {
                Settings.System.putString(context.getContentResolver(), Settings.System.AIRPLANE_MODE_RADIOS, "cell"
                        + (closeWifi ? ",wifi" : ""));
                Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
            }
        } else {
            Settings.System.putString(context.getContentResolver(), Settings.System.AIRPLANE_MODE_RADIOS,
                    "cell,bluetooth,wifi");
            Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
        }

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", state);
        context.sendBroadcast(intent);
    }

    /**
     * 获取飞行模式状态
     *
     * @param context
     * @return
     */
    public static boolean getAirplaneState(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * 加载SD卡中的媒体
     */
    public static void scanMedia(Context context) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                + Environment.getExternalStorageDirectory())));
        Toast.makeText(context, R.string.media_scanner_start, Toast.LENGTH_LONG).show();
    }

    public static void toggleVibrate(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        String btn = config.getString(Constants.PREFS_SILENT_BTN, Constants.BTN_VS);
        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);

        // 如果当前是正常的，则需要进行静音
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            if (btn.equals(Constants.BTN_ONLY_SILENT)) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } else if (btn.equals(Constants.BTN_ONLY_VIVERATE)) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }

            // 如果选择了媒体静音，则在静音时关闭媒体音量
            if (config.getBoolean(Constants.PREFS_MUTE_MEDIA, false)) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }

            if (config.getBoolean(Constants.PREFS_MUTE_ALARM, false)) {
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            }
        } else {
            // 只选择了静音，则恢复铃声
            if (btn.equals(Constants.BTN_ONLY_SILENT)) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            }
            // 只选择了振动则恢复铃声
            else if (btn.equals(Constants.BTN_ONLY_VIVERATE)) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            }
            // 选择了静音和振动模式
            else {
                // 如果当前是振动则下面进入静音模式，注意这地方不用再设置媒体静音，因为进入振动模式时已经设置一次了，再设置一次将无法恢复
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
                }
            }
        }
    }

    /**
     * 获取静音模式
     *
     * @param context
     * @return
     */
    public static int getViberate(Context context) {
        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(
                Context.AUDIO_SERVICE);
        return audioManager.getRingerMode();
    }

    public static void toggleUnlockPattern(Context context) {
        if (VERSION.SDK_INT >= 8) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            android.provider.Settings.System.putInt(context.getContentResolver(), Settings.System.LOCK_PATTERN_ENABLED,
                    getUnlockPattern(context) ? 0 : 1);
        }
    }

    public static boolean getUnlockPattern(Context context) {
        return 1 == android.provider.Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_PATTERN_ENABLED, 0);
    }

    public static void toggleFlashlight(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        boolean openCameraFlash = config.getBoolean(Constants.PREFS_TOGGLE_FLASH, true);

        if (!openCameraFlash) {
            Intent intent = new Intent(context, FlashlightActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            setCameraFlashState(context, !getFlashlight(context));
        }
    }

    public static void setCameraFlashState(Context context, boolean state) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor configEditor = config.edit();
        int deviceType = Integer.parseInt(config.getString(Constants.PREFS_DEVICE_TYPE, "0"));

        switch (deviceType) {
            case 0:
                FlashlightUtils.setFlashlightDefault(state, context);
                break;
            // Milestone/Droid
            case 1:
                FlashlightUtils.setFlashlightDroid(context, state);
                FlashlightUtils.setFlashlightDroid2(state);
                break;
            // HTC Phone
            case 2:
                FlashlightUtils.setFlashlightHtc(state);
                break;
            // LG
            case 3:
                FlashlightUtils.setFlashlightLG(state);
                break;
            // samsung
            case 4:
                FlashlightUtils.setFlashlightSamsung(state);
                break;
            // other
            case 5:
                FlashlightUtils.setFlashlight(state, context);
                break;
            // final
            case 6:
                FlashlightUtils.setFlashlightV9(state, context);
                break;
            default:
                FlashlightUtils.setFlashlightDefault(state, context);
                break;
        }

        if (state) {
            wakeLock = Utils.getWakeLock(context);
            wakeLock.acquire();
        } else {
            if (wakeLock != null) {
                wakeLock.release();
            }
        }

        configEditor.putBoolean(Constants.PREFS_FLASH_STATE, state);
        configEditor.commit();
    }

    public static boolean getFlashlight(Context context) {
        return FlashlightUtils.getFlashlight();
    }

    public static void rebootSystem(Context context) {
        Intent intent = new Intent(context, RebootActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        try {
            // 如果是从通知栏弹出，需要先弹起通知栏
            PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
            pendingIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public static int getWimaxState(Context context) {
        int wimaxState = WidgetProviderUtil.STATE_DISABLED;

        try {
            Object wimaxManager = context.getSystemService("wimax");
            Method getWimaxState = wimaxManager.getClass().getMethod("getWimaxState", (Class[]) null);
            wimaxState = (Integer) getWimaxState.invoke(wimaxManager, (Object[]) null);
        } catch (Exception e) {
        }

        if (wimaxState == 1) {
            return WidgetProviderUtil.STATE_DISABLED;
        } else if (wimaxState == 3) {
            return WidgetProviderUtil.STATE_ENABLED;
        } else {
            return WidgetProviderUtil.STATE_INTERMEDIATE;
        }
    }

    public static void toggleWimax(Context context) {
        // 存储当前的状态，以便在重启以后恢复状态
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor configEditor = config.edit();
        int wimaxState = getWimaxState(context);

        try {
            Object wimaxManager = context.getSystemService("wimax");
            Method setWimaxEnabled = wimaxManager.getClass().getMethod("setWimaxEnabled", new Class[]{Boolean.TYPE});

            if (wimaxState == WidgetProviderUtil.STATE_ENABLED) {
                setWimaxEnabled.invoke(wimaxManager, new Object[]{Boolean.FALSE});
                configEditor.putInt(Constants.PREF_4G_STATE, WidgetProviderUtil.STATE_DISABLED);
            } else if (wimaxState == WidgetProviderUtil.STATE_DISABLED) {
                setWimaxEnabled.invoke(wimaxManager, new Object[]{Boolean.TRUE});
                configEditor.putInt(Constants.PREF_4G_STATE, WidgetProviderUtil.STATE_ENABLED);
            }

            configEditor.commit();
        } catch (Exception e) {
            Intent intent = new Intent("android.intent.action.MAIN");

            if (VERSION.SDK_INT >= 16) {
                intent.setClassName("com.android.phone", "com.android.phone.MobileNetworkSettings");
            } else {
                intent.setClassName("com.android.phone", "com.android.phone.Settings");
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException ce) {
                ce.printStackTrace();
            }

            Toast.makeText(context, R.string.func_error, Toast.LENGTH_SHORT).show();
        }
    }

    // 需要在Widget在创建是初始化屏幕锁的状态
    public static void toggleAutoLock(Context context) {
        KeyguardManager keyManager = (KeyguardManager) context.getApplicationContext().getSystemService("keyguard");
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor configEditor = config.edit();

        // 自动锁锁如果是打开的
        if (config.getBoolean(Constants.PREF_AUTOLOCK_STATE, true)) {
            if (keyLock == null) {
                keyLock = keyManager.newKeyguardLock(context.getPackageName());
            }

            keyLock.disableKeyguard();
            configEditor.putBoolean(Constants.PREF_AUTOLOCK_STATE, false);
            Toast.makeText(context, R.string.auto_screen_lock_tip, Toast.LENGTH_LONG).show();
        } else {
            if (keyLock == null) {
                keyLock = keyManager.newKeyguardLock(context.getPackageName());
                keyLock.disableKeyguard();
                keyLock.reenableKeyguard();
            } else {
                keyLock.reenableKeyguard();
                keyLock = null;
            }
            configEditor.putBoolean(Constants.PREF_AUTOLOCK_STATE, true);
        }

        configEditor.commit();
    }

    public static boolean getAutoLock(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.PREF_AUTOLOCK_STATE, true);
    }

    public static void toggleSpeakMode(Context context) {
        AudioManager sAudioManager = (AudioManager) context.getApplicationContext().getSystemService("audio");
        boolean speakModeState = sAudioManager.isSpeakerphoneOn();
        sAudioManager.setSpeakerphoneOn(!speakModeState);
    }

    public static boolean getSpeakMode(Context context) {
        AudioManager sAudioManager = (AudioManager) context.getApplicationContext().getSystemService("audio");
        return sAudioManager.isSpeakerphoneOn();
    }

    public static int getWifiApState(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int state = WIFI_AP_STATE_DISABLED;

        try {
            Method wmMethod = wifi.getClass().getDeclaredMethod("getWifiApState", new Class[]{});
            state = ((Integer) wmMethod.invoke(wifi, new Object[]{})).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (VERSION.SDK_INT >= 14) {
            switch (state) {
                case WIFI_AP_STATE_DISABLED_14:
                    return WidgetProviderUtil.STATE_DISABLED;
                case WIFI_AP_STATE_ENABLED_14:
                    return WidgetProviderUtil.STATE_ENABLED;
                default:
                    return WidgetProviderUtil.STATE_INTERMEDIATE;
            }
        } else {
            switch (state) {
                case WIFI_AP_STATE_DISABLED:
                    return WidgetProviderUtil.STATE_DISABLED;
                case WIFI_AP_STATE_ENABLED:
                    return WidgetProviderUtil.STATE_ENABLED;
                default:
                    return WidgetProviderUtil.STATE_INTERMEDIATE;
            }
        }
    }

    public static void toggleWifiAp(Context context) {
        if (getWifiState(context) == WidgetProviderUtil.STATE_ENABLED) {
            toggleWifi(context);
        }

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        try {
            Method m = WifiManager.class.getMethod("setWifiApEnabled", new Class[]{WifiConfiguration.class,
                    boolean.class});
            m.invoke(wifi, null, !(getWifiApState(context) == WidgetProviderUtil.STATE_ENABLED));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getMountState() {
        return MountUtils.getMountState();
    }

    public static void toggleMount(Context context) {
        MountUtils.toggleMount(context);
    }

    public static void toggleWifiSleepPolicy(Context context) {
        if (VERSION.SDK_INT >= 17) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.android.settings", "com.android.settings.Settings$AdvancedWifiSettingsActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            try {
                // 如果是从通知栏弹出，需要先弹起通知栏
                PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                pendingIntent.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            if (getWifiSleepPolicy(context)) {
                Settings.System.putInt(context.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, 0);
                Toast.makeText(context, R.string.wifi_sleep_off, Toast.LENGTH_SHORT).show();
            } else {
                Settings.System.putInt(context.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, 2);
                Toast.makeText(context, R.string.wifi_sleep_on, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean getWifiSleepPolicy(Context context) {
        int value = Settings.System.getInt(context.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, 0);
        return value == 2 ? true : false;
    }

    public static boolean getUsbTetherState(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class<?> cls = Class.forName("android.net.ConnectivityManager");

            Method getTetherableUsbRegexs = cls.getMethod("getTetherableUsbRegexs", new Class[]{});
            Method getTetheredIfaces = cls.getMethod("getTetheredIfaces", new Class[]{});

            String[] mUsbRegexs = (String[]) getTetherableUsbRegexs.invoke(cm, new Object[]{});
            String[] tethered = (String[]) getTetheredIfaces.invoke(cm, new Object[]{});

            boolean usbTethered = false;

            for (Object o : tethered) {
                String s = (String) o;
                for (String regex : mUsbRegexs) {
                    if (s.matches(regex))
                        usbTethered = true;
                }
            }

            if (usbTethered) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void toggleUsbTether(Context context) {
        try {
            boolean massStorageActive = Environment.MEDIA_SHARED.equals(Environment.getExternalStorageState());

            if (massStorageActive) {
                Toast.makeText(context, R.string.usb_tethering_storage_active_subtext, Toast.LENGTH_SHORT).show();
                return;
            }

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Class<?> cls = Class.forName("android.net.ConnectivityManager");

            Method getTetherableIfaces = cls.getMethod("getTetherableIfaces", new Class[]{});
            Method getTetherableUsbRegexs = cls.getMethod("getTetherableUsbRegexs", new Class[]{});
            Method getTetheredIfaces = cls.getMethod("getTetheredIfaces", new Class[]{});
            Method tetherMethod = cls.getMethod("tether", new Class[]{String.class});
            Method untetherMethod = cls.getMethod("untether", new Class[]{String.class});
            getTetherableIfaces.setAccessible(true);
            getTetherableUsbRegexs.setAccessible(true);

            String[] mUsbRegexs = (String[]) getTetherableUsbRegexs.invoke(cm, new Object[]{});

            if (getUsbTetherState(context)) {
                String[] tethered = (String[]) getTetheredIfaces.invoke(cm, new Object[]{});
                String usbIface = findIface(tethered, mUsbRegexs);

                if (usbIface == null) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.settings", "com.android.settings.Settings$TetherSettingsActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                    try {
                        // 如果是从通知栏弹出，需要先弹起通知栏
                        PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0)
                                .send();
                        pendingIntent.send();
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                if ((Integer) untetherMethod.invoke(cm, new Object[]{usbIface}) != Constants.TETHER_ERROR_NO_ERROR) {
                    Toast.makeText(context, R.string.usb_tethering_errored_subtext, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                String[] available = (String[]) getTetherableIfaces.invoke(cm, new Object[]{});
                String usbIface = findIface(available, mUsbRegexs);

                if (usbIface == null) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.settings", "com.android.settings.Settings$TetherSettingsActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                    try {
                        // 如果是从通知栏弹出，需要先弹起通知栏
                        PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0)
                                .send();
                        pendingIntent.send();
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                if ((Integer) tetherMethod.invoke(cm, new Object[]{usbIface}) != Constants.TETHER_ERROR_NO_ERROR) {
                    Toast.makeText(context, R.string.usb_tethering_errored_subtext, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    private static String findIface(String[] ifaces, String[] regexes) {
        for (String iface : ifaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }

    public static void lockScreen(Context context) {
        Object localObject1 = context.getApplicationContext().getSystemService("device_policy");

        try {
            boolean isActive = ((Boolean) localObject1.getClass()
                    .getMethod("isAdminActive", new Class[]{ComponentName.class})
                    .invoke(localObject1, new Object[]{new ComponentName(context, MainBrocastReceiver.class)}))
                    .booleanValue();

            if (isActive) {
                localObject1.getClass().getMethod("lockNow", new Class[]{}).invoke(localObject1, new Object[]{});
            } else {
                Intent intent = new Intent(context, DeviceAdminActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                try {
                    // 如果是从通知栏弹出，需要先弹起通知栏
                    PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                    pendingIntent.send();
                } catch (CanceledException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public static void setvolume(Context context) {
        Intent intent = new Intent(context, VolumeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        try {
            // 如果是从通知栏弹出，需要先弹起通知栏
            PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
            pendingIntent.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void killProcess(Context context) {
        ProcessUtils.killProcess(context);
    }

    public static void toggleMemory(Context context) {
        Intent alarmIntent = new Intent(Intent.ACTION_MAIN);
        alarmIntent.setClassName("com.android.settings", "com.android.settings.Settings$RunningServicesActivity");
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
            PendingIntent.getActivity(context, 0, alarmIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void toggleStorage(Context context) {
        Intent alarmIntent = new Intent(Intent.ACTION_MAIN);
        alarmIntent.setClassName("com.android.settings", "com.android.settings.Settings$StorageSettingsActivity");
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
            PendingIntent.getActivity(context, 0, alarmIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void toggleBlueToothTe(Context context) {
        if (VERSION.SDK_INT > 10) {
            if (getBluetoothState(context) == WidgetProviderUtil.STATE_ENABLED) {
                BluetoothTeUtil.toggleBluetoothTe();
            } else {
                toggle_bluetooth_te = true;
                toggleBluetooth(context);
            }
        }
    }

    public static boolean getBluetoothTe(Context context) {
        if (VERSION.SDK_INT > 10) {
            return BluetoothTeUtil.getBluetoothTe();
        }
        return false;
    }

    public static void toggleNFC(Context context) {
        if (VERSION.SDK_INT > 8) {
            NFCUtil.toggleNFC(context);
        }
    }

    public static boolean getNFC(Context context) {
        if (VERSION.SDK_INT > 8) {
            return NFCUtil.getNFC(context);
        }

        return false;
    }
}
