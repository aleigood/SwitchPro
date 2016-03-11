package alei.switchpro;

import alei.switchpro.load.XmlUtil;
import alei.switchpro.modify.ConfigModifyPref;
import alei.switchpro.modify.MenuModifyPref;
import alei.switchpro.task.TaskUtil;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class MainConfigActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private AboutDlg aboutDlg;
    private ConfigModifyPref modifyWidgetPre;
    private MenuModifyPref modifyMenuPre;
    private Preference saveCfg;
    private Preference loadCfg;
    private Preference clearNotification;
    private Preference addNotification;
    private ListPreference iconTheme;
    private DatabaseOper dbOper;
    private ListPreference notificationIconColor;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.pref_main);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        dbOper = MyApplication.getInstance().getDataOper();
        initUI();
    }

    /**
     * 因为在onCreate后会调用onResume所以直接在onResume中初始化
     */
    private void initUI() {
        aboutDlg = (AboutDlg) findPreference("about");
        modifyWidgetPre = (ConfigModifyPref) findPreference("widget_modify");
        modifyMenuPre = (MenuModifyPref) findPreference("menu_modify");
        saveCfg = (Preference) findPreference("save_cfg");
        loadCfg = (Preference) findPreference("load_cfg");
        clearNotification = (Preference) findPreference("clear_notification");
        addNotification = (Preference) findPreference("add_notification");
        iconTheme = (ListPreference) findPreference(Constants.PREFS_ICON_THEME);
        iconTheme.setSummary(iconTheme.getValue().equals("1") ? "Default" : "Holo");
        notificationIconColor = (ListPreference) findPreference(Constants.PREFS_NOTIFY_ICON_COLOR);
        notificationIconColor.setSummary(notificationIconColor.getValue().equals("1") ? "White" : "Holo style");

        aboutDlg.setActivity(this);
        modifyWidgetPre.setActivity(this);
        modifyMenuPre.setActivity(this);
    }

    @Override
    protected void onPause() {
        if (modifyWidgetPre.getAlertDlg() != null) {
            modifyWidgetPre.getAlertDlg().dismiss();
        }

        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == saveCfg) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.save_cfg))
                    .setMessage(getString(R.string.continue_confirm))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            boolean b1 = TaskUtil.saveTaskConf(dbOper);
                            boolean b2 = XmlUtil.writeGlobalXml(MainConfigActivity.this, "global");
                            boolean b3 = XmlUtil.writeProcessExcludeToXml(dbOper);

                            if (b1 && b2 && b3) {
                                Toast.makeText(MainConfigActivity.this, R.string.backup_succ, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainConfigActivity.this, R.string.backup_error, Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
        } else if (preference == loadCfg) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.load_cfg))
                    .setMessage(getString(R.string.continue_confirm))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            boolean b1 = TaskUtil.loadTask(dbOper);
                            boolean b2 = XmlUtil.parseGlobalCfg(MainConfigActivity.this, "global");
                            boolean b3 = XmlUtil.parseProcessExcludeCfg(dbOper);

                            if (b1 || b2 || b3) {
                                Toast.makeText(MainConfigActivity.this, R.string.restore_succ, Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(MainConfigActivity.this, R.string.restore_error, Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
        } else if (preference == addNotification) {
            if (VERSION.SDK_INT < 11) {
                (new AlertDialog.Builder(this)).setTitle(R.string.notify_add_err_title)
                        .setMessage(R.string.notify_add_err).setNegativeButton(android.R.string.ok, null).create()
                        .show();
            } else {
                Intent intent = new Intent(this, WidgetConfigActivityNotify.class);
                startActivity(intent);
            }
        } else if (preference == clearNotification) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.notification_remove))
                    .setMessage(getString(R.string.continue_confirm))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) {
                            SharedPreferences sp = PreferenceManager
                                    .getDefaultSharedPreferences(MainConfigActivity.this);
                            // 删除相应参数
                            String[] notificationWidgetIds = sp.getString(Constants.PREFS_IN_NOTIFICATION_BAR, "")
                                    .split(",");

                            for (int i = 0; i < notificationWidgetIds.length; i++) {
                                if (!notificationWidgetIds[i].equals("")) {
                                    int widgetId = Integer.parseInt(notificationWidgetIds[i]);
                                    String fileName = sp.getString(
                                            String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId), "");
                                    // 删除背景图片文件
                                    deleteFile(fileName);

                                    sp.edit()
                                            .remove(String.format(Constants.PREFS_BUTTONS_FIELD_PATTERN, widgetId))
                                            .remove(String.format(Constants.PREFS_LAYOUT_FIELD_PATTERN, widgetId))
                                            .remove(String.format(Constants.PREFS_BACK_COLOR_FIELD_PATTERN, widgetId))
                                            .remove(String.format(Constants.PREFS_IND_COLOR_FIELD_PATTERN, widgetId))
                                            .remove(String.format(Constants.PREFS_ICON_COLOR_FIELD_PATTERN, widgetId))
                                            .remove(String.format(Constants.PREFS_ICON_TRANS_FIELD_PATTERN, widgetId))
                                            .remove(String
                                                    .format(Constants.PREFS_DIVIDER_COLOR_FIELD_PATTERN, widgetId))
                                            .remove(String.format(Constants.PREFS_BACK_IMAGE_FIELD_PATTERN, widgetId))
                                            .commit();
                                }
                            }

                            sp.edit().remove(Constants.PREFS_IN_NOTIFICATION_BAR)
                                    .remove(Constants.PREFS_LASE_NOTIFY_WIDGET).commit();
                            NotificationManager notificationManager = (NotificationManager) MainConfigActivity.this
                                    .getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();

        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.PREFS_SHOW_NOTIFY_ICON) || key.equals(Constants.PREFS_NOTIFY_PRIORITY)) {
            Utils.updateAllNotification(this);
        } else if (key.equals(Constants.PREFS_ICON_THEME)) {
            String value = sharedPreferences.getString(key, "1");

            if (value.equals("2")) {
                if (!Utils.isAppExist(this, Constants.PKG_THEME_HOLO)) {
                    (new AlertDialog.Builder(MainConfigActivity.this))
                            .setTitle(R.string.ico_theme)
                            .setNeutralButton(R.string.download, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();

                                    if (Utils.isAppExist(MainConfigActivity.this, "com.android.vending")) {
                                        intent.setData(Uri.parse("market://details?id=" + Constants.PKG_THEME_HOLO));
                                    } else {
                                        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="
                                                + Constants.PKG_THEME_HOLO));
                                    }
                                    startActivity(intent);
                                }
                            }).setNegativeButton(android.R.string.cancel, null).setMessage(R.string.download_app)
                            .show();
                    iconTheme.setValue("1");
                    return;
                }

                iconTheme.setSummary("Holo");
            } else {
                iconTheme.setSummary("Default");
            }

            Utils.updateWidget(this);
        } else if (key.equals(Constants.PREFS_NOTIFY_ICON_COLOR)) {
            String value = sharedPreferences.getString(key, "1");

            if (value.equals("1")) {
                notificationIconColor.setSummary("White");
            } else {
                notificationIconColor.setSummary("Holo style");
            }

            Utils.updateWidget(this);
        }

    }
}
