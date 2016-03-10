package alei.switchpro.net;

import java.lang.reflect.Method;

import alei.switchpro.Constants;
import alei.switchpro.R;
import alei.switchpro.SwitchUtils;
import alei.switchpro.Utils;
import alei.switchpro.WidgetProviderUtil;
import alei.switchpro.apn.ApnUtils;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * @author Alei
 * 
 */
public class NetUtils
{

    public static void setMobileNetworkState(Context context, boolean state)
    {
        TelephonyManager sTelephonyManager = (TelephonyManager) context.getSystemService("phone");

        try
        {
            Method method = sTelephonyManager.getClass().getDeclaredMethod("getITelephony", new Class[] {});
            method.setAccessible(true);
            Object obj = method.invoke(sTelephonyManager, new Object[] {});

            if (state)
            {
                // invoke后返回ITelephony
                Method tmp = obj.getClass().getMethod("enableDataConnectivity", new Class[] {});
                tmp.invoke(obj, new Object[] {});
            }
            else
            {
                // invoke后返回ITelephony
                Method tmp = obj.getClass().getMethod("disableDataConnectivity", new Class[] {});
                tmp.invoke(obj, new Object[] {});
            }

            // 存储当前的状态，以便在重启以后恢复状态
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor configEditor = config.edit();
            configEditor.putBoolean(Constants.PREF_NET_STATE, state);
            configEditor.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void toggleMobileNetwork9(Context context)
    {
        ConnectivityManager sConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");

        // 如果用这个开关切换，先打开APN(sdk17 读取apn会报错)
        if (VERSION.SDK_INT < 17 && !ApnUtils.getApnState(context))
        {
            ApnUtils.setApnState(context, true);
        }

        try
        {
            Method setMethod = ConnectivityManager.class.getMethod("setMobileDataEnabled",
                    new Class[] { boolean.class });

            if (getMobileNetworkState(context))
            {
                setMethod.invoke(sConnectivityManager, new Object[] { false });
            }
            else
            {
                setMethod.invoke(sConnectivityManager, new Object[] { true });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void toggleMobileNetwork(Context context)
    {
        // 如果用这个开关切换，先打开APN
        if (!ApnUtils.getApnState(context))
        {
            ApnUtils.setApnState(context, true);
        }

        boolean state = getMobileNetworkState(context);

        if (state)
        {
            setMobileNetworkState(context, false);
        }
        else
        {
            if (SwitchUtils.getWifiState(context) == WidgetProviderUtil.STATE_ENABLED)
            {
                Toast.makeText(context, R.string.update_data_conn_err, Toast.LENGTH_LONG).show();
                Utils.updateWidget(context);
                return;
            }

            setMobileNetworkState(context, true);
        }

        Toast.makeText(context, R.string.update_data, Toast.LENGTH_LONG).show();
    }

    public static boolean getMobileNetworkState(Context context)
    {
        ConnectivityManager sConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");

        if (VERSION.SDK_INT <= 9)
        {
            NetworkInfo info = sConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (info == null)
            {
                return false;
            }

            if (info.getState() == NetworkInfo.State.DISCONNECTED)
            {
                return false;
            }
            else if (info.getState() == NetworkInfo.State.CONNECTED)
            {
                return true;
            }
        }
        else
        {
            try
            {
                Method getMethod = ConnectivityManager.class.getMethod("getMobileDataEnabled");
                return (Boolean) getMethod.invoke(sConnectivityManager, new Object[] {});
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static void initNetworkState(Context context)
    {
        // 存储当前的状态，以便在重启以后恢复状态
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

        if (config.contains(Constants.PREF_NET_STATE))
        {
            // 如果没获取到参数就认为是已经打开的，不做操作
            boolean state = config.getBoolean(Constants.PREF_NET_STATE, true);

            // 如果之前是关闭的,那在重启后关闭移动网络
            if (!state)
            {
                NetUtils.setMobileNetworkState(context, false);
            }
        }
    }

    public static void setSignalState(Context context, boolean state)
    {
        TelephonyManager sTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        ConnectivityManager sConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");

        try
        {
            if (VERSION.SDK_INT <= 9)
            {
                Method method = sTelephonyManager.getClass().getDeclaredMethod("getITelephony", new Class[] {});
                method.setAccessible(true);
                Object obj = method.invoke(sTelephonyManager, new Object[] {});
                Method tmp = obj.getClass().getMethod("setRadio", new Class[] { boolean.class });

                if (state)
                {
                    tmp.invoke(obj, new Object[] { true });
                }
                else
                {
                    tmp.invoke(obj, new Object[] { false });
                }
            }
            else
            {
                Method setMethod = ConnectivityManager.class.getMethod("setRadio", new Class[] { int.class,
                        boolean.class });

                if (state)
                {
                    setMethod.invoke(sConnectivityManager, new Object[] { 0, true });
                }
                else
                {
                    setMethod.invoke(sConnectivityManager, new Object[] { 0, false });
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean getSignalState(Context context)
    {
        TelephonyManager sTelephonyManager = (TelephonyManager) context.getSystemService("phone");

        try
        {
            Method method = sTelephonyManager.getClass().getDeclaredMethod("getITelephony", new Class[] {});
            method.setAccessible(true);
            Object obj = method.invoke(sTelephonyManager, new Object[] {});
            Method tmp = obj.getClass().getMethod("isRadioOn", new Class[] {});

            return ((Boolean) tmp.invoke(obj, new Object[] {})).booleanValue();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public static void toggleSignal(Context context)
    {
        if (getSignalState(context))
        {
            setSignalState(context, false);
        }
        else
        {
            setSignalState(context, true);
        }
    }

    public static int getNetworkType(Context context)
    {
        TelephonyManager sTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        return sTelephonyManager.getNetworkType();
    }
}
