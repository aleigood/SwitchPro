package alei.switchpro;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class WidgetProviderX4 extends AppWidgetProvider {
    /**
     * ɾ��ÿ��widget��Ӧ�Ĳ���
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        WidgetProviderUtil.onDeleted(context, appWidgetIds);
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * ���հ�ť�¼�����������״̬������¼�
     *
     * @param context
     * @param intent  Indicates the pressed button.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        WidgetProviderUtil.updateAction(context, intent, WidgetProviderX4.class);
        super.onReceive(context, intent);
    }
}
