package alei.switchpro;

public class Constants {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String APP_NAME = "switchpro";
    public static final String PREFS_NAME = "SwitchProPrefs";

    public static final String MODEL_MILESTONE = "Milestone";
    public static final String MODEL_DROID = "Droid";
    public static final String MODEL_X10 = "X10i";
    public static final int DEFAULT_DEVIDER_COLOR = 0x34FFFFFF;
    public static final int DEFAULT_BACKGROUND_COLOR = 0xD3212121;
    public static final int NOT_SHOW_FLAG = 100;

    public static final String PKG_THEME_HOLO = "alei.switchpro.theme.holo";
    // 按钮标识
    public static final int BUTTON_WIFI = 0;
    public static final int BUTTON_EDGE = 1;
    public static final int BUTTON_BLUETOOTH = 2;
    public static final int BUTTON_GPS = 3;
    public static final int BUTTON_SYNC = 4;
    public static final int BUTTON_GRAVITY = 5;
    public static final int BUTTON_BRIGHTNESS = 6;
    public static final int BUTTON_BATTERY = 7;
    public static final int BUTTON_SCREEN_TIMEOUT = 8;
    public static final int BUTTON_AIRPLANE = 9;
    public static final int BUTTON_SCANMEDIA = 10;
    public static final int BUTTON_VIBRATE = 11;
    public static final int BUTTON_NET_SWITCH = 12;
    public static final int BUTTON_UNLOCK = 13;
    public static final int BUTTON_REBOOT = 14;
    public static final int BUTTON_FLASHLIGHT = 15;
    public static final int BUTTON_WIMAX = 16;
    public static final int BUTTON_SPEAKER = 17;
    public static final int BUTTON_AUTOLOCK = 18;
    public static final int BUTTON_WIFIAP = 19;
    public static final int BUTTON_MOUNT = 20;
    public static final int BUTTON_USBTE = 21;
    public static final int BUTTON_LOCK_SCREEN = 22;
    public static final int BUTTON_WIFI_SLEEP = 23;
    public static final int BUTTON_VOLUME = 24;
    public static final int BUTTON_KILL_PROCESS = 25;
    public static final int BUTTON_MEMORY_USAGE = 26;
    public static final int BUTTON_STORAGE_USAGE = 27;
    public static final int BUTTON_BT_TE = 28;
    public static final int BUTTON_NFC = 29;

    // 图标的标识
    public static final int ICON_WIFI = 0;
    public static final int ICON_EDGE = 1;
    public static final int ICON_BLUETOOTH = 2;
    public static final int ICON_GPS = 3;
    public static final int ICON_SYNC = 4;
    public static final int ICON_GRAVITY = 5;
    public static final int ICON_BRIGHTNESS = 6;
    public static final int ICON_BATTERY = 7;
    public static final int ICON_SCREEN_TIMEOUT = 8;
    public static final int ICON_AIRPLANE = 9;
    public static final int ICON_SCANMEDIA = 10;
    public static final int ICON_VIBRATE = 11;
    public static final int ICON_NET_SWITCH = 12;
    public static final int ICON_UNLOCK = 13;
    public static final int ICON_REBOOT = 14;
    public static final int ICON_FLASHLIGHT = 15;
    public static final int ICON_WIMAX = 16;
    public static final int ICON_SPEAKER = 17;
    public static final int ICON_AUTOLOCK = 18;
    public static final int ICON_WIFIAP = 19;
    public static final int ICON_MOUNT = 20;
    public static final int ICON_USBTE = 21;
    public static final int ICON_LOCK_SCREEN = 22;
    public static final int ICON_WIFI_SLEEP = 23;
    public static final int ICON_AUTO_BRIGHTNESS = 24;
    public static final int ICON_SILENT = 25;
    public static final int ICON_COUNT = 25;
    public static final int ICON_VOLUME = 26;
    public static final int ICON_KILL_PROCESS = 27;
    public static final int ICON_MEMORY_USAGE = 28;
    public static final int ICON_STORAGE_USAGE = 29;
    public static final int ICON_BT_TE = 30;
    public static final int ICON_NFC = 31;

    // 保存各种状态的参数
    public static final String PREFS_FLASH_STATE = "flashState";
    public static final String PREF_NET_STATE = "netstate";
    public static final String PREF_AUTOLOCK_STATE = "autolockState";
    public static final String PREF_4G_STATE = "4gState";

