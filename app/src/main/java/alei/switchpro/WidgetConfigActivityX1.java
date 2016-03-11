package alei.switchpro;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class WidgetConfigActivityX1 extends WidgetConfigBaseActivity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_widget_conf);
        addPreferencesFromResource(R.xml.pref_widget_conf);

        // 从启动这个活动的意图中获取App Widget ID
        Intent launchIntent = getIntent();
        Bundle extras = launchIntent.getExtras();

        // 弹出这个配置界面的widget的ID
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        // 转入此配置界面必须带widgetId参数
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        createAction(appWidgetId);
    }

    protected void updateWidget(int appWidgetId) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(this, WidgetProviderX1.class);
        launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // 通过设置不同的Data来标记每个Intent都是唯一的，不会被相互覆盖
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
     * 获取最后一次配置的按钮顺序，如果没有的话返回一个默认顺序，子类要覆盖
     *
     * @return
     */
    protected String getLastBtnOrder() {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        String btnOrder = config.getString(Constants.PREFS_LAST_BUTTONS_ORDER, "0,2");
        return btnOrder;
    }

    @Override
    protected int getWidgetSize() {
        return 1;
    }

    protected void saveBtnAction() {
        super.saveBtnAction();
        saveCfgToSD(PreferenceManager.getDefaultSharedPreferences(this), false, null);
        // 转交给相应的widget进行更新
        updateWidget(widgetId);
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
    }
}
