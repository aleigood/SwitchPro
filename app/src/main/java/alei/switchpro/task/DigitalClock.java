package alei.switchpro.task;

import java.util.Calendar;

import alei.switchpro.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Displays the time
 */
public class DigitalClock extends LinearLayout
{
    private final static String M24 = "kk:mm";
    private final static String M12 = "h:mm";

    private Calendar mCalendarStart;
    private Calendar mCalendarEnd;
    private String mFormat;
    private TextView mTimeDisplayStart;
    private TextView mTimeDisplayEnd;
    private AmPm mAmPm;
    private ContentObserver mFormatChangeObserver;
    private boolean mLive = true;
    private boolean mAttached;

    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (mLive && intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
            {
                mCalendarStart = Calendar.getInstance();
            }
            updateTime();
        }
    };

    private Context mContext;

    static class AmPm
    {
        private int mColorOn, mColorOff;

        private LinearLayout mAmPmLayoutStart;
        private LinearLayout mAmPmLayoutEnd;
        private TextView mAmStart, mPmStart;
        private TextView mAmEnd, mPmEnd;

        AmPm(View parent)
        {
            mAmPmLayoutStart = (LinearLayout) parent.findViewById(R.id.am_pm);
            mAmPmLayoutEnd = (LinearLayout) parent.findViewById(R.id.am_pm2);
            mAmStart = (TextView) mAmPmLayoutStart.findViewById(R.id.am);
            mPmStart = (TextView) mAmPmLayoutStart.findViewById(R.id.pm);
            mAmEnd = (TextView) mAmPmLayoutEnd.findViewById(R.id.am2);
            mPmEnd = (TextView) mAmPmLayoutEnd.findViewById(R.id.pm2);

            Resources r = parent.getResources();
            mColorOn = r.getColor(R.color.ampm_on);
            mColorOff = r.getColor(R.color.ampm_off);
        }

        void setShowAmPm(boolean show)
        {
            mAmPmLayoutStart.setVisibility(show ? View.VISIBLE : View.GONE);
            mAmPmLayoutEnd.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        void setIsMorningStart(boolean isMorning)
        {
            mAmStart.setTextColor(isMorning ? mColorOn : mColorOff);
            mPmStart.setTextColor(isMorning ? mColorOff : mColorOn);
        }

        void setIsMorningEnd(boolean isMorning)
        {
            mAmEnd.setTextColor(isMorning ? mColorOn : mColorOff);
            mPmEnd.setTextColor(isMorning ? mColorOff : mColorOn);
        }
    }

    private class FormatChangeObserver extends ContentObserver
    {
        public FormatChangeObserver()
        {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange)
        {
            setDateFormat();
            updateTime();
        }
    }

    public DigitalClock(Context context)
    {
        this(context, null);
        mContext = context;
    }

    public DigitalClock(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mTimeDisplayStart = (TextView) findViewById(R.id.timeDisplay);
        mTimeDisplayEnd = (TextView) findViewById(R.id.timeDisplay2);
        mAmPm = new AmPm(this);
        mCalendarStart = Calendar.getInstance();
        mCalendarEnd = Calendar.getInstance();

        setDateFormat();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        if (mAttached)
            return;
        mAttached = true;

        if (mLive)
        {
            /* monitor time ticks, time changed, timezone */
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
        }

        /* monitor 12/24-hour display preference */
        mFormatChangeObserver = new FormatChangeObserver();
        mContext.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        updateTime();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if (!mAttached)
            return;
        mAttached = false;

        Drawable background = getBackground();
        if (background instanceof AnimationDrawable)
        {
            ((AnimationDrawable) background).stop();
        }

        if (mLive)
        {
            mContext.unregisterReceiver(mIntentReceiver);
        }
        mContext.getContentResolver().unregisterContentObserver(mFormatChangeObserver);
    }

    void updateTime(Calendar cStart, Calendar cEnd)
    {
        mCalendarStart = cStart;
        mCalendarEnd = cEnd;
        updateTime();
    }

    private void updateTime()
    {
        if (mLive)
        {
            mCalendarStart.setTimeInMillis(System.currentTimeMillis());
            mCalendarEnd.setTimeInMillis(System.currentTimeMillis());
        }

        CharSequence newTimeStart = DateFormat.format(mFormat, mCalendarStart);
        CharSequence newTimeEnd = DateFormat.format(mFormat, mCalendarEnd);
        mTimeDisplayStart.setText(newTimeStart);
        mTimeDisplayEnd.setText(newTimeEnd);
        mAmPm.setIsMorningStart(mCalendarStart.get(Calendar.AM_PM) == 0);
        mAmPm.setIsMorningEnd(mCalendarEnd.get(Calendar.AM_PM) == 0);
    }

    private void setDateFormat()
    {
        mFormat = TaskUtil.get24HourMode(mContext) ? M24 : M12;
        mAmPm.setShowAmPm(mFormat == M12);
    }

    void setLive(boolean live)
    {
        mLive = live;
    }

    public void setContext(Context context)
    {
        mContext = context;
    }
}
