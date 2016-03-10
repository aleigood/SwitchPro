package alei.switchpro.bt;

import alei.switchpro.Constants;
import alei.switchpro.R;
import alei.switchpro.WidgetProviderUtil;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import java.lang.reflect.Method;

public class BluetoothUtils {
    public static final int STATE_OFF = 10;
    public static final int STATE_TURNING_ON = 11;
    public static final int STATE_ON = 12;
    public static final int STATE_TURNING_OFF = 13;

    // ��һ�ַ�����ʹ�÷����ȡBluetooth Adapter
    private static Object device;
    private static Method disableMethod;
    private static Method enableMethod;
    private static Method isEnabledMethod;
    private static Method getStateMethod;

    // �ڶ��ַ�����ֱ��ͨ�������ȡBluetooth Adapter
    private static Object device1;
    private static Method disableMethod1;
    private static Method enableMethod1;
    private static Method isEnabledMethod1;
    private static Method getStateMethod1;

    private static void initialize(Context paramContext) {
        try {
            device = paramContext.getSystemService("bluetooth");

            if (device != null) {
                Class<?> localClass = device.getClass();
                enableMethod = localClass.getMethod("enable", new Class[0]);
                enableMethod.setAccessible(true);
                disableMethod = localClass.getMethod("disable", new Class[0]);
                disableMethod.setAccessible(true);
                isEnabledMethod = localClass.getMethod("isEnabled", new Class[0]);
                isEnabledMethod.setAccessible(true);
                getStateMethod = localClass.getMethod("getState", new Class[0]);
                getStateMethod.setAccessible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class<?> localClass = ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothAdapter");
            Method localMethod = localClass.getMethod("getDefaultAdapter", new Class[0]);
            Object[] arrayOfObject = new Object[0];
            device1 = localMethod.invoke(null, arrayOfObject);
            enableMethod1 = localClass.getMethod("enable", new Class[0]);
            disableMethod1 = localClass.getMethod("disable", new Class[0]);
            isEnabledMethod1 = localClass.getMethod("isEnabled", new Class[0]);
            getStateMethod1 = localClass.getMethod("getState", new Class[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isBluetoothEnabled(Context context) {
        try {
            return ((Boolean) isEnabledMethod.invoke(device, new Object[]{})).booleanValue();
        } catch (Exception e) {
            // ��֤��һ�ַ����޷�ִ��ʱ�������쳣���ڶ��ַ�������ִ��
            try {
                return ((Boolean) isEnabledMethod1.invoke(device1, new Object[]{})).booleanValue();
            } catch (Exception e2) {
                String bluetoothString = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.BLUETOOTH_ON);

                // "0" means closed
                if (bluetoothString.equals("0")) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    public static int getBluetoothState(Context context) {
        int state = STATE_OFF;

        try {
            state = ((Integer) getStateMethod.invoke(device, new Object[]{})).intValue();
        } catch (Exception e) {
            // ��֤��һ�ַ����޷�ִ��ʱ�������쳣���ڶ��ַ�������ִ��
            try {
                state = ((Integer) getStateMethod1.invoke(device1, new Object[]{})).intValue();
            } catch (Exception e2) {
                if (isBluetoothEnabled(context)) {
                    state = STATE_ON;
                } else {
                    state = STATE_OFF;
                }
            }
        }

        switch (state) {
            case STATE_OFF:
                return WidgetProviderUtil.STATE_DISABLED;
            case STATE_ON:
                return WidgetProviderUtil.STATE_ENABLED;
            case STATE_TURNING_OFF:
                return WidgetProviderUtil.STATE_INTERMEDIATE;
            case STATE_TURNING_ON:
                return WidgetProviderUtil.STATE_INTERMEDIATE;
            default:
                return WidgetProviderUtil.STATE_DISABLED;
        }
    }

    public static void toggleBluetooth(Context context) {
        initialize(context);

        // ���Ҫ������
        if (!isBluetoothEnabled(context)) {
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

            if (config.getBoolean(Constants.PREFS_BLUETOOTH_DISCOVER, false)) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, discoverableIntent, 0);

                try {
                    PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
                    pendingIntent.send();
                } catch (CanceledException e) {
                    e.printStackTrace();
                }
            } else {
                enable(context);

                // �����������Ҫ�����ý��棬�ڴ򿪺󵯳�����
                boolean openAction = config.getBoolean(Constants.PREFS_TOGGLE_BLUETOOTH, false);

                if (openAction) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setClassName("com.android.settings", "com.android.settings.bluetooth.BluetoothSettings");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                    try {
                        pendingIntent.send();
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                }

                Toast.makeText(context, R.string.update_buetooth, Toast.LENGTH_LONG).show();
            }
        } else {
            disable(context);
        }

    }

    public static void disable(Context context) {
        // �ж��Ƿ��Ѿ���ȷִ����
        boolean result = false;

        try {
            disableMethod.invoke(device, new Object[]{});
            result = true;
        } catch (Exception e) {
        }

        // ��֤��һ�ַ����޷�ִ��ʱ�������쳣���ڶ��ַ�������ִ��
        try {
            disableMethod1.invoke(device1, new Object[]{});
            result = true;
        } catch (Exception e) {
        }

        // ������ַ���������
        if (!result) {
            switchBT(context);
        }
    }

    public static void enable(Context context) {
        // �ж��Ƿ��Ѿ���ȷִ����
        boolean result = false;

        try {
            enableMethod.invoke(device, new Object[]{});
            result = true;
        } catch (Exception e) {
        }

        // ��֤��һ�ַ����޷�ִ��ʱ�������쳣���ڶ��ַ�������ִ��
        try {
            enableMethod1.invoke(device1, new Object[]{});
            result = true;
        } catch (Exception e) {
        }

        if (!result) {
            switchBT(context);
        }
    }

    private static void switchBT(Context context) {
        Intent launchIntent = new Intent();
        launchIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        launchIntent.setData(Uri.parse("custom:" + 4));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, launchIntent, 0);

        try {
            pi.send();
        } catch (CanceledException e1) {
            e1.printStackTrace();
        }
    }

}