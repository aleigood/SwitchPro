package alei.switchpro.modify;

import java.util.List;

import alei.switchpro.R;
import alei.switchpro.WidgetProviderX1;
import alei.switchpro.WidgetProviderX2;
import alei.switchpro.WidgetProviderX3;
import alei.switchpro.WidgetProviderX4;
import alei.switchpro.WidgetProviderX5;
import alei.switchpro.modify.MenuModifyAdapter.ListItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

public class MenuModifyPref extends Preference
{
    private Activity parent;
    private AlertDialog dialog;
    private MenuModifyAdapter adapter;

    public MenuModifyPref(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MenuModifyPref(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void onClick()
    {
        final String pkgName = WidgetProviderX4.class.getPackage().getName();

        // 获取当前已经创建的部件
        List<AppWidgetProviderInfo> itemList = AppWidgetManager.getInstance(parent).getInstalledProviders();

        final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(R.string.custom_menu);
        adapter = new MenuModifyAdapter(parent, itemList);

        builder.setAdapter(adapter, new OnClickListener()
        {
            public void onClick(DialogInterface arg0, int arg1)
            {
            }
        });

        builder.setNeutralButton(parent.getResources().getString(R.string.button_apply), new OnClickListener()
        {
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                if (dialog != null)
                {
                    List<ListItem> list = adapter.getItems();

                    for (int i = 0; i < list.size(); i++)
                    {
                        ListItem item = (ListItem) list.get(i);

                        if (item.size == 4)
                        {
                            parent.getPackageManager().setComponentEnabledSetting(
                                    new ComponentName(pkgName, WidgetProviderX4.class.getName()),
                                    item.isSelected ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);
                        }
                        else if (item.size == 1)
                        {
                            parent.getPackageManager().setComponentEnabledSetting(
                                    new ComponentName(pkgName, WidgetProviderX1.class.getName()),
                                    item.isSelected ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);
                        }
                        else if (item.size == 2)
                        {
                            parent.getPackageManager().setComponentEnabledSetting(
                                    new ComponentName(pkgName, WidgetProviderX2.class.getName()),
                                    item.isSelected ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);
                        }
                        else if (item.size == 3)
                        {
                            parent.getPackageManager().setComponentEnabledSetting(
                                    new ComponentName(pkgName, WidgetProviderX3.class.getName()),
                                    item.isSelected ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);
                        }
                        else if (item.size == 5)
                        {
                            parent.getPackageManager().setComponentEnabledSetting(
                                    new ComponentName(pkgName, WidgetProviderX5.class.getName()),
                                    item.isSelected ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);
                        }
                    }

                    Toast.makeText(parent, R.string.need_reboot, Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(parent.getResources().getString(R.string.button_cancel), new OnClickListener()
        {
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                if (dialog != null)
                {
                    dialog.dismiss();
                }
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void setActivity(Activity parent)
    {
        this.parent = parent;
    }

    public AlertDialog getAlertDlg()
    {
        return dialog;
    }

}
