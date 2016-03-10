package alei.switchpro;

import alei.switchpro.net.NetStateListener;
import alei.switchpro.net.NetUtils;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.WindowManager;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static MainBrocastReceiver mainReceiver = new MainBrocastReceiver();
    private DatabaseOper dbOper;
    private WindowManager.LayoutParams mWindowParams;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        dbOper = new DatabaseOper(this);
        mWindowParams = new WindowManager.LayoutParams(1, 1, 0x7d3, 0x50128, -0x3);
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = 0;
        mWindowParams.windowAnimations = 0;

        registerReceiver(mainReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));

        // �������������ʼ������״̬����ΪĬ�������󶼻����������
        NetUtils.initNetworkState(this);

        // ��ʼ����������״̬�ļ�����
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgr.listen(new NetStateListener(this), PhoneStateListener.LISTEN_SERVICE_STATE
                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dbOper.close();

        unregisterReceiver(mainReceiver);
    }

    public DatabaseOper getDataOper() {
        return dbOper;
    }

    public WindowManager.LayoutParams getWindowParams() {
        return mWindowParams;
    }
}
