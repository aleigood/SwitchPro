package alei.switchpro;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import java.io.InputStream;
import java.lang.reflect.Field;

public class Utils {
    public static final int PRIORITY_MAX = 2;
    public static final int PRIORITY_MIN = -2;
    public static final int ICON_DP_WIDTH = 32;
    public static final int ICON_DP_HEIGHT = 32;
    public static final int IND_DP_WIDTH = 8;
    public static final int IND_DP_HEIGHT = 8;

    public static Bitmap getBitmapFromResource(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        BitmapDrawable bmpDraw = new BitmapDrawable(is);
        return bmpDraw.getBitmap();
    }

    public static Bitmap setIconColor(Context context, int source, Integer alpha, Integer color) {
        return setBitmapColor(context, source, alpha, color, ICON_DP_WIDTH, ICON_DP_HEIGHT);
    }

    public static Bitmap setIconColor(Context context, Drawable source, Integer alpha, Integer color) {
        return setBitmapColor(context, source, alpha, color, ICON_DP_WIDTH, ICON_DP_HEIGHT);
    }

    public static Bitmap setIconColor(Context context, Bitmap source, Integer alpha, Integer color) {
        return setBitmapColor(context, source, alpha, color, ICON_DP_WIDTH, ICON_DP_HEIGHT);
    }

    public static Bitmap setIndColor(Context context, int source, Integer alpha, Integer color) {
        return setBitmapColor(context, source, alpha, color, IND_DP_WIDTH, IND_DP_HEIGHT);
    }

    public static Bitmap setIndColor(Context context, Bitmap source, Integer alpha, Integer color) {
        return setBitmapColor(context, source, alpha, color, IND_DP_WIDTH, IND_DP_HEIGHT);
    }

    /**
     * Color ����Ϊ�գ�Ϊ��ʱ��ʾ������ɫ�˾�
     */
    private static Bitmap setBitmapColor(Context context, int source, Integer alpha, Integer color, int width,
                                         int height) {
        return setBitmapColor(context, context.getResources().getDrawable(source), alpha, color, width, height);
    }

    private static Bitmap setBitmapColor(Context context, Bitmap source, Integer alpha, Integer color, int width,
                                         int height) {
        return setBitmapColor(context, new BitmapDrawable(source), alpha, color, width, height);
    }

    private static Bitmap setBitmapColor(Context context, Drawable drawable, Integer alpha, Integer color, int width,
                                         int height) {
        int iWidth = dip2px(context, width);
        int iHeight = dip2px(context, height);

        Bitmap newBitmap = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);

        if (color != null) {
            ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
            drawable.setColorFilter(filter);
        } else {
            drawable.clearColorFilter();
        }

        if (alpha != null) {
            drawable.setAlpha(alpha);
        } else {
            drawable.setAlpha(255);
        }

        drawable.setBounds(0, 0, iWidth, iHeight);
        drawable.draw(canvas);
        return newBitmap;
    }

    public static Bitmap createBitmap(int color) {
        Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
        return Bitmap.createBitmap(new int[]{color}, 1, 1, localConfig);
    }

    public static int setAlpha(int color, boolean trans) {
        int alpha = Color.alpha(trans ? 0x00000000 : 0xFF000000);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static void updateWidget(Context context) {
        // ֪ͨwidget����
        Intent intent = new Intent("alei.switchpro.APPWIDGET_UPDATE");
        PendingIntent newIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            newIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }

        // ����֪ͨ���е�widget
        updateAllNotification(context);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void updateAllNotification(Context context) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        String[] notificationWidgetIds = config.getString(Constants.PREFS_IN_NOTIFICATION_BAR, "").split(",");

        for (int i = 0; i < notificationWidgetIds.length; i++) {
            if (!notificationWidgetIds[i].equals("")) {
                int widgetId = Integer.parseInt(notificationWidgetIds[i]);
                Utils.updateNotification(context, widgetId);
            }
        }
    }

    public static void updateNotification(Context context, int widgetId) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = SwitchUtils.notifications.get(widgetId) != null ? SwitchUtils.notifications
                .get(widgetId) : new Notification();

        if (VERSION.SDK_INT >= 16) {
            int priority = Integer.parseInt(config.getString(Constants.PREFS_NOTIFY_PRIORITY, "1"));

            try {
                Field field = notification.getClass().getDeclaredField("priority");
                field.set(notification, priority == 1 ? PRIORITY_MAX : PRIORITY_MIN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        // align left (0) / right (max) in status bar
        notification.when = 0;

        if (config.getBoolean(Constants.PREFS_SHOW_NOTIFY_ICON, true)
                && (widgetId + "").equals(config.getString(Constants.PREFS_LASE_NOTIFY_WIDGET, ""))) {
            notification.icon = config.getString(Constants.PREFS_NOTIFY_ICON_COLOR, "1").equals("1") ? R.drawable.notify_battery_white_icon
                    : R.drawable.notify_battery_icon;
            notification.iconLevel = config.getInt(Constants.PREFS_BATTERY_LEVEL, 0);
        } else {
            notification.icon = R.drawable.ic_notify;
        }

        notification.contentView = WidgetProviderUtil.buildAndUpdateButtons(context, widgetId, config, null);

        // �����nobg�Ļ��͸��ı���������Ϊ�޿�ı���
        String layoutName = config.getString(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, widgetId),
                context.getString(R.string.list_pre_bg_default));
        int layoutId = WidgetProviderUtil.getLayoutId(context, layoutName);

        if ((layoutId == R.layout.view_widget_nobg || layoutId == R.layout.view_widget_custom)
                && notification.contentView != null) {
            notification.contentView.setInt(R.id.top, "setBackgroundResource", R.drawable.bg_none);
        }

        try {
            notificationManager.notify(widgetId, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwitchUtils.notifications.put(widgetId, notification);
    }

    public static Bitmap createUsageIcon(Context context, float percentage) {
        int offset = dip2px(context, 6);
        int iWidth = dip2px(context, ICON_DP_WIDTH);
        int iHeight = dip2px(context, ICON_DP_HEIGHT);
        Bitmap bitmap = Bitmap.createBitmap(iWidth, iHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paints = new Paint();
        paints.setAntiAlias(true);
        paints.setColor(0xFFFFFFFF);
        paints.setStrokeWidth(dip2px(context, 2));
        paints.setStyle(Paint.Style.STROKE);
        RectF rect = new RectF(offset, offset, iWidth - offset, iHeight - offset);
        canvas.drawArc(rect, 0, 360, false, paints);
        paints.setStrokeWidth(0);
        paints.setStyle(Paint.Style.FILL);
        canvas.drawArc(rect, 270, 360 * percentage, true, paints);
        return bitmap;
    }

    public static PowerManager.WakeLock getWakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "SwitchPro");
    }

    public static boolean isAppExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }

        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

}
