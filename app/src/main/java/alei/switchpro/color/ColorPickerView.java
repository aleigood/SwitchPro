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

public class ColorPickerView extends View {
    private static final float PI = 3.1415926f;
    private Paint ringPaint;
    // ����м�Բ�γ��ֵ�Բ��
    private Paint centerPaint;
    private Paint grayBarPaint;
    private Paint transBarPaint;
    private int[] transBarColors;
    private int[] grayBarColors;
    private int[] mColors;
    // ��ǣ���ֹ�ڻ����Ҷȵ��ڵ�ʱ��ı��˻Ҷ����������ɫ
    private boolean changeGrayBar;
    private boolean changeTransBar;
    private boolean needTransBar;
    private OnColorChangedListener mListener;
    private boolean mTrackingCenter;
    private boolean mHighlightCenter;
    // Բ���볤��ֱ�ӵľ���
    private int SEPARATOR_HIGH = 10;

    private float paintWidth;
    private float width;
    private float r;
    private onColorChangingListener mOnColorChangingListener;

    public ColorPickerView(Context c, OnColorChangedListener l, int color, int x, boolean needTransBar) {
        super(c);
        this.needTransBar = needTransBar;
        // ͨ�û��ʿ��
        paintWidth = x / 3f;
        // �ܿ��
        width = x;
        // ��Բ�İ뾶 �뾶 = �ܿ�� - һ�����ʵĿ�� - һ����� - ������ʵĿ��
        r = width - paintWidth / 2f;

        mListener = l;

        // ��Բ����
        mColors = new int[]{0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setShader(new SweepGradient(0, 0, mColors, null));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(paintWidth);

        // �м�СԲ���ʣ�StrokeWidthΪ���ʱ��ʾ��Բ���İ뾶
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(color);
        // ���ʵĿ���ڻ�ʵ��Բ��ʱ�������ã�������ʱ����
        centerPaint.setStrokeWidth(3f);

        // ����ϸ����bar ��ɫ�� ��ɫ���������ѡ�е���ɫ�ٹ��ɵ���ɫ ��Ҫ��� û�а�ɫ�ͺ�ɫ������
        grayBarColors = new int[]{0xFF000000, Utils.setAlpha(color, false), 0xFFFFFFFF};
        grayBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grayBarPaint.setStrokeWidth(paintWidth);
        changeGrayBar = true;

        // ����͸���ȵ�bar
        transBarColors = new int[]{Utils.setAlpha(color, false), Utils.setAlpha(color, true)};
        transBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transBarPaint.setStrokeWidth(paintWidth);
        changeTransBar = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // ������͸��
        canvas.translate(width, width);
        canvas.drawOval(new RectF(-r, -r, r, r), ringPaint);

        // ���м��Բ�Σ��뾶����һ�����ʵĿ��
        canvas.drawCircle(0, 0, paintWidth, centerPaint);

        // �м��Բ�����ʱ�����м��Բ��
        if (mTrackingCenter) {
            int c = centerPaint.getColor();
            centerPaint.setStyle(Paint.Style.STROKE);

            if (mHighlightCenter) {
                centerPaint.setAlpha(0xFF);
            } else {
                centerPaint.setAlpha(0x80);
            }

            canvas.drawCircle(0, 0, paintWidth + 5f, centerPaint);
            centerPaint.setStyle(Paint.Style.FILL);
            centerPaint.setColor(c);
        }

        // ���ƻҶ�ϸ����BAR

        // ��ǣ���ֹ�ڻ����Ҷȵ��ڵ�ʱ��ı��˻Ҷ����������ɫ
        if (changeGrayBar) {
            grayBarColors[1] = Utils.setAlpha(centerPaint.getColor(), false);
            grayBarPaint.setShader(new LinearGradient(-width, 0, width, 0, grayBarColors, null, Shader.TileMode.CLAMP));
        }
        canvas.drawRect(new RectF(-width, width + SEPARATOR_HIGH, width, width + SEPARATOR_HIGH + paintWidth),
                grayBarPaint);

        // ��ǣ���ֹ�ڻ���͸���ȶȵ��ڵ�ʱ��ı��˻Ҷ����������ɫ
        if (changeTransBar) {
            transBarColors[0] = Utils.setAlpha(centerPaint.getColor(), false);
            transBarColors[1] = Utils.setAlpha(centerPaint.getColor(), true);
            transBarPaint
                    .setShader(new LinearGradient(-width, 0, width, 0, transBarColors, null, Shader.TileMode.CLAMP));
        }

        // ����͸����ϸ����BAR
        canvas.drawRect(new RectF(-width, width + SEPARATOR_HIGH * 2 + paintWidth, width, width + SEPARATOR_HIGH * 2
                + paintWidth * 2), transBarPaint);

        changeGrayBar = true;
        changeTransBar = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // ��������ͼ�εķ�ΧҪ���Ϸ��εĿ��
        if (needTransBar) {
            setMeasuredDimension((int) (width * 2), (int) (width * 2 + SEPARATOR_HIGH * 2 + paintWidth * 2));
        } else {
            setMeasuredDimension((int) (width * 2), (int) (width * 2 + SEPARATOR_HIGH + paintWidth));
        }
    }

    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
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
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - width;
        float y = event.getY() - width;
        boolean inCenter = java.lang.Math.sqrt(x * x + y * y) <= paintWidth;

        // ������Χ��Ĳ���Ӧ
        if ((x < -width || x > width || y < -width)
                || (y > width && y < width + SEPARATOR_HIGH)
                || (y > width + SEPARATOR_HIGH + paintWidth && y < width + SEPARATOR_HIGH + paintWidth + SEPARATOR_HIGH)) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;

                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                attemptClaimDrag();

                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                }
                // �ҶȻ���
                else if ((x >= -width && x <= width)
                        && (y >= width + SEPARATOR_HIGH && y <= width + SEPARATOR_HIGH + paintWidth)) {
                    int a, r, g, b, c0, c1;
                    float p;

                    if (x < 0) {
                        c0 = grayBarColors[0];
                        c1 = grayBarColors[1];
                        p = (x + width) / width;
                    } else {
                        c0 = grayBarColors[1];
                        c1 = grayBarColors[2];
                        p = x / width;
                    }

                    a = ave(Color.alpha(c0), Color.alpha(c1), p);
                    r = ave(Color.red(c0), Color.red(c1), p);
                    g = ave(Color.green(c0), Color.green(c1), p);
                    b = ave(Color.blue(c0), Color.blue(c1), p);
                    // ��ϸ����ɫ���õ���ʾ�����
                    centerPaint.setColor(Color.argb(a, r, g, b));
                    changeTransBar = true;
                    changeGrayBar = false;
                    invalidate();
                } else if ((x >= -width && x <= width)
                        && (y >= width + SEPARATOR_HIGH + paintWidth + SEPARATOR_HIGH && y <= width + SEPARATOR_HIGH
                        + paintWidth + SEPARATOR_HIGH + paintWidth)) {
                    int c0 = transBarColors[0];
                    int c1 = transBarColors[1];
                    float p = (x + width) / (width * 2);
                    int a = ave(Color.alpha(c0), Color.alpha(c1), p);

                    // ��ϸ����ɫ���õ���ʾ�����
                    centerPaint.setColor(Color.argb(a, Color.red(c0), Color.green(c0), Color.blue(c0)));
                    changeGrayBar = false;
                    changeTransBar = false;
                    invalidate();
                } else if ((x >= -width & x <= width) && (y <= width && y >= -width)) {
                    float angle = (float) java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle / (2 * PI);

                    if (unit < 0) {
                        unit += 1;
                    }
                    centerPaint.setColor(interpColor(mColors, unit));
                    invalidate();
                }

                if (mOnColorChangingListener != null) {
                    mOnColorChangingListener.onChange(centerPaint.getColor());
                }
                break;
            case MotionEvent.ACTION_UP:

                if (mTrackingCenter) {
                    if (inCenter) {
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
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    public int getColor() {
        return centerPaint.getColor();
    }

    public void setColor(int color) {
        centerPaint.setColor(color);
        invalidate();
    }

    public void setOnColorChangingListener(onColorChangingListener listener) {
        mOnColorChangingListener = listener;
    }

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public interface onColorChangingListener {
        void onChange(int color);
    }

}
