package alei.switchpro.apn;

import alei.switchpro.net.NetUtils;
import android.content.Context;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class ApnUtils
{
    static final String SETTINGS_KEEP_MMS_ACTIVE = "apn_mms_enabled";
    static final String SETTING_PREFERRED_APN = "preferred_apn_id";

    public static ConnectivityManager sConnectivityManager;
    public static TelephonyManager sTelephonyManager;

    public static final int OFF = 0;
    public static final int ON = 1;

    public static boolean getApnState(Context context)
    {
        try
        {
            ApnDao dao = new ApnDao(context.getContentResolver());
            return dao.getApnState() == ON;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Convinience method for switching apn state to another state (based on
     * current system state). It performs switch and send notification about it
     * by sending broadcast message. As a result method also returns current apn
     * state. If you does not need some special logic for switching it's the
     * best way.
     * 
     * @param context
     *            current application context
     * @return current apn state after switch procedure.
     */
    public static void switchAndNotify(Context context)
    {
        try
        {
            // 如果数据连接是关闭的要先打开
            if (!NetUtils.getMobileNetworkState(context))
            {
                NetUtils.setMobileNetworkState(context, true);
            }

            // 这个参数暂时不支持
            ApnDao dao = new ApnDao(context.getContentResolver());
            dao.setDisableAllApns(false);
            int currentState = dao.getApnState();

            if (currentState == ON)
            {
                setApnState(context, false);
            }
            else
            {
                setApnState(context, true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void setApnState(Context context, boolean b)
    {
        try
        {
            // 这个参数暂时不支持
            ApnDao dao = new ApnDao(context.getContentResolver());
            dao.setDisableAllApns(false);
            switchAndNotify(b ? ON : OFF, ON, context, dao);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Performs direct switching to passed target state. This method should be
     * used if you already has apnDao. Passing existing dao helps to avoid
     * creating a new one
     * 
     * @param targetState
     *            target state
     * @param mmsTarget
     *            mmsTarget state
     * @param showNotification
     *            show notification on success switch
     * @param context
     *            application context
     * @param dao
     *            apn dao.
     * @return {@code true} if switch was successfull and {@code false}
     *         otherwise
     */
    private static boolean switchAndNotify(int targetState, int mmsTarget, Context context, ApnDao dao)
    {
        int onState = ON;
        dao.setMmsTarget(mmsTarget);
        // this var is used for storing preferred apn in switch on->off, and as
        // a container for restoring id in switch off->on
        long preferredApnId = -1;

        if (targetState == onState)
        {
            preferredApnId = PreferenceManager.getDefaultSharedPreferences(context).getLong(SETTING_PREFERRED_APN, -1);
        }
        else
        {
            preferredApnId = dao.getPreferredApnId();
        }
        boolean success = dao.switchApnState(targetState);

        if (success)
        {
            if (targetState != onState)
            {
                storeMmsSettings(context, mmsTarget);
                // storing preferred apn id
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putLong(SETTING_PREFERRED_APN, preferredApnId).commit();
            }
            else
            {
                // reinitializing preferred apn
                tryFixConnection(dao, preferredApnId);
            }
        }

        return success;
    }

    private static void storeMmsSettings(Context context, int mmsTarget)
    {
        boolean keepMmsActive = mmsTarget == ON;
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(SETTINGS_KEEP_MMS_ACTIVE, keepMmsActive).commit();
    }

    private static void tryFixConnection(ApnDao dao, long preferredApn)
    {
        if (preferredApn != -1)
        {
            dao.restorePreferredApn(preferredApn);
        }
        else
        {
            // we does not have preferred apn now, so lets try to set some
            // random data apn
            long apnId = dao.getRandomCurrentDataApn();
            if (apnId != -1)
            {
                dao.restorePreferredApn(apnId);
            }
        }
    }
}
