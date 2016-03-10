package alei.switchpro.color;

import alei.switchpro.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View
{
    private Paint ringPaint;
    // 点击中间圆形出现的圆环
    private Paint centerPaint;
    private Paint grayBarPaint;
    private Paint transBarPaint;

    private int[] transBarColors;
    private int[] grayBarColors;
    private int[] mColors;

    // 标记，防止在滑动灰度调节的时候改变了灰度条本身的颜色
    private boolean changeGrayBar;
    private boolean changeTransBar;
    private boolean needTransBar;

    private OnColorChangedListener mListener;

    private boolean mTrackingCenter;
    private boolean mHighlightCenter;

    private static final float PI = 3.1415926f;

    // 圆形与长条直接的距离
    private int SEPARATOR_HIGH = 10;

    private float paintWidth;
    private float width;
    private float r;
    private onColorChangingListener mOnColorChangingListener;

    public ColorPickerView(Context c, OnColorChangedListener l, int color, int x, boolean needTransBar)
    {
        super(c);
        this.needTransBar = needTransBar;
        // 通用画笔宽度
        paintWidth = x / 3f;
        // 总宽度
        width = x;
        // 大圆的半径 半径 = 总宽度 - 一个画笔的宽度 - 一个间隔 - 半个画笔的宽度
        r = width - paintWidth / 2f;

        mListener = l;

        // 大圆画笔
        mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setShader(new SweepGradient(0, 0, mColors, null));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(paintWidth);

        // 中间小圆画笔，StrokeWidth为点击时显示的圆环的半径
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(color);
        // 画笔的宽度在画实心圆的时候不起作用，画环的时候用
        centerPaint.setStrokeWidth(3f);

        // 定义细条的bar 颜色从 黑色过渡主面板选中的颜色再过渡到白色 主要解决 没有白色和黑色的问题
        grayBarColors = new int[] { 0xFF000000, Utils.setAlpha(color, false), 0xFFFFFFFF };
        grayBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grayBarPaint.setStrokeWidth(paintWidth);
        changeGrayBar = true;

        // 设置透明度的bar
        transBarColors = new int[] { Utils.setAlpha(color, false), Utils.setAlpha(color, true) };
        transBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transBarPaint.setStrokeWidth(paintWidth);
        changeTransBar = true;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        // 先设置透明
        canvas.translate(width, width);
        canvas.drawOval(new RectF(-r, -r, r, r), ringPaint);

        // 画中间的圆形，半径就是一个画笔的宽度
        canvas.drawCircle(0, 0, paintWidth, centerPaint);

        // 中间的圆被点击时绘制中间的圆环
        if (mTrackingCenter)
        {
            int c = centerPaint.getColor();
            centerPaint.setStyle(Paint.Style.STROKE);

            if (mHighlightCenter)
            {
                centerPaint.setAlpha(0xFF);
            }
            else
            {
                centerPaint.setAlpha(0x80);
            }

            canvas.drawCircle(0, 0, paintWidth + 5f, centerPaint);
            centerPaint.setStyle(Paint.Style.FILL);
            centerPaint.setColor(c);
        }

        // 绘制灰度细调的BAR

        // 标记，防止在滑动灰度调节的时候改变了灰度条本身的颜色
        if (changeGrayBar)
        {
            grayBarColors[1] = Utils.setAlpha(centerPaint.getColor(), false);
            grayBarPaint.setShader(new LinearGradient(-width, 0, width, 0, grayBarColors, null, Shader.TileMode.CLAMP));
        }
        canvas.drawRect(new RectF(-width, width + SEPARATOR_HIGH, width, width + SEPARATOR_HIGH + paintWidth),
                grayBarPaint);

        // 标记，防止在滑动透明度度调节的时候改变了灰度条本身的颜色
        if (changeTransBar)
        {
            transBarColors[0] = Utils.setAlpha(centerPaint.getColor(), false);
            transBarColors[1] = Utils.setAlpha(centerPaint.getColor(), true);
            transBarPaint
                    .setShader(new LinearGradient(-width, 0, width, 0, transBarColors, null, Shader.TileMode.CLAMP));
        }

        // 绘制透明度细调的BAR
        canvas.drawRect(new RectF(-width, width + SEPARATOR_HIGH * 2 + paintWidth, width, width + SEPARATOR_HIGH * 2
                + paintWidth * 2), transBarPaint);

        changeGrayBar = true;
        changeTransBar = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // 设置整个图形的范围要加上方形的宽度
        if (needTransBar)
        {
            setMeasuredDimension((int) (width * 2), (int) (width * 2 + SEPARATOR_HIGH * 2 + paintWidth * 2));
        }
        else
        {
            setMeasuredDimension((int) (width * 2), (int) (width * 2 + SEPARATOR_HIGH + paintWidth));
        }
    }

    private int ave(int s, int d, float p)
    {
        return s + java.lang.Math.round(p * (d - s));
    }

    private int interpColor(int colors[], float unit)
    {
        if (unit <= 0)
        {
            return colors[0];
        }
        if (unit >= 1)
        {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX() - width;
        float y = event.getY() - width;
        boolean inCenter = java.lang.Math.sqrt(x * x + y * y) <= paintWidth;

        // 超出范围外的不响应
        if ((x < -width || x > width || y < -width)
                || (y > width && y < width + SEPARATOR_HIGH)
                || (y > width + SEPARATOR_HIGH + paintWidth && y < width + SEPARATOR_HIGH + paintWidth + SEPARATOR_HIGH))
        {
            return false;
        }

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;

                if (inCenter)
                {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                attemptClaimDrag();

                if (mTrackingCenter)
                {
                    if (mHighlightCenter != inCenter)
                    {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                }
                // 灰度滑块
                else if ((x >= -width && x <= width)
                        && (y >= width + SEPARATOR_HIGH && y <= width + SEPARATOR_HIGH + paintWidth))
                {
                    int a, r, g, b, c0, c1;
                    float p;

                    if (x < 0)
                    {
                        c0 = grayBarColors[0];
                        c1 = grayBarColors[1];
                        p = (x + width) / width;
                    }
                    else
                    {
                        c0 = grayBarColors[1];
                        c1 = grayBarColors[2];
                        p = x / width;
                    }

                    a = ave(Color.alpha(c0), Color.alpha(c1), p);
                    r = ave(Color.red(c0), Color.red(c1), p);
                    g = ave(Color.green(c0), Color.green(c1), p);
                    b = ave(Color.blue(c0), Color.blue(c1), p);
                    // 把细调颜色设置到显示面板中
                    centerPaint.setColor(Color.argb(a, r, g, b));
                    changeTransBar = true;
                    changeGrayBar = false;
                    invalidate();
                }
                else if ((x >= -width && x <= width)
                        && (y >= width + SEPARATOR_HIGH + paintWidth + SEPARATOR_HIGH && y <= width + SEPARATOR_HIGH
                                + paintWidth + SEPARATOR_HIGH + paintWidth))
                {
                    int c0 = transBarColors[0];
                    int c1 = transBarColors[1];
                    float p = (x + width) / (width * 2);
                    int a = ave(Color.alpha(c0), Color.alpha(c1), p);

                    // 把细调颜色设置到显示面板中
                    centerPaint.setColor(Color.argb(a, Color.red(c0), Color.green(c0), Color.blue(c0)));
                    changeGrayBar = false;
                    changeTransBar = false;
                    invalidate();
                }
                else if ((x >= -width & x <= width) && (y <= width && y >= -width))
                {
                    float angle = (float) java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle / (2 * PI);

                    if (unit < 0)
                    {
                        unit += 1;
                    }
                    centerPaint.setColor(interpColor(mColors, unit));
                    invalidate();
                }

                if (mOnColorChangingListener != null)
                {
                    mOnColorChangingListener.onChange(centerPaint.getColor());
                }
                break;
            case MotionEvent.ACTION_UP:

                if (mTrackingCenter)
                {
                    if (inCenter)
                    {
                        mListener.colorChanged(centerPaint.getColor());
                    }
                    mTrackingCenter = false; // so we draw w/o halo
                    invalidate();
                }
                break;
        }
        return true;
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag()
    {
        if (getParent() != null)
        {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    public int getColor()
    {
        return centerPaint.getColor();
    }

    public interface OnColorChangedListener
    {
        void colorChanged(int color);
    }

    public void setColor(int color)
    {
        centerPaint.setColor(color);
        invalidate();
    }

    public void setOnColorChangingListener(onColorChangingListener listener)
    {
        mOnColorChangingListener = listener;
    }

    public interface onColorChangingListener
    {
        void onChange(int color);
    }

}