    // 全局参数
    public static final String PREFS_TOGGLE_WIFI = "toggle_wifi";
    public static final String PREFS_TOGGLE_BLUETOOTH = "toggle_bluetooth";
    public static final String PREFS_TOGGLE_GPS = "toggle_gps";
    public static final String PREFS_TOGGLE_SYNC = "toggle_sync";
    public static final String PREFS_AIRPLANE_WIFI = "airplane_wifi";
    public static final String PREFS_TOGGLE_FLASH = "toggle_usecamera";
    public static final String PREFS_TOGGLE_TIMEOUT = "toggle_timeout";
    public static final String PREFS_USE_APN = "use_apn";
    public static final String PREFS_MUTE_MEDIA = "mute_media";
    public static final String PREFS_MUTE_ALARM = "mute_alarm";
    public static final String PREFS_AIRPLANE_RADIO = "airplane_radio";
    public static final String PREFS_BRIGHT_LEVEL = "bright_level";
    public static final String PREFS_SILENT_BTN = "silent_btn";
    public static final String PREFS_DEVICE_TYPE = "device_type";
    public static final String PREFS_SYNC_NOW = "sync_now";
    public static final String PREFS_HAPTIC_FEEDBACK = "haptic_feedback";
    public static final String PREFS_SHOW_NOTIFY_ICON = "show_notify_icon";
    public static final String PREFS_NOTIFY_PRIORITY = "notify_priority";
    public static final String PREFS_BLUETOOTH_DISCOVER = "bluetooth_discover";
    public static final String PREFS_ICON_THEME = "icon_theme";
    public static final String PREFS_NOTIFY_ICON_COLOR = "notify_icon_color";
    public static final String PREFS_SHOW_BRIGHTNESS_BAR = "show_brightness_bar";
    public static final String PREFS_GPS_FIRST_LAUNCH = "gps_first_launch";

    // 电池参数
    public static final String PREFS_BATTERY_LEVEL = "Pref_Battery_Level";

    public static final String PREFS_CUSICON_FIELD_PATTERN = "cusIcon-%d";
    // 每个Widget的私有参数
    public static final String PREFS_BUTTONS_FIELD_PATTERN = "buttonIds-%d";
    public static final String PREFS_BACK_COLOR_FIELD_PATTERN = "backColor-%d";
    public static final String PREFS_BACK_IMAGE_FIELD_PATTERN = "backImage-%d";
    public static final String PREFS_IND_COLOR_FIELD_PATTERN = "indColor-%d";
    public static final String PREFS_ICON_COLOR_FIELD_PATTERN = "iconColor-%d";
    public static final String PREFS_ICON_TRANS_FIELD_PATTERN = "iconTrans-%d";
    public static final String PREFS_DIVIDER_COLOR_FIELD_PATTERN = "dividerColor-%d";
    public static final String PREFS_LAYOUT_FIELD_PATTERN = "widgetLayout-%d";

    // 创建Widget后保存的最近配置
    public static final String PREFS_LAST_BUTTONS_ORDER = "lastBtnOrder";
    public static final String PREFS_LAST_BACKGROUND = "lastWidgetLayout";
    public static final String PREFS_LAST_BACK_COLOR = "lastBackColor";
    public static final String PREFS_LAST_ICON_COLOR = "lastIconColor";
    public static final String PREFS_LAST_ICON_TRANS = "lastIconTrans";
    public static final String PREFS_LAST_IND_COLOR = "lastIndColor";
    public static final String PREFS_LAST_DIVIDER_COLOR = "lastDividerColor";
    public static final String PREFS_FLASH_COLOR = "flash_color";
    public static final String PREFS_IN_NOTIFICATION_BAR = "in_notification_bar";
    public static final String PREFS_LASE_NOTIFY_WIDGET = "last_notify_widget";

    /**
     * +45 green +90 lightblue +135 blue +180 purple -20 yellow -86 red -140 pink (-43 +83 Orange)
     */
    public static final int IND_COLOR_PINK = 0;
    public static final int IND_COLOR_RED = 1;
    public static final int IND_COLOR_ORANGE = 2;
    public static final int IND_COLOR_YELLOW = 3;
    public static final int IND_COLOR_DEFAULT = 4;
    public static final int IND_COLOR_GREEN = 5;
    public static final int IND_COLOR_LIGHTBLUE = 6;
    public static final int IND_COLOR_BLUE = 7;
    public static final int IND_COLOR_PURPLE = 8;
    public static final int IND_COLOR_GRAY = 9;

