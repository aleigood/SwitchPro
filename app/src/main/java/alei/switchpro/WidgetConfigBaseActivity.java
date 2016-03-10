package alei.switchpro;

import alei.switchpro.color.BackCustomPreference;
import alei.switchpro.color.DividerCustomPreference;
import alei.switchpro.color.IconCustomPreference;
import alei.switchpro.color.IndCustomPreference;
import alei.switchpro.load.LoadConfigAdapter;
import alei.switchpro.load.LoadConfigAdapter.ListItem;
import alei.switchpro.load.XmlEntity;
import alei.switchpro.load.XmlUtil;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public abstract class WidgetConfigBaseActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String DEFAULT_BUTTON_IDS = "0,2,3,4,6";
    public static final int PREVIEW_ICON_COLOR = 0xFF4D4D4D;
    // ���пؼ���key
    private static final String KEY_LAYOUT = "list_layout";
    private static final String KEY_IND_COLOR_PICKER = "ind_color";
    private static final String KEY_ICON_COLOR_PICKER = "icon_color";
    private static final String KEY_BACK_COLOR_PICKER = "back_color";
    private static final String KEY_DIVIDER_COLOR_PICKER = "divider_color";
    private final float ALPHA_VALUE = 0.8F;
    public String btnIds = DEFAULT_BUTTON_IDS;
    // ����ѡ���ؼ�
    public LinearLayout preView;
    public IndCustomPreference indColorPicker;
    public IconCustomPreference iconColorPicker;
    public BackCustomPreference backColorPicker;
    public DividerCustomPreference dividerColorPicker;
    public ListPreference listLayout;
    public String layoutDefault;
    public String layoutWhite;
    public String layoutCustom;
    public String layoutCustomShadow;
    public String layoutNoBack;
    public Bitmap backBitmap;
    protected int widgetId;
    private ImageView mDragImage;
    private LayoutParams mWindowParams;
    private WindowManager mWindowManager;
    private int mDragPointX;
    private int mDragPointY;
    private int mXOffset;
    private int mYOffset;
    private int mMoveX;

    private int mMoveY;

    protected void createAction(int appWidgetId) {
        widgetId = appWidgetId;
        // ��ʼ�����ý���ؼ�
        initUI();
        // ��ʼ����������
        initUIData();
        // ��ʼ����ť��Ӧ�¼�
        initBtnAction();
        // ��ʼ��Ԥ��
        updatePreView();
    }

    protected void createAction(int appWidgetId, XmlEntity entity) {
        widgetId = appWidgetId;
        // ��ʼ�����ý���ؼ�
        initUI();
        // ��ʼ����������
        initUIData(entity);
        // ��ʼ����ť��Ӧ�¼�
        initBtnAction();
        // ��ʼ��Ԥ��
        updatePreView();
    }

    private void initBtnAction() {
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View paramView) {
                // ��widgetId�ͷ���ȡ�������û��ֱ�ӽ���
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                    setResult(RESULT_CANCELED, resultValue);
                }

                finish();
            }
        });
        Button loadButton = (Button) findViewById(R.id.load_conf);
        loadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View paramView) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(WidgetConfigBaseActivity.this);
                builder.setTitle(R.string.load_conf);
                List<XmlEntity> savedCfgList = XmlUtil.parseWidgetCfg("data");

                final LoadConfigAdapter adapter = new LoadConfigAdapter(WidgetConfigBaseActivity.this, savedCfgList);

                if (savedCfgList.size() != 0) {
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            ListItem item = (ListItem) adapter.getItem(arg1);
                            initUIData(item.entity);
                            updatePreView();
                        }
                    });

                    builder.setNeutralButton(R.string.clear_conf, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (XmlUtil.deleteFile("data")) {
                                Toast.makeText(WidgetConfigBaseActivity.this, R.string.clear_conf_succ,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    builder.setMessage(R.string.no_saved_conf);
                }

                builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });

                builder.create().show();
            }
        });

        // ȷ����ť
        Button saveButton = (Button) findViewById(R.id.button_apply);
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View paramView) {
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    saveBtnAction();
                }

                finish();
            }
        });
    }

    /**
     * ȷ����ť�¼�
     */
    protected void saveBtnAction() {
        // ��ÿ��WidgetId��Ӧ�İ�ť���ô��빲�����
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor configEditor = config.edit();

        // ��ť˳����֯���ַ�������
        configEditor.putString(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetId), btnIds);

        // ʹ�õĲ��ֵ�����,����ֱ������Դid,����������ʱ������
        configEditor.putString(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, widgetId), listLayout.getValue());

        // ��ȡͼ�����õ���ɫ
        configEditor.putInt(String.format(Constants.PREFS_ICON_COLOR_FIELD_PATTERN, widgetId),
                iconColorPicker.getLastColor());
        configEditor.putInt(String.format(Constants.PREFS_ICON_TRANS_FIELD_PATTERN, widgetId),
                iconColorPicker.getLastTrans());

        // ��ȡָʾ�����õ���ɫ
        configEditor.putInt(String.format(Constants.PREFS_IND_COLOR_FIELD_PATTERN, widgetId),
                indColorPicker.getLastColor());

        // ��ȡ�������õ���ɫ
        if (backBitmap != null) {
            try {
                String fileName = widgetId + "_back.png";
                FileOutputStream fileOutputStream = openFileOutput(fileName, 0);
                backBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.close();
                configEditor.putString(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId), fileName);

                // ���������б�Ŀǰû�и��õĸ��·���
                getListView().invalidateViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            configEditor.putInt(String.format(Constants.PREFS_BACK_COLOR_FIELD_PATTERN, widgetId),
                    backColorPicker.getLastColor());

            // ɾ������ͼƬ�����ú�ͼƬ�ļ�
            if (config.contains(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId))) {
                String fileName = config.getString(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId),
                        "");
                configEditor.remove(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId));

                // ɾ������ͼƬ�ļ�
                deleteFile(fileName);
            }
        }

        // ��ȡ�ָ��ߵ���ɫ
        configEditor.putInt(String.format(Constants.PREFS_DIVIDER_COLOR_FIELD_PATTERN, widgetId),
                dividerColorPicker.getLastColor());

        // �������һ������
        configEditor.putString(Constants.PREFS_LAST_BUTTONS_ORDER, btnIds);
        configEditor.putString(Constants.PREFS_LAST_BACKGROUND, listLayout.getValue());
        configEditor.putInt(Constants.PREFS_LAST_ICON_COLOR, iconColorPicker.getLastColor());
        configEditor.putInt(Constants.PREFS_LAST_ICON_TRANS, iconColorPicker.getLastTrans());
        configEditor.putInt(Constants.PREFS_LAST_IND_COLOR, indColorPicker.getLastColor());
        configEditor.putInt(Constants.PREFS_LAST_DIVIDER_COLOR, dividerColorPicker.getLastColor());
        // ������û��ʹ�ñ���ͼƬ�����汳����ɫ
        configEditor.putInt(Constants.PREFS_LAST_BACK_COLOR, backColorPicker.getLastColor());

        configEditor.commit();
    }

    /**
     * �������õ�SD���ϣ�ÿ�ε��ȷ��ʱ�������������½������޸�
     *
     * @param config
     * @param widgetId
     * @param isModify
     * @param oldIds
     */
    protected void saveCfgToSD(SharedPreferences config, boolean isModify, String oldIds) {
        String newIds = btnIds;

        XmlEntity entity = new XmlEntity();
        entity.setBtnIds(newIds);
        entity.setIconColor(iconColorPicker.getLastColor());
        entity.setIconTrans(iconColorPicker.getLastTrans());
        entity.setIndColor(indColorPicker.getLastColor());
        entity.setLayoutName(listLayout.getValue());
        entity.setBackColor(backColorPicker.getLastColor());
        entity.setDividerColor(dividerColorPicker.getLastColor());

        List<XmlEntity> list = XmlUtil.parseWidgetCfg("data");
        XmlEntity theSameEntity = getSameConf(list, newIds);
        boolean hasSameConf = theSameEntity == null ? false : true;
        boolean saveSuccess = false;

        // ������½���Widget,���Ƿ��Ѿ��������widget���������ж�
        if (!isModify) {
            // ����ǵ�һ�δ���widget���ߴ�����һ����ͬ��Widget
            if (list.size() == 0 || !hasSameConf) {
                list.add(entity);
            }
            // ���������һ����ͬ��widget(����ֻ��˳��ͬ)���򸲸��Ѿ����ڵ����ã�������ť��˳��
            else {
                theSameEntity.setBtnIds(newIds);
                theSameEntity.setIconColor(iconColorPicker.getLastColor());
                theSameEntity.setIconTrans(iconColorPicker.getLastTrans());
                theSameEntity.setIndColor(indColorPicker.getLastColor());
                theSameEntity.setLayoutName(listLayout.getValue());
                theSameEntity.setBackColor(backColorPicker.getLastColor());
                theSameEntity.setDividerColor(dividerColorPicker.getLastColor());
            }
        }
        // ������޸Ľ���ʱ���ҵ�ԭ����������ò��޸�
        else {
            XmlEntity tmpEntity = getSameConf(list, oldIds);

            if (tmpEntity == null) {
                tmpEntity = new XmlEntity();
                list.add(tmpEntity);
            }

            tmpEntity.setBtnIds(newIds);
            tmpEntity.setIconColor(iconColorPicker.getLastColor());
            tmpEntity.setIconTrans(iconColorPicker.getLastTrans());
            tmpEntity.setIndColor(indColorPicker.getLastColor());
            tmpEntity.setLayoutName(listLayout.getValue());
            tmpEntity.setBackColor(backColorPicker.getLastColor());
            tmpEntity.setDividerColor(dividerColorPicker.getLastColor());
        }

        // ���浽"data"�ļ���
        saveSuccess = XmlUtil.writeWidgetXml(list, "data");

        if (!saveSuccess) {
            Toast.makeText(this, R.string.save_conf_error, Toast.LENGTH_SHORT).show();
        }
    }

    private XmlEntity getSameConf(List<XmlEntity> savedList, String btnIds) {
        if (savedList == null) {
            return null;
        }

        for (XmlEntity xmlEntity : savedList) {
            String savedIds = xmlEntity.getBtnIds();
            String[] part = savedIds.split(",");
            String[] part2 = btnIds.split(",");
            List<String> partList = Arrays.asList(part);
            List<String> part2List = Arrays.asList(part2);

            if (part.length == part2.length && partList.containsAll(part2List)) {
                return xmlEntity;
            }
        }

        return null;
    }

    protected abstract void updateWidget(int appWidgetId);

    /**
     * ��ʼ�����ý��������
     *
     * @param appWidgetId
     */
    protected void initUI() {
        layoutDefault = getResources().getString(R.string.list_pre_bg_default);
        layoutWhite = getResources().getString(R.string.list_pre_bg_white);
        layoutCustom = getResources().getString(R.string.list_pre_bg_custom);
        layoutCustomShadow = getResources().getString(R.string.list_pre_bg_custom_shadow);
        layoutNoBack = getResources().getString(R.string.list_pre_bg_none);

        preView = (LinearLayout) findViewById(R.id.pre_view);
        listLayout = (ListPreference) findPreference(KEY_LAYOUT);
        listLayout.setEntries(buildBackgroundEntries());
        listLayout.setEntryValues(buildBackgroundEntries());

        // ��ʼ����ɫѡ����
        backColorPicker = (BackCustomPreference) findPreference(KEY_BACK_COLOR_PICKER);
        indColorPicker = (IndCustomPreference) findPreference(KEY_IND_COLOR_PICKER);
        iconColorPicker = (IconCustomPreference) findPreference(KEY_ICON_COLOR_PICKER);
        dividerColorPicker = (DividerCustomPreference) findPreference(KEY_DIVIDER_COLOR_PICKER);

        OnClickListener btnOnClickListener = new OnClickListener() {
            public void onClick(View paramView) {
                int btnId = (Integer) paramView.getTag();
                boolean isExist = false;
                String[] ids = btnIds.split(",");

                for (int i = 0; i < ids.length; i++) {
                    if (!ids[i].equals("") && btnId == Integer.parseInt(ids[i])) {
                        isExist = true;
                        break;
                    }
                }

                if (isExist) {
                    Toast.makeText(WidgetConfigBaseActivity.this, R.string.button_already_exists, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    if (ids.length == 20) {
                        Toast.makeText(WidgetConfigBaseActivity.this, R.string.button_max, Toast.LENGTH_SHORT).show();
                    } else {
                        if (ids.length == 1 && ids[0].equals("")) {
                            btnIds += btnId;
                        } else {
                            btnIds += "," + btnId;
                        }

                        updatePreView();
                    }
                }
            }
        };

        ImageView airplane = ((ImageView) findViewById(R.id.btn_aireplane));
        airplane.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_airplane_on, 255, PREVIEW_ICON_COLOR));
        airplane.setTag(Constants.BUTTON_AIRPLANE);
        airplane.setOnClickListener(btnOnClickListener);

        ImageView autoLock = ((ImageView) findViewById(R.id.btn_autolock));
        autoLock.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_autolock_on, 255, PREVIEW_ICON_COLOR));
        autoLock.setTag(Constants.BUTTON_AUTOLOCK);
        autoLock.setOnClickListener(btnOnClickListener);

        ImageView battery = ((ImageView) findViewById(R.id.btn_battery));
        battery.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_battery_full, 255, PREVIEW_ICON_COLOR));
        battery.setTag(Constants.BUTTON_BATTERY);
        battery.setOnClickListener(btnOnClickListener);

        ImageView brightness = ((ImageView) findViewById(R.id.btn_brightness));
        brightness.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_brightness_on, 255, PREVIEW_ICON_COLOR));
        brightness.setTag(Constants.BUTTON_BRIGHTNESS);
        brightness.setOnClickListener(btnOnClickListener);

        ImageView bluetooth = ((ImageView) findViewById(R.id.btn_bt));
        bluetooth.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_bluetooth_on, 255, PREVIEW_ICON_COLOR));
        bluetooth.setTag(Constants.BUTTON_BLUETOOTH);
        bluetooth.setOnClickListener(btnOnClickListener);

        ImageView data = ((ImageView) findViewById(R.id.btn_data));
        data.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_edge_on, 255, PREVIEW_ICON_COLOR));
        data.setTag(Constants.BUTTON_EDGE);
        data.setOnClickListener(btnOnClickListener);

        ImageView flashlight = ((ImageView) findViewById(R.id.btn_flashlight));
        flashlight.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_flashlight_on, 255, PREVIEW_ICON_COLOR));
        flashlight.setTag(Constants.BUTTON_FLASHLIGHT);
        flashlight.setOnClickListener(btnOnClickListener);

        ImageView gps = ((ImageView) findViewById(R.id.btn_GPS));
        gps.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_gps_on, 255, PREVIEW_ICON_COLOR));
        gps.setTag(Constants.BUTTON_GPS);
        gps.setOnClickListener(btnOnClickListener);

        ImageView gravity = ((ImageView) findViewById(R.id.btn_gravity));
        gravity.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_gravity_on, 255, PREVIEW_ICON_COLOR));
        gravity.setTag(Constants.BUTTON_GRAVITY);
        gravity.setOnClickListener(btnOnClickListener);

        ImageView lockscreen = ((ImageView) findViewById(R.id.btn_lockscreen));
        lockscreen.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_lockscreen_on, 255, PREVIEW_ICON_COLOR));
        lockscreen.setTag(Constants.BUTTON_LOCK_SCREEN);
        lockscreen.setOnClickListener(btnOnClickListener);

        ImageView mount = ((ImageView) findViewById(R.id.btn_mount));
        mount.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_sdcard_on, 255, PREVIEW_ICON_COLOR));
        mount.setTag(Constants.BUTTON_MOUNT);
        mount.setOnClickListener(btnOnClickListener);

        ImageView netswitch = ((ImageView) findViewById(R.id.btn_netswitch));
        netswitch.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_netswitch_on, 255, PREVIEW_ICON_COLOR));
        netswitch.setTag(Constants.BUTTON_NET_SWITCH);
        netswitch.setOnClickListener(btnOnClickListener);

        ImageView reboot = ((ImageView) findViewById(R.id.btn_reboot));
        reboot.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_reboot_on, 255, PREVIEW_ICON_COLOR));
        reboot.setTag(Constants.BUTTON_REBOOT);
        reboot.setOnClickListener(btnOnClickListener);

        ImageView reload = ((ImageView) findViewById(R.id.btn_reload));
        reload.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_media_on, 255, PREVIEW_ICON_COLOR));
        reload.setTag(Constants.BUTTON_SCANMEDIA);
        reload.setOnClickListener(btnOnClickListener);

        ImageView screen = ((ImageView) findViewById(R.id.btn_screen));
        screen.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_screen_on, 255, PREVIEW_ICON_COLOR));
        screen.setTag(Constants.BUTTON_SCREEN_TIMEOUT);
        screen.setOnClickListener(btnOnClickListener);

        ImageView speaker = ((ImageView) findViewById(R.id.btn_speaker));
        speaker.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_speaker_on, 255, PREVIEW_ICON_COLOR));
        speaker.setTag(Constants.BUTTON_SPEAKER);
        speaker.setOnClickListener(btnOnClickListener);

        ImageView sync = ((ImageView) findViewById(R.id.btn_sync));
        sync.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_sync_on, 255, PREVIEW_ICON_COLOR));
        sync.setTag(Constants.BUTTON_SYNC);
        sync.setOnClickListener(btnOnClickListener);

        ImageView task = ((ImageView) findViewById(R.id.btn_task));
        task.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_killprocess_on, 255, PREVIEW_ICON_COLOR));
        task.setTag(Constants.BUTTON_KILL_PROCESS);
        task.setOnClickListener(btnOnClickListener);

        ImageView tether = ((ImageView) findViewById(R.id.btn_tether));
        tether.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_usbte_on, 255, PREVIEW_ICON_COLOR));
        tether.setTag(Constants.BUTTON_USBTE);
        tether.setOnClickListener(btnOnClickListener);

        ImageView unlock = ((ImageView) findViewById(R.id.btn_unlock));
        unlock.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_unlock_on, 255, PREVIEW_ICON_COLOR));
        unlock.setTag(Constants.BUTTON_UNLOCK);
        unlock.setOnClickListener(btnOnClickListener);

        ImageView vibration = ((ImageView) findViewById(R.id.btn_vibration));
        vibration.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_vibrate_on, 255, PREVIEW_ICON_COLOR));
        vibration.setTag(Constants.BUTTON_VIBRATE);
        vibration.setOnClickListener(btnOnClickListener);

        ImageView volume = ((ImageView) findViewById(R.id.btn_volume));
        volume.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_volume, 255, PREVIEW_ICON_COLOR));
        volume.setTag(Constants.BUTTON_VOLUME);
        volume.setOnClickListener(btnOnClickListener);

        ImageView wifi = ((ImageView) findViewById(R.id.btn_wifi));
        wifi.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_wifi_on, 255, PREVIEW_ICON_COLOR));
        wifi.setTag(Constants.BUTTON_WIFI);
        wifi.setOnClickListener(btnOnClickListener);

        ImageView wifiap = ((ImageView) findViewById(R.id.btn_wifiap));
        wifiap.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_wifite_on, 255, PREVIEW_ICON_COLOR));
        wifiap.setTag(Constants.BUTTON_WIFIAP);
        wifiap.setOnClickListener(btnOnClickListener);

        ImageView wifisleep = ((ImageView) findViewById(R.id.btn_wifisleep));
        wifisleep.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_wifi_sleep, 255, PREVIEW_ICON_COLOR));
        wifisleep.setTag(Constants.BUTTON_WIFI_SLEEP);
        wifisleep.setOnClickListener(btnOnClickListener);

        ImageView wimax = ((ImageView) findViewById(R.id.btn_wimax));
        wimax.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_wimax_on, 255, PREVIEW_ICON_COLOR));
        wimax.setTag(Constants.BUTTON_WIMAX);
        wimax.setOnClickListener(btnOnClickListener);

        ImageView memory = ((ImageView) findViewById(R.id.btn_memory));
        memory.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_usage_on, 255, PREVIEW_ICON_COLOR));
        memory.setTag(Constants.BUTTON_MEMORY_USAGE);
        memory.setOnClickListener(btnOnClickListener);

        ImageView storage = ((ImageView) findViewById(R.id.btn_storage));
        storage.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_usage_on, 255, PREVIEW_ICON_COLOR));
        storage.setTag(Constants.BUTTON_STORAGE_USAGE);
        storage.setOnClickListener(btnOnClickListener);

        ImageView btTether = ((ImageView) findViewById(R.id.btn_bt_tether));
        btTether.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_blutoothte_on, 255, PREVIEW_ICON_COLOR));
        btTether.setTag(Constants.BUTTON_BT_TE);
        btTether.setOnClickListener(btnOnClickListener);

        ImageView btNfc = ((ImageView) findViewById(R.id.btn_nfc));
        btNfc.setImageBitmap(Utils.setIconColor(this, R.drawable.icon_nfc_on, 255, PREVIEW_ICON_COLOR));
        btNfc.setTag(Constants.BUTTON_NFC);
        btNfc.setOnClickListener(btnOnClickListener);

        // ע��ѡ��仯������
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * ��ȡ���һ�����õİ�ť˳�����û�еĻ�����һ��Ĭ��˳������Ҫ����
     *
     * @return
     */
    protected abstract String getLastBtnOrder();

    public void initUIData(XmlEntity entity) {
        btnIds = entity.getBtnIds();
        String layoutName = entity.getLayoutName();

        listLayout.setValue(layoutName);
        listLayout.setSummary(layoutName);

        backColorPicker.setLastColor(entity.getBackColor());
        indColorPicker.setLastColor(entity.getIndColor());
        iconColorPicker.setLastColor(entity.getIconColor());
        iconColorPicker.setLastTrans(entity.getIconTrans());
        dividerColorPicker.setLastColor(entity.getDividerColor());

        PreferenceScreen cate = (PreferenceScreen) findPreference("background_category");
        // ��ʼ���������ж�����ޱ�����Ҫɾ��ѡ��
        if (layoutName.equals(layoutNoBack)) {
            cate.removePreference(backColorPicker);
            cate.removePreference(dividerColorPicker);
        } else if (layoutName.equals(layoutDefault) || layoutName.equals(layoutWhite)) {
            cate.removePreference(dividerColorPicker);
        }
    }

    private void initUIData() {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);

        // ��ȡ��ǰwidget�ı����������ȡ������ʹ�����һ�α���
        String layoutName = config.getString(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, widgetId), null);

        if (layoutName == null) {
            layoutName = config.getString(Constants.PREFS_LAST_BACKGROUND, layoutDefault);
        }

        listLayout.setValue(layoutName);
        listLayout.setSummary(layoutName);

        // ���һ�����õİ�ť˳��
        btnIds = getLastBtnOrder();
        backColorPicker.updateView();
        indColorPicker.updateView();
        iconColorPicker.updateView();
        dividerColorPicker.updateView();

        PreferenceScreen cate = (PreferenceScreen) findPreference("background_category");
        // ��ʼ���������ж�����ޱ�����Ҫɾ��ѡ��
        if (layoutName.equals(layoutNoBack)) {
            cate.removePreference(backColorPicker);
            cate.removePreference(dividerColorPicker);
        } else if (layoutName.equals(layoutDefault) || layoutName.equals(layoutWhite)) {
            cate.removePreference(dividerColorPicker);
        }
    }

    private String[] buildBackgroundEntries() {
        return new String[]{getResources().getString(R.string.list_pre_bg_default),
                getResources().getString(R.string.list_pre_bg_white),
                getResources().getString(R.string.list_pre_bg_custom),
                getResources().getString(R.string.list_pre_bg_custom_shadow),
                getResources().getString(R.string.list_pre_bg_none)};
    }

    /**
     * ���任ѡ��ʱҪ��̬���������ؼ��п�ѡ����Ŀ
     *
     * @param btnName
     * @return
     */
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(KEY_LAYOUT)) {
            String layoutName = preferences.getString(key, "");
            listLayout.setSummary(preferences.getString(key, ""));
            PreferenceScreen cate = (PreferenceScreen) findPreference("background_category");

            if (layoutName.equals(layoutCustom) || layoutName.equals(layoutCustomShadow)) {
                indColorPicker.updateView();
                cate.addPreference(backColorPicker);
                backColorPicker.updateView();
                cate.addPreference(dividerColorPicker);
            } else if (layoutName.equals(layoutNoBack)) {
                indColorPicker.updateView();
                cate.removePreference(backColorPicker);
                cate.removePreference(dividerColorPicker);
            } else {
                indColorPicker.updateView();
                cate.addPreference(backColorPicker);
                backColorPicker.updateView();
                cate.removePreference(dividerColorPicker);
            }
        }

        // ����Ԥ��
        updatePreView();
    }

    public void updatePreView() {
        preView.removeAllViews();
        RemoteViews remoteView = WidgetProviderUtil.buildAndUpdateButtons(this, widgetId, btnIds,
                listLayout.getValue(), iconColorPicker.getLastColor(), iconColorPicker.getLastTrans(),
                indColorPicker.getLastColor(), dividerColorPicker.getLastColor(), backColorPicker.getLastColor(),
                backBitmap);
        View widgetView = remoteView.apply(this, preView);
        setBtnOnClickListener(widgetView);
        preView.addView(widgetView);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_preview);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        drawable.setDither(true);
        preView.setBackgroundDrawable(drawable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        switch (requestCode) {
            case 1:
                int size = getWidgetSize();
                int y = 0;

                // ����С129600����
                switch (size) {
                    case 1:
                        y = 360;
                        break;
                    case 2:
                        y = 254;
                        break;
                    case 3:
                        y = 207;
                        break;
                    case 4:
                        y = 180;
                        break;
                    case 5:
                        y = 160;
                        break;
                    default:
                        y = 180;
                        break;
                }

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setData(data.getData());
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", size);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", y * size);
                intent.putExtra("outputY", y);
                intent.putExtra("scale", true);
                intent.putExtra("noFaceDetection", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, 2);
                break;
            case 2:
                Bundle extras = data.getExtras();

                if (extras != null) {
                    backBitmap = extras.getParcelable("data");
                    backColorPicker.updateView();
                    updatePreView();
                }
                break;
            default:
                break;
        }
    }

    protected abstract int getWidgetSize();

    public int getWidgetId() {
        return widgetId;
    }

    private void setBtnOnClickListener(final View views) {
        final String[] btnId = btnIds.split(",");
        for (int i = 0; i < btnId.length; i++) {
            int pos = -1;
            // �ӵڶ�����ť��ʼƫ��
            if (i != 0) {
                switch (btnId.length) {
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

            final View tmp = views.findViewById(getResources().getIdentifier("btn_" + pos, "id", getPackageName()));
            tmp.setTag(btnId[i]);
            tmp.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, final MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setOnLongClickListener(new OnLongClickListener() {
                            public boolean onLongClick(View v) {
                                int[] location = new int[2];
                                v.getLocationOnScreen(location);
                                mDragPointX = location[0];
                                mDragPointY = location[1];
                                mXOffset = (int) event.getRawX() - mDragPointX;
                                mYOffset = (int) event.getRawY() - mDragPointY;

                                v.setDrawingCacheEnabled(true);
                                startDrag(v, Bitmap.createBitmap(v.getDrawingCache()));

                                // �������������»�ȡ��������Ч��
                                updatePreView();
                                return true;
                            }
                        });

                        v.setOnClickListener(new OnClickListener() {
                            public void onClick(View paramView) {
                                delBtn(paramView, btnId);
                            }
                        });
                    }
                    return false;
                }
            });
        }
    }

    private void delBtn(View view, String[] btnId) {
        String newIds = "";

        for (int i = 0; i < btnId.length; i++) {
            // Ҫ��toString()������������Ч
            if (!view.getTag().toString().equals(btnId[i])) {
                if (newIds.equals("")) {
                    newIds += btnId[i];
                } else {
                    newIds += "," + btnId[i];
                }
            }
        }

        btnIds = newIds;
        updatePreView();
    }

    /**
     * ׼���϶�����ʼ���϶����ͼ��
     */
    private void startDrag(View v, Bitmap bm) {
        // �ͷ�Ӱ����׼��Ӱ���ʱ�򣬷�ֹӰ��û�ͷţ�ÿ�ζ�ִ��һ��
        if (mDragImage != null) {
            mWindowManager.removeView(mDragImage);
            mDragImage = null;
        }

        mWindowParams = new WindowManager.LayoutParams();
        // ���ϵ��¼���y�����ϵ����λ�ã�
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = mDragPointX;
        mWindowParams.y = mDragPointY;

        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // ������Щ�����ܹ�����׼ȷ��λ��ѡ������λ�ã��ճ�����
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        // ��Ӱ��ImagView��ӵ���ǰ��ͼ��
        mDragImage = new ImageView(this);
        mDragImage.setImageBitmap(bm);
        mDragImage.setPadding(0, 0, 0, 0);
        mDragImage.setTag(v.getTag());

        mWindowManager = (WindowManager) getSystemService("window");
        mWindowManager.addView(mDragImage, mWindowParams);

        // ��
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(25);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDragImage != null) {
            int action = ev.getAction();

            switch (action) {
                case MotionEvent.ACTION_UP:
                    // �ͷ��϶�Ӱ��
                    onDrop();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // �϶�Ӱ��
                    onMove((int) ev.getRawX(), (int) ev.getRawY());
                    break;
                default:
                    break;
            }
            return true;
        }

        return super.onTouchEvent(ev);
    }

    /**
     * �϶�ִ�У���Move������ִ��
     */
    public void onMove(final int x, final int y) {
        if (mDragImage != null) {
            // ����һ����͸����
            mWindowParams.alpha = ALPHA_VALUE;

            // ��������λ��
            int[] location = new int[2];
            preView.getLocationOnScreen(location);
            int maxHeight = location[1] + preView.getHeight() - mDragImage.getHeight();
            int tmp = y - mYOffset;

            mWindowParams.y = tmp > maxHeight ? maxHeight : tmp;
            mWindowParams.x = x - mXOffset;
            // ���½���
            mWindowManager.updateViewLayout(mDragImage, mWindowParams);
        }

        mMoveX = x;
        mMoveY = y;

        preView.postDelayed(new Runnable() {
            public void run() {
                if (x == mMoveX && y == mMoveY) {
                    onStop(x, y);
                }
            }
        }, 250);
    }

    private void onStop(int x, int y) {
        if (mDragImage == null) {
            return;
        }

        String tmpIds = "";
        String[] srcBtnId = btnIds.split(",");
        String dragId = mDragImage.getTag().toString();

        // ���ҵ������ť��λ�ã������ж�����ǰ�ƶ����������ƶ�
        int pos = 0;

        // �������ɾ��
        for (int i = 0; i < srcBtnId.length; i++) {
            // Ҫ��toString()������������Ч
            if (dragId.equals(srcBtnId[i])) {
                pos = i;
            } else {
                if (tmpIds.equals("")) {
                    tmpIds += srcBtnId[i];
                } else {
                    tmpIds += "," + srcBtnId[i];
                }
            }
        }

        final String[] btnId = tmpIds.split(",");

        // �ҵ�Ҫ�滻�İ�ť��id
        for (int i = 0; i < btnId.length; i++) {
            View childView = preView.findViewWithTag(btnId[i]);

            if (childView == null || childView.getTag() == null) {
                return;
            }

            String childId = childView.getTag().toString();

            // ��Ҫ�滻�İ�ť�����귶Χ�ڶ��Ҳ�������
            if (x < childView.getRight() && x > childView.getLeft() && !dragId.equals(childId)) {
                String newIds = "";

                for (int j = 0; j < btnId.length; j++) {
                    // �����Ҫ�滻�İ�ť
                    if (childId.equals(btnId[j])) {
                        if (newIds.equals("")) {
                            // ��ǰ���ƶ�
                            if (j < pos) {
                                newIds += dragId + "," + btnId[j];
                            } else {
                                newIds += btnId[j] + "," + dragId;
                            }
                        } else {
                            // ��ǰ���ƶ�
                            if (j < pos) {
                                newIds += "," + dragId + "," + btnId[j];
                            } else {
                                newIds += "," + btnId[j] + "," + dragId;
                            }
                        }
                    } else {
                        if (newIds.equals("")) {
                            newIds += btnId[j];
                        } else {
                            newIds += "," + btnId[j];
                        }
                    }
                }

                btnIds = newIds;
                updatePreView();
                break;
            }
        }
    }

    /**
     * ֹͣ�϶�
     */
    public void onDrop() {
        if (mDragImage != null) {
            mWindowManager.removeView(mDragImage);
            mDragImage = null;
        }
    }
}
