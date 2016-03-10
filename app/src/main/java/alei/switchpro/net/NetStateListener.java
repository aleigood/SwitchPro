package alei.switchpro.net;

import alei.switchpro.Constants;
import alei.switchpro.Utils;
import alei.switchpro.WidgetProviderUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;

public class NetStateListener extends PhoneStateListener
{
    private Context context;

    public NetStateListener(Context context)
    {
        this.context = context;
    }

    @Override
    public void onDataConnectionStateChanged(int state)
    {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

        // 如果没获取到参数就认为是已经打开的，不做操作
        boolean oldState = config.getBoolean(Constants.PREF_NET_STATE, true);

        // 如果不是是从本软件触发，且当前是开着的，且之前存储的状态是关闭的
        if (!WidgetProviderUtil.dataConnectionFlag && NetUtils.getMobileNetworkState(context) && !oldState)
        {
            NetUtils.setMobileNetworkState(context, false);
        }

        WidgetProviderUtil.dataConnectionFlag = false;
        super.onDataConnectionStateChanged(state);
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState)
    {
        // 通知widget更新
        Utils.updateWidget(context);
        super.onServiceStateChanged(serviceState);
    }
}