package alei.switchpro;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class WidgetConfigActivityNotify extends WidgetConfigBaseActivity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_widget_conf);
        addPreferencesFromResource(R.xml.pref_widget_conf);
        createAction((int) System.currentTimeMillis() / 1000);
    }

    protected void updateWidget(int appWidgetId) {
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
        return 4;
    }

    protected void saveBtnAction() {
        super.saveBtnAction();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (sp.contains(Constants.PREFS_IN_NOTIFICATION_BAR)) {
            sp.edit()
                    .putString(Constants.PREFS_IN_NOTIFICATION_BAR,
                            sp.getString(Constants.PREFS_IN_NOTIFICATION_BAR, "") + "," + widgetId)
                    .putString(Constants.PREFS_LASE_NOTIFY_WIDGET, "" + widgetId).commit();
        } else {
            sp.edit().putString(Constants.PREFS_IN_NOTIFICATION_BAR, "" + widgetId)
                    .putString(Constants.PREFS_LASE_NOTIFY_WIDGET, "" + widgetId).commit();
        }

        saveCfgToSD(sp, false, null);
        Utils.updateNotification(this, widgetId);
        Toast.makeText(this, R.string.notification_created, Toast.LENGTH_SHORT).show();

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_CANCELED, resultValue);
    }
}
