package alei.switchpro.reboot;

import alei.switchpro.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class RebootActivity extends Activity {
    public static void rebootInBootloader(Context context, String mode) {
        String rebootPath = "";
        File file = context.getFileStreamPath("reboot");

        try {
            if (!file.exists()) {
                AssetManager am = context.getAssets();
                InputStream is = am.open("reboot");
                FileOutputStream fos = context.openFileOutput("reboot", 1);
                byte[] b = new byte[6000];
                is.read(b);
                fos.write(b);
                is.close();
                fos.close();
            }

            rebootPath = file.getAbsolutePath();
            StringBuilder sb = new StringBuilder("chmod 755 ");
            sb.append(rebootPath);
            Su.executeCommand(sb.toString());
            String s1 = "su -c " + rebootPath + " " + mode;
            String s2 = "su -c \"" + rebootPath + " " + mode + "\"";
            Su.executeCommand(s1);
            Su.executeCommand(s2);
        } catch (Exception e) {
            String s1 = "su -c " + rebootPath + " " + mode;
            String s2 = "su -c \"" + rebootPath + " " + mode + "\"";
            Su.executeCommand(s1);
            Su.executeCommand(s2);
            e.printStackTrace();
        }

        try {
            Su su = new Su();
            rebootPath = file.getAbsolutePath();
            StringBuilder sb = new StringBuilder("chmod 755 ");
            sb.append(rebootPath);
            su.Run(sb.toString());

            // �������ŵ�ִ�з�ʽ
            StringBuilder sb1 = new StringBuilder(rebootPath);
            sb1.append(" ");
            sb1.append(String.valueOf(mode));
            su.Run(sb1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        adapter.add(getString(R.string.dlg_reboot));
        adapter.add("Reboot recovery");
        adapter.add("Reboot bootloader");
        adapter.add("Shutdown");
        return new AlertDialog.Builder(this).setTitle(R.string.confirm).setIcon(android.R.drawable.ic_dialog_alert)
                .setAdapter(adapter, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            switch (which) {
                                case 0:
                                    rebootInBootloader(getApplicationContext(), "");
                                    break;
                                case 1:
                                    rebootInBootloader(getApplicationContext(), "recovery");
                                    break;
                                case 2:
                                    rebootInBootloader(getApplicationContext(), "bootloader");
                                    break;
                                case 3:
                                    rebootInBootloader(getApplicationContext(), "-p");
                                    break;
                                default:
                                    break;
                            }
                            dialog.dismiss();
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        finish();
                    }
                }).show();
    }

    // һ��Ҫ��pause��ʱ�������activity
    @Override
    protected void onPause() {
        super.onPause();
        dismissDialog(0);
        finish();
    }

}