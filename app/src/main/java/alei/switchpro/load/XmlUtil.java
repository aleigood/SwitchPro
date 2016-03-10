package alei.switchpro.load;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import alei.switchpro.Constants;
import alei.switchpro.DatabaseOper;
import alei.switchpro.brightness.LevelPreference;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Xml;

public class XmlUtil
{
    private static final String TAG_WIDGET = "widget";
    private static final String TAG_GLOBAL = "global";
    private static final String TAG_APP_NAME = "name";

    private static boolean saveToFile(byte[] msgs, String fileName)
    {
        try
        {
            // 在保存之前需要判断 SDCard 是否存在,并且是否具有可写权限：
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                // 获取SDCard目录,2.2的时候为:/mnt/sdcart
                // 2.1的时候为：/sdcard，所以使用静态方法得到路径会好一点。
                File sdCardDir = Environment.getExternalStorageDirectory();
                File dir = new File(sdCardDir.getPath() + File.separator + Constants.BACK_FILE_PATH);

                if (!dir.exists())
                {
                    dir.mkdir();
                }

                File saveFile = new File(dir.getPath(), fileName);
                FileOutputStream outStream = new FileOutputStream(saveFile);
                outStream.write(msgs);
                outStream.close();
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }

        return false;
    }

    /**
     * 假设两个文件所在目录都存在
     * 
     * @param oldPath
     * @param newPath
     * @return
     */
    public static boolean copyFile(String oldPath, String newPath)
    {
        try
        {
            int byteRead = 0;

            // 文件存在时
            if (new File(oldPath).exists())
            {
                InputStream is = new FileInputStream(oldPath);
                FileOutputStream fos = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];

                while ((byteRead = is.read(buffer)) != -1)
                {
                    fos.write(buffer, 0, byteRead);
                }

                fos.close();
                is.close();
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public static boolean deleteFile(String fileName)
    {
        try
        {
            // 在保存之前需要判断 SDCard 是否存在,并且是否具有可写权限：
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                // 获取SDCard目录,2.2的时候为:/mnt/sdcart
                // 2.1的时候为：/sdcard，所以使用静态方法得到路径会好一点。
                File sdCardDir = Environment.getExternalStorageDirectory();
                File file = new File(sdCardDir.getPath() + File.separator + Constants.BACK_FILE_PATH + File.separator
                        + fileName);

                if (file.exists())
                {
                    return file.delete();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 读取配置文件，当出现错误时返回null
     * 
     * @param fileName
     * @return
     */
    private static FileInputStream readFile(String fileName)
    {
        try
        {
            // 在保存之前需要判断 SDCard 是否存在,并且是否具有可写权限：
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                // 获取SDCard目录,2.2的时候为:/mnt/sdcart
                // 2.1的时候为：/sdcard，所以使用静态方法得到路径会好一点。
                File sdCardDir = Environment.getExternalStorageDirectory();
                File file = new File(sdCardDir.getPath() + File.separator + Constants.BACK_FILE_PATH + File.separator
                        + fileName);

                if (file.exists())
                {
                    FileInputStream fis = new FileInputStream(file);
                    return fis;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 将配置转换成xml的字符串
     * 
     * @param XmlEntitys
     * @return
     */
    public static boolean writeWidgetXml(List<XmlEntity> XmlEntitys, String fileName)
    {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        String result = "";

        try
        {
            serializer.setOutput(writer);
            serializer.startDocument(Constants.DEFAULT_ENCODING, true);
            serializer.startTag("", Constants.APP_NAME);

            for (XmlEntity msg : XmlEntitys)
            {
                serializer.startTag("", TAG_WIDGET);
                serializer.attribute("", Constants.ATTR_WIDGET_BUTTONS, msg.getBtnIds());
                serializer.attribute("", Constants.ATTR_WIDGET_ICON_COLOR, msg.getIconColor() + "");
                serializer.attribute("", Constants.ATTR_WIDGET_ICON_TRANS, msg.getIconTrans() + "");
                serializer.attribute("", Constants.ATTR_WIDGET_BACK_COLOR, msg.getBackColor() + "");
                serializer.attribute("", Constants.ATTR_WIDGET_IND_COLOR, msg.getIndColor() + "");
                serializer.attribute("", Constants.ATTR_WIDGET_DIVIDER, msg.getDividerColor() + "");
                serializer.attribute("", Constants.ATTR_WIDGET_LAYOUT, msg.getLayoutName());
                serializer.endTag("", TAG_WIDGET);
            }

            serializer.endTag("", Constants.APP_NAME);
            serializer.endDocument();
            result = writer.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return saveToFile(result.getBytes(), fileName);
    }

    /**
     * @param is
     * @return 当读取文件出错时返回Size为0的集合
     */
    public static List<XmlEntity> parseWidgetCfg(String fileName)
    {
        InputStream is = readFile(fileName);
        List<XmlEntity> xmlEntitys = new ArrayList<XmlEntity>();

        if (is == null)
        {
            return xmlEntitys;
        }

        XmlPullParser parser = Xml.newPullParser();

        try
        {
            // auto-detect the encoding from the stream
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            XmlEntity currentXmlEntity = null;
            boolean done = false;

            while (eventType != XmlPullParser.END_DOCUMENT && !done)
            {
                String name = null;

                switch (eventType)
                {
                    case XmlPullParser.START_DOCUMENT:
                        xmlEntitys = new ArrayList<XmlEntity>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(TAG_WIDGET))
                        {
                            currentXmlEntity = new XmlEntity();
                            currentXmlEntity.setBtnIds(parser.getAttributeValue("", Constants.ATTR_WIDGET_BUTTONS));
                            currentXmlEntity.setIconColor(Integer.parseInt(parser.getAttributeValue("",
                                    Constants.ATTR_WIDGET_ICON_COLOR)));
                            currentXmlEntity.setIconTrans(Integer.parseInt(parser.getAttributeValue("",
                                    Constants.ATTR_WIDGET_ICON_TRANS)));
                            currentXmlEntity.setIndColor(Integer.parseInt(parser.getAttributeValue("",
                                    Constants.ATTR_WIDGET_IND_COLOR)));
                            currentXmlEntity.setBackColor(Integer.parseInt(parser.getAttributeValue("",
                                    Constants.ATTR_WIDGET_BACK_COLOR)));
                            currentXmlEntity.setDividerColor(Integer.parseInt(parser.getAttributeValue("",
                                    Constants.ATTR_WIDGET_DIVIDER)));
                            currentXmlEntity.setLayoutName(parser.getAttributeValue("", Constants.ATTR_WIDGET_LAYOUT));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(TAG_WIDGET) && currentXmlEntity != null)
                        {
                            xmlEntitys.add(currentXmlEntity);
                        }
                        else if (name.equalsIgnoreCase(Constants.APP_NAME))
                        {
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }

            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return xmlEntitys;
    }

    /**
     * 将配置转换成xml的字符串
     * 
     * @param XmlEntitys
     * @return
     */
    public static boolean writeGlobalXml(Context context, String fileName)
    {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        String result = "";

        try
        {
            serializer.setOutput(writer);
            serializer.startDocument(Constants.DEFAULT_ENCODING, true);
            serializer.startTag("", Constants.APP_NAME);

            serializer.startTag("", TAG_GLOBAL);

            if (config.contains(Constants.PREFS_TOGGLE_WIFI))
            {
                serializer.attribute("", Constants.PREFS_TOGGLE_WIFI,
                        config.getBoolean(Constants.PREFS_TOGGLE_WIFI, false) + "");
            }

            if (config.contains(Constants.PREFS_TOGGLE_BLUETOOTH))
            {
                serializer.attribute("", Constants.PREFS_TOGGLE_BLUETOOTH,
                        config.getBoolean(Constants.PREFS_TOGGLE_BLUETOOTH, false) + "");
            }

            if (config.contains(Constants.PREFS_TOGGLE_GPS))
            {
                serializer.attribute("", Constants.PREFS_TOGGLE_GPS,
                        config.getBoolean(Constants.PREFS_TOGGLE_GPS, false) + "");
            }

            if (config.contains(Constants.PREFS_TOGGLE_SYNC))
            {
                serializer.attribute("", Constants.PREFS_TOGGLE_SYNC,
                        config.getBoolean(Constants.PREFS_TOGGLE_SYNC, false) + "");
            }

            if (config.contains(Constants.PREFS_AIRPLANE_WIFI))
            {
                serializer.attribute("", Constants.PREFS_AIRPLANE_WIFI,
                        config.getBoolean(Constants.PREFS_AIRPLANE_WIFI, false) + "");
            }

            if (config.contains(Constants.PREFS_TOGGLE_FLASH))
            {
                serializer.attribute("", Constants.PREFS_TOGGLE_FLASH,
                        config.getBoolean(Constants.PREFS_TOGGLE_FLASH, true) + "");
            }

            if (config.contains(Constants.PREFS_TOGGLE_TIMEOUT))
            {
                serializer.attribute("", Constants.PREFS_TOGGLE_TIMEOUT,
                        config.getBoolean(Constants.PREFS_TOGGLE_TIMEOUT, false) + "");
            }

            if (config.contains(Constants.PREFS_USE_APN))
            {
                serializer.attribute("", Constants.PREFS_USE_APN, config.getBoolean(Constants.PREFS_USE_APN, false)
                        + "");
            }

            if (config.contains(Constants.PREFS_MUTE_MEDIA))
            {
                serializer.attribute("", Constants.PREFS_MUTE_MEDIA,
                        config.getBoolean(Constants.PREFS_MUTE_MEDIA, false) + "");
            }

            if (config.contains(Constants.PREFS_MUTE_ALARM))
            {
                serializer.attribute("", Constants.PREFS_MUTE_ALARM,
                        config.getBoolean(Constants.PREFS_MUTE_ALARM, false) + "");
            }

            if (config.contains(Constants.PREFS_AIRPLANE_RADIO))
            {
                serializer.attribute("", Constants.PREFS_AIRPLANE_RADIO,
                        config.getBoolean(Constants.PREFS_AIRPLANE_RADIO, false) + "");
            }

            if (config.contains(Constants.PREFS_SYNC_NOW))
            {
                serializer.attribute("", Constants.PREFS_SYNC_NOW, config.getBoolean(Constants.PREFS_SYNC_NOW, false)
                        + "");
            }

            if (config.contains(Constants.PREFS_BRIGHT_LEVEL))
            {
                serializer.attribute("", Constants.PREFS_BRIGHT_LEVEL,
                        config.getString(Constants.PREFS_BRIGHT_LEVEL, LevelPreference.DEFAULT_LEVEL));
            }

            if (config.contains(Constants.PREFS_SILENT_BTN))
            {
                serializer.attribute("", Constants.PREFS_SILENT_BTN,
                        config.getString(Constants.PREFS_SILENT_BTN, Constants.BTN_VS));
            }

            if (config.contains(Constants.PREFS_DEVICE_TYPE))
            {
                serializer.attribute("", Constants.PREFS_DEVICE_TYPE,
                        config.getString(Constants.PREFS_DEVICE_TYPE, "0") + "");
            }

            for (int i = 0; i < Constants.ICON_COUNT; i++)
            {
                String tmp = String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, i);
                String existFileName = config.getString(tmp, "");

                if (config.contains(tmp))
                {
                    serializer.attribute("", tmp, existFileName);
                }

                // 把图标文件也拷贝到SD卡
                try
                {
                    // 在保存之前需要判断 SDCard 是否存在,并且是否具有可写权限：
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    {
                        // 获取SDCard目录,2.2的时候为:/mnt/sdcart
                        // 2.1的时候为：/sdcard，所以使用静态方法得到路径会好一点。
                        File sdCardDir = Environment.getExternalStorageDirectory();
                        File dir = new File(sdCardDir.getPath() + File.separator + Constants.BACK_FILE_PATH);

                        if (!dir.exists())
                        {
                            dir.mkdir();
                        }

                        copyFile(context.getFileStreamPath(existFileName).getPath(), dir.getPath() + File.separator
                                + existFileName);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            serializer.endTag("", TAG_GLOBAL);
            serializer.endTag("", Constants.APP_NAME);
            serializer.endDocument();
            result = writer.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return saveToFile(result.getBytes(), fileName);
    }

    public static boolean writeProcessExcludeToXml(DatabaseOper ap)
    {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try
        {
            serializer.setOutput(writer);
            serializer.startDocument(Constants.DEFAULT_ENCODING, true);
            serializer.startTag("", Constants.APP_NAME);
            Cursor cursor = ap.queryIgnored();

            if (cursor.moveToFirst())
            {
                do
                {
                    String pakName = cursor.getString(Constants.IGNORED.INDEX_NAME);

                    if (pakName != null && !pakName.equals(""))
                    {
                        serializer.startTag("", TAG_APP_NAME);
                        serializer.attribute("", Constants.IGNORED.COLUMN_NAME, pakName);
                        serializer.endTag("", TAG_APP_NAME);
                    }
                }
                while (cursor.moveToNext());
            }

            cursor.close();
            serializer.endTag("", Constants.APP_NAME);
            serializer.endDocument();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return saveToFile(writer.toString().getBytes(), Constants.IGNORED.TABLE_IGNORED);
    }

    /**
     * 备份全局参数，包括图标设置
     * 
     * @param is
     * @return 当读取文件出错时返回null
     */
    public static boolean parseGlobalCfg(Context context, String fileName)
    {
        InputStream is = readFile(fileName);

        if (is == null)
        {
            return false;
        }

        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        XmlPullParser parser = Xml.newPullParser();

        try
        {
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            boolean done = false;

            while (eventType != XmlPullParser.END_DOCUMENT && !done)
            {
                String name = null;

                switch (eventType)
                {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(TAG_GLOBAL))
                        {
                            // 是否打开wifi界面
                            String prefToggleWifi = parser.getAttributeValue("", Constants.PREFS_TOGGLE_WIFI);

                            if (prefToggleWifi != null)
                            {
                                editor.putBoolean(Constants.PREFS_TOGGLE_WIFI, Boolean.parseBoolean(prefToggleWifi))
                                        .commit();
                            }

                            // 是否打开蓝牙界面
                            String prefToggleBt = parser.getAttributeValue("", Constants.PREFS_TOGGLE_BLUETOOTH);

                            if (prefToggleBt != null)
                            {
                                editor.putBoolean(Constants.PREFS_TOGGLE_BLUETOOTH, Boolean.parseBoolean(prefToggleBt))
                                        .commit();
                            }

                            // 是否打开GPS界面
                            String prefToggleGPS = parser.getAttributeValue("", Constants.PREFS_TOGGLE_GPS);

                            if (prefToggleGPS != null)
                            {
                                editor.putBoolean(Constants.PREFS_TOGGLE_GPS, Boolean.parseBoolean(prefToggleGPS))
                                        .commit();
                            }

                            // 是否打开同步界面
                            String prefToggleSync = parser.getAttributeValue("", Constants.PREFS_TOGGLE_SYNC);

                            if (prefToggleSync != null)
                            {
                                editor.putBoolean(Constants.PREFS_TOGGLE_SYNC, Boolean.parseBoolean(prefToggleSync))
                                        .commit();
                            }

                            // 飞行模式是否关闭wifi
                            String prefToggleAireWifi = parser.getAttributeValue("", Constants.PREFS_AIRPLANE_WIFI);

                            if (prefToggleAireWifi != null)
                            {
                                editor.putBoolean(Constants.PREFS_AIRPLANE_WIFI,
                                        Boolean.parseBoolean(prefToggleAireWifi)).commit();
                            }

                            // 是否使用闪光灯
                            String prefToggleFlash = parser.getAttributeValue("", Constants.PREFS_TOGGLE_FLASH);

                            if (prefToggleFlash != null)
                            {
                                editor.putBoolean(Constants.PREFS_TOGGLE_FLASH, Boolean.parseBoolean(prefToggleFlash))
                                        .commit();
                            }

                            // 是否显示时间选择器
                            String prefToggleTime = parser.getAttributeValue("", Constants.PREFS_TOGGLE_TIMEOUT);

                            if (prefToggleTime != null)
                            {
                                editor.putBoolean(Constants.PREFS_TOGGLE_TIMEOUT, Boolean.parseBoolean(prefToggleTime))
                                        .commit();
                            }

                            // 是否使用APN
                            String prefApn = parser.getAttributeValue("", Constants.PREFS_USE_APN);

                            if (prefApn != null)
                            {
                                editor.putBoolean(Constants.PREFS_USE_APN, Boolean.parseBoolean(prefApn)).commit();
                            }

                            // 是否关闭媒体音量
                            String prefMuteMedia = parser.getAttributeValue("", Constants.PREFS_MUTE_MEDIA);

                            if (prefMuteMedia != null)
                            {
                                editor.putBoolean(Constants.PREFS_MUTE_MEDIA, Boolean.parseBoolean(prefMuteMedia))
                                        .commit();
                            }

                            // 是否关闭警告音量
                            String prefMuteAlarm = parser.getAttributeValue("", Constants.PREFS_MUTE_ALARM);

                            if (prefMuteAlarm != null)
                            {
                                editor.putBoolean(Constants.PREFS_MUTE_ALARM, Boolean.parseBoolean(prefMuteAlarm))
                                        .commit();
                            }

                            // 是否关闭手机信号
                            String prefRadio = parser.getAttributeValue("", Constants.PREFS_AIRPLANE_RADIO);

                            if (prefRadio != null)
                            {
                                editor.putBoolean(Constants.PREFS_AIRPLANE_RADIO, Boolean.parseBoolean(prefRadio))
                                        .commit();
                            }

                            // 亮度级别
                            String preBrightLevel = parser.getAttributeValue("", Constants.PREFS_BRIGHT_LEVEL);

                            if (preBrightLevel != null)
                            {
                                editor.putString(Constants.PREFS_BRIGHT_LEVEL, preBrightLevel).commit();
                            }

                            // 静音按钮
                            String preSilentBtn = parser.getAttributeValue("", Constants.PREFS_SILENT_BTN);

                            if (preSilentBtn != null)
                            {
                                editor.putString(Constants.PREFS_SILENT_BTN, preSilentBtn).commit();
                            }

                            // 手机类型
                            String preDvieceType = parser.getAttributeValue("", Constants.PREFS_DEVICE_TYPE);

                            if (preDvieceType != null)
                            {
                                editor.putString(Constants.PREFS_DEVICE_TYPE, preDvieceType).commit();
                            }

                            // 是否立即同步
                            String preSyncNow = parser.getAttributeValue("", Constants.PREFS_SYNC_NOW);

                            if (preSyncNow != null)
                            {
                                editor.putBoolean(Constants.PREFS_SYNC_NOW, Boolean.parseBoolean(preSyncNow)).commit();
                            }

                            for (int i = 0; i < Constants.ICON_COUNT; i++)
                            {
                                String tmp = String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, i);
                                String stored = parser.getAttributeValue("", tmp);

                                // 如果属性存在
                                if (stored != null)
                                {
                                    editor.putString(tmp, stored).commit();

                                    try
                                    {
                                        // 在保存之前需要判断 SDCard 是否存在,并且是否具有可写权限：
                                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                                        {
                                            // 获取SDCard目录,2.2的时候为:/mnt/sdcart
                                            // 2.1的时候为：/sdcard，所以使用静态方法得到路径会好一点。
                                            File sdCardDir = Environment.getExternalStorageDirectory();
                                            File dir = new File(sdCardDir.getPath() + File.separator
                                                    + Constants.BACK_FILE_PATH);

                                            if (!dir.exists())
                                            {
                                                dir.mkdir();
                                            }

                                            copyFile(dir.getPath() + File.separator + stored, context.getFilesDir()
                                                    + File.separator + stored);
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();

                                    }
                                }
                            }
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(Constants.APP_NAME))
                        {
                            done = true;
                        }

                        break;
                }
                eventType = parser.next();
            }

            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean parseProcessExcludeCfg(DatabaseOper ap)
    {
        XmlPullParser parser = Xml.newPullParser();
        FileInputStream fis = null;

        try
        {
            // 在保存之前需要判断 SDCard 是否存在,并且是否具有可写权限：
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                // 获取SDCard目录,2.2的时候为:/mnt/sdcart
                // 2.1的时候为：/sdcard，所以使用静态方法得到路径会好一点。
                File sdCardDir = Environment.getExternalStorageDirectory();
                File file = new File(sdCardDir.getPath() + File.separator + Constants.BACK_FILE_PATH + File.separator
                        + Constants.IGNORED.TABLE_IGNORED);

                if (file.exists())
                {
                    fis = new FileInputStream(file);
                }
            }

            if (fis == null)
            {
                return false;
            }

            parser.setInput(fis, null);
            int eventType = parser.getEventType();
            boolean done = false;
            // 删除数据库所有的内容
            ap.deleteAllIgnoredApp();

            while (eventType != XmlPullParser.END_DOCUMENT && !done)
            {
                String name = null;

                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(TAG_APP_NAME))
                        {
                            ContentValues values = new ContentValues(1);
                            values.put(Constants.IGNORED.COLUMN_NAME,
                                    parser.getAttributeValue("", Constants.IGNORED.COLUMN_NAME));
                            ap.insertIgnoredApp(values);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();

                        if (name.equalsIgnoreCase(Constants.APP_NAME))
                        {
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }

            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;
    }
}
