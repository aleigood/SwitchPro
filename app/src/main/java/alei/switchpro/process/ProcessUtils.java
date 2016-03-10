package alei.switchpro.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import alei.switchpro.Constants;
import alei.switchpro.DatabaseOper;
import alei.switchpro.MyApplication;
import alei.switchpro.R;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Build.VERSION;
import android.text.format.Formatter;
import android.widget.Toast;

public class ProcessUtils
{
    static final long PAGE_SIZE = 4 * 1024;

    public static ArrayList<ProcessData> getProcessData(Context context)
    {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService("activity");
        List<RunningAppProcessInfo> localList = mActivityManager.getRunningAppProcesses();
        ArrayList<ProcessData> retList = new ArrayList<ProcessData>();

        if (localList == null)
        {
            return retList;
        }

        Iterator<RunningAppProcessInfo> iterator = localList.iterator();

        while (iterator.hasNext())
        {
            RunningAppProcessInfo runningAppProcess = (RunningAppProcessInfo) iterator.next();

            if ((runningAppProcess.processName.equals("android")) || (runningAppProcess.processName.equals("system")))
            {
                continue;
            }

            addProcessData(context, retList, runningAppProcess);
        }

        return retList;
    }

    private static void addProcessData(Context context, ArrayList<ProcessData> retList,
            RunningAppProcessInfo runningAppProcess)
    {
        PackageManager mPackageManager = context.getPackageManager();

        try
        {
            String str1 = runningAppProcess.processName;
            int i = runningAppProcess.processName.indexOf(":");

            if (i != -1)
            {
                str1 = runningAppProcess.processName.substring(0, i);
            }

            ProcessData processData = new ProcessData();
            processData.packagename = str1;
            ApplicationInfo appInfo = mPackageManager.getApplicationInfo(processData.packagename, 0);
            processData.icon = mPackageManager.getApplicationIcon(appInfo);
            processData.name = mPackageManager.getApplicationLabel(appInfo).toString().replace("com.android.", "");

            if (processData.name == null || processData.name.equals(""))
            {
                processData.name = appInfo.packageName.replace("com.android.", "");
            }
            processData.pid = runningAppProcess.pid;
            processData.importance = runningAppProcess.importance;
            processData.importanceText = getImportance(processData.importance);
            processData.memory = Formatter.formatFileSize(context, getMemoryForPid(runningAppProcess.pid));
            retList.add(processData);
        }
        catch (NameNotFoundException e)
        {
        }
    }

    private static String getImportance(int paramInt)
    {
        switch (paramInt)
        {
            case 100:
                return "Foreground";
            case 200:
                return "Visible";
            case 300:
                return "Service";
            case 400:
                return "Background";
            case 500:
                return "Empty";
            default:
                return "Empty";
        }
    }

    public static int getMemoryForPid(int paramInt)
    {
        try
        {
            FileReader localFileReader = new FileReader("/proc/" + paramInt + "/statm");
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            String str2 = localBufferedReader.readLine();
            int i = 0;

            if (str2 != null)
            {
                String[] arrayOfString = str2.split("\\s+");

                if (arrayOfString.length >= 6)
                {
                    i = Integer.valueOf(arrayOfString[5]).intValue();
                }
            }

            localBufferedReader.close();
            localFileReader.close();
            return i *= 1024;
        }
        catch (Exception Exception)
        {
            return 0;
        }
    }

    public static void killProcess(final Context context)
    {
        DatabaseOper dbOper = MyApplication.getInstance().getDataOper();
        Cursor cursor = dbOper.queryIgnored();
        List<String> list = new ArrayList<String>();

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    list.add(cursor.getString(Constants.IGNORED.INDEX_NAME));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }

        List<ProcessData> dataList = getProcessData(context);

        int count = 0;
        String self = context.getPackageName();

        for (int i = 0; i < dataList.size(); i++)
        {
            ProcessData localProcessData = dataList.get(i);

            if (localProcessData.packagename.equals(self) || list.contains(localProcessData.name))
            {
                continue;
            }

            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");

            if (VERSION.SDK_INT >= 8)
            {
                try
                {
                    ActivityManager.class.getMethod("killBackgroundProcesses", new Class[] { String.class }).invoke(
                            activityManager, new Object[] { localProcessData.packagename });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                activityManager.restartPackage(localProcessData.packagename);
            }
            count++;
        }

        if (count == 1 || count == 0)
        {
            Toast.makeText(
                    context,
                    context.getString(R.string.task_killed_tip_part1) + " " + count + " "
                            + context.getString(R.string.task_killed_tip_part2) + " " + refreshMemory(context),
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(
                    context,
                    context.getString(R.string.task_killed_tip_part1) + " " + count + " "
                            + context.getString(R.string.task_killed_tip_part3) + " " + refreshMemory(context),
                    Toast.LENGTH_LONG).show();
        }
    }

    private static String refreshMemory(Context context)
    {
        ActivityManager localActivityManager = (ActivityManager) context.getSystemService("activity");
        ActivityManager.MemoryInfo localMemoryInfo = new ActivityManager.MemoryInfo();
        localActivityManager.getMemoryInfo(localMemoryInfo);
        return Formatter.formatFileSize(context, localMemoryInfo.availMem);
    }
}
