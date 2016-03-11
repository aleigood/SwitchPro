package alei.switchpro;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class WidgetProviderX5 extends AppWidgetProvider {
    /**
     * 删除每个widget对应的参数
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
     * 接收按钮事件和其他设置状态变更的事件
     *
     * @param context
     * @param intent  Indicates the pressed button.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        WidgetProviderUtil.updateAction(context, intent, WidgetProviderX5.class);
        super.onReceive(context, intent);
    }
}
