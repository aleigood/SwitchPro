package alei.switchpro.modify;

import alei.switchpro.R;
import alei.switchpro.WidgetProviderUtil;
import alei.switchpro.modify.ConfigModifyAdapter.ListItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.SparseIntArray;

public class ConfigModifyPref extends Preference
{
    private Activity parent;
    private AlertDialog dialog;

    public ConfigModifyPref(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ConfigModifyPref(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void onClick()
    {
        final SparseIntArray map = WidgetProviderUtil.getAllWidget(parent, true);
        int count = map.size();

        // 获取部件个数，如果只有一个就直接修改
        if (count == 1)
        {
            int widgetId = map.keyAt(0);
            startModifyActivity(widgetId, map.get(widgetId));
        }
        else
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
            builder.setTitle(R.string.select_widget);
            final ConfigModifyAdapter adapter = new ConfigModifyAdapter(parent, map);

            if (count != 0)
            {
                builder.setAdapter(adapter, new OnClickListener()
                {
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        ListItem item = (ListItem) adapter.getItem(arg1);
                        startModifyActivity(item.widgetId, map.get(item.widgetId));
                    }
                });
            }
            else
            {
                builder.setMessage(R.string.no_widget_to_modify);
            }

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
    }

    public void setActivity(Activity parent)
    {
        this.parent = parent;
    }

    private void startModifyActivity(int widgetId, int size)
    {
        Intent intent = new Intent();
        intent.setClass(parent, ConfigModifyActivity.class);
        intent.putExtra("widgetId", widgetId);
        intent.putExtra("size", size);
        parent.startActivity(intent);
    }

    public AlertDialog getAlertDlg()
    {
        return dialog;
    }

}
