package alei.switchpro;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

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

public abstract class WidgetConfigBaseActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    public static final String DEFAULT_BUTTON_IDS = "0,2,3,4,6";
    public static final int PREVIEW_ICON_COLOR = 0xFF4D4D4D;

    public String btnIds = DEFAULT_BUTTON_IDS;

    // 下拉选择框控件
    public LinearLayout preView;
    public IndCustomPreference indColorPicker;
    public IconCustomPreference iconColorPicker;
    public BackCustomPreference backColorPicker;
    public DividerCustomPreference dividerColorPicker;
    public ListPreference listLayout;

    // 所有控件的key
    private static final String KEY_LAYOUT = "list_layout";
    private static final String KEY_IND_COLOR_PICKER = "ind_color";
    private static final String KEY_ICON_COLOR_PICKER = "icon_color";
    private static final String KEY_BACK_COLOR_PICKER = "back_color";
    private static final String KEY_DIVIDER_COLOR_PICKER = "divider_color";

    public String layoutDefault;
    public String layoutWhite;
    public String layoutCustom;
    public String layoutCustomShadow;
    public String layoutNoBack;

    protected int widgetId;

    public Bitmap backBitmap;

    private ImageView mDragImage;

    private LayoutParams mWindowParams;

    private WindowManager mWindowManager;

    private int mDragPointX;

    private int mDragPointY;

    private int mXOffset;

    private int mYOffset;

    private final float ALPHA_VALUE = 0.8F;

    private int mMoveX;

    private int mMoveY;

    protected void createAction(int appWidgetId)
    {
        widgetId = appWidgetId;
        // 初始化配置界面控件
        initUI();
        // 初始化界面数据
        initUIData();
        // 初始化按钮响应事件
        initBtnAction();
        // 初始化预览
        updatePreView();
    }

    protected void createAction(int appWidgetId, XmlEntity entity)
    {
        widgetId = appWidgetId;
        // 初始化配置界面控件
        initUI();
        // 初始化界面数据
        initUIData(entity);
        // 初始化按钮响应事件
        initBtnAction();
        // 初始化预览
        updatePreView();
    }

    private void initBtnAction()
    {
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View paramView)
            {
                // 有widgetId就返回取消结果，没有直接结束
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
                {
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                    setResult(RESULT_CANCELED, resultValue);
                }

                finish();
            }
        });
        Button loadButton = (Button) findViewById(R.id.load_conf);
        loadButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View paramView)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(WidgetConfigBaseActivity.this);
                builder.setTitle(R.string.load_conf);
                List<XmlEntity> savedCfgList = XmlUtil.parseWidgetCfg("data");

                final LoadConfigAdapter adapter = new LoadConfigAdapter(WidgetConfigBaseActivity.this, savedCfgList);

                if (savedCfgList.size() != 0)
                {
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            ListItem item = (ListItem) adapter.getItem(arg1);
                            initUIData(item.entity);
                            updatePreView();
                        }
                    });

                    builder.setNeutralButton(R.string.clear_conf, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if (XmlUtil.deleteFile("data"))
                            {
                                Toast.makeText(WidgetConfigBaseActivity.this, R.string.clear_conf_succ,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    builder.setMessage(R.string.no_saved_conf);
                }

                builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt)
                    {
                    }
                });

                builder.create().show();
            }
        });

        // 确定按钮
        Button saveButton = (Button) findViewById(R.id.button_apply);
        saveButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View paramView)
            {
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
                {
                    saveBtnAction();
                }

                finish();
            }
        });
    }

    /**
     * 确定按钮事件
     */
    protected void saveBtnAction()
    {
        // 把每个WidgetId对应的按钮配置存入共享参数
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor configEditor = config.edit();

        // 按钮顺序组织成字符串存入
        configEditor.putString(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetId), btnIds);

        // 使用的布局的名称,不能直接用资源id,否则在升级时有问题
        configEditor.putString(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, widgetId), listLayout.getValue());

        // 获取图标配置的颜色
        configEditor.putInt(String.format(Constants.PREFS_ICON_COLOR_FIELD_PATTERN, widgetId),
                iconColorPicker.getLastColor());
        configEditor.putInt(String.format(Constants.PREFS_ICON_TRANS_FIELD_PATTERN, widgetId),
                iconColorPicker.getLastTrans());

        // 获取指示器配置的颜色
        configEditor.putInt(String.format(Constants.PREFS_IND_COLOR_FIELD_PATTERN, widgetId),
                indColorPicker.getLastColor());

        // 获取背景配置的颜色
        if (backBitmap != null)
        {
            try
            {
                String fileName = widgetId + "_back.png";
                FileOutputStream fileOutputStream = openFileOutput(fileName, 0);
                backBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.close();
                configEditor.putString(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId), fileName);

                // 更新整个列表，目前没有更好的更新方法
                getListView().invalidateViews();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            configEditor.putInt(String.format(Constants.PREFS_BACK_COLOR_FIELD_PATTERN, widgetId),
                    backColorPicker.getLastColor());

            // 删除背景图片的配置和图片文件
            if (config.contains(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId)))
            {
                String fileName = config.getString(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId),
                        "");
                configEditor.remove(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId));

                // 删除背景图片文件
                deleteFile(fileName);
            }
        }

        // 获取分隔线的颜色
        configEditor.putInt(String.format(Constants.PREFS_DIVIDER_COLOR_FIELD_PATTERN, widgetId),
                dividerColorPicker.getLastColor());

        // 保存最后一次配置
        configEditor.putString(Constants.PREFS_LAST_BUTTONS_ORDER, btnIds);
        configEditor.putString(Constants.PREFS_LAST_BACKGROUND, listLayout.getValue());
        configEditor.putInt(Constants.PREFS_LAST_ICON_COLOR, iconColorPicker.getLastColor());
        configEditor.putInt(Constants.PREFS_LAST_ICON_TRANS, iconColorPicker.getLastTrans());
        configEditor.putInt(Constants.PREFS_LAST_IND_COLOR, indColorPicker.getLastColor());
        configEditor.putInt(Constants.PREFS_LAST_DIVIDER_COLOR, dividerColorPicker.getLastColor());
        // 不管有没有使用背景图片都保存背景颜色
        configEditor.putInt(Constants.PREFS_LAST_BACK_COLOR, backColorPicker.getLastColor());

        configEditor.commit();
    }

    /**
     * 保存配置到SD卡上，每次点击确定时触发，无论是新建还是修改
     * 
     * @param config
     * @param widgetId
     * @param isModify
     * @param oldIds
     */
    protected void saveCfgToSD(SharedPreferences config, boolean isModify, String oldIds)
    {
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

        // 如果是新建的Widget,用是否已经存在这个widget的配置来判断
        if (!isModify)
        {
            // 如果是第一次创建widget或者创建了一个不同的Widget
            if (list.size() == 0 || !hasSameConf)
            {
                list.add(entity);
            }
            // 如果创建了一个相同的widget(或者只是顺不同)，则覆盖已经存在的配置（包括按钮的顺序）
            else
            {
                theSameEntity.setBtnIds(newIds);
                theSameEntity.setIconColor(iconColorPicker.getLastColor());
                theSameEntity.setIconTrans(iconColorPicker.getLastTrans());
                theSameEntity.setIndColor(indColorPicker.getLastColor());
                theSameEntity.setLayoutName(listLayout.getValue());
                theSameEntity.setBackColor(backColorPicker.getLastColor());
                theSameEntity.setDividerColor(dividerColorPicker.getLastColor());
            }
        }
        // 如果是修改界面时，找到原来保存的配置并修改
        else
        {
            XmlEntity tmpEntity = getSameConf(list, oldIds);

            if (tmpEntity == null)
            {
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

        // 保存到"data"文件中
        saveSuccess = XmlUtil.writeWidgetXml(list, "data");

        if (!saveSuccess)
        {
            Toast.makeText(this, R.string.save_conf_error, Toast.LENGTH_SHORT).show();
        }
    }

    private XmlEntity getSameConf(List<XmlEntity> savedList, String btnIds)
    {
        if (savedList == null)
        {
            return null;
        }

        for (XmlEntity xmlEntity : savedList)
        {
            String savedIds = xmlEntity.getBtnIds();
            String[] part = savedIds.split(",");
            String[] part2 = btnIds.split(",");
            List<String> partList = Arrays.asList(part);
            List<String> part2List = Arrays.asList(part2);

            if (part.length == part2.length && partList.containsAll(part2List))
            {
                return xmlEntity;
            }
        }

        return null;
    }

    protected abstract void updateWidget(int appWidgetId);

    /**
     * 初始化配置界面的数据
     * 
     * @param appWidgetId
     */
    protected void initUI()
    {
        layoutDefault = getResources().getString(R.string.list_pre_bg_default);
        layoutWhite = getResources().getString(R.string.list_pre_bg_white);
        layoutCustom = getResources().getString(R.string.list_pre_bg_custom);
        layoutCustomShadow = getResources().getString(R.string.list_pre_bg_custom_shadow);
        layoutNoBack = getResources().getString(R.string.list_pre_bg_none);

        preView = (LinearLayout) findViewById(R.id.pre_view);
        listLayout = (ListPreference) findPreference(KEY_LAYOUT);
        listLayout.setEntries(buildBackgroundEntries());
        listLayout.setEntryValues(buildBackgroundEntries());

        // 初始化颜色选择器
        backColorPicker = (BackCustomPreference) findPreference(KEY_BACK_COLOR_PICKER);
        indColorPicker = (IndCustomPreference) findPreference(KEY_IND_COLOR_PICKER);
        iconColorPicker = (IconCustomPreference) findPreference(KEY_ICON_COLOR_PICKER);
        dividerColorPicker = (DividerCustomPreference) findPreference(KEY_DIVIDER_COLOR_PICKER);

        OnClickListener btnOnClickListener = new OnClickListener()
        {
            public void onClick(View paramView)
            {
                int btnId = (Integer) paramView.getTag();
                boolean isExist = false;
                String[] ids = btnIds.split(",");

                for (int i = 0; i < ids.length; i++)
                {
                    if (!ids[i].equals("") && btnId == Integer.parseInt(ids[i]))
                    {
                        isExist = true;
                        break;
                    }
                }

                if (isExist)
                {
                    Toast.makeText(WidgetConfigBaseActivity.this, R.string.button_already_exists, Toast.LENGTH_SHORT)
                            .show();
                }
                else
                {
                    if (ids.length == 20)
                    {
                        Toast.makeText(WidgetConfigBaseActivity.this, R.string.button_max, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if (ids.length == 1 && ids[0].equals(""))
                        {
                            btnIds += btnId;
                        }
                        else
                        {
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

        // 注册选项变化监听器
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * 获取最后一次配置的按钮顺序，如果没有的话返回一个默认顺序，子类要覆盖
     * 
     * @return
     */
    protected abstract String getLastBtnOrder();

    public void initUIData(XmlEntity entity)
    {
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
        // 初始化完成最后判断如果无背景，要删除选项
        if (layoutName.equals(layoutNoBack))
        {
            cate.removePreference(backColorPicker);
            cate.removePreference(dividerColorPicker);
        }
        else if (layoutName.equals(layoutDefault) || layoutName.equals(layoutWhite))
        {
            cate.removePreference(dividerColorPicker);
        }
    }

    private void initUIData()
    {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);

        // 获取当前widget的背景，如果获取不到就使用最后一次背景
        String layoutName = config.getString(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, widgetId), null);

        if (layoutName == null)
        {
            layoutName = config.getString(Constants.PREFS_LAST_BACKGROUND, layoutDefault);
        }

        listLayout.setValue(layoutName);
        listLayout.setSummary(layoutName);

        // 最后一次配置的按钮顺序
        btnIds = getLastBtnOrder();
        backColorPicker.updateView();
        indColorPicker.updateView();
        iconColorPicker.updateView();
        dividerColorPicker.updateView();

        PreferenceScreen cate = (PreferenceScreen) findPreference("background_category");
        // 初始化完成最后判断如果无背景，要删除选项
        if (layoutName.equals(layoutNoBack))
        {
            cate.removePreference(backColorPicker);
            cate.removePreference(dividerColorPicker);
        }
        else if (layoutName.equals(layoutDefault) || layoutName.equals(layoutWhite))
        {
            cate.removePreference(dividerColorPicker);
        }
    }

    private String[] buildBackgroundEntries()
    {
        return new String[] { getResources().getString(R.string.list_pre_bg_default),
                getResources().getString(R.string.list_pre_bg_white),
                getResources().getString(R.string.list_pre_bg_custom),
                getResources().getString(R.string.list_pre_bg_custom_shadow),
                getResources().getString(R.string.list_pre_bg_none) };
    }

    /**
     * 当变换选项时要动态更新其他控件中可选的条目
     * 
     * @param btnName
     * @return
     */
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        if (key.equals(KEY_LAYOUT))
        {
            String layoutName = preferences.getString(key, "");
            listLayout.setSummary(preferences.getString(key, ""));
            PreferenceScreen cate = (PreferenceScreen) findPreference("background_category");

            if (layoutName.equals(layoutCustom) || layoutName.equals(layoutCustomShadow))
            {
                indColorPicker.updateView();
                cate.addPreference(backColorPicker);
                backColorPicker.updateView();
                cate.addPreference(dividerColorPicker);
            }
            else if (layoutName.equals(layoutNoBack))
            {
                indColorPicker.updateView();
                cate.removePreference(backColorPicker);
                cate.removePreference(dividerColorPicker);
            }
            else
            {
                indColorPicker.updateView();
                cate.addPreference(backColorPicker);
                backColorPicker.updateView();
                cate.removePreference(dividerColorPicker);
            }
        }

        // 更新预览
        updatePreView();
    }

    public void updatePreView()
    {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
        {
            return;
        }

        switch (requestCode)
        {
            case 1:
                int size = getWidgetSize();
                int y = 0;

                // 最大大小129600像素
                switch (size)
                {
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

                if (extras != null)
                {
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

    public int getWidgetId()
    {
        return widgetId;
    }

    private void setBtnOnClickListener(final View views)
    {
        final String[] btnId = btnIds.split(",");
        for (int i = 0; i < btnId.length; i++)
        {
            int pos = -1;
            // 从第二个按钮开始偏移
            if (i != 0)
            {
                switch (btnId.length)
                {
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
            }
            else
            {
                pos = i;
            }

            final View tmp = views.findViewById(getResources().getIdentifier("btn_" + pos, "id", getPackageName()));
            tmp.setTag(btnId[i]);
            tmp.setOnTouchListener(new OnTouchListener()
            {
                public boolean onTouch(View v, final MotionEvent event)
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        v.setOnLongClickListener(new OnLongClickListener()
                        {
                            public boolean onLongClick(View v)
                            {
                                int[] location = new int[2];
                                v.getLocationOnScreen(location);
                                mDragPointX = location[0];
                                mDragPointY = location[1];
                                mXOffset = (int) event.getRawX() - mDragPointX;
                                mYOffset = (int) event.getRawY() - mDragPointY;

                                v.setDrawingCacheEnabled(true);
                                startDrag(v, Bitmap.createBitmap(v.getDrawingCache()));

                                // 长按后立即更新会取消长按震动效果
                                updatePreView();
                                return true;
                            }
                        });

                        v.setOnClickListener(new OnClickListener()
                        {
                            public void onClick(View paramView)
                            {
                                delBtn(paramView, btnId);
                            }
                        });
                    }
                    return false;
                }
            });
        }
    }

    private void delBtn(View view, String[] btnId)
    {
        String newIds = "";

        for (int i = 0; i < btnId.length; i++)
        {
            // 要用toString()方法，否则无效
            if (!view.getTag().toString().equals(btnId[i]))
            {
                if (newIds.equals(""))
                {
                    newIds += btnId[i];
                }
                else
                {
                    newIds += "," + btnId[i];
                }
            }
        }

        btnIds = newIds;
        updatePreView();
    }

    /**
     * 准备拖动，初始化拖动项的图像
     */
    private void startDrag(View v, Bitmap bm)
    {
        // 释放影像，在准备影像的时候，防止影像没释放，每次都执行一下
        if (mDragImage != null)
        {
            mWindowManager.removeView(mDragImage);
            mDragImage = null;
        }

        mWindowParams = new WindowManager.LayoutParams();
        // 从上到下计算y方向上的相对位置，
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = mDragPointX;
        mWindowParams.y = mDragPointY;

        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // 下面这些参数能够帮助准确定位到选中项点击位置，照抄即可
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        // 把影像ImagView添加到当前视图中
        mDragImage = new ImageView(this);
        mDragImage.setImageBitmap(bm);
        mDragImage.setPadding(0, 0, 0, 0);
        mDragImage.setTag(v.getTag());

        mWindowManager = (WindowManager) getSystemService("window");
        mWindowManager.addView(mDragImage, mWindowParams);

        // 震动
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(25);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        if (mDragImage != null)
        {
            int action = ev.getAction();

            switch (action)
            {
                case MotionEvent.ACTION_UP:
                    // 释放拖动影像
                    onDrop();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 拖动影像
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
     * 拖动执行，在Move方法中执行
     */
    public void onMove(final int x, final int y)
    {
        if (mDragImage != null)
        {
            // 设置一点点的透明度
            mWindowParams.alpha = ALPHA_VALUE;

            // 更新坐标位置
            int[] location = new int[2];
            preView.getLocationOnScreen(location);
            int maxHeight = location[1] + preView.getHeight() - mDragImage.getHeight();
            int tmp = y - mYOffset;

            mWindowParams.y = tmp > maxHeight ? maxHeight : tmp;
            mWindowParams.x = x - mXOffset;
            // 更新界面
            mWindowManager.updateViewLayout(mDragImage, mWindowParams);
        }

        mMoveX = x;
        mMoveY = y;

        preView.postDelayed(new Runnable()
        {
            public void run()
            {
                if (x == mMoveX && y == mMoveY)
                {
                    onStop(x, y);
                }
            }
        }, 250);
    }

    private void onStop(int x, int y)
    {
        if (mDragImage == null)
        {
            return;
        }

        String tmpIds = "";
        String[] srcBtnId = btnIds.split(",");
        String dragId = mDragImage.getTag().toString();

        // 先找到这个按钮的位置，用于判断是往前移动还是往后移动
        int pos = 0;

        // 如果有先删除
        for (int i = 0; i < srcBtnId.length; i++)
        {
            // 要用toString()方法，否则无效
            if (dragId.equals(srcBtnId[i]))
            {
                pos = i;
            }
            else
            {
                if (tmpIds.equals(""))
                {
                    tmpIds += srcBtnId[i];
                }
                else
                {
                    tmpIds += "," + srcBtnId[i];
                }
            }
        }

        final String[] btnId = tmpIds.split(",");

        // 找到要替换的按钮的id
        for (int i = 0; i < btnId.length; i++)
        {
            View childView = preView.findViewWithTag(btnId[i]);

            if (childView == null || childView.getTag() == null)
            {
                return;
            }

            String childId = childView.getTag().toString();

            // 在要替换的按钮的坐标范围内而且不是自身
            if (x < childView.getRight() && x > childView.getLeft() && !dragId.equals(childId))
            {
                String newIds = "";

                for (int j = 0; j < btnId.length; j++)
                {
                    // 如果是要替换的按钮
                    if (childId.equals(btnId[j]))
                    {
                        if (newIds.equals(""))
                        {
                            // 往前面移动
                            if (j < pos)
                            {
                                newIds += dragId + "," + btnId[j];
                            }
                            else
                            {
                                newIds += btnId[j] + "," + dragId;
                            }
                        }
                        else
                        {
                            // 往前面移动
                            if (j < pos)
                            {
                                newIds += "," + dragId + "," + btnId[j];
                            }
                            else
                            {
                                newIds += "," + btnId[j] + "," + dragId;
                            }
                        }
                    }
                    else
                    {
                        if (newIds.equals(""))
                        {
                            newIds += btnId[j];
                        }
                        else
                        {
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
     * 停止拖动
     */
    public void onDrop()
    {
        if (mDragImage != null)
        {
            mWindowManager.removeView(mDragImage);
            mDragImage = null;
        }
    }
}