    public static final int TETHER_ERROR_NO_ERROR = 0;
    public static final int TETHER_ERROR_UNKNOWN_IFACE = 1;
    public static final int TETHER_ERROR_SERVICE_UNAVAIL = 2;
    public static final int TETHER_ERROR_UNSUPPORTED = 3;
    public static final int TETHER_ERROR_UNAVAIL_IFACE = 4;
    public static final int TETHER_ERROR_MASTER_ERROR = 5;
    public static final int TETHER_ERROR_TETHER_IFACE_ERROR = 6;
    public static final int TETHER_ERROR_UNTETHER_IFACE_ERROR = 7;
    public static final int TETHER_ERROR_ENABLE_NAT_ERROR = 8;
    public static final int TETHER_ERROR_DISABLE_NAT_ERROR = 9;
    public static final int TETHER_ERROR_IFACE_CFG_ERROR = 10;

    public static final String ATTR_WIDGET_BUTTONS = "buttonIds";
    public static final String ATTR_WIDGET_BACK_COLOR = "backColor";
    public static final String ATTR_WIDGET_IND_COLOR = "indColor";
    public static final String ATTR_WIDGET_ICON_COLOR = "iconColor";
    public static final String ATTR_WIDGET_ICON_TRANS = "iconTrans";
    public static final String ATTR_WIDGET_DIVIDER = "divColor";
    public static final String ATTR_WIDGET_LAYOUT = "layout";

    public static final String BACK_FILE_PATH = ".switchpro";

    // 静音振动按钮的配置
    public static final String BTN_ONLY_SILENT = "2";
    public static final String BTN_ONLY_VIVERATE = "1";
    public static final String BTN_VS = "0";

    // 设备类型的配置
    public static final String DEVICE_TYPE1 = "0";
    public static final String DEVICE_TYPE2 = "1";
    public static final String DEVICE_TYPE3 = "2";
    public static final String DEVICE_TYPE4 = "3";
    public static final String DEVICE_TYPE5 = "4";
    public static final String DEVICE_TYPE6 = "5";
    public static final String DEVICE_TYPE7 = "6";

    public interface TASK {
        public static final String TABLE_TASK = "tasks";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_START_HOUR = "starthour";
        public static final String COLUMN_START_MINUTES = "startminutes";
        public static final String COLUMN_END_HOUR = "endhour";
        public static final String COLUMN_END_MINUTES = "endminutes";
        public static final String COLUMN_DAYS_OF_WEEK = "daysofweek";
        public static final String COLUMN_START_TIME = "starttime";
        public static final String COLUMN_END_TIME = "endtime";
        public static final String COLUMN_ENABLED = "enabled";
        public static final String COLUMN_MESSAGE = "message";

        /**
         * These save calls to cursor.getColumnIndexOrThrow() 要与创建表格时的列保持同步
         */
        public static final int INDEX_ID = 0;
        public static final int INDEX_START_HOUR = 1;
        public static final int INDEX_START_MINUTES = 2;
        public static final int INDEX_END_HOUR = 3;
        public static final int INDEX_END_MINUTES = 4;
        public static final int INDEX_DAYS_OF_WEEK = 5;
        public static final int INDEX_START_TIME = 6;
        public static final int INDEX_END_TIME = 7;
        public static final int INDEX_ENABLED = 8;
        public static final int INDEX_MESSAGE = 9;
    }

    public interface SWITCH {
        public static final String TABLE_SWITCH = "switches";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TASK_ID = "taskid";
        public static final String COLUMN_SWITCH_ID = "switchid";
        public static final String COLUMN_PARAM1 = "param1";
        public static final String COLUMN_PARAM2 = "param2";

        public static final int INDEX_ID = 0;
        public static final int INDEX_TASK_ID = 1;
        public static final int INDEX_SWITCH_ID = 2;
        public static final int INDEX_PARAM1 = 3;
        public static final int INDEX_PARAM2 = 4;
    }

    public interface IGNORED {
        public static final String TABLE_IGNORED = "ignoreds";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";

        public static final int INDEX_ID = 0;
        public static final int INDEX_NAME = 1;
    }
}
