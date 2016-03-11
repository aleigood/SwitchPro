package alei.switchpro.timeout;

import alei.switchpro.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings;

public class TimeoutSelectorActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showDialog(0);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        String[] items = new String[]{getString(R.string.screen_timeout_15sec),
                getString(R.string.screen_timeout_30sec), getString(R.string.screen_timeout_1min),
                getString(R.string.screen_timeout_2min), getString(R.string.screen_timeout_10min),
                getString(R.string.screen_timeout_30min)};
        int indexChecked = 0;
        switch (getScreenTimeout()) {
            case 15000:
                indexChecked = 0;
                break;
            case 30000:
                indexChecked = 1;
                break;
            case 60000:
                indexChecked = 2;
                break;
            case 120000:
                indexChecked = 3;
                break;
            case 600000:
                indexChecked = 4;
                break;
            case 1800000:
                indexChecked = 5;
                break;
            default:
                indexChecked = -1;
                break;
        }
        return new AlertDialog.Builder(this).setTitle(R.string.screen_timeout)
                .setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        finish();
                    }
                }).setSingleChoiceItems(items, indexChecked, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                setScreenTimeout(15000);
                                break;
                            case 1:
                                setScreenTimeout(30000);
                                break;
                            case 2:
                                setScreenTimeout(60000);
                                break;
                            case 3:
                                setScreenTimeout(120000);
                                break;
                            case 4:
                                setScreenTimeout(600000);
                                break;
                            case 5:
                                setScreenTimeout(1800000);
                                break;
                            default:
                                break;
                        }

                        finish();
                    }
                }).show();
    }

    // 一定要在pause的时候结束本activity
    @Override
    protected void onPause() {
        super.onPause();
        dismissDialog(0);
        finish();
    }

    /**
     * 获取屏幕超时
     *
     * @param context
     */
    public int getScreenTimeout() {
        int screenTimeOut = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
        return screenTimeOut;
    }

    /**
     * 设置屏幕超时
     *
     * @param context
     */
    public void setScreenTimeout(int time) {
        Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);
    }

}
