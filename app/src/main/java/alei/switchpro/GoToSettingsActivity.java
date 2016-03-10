package alei.switchpro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;

public class GoToSettingsActivity extends Activity
{
    private String title;
    private String content;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        showDialog(0);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        AlertDialog dlg = new AlertDialog.Builder(this).setTitle(title).setMessage(content)
                .setNeutralButton("Go To", new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent intent = new Intent(GoToSettingsActivity.this, ToggleConfigActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton(android.R.string.cancel, new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                }).create();
        dlg.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                finish();
            }
        });
        return dlg;
    }

    // 一定要在pause的时候结束本activity
    @Override
    protected void onPause()
    {
        super.onPause();
        dismissDialog(0);
        finish();
    }

}
