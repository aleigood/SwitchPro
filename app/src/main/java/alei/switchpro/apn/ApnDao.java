package alei.switchpro.apn;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public final class ApnDao {
    private static final String ID = "_id";
    private static final String APN = "apn";
    private static final String TYPE = "type";

    // from frameworks/base/core/java/android/provider/Telephony.java
    private static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers");

    // from packages/providers/TelephonyProvider/TelephonyProvider.java
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    private static final String PREFER_APN_ID_KEY = "apn_id";

    private static final String SUFFIX_START = "[";
    private static final String SUFFIX_END = "]";

    private static final String DB_LIKE_SUFFIX = SUFFIX_START + "%" + SUFFIX_END;

    private static final String[] MMS_SUFFIX = new String[]{SUFFIX_START + "mms" + SUFFIX_END};

    private final ContentResolver contentResolver;

    private int mmsTarget = ApnUtils.ON;
    private boolean disableAll = false;

    public ApnDao(ContentResolver contentResolver, int mmsTarget) {
        this.contentResolver = contentResolver;
        this.mmsTarget = mmsTarget;
    }

    public ApnDao(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    private static String addSuffix(String currentName) {
        if (currentName == null) {
            return SUFFIX_START + SUFFIX_END;
        } else {
            return SUFFIX_START + currentName + SUFFIX_END;
        }
    }

    private static String removeSuffix(String currentName) {
        if (currentName.startsWith(SUFFIX_START) && currentName.endsWith(SUFFIX_END)) {
            String tmp = currentName.substring(1, currentName.length() - SUFFIX_END.length());

            // 兼容老的版本
            if (tmp.endsWith("_off")) {
                return tmp.substring(0, tmp.length() - "_off".length());
            } else {
                return tmp;
            }
        } else {
            return currentName;
        }
    }

    /**
     * 设置是否要保持MMS连接
     *
     * @param mmsTarget
     */
    public void setMmsTarget(int mmsTarget) {
        this.mmsTarget = mmsTarget;
    }

    /**
     * 设置是否关闭所有apn
     *
     * @param disableAll
     */
    public void setDisableAllApns(boolean disableAll) {
        this.disableAll = disableAll;
    }

    /**
     * @return current mms state
     */
    public int getMmsState() {
        int countMmsApns = executeCountQuery("(type like ? or type like 'mms')"
                + (disableAll ? "" : " and current is not null"), MMS_SUFFIX);
        int countDisabledMmsApns = executeCountQuery("type like ?", MMS_SUFFIX);
        return countMmsApns > 0 && countDisabledMmsApns > 0 ? ApnUtils.OFF : ApnUtils.ON;
    }

    public long getRandomCurrentDataApn() {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(CONTENT_URI, new String[]{ID},
                    "(not lower(type)='mms' or type is null) and current is not null", null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }

    /**
     * 获得被引用的APN配置
     *
     * @return
     */
    public long getPreferredApnId() {
        Cursor cursor = contentResolver.query(PREFERRED_APN_URI, new String[]{ID}, null, null, null);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            return cursor.getLong(0);
        }

        return -1L;
    }

    /**
     * 恢复被引用的APN
     *
     * @param id
     */
    public void restorePreferredApn(long id) {
        ContentValues cv = new ContentValues();
        cv.putNull(PREFER_APN_ID_KEY);
        contentResolver.update(PREFERRED_APN_URI, cv, null, null);
        cv.put(PREFER_APN_ID_KEY, id);
        contentResolver.update(PREFERRED_APN_URI, cv, null, null);
    }

    /**
     * Performs switching apns work state according to passed state parameter
     *
     * @param targetState apn state. this method tries to make a switch passed target
     *                    state
     * @return {@code true} if switch was successfull (apn state changed) and
     * {@code false} if apn state was not changed
     */
    public boolean switchApnState(int targetState) {
        if (targetState == ApnUtils.OFF) {
            // 获取所有可用的APN配置
            String query;
            boolean disableAll = this.disableAll;
            String disableAllQuery = disableAll ? null : "current is not null";

            if (mmsTarget == ApnUtils.OFF) {
                query = disableAllQuery;
            } else {
                query = "(not lower(type)='mms' or type is null)";

                if (!disableAll) {
                    query += " and " + disableAllQuery;
                }
            }

            // Tries to disable apn's according to user preferences.
            List<ApnInfo> apns = selectApnInfo(query, null);

            // when selected apns is empty
            if (apns.isEmpty()) {
                int countDisabledApns = executeCountQuery("apn like ? or type like ?", new String[]{DB_LIKE_SUFFIX,
                        DB_LIKE_SUFFIX});
                return countDisabledApns > 0;
            }

            // if one o more apns changed and {@code false} if all APNs did not
            // changed their states
            return disableApnList(apns);
        } else {
            return enableAllInDb();
        }
    }

    /**
     * 获取“不关闭MMS”的状态是否是打开或关闭的
     *
     * @param targetState apn state. this method tries to passed target state
     * @return {@code true} if switch was successfull (apn state changed) and
     * {@code false} if apn state was not changed
     */
    public boolean switchMmsState(int targetState) {
        if (targetState == ApnUtils.OFF) {
            // selectEnabledMmsApns
            final List<ApnInfo> mmsList = selectApnInfo("type like ?" + (disableAll ? "" : " and current is not null"),
                    new String[]{"mms"});
            return mmsList.size() != 0 && disableApnList(mmsList);
        } else {
            // selectDisabledMmsApns
            return enableApnList(selectApnInfo("type like ?", MMS_SUFFIX));
        }
    }

    /**
     * 获取APN是否打开的状态 Calculates current apn state
     *
     * @return current apn state;
     */
    public int getApnState() {
        int countDisabledApns = executeCountQuery("apn like ? or type like ?", new String[]{DB_LIKE_SUFFIX,
                DB_LIKE_SUFFIX});
        return countDisabledApns == 0 ? ApnUtils.ON : ApnUtils.OFF;
    }

    private int executeCountQuery(String whereQuery, String[] whereParams) {
        Cursor cursor = null;

        try {
            cursor = contentResolver.query(CONTENT_URI, new String[]{"count(*)"}, whereQuery, whereParams, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return -1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private List<ApnInfo> selectApnInfo(String whereQuery, String[] whereParams) {
        Cursor cursor = null;

        try {
            cursor = contentResolver.query(CONTENT_URI, new String[]{ID, APN, TYPE}, whereQuery, whereParams, null);
            return createApnList(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean enableAllInDb() {
        // 获取已经被加了后缀的不可用的APN
        String suffix = DB_LIKE_SUFFIX;
        List<ApnInfo> apns = selectApnInfo("apn like ? or type like ?", new String[]{suffix, suffix});
        return enableApnList(apns);
    }

    /**
     * Creates list of apn dtos from a DB cursor
     *
     * @param mCursor db cursor with select result set
     * @return list of APN dtos
     */
    private List<ApnInfo> createApnList(Cursor mCursor) {
        List<ApnInfo> result = new ArrayList<ApnInfo>();
        mCursor.moveToFirst();

        while (!mCursor.isAfterLast()) {
            long id = mCursor.getLong(0);
            String apn = mCursor.getString(1);
            String type = mCursor.getString(2);
            result.add(new ApnInfo(id, apn, type));
            mCursor.moveToNext();
        }

        return result;
    }

    /**
     * Use this one if you have fresh list of APNs already and you can save one
     * query to DB
     *
     * @param apns list of apns data to modify
     * @return {@code true} if switch was successfull and {@code false}
     * otherwise
     */
    private boolean enableApnList(List<ApnInfo> apns) {
        final ContentResolver contentResolver = this.contentResolver;
        for (ApnInfo apnInfo : apns) {
            ContentValues values = new ContentValues();
            String newApnName = removeSuffix(apnInfo.apn);
            values.put(APN, newApnName);
            String newApnType = removeSuffix(apnInfo.type);
            if ("".equals(newApnType)) {
                values.putNull(TYPE);
            } else {
                values.put(TYPE, newApnType);
            }
            contentResolver.update(CONTENT_URI, values, ID + "=?", new String[]{String.valueOf(apnInfo.id)});

        }
        return true;// we always return true because in any situation we can
        // reset all apns to initial state
    }

    private boolean disableApnList(List<ApnInfo> apns) {
        final ContentResolver contentResolver = this.contentResolver;
        for (ApnInfo apnInfo : apns) {
            ContentValues values = new ContentValues();
            String newApnName = addSuffix(apnInfo.apn);
            values.put(APN, newApnName);
            String newApnType = addSuffix(apnInfo.type);
            values.put(TYPE, newApnType);
            contentResolver.update(CONTENT_URI, values, ID + "=?", new String[]{String.valueOf(apnInfo.id)});
        }
        return true;
    }

    /**
     * Selection of few interesting columns from APN table
     */
    private static final class ApnInfo {
        final long id;
        final String apn;
        final String type;

        public ApnInfo(long id, String apn, String type) {
            this.id = id;
            this.apn = apn;
            this.type = type;
        }
    }
}
