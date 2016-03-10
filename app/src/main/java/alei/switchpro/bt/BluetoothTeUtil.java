package alei.switchpro.bt;

import alei.switchpro.MyApplication;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;

import java.lang.reflect.Method;

@SuppressLint("NewApi")
public class BluetoothTeUtil {
    private static Object mBluetoothPan;
    private static BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            mBluetoothPan = proxy;
        }

        public void onServiceDisconnected(int profile) {
            mBluetoothPan = null;
        }
    };

    static {
        try {
            Class<?> localClass = ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothAdapter");
            Method localMethod = localClass.getMethod("getDefaultAdapter", new Class[0]);
            Object[] arrayOfObject = new Object[0];
            BluetoothAdapter bluetoothAdapter = (BluetoothAdapter) localMethod.invoke(null, arrayOfObject);
            bluetoothAdapter.getProfileProxy(MyApplication.getInstance(), mProfileServiceListener, 5);
        } catch (Exception e) {
        }
    }

    public static void toggleBluetoothTe() {
        if (mBluetoothPan != null) {
            try {
                Method setMethod = mBluetoothPan.getClass().getDeclaredMethod("setBluetoothTethering",
                        new Class[]{boolean.class});

                if (getBluetoothTe()) {
                    setMethod.invoke(mBluetoothPan, new Object[]{false});
                } else {
                    setMethod.invoke(mBluetoothPan, new Object[]{true});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean getBluetoothTe() {
        try {
            return (Boolean) (mBluetoothPan.getClass().getDeclaredMethod("isTetheringOn", new Class[0]).invoke(
                    mBluetoothPan, new Object[0]));
        } catch (Exception e) {
            return false;
        }
    }
}
