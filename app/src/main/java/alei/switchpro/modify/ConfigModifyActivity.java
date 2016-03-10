package alei.switchpro.modify;

import alei.switchpro.Constants;
import alei.switchpro.R;
import alei.switchpro.Utils;
import alei.switchpro.WidgetConfigBaseActivity;
import alei.switchpro.WidgetProviderUtil;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class ConfigModifyActivity extends WidgetConfigBaseActivity
{
    private String btnIds;
    private int size;

    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        Intent launchIntent = getIntent();
        widgetId = launchIntent.getIntExtra("widgetId", 0);
        size = launchIntent.getIntExtra("size", 0);

        // ֪ͨ���еĲ���size��0
        if (widgetId == 0 || size < 0 || size > 5)
        {
            return;
        }

        // ��ʼ������
        setContentView(R.layout.activity_widget_conf);
        addPreferencesFromResource(R.xml.pref_widget_conf);

        // ��ȡ��ǰ����������
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        // ��ȡ��ť��˳��
        btnIds = config.getString(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetId), DEFAULT_BUTTON_IDS);
        // ���ø��෽������ʼ������ͳ�ʼֵ
        if (config.contains(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId)))
        {
            backBitmap = WidgetProviderUtil.getBackgroundBitmap(this, widgetId, config);
        }

        createAction(widgetId);
        setTitle(R.string.modify_widget);
    }

    protected void updateWidget(int appWidgetId)
    {
        Intent launchIntent = new Intent();
        launchIntent.setClassName(this, "alei.switchpro.WidgetProviderX" + size);
        launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // ͨ�����ò�ͬ��Data�����ÿ��Intent����Ψһ�ģ����ᱻ�໥����
        launchIntent.setData(Uri.withAppendedPath(Uri.parse(WidgetProviderUtil.URI_SCHEME + "://widget/id/"),
                String.valueOf(appWidgetId)));
        PendingIntent newIntent = PendingIntent.getBroadcast(this, 0, launchIntent, 0);

        try
        {
            newIntent.send();
        }
        catch (CanceledException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * ��ȡ���һ�����õİ�ť˳�����û�еĻ�����һ��Ĭ��˳������Ҫ����
     * 
     * @return
     */
    protected String getLastBtnOrder()
    {
        return btnIds;
    }

    @Override
    protected int getWidgetSize()
    {
        return size;
    }

    protected void saveBtnAction()
    {
        super.saveBtnAction();
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        saveCfgToSD(config, true,
                config.getString(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetId), null));

        String[] notificationWidgetIds = config.getString(Constants.PREFS_IN_NOTIFICATION_BAR, "").split(",");
        boolean isExist = false;

        for (int i = 0; i < notificationWidgetIds.length; i++)
        {
            if (!notificationWidgetIds[i].equals("") && widgetId == Integer.parseInt(notificationWidgetIds[i]))
            {
                isExist = true;
                break;
            }
        }

        if (!isExist)
        {
            // ת������Ӧ��widget���и���
            updateWidget(widgetId);
        }
        else
        {
            Utils.updateNotification(this, widgetId);
        }
    }
}
