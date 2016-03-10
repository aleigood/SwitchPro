package alei.switchpro;

import alei.switchpro.battery.BatteryIndicatorUtils;
import alei.switchpro.load.XmlEntity;
import alei.switchpro.net.NetUtils;
import alei.switchpro.process.MemInfoReader;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.RemoteViews;

import java.io.FileNotFoundException;

public class WidgetProviderUtil {
    // ����״̬
    public static final int STATE_OTHER = -1;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_INTERMEDIATE = 2;

    public static final int IND_POS_LEFT = 1;
    public static final int IND_POS_CENTER = 2;
    public static final int IND_POS_RIGHT = 3;

    // Ĭ�ϵİ�ť˳��
    public static final String EXTRA_BUTTON_ID = "buttonId";
    public static final String URI_SCHEME = "SWITCH_PRO_WIDGET";
    public static volatile boolean dataConnectionFlag = false;
    // ��Ϊ�Ǳ�����򿪵ı�ǣ����Ϊtrue���ͱ�ʾ�Ǵӱ�widget�����ģ���Ҫ��ʾ���������ʾ
    public static volatile boolean scanMediaFlag = false;

    public static Bitmap BITMAP_WIFI;
    public static Bitmap BITMAP_EDGE;
    public static Bitmap BITMAP_BRIGHTNESS;
    public static Bitmap BITMAP_AUTO_BRIGHT;
    public static Bitmap BITMAP_SYNC;
    public static Bitmap BITMAP_GPS;
    public static Bitmap BITMAP_GRAVITY;
    public static Bitmap BITMAP_BLUETOOTH;
    public static Bitmap BITMAP_AIRPLANE;
    public static Bitmap BITMAP_SCREEN_TIMEOUT;
    public static Bitmap BITMAP_VIBRATE;
    public static Bitmap BITMAP_SILENT;
    public static Bitmap BITMAP_SCANMEDIA;
    public static Bitmap BITMAP_NET_SWITCH;
    public static Bitmap BITMAP_UNLOCK;
    public static Bitmap BITMAP_FLASHLIGHT;
    public static Bitmap BITMAP_WIMAX;
    public static Bitmap BITMAP_REBOOT;
    public static Bitmap BITMAP_SPEAKER;
    public static Bitmap BITMAP_AUTOLOCK;
    public static Bitmap BITMAP_WIFIAP;
    public static Bitmap BITMAP_WIFI_SLEEP;
    public static Bitmap BITMAP_USBTE;
    public static Bitmap BITMAP_MOUNT;
    public static Bitmap BITMAP_LOCK_SCREEN;
    public static Bitmap BITMAP_BATTERY;
    public static Bitmap BITMAP_VOLUME;
    public static Bitmap BITMAP_KILL_PROCESS;
    public static Bitmap BITMAP_MEMORY_USAGE;
    public static Bitmap BITMAP_STORAGE_USAGE;
    public static Bitmap BITMAP_BT_TE;
    public static Bitmap BITMAP_NFC;

    /**
     * ɾ��ÿ��widget��Ӧ�Ĳ���
     */
    public static void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // ɾ��ɾ��ÿ��widgetʵ���İ�ť����,�����ɾ��������޸�����ʾ����
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor configEditor = config.edit();

            // �����widget��ɾ����Ӧ����
            String[] notificationWidgetIds = config.getString(Constants.PREFS_IN_NOTIFICATION_BAR, "").split(",");
            boolean isExist = false;

