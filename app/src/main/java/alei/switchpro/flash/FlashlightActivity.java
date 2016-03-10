package alei.switchpro.flash;

import alei.switchpro.Constants;
import alei.switchpro.R;
import alei.switchpro.Utils;
import alei.switchpro.color.ColorPickerView;
import alei.switchpro.color.ColorPickerView.OnColorChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class FlashlightActivity extends Activity
{
    private PowerManager.WakeLock mWakeLock;
    private View mWindow;
    private AlertDialog dlg;
    private int lastColor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_flashlight);

        // 设置全屏
        Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 设置背景为最后一次设置的颜色
        mWindow = findViewById(R.id.flashlight_main);
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(this);
        lastColor = pre.getInt(Constants.PREFS_FLASH_COLOR, Color.WHITE);
        mWindow.setBackgroundColor(lastColor);

        // 设置屏幕亮度
        LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = 1.0f;
        getWindow().setAttributes(attributes);

        // 设置屏幕常亮
        mWakeLock = Utils.getWakeLock(this);
        mWakeLock.acquire();

        findViewById(R.id.color_image).setOnClickListener(new OnClickListener()
        {
            public void onClick(View paramView)
            {
                dlg = initColorChooser();
                dlg.show();
            }
        });
        findViewById(R.id.flash_image).setOnClickListener(new OnClickListener()
        {
            public void onClick(View paramView)
            {
                finish();
            }
        });

        super.onCreate(savedInstanceState);
    }

    private AlertDialog initColorChooser()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.flash_color);

        OnColorChangedListener listener = new OnColorChangedListener()
        {
            public void colorChanged(int color)
            {
                // 保存设置好的颜色
                lastColor = color;

                // 保存最后一次配置的颜色
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(FlashlightActivity.this).edit();
                editor.putInt(Constants.PREFS_FLASH_COLOR, lastColor);
                editor.commit();
                mWindow.setBackgroundColor(lastColor);
                dlg.dismiss();
            }
        };

        // 主要是设置 调色板的布局
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(0, 0, 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        // 无背景时可选择半透明选择，所以半径要小
        Display display = getWindowManager().getDefaultDisplay();
        int x = ((display.getWidth() - display.getWidth() / 3) / 2);

        final ColorPickerView mCPView = new ColorPickerView(this, listener, lastColor, x, false);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        params1.gravity = Gravity.CENTER;
        mCPView.setLayoutParams(params1);
        layout.addView(mCPView);

        layout.setId(android.R.id.widget_frame);
        ScrollView hsv = new ScrollView(this);
        hsv.addView(layout);
        builder.setView(hsv);

        // 初始化按钮事件
        builder.setPositiveButton(getResources().getString(R.string.button_apply),
                new android.content.DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt)
                    {
                        // 保存设置好的颜色
                        lastColor = mCPView.getColor();

                        // 保存最后一次配置的颜色
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                                FlashlightActivity.this).edit();
                        editor.putInt(Constants.PREFS_FLASH_COLOR, lastColor);
                        editor.commit();
                        mWindow.setBackgroundColor(lastColor);
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.button_cancel),
                new android.content.DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt)
                    {
                    }
                });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    // 一定要在pause的时候结束本activity
    @Override
    protected void onPause()
    {
        super.onPause();
        mWakeLock.release();
        finish();
    }
}
