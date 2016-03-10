package alei.switchpro.process;

import java.util.ArrayList;

import alei.switchpro.DatabaseOper;
import alei.switchpro.MyApplication;
import alei.switchpro.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ProcessMainActivity extends Activity
{
    protected static final String ACTION_LOADFINISH = "com.xmobileapp.taskmanager.ACTION_LOADFINISH";
    public static final boolean DEBUG = true;
    public static final String TAG = "TaskManager";
    private ProcessAdapter mProcessAdapter;
    private BroadcastReceiver loadFinish = new LoadFinishReceiver();
    private ArrayList<ProcessData> dataList;
    DatabaseOper dbOper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_process);
        dbOper = MyApplication.getInstance().getDataOper();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(loadFinish, new IntentFilter(ACTION_LOADFINISH));
        refreshList();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(loadFinish);
    }

    /**
     * 添加弹出菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate our menu.
        getMenuInflater().inflate(R.menu.process_refresh_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 弹出菜单的响应事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.menu_refresh)
        {
            refreshList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshList()
    {
        setProgressBarIndeterminateVisibility(true);

        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                dataList = ProcessUtils.getProcessData(ProcessMainActivity.this);
                sendBroadcast(new Intent(ACTION_LOADFINISH));
            }

        });
        t.start();
    }

    private class LoadFinishReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(final Context ctx, Intent intent)
        {
            setProgressBarIndeterminateVisibility(false);
            mProcessAdapter = new ProcessAdapter(ProcessMainActivity.this, dataList);
            ListView listView = (ListView) findViewById(R.id.listbody);
            listView.setOnItemClickListener(new OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    ProcessData processData = (ProcessData) mProcessAdapter.getItem(position);

                    Intent intent = new Intent();

                    if (VERSION.SDK_INT >= 9)
                    {
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        Uri uri = Uri.fromParts("package", processData.packagename, null);
                        intent.setData(uri);
                    }
                    else
                    {
                        final String appPkgName = (VERSION.SDK_INT == 8 ? "pkg"
                                : "com.android.settings.ApplicationPkgName");
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                        intent.putExtra(appPkgName, processData.packagename);
                    }
                    ctx.startActivity(intent);
                }
            });

            listView.setAdapter(mProcessAdapter);
        }
    }
}