            for (int i = 0; i < notificationWidgetIds.length; i++) {
                if (!notificationWidgetIds[i].equals("") && appWidgetId == Integer.parseInt(notificationWidgetIds[i])) {
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                String fileName = config.getString(
                        String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, appWidgetId), "");
                // ɾ������ͼƬ�ļ�
                context.deleteFile(fileName);

                configEditor.remove(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, appWidgetId));
                configEditor.remove(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, appWidgetId));
                configEditor.remove(String.format(Constants.PREFS_BACK_COLOR_FIELD_PATTERN, appWidgetId));
                configEditor.remove(String.format(Constants.PREFS_IND_COLOR_FIELD_PATTERN, appWidgetId));
                configEditor.remove(String.format(Constants.PREFS_ICON_COLOR_FIELD_PATTERN, appWidgetId));
                configEditor.remove(String.format(Constants.PREFS_ICON_TRANS_FIELD_PATTERN, appWidgetId));
                configEditor.remove(String.format(Constants.PREFS_DIVIDER_COLOR_FIELD_PATTERN, appWidgetId));
                configEditor.remove(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, appWidgetId));
                configEditor.commit();
            }
        }
    }

    public static void updateAction(Context context, Intent intent, Class<?> cls) {
        // long curTime = System.currentTimeMillis();
        performButtonEvent(context, intent);

        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

        // ���������¼���һ���ǽ��յ�״̬���µ���Ϣ������Ҫ�������е�widget
        int[] widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(intent.getComponent());

        for (int i = 0; i < widgetIds.length; i++) {
            // Views�п���Ϊ�գ����Ϊ��ֱ��������������
            RemoteViews views = buildAndUpdateButtons(context, widgetIds[i], config, cls);
            AppWidgetManager gm = AppWidgetManager.getInstance(context);

            if (views != null) {
                gm.updateAppWidget(widgetIds[i], views);
            }
        }
        // Log.d("TimeMillis", intent.getAction() + ":" +
        // (System.currentTimeMillis() - curTime));
    }

    public static void performButtonEvent(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);

        // ����ǰ�ť������¼�
        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE) && bundle != null) {
            int buttonId = bundle.getInt(EXTRA_BUTTON_ID, -1);

            if (config.getBoolean(Constants.PREFS_HAPTIC_FEEDBACK, true)) {
                Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
                vibrator.vibrate(25);
            }

            switch (buttonId) {
                case Constants.BUTTON_NET_SWITCH:
                    SwitchUtils.toggleNetSwitch(context);
                    break;
                case Constants.BUTTON_BATTERY:
                    SwitchUtils.toggleBattery(context);
                    break;
                case Constants.BUTTON_BRIGHTNESS:
                    SwitchUtils.toggleBirghtness(context);
                    break;
                case Constants.BUTTON_WIFI:
                    SwitchUtils.toggleWifi(context);
                    break;
                case Constants.BUTTON_EDGE:
                    dataConnectionFlag = true;
                    SwitchUtils.toggleApn(context);
                    return;
                case Constants.BUTTON_SYNC:
                    SwitchUtils.toggleSync(context);
                    break;
                case Constants.BUTTON_GPS:
                    SwitchUtils.toggleGps(context);
                    break;
                case Constants.BUTTON_BLUETOOTH:
                    SwitchUtils.toggleBluetooth(context);
                    break;
                case Constants.BUTTON_GRAVITY:
                    SwitchUtils.toggleGrayity(context);
                    break;
                case Constants.BUTTON_AIRPLANE:
                    SwitchUtils.toggleNetwork(context);
                    break;
                case Constants.BUTTON_VIBRATE:
                    SwitchUtils.toggleVibrate(context);
                    break;
                case Constants.BUTTON_SCANMEDIA:
                    scanMediaFlag = true;
                    SwitchUtils.scanMedia(context);
                    break;
                case Constants.BUTTON_SCREEN_TIMEOUT:
                    SwitchUtils.toggleScreenTimeout(context);
                    break;
                case Constants.BUTTON_UNLOCK:
                    SwitchUtils.toggleUnlockPattern(context);
                    break;
                case Constants.BUTTON_REBOOT:
                    SwitchUtils.rebootSystem(context);
                    break;
                case Constants.BUTTON_FLASHLIGHT:
                    SwitchUtils.toggleFlashlight(context);
                    break;
                case Constants.BUTTON_WIMAX:
                    SwitchUtils.toggleWimax(context);
                    break;
                case Constants.BUTTON_SPEAKER:
                    SwitchUtils.toggleSpeakMode(context);
                    break;
                case Constants.BUTTON_AUTOLOCK:
                    SwitchUtils.toggleAutoLock(context);
                    break;
                case Constants.BUTTON_WIFIAP:
                    SwitchUtils.toggleWifiAp(context);
                    break;
                case Constants.BUTTON_USBTE:
                    SwitchUtils.toggleUsbTether(context);
                    break;
                case Constants.BUTTON_MOUNT:
                    SwitchUtils.toggleMount(context);
                    break;
                case Constants.BUTTON_LOCK_SCREEN:
                    SwitchUtils.lockScreen(context);
                    break;
                case Constants.BUTTON_WIFI_SLEEP:
                    SwitchUtils.toggleWifiSleepPolicy(context);
                    break;
                case Constants.BUTTON_VOLUME:
                    SwitchUtils.setvolume(context);
                    break;
                case Constants.BUTTON_KILL_PROCESS:
                    SwitchUtils.killProcess(context);
                    break;
                case Constants.BUTTON_MEMORY_USAGE:
                    SwitchUtils.toggleMemory(context);
                    break;
                case Constants.BUTTON_STORAGE_USAGE:
                    SwitchUtils.toggleStorage(context);
                    break;
                case Constants.BUTTON_BT_TE:
                    SwitchUtils.toggleBlueToothTe(context);
                    break;
                case Constants.BUTTON_NFC:
                    SwitchUtils.toggleNFC(context);
                    break;
                default:
                    break;
            }
        }
    }

    public static RemoteViews buildAndUpdateButtons(Context context, XmlEntity entity) {
        return buildAndUpdateButtons(context, null, entity.getBtnIds(), entity.getLayoutName(), entity.getIconColor(),
                entity.getIconTrans(), entity.getIndColor(), entity.getDividerColor(), entity.getBackColor(), null);
    }

    public static RemoteViews buildAndUpdateButtons(Context context, Integer widgetId, String strBtnIds,
                                                    String layoutName, int iconColor, int iconTrans, int indColor, int dividerColor, int backColor,
                                                    Bitmap backImg) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        // ��ȡ���в���

        if (layoutName == null || layoutName.equals("")) {
            return null;
        }

        int layoutId = getLayoutId(context, layoutName);

        // ����RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        // ������Զ�����ɫ������ֱ��������ɫ
        updateBackground(context, widgetId, views, layoutName, backColor, backImg);
        updateDivider(context, views, layoutName, dividerColor);

        // ��ť��ɾ���˵�����
        if (strBtnIds == null || strBtnIds.trim().equals("")) {
            return views;
        }

        int[] btnIds = getButtonIdsFromStr(context, strBtnIds);
        buildButtons(views, context, btnIds, null, AppWidgetManager.INVALID_APPWIDGET_ID, layoutName, dividerColor);
        updateButtons(views, context, config, layoutName, btnIds, iconColor, iconTrans, indColor);
        return views;
    }

    /**
     * Load image for given widget and build {@link RemoteViews} for it.
     *
     * @param context
     * @param appWidgetId
     * @param config
     * @param cls         ����Ϊ��
     * @return
     */
    public static RemoteViews buildAndUpdateButtons(Context context, int appWidgetId, SharedPreferences config,
                                                    Class<?> cls) {
        // �ж��������Ƿ������WidgetId,��������ý�����Home���˳�ʱ���ᴴ��һ��WidgetId
        if (!config.contains(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, appWidgetId))) {
            return null;
        }

        // ����Layout
        String layoutName = config.getString(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, appWidgetId),
                context.getString(R.string.list_pre_bg_default));
        int layoutId = getLayoutId(context, layoutName);

        // ��ȡ��ťID
        String buttonIdsStr = config.getString(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, appWidgetId),
                WidgetConfigBaseActivity.DEFAULT_BUTTON_IDS);

        if (buttonIdsStr == null || buttonIdsStr.trim().equals("")) {
            return null;
        }

        int[] buttonIds = getButtonIdsFromStr(context, buttonIdsStr);

        // ��ȡָʾ������ɫ
        int indColor = config.getInt(String.format(Constants.PREFS_IND_COLOR_FIELD_PATTERN, appWidgetId),
                Constants.IND_COLOR_DEFAULT);

        // ��ȡͼ����ɫ
        int iconColor = config
                .getInt(String.format(Constants.PREFS_ICON_COLOR_FIELD_PATTERN, appWidgetId), Color.WHITE);

        // ��ȡ�ָ�����ɫ
        int dividerColor = config.getInt(String.format(Constants.PREFS_DIVIDER_COLOR_FIELD_PATTERN, appWidgetId),
                Constants.DEFAULT_DEVIDER_COLOR);

        // ��ȡ͸������
        int transPref = config.getInt(String.format(Constants.PREFS_ICON_TRANS_FIELD_PATTERN, appWidgetId), 50);

        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        updateBackground(context, appWidgetId, config, views, layoutName);
        updateDivider(context, views, layoutName, dividerColor);
        // �������а�ť
        buildButtons(views, context, buttonIds, cls, appWidgetId, layoutName, dividerColor);
        // �������а�ť
        updateButtons(views, context, config, layoutName, buttonIds, iconColor, transPref, indColor);
        return views;
    }

    public static int getLayoutId(Context context, String layoutName) {
        int layoutId = R.layout.view_widget;

        if (layoutName.equals(context.getString(R.string.list_pre_bg_custom))) {
            layoutId = R.layout.view_widget_custom;
        }

        if (layoutName.equals(context.getString(R.string.list_pre_bg_custom_shadow))) {
            layoutId = R.layout.view_widget_shadow;
        }

        if (layoutName.equals(context.getString(R.string.list_pre_bg_none))) {
            layoutId = R.layout.view_widget_nobg;
        }

        return layoutId;
    }

    public static Bitmap getBackgroundBitmap(Context context, int appWidgetId, SharedPreferences config) {
        try {
            return BitmapFactory.decodeStream(context.openFileInput(config.getString(
                    String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, appWidgetId), "")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateBackground(Context context, Integer widgetId, RemoteViews views, String layoutName,
                                         int pBackColor, Bitmap backImg) {
        if (layoutName.equals(context.getString(R.string.list_pre_bg_custom))
                || layoutName.equals(context.getString(R.string.list_pre_bg_custom_shadow))) {
            if (backImg == null) {
                int backColor = pBackColor == 0 ? Constants.DEFAULT_BACKGROUND_COLOR : pBackColor;
                views.setImageViewBitmap(R.id.custom_img, Utils.createBitmap(backColor));
            } else {
                views.setImageViewBitmap(R.id.custom_img, backImg);
            }
        } else if ((layoutName.equals(context.getString(R.string.list_pre_bg_default)) || layoutName.equals(context
                .getString(R.string.list_pre_bg_white))) && VERSION.SDK_INT >= 10) {
            views.setInt(R.id.custom_img, "setAlpha", pBackColor);
        }
    }

    private static void updateBackground(Context context, Integer appWidgetId, SharedPreferences config,
                                         RemoteViews views, String layoutName) {
        String backColorKey = String.format(Constants.PREFS_BACK_COLOR_FIELD_PATTERN, appWidgetId);
        String backImgKey = String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, appWidgetId);

        if (layoutName.equals(context.getString(R.string.list_pre_bg_custom))
                || layoutName.equals(context.getString(R.string.list_pre_bg_custom_shadow))) {
            // ����õ��Ǳ���ͼƬ����Ҳ�ᱣ��һ��Ĭ�ϵı�����ɫ����������б���ͼƬ������������
            if (config.contains(backImgKey)) {
                Bitmap backBitmap = getBackgroundBitmap(context, appWidgetId, config);

                // ��������ȡͼƬ
                if (backBitmap != null) {
                    views.setImageViewBitmap(R.id.custom_img, backBitmap);
                } else {
                    views.setImageViewBitmap(R.id.custom_img, Utils.createBitmap(Constants.DEFAULT_BACKGROUND_COLOR));
                }
            } else {
                views.setImageViewBitmap(R.id.custom_img,
                        Utils.createBitmap(config.getInt(backColorKey, Constants.DEFAULT_BACKGROUND_COLOR)));
            }
        } else if ((layoutName.equals(context.getString(R.string.list_pre_bg_default)) || layoutName.equals(context
                .getString(R.string.list_pre_bg_white))) && VERSION.SDK_INT >= 10) {
            views.setInt(R.id.custom_img, "setAlpha", config.getInt(backColorKey, 255));
        }
    }

    private static void updateDivider(Context context, RemoteViews views, String layoutName, int dividerColor) {
        if (layoutName.equals(context.getString(R.string.list_pre_bg_custom))
                || layoutName.equals(context.getString(R.string.list_pre_bg_custom_shadow))) {
            Bitmap dividerImg = Utils.createBitmap(dividerColor);
            views.setImageViewBitmap(R.id.div_1, dividerImg);
            views.setImageViewBitmap(R.id.div_2, dividerImg);
            views.setImageViewBitmap(R.id.div_3, dividerImg);
            views.setImageViewBitmap(R.id.div_4, dividerImg);
            views.setImageViewBitmap(R.id.div_5, dividerImg);
            views.setImageViewBitmap(R.id.div_6, dividerImg);
            views.setImageViewBitmap(R.id.div_7, dividerImg);
            views.setImageViewBitmap(R.id.div_8, dividerImg);
            views.setImageViewBitmap(R.id.div_9, dividerImg);
            views.setImageViewBitmap(R.id.div_10, dividerImg);
            views.setImageViewBitmap(R.id.div_11, dividerImg);
            views.setImageViewBitmap(R.id.div_12, dividerImg);
            views.setImageViewBitmap(R.id.div_13, dividerImg);
            views.setImageViewBitmap(R.id.div_14, dividerImg);
            views.setImageViewBitmap(R.id.div_15, dividerImg);
            views.setImageViewBitmap(R.id.div_16, dividerImg);
            views.setImageViewBitmap(R.id.div_17, dividerImg);
            views.setImageViewBitmap(R.id.div_18, dividerImg);
            views.setImageViewBitmap(R.id.div_19, dividerImg);
        } else if (layoutName.equals(context.getString(R.string.list_pre_bg_white))) {
            views.setImageViewResource(R.id.custom_img, R.drawable.bg_w);
            views.setImageViewResource(R.id.div_1, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_2, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_3, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_4, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_5, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_6, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_7, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_8, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_9, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_10, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_11, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_12, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_13, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_14, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_15, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_16, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_17, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_18, R.drawable.divider_w);
            views.setImageViewResource(R.id.div_19, R.drawable.divider_w);
        } else if (layoutName.equals(context.getString(R.string.list_pre_bg))) {
            views.setImageViewResource(R.id.custom_img, R.drawable.bg);
            views.setImageViewResource(R.id.div_1, R.drawable.divider);
            views.setImageViewResource(R.id.div_2, R.drawable.divider);
            views.setImageViewResource(R.id.div_3, R.drawable.divider);
            views.setImageViewResource(R.id.div_4, R.drawable.divider);
            views.setImageViewResource(R.id.div_5, R.drawable.divider);
            views.setImageViewResource(R.id.div_6, R.drawable.divider);
            views.setImageViewResource(R.id.div_7, R.drawable.divider);
            views.setImageViewResource(R.id.div_8, R.drawable.divider);
            views.setImageViewResource(R.id.div_9, R.drawable.divider);
            views.setImageViewResource(R.id.div_10, R.drawable.divider);
            views.setImageViewResource(R.id.div_11, R.drawable.divider);
            views.setImageViewResource(R.id.div_12, R.drawable.divider);
            views.setImageViewResource(R.id.div_13, R.drawable.divider);
            views.setImageViewResource(R.id.div_14, R.drawable.divider);
            views.setImageViewResource(R.id.div_15, R.drawable.divider);
            views.setImageViewResource(R.id.div_16, R.drawable.divider);
            views.setImageViewResource(R.id.div_17, R.drawable.divider);
            views.setImageViewResource(R.id.div_18, R.drawable.divider);
            views.setImageViewResource(R.id.div_19, R.drawable.divider);
        }
    }

    /**
     * ���찴ť
     *
     * @param views
     * @param context
     * @param buttonIds
     * @param cls         ����Ϊ�գ�Ϊ��ʱ������ÿ����ť����Ӧ�¼�
     * @param appWidgetId ����Ϊ�գ�Ϊ��ʱ������ÿ����ť����Ӧ�¼�
     * @return
     */
    private static RemoteViews buildButtons(RemoteViews views, Context context, int[] buttonIds, Class<?> cls,
                                            Integer appWidgetId, String layoutName, int dividerColor) {
        switch (buttonIds.length) {
            case 1:
                // ���ص�2-7����ť
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.GONE);
                views.setViewVisibility(R.id.btn_15, View.GONE);
                views.setViewVisibility(R.id.btn_16, View.GONE);
                views.setViewVisibility(R.id.btn_17, View.GONE);
                views.setViewVisibility(R.id.btn_18, View.GONE);
                views.setViewVisibility(R.id.btn_19, View.GONE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);
                views.setViewVisibility(R.id.div_14, View.GONE);
                views.setViewVisibility(R.id.div_15, View.GONE);
                views.setViewVisibility(R.id.div_16, View.GONE);
                views.setViewVisibility(R.id.div_17, View.GONE);
                views.setViewVisibility(R.id.div_18, View.GONE);
                views.setViewVisibility(R.id.div_19, View.GONE);
                break;
            case 2:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.GONE);
                views.setViewVisibility(R.id.btn_15, View.GONE);
                views.setViewVisibility(R.id.btn_16, View.GONE);
                views.setViewVisibility(R.id.btn_17, View.GONE);
                views.setViewVisibility(R.id.btn_18, View.GONE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);
                views.setViewVisibility(R.id.div_14, View.GONE);
                views.setViewVisibility(R.id.div_15, View.GONE);
                views.setViewVisibility(R.id.div_16, View.GONE);
                views.setViewVisibility(R.id.div_17, View.GONE);
                views.setViewVisibility(R.id.div_18, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 3:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.GONE);
                views.setViewVisibility(R.id.btn_15, View.GONE);
                views.setViewVisibility(R.id.btn_16, View.GONE);
                views.setViewVisibility(R.id.btn_17, View.GONE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);
                views.setViewVisibility(R.id.div_14, View.GONE);
                views.setViewVisibility(R.id.div_15, View.GONE);
                views.setViewVisibility(R.id.div_16, View.GONE);
                views.setViewVisibility(R.id.div_17, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 4:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.GONE);
                views.setViewVisibility(R.id.btn_15, View.GONE);
                views.setViewVisibility(R.id.btn_16, View.GONE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);
                views.setViewVisibility(R.id.div_14, View.GONE);
                views.setViewVisibility(R.id.div_15, View.GONE);
                views.setViewVisibility(R.id.div_16, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 5:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.GONE);
                views.setViewVisibility(R.id.btn_15, View.GONE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);
                views.setViewVisibility(R.id.div_14, View.GONE);
                views.setViewVisibility(R.id.div_15, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 6:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));

                // ���صڶ�����ť
                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.GONE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);
                views.setViewVisibility(R.id.div_14, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);

                } else {
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 7:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 8:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 9:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 10:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 11:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 12:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 13:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 14:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_7,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[13], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.VISIBLE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_7, View.GONE);
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_7, View.VISIBLE);
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 15:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_6,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_7,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[13], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[14], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.VISIBLE);
                views.setViewVisibility(R.id.btn_7, View.VISIBLE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_6, View.GONE);
                    views.setViewVisibility(R.id.div_7, View.GONE);
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_6, View.VISIBLE);
                    views.setViewVisibility(R.id.div_7, View.VISIBLE);
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 16:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_5,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_6,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_7,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[13], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[14], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[15], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.VISIBLE);
                views.setViewVisibility(R.id.btn_6, View.VISIBLE);
                views.setViewVisibility(R.id.btn_7, View.VISIBLE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_5, View.GONE);
                    views.setViewVisibility(R.id.div_6, View.GONE);
                    views.setViewVisibility(R.id.div_7, View.GONE);
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_5, View.VISIBLE);
                    views.setViewVisibility(R.id.div_6, View.VISIBLE);
                    views.setViewVisibility(R.id.div_7, View.VISIBLE);
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 17:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_4,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_5,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_6,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_7,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[13], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[14], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[15], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[16], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.VISIBLE);
                views.setViewVisibility(R.id.btn_5, View.VISIBLE);
                views.setViewVisibility(R.id.btn_6, View.VISIBLE);
                views.setViewVisibility(R.id.btn_7, View.VISIBLE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_4, View.GONE);
                    views.setViewVisibility(R.id.div_5, View.GONE);
                    views.setViewVisibility(R.id.div_6, View.GONE);
                    views.setViewVisibility(R.id.div_7, View.GONE);
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_4, View.VISIBLE);
                    views.setViewVisibility(R.id.div_5, View.VISIBLE);
                    views.setViewVisibility(R.id.div_6, View.VISIBLE);
                    views.setViewVisibility(R.id.div_7, View.VISIBLE);
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 18:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_3,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_4,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_5,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_6,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_7,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[13], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[14], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[15], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[16], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[17], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.VISIBLE);
                views.setViewVisibility(R.id.btn_4, View.VISIBLE);
                views.setViewVisibility(R.id.btn_5, View.VISIBLE);
                views.setViewVisibility(R.id.btn_6, View.VISIBLE);
                views.setViewVisibility(R.id.btn_7, View.VISIBLE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_3, View.GONE);
                    views.setViewVisibility(R.id.div_4, View.GONE);
                    views.setViewVisibility(R.id.div_5, View.GONE);
                    views.setViewVisibility(R.id.div_6, View.GONE);
                    views.setViewVisibility(R.id.div_7, View.GONE);
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_3, View.VISIBLE);
                    views.setViewVisibility(R.id.div_4, View.VISIBLE);
                    views.setViewVisibility(R.id.div_5, View.VISIBLE);
                    views.setViewVisibility(R.id.div_6, View.VISIBLE);
                    views.setViewVisibility(R.id.div_7, View.VISIBLE);
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 19:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_2,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_3,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_4,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_5,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_6,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_7,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[13], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[14], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[15], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[16], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[17], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[18], cls));

                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.VISIBLE);
                views.setViewVisibility(R.id.btn_3, View.VISIBLE);
                views.setViewVisibility(R.id.btn_4, View.VISIBLE);
                views.setViewVisibility(R.id.btn_5, View.VISIBLE);
                views.setViewVisibility(R.id.btn_6, View.VISIBLE);
                views.setViewVisibility(R.id.btn_7, View.VISIBLE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                views.setViewVisibility(R.id.div_1, View.GONE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_2, View.GONE);
                    views.setViewVisibility(R.id.div_3, View.GONE);
                    views.setViewVisibility(R.id.div_4, View.GONE);
                    views.setViewVisibility(R.id.div_5, View.GONE);
                    views.setViewVisibility(R.id.div_6, View.GONE);
                    views.setViewVisibility(R.id.div_7, View.GONE);
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_2, View.VISIBLE);
                    views.setViewVisibility(R.id.div_3, View.VISIBLE);
                    views.setViewVisibility(R.id.div_4, View.VISIBLE);
                    views.setViewVisibility(R.id.div_5, View.VISIBLE);
                    views.setViewVisibility(R.id.div_6, View.VISIBLE);
                    views.setViewVisibility(R.id.div_7, View.VISIBLE);
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;
            case 20:
                views.setOnClickPendingIntent(R.id.btn_0,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[0], cls));
                views.setOnClickPendingIntent(R.id.btn_1,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[1], cls));
                views.setOnClickPendingIntent(R.id.btn_2,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[2], cls));
                views.setOnClickPendingIntent(R.id.btn_3,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[3], cls));
                views.setOnClickPendingIntent(R.id.btn_4,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[4], cls));
                views.setOnClickPendingIntent(R.id.btn_5,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[5], cls));
                views.setOnClickPendingIntent(R.id.btn_6,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[6], cls));
                views.setOnClickPendingIntent(R.id.btn_7,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[7], cls));
                views.setOnClickPendingIntent(R.id.btn_8,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[8], cls));
                views.setOnClickPendingIntent(R.id.btn_9,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[9], cls));
                views.setOnClickPendingIntent(R.id.btn_10,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[10], cls));
                views.setOnClickPendingIntent(R.id.btn_11,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[11], cls));
                views.setOnClickPendingIntent(R.id.btn_12,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[12], cls));
                views.setOnClickPendingIntent(R.id.btn_13,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[13], cls));
                views.setOnClickPendingIntent(R.id.btn_14,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[14], cls));
                views.setOnClickPendingIntent(R.id.btn_15,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[15], cls));
                views.setOnClickPendingIntent(R.id.btn_16,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[16], cls));
                views.setOnClickPendingIntent(R.id.btn_17,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[17], cls));
                views.setOnClickPendingIntent(R.id.btn_18,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[18], cls));
                views.setOnClickPendingIntent(R.id.btn_19,
                        getLaunchPendingIntent(context, appWidgetId, buttonIds[19], cls));

                views.setViewVisibility(R.id.btn_1, View.VISIBLE);
                views.setViewVisibility(R.id.btn_2, View.VISIBLE);
                views.setViewVisibility(R.id.btn_3, View.VISIBLE);
                views.setViewVisibility(R.id.btn_4, View.VISIBLE);
                views.setViewVisibility(R.id.btn_5, View.VISIBLE);
                views.setViewVisibility(R.id.btn_6, View.VISIBLE);
                views.setViewVisibility(R.id.btn_7, View.VISIBLE);
                views.setViewVisibility(R.id.btn_8, View.VISIBLE);
                views.setViewVisibility(R.id.btn_9, View.VISIBLE);
                views.setViewVisibility(R.id.btn_10, View.VISIBLE);
                views.setViewVisibility(R.id.btn_11, View.VISIBLE);
                views.setViewVisibility(R.id.btn_12, View.VISIBLE);
                views.setViewVisibility(R.id.btn_13, View.VISIBLE);
                views.setViewVisibility(R.id.btn_14, View.VISIBLE);
                views.setViewVisibility(R.id.btn_15, View.VISIBLE);
                views.setViewVisibility(R.id.btn_16, View.VISIBLE);
                views.setViewVisibility(R.id.btn_17, View.VISIBLE);
                views.setViewVisibility(R.id.btn_18, View.VISIBLE);
                views.setViewVisibility(R.id.btn_19, View.VISIBLE);

                if (((layoutName.equals(context.getString(R.string.list_pre_bg_custom)) || layoutName.equals(context
                        .getString(R.string.list_pre_bg_custom_shadow))) && dividerColor == Constants.NOT_SHOW_FLAG)) {
                    views.setViewVisibility(R.id.div_1, View.GONE);
                    views.setViewVisibility(R.id.div_2, View.GONE);
                    views.setViewVisibility(R.id.div_3, View.GONE);
                    views.setViewVisibility(R.id.div_4, View.GONE);
                    views.setViewVisibility(R.id.div_5, View.GONE);
                    views.setViewVisibility(R.id.div_6, View.GONE);
                    views.setViewVisibility(R.id.div_7, View.GONE);
                    views.setViewVisibility(R.id.div_8, View.GONE);
                    views.setViewVisibility(R.id.div_9, View.GONE);
                    views.setViewVisibility(R.id.div_10, View.GONE);
                    views.setViewVisibility(R.id.div_11, View.GONE);
                    views.setViewVisibility(R.id.div_12, View.GONE);
                    views.setViewVisibility(R.id.div_13, View.GONE);
                    views.setViewVisibility(R.id.div_14, View.GONE);
                    views.setViewVisibility(R.id.div_15, View.GONE);
                    views.setViewVisibility(R.id.div_16, View.GONE);
                    views.setViewVisibility(R.id.div_17, View.GONE);
                    views.setViewVisibility(R.id.div_18, View.GONE);
                    views.setViewVisibility(R.id.div_19, View.GONE);
                } else {
                    views.setViewVisibility(R.id.div_1, View.VISIBLE);
                    views.setViewVisibility(R.id.div_2, View.VISIBLE);
                    views.setViewVisibility(R.id.div_3, View.VISIBLE);
                    views.setViewVisibility(R.id.div_4, View.VISIBLE);
                    views.setViewVisibility(R.id.div_5, View.VISIBLE);
                    views.setViewVisibility(R.id.div_6, View.VISIBLE);
                    views.setViewVisibility(R.id.div_7, View.VISIBLE);
                    views.setViewVisibility(R.id.div_8, View.VISIBLE);
                    views.setViewVisibility(R.id.div_9, View.VISIBLE);
                    views.setViewVisibility(R.id.div_10, View.VISIBLE);
                    views.setViewVisibility(R.id.div_11, View.VISIBLE);
                    views.setViewVisibility(R.id.div_12, View.VISIBLE);
                    views.setViewVisibility(R.id.div_13, View.VISIBLE);
                    views.setViewVisibility(R.id.div_14, View.VISIBLE);
                    views.setViewVisibility(R.id.div_15, View.VISIBLE);
                    views.setViewVisibility(R.id.div_16, View.VISIBLE);
                    views.setViewVisibility(R.id.div_17, View.VISIBLE);
                    views.setViewVisibility(R.id.div_18, View.VISIBLE);
                    views.setViewVisibility(R.id.div_19, View.VISIBLE);
                }
                break;

            default:
                views.setViewVisibility(R.id.btn_0, View.GONE);
                views.setViewVisibility(R.id.btn_1, View.GONE);
                views.setViewVisibility(R.id.btn_2, View.GONE);
                views.setViewVisibility(R.id.btn_3, View.GONE);
                views.setViewVisibility(R.id.btn_4, View.GONE);
                views.setViewVisibility(R.id.btn_5, View.GONE);
                views.setViewVisibility(R.id.btn_6, View.GONE);
                views.setViewVisibility(R.id.btn_7, View.GONE);
                views.setViewVisibility(R.id.btn_8, View.GONE);
                views.setViewVisibility(R.id.btn_9, View.GONE);
                views.setViewVisibility(R.id.btn_10, View.GONE);
                views.setViewVisibility(R.id.btn_11, View.GONE);
                views.setViewVisibility(R.id.btn_12, View.GONE);
                views.setViewVisibility(R.id.btn_13, View.GONE);
                views.setViewVisibility(R.id.btn_14, View.GONE);
                views.setViewVisibility(R.id.btn_15, View.GONE);
                views.setViewVisibility(R.id.btn_16, View.GONE);
                views.setViewVisibility(R.id.btn_17, View.GONE);
                views.setViewVisibility(R.id.btn_18, View.GONE);
                views.setViewVisibility(R.id.btn_19, View.GONE);

                views.setViewVisibility(R.id.div_1, View.GONE);
                views.setViewVisibility(R.id.div_2, View.GONE);
                views.setViewVisibility(R.id.div_3, View.GONE);
                views.setViewVisibility(R.id.div_4, View.GONE);
                views.setViewVisibility(R.id.div_5, View.GONE);
                views.setViewVisibility(R.id.div_6, View.GONE);
                views.setViewVisibility(R.id.div_7, View.GONE);
                views.setViewVisibility(R.id.div_8, View.GONE);
                views.setViewVisibility(R.id.div_9, View.GONE);
                views.setViewVisibility(R.id.div_10, View.GONE);
                views.setViewVisibility(R.id.div_11, View.GONE);
                views.setViewVisibility(R.id.div_12, View.GONE);
                views.setViewVisibility(R.id.div_13, View.GONE);
                views.setViewVisibility(R.id.div_14, View.GONE);
                views.setViewVisibility(R.id.div_15, View.GONE);
                views.setViewVisibility(R.id.div_16, View.GONE);
                views.setViewVisibility(R.id.div_17, View.GONE);
                views.setViewVisibility(R.id.div_18, View.GONE);
                views.setViewVisibility(R.id.div_19, View.GONE);
                break;
        }

        return views;
    }

    private static int getDataConnIcon(Context context) {
        int netType = NetUtils.getNetworkType(context);

        if (netType == TelephonyManager.NETWORK_TYPE_EDGE || netType == TelephonyManager.NETWORK_TYPE_GPRS) {
            return R.drawable.icon_edge_on;
        } else {
            return R.drawable.icon_3g_on;
        }
    }

    /**
     * ��ȡ��ť���õĲ���ֵ
     *
     * @param context
     * @param appWidgetId
     * @return
     */
    private static int[] getButtonIdsFromStr(Context context, String buttonIdsStr) {
        String[] part = buttonIdsStr.split(",");
        int length = part.length;

        switch (length) {
            case 1:
                return new int[]{Integer.parseInt(part[0])};
            case 2:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1])};
            case 3:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2])};
            case 4:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3])};
            case 5:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4])};
            case 6:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5])};
            case 7:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6])};
            case 8:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7])};
            case 9:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8])};
            case 10:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9])};
            case 11:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10])};
            case 12:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11])};
            case 13:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12])};
            case 14:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12]), Integer.parseInt(part[13])};
            case 15:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12]), Integer.parseInt(part[13]), Integer.parseInt(part[14])};
            case 16:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12]), Integer.parseInt(part[13]), Integer.parseInt(part[14]),
                        Integer.parseInt(part[15])};
            case 17:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12]), Integer.parseInt(part[13]), Integer.parseInt(part[14]),
                        Integer.parseInt(part[15]), Integer.parseInt(part[16])};
            case 18:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12]), Integer.parseInt(part[13]), Integer.parseInt(part[14]),
                        Integer.parseInt(part[15]), Integer.parseInt(part[16]), Integer.parseInt(part[17])};
            case 19:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12]), Integer.parseInt(part[13]), Integer.parseInt(part[14]),
                        Integer.parseInt(part[15]), Integer.parseInt(part[16]), Integer.parseInt(part[17]),
                        Integer.parseInt(part[18])};
            case 20:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4]), Integer.parseInt(part[5]),
                        Integer.parseInt(part[6]), Integer.parseInt(part[7]), Integer.parseInt(part[8]),
                        Integer.parseInt(part[9]), Integer.parseInt(part[10]), Integer.parseInt(part[11]),
                        Integer.parseInt(part[12]), Integer.parseInt(part[13]), Integer.parseInt(part[14]),
                        Integer.parseInt(part[15]), Integer.parseInt(part[16]), Integer.parseInt(part[17]),
                        Integer.parseInt(part[18]), Integer.parseInt(part[19])};
            default:
                return new int[]{Integer.parseInt(part[0]), Integer.parseInt(part[1]), Integer.parseInt(part[2]),
                        Integer.parseInt(part[3]), Integer.parseInt(part[4])};
        }
    }

    /**
     * Creates PendingIntent to notify the widget of a button click.
     *
     * @param context
     * @param appWidgetId
     * @return
     */
    protected static PendingIntent getLaunchPendingIntent(Context context, Integer appWidgetId, int buttonId,
                                                          Class<?> cls) {
        Intent launchIntent = new Intent();
        launchIntent.setData(Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId)
                + "/" + buttonId));
        launchIntent.setClass(context, cls == null ? MainBrocastReceiver.class : cls);
        launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        launchIntent.putExtra(EXTRA_BUTTON_ID, buttonId);
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        return PendingIntent.getBroadcast(context, 0, launchIntent, 0);
    }

    /**
     * Updates the buttons based on the underlying states of wifi, etc.
     * ��ص�״̬��BatteryInfoService�����
     *
     * @param views   The RemoteViews to update.
     * @param context
     */
    private static void updateButtons(RemoteViews views, Context context, SharedPreferences config, String layoutName,
                                      int[] buttonIds, int iconColor, int iconTrans, int indColor) {
        int viewId = -1;

        // ����WIFI״̬
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_WIFI, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_WIFI));
            int state = SwitchUtils.getWifiState(context);

            if (needCustomIcon) {
                if (BITMAP_WIFI == null) {
                    try {
                        BITMAP_WIFI = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_WIFI), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_wifi_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_WIFI, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_wifi_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_WIFI, views, layoutName, buttonIds, state, indColor);
        }

        // ��ȡAPN״̬
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_EDGE, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_EDGE));
            int state = SwitchUtils.getApnState(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_EDGE == null) {
                    try {
                        BITMAP_EDGE = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_EDGE), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, getDataConnIcon(context), state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_EDGE, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, getDataConnIcon(context), state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_EDGE, views, layoutName, buttonIds, state, indColor);
        }

        // ��ȡ����״̬
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_BRIGHTNESS, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_BRIGHTNESS));
            boolean needCustomAutoIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_AUTO_BRIGHTNESS));
            int state = SwitchUtils.getBrightness(context);

            // �Զ�����
            if (state == STATE_OTHER) {
                if (needCustomAutoIcon) {
                    if (BITMAP_AUTO_BRIGHT == null) {
                        try {
                            BITMAP_AUTO_BRIGHT = BitmapFactory
                                    .decodeStream(context.openFileInput(config.getString(String.format(
                                            Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_AUTO_BRIGHTNESS), "")));
                        } catch (FileNotFoundException e) {
                            // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                            setIconResource(context, views, viewId, R.drawable.icon_brightness_auto_on, STATE_ENABLED,
                                    iconColor, iconTrans);
                            e.printStackTrace();
                        }
                    }

                    setIconResource(context, views, viewId, BITMAP_AUTO_BRIGHT, STATE_ENABLED, iconColor, iconTrans);
                } else {
                    setIconResource(context, views, viewId, R.drawable.icon_brightness_auto_on, STATE_ENABLED,
                            iconColor, iconTrans);
                }

                setIndViewResource(context, Constants.BUTTON_BRIGHTNESS, views, layoutName, buttonIds, STATE_ENABLED,
                        indColor);
            } else {
                if (needCustomIcon) {
                    if (BITMAP_BRIGHTNESS == null) {
                        try {
                            BITMAP_BRIGHTNESS = BitmapFactory
                                    .decodeStream(context.openFileInput(config.getString(String.format(
                                            Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_BRIGHTNESS), "")));
                        } catch (FileNotFoundException e) {
                            // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                            setIconResource(context, views, viewId, R.drawable.icon_brightness_on, state, iconColor,
                                    iconTrans);
                            e.printStackTrace();
                        }
                    }

                    setIconResource(context, views, viewId, BITMAP_BRIGHTNESS, state, iconColor, iconTrans);
                } else {
                    setIconResource(context, views, viewId, R.drawable.icon_brightness_on, state, iconColor, iconTrans);
                }

                setIndViewResource(context, Constants.BUTTON_BRIGHTNESS, views, layoutName, buttonIds, state, indColor);
            }
        }

        // ��ȡͬ��״̬
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_SYNC, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_SYNC));
            int state = SwitchUtils.getSync(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_SYNC == null) {
                    try {
                        BITMAP_SYNC = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_SYNC), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_sync_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_SYNC, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_sync_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_SYNC, views, layoutName, buttonIds, state, indColor);
        }

        // ��ȡGPS״̬
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_GPS, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_GPS));
            int state = SwitchUtils.getGpsState(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_GPS == null) {
                    try {
                        BITMAP_GPS = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_GPS), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_gps_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_GPS, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_gps_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_GPS, views, layoutName, buttonIds, state, indColor);
        }

        // ��ȡ������Ӧ״̬
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_GRAVITY, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_GRAVITY));
            int state = SwitchUtils.getGravityState(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_GRAVITY == null) {
                    try {
                        BITMAP_GRAVITY = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_GRAVITY), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_gravity_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_GRAVITY, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_gravity_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_GRAVITY, views, layoutName, buttonIds, state, indColor);
        }

        // ��ȡ����״̬
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_BLUETOOTH, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_BLUETOOTH));
            int state = SwitchUtils.getBluetoothState(context);

            if (needCustomIcon) {
                if (BITMAP_BLUETOOTH == null) {
                    try {
                        BITMAP_BLUETOOTH = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_BLUETOOTH), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_bluetooth_on, state, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_BLUETOOTH, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_bluetooth_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_BLUETOOTH, views, layoutName, buttonIds, state, indColor);
        }

        // ����ģʽ
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_AIRPLANE, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_AIRPLANE));
            int state = SwitchUtils.getNetworkState(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_AIRPLANE == null) {
                    try {
                        BITMAP_AIRPLANE = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_AIRPLANE), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_airplane_on, state, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_AIRPLANE, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_airplane_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_AIRPLANE, views, layoutName, buttonIds, state, indColor);
        }

        // ��Ļ��ʱ
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_SCREEN_TIMEOUT, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_SCREEN_TIMEOUT));
            int state = SwitchUtils.getScreenTimeoutState(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_SCREEN_TIMEOUT == null) {
                    try {
                        BITMAP_SCREEN_TIMEOUT = BitmapFactory
                                .decodeStream(context.openFileInput(config.getString(String.format(
                                        Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_SCREEN_TIMEOUT), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_screen_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_SCREEN_TIMEOUT, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_screen_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_SCREEN_TIMEOUT, views, layoutName, buttonIds, state, indColor);
        }

        // ���¾�����ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_VIBRATE, buttonIds)) != -1) {
            int mode = SwitchUtils.getViberate(context);
            String btn = config.getString(Constants.PREFS_SILENT_BTN, Constants.BTN_VS);

            // ֻ�а�ťѡ����˫ģʽ����ģʽʱ����ʾ��ͼ��
            if (mode == AudioManager.RINGER_MODE_VIBRATE && !btn.equals(Constants.BTN_ONLY_SILENT)) {
                updateVibrate(views, context, config, buttonIds, iconColor, iconTrans, indColor, viewId, STATE_ENABLED);
                setIndViewResource(context, Constants.BUTTON_VIBRATE, views, layoutName, buttonIds, STATE_ENABLED,
                        indColor);
            } else if (mode == AudioManager.RINGER_MODE_SILENT && !btn.equals(Constants.BTN_ONLY_VIVERATE)) {
                updateSilent(views, context, config, buttonIds, iconColor, iconTrans, indColor, viewId, STATE_ENABLED);
                setIndViewResource(context, Constants.BUTTON_VIBRATE, views, layoutName, buttonIds, STATE_ENABLED,
                        indColor);
            } else {
                // �����ťֻѡ���˾�������ô��ģʽ�ر�ʱ��ʾ������ť
                if (btn.equals(Constants.BTN_ONLY_SILENT)) {
                    updateSilent(views, context, config, buttonIds, iconColor, iconTrans, indColor, viewId,
                            STATE_DISABLED);
                } else {
                    updateVibrate(views, context, config, buttonIds, iconColor, iconTrans, indColor, viewId,
                            STATE_DISABLED);
                }

                // ����������ʱ�ָ����֣���ֹ��������Ӵ�����ģʽ�Ӷ��޷��ָ�ý������
                AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(
                        Context.AUDIO_SERVICE);
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
                setIndViewResource(context, Constants.BUTTON_VIBRATE, views, layoutName, buttonIds, STATE_DISABLED,
                        indColor);
            }
        }

        // ����ý��ɨ�谴ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_SCANMEDIA, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_SCANMEDIA));

            if (needCustomIcon) {
                if (BITMAP_SCANMEDIA == null) {
                    try {
                        BITMAP_SCANMEDIA = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_SCANMEDIA), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_media_on, STATE_DISABLED, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_SCANMEDIA, STATE_DISABLED, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_media_on, STATE_DISABLED, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_SCANMEDIA, views, layoutName, buttonIds, STATE_DISABLED,
                    indColor);
        }

        // ���������л���ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_NET_SWITCH, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_NET_SWITCH));
            int state = SwitchUtils.getNetSwitch(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_NET_SWITCH == null) {
                    try {
                        BITMAP_NET_SWITCH = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_NET_SWITCH), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_netswitch_on, state, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_NET_SWITCH, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_netswitch_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_NET_SWITCH, views, layoutName, buttonIds, state, indColor);
        }

        // ���½���ģʽ��ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_UNLOCK, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_UNLOCK));
            int state = SwitchUtils.getUnlockPattern(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_UNLOCK == null) {
                    try {
                        BITMAP_UNLOCK = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_UNLOCK), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_unlock_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_UNLOCK, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_unlock_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_UNLOCK, views, layoutName, buttonIds, state, indColor);
        }

        // ��������ư�ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_FLASHLIGHT, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_FLASHLIGHT));
            int state = SwitchUtils.getFlashlight(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_FLASHLIGHT == null) {
                    try {
                        BITMAP_FLASHLIGHT = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_FLASHLIGHT), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_flashlight_on, state, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_FLASHLIGHT, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_flashlight_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_FLASHLIGHT, views, layoutName, buttonIds, state, indColor);
        }

        // ����4G����
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_WIMAX, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_FLASHLIGHT));
            int state = SwitchUtils.getWimaxState(context);

            if (needCustomIcon) {
                if (BITMAP_WIMAX == null) {
                    try {
                        BITMAP_WIMAX = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_WIMAX), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_wimax_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_WIMAX, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_wimax_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_WIMAX, views, layoutName, buttonIds, state, indColor);
        }

        // ����reboot��ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_REBOOT, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_REBOOT));

            if (needCustomIcon) {
                if (BITMAP_REBOOT == null) {
                    try {
                        BITMAP_REBOOT = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_REBOOT), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_reboot_on, STATE_DISABLED, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_REBOOT, STATE_DISABLED, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_reboot_on, STATE_DISABLED, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_REBOOT, views, layoutName, buttonIds, STATE_DISABLED, indColor);
        }

        // ����Speakerģʽ
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_SPEAKER, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_SPEAKER));
            int state = SwitchUtils.getSpeakMode(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_SPEAKER == null) {
                    try {
                        BITMAP_SPEAKER = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_SPEAKER), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_speaker_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_SPEAKER, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_speaker_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_SPEAKER, views, layoutName, buttonIds, state, indColor);
        }

        // �����Զ���Ļ��
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_AUTOLOCK, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_AUTOLOCK));
            int state = SwitchUtils.getAutoLock(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_AUTOLOCK == null) {
                    try {
                        BITMAP_AUTOLOCK = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_AUTOLOCK), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_autolock_on, state, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_AUTOLOCK, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_autolock_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_AUTOLOCK, views, layoutName, buttonIds, state, indColor);
        }

        // ����Wifiap
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_WIFIAP, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_WIFIAP));
            int state = SwitchUtils.getWifiApState(context);

            if (needCustomIcon) {
                if (BITMAP_WIFIAP == null) {
                    try {
                        BITMAP_WIFIAP = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_WIFIAP), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_wifite_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_WIFIAP, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_wifite_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_WIFIAP, views, layoutName, buttonIds, state, indColor);
        }

        // ����Wifi���߲���
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_WIFI_SLEEP, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_WIFI_SLEEP));
            int state = SwitchUtils.getWifiSleepPolicy(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_WIFI_SLEEP == null) {
                    try {
                        BITMAP_WIFI_SLEEP = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_WIFI_SLEEP), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_wifi_sleep, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_WIFI_SLEEP, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_wifi_sleep, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_WIFI_SLEEP, views, layoutName, buttonIds, state, indColor);
        }

        // ����Usb tether
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_USBTE, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_USBTE));
            int state = SwitchUtils.getUsbTetherState(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_USBTE == null) {
                    try {
                        BITMAP_USBTE = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_USBTE), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_usbte_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_USBTE, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_usbte_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_USBTE, views, layoutName, buttonIds, state, indColor);
        }

        // ����Mount
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_MOUNT, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_MOUNT));
            int state = SwitchUtils.getMountState();

            if (needCustomIcon) {
                if (BITMAP_MOUNT == null) {
                    try {
                        BITMAP_MOUNT = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_MOUNT), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_sdcard_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_MOUNT, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_sdcard_on, state, iconColor, iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_MOUNT, views, layoutName, buttonIds, state, indColor);
        }

        // ������Ļ
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_LOCK_SCREEN, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_LOCK_SCREEN));

            if (needCustomIcon) {
                if (BITMAP_LOCK_SCREEN == null) {
                    try {
                        BITMAP_LOCK_SCREEN = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_LOCK_SCREEN), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_lockscreen_on, STATE_DISABLED,
                                iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }

                setIconResource(context, views, viewId, BITMAP_LOCK_SCREEN, STATE_DISABLED, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_lockscreen_on, STATE_DISABLED, iconColor,
                        iconTrans);
            }

            setIndViewResource(context, Constants.BUTTON_LOCK_SCREEN, views, layoutName, buttonIds, STATE_DISABLED,
                    indColor);
        }

        // ���µ��״̬��ť �����ȡ���������Ϣ�Ǵ����currentBatteryLevelΪ-1
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_BATTERY, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_BATTERY));
            int currentBatteryLevel = config.getInt(Constants.PREFS_BATTERY_LEVEL, -1);

            // ����nullʱ��������ͼƬ����ɫ�˾�
            Integer vcolor = iconColor == Constants.NOT_SHOW_FLAG ? null : iconColor;

            if (currentBatteryLevel < 0 || currentBatteryLevel > 100) {
                if (needCustomIcon) {
                    if (BITMAP_BATTERY == null) {
                        try {
                            BITMAP_BATTERY = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                    String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_LOCK_SCREEN),
                                    "")));
                        } catch (FileNotFoundException e) {
                            // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                            setIconResource(context, views, viewId, R.drawable.batterynumber_blank, STATE_DISABLED,
                                    vcolor, iconTrans);
                            e.printStackTrace();
                        }
                    }

                    setIconResource(context, views, viewId, BITMAP_BATTERY, STATE_DISABLED, vcolor, iconTrans);
                } else {
                    setIconResource(context, views, viewId, R.drawable.batterynumber_blank, STATE_DISABLED, vcolor,
                            iconTrans);
                }

                setIndViewResource(context, Constants.BUTTON_BATTERY, views, layoutName, buttonIds, STATE_DISABLED,
                        indColor);
            } else if (currentBatteryLevel == 100) {
                if (needCustomIcon) {
                    if (BITMAP_BATTERY == null) {
                        try {
                            BITMAP_BATTERY = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                    String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_BATTERY), "")));
                        } catch (FileNotFoundException e) {
                            // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                            setIconResource(context, views, viewId, R.drawable.icon_battery_full, STATE_ENABLED,
                                    vcolor, iconTrans);
                            e.printStackTrace();
                        }
                    }

                    setIconResource(context, views, viewId,
                            BatteryIndicatorUtils.getIcon(context, BITMAP_BATTERY, currentBatteryLevel), STATE_ENABLED,
                            vcolor, iconTrans);
                } else {
                    setIconResource(context, views, viewId, R.drawable.icon_battery_full, STATE_ENABLED, vcolor,
                            iconTrans);
                }

                setIndViewResource(context, Constants.BUTTON_BATTERY, views, layoutName, buttonIds, STATE_ENABLED,
                        indColor);
            } else {
                if (needCustomIcon) {
                    if (BITMAP_BATTERY == null) {
                        try {
                            BITMAP_BATTERY = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                    String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_BATTERY), "")));
                        } catch (FileNotFoundException e) {
                            // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                            views.setImageViewBitmap(viewId, Utils.setIconColor(context,
                                    BatteryIndicatorUtils.getBitmap(context.getResources(), currentBatteryLevel), 255,
                                    vcolor));
                            e.printStackTrace();
                        }
                    }

                    setIconResource(context, views, viewId,
                            BatteryIndicatorUtils.getIcon(context, BITMAP_BATTERY, currentBatteryLevel), STATE_ENABLED,
                            vcolor, iconTrans);
                } else {
                    views.setImageViewBitmap(viewId, Utils.setIconColor(context,
                            BatteryIndicatorUtils.getBitmap(context.getResources(), currentBatteryLevel), 255, vcolor));
                }

                if (currentBatteryLevel > 20) {
                    setIndViewResource(context, Constants.BUTTON_BATTERY, views, layoutName, buttonIds, STATE_ENABLED,
                            indColor);
                } else {
                    setIndViewResource(context, Constants.BUTTON_BATTERY, views, layoutName, buttonIds,
                            STATE_INTERMEDIATE, indColor);
                }
            }
        }

        // ����������ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_VOLUME, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_VOLUME));
            if (needCustomIcon) {
                if (BITMAP_VOLUME == null) {
                    try {
                        BITMAP_VOLUME = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_VOLUME), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_volume, STATE_DISABLED, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }
                setIconResource(context, views, viewId, BITMAP_VOLUME, STATE_DISABLED, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_volume, STATE_DISABLED, iconColor, iconTrans);
            }
            setIndViewResource(context, Constants.BUTTON_VOLUME, views, layoutName, buttonIds, STATE_DISABLED, indColor);
        }

        // ɱ���̰�ť
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_KILL_PROCESS, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_KILL_PROCESS));
            if (needCustomIcon) {
                if (BITMAP_KILL_PROCESS == null) {
                    try {
                        BITMAP_KILL_PROCESS = BitmapFactory
                                .decodeStream(context.openFileInput(config.getString(String.format(
                                        Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_KILL_PROCESS), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_killprocess_on, STATE_DISABLED,
                                iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }
                setIconResource(context, views, viewId, BITMAP_KILL_PROCESS, STATE_DISABLED, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_killprocess_on, STATE_DISABLED, iconColor,
                        iconTrans);
            }
            setIndViewResource(context, Constants.BUTTON_KILL_PROCESS, views, layoutName, buttonIds, STATE_DISABLED,
                    indColor);
        }

        // �ڴ�ռ�
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_MEMORY_USAGE, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_MEMORY_USAGE));
            MemInfoReader memInfoReader = new MemInfoReader();
            memInfoReader.readMemInfo();
            long freeSize = memInfoReader.getFreeSize() + memInfoReader.getCachedSize();
            long total = memInfoReader.getTotalSize();
            float percent = ((float) total - (float) freeSize) / (float) total;

            if (needCustomIcon) {
                if (BITMAP_MEMORY_USAGE == null) {
                    try {
                        BITMAP_MEMORY_USAGE = BitmapFactory
                                .decodeStream(context.openFileInput(config.getString(String.format(
                                        Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_MEMORY_USAGE), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, Utils.createUsageIcon(context, percent),
                                STATE_DISABLED, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }
                setIconResource(context, views, viewId, BITMAP_MEMORY_USAGE, STATE_DISABLED, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, Utils.createUsageIcon(context, percent), STATE_DISABLED,
                        iconColor, iconTrans);
            }
            setIndViewResource(context, Constants.BUTTON_MEMORY_USAGE, views, layoutName, buttonIds, STATE_DISABLED,
                    indColor);
        }

        // �洢�ռ�
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_STORAGE_USAGE, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_STORAGE_USAGE));
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long totalBlocks = statFs.getBlockCount();
            long availableBlocks = statFs.getAvailableBlocks();
            float percent = ((float) totalBlocks - (float) availableBlocks) / (float) totalBlocks;

            if (needCustomIcon) {
                if (BITMAP_STORAGE_USAGE == null) {
                    try {
                        BITMAP_STORAGE_USAGE = BitmapFactory
                                .decodeStream(context.openFileInput(config.getString(String.format(
                                        Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_STORAGE_USAGE), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, Utils.createUsageIcon(context, percent),
                                STATE_DISABLED, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }
                setIconResource(context, views, viewId, BITMAP_STORAGE_USAGE, STATE_DISABLED, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, Utils.createUsageIcon(context, percent), STATE_DISABLED,
                        iconColor, iconTrans);
            }
            setIndViewResource(context, Constants.BUTTON_STORAGE_USAGE, views, layoutName, buttonIds, STATE_DISABLED,
                    indColor);
        }

        // ����te
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_BT_TE, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_BT_TE));
            int state = SwitchUtils.getBluetoothTe(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_BT_TE == null) {
                    try {
                        BITMAP_BT_TE = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_BT_TE), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_blutoothte_on, state, iconColor,
                                iconTrans);
                        e.printStackTrace();
                    }
                }
                setIconResource(context, views, viewId, BITMAP_BT_TE, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_blutoothte_on, state, iconColor, iconTrans);
            }
            setIndViewResource(context, Constants.BUTTON_BT_TE, views, layoutName, buttonIds, state, indColor);
        }

        // NFC
        if ((viewId = getImgIdByBtnId(Constants.BUTTON_NFC, buttonIds)) != -1) {
            boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                    Constants.ICON_NFC));
            int state = SwitchUtils.getNFC(context) ? STATE_ENABLED : STATE_DISABLED;

            if (needCustomIcon) {
                if (BITMAP_NFC == null) {
                    try {
                        BITMAP_NFC = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                                String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_NFC), "")));
                    } catch (FileNotFoundException e) {
                        // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                        setIconResource(context, views, viewId, R.drawable.icon_nfc_on, state, iconColor, iconTrans);
                        e.printStackTrace();
                    }
                }
                setIconResource(context, views, viewId, BITMAP_NFC, state, iconColor, iconTrans);
            } else {
                setIconResource(context, views, viewId, R.drawable.icon_nfc_on, state, iconColor, iconTrans);
            }
            setIndViewResource(context, Constants.BUTTON_NFC, views, layoutName, buttonIds, state, indColor);
        }
    }

    private static void updateVibrate(RemoteViews views, Context context, SharedPreferences config, int[] buttonIds,
                                      int iconColor, int iconTrans, int indColor, int viewId, int state) {
        boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                Constants.ICON_VIBRATE));

        if (needCustomIcon) {
            if (BITMAP_VIBRATE == null) {
                try {
                    BITMAP_VIBRATE = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                            String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_VIBRATE), "")));
                } catch (FileNotFoundException e) {
                    // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                    setIconResource(context, views, viewId, R.drawable.icon_vibrate_on, state, iconColor, iconTrans);
                    e.printStackTrace();
                }
            }

            setIconResource(context, views, viewId, BITMAP_VIBRATE, state, iconColor, iconTrans);
        } else {
            setIconResource(context, views, viewId, R.drawable.icon_vibrate_on, state, iconColor, iconTrans);
        }
    }

    private static void updateSilent(RemoteViews views, Context context, SharedPreferences config, int[] buttonIds,
                                     int iconColor, int iconTrans, int indColor, int viewId, int state) {
        boolean needCustomIcon = config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN,
                Constants.ICON_SILENT));

        if (needCustomIcon) {
            if (BITMAP_SILENT == null) {
                try {
                    BITMAP_SILENT = BitmapFactory.decodeStream(context.openFileInput(config.getString(
                            String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, Constants.ICON_SILENT), "")));
                } catch (FileNotFoundException e) {
                    // ���ܴ���ֻ�������ļ�������û��ͼƬ�������ʱ���滻ͼƬ
                    setIconResource(context, views, viewId, R.drawable.icon_cilent_on, state, iconColor, iconTrans);
                    e.printStackTrace();
                }
            }

            setIconResource(context, views, viewId, BITMAP_SILENT, state, iconColor, iconTrans);
        } else {
            setIconResource(context, views, viewId, R.drawable.icon_cilent_on, state, iconColor, iconTrans);
        }

    }

    /**
     * ����ĳ����ťID�ҵ���Ӧ��ͼ��λ��
     *
     * @param btnId
     * @param buttonIds
     * @return
     */
    public static int getImgIdByBtnId(int btnId, int[] buttonIds) {
        int pos = -1;

        // ����ָ����ť�ڰ�ť˳������(buttonIds[])�е�λ��
        for (int i = 0; i < buttonIds.length; i++) {
            if (buttonIds[i] == btnId) {
                // �ӵڶ�����ť��ʼƫ��
                if (i != 0) {
                    switch (buttonIds.length) {
                        case 1:
                            pos = i;
                            break;
                        case 2:
                            pos = i + 18;
                            break;
                        case 3:
                            pos = i + 17;
                            break;
                        case 4:
                            pos = i + 16;
                            break;
                        case 5:
                            pos = i + 15;
                            break;
                        case 6:
                            pos = i + 14;
                            break;
                        case 7:
                            pos = i + 13;
                            break;
                        case 8:
                            pos = i + 12;
                            break;
                        case 9:
                            pos = i + 11;
                            break;
                        case 10:
                            pos = i + 10;
                            break;
                        case 11:
                            pos = i + 9;
                            break;
                        case 12:
                            pos = i + 8;
                            break;
                        case 13:
                            pos = i + 7;
                            break;
                        case 14:
                            pos = i + 6;
                            break;
                        case 15:
                            pos = i + 5;
                            break;
                        case 16:
                            pos = i + 4;
                            break;
                        case 17:
                            pos = i + 3;
                            break;
                        case 18:
                            pos = i + 2;
                            break;
                        case 19:
                            pos = i + 1;
                            break;
                        case 20:
                            pos = i;
                            break;

                        default:
                            break;
                    }
                } else {
                    pos = i;
                }
                break;
            }
        }

        switch (pos) {
            case 0:
                return R.id.img_0;
            case 1:
                return R.id.img_1;
            case 2:
                return R.id.img_2;
            case 3:
                return R.id.img_3;
            case 4:
                return R.id.img_4;
            case 5:
                return R.id.img_5;
            case 6:
                return R.id.img_6;
            case 7:
                return R.id.img_7;
            case 8:
                return R.id.img_8;
            case 9:
                return R.id.img_9;
            case 10:
                return R.id.img_10;
            case 11:
                return R.id.img_11;
            case 12:
                return R.id.img_12;
            case 13:
                return R.id.img_13;
            case 14:
                return R.id.img_14;
            case 15:
                return R.id.img_15;
            case 16:
                return R.id.img_16;
            case 17:
                return R.id.img_17;
            case 18:
                return R.id.img_18;
            case 19:
                return R.id.img_19;

            default:
                return -1;
        }
    }

    /**
     * ����ÿ����ť��ָʾ��ͼ��
     *
     * @param btnId
     * @param views
     * @param buttonIds
     * @param state
     */
    public static void setIndViewResource(Context context, int btnId, RemoteViews views, String layoutName,
                                          int[] buttonIds, int state, int intColor) {
        int pos = -1;

        for (int i = 0; i < buttonIds.length; i++) {
            if (buttonIds[i] == btnId) {
                // �ӵڶ�����ť��ʼƫ��
                if (i != 0) {
                    switch (buttonIds.length) {
                        case 1:
                            pos = i;
                            break;
                        case 2:
                            pos = i + 18;
                            break;
                        case 3:
                            pos = i + 17;
                            break;
                        case 4:
                            pos = i + 16;
                            break;
                        case 5:
                            pos = i + 15;
                            break;
                        case 6:
                            pos = i + 14;
                            break;
                        case 7:
                            pos = i + 13;
                            break;
                        case 8:
                            pos = i + 12;
                            break;
                        case 9:
                            pos = i + 11;
                            break;
                        case 10:
                            pos = i + 10;
                            break;
                        case 11:
                            pos = i + 9;
                            break;
                        case 12:
                            pos = i + 8;
                            break;
                        case 13:
                            pos = i + 7;
                            break;
                        case 14:
                            pos = i + 6;
                            break;
                        case 15:
                            pos = i + 5;
                            break;
                        case 16:
                            pos = i + 4;
                            break;
                        case 17:
                            pos = i + 3;
                            break;
                        case 18:
                            pos = i + 2;
                            break;
                        case 19:
                            pos = i + 1;
                            break;
                        case 20:
                            pos = i;
                            break;

                        default:
                            break;
                    }
                } else {
                    pos = i;
                }
                break;
            }
        }

        if (layoutName.equals(context.getText(R.string.list_pre_bg_custom))
                || layoutName.equals(context.getText(R.string.list_pre_bg_custom_shadow))
                || layoutName.equals(context.getText(R.string.list_pre_bg_none))) {
            // ����ʾָʾ��
            if (intColor == Constants.NOT_SHOW_FLAG) {
                switch (pos) {
                    case 0:
                        views.setViewVisibility(R.id.ind_0, View.GONE);
                        break;
                    case 1:
                        views.setViewVisibility(R.id.ind_1, View.GONE);
                        break;
                    case 2:
                        views.setViewVisibility(R.id.ind_2, View.GONE);
                        break;
                    case 3:
                        views.setViewVisibility(R.id.ind_3, View.GONE);
                        break;
                    case 4:
                        views.setViewVisibility(R.id.ind_4, View.GONE);
                        break;
                    case 5:
                        views.setViewVisibility(R.id.ind_5, View.GONE);
                        break;
                    case 6:
                        views.setViewVisibility(R.id.ind_6, View.GONE);
                        break;
                    case 7:
                        views.setViewVisibility(R.id.ind_7, View.GONE);
                        break;
                    case 8:
                        views.setViewVisibility(R.id.ind_8, View.GONE);
                        break;
                    case 9:
                        views.setViewVisibility(R.id.ind_9, View.GONE);
                        break;
                    case 10:
                        views.setViewVisibility(R.id.ind_10, View.GONE);
                        break;
                    case 11:
                        views.setViewVisibility(R.id.ind_11, View.GONE);
                        break;
                    case 12:
                        views.setViewVisibility(R.id.ind_12, View.GONE);
                        break;
                    case 13:
                        views.setViewVisibility(R.id.ind_13, View.GONE);
                        break;
                    case 14:
                        views.setViewVisibility(R.id.ind_14, View.GONE);
                        break;
                    case 15:
                        views.setViewVisibility(R.id.ind_15, View.GONE);
                        break;
                    case 16:
                        views.setViewVisibility(R.id.ind_16, View.GONE);
                        break;
                    case 17:
                        views.setViewVisibility(R.id.ind_17, View.GONE);
                        break;
                    case 18:
                        views.setViewVisibility(R.id.ind_18, View.GONE);
                        break;
                    case 19:
                        views.setViewVisibility(R.id.ind_19, View.GONE);
                        break;

                    default:
                        break;
                }
            }
            // ���ʹ����ɫ�˾�
            else {
                switch (pos) {
                    case 0:
                        views.setViewVisibility(R.id.ind_0, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_0,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 1:
                        views.setViewVisibility(R.id.ind_1, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_1,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 2:
                        views.setViewVisibility(R.id.ind_2, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_2,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 3:
                        views.setViewVisibility(R.id.ind_3, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_3,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 4:
                        views.setViewVisibility(R.id.ind_4, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_4,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 5:
                        views.setViewVisibility(R.id.ind_5, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_5,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 6:
                        views.setViewVisibility(R.id.ind_6, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_6,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 7:
                        views.setViewVisibility(R.id.ind_7, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_7,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 8:
                        views.setViewVisibility(R.id.ind_8, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_8,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 9:
                        views.setViewVisibility(R.id.ind_9, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_9,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 10:
                        views.setViewVisibility(R.id.ind_10, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_10,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 11:
                        views.setViewVisibility(R.id.ind_11, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_11,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 12:
                        views.setViewVisibility(R.id.ind_12, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_12,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 13:
                        views.setViewVisibility(R.id.ind_13, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_13,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 14:
                        views.setViewVisibility(R.id.ind_14, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_14,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 15:
                        views.setViewVisibility(R.id.ind_15, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_15,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 16:
                        views.setViewVisibility(R.id.ind_16, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_16,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 17:
                        views.setViewVisibility(R.id.ind_17, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_17,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 18:
                        views.setViewVisibility(R.id.ind_18, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_18,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;
                    case 19:
                        views.setViewVisibility(R.id.ind_19, View.VISIBLE);
                        views.setImageViewBitmap(R.id.ind_19,
                                getIndImgBitmap(context, state, views, layoutName, intColor, buttonIds));
                        break;

                    default:
                        break;
                }
            }
        } else {
            switch (pos) {
                case 0:
                    views.setViewVisibility(R.id.ind_0, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_0,
                            getIndImgSource(context, state, IND_POS_LEFT, views, layoutName, intColor, buttonIds));
                    break;
                case 1:
                    views.setViewVisibility(R.id.ind_1, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_1,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 2:
                    views.setViewVisibility(R.id.ind_2, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_2,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 3:
                    views.setViewVisibility(R.id.ind_3, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_3,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 4:
                    views.setViewVisibility(R.id.ind_4, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_4,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 5:
                    views.setViewVisibility(R.id.ind_5, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_5,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 6:
                    views.setViewVisibility(R.id.ind_6, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_6,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 7:
                    views.setViewVisibility(R.id.ind_7, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_7,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 8:
                    views.setViewVisibility(R.id.ind_8, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_8,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 9:
                    views.setViewVisibility(R.id.ind_9, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_9,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 10:
                    views.setViewVisibility(R.id.ind_10, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_10,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 11:
                    views.setViewVisibility(R.id.ind_11, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_11,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 12:
                    views.setViewVisibility(R.id.ind_12, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_12,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 13:
                    views.setViewVisibility(R.id.ind_13, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_13,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 14:
                    views.setViewVisibility(R.id.ind_14, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_14,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 15:
                    views.setViewVisibility(R.id.ind_15, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_15,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 16:
                    views.setViewVisibility(R.id.ind_16, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_16,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 17:
                    views.setViewVisibility(R.id.ind_17, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_17,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 18:
                    views.setViewVisibility(R.id.ind_18, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_18,
                            getIndImgSource(context, state, IND_POS_CENTER, views, layoutName, intColor, buttonIds));
                    break;
                case 19:
                    views.setViewVisibility(R.id.ind_19, View.VISIBLE);
                    views.setImageViewResource(R.id.ind_19,
                            getIndImgSource(context, state, IND_POS_RIGHT, views, layoutName, intColor, buttonIds));
                    break;

                default:
                    break;
            }
        }
    }

    private static Bitmap getIndImgBitmap(Context context, int state, RemoteViews views, String layoutName,
                                          int indColor, int[] buttonIds) {
        if (layoutName.equals(context.getString(R.string.list_pre_bg_none))) {
            if (state == STATE_INTERMEDIATE) {
                return Utils.setIndColor(context, R.drawable.ind_nobg_on, 170, indColor);
            } else if (state == STATE_ENABLED) {
                return Utils.setIndColor(context, R.drawable.ind_nobg_on, 255, indColor);
            } else {
                return Utils.setIndColor(context, R.drawable.ind_nobg_on, 85, indColor);
            }
        } else {
            int red = Color.red(indColor);
            int blue = Color.blue(indColor);
            int green = Color.green(indColor);

            if (state == STATE_INTERMEDIATE) {
                return Utils.createBitmap(Color.argb(170, red, green, blue));
            } else if (state == STATE_ENABLED) {
                return Utils.createBitmap(Color.argb(255, red, green, blue));
            } else {
                return Utils.createBitmap(Color.argb(85, red, green, blue));
            }
        }
    }

    /**
     * ����λ�ã�״̬����ɫ�ҵ���Դ
     *
     * @param pos
     * @param state
     * @param views
     * @param intColor
     * @param buttonIds
     * @return
     */
    private static int getIndImgSource(Context context, int state, int pos, RemoteViews views, String layoutName,
                                       int intColor, int[] buttonIds) {
        // �����ֻ��һ����ť����ֻ��״̬�ı仯����ʹ�����߶���Բ�ǵ�ͼƬ
        if (buttonIds.length == 1) {
            if (state == STATE_INTERMEDIATE) {
                switch (intColor) {
                    case Constants.IND_COLOR_PINK:
                        return R.drawable.ind_pink_single_mid;
                    case Constants.IND_COLOR_RED:
                        return R.drawable.ind_red_single_mid;
                    case Constants.IND_COLOR_YELLOW:
                        return R.drawable.ind_yellow_single_mid;
                    case Constants.IND_COLOR_ORANGE:
                        return R.drawable.ind_orange_single_mid;
                    case Constants.IND_COLOR_DEFAULT:
                        return R.drawable.ind_single_mid;
                    case Constants.IND_COLOR_GREEN:
                        return R.drawable.ind_green_single_mid;
                    case Constants.IND_COLOR_LIGHTBLUE:
                        return R.drawable.ind_lightblue_single_mid;
                    case Constants.IND_COLOR_BLUE:
                        return R.drawable.ind_blue_single_mid;
                    case Constants.IND_COLOR_PURPLE:
                        return R.drawable.ind_purple_single_mid;
                    case Constants.IND_COLOR_GRAY:
                        return R.drawable.ind_gray_single_mid;
                    default:
                        return R.drawable.ind_single_mid;
                }
            } else if (state == STATE_ENABLED) {
                switch (intColor) {
                    case Constants.IND_COLOR_PINK:
                        return R.drawable.ind_pink_single_on;
                    case Constants.IND_COLOR_RED:
                        return R.drawable.ind_red_single_on;
                    case Constants.IND_COLOR_ORANGE:
                        return R.drawable.ind_orange_single_on;
                    case Constants.IND_COLOR_YELLOW:
                        return R.drawable.ind_yellow_single_on;
                    case Constants.IND_COLOR_DEFAULT:
                        return R.drawable.ind_single_on;
                    case Constants.IND_COLOR_GREEN:
                        return R.drawable.ind_green_single_on;
                    case Constants.IND_COLOR_LIGHTBLUE:
                        return R.drawable.ind_lightblue_single_on;
                    case Constants.IND_COLOR_BLUE:
                        return R.drawable.ind_blue_single_on;
                    case Constants.IND_COLOR_PURPLE:
                        return R.drawable.ind_purple_single_on;
                    case Constants.IND_COLOR_GRAY:
                        return R.drawable.ind_gray_single_on;
                    default:
                        return R.drawable.ind_single_on;
                }
            } else {
                if (layoutName.equals(context.getString(R.string.list_pre_bg_white))) {
                    return R.drawable.ind_single_off_w;
                } else {
                    return R.drawable.ind_single_off;
                }
            }
        } else {
            // �ұߵ�λ��
            if (pos == IND_POS_RIGHT) {
                if (state == STATE_INTERMEDIATE) {
                    switch (intColor) {
                        case Constants.IND_COLOR_PINK:
                            return R.drawable.ind_pink_mid_r;
                        case Constants.IND_COLOR_RED:
                            return R.drawable.ind_red_mid_r;
                        case Constants.IND_COLOR_ORANGE:
                            return R.drawable.ind_orange_mid_r;
                        case Constants.IND_COLOR_YELLOW:
                            return R.drawable.ind_yellow_mid_r;
                        case Constants.IND_COLOR_DEFAULT:
                            return R.drawable.ind_mid_r;
                        case Constants.IND_COLOR_GREEN:
                            return R.drawable.ind_green_mid_r;
                        case Constants.IND_COLOR_LIGHTBLUE:
                            return R.drawable.ind_lightblue_mid_r;
                        case Constants.IND_COLOR_BLUE:
                            return R.drawable.ind_blue_mid_r;
                        case Constants.IND_COLOR_PURPLE:
                            return R.drawable.ind_purple_mid_r;
                        case Constants.IND_COLOR_GRAY:
                            return R.drawable.ind_gray_mid_r;
                        default:
                            return R.drawable.ind_mid_r;
                    }
                } else if (state == STATE_ENABLED) {
                    switch (intColor) {
                        case Constants.IND_COLOR_PINK:
                            return R.drawable.ind_pink_on_r;
                        case Constants.IND_COLOR_RED:
                            return R.drawable.ind_red_on_r;
                        case Constants.IND_COLOR_ORANGE:
                            return R.drawable.ind_orange_on_r;
                        case Constants.IND_COLOR_YELLOW:
                            return R.drawable.ind_yellow_on_r;
                        case Constants.IND_COLOR_DEFAULT:
                            return R.drawable.ind_on_r;
                        case Constants.IND_COLOR_GREEN:
                            return R.drawable.ind_green_on_r;
                        case Constants.IND_COLOR_LIGHTBLUE:
                            return R.drawable.ind_lightblue_on_r;
                        case Constants.IND_COLOR_BLUE:
                            return R.drawable.ind_blue_on_r;
                        case Constants.IND_COLOR_PURPLE:
                            return R.drawable.ind_purple_on_r;
                        case Constants.IND_COLOR_GRAY:
                            return R.drawable.ind_gray_on_r;
                        default:
                            return R.drawable.ind_on_r;
                    }
                } else {
                    if (layoutName.equals(context.getString(R.string.list_pre_bg_white))) {
                        return R.drawable.ind_off_r_w;
                    } else {
                        return R.drawable.ind_off_r;
                    }
                }
            }
            // ��ߵ�λ��
            else if (pos == IND_POS_LEFT) {
                if (state == STATE_INTERMEDIATE) {
                    switch (intColor) {
                        case Constants.IND_COLOR_PINK:
                            return R.drawable.ind_pink_mid_l;
                        case Constants.IND_COLOR_RED:
                            return R.drawable.ind_red_mid_l;
                        case Constants.IND_COLOR_ORANGE:
                            return R.drawable.ind_orange_mid_l;
                        case Constants.IND_COLOR_YELLOW:
                            return R.drawable.ind_yellow_mid_l;
                        case Constants.IND_COLOR_DEFAULT:
                            return R.drawable.ind_mid_l;
                        case Constants.IND_COLOR_GREEN:
                            return R.drawable.ind_green_mid_l;
                        case Constants.IND_COLOR_LIGHTBLUE:
                            return R.drawable.ind_lightblue_mid_l;
                        case Constants.IND_COLOR_BLUE:
                            return R.drawable.ind_blue_mid_l;
                        case Constants.IND_COLOR_PURPLE:
                            return R.drawable.ind_purple_mid_l;
                        case Constants.IND_COLOR_GRAY:
                            return R.drawable.ind_gray_mid_l;
                        default:
                            return R.drawable.ind_mid_l;
                    }
                } else if (state == STATE_ENABLED) {
                    switch (intColor) {
                        case Constants.IND_COLOR_PINK:
                            return R.drawable.ind_pink_on_l;
                        case Constants.IND_COLOR_RED:
                            return R.drawable.ind_red_on_l;
                        case Constants.IND_COLOR_ORANGE:
                            return R.drawable.ind_orange_on_l;
                        case Constants.IND_COLOR_YELLOW:
                            return R.drawable.ind_yellow_on_l;
                        case Constants.IND_COLOR_DEFAULT:
                            return R.drawable.ind_on_l;
                        case Constants.IND_COLOR_GREEN:
                            return R.drawable.ind_green_on_l;
                        case Constants.IND_COLOR_LIGHTBLUE:
                            return R.drawable.ind_lightblue_on_l;
                        case Constants.IND_COLOR_BLUE:
                            return R.drawable.ind_blue_on_l;
                        case Constants.IND_COLOR_PURPLE:
                            return R.drawable.ind_purple_on_l;
                        case Constants.IND_COLOR_GRAY:
                            return R.drawable.ind_gray_on_l;
                        default:
                            return R.drawable.ind_on_l;
                    }
                } else {
                    if (layoutName.equals(context.getString(R.string.list_pre_bg_white))) {
                        return R.drawable.ind_off_l_w;
                    } else {
                        return R.drawable.ind_off_l;
                    }
                }
            }
            // ���������м�λ��
            else {
                if (state == STATE_INTERMEDIATE) {
                    switch (intColor) {
                        case Constants.IND_COLOR_PINK:
                            return R.drawable.ind_pink_mid_c;
                        case Constants.IND_COLOR_RED:
                            return R.drawable.ind_red_mid_c;
                        case Constants.IND_COLOR_ORANGE:
                            return R.drawable.ind_orange_mid_c;
                        case Constants.IND_COLOR_YELLOW:
                            return R.drawable.ind_yellow_mid_c;
                        case Constants.IND_COLOR_DEFAULT:
                            return R.drawable.ind_mid_c;
                        case Constants.IND_COLOR_GREEN:
                            return R.drawable.ind_green_mid_c;
                        case Constants.IND_COLOR_LIGHTBLUE:
                            return R.drawable.ind_lightblue_mid_c;
                        case Constants.IND_COLOR_BLUE:
                            return R.drawable.ind_blue_mid_c;
                        case Constants.IND_COLOR_PURPLE:
                            return R.drawable.ind_purple_mid_c;
                        case Constants.IND_COLOR_GRAY:
                            return R.drawable.ind_gray_mid_c;
                        default:
                            return R.drawable.ind_mid_c;
                    }
                } else if (state == STATE_ENABLED) {
                    switch (intColor) {
                        case Constants.IND_COLOR_PINK:
                            return R.drawable.ind_pink_on_c;
                        case Constants.IND_COLOR_RED:
                            return R.drawable.ind_red_on_c;
                        case Constants.IND_COLOR_ORANGE:
                            return R.drawable.ind_orange_on_c;
                        case Constants.IND_COLOR_YELLOW:
                            return R.drawable.ind_yellow_on_c;
                        case Constants.IND_COLOR_DEFAULT:
                            return R.drawable.ind_on_c;
                        case Constants.IND_COLOR_GREEN:
                            return R.drawable.ind_green_on_c;
                        case Constants.IND_COLOR_LIGHTBLUE:
                            return R.drawable.ind_lightblue_on_c;
                        case Constants.IND_COLOR_BLUE:
                            return R.drawable.ind_blue_on_c;
                        case Constants.IND_COLOR_PURPLE:
                            return R.drawable.ind_purple_on_c;
                        case Constants.IND_COLOR_GRAY:
                            return R.drawable.ind_gray_on_c;
                        default:
                            return R.drawable.ind_on_c;
                    }
                } else {
                    if (layoutName.equals(context.getString(R.string.list_pre_bg_white))) {
                        return R.drawable.ind_off_c_w;
                    } else {
                        return R.drawable.ind_off_c;
                    }
                }
            }
        }
    }

    /**
     * ����ͼ��
     *
     * @param context
     * @param views
     * @param viewId
     * @param source
     * @param state
     * @param color
     * @param alpha
     */
    private static void setIconResource(Context context, RemoteViews views, int viewId, int source, int state,
                                        Integer color, Integer alpha) {
        Drawable drawable = context.getResources().getDrawable(source);
        String theme = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.PREFS_ICON_THEME, "1");

        if (theme.equals("2")) {
            try {
                Resources res = context.getPackageManager().getResourcesForApplication(Constants.PKG_THEME_HOLO);
                String resName = context.getResources().getResourceEntryName(source);
                drawable = res.getDrawable(res.getIdentifier(resName, "drawable", Constants.PKG_THEME_HOLO));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Integer vcolor = ((color == null || color == Constants.NOT_SHOW_FLAG) ? null : color);

        // ״̬Ϊonʱֱ������ͼ��,����ɫ�Ļ�������ɫ
        if (state == STATE_ENABLED) {
            // ******����һ��Ҫ����͸����Ϊ255���������֮ǰ��������͸�����Ҵ���null�ᵼ��ֻ�ı���ɫ��͸���Ȳ���
            views.setImageViewBitmap(viewId, Utils.setIconColor(context, drawable, 255, vcolor));
        } else if (state == STATE_DISABLED) {
            // ����ͬʱ������ɫ��͸����
            views.setImageViewBitmap(viewId, Utils.setIconColor(context, drawable, alpha, vcolor));
        } else {
            views.setImageViewBitmap(viewId, Utils.setIconColor(context, drawable, (255 + alpha) / 2, vcolor));
        }
    }

    /**
     * ����ͼ��
     *
     * @param context
     * @param views
     * @param viewId
     * @param source
     * @param state
     * @param color
     * @param alpha
     */
    private static void setIconResource(Context context, RemoteViews views, int viewId, Bitmap bitmap, int state,
                                        Integer color, Integer alpha) {
        Integer vcolor = (color == Constants.NOT_SHOW_FLAG ? null : color);

        // ״̬Ϊonʱֱ������ͼ��,����ɫ�Ļ�������ɫ
        if (state == STATE_ENABLED) {
            // ******����һ��Ҫ����͸����Ϊ255���������֮ǰ��������͸�����Ҵ���null�ᵼ��ֻ�ı���ɫ��͸���Ȳ���
            views.setImageViewBitmap(viewId, Utils.setIconColor(context, bitmap, 255, vcolor));
        } else if (state == STATE_INTERMEDIATE) {
            views.setImageViewBitmap(viewId, Utils.setIconColor(context, bitmap, (255 + alpha) / 2, vcolor));
        } else {
            // ����ͬʱ������ɫ��͸����
            views.setImageViewBitmap(viewId, Utils.setIconColor(context, bitmap, alpha, vcolor));
        }
    }

    public static void freeMemory(int iconId) {
        switch (iconId) {
            case Constants.ICON_AIRPLANE:
                WidgetProviderUtil.BITMAP_AIRPLANE = null;
                break;
            case Constants.ICON_AUTOLOCK:
                WidgetProviderUtil.BITMAP_AUTOLOCK = null;
                break;
            case Constants.ICON_BATTERY:
                WidgetProviderUtil.BITMAP_BATTERY = null;
                break;
            case Constants.ICON_BLUETOOTH:
                WidgetProviderUtil.BITMAP_BLUETOOTH = null;
                break;
            case Constants.ICON_BRIGHTNESS:
                WidgetProviderUtil.BITMAP_BRIGHTNESS = null;
                break;
            case Constants.ICON_AUTO_BRIGHTNESS:
                WidgetProviderUtil.BITMAP_AUTO_BRIGHT = null;
                break;
            case Constants.ICON_EDGE:
                WidgetProviderUtil.BITMAP_EDGE = null;
                break;
            case Constants.ICON_FLASHLIGHT:
                WidgetProviderUtil.BITMAP_FLASHLIGHT = null;
                break;
            case Constants.ICON_GPS:
                WidgetProviderUtil.BITMAP_GPS = null;
                break;
            case Constants.ICON_GRAVITY:
                WidgetProviderUtil.BITMAP_GRAVITY = null;
                break;
            case Constants.ICON_LOCK_SCREEN:
                WidgetProviderUtil.BITMAP_LOCK_SCREEN = null;
                break;
            case Constants.ICON_MOUNT:
                WidgetProviderUtil.BITMAP_MOUNT = null;
                break;
            case Constants.ICON_NET_SWITCH:
                WidgetProviderUtil.BITMAP_NET_SWITCH = null;
                break;
            case Constants.ICON_REBOOT:
                WidgetProviderUtil.BITMAP_REBOOT = null;
                break;
            case Constants.ICON_SCANMEDIA:
                WidgetProviderUtil.BITMAP_SCANMEDIA = null;
                break;
            case Constants.ICON_SCREEN_TIMEOUT:
                WidgetProviderUtil.BITMAP_SCREEN_TIMEOUT = null;
                break;
            case Constants.ICON_SPEAKER:
                WidgetProviderUtil.BITMAP_SPEAKER = null;
                break;
            case Constants.ICON_SYNC:
                WidgetProviderUtil.BITMAP_SYNC = null;
                break;
            case Constants.ICON_UNLOCK:
                WidgetProviderUtil.BITMAP_UNLOCK = null;
                break;
            case Constants.ICON_USBTE:
                WidgetProviderUtil.BITMAP_USBTE = null;
                break;
            case Constants.ICON_VIBRATE:
                WidgetProviderUtil.BITMAP_VIBRATE = null;
                break;
            case Constants.ICON_SILENT:
                WidgetProviderUtil.BITMAP_SILENT = null;
                break;
            case Constants.ICON_WIFI:
                WidgetProviderUtil.BITMAP_WIFI = null;
                break;
            case Constants.ICON_WIFI_SLEEP:
                WidgetProviderUtil.BITMAP_WIFI_SLEEP = null;
                break;
            case Constants.ICON_WIFIAP:
                WidgetProviderUtil.BITMAP_WIFIAP = null;
                break;
            case Constants.ICON_WIMAX:
                WidgetProviderUtil.BITMAP_WIMAX = null;
                break;
            default:
                break;
        }
    }

    /**
     * @param context
     * @return key:widgetid value:size
     */
    public static SparseIntArray getAllWidget(Context context, boolean containsNotification) {
        // ��ȡ��ǰ�Ѿ������Ĳ���
        String pkgName = WidgetProviderX4.class.getPackage().getName();

        final int[] widgetIdsX3 = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(pkgName, WidgetProviderX3.class.getName()));
        final int[] widgetIdsX2 = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(pkgName, WidgetProviderX2.class.getName()));
        final int[] widgetIdsX1 = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(pkgName, WidgetProviderX1.class.getName()));
        final int[] widgetIdsX4 = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(pkgName, WidgetProviderX4.class.getName()));
        final int[] widgetIdsX5 = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(pkgName, WidgetProviderX5.class.getName()));

        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        // key:widgetid value:size
        final SparseIntArray map = new SparseIntArray();

        if (widgetIdsX1.length > 0) {
            for (int i = 0; i < widgetIdsX1.length; i++) {
                // �ж��Ƿ������WidgetId��������ã����û�о���Ϊ���Widgetû�д�����Launcher��
                // �������widgetʱ��Home���˳��������ڴ����Widget������
                if (config.contains(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetIdsX1[i]))) {
                    map.put(widgetIdsX1[i], 1);
                }
            }
        }

        if (widgetIdsX2.length > 0) {
            for (int i = 0; i < widgetIdsX2.length; i++) {
                if (config.contains(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetIdsX2[i]))) {
                    map.put(widgetIdsX2[i], 2);
                }
            }
        }

        if (widgetIdsX3.length > 0) {
            for (int i = 0; i < widgetIdsX3.length; i++) {
                if (config.contains(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetIdsX3[i]))) {
                    map.put(widgetIdsX3[i], 3);
                }
            }
        }

        if (widgetIdsX4.length > 0) {
            for (int i = 0; i < widgetIdsX4.length; i++) {
                if (config.contains(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetIdsX4[i]))) {
                    map.put(widgetIdsX4[i], 4);
                }
            }
        }

        if (widgetIdsX5.length > 0) {
            for (int i = 0; i < widgetIdsX5.length; i++) {
                if (config.contains(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetIdsX5[i]))) {
                    map.put(widgetIdsX5[i], 5);
                }
            }
        }

        // ����֪ͨ���еĲ���,֪ͨ��������sizeͳһΪ 0
        if (containsNotification) {
            String[] notificationWidgetIds = config.getString(Constants.PREFS_IN_NOTIFICATION_BAR, "").split(",");

            for (int i = 0; i < notificationWidgetIds.length; i++) {
                if (!notificationWidgetIds[i].equals("")) {
                    map.put(Integer.parseInt(notificationWidgetIds[i]), 0);
                }
            }
        }

        return map;
    }
}
