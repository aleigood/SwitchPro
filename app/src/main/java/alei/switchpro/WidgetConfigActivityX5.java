package alei.switchpro;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class WidgetConfigActivityX5 extends WidgetConfigBaseActivity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_widget_conf);
        addPreferencesFromResource(R.xml.pref_widget_conf);

        // ��������������ͼ�л�ȡApp Widget ID
        Intent launchIntent = getIntent();
        Bundle extras = launchIntent.getExtras();

        // ����������ý����widget��ID
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        // ת������ý�������widgetId����
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        createAction(appWidgetId);
    }

    protected void updateWidget(int appWidgetId) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(this, WidgetProviderX5.class);
        launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // ÿ��Intent����Ψһ�ģ����ᱻ�໥����
        launchIntent.setData(Uri.withAppendedPath(Uri.parse(WidgetProviderUtil.URI_SCHEME + "://widget/id/"),
                String.valueOf(appWidgetId)));
        PendingIntent newIntent = PendingIntent.getBroadcast(this, 0, launchIntent, 0);

        try {
            newIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ȡ���һ�����õİ�ť˳�����û�еĻ�����һ��Ĭ��˳������Ҫ����
     *
     * @return
     */
    protected String getLastBtnOrder() {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        String btnOrder = config.getString(Constants.PREFS_LAST_BUTTONS_ORDER, "0,2,3,4,6,25,1,7");
        return btnOrder;
    }

    @Override
    protected int getWidgetSize() {
        return 5;
    }

    protected void saveBtnAction() {
        super.saveBtnAction();
        saveCfgToSD(PreferenceManager.getDefaultSharedPreferences(this), false, null);
        // ת������Ӧ��widget���и���
        updateWidget(widgetId);
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
    }
}
