package alei.switchpro.net;

import alei.switchpro.Constants;
import alei.switchpro.Utils;
import alei.switchpro.WidgetProviderUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;

public class NetStateListener extends PhoneStateListener {
    private Context context;

    public NetStateListener(Context context) {
        this.context = context;
    }

    @Override
    public void onDataConnectionStateChanged(int state) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

        // ���û��ȡ����������Ϊ���Ѿ��򿪵ģ���������
        boolean oldState = config.getBoolean(Constants.PREF_NET_STATE, true);

        // ��������Ǵӱ�����������ҵ�ǰ�ǿ��ŵģ���֮ǰ�洢��״̬�ǹرյ�
        if (!WidgetProviderUtil.dataConnectionFlag && NetUtils.getMobileNetworkState(context) && !oldState) {
            NetUtils.setMobileNetworkState(context, false);
        }

        WidgetProviderUtil.dataConnectionFlag = false;
        super.onDataConnectionStateChanged(state);
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        // ֪ͨwidget����
        Utils.updateWidget(context);
        super.onServiceStateChanged(serviceState);
    }
}