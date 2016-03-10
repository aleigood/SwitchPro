package alei.switchpro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOper
{
    private SQLiteOpenHelper mOpenHelper;
    private Context context;

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "switchpro.db";
        private static final int DATABASE_VERSION = 8;

        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL("CREATE TABLE " + Constants.TASK.TABLE_TASK + "  (" + Constants.TASK.COLUMN_ID
                    + " INTEGER PRIMARY KEY," + Constants.TASK.COLUMN_START_HOUR + " INTEGER, "
                    + Constants.TASK.COLUMN_START_MINUTES + " INTEGER, " + Constants.TASK.COLUMN_END_HOUR
                    + " INTEGER, " + Constants.TASK.COLUMN_END_MINUTES + " INTEGER, "
                    + Constants.TASK.COLUMN_DAYS_OF_WEEK + " INTEGER, " + Constants.TASK.COLUMN_START_TIME
                    + " INTEGER, " + Constants.TASK.COLUMN_END_TIME + " INTEGER, " + Constants.TASK.COLUMN_ENABLED
                    + " INTEGER, " + Constants.TASK.COLUMN_MESSAGE + " TEXT);");

            db.execSQL("CREATE TABLE " + Constants.SWITCH.TABLE_SWITCH + " (" + Constants.SWITCH.COLUMN_ID
                    + " INTEGER PRIMARY KEY," + Constants.SWITCH.COLUMN_TASK_ID + " INTEGER,"
                    + Constants.SWITCH.COLUMN_SWITCH_ID + " INTEGER, " + Constants.SWITCH.COLUMN_PARAM1 + " TEXT,"
                    + Constants.SWITCH.COLUMN_PARAM2 + " TEXT);");

            db.execSQL("CREATE TABLE " + Constants.IGNORED.TABLE_IGNORED + " (" + Constants.IGNORED.COLUMN_ID
                    + " INTEGER PRIMARY KEY," + Constants.IGNORED.COLUMN_NAME + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TASK.TABLE_TASK);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.SWITCH.TABLE_SWITCH);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.IGNORED.TABLE_IGNORED);
            onCreate(db);
        }
    }

    public DatabaseOper(Context context)
    {
        this.context = context;
        mOpenHelper = new DatabaseHelper(context);
    }

    public Cursor queryAllTask()
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + Constants.TASK.TABLE_TASK, null);
    }

    public Cursor queryTaskById(int id)
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + Constants.TASK.TABLE_TASK + " WHERE " + Constants.TASK.COLUMN_ID + "="
                + id, null);
    }

    public Cursor queryEnabledTask()
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + Constants.TASK.TABLE_TASK + " WHERE " + Constants.TASK.COLUMN_ENABLED
                + "=" + 1, null);
    }

    public int updateTask(int rowId, ContentValues values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(Constants.TASK.TABLE_TASK, values, Constants.TASK.COLUMN_ID + "=?",
                new String[] { String.valueOf(rowId) });
    }

    public int insertTask(ContentValues initialValues)
    {
        if (!initialValues.containsKey(Constants.TASK.COLUMN_START_HOUR))
            initialValues.put(Constants.TASK.COLUMN_START_HOUR, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_START_MINUTES))
            initialValues.put(Constants.TASK.COLUMN_START_MINUTES, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_END_HOUR))
            initialValues.put(Constants.TASK.COLUMN_END_HOUR, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_END_MINUTES))
            initialValues.put(Constants.TASK.COLUMN_END_MINUTES, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_DAYS_OF_WEEK))
            initialValues.put(Constants.TASK.COLUMN_DAYS_OF_WEEK, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_START_TIME))
            initialValues.put(Constants.TASK.COLUMN_START_TIME, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_END_TIME))
            initialValues.put(Constants.TASK.COLUMN_END_TIME, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_ENABLED))
            initialValues.put(Constants.TASK.COLUMN_ENABLED, 0);

        if (!initialValues.containsKey(Constants.TASK.COLUMN_MESSAGE))
            initialValues.put(Constants.TASK.COLUMN_MESSAGE, "");

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return (int) db.insert(Constants.TASK.TABLE_TASK, Constants.TASK.COLUMN_MESSAGE, initialValues);
    }

    public int deleteTask(int rowId)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(Constants.TASK.TABLE_TASK, Constants.TASK.COLUMN_ID + "=?",
                new String[] { String.valueOf(rowId) });
        db.delete(Constants.SWITCH.TABLE_SWITCH, Constants.SWITCH.COLUMN_TASK_ID + "=?",
                new String[] { String.valueOf(rowId) });
        return count;
    }

    public Cursor querySwitchesByTaskId(int id)
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + Constants.SWITCH.TABLE_SWITCH + " WHERE "
                + Constants.SWITCH.COLUMN_TASK_ID + "=" + id, null);
    }

    public int deleteSwitchById(int taskId)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(Constants.SWITCH.TABLE_SWITCH, Constants.SWITCH.COLUMN_TASK_ID + "=?",
                new String[] { String.valueOf(taskId) });
        return count;
    }

    public int updateSwitch(int taskId, int switchId, ContentValues values)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(Constants.SWITCH.TABLE_SWITCH, values, Constants.SWITCH.COLUMN_TASK_ID + "=? and "
                + Constants.SWITCH.COLUMN_SWITCH_ID + "=?",
                new String[] { String.valueOf(taskId), String.valueOf(switchId) });
    }

    public int insertSwitch(ContentValues initialValues)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return (int) db.insert(Constants.SWITCH.TABLE_SWITCH, null, initialValues);
    }

    public Context getContext()
    {
        return context;
    }

    public void close()
    {
        mOpenHelper.getWritableDatabase().close();
        mOpenHelper.getReadableDatabase().close();
    }

    public int insertIgnoredApp(ContentValues initialValues)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return (int) db.insert(Constants.IGNORED.TABLE_IGNORED, null, initialValues);
    }

    public int deleteIgnoredApp(String name)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.delete(Constants.IGNORED.TABLE_IGNORED, Constants.IGNORED.COLUMN_NAME + "=?",
                new String[] { name.replace("'", "''") });
    }

    public int deleteAllIgnoredApp()
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.delete(Constants.IGNORED.TABLE_IGNORED, null, null);
    }

    public boolean isIgnored(String name)
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Constants.IGNORED.TABLE_IGNORED + " WHERE "
                + Constants.IGNORED.COLUMN_NAME + "='" + name.replace("'", "''") + "'", null);
        boolean ret = cursor.getCount() != 0;
        cursor.close();
        return ret;
    }

    public Cursor queryIgnored()
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + Constants.IGNORED.TABLE_IGNORED, null);
    }
}
