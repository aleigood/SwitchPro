package alei.switchpro.nfc;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class NFCUtil {
    public static void toggleNFC(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", "com.android.settings.Settings$WirelessSettingsActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        try {
            // ����Ǵ�֪ͨ����������Ҫ�ȵ���֪ͨ��
            PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), 0).send();
            pendingIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public static boolean getNFC(Context context) {
        try {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);

            if (nfcAdapter == null) {
                return false;
            }

            return nfcAdapter.isEnabled();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
