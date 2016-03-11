package alei.switchpro.icon;

import alei.switchpro.Constants;
import alei.switchpro.R;
import alei.switchpro.WidgetProviderUtil;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.FileOutputStream;

public class IconPrefActivity extends PreferenceActivity {
    private IconPreferenceScreen wifi;
    private IconPreferenceScreen edge;
    private IconPreferenceScreen bluetooth;
    private IconPreferenceScreen gps;
    private IconPreferenceScreen sync;
    private IconPreferenceScreen gravity;
    private IconPreferenceScreen brightness;
    private IconPreferenceScreen autoBrightness;
    private IconPreferenceScreen screenTimeout;
    private IconPreferenceScreen airplane;
    private IconPreferenceScreen scanmedia;
    private IconPreferenceScreen vibrate;
    private IconPreferenceScreen silent;
    private IconPreferenceScreen netSwitch;
    private IconPreferenceScreen battery;
    private IconPreferenceScreen unlock;
    private IconPreferenceScreen reboot;
    private IconPreferenceScreen flashlight;
    private IconPreferenceScreen wimax;
    private IconPreferenceScreen speaker;
    private IconPreferenceScreen autolock;
    private IconPreferenceScreen wifiap;
    private IconPreferenceScreen mount;
    private IconPreferenceScreen usbte;
    private IconPreferenceScreen lockScreen;
    private IconPreferenceScreen wifiSleep;
    private IconPreferenceScreen volume;
    private IconPreferenceScreen killProcess;
    private IconPreferenceScreen bluetoothTe;
    private IconPreferenceScreen nfc;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.pref_icon_conf);

        wifi = (IconPreferenceScreen) findPreference("custom_ico_wifi");
        edge = (IconPreferenceScreen) findPreference("custom_ico_edge");
        bluetooth = (IconPreferenceScreen) findPreference("custom_ico_bluetooth");
        gps = (IconPreferenceScreen) findPreference("custom_ico_gps");
        sync = (IconPreferenceScreen) findPreference("custom_ico_sync");
        gravity = (IconPreferenceScreen) findPreference("custom_ico_gravity");
        brightness = (IconPreferenceScreen) findPreference("custom_ico_brightness");
        autoBrightness = (IconPreferenceScreen) findPreference("custom_ico_autobrightness");
        screenTimeout = (IconPreferenceScreen) findPreference("custom_ico_screenTimeout");
        airplane = (IconPreferenceScreen) findPreference("custom_ico_airplane");
        scanmedia = (IconPreferenceScreen) findPreference("custom_ico_scanmedia");
        vibrate = (IconPreferenceScreen) findPreference("custom_ico_vibrate");
        silent = (IconPreferenceScreen) findPreference("custom_ico_silent");
        netSwitch = (IconPreferenceScreen) findPreference("custom_ico_netSwitch");
        battery = (IconPreferenceScreen) findPreference("custom_ico_battery");
        unlock = (IconPreferenceScreen) findPreference("custom_ico_unlock");
        reboot = (IconPreferenceScreen) findPreference("custom_ico_reboot");
        flashlight = (IconPreferenceScreen) findPreference("custom_ico_flashlight");
        wimax = (IconPreferenceScreen) findPreference("custom_ico_wimax");
        speaker = (IconPreferenceScreen) findPreference("custom_ico_speaker");
        autolock = (IconPreferenceScreen) findPreference("custom_ico_autolock");
        wifiap = (IconPreferenceScreen) findPreference("custom_ico_wifiap");
        mount = (IconPreferenceScreen) findPreference("custom_ico_mount");
        usbte = (IconPreferenceScreen) findPreference("custom_ico_usbte");
        lockScreen = (IconPreferenceScreen) findPreference("custom_ico_lockScreen");
        wifiSleep = (IconPreferenceScreen) findPreference("custom_ico_wifiSleep");
        volume = (IconPreferenceScreen) findPreference("custom_ico_volume");
        killProcess = (IconPreferenceScreen) findPreference("custom_ico_killProcess");
        bluetoothTe = (IconPreferenceScreen) findPreference("custom_ico_bluetooth_te");
        nfc = (IconPreferenceScreen) findPreference("custom_ico_nfc");
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == airplane) {
            pickIcon(Constants.ICON_AIRPLANE);
        } else if (preference == autolock) {
            pickIcon(Constants.ICON_AUTOLOCK);
        } else if (preference == battery) {
            pickIcon(Constants.ICON_BATTERY);
        } else if (preference == bluetooth) {
            pickIcon(Constants.ICON_BLUETOOTH);
        } else if (preference == brightness) {
            pickIcon(Constants.ICON_BRIGHTNESS);
        } else if (preference == autoBrightness) {
            pickIcon(Constants.ICON_AUTO_BRIGHTNESS);
        } else if (preference == edge) {
            pickIcon(Constants.ICON_EDGE);
        } else if (preference == flashlight) {
            pickIcon(Constants.ICON_FLASHLIGHT);
        } else if (preference == gps) {
            pickIcon(Constants.ICON_GPS);
        } else if (preference == gravity) {
            pickIcon(Constants.ICON_GRAVITY);
        } else if (preference == lockScreen) {
            pickIcon(Constants.ICON_LOCK_SCREEN);
        } else if (preference == mount) {
            pickIcon(Constants.ICON_MOUNT);
        } else if (preference == netSwitch) {
            pickIcon(Constants.ICON_NET_SWITCH);
        } else if (preference == reboot) {
            pickIcon(Constants.ICON_REBOOT);
        } else if (preference == scanmedia) {
            pickIcon(Constants.ICON_SCANMEDIA);
        } else if (preference == screenTimeout) {
            pickIcon(Constants.ICON_SCREEN_TIMEOUT);
        } else if (preference == speaker) {
            pickIcon(Constants.ICON_SPEAKER);
        } else if (preference == sync) {
            pickIcon(Constants.ICON_SYNC);
        } else if (preference == unlock) {
            pickIcon(Constants.ICON_UNLOCK);
        } else if (preference == usbte) {
            pickIcon(Constants.ICON_USBTE);
        } else if (preference == vibrate) {
            pickIcon(Constants.ICON_VIBRATE);
        } else if (preference == silent) {
            pickIcon(Constants.ICON_SILENT);
        } else if (preference == wifi) {
            pickIcon(Constants.ICON_WIFI);
        } else if (preference == wifiSleep) {
            pickIcon(Constants.ICON_WIFI_SLEEP);
        } else if (preference == wifiap) {
            pickIcon(Constants.ICON_WIFIAP);
        } else if (preference == wimax) {
            pickIcon(Constants.ICON_WIMAX);
        } else if (preference == volume) {
            pickIcon(Constants.ICON_VOLUME);
        } else if (preference == killProcess) {
            pickIcon(Constants.ICON_KILL_PROCESS);
        } else if (preference == bluetoothTe) {
            pickIcon(Constants.ICON_BT_TE);
        } else if (preference == nfc) {
            pickIcon(Constants.ICON_NFC);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void pickIcon(int reqCode) {
        Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Intent localIntent = new Intent("android.intent.action.PICK", localUri);
        this.startActivityForResult(localIntent, reqCode);
    }

    // 需要考虑到如果没有选择的图片的应用如何返回，和没选择图片如何返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        try {
            Uri localUri = data.getData();
            ContentResolver localContentResolver = getContentResolver();
            Cursor localCursor = localContentResolver.query(localUri, new String[]{"_data"}, null, null, null);
            localCursor.moveToFirst();
            String str1 = localCursor.getString(0);
            localCursor.close();

            Bitmap localBitmap1 = BitmapFactory.decodeFile(str1);
            Bitmap arrayOfBitmap = makeDockIcon(localBitmap1);
            saveDockIcon(arrayOfBitmap, requestCode);
            arrayOfBitmap.recycle();
            WidgetProviderUtil.freeMemory(requestCode);

            switch (requestCode) {
                case Constants.ICON_AIRPLANE:
                    airplane.update();
                    break;
                case Constants.ICON_AUTOLOCK:
                    autolock.update();
                    break;
                case Constants.ICON_BATTERY:
                    battery.update();
                    break;
                case Constants.ICON_BLUETOOTH:
                    bluetooth.update();
                    break;
                case Constants.ICON_BRIGHTNESS:
                    brightness.update();
                    break;
                case Constants.ICON_AUTO_BRIGHTNESS:
                    autoBrightness.update();
                    break;
                case Constants.ICON_EDGE:
                    edge.update();
                    break;
                case Constants.ICON_FLASHLIGHT:
                    flashlight.update();
                    break;
                case Constants.ICON_GPS:
                    gps.update();
                    break;
                case Constants.ICON_GRAVITY:
                    gravity.update();
                    break;
                case Constants.ICON_LOCK_SCREEN:
                    lockScreen.update();
                    break;
                case Constants.ICON_MOUNT:
                    mount.update();
                    break;
                case Constants.ICON_NET_SWITCH:
                    netSwitch.update();
                    break;
                case Constants.ICON_REBOOT:
                    reboot.update();
                    break;
                case Constants.ICON_SCANMEDIA:
                    scanmedia.update();
                    break;
                case Constants.ICON_SCREEN_TIMEOUT:
                    screenTimeout.update();
                    break;
                case Constants.ICON_SPEAKER:
                    speaker.update();
                    break;
                case Constants.ICON_SYNC:
                    sync.update();
                    break;
                case Constants.ICON_UNLOCK:
                    unlock.update();
                    break;
                case Constants.ICON_USBTE:
                    usbte.update();
                    break;
                case Constants.ICON_VIBRATE:
                    vibrate.update();
                    break;
                case Constants.ICON_SILENT:
                    silent.update();
                    break;
                case Constants.ICON_WIFI:
                    wifi.update();
                    break;
                case Constants.ICON_WIFI_SLEEP:
                    wifiSleep.update();
                    break;
                case Constants.ICON_WIFIAP:
                    wifiap.update();
                    break;
                case Constants.ICON_WIMAX:
                    wimax.update();
                    break;
                case Constants.ICON_VOLUME:
                    volume.update();
                    break;
                case Constants.ICON_KILL_PROCESS:
                    killProcess.update();
                    break;
                case Constants.ICON_BT_TE:
                    bluetoothTe.update();
                    break;
                case Constants.ICON_NFC:
                    nfc.update();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.select_img_error, Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap makeDockIcon(Bitmap paramBitmap) {
        int scaledSize = (int) Math.abs(32.0F * getResources().getDisplayMetrics().density);
        int width = paramBitmap.getWidth();
        int height = paramBitmap.getHeight();
        int newWidth = width;
        int newHeight = height;

        if ((width > scaledSize) || (height > scaledSize)) {
            if (width > height) {
                // 使图像不会拉伸
                newWidth = scaledSize;
                newHeight = scaledSize * height / width;
            } else {
                newHeight = scaledSize;
                newWidth = scaledSize * width / height;
            }
        }

        int left = (scaledSize - newWidth) / 2;
        int right = (scaledSize - newHeight) / 2;
        Rect srcRect = new android.graphics.Rect(0, 0, width, height);
        // 如果图像本身小于48，使图像居中
        Rect dstRect = new android.graphics.Rect(left, right, left + newWidth, right + newHeight);

        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);

        Bitmap localBitmap1 = Bitmap.createBitmap(scaledSize, scaledSize, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(localBitmap1);
        canvas.drawBitmap(paramBitmap, srcRect, dstRect, paint);

        return localBitmap1;
    }

    private void saveDockIcon(Bitmap paramArrayOfBitmap, int btnId) {
        try {
            FileOutputStream fileOutputStream = openFileOutput(btnId + "_icon.png", 0);
            paramArrayOfBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
            config.edit().putString(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, btnId), btnId + "_icon.png")
                    .commit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
