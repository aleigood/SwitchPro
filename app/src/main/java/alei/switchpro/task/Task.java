package alei.switchpro.task;

import alei.switchpro.Constants;
import alei.switchpro.R;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public final class Task {
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel p) {
            return new Task(p);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    public int id;
    public int startHour;
    public int startMinutes;
    public int endHour;
    public int endMinutes;
    public DaysOfWeek daysOfWeek;
    public long startTime;
    public long endTime;
    public boolean enabled;
    /**
     * ���ڱ�Ǵ�Task�ǿ�ʼ�����ǽ������񣬲��������ݿ�
     */
    public int type;
    public String message;

    public Task(Parcel p) {
        id = p.readInt();
        startHour = p.readInt();
        startMinutes = p.readInt();
        endHour = p.readInt();
        endMinutes = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        startTime = p.readLong();
        endTime = p.readLong();
        enabled = p.readInt() == 1;
        type = p.readInt();
        message = p.readString();
    }

    public Task(Cursor c) {
        id = c.getInt(Constants.TASK.INDEX_ID);
        startHour = c.getInt(Constants.TASK.INDEX_START_HOUR);
        startMinutes = c.getInt(Constants.TASK.INDEX_START_MINUTES);
        endHour = c.getInt(Constants.TASK.INDEX_END_HOUR);
        endMinutes = c.getInt(Constants.TASK.INDEX_END_MINUTES);
        daysOfWeek = new DaysOfWeek(c.getInt(Constants.TASK.INDEX_DAYS_OF_WEEK));
        startTime = c.getLong(Constants.TASK.INDEX_START_TIME);
        endTime = c.getLong(Constants.TASK.INDEX_END_TIME);
        enabled = c.getInt(Constants.TASK.INDEX_ENABLED) == 1;
        message = c.getString(Constants.TASK.INDEX_MESSAGE);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(id);
        p.writeInt(startHour);
        p.writeInt(startMinutes);
        p.writeInt(endHour);
        p.writeInt(endMinutes);
        p.writeInt(daysOfWeek.getCoded());
        p.writeLong(startTime);
        p.writeLong(endTime);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(type);
        p.writeString(message);
    }

    /*
     * Days of week code as a single int. 0x00: no day 0x01: Monday 0x02:
     * Tuesday 0x04: Wednesday 0x08: Thursday 0x10: Friday 0x20: Saturday 0x40:
     * Sunday
     */
    static final class DaysOfWeek {
        private static int[] DAY_MAP = new int[]{Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY,};

        // Bitmask of all repeating days
        private int mDays;

        DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context, boolean showNever) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == 0) {
                return showNever ? context.getText(R.string.never).toString() : "";
            }

            // every day
            if (mDays == 0x7f) {
                return context.getText(R.string.every_day).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1)
                    dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ? dfs.getShortWeekdays() : dfs.getWeekdays();

            // selected days
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > 0)
                        ret.append(context.getText(R.string.day_concat));
                }
            }
            return ret.toString();
        }

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }

        /**
         * returns number of days from today until next alarm
         *
         * @param c must be set to today
         */
        public int getNextAlarm(Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            int day = 0;
            int dayCount = 0;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }
    }
}
