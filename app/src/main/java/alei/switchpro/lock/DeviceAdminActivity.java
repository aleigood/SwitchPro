package alei.switchpro.lock;

import alei.switchpro.MainBrocastReceiver;
import alei.switchpro.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class DeviceAdminActivity extends Activity {
    static final int RESULT_ENABLE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComponentName mDeviceAdminSample = new ComponentName(this, MainBrocastReceiver.class);

        Intent intent = new Intent("android.app.action.ADD_DEVICE_ADMIN");
        intent.putExtra("android.app.extra.DEVICE_ADMIN", mDeviceAdminSample);
        intent.putExtra("android.app.extra.ADD_EXPLANATION", getResources().getString(R.string.activate_admin_Warning));
        startActivityForResult(intent, RESULT_ENABLE);
        finish();
    }
}
