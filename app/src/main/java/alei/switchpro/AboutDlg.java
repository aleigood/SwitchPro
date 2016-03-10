package alei.switchpro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.Preference;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutDlg extends Preference
{
    private Activity parent;
    private AlertDialog dialog;
    private String s = "<html><body><b>In home, press \"Menu\" button and select \"Add\"-&gt;\"Widgets\"-&gt;\"SwitchPro\" to create widget. </b><br><br>"
            + "<b>1.</b> Do not install on SD card.<br><br>"
            + "<b>2.</b> Please select the \"Data enabled\" (Settings-> Wireless & networks-> Mobile networks-> Data enabled) and to ensure that it connected to the Internet before using Data connection. If the data connection is not available, please try \"Use APN toggle\" in settings panel.<br><br>"
            + "<b>3.</b> As the difference between the phone, some features on your device may not work, if it happens please <a href=\"mailto:aleigood@gmail.com\">contact me</a>, I will try to help you solve the problem. <br><br>"
            + "<b>4.</b> Do not delete app from running list, otherwise it cannot be updated. <br><br>"
            + "<b>5.</b> Please deactivate device administrator before uninstalling (Settings-&gt;Security-&gt;Device administration), otherwise it can not be uninstalled. "
            + "<br><br>Copyright &copy; 2011 Leo</body></html>";

    public AboutDlg(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public AboutDlg(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        parent = (Activity) context;
        try
        {
            setSummary(context.getText(R.string.app_name) + " "
                    + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        }
        catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void onClick()
    {
        // 获取对话框Builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(R.string.about);

        // 设置对话框的View
        LayoutInflater inflater = parent.getLayoutInflater();
        View dlgView = inflater.inflate(R.layout.activity_about, null, false);
        TextView aboutTxt = ((TextView) dlgView.findViewById(R.id.about_txt));
        aboutTxt.setText(Html.fromHtml(s));
        aboutTxt.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setView(dlgView);

        // 显示按钮
        builder.setNeutralButton(R.string.button_apply, new OnClickListener()
        {
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                if (dialog != null)
                {
                    dialog.cancel();
                }
            }
        });

        // 创建对话框并显示
        dialog = builder.create();
        dialog.show();
    }

    public void setActivity(Activity parent)
    {
        this.parent = parent;
    }

}
