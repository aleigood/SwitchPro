package alei.switchpro.reboot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class Su
{
    /*
     * This class handles 'su' functionality. Ensures that the command can be
     * run, then handles run/pipe/return flow.
     */
    private boolean can_su;
    private String su_bin_file;

    public Su()
    {
        this.can_su = true;
        this.su_bin_file = "/system/xbin/su";
        if (this.Run("echo"))
            return;
        this.su_bin_file = "/system/bin/su";
        if (this.Run("echo"))
            return;
        this.su_bin_file = "/data/bin/su";
        if (this.Run("echo"))
            return;
        this.su_bin_file = "";
        this.can_su = false;
    }

    public boolean Run(String command)
    {
        DataOutputStream os = null;

        try
        {
            Process process = Runtime.getRuntime().exec(su_bin_file);
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isCan_su()
    {
        return can_su;
    }

    public void setCan_su(boolean canSu)
    {
        can_su = canSu;
    }

    public String getSu_bin_file()
    {
        return su_bin_file;
    }

    public void setSu_bin_file(String suBinFile)
    {
        su_bin_file = suBinFile;
    }

    public static void executeCommand(String str1)
    {
        try
        {
            Class<?> execClass = Class.forName("android.os.Exec");
            Method createSubprocess = execClass.getMethod("createSubprocess", String.class, String.class, String.class,
                    int[].class);
            createSubprocess.invoke(null, "/system/bin/sh", "-c", str1, new int[1]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        exec("/system/bin/sh", "-c", str1);
    }

    public static String exec(String arg0, String arg1, String arg2)
    {
        try
        {
            // android.os.Exec is not included in android.jar so we need to use
            // reflection.
            Class<?> execClass = Class.forName("android.os.Exec");
            Method createSubprocess = execClass.getMethod("createSubprocess", String.class, String.class, String.class,
                    int[].class);
            Method waitFor = execClass.getMethod("waitFor", int.class);

            // Executes the command.
            // NOTE: createSubprocess() is asynchronous.
            int[] pid = new int[1];
            FileDescriptor fd = (FileDescriptor) createSubprocess.invoke(null, arg0, arg1, arg2, pid);

            // Reads stdout.
            // NOTE: You can write to stdin of the command using new
            // FileOutputStream(fd).
            FileInputStream in = new FileInputStream(fd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String output = "";
            try
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    output += line + "\n";
                }
                reader.close();
            }
            catch (IOException e)
            {
                // It seems IOException is thrown when it reaches EOF.
            }

            // Waits for the command to finish.
            waitFor.invoke(null, pid[0]);

            return output;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "";
    }

}