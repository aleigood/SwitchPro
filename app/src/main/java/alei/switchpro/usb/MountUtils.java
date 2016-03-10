package alei.switchpro.usb;

import alei.switchpro.R;
import alei.switchpro.WidgetProviderUtil;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * @author Alei
 */
public class MountUtils {
    private static Object iMountServiceV8;
    private static Object iMountService;

    static {
        try {
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method method = serviceManagerClass.getMethod("getService", String.class);
            Object hardwareService = method.invoke(null, "mount");

            Class<?> iHardwareServiceStubClass = Class.forName("android.os.storage.IMountService$Stub");
            Method asInterfaceMethod = iHardwareServiceStubClass.getMethod("asInterface", IBinder.class);
            iMountServiceV8 = asInterfaceMethod.invoke(null, hardwareService);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method method = serviceManagerClass.getMethod("getService", String.class);
            Object hardwareService = method.invoke(null, "mount");

            Class<?> iHardwareServiceStubClass = Class.forName("android.os.IMountService$Stub");
            Method asInterfaceMethod = iHardwareServiceStubClass.getMethod("asInterface", IBinder.class);
            iMountService = asInterfaceMethod.invoke(null, hardwareService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MountUtils() {

    }

    public static boolean isUsbMassStorageConnectedV8() {
        try {
            Class<?> proxyClass = iMountServiceV8.getClass();
            Method method = proxyClass.getMethod("isUsbMassStorageConnected", new Class[]{});
            return (Boolean) method.invoke(iMountServiceV8, new Object[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isUsbMassStorageConnected() {
        try {
            Class<?> proxyClass = iMountService.getClass();
            Method method = proxyClass.getMethod("getMassStorageEnabled", new Class[]{});
            return (Boolean) method.invoke(iMountService, new Object[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void setUsbMassStorageEnabled(boolean b) {
        try {
            Class<?> proxyClass = iMountService.getClass();
            Method method = proxyClass.getMethod("setMassStorageEnabled", new Class[]{boolean.class});
            method.invoke(iMountService, new Object[]{b});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUsbMassStorageEnabledV8(boolean b) {
        try {
            Class<?> proxyClass = iMountServiceV8.getClass();
            Method method = proxyClass.getMethod("setUsbMassStorageEnabled", new Class[]{boolean.class});
            method.invoke(iMountServiceV8, new Object[]{b});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleMount(Context context) {
        boolean state = false;

        if (getMountState() == WidgetProviderUtil.STATE_ENABLED) {
            state = false;
        } else {
            Toast.makeText(context, R.string.update_state, Toast.LENGTH_LONG).show();
            state = true;
        }

        if (VERSION.SDK_INT >= 8) {
            setUsbMassStorageEnabledV8(state);
        } else {
            setUsbMassStorageEnabled(state);
        }
    }

    public static int getMountState() {
        String state = Environment.getExternalStorageState();

        if (state.equals("mounted")) {
            return WidgetProviderUtil.STATE_DISABLED;
        } else if (state.equals("shared")) {
            return WidgetProviderUtil.STATE_ENABLED;
        } else {
            return WidgetProviderUtil.STATE_INTERMEDIATE;
        }
    }
}
