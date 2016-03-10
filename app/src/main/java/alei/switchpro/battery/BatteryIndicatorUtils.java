package alei.switchpro.battery;

import alei.switchpro.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

public class BatteryIndicatorUtils
{

    /**
     * Returns a bitmap with digits for the specified level
     * 
     * @param resources
     *            the resources of the application. (e.g. getResources() in the
     *            activity class)
     * @param level
     *            the level of the battery
     * @return the generated image
     */
    public static Bitmap getBitmap(Resources resources, int level)
    {
        if (level < 0 || level > 100)
        {
            return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_blank);
        }

        // Return the "F" bitmap in case of a fully loaded battery
        if (level >= 100)
        {
            return BitmapFactory.decodeResource(resources, R.drawable.icon_battery_full);
        }

        // inMutable属性level 11才支持，这里只能复制
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap scaledBitmap = BitmapFactory.decodeResource(resources, R.drawable.batterynumber_blank, options);
        Bitmap bitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true);
        scaledBitmap.recycle();

        // for a level less than 10, only add one digit
        if (level < 10)
        {
            // get the bitmap for the digit
            Bitmap insertedBitmap = getBitmapForNumber(resources, level);
            // create an array as buffer for the colors
            int[] pixels = new int[insertedBitmap.getWidth() * insertedBitmap.getHeight()];
            // fill the buffer with the digit image data
            insertedBitmap.getPixels(pixels, 0, insertedBitmap.getWidth(), 0, 0, insertedBitmap.getWidth(),
                    insertedBitmap.getHeight());
            // copy the image data to the main battery bitmap
            bitmap.setPixels(pixels, 0, insertedBitmap.getWidth(), 18, 18, insertedBitmap.getWidth(),
                    insertedBitmap.getHeight());

            insertedBitmap.recycle();
        }
        else
        {
            int firstnumber = level / 10;
            int secondnumber = level % 10;

            // get the bitmap for the first digit
            Bitmap insertedBitmap = getBitmapForNumber(resources, firstnumber);
            // create an array as buffer for the colors
            int[] pixels = new int[insertedBitmap.getWidth() * insertedBitmap.getHeight()];
            // fill the buffer with the digit image data
            insertedBitmap.getPixels(pixels, 0, insertedBitmap.getWidth(), 0, 0, insertedBitmap.getWidth(),
                    insertedBitmap.getHeight());
            // copy the image data to the main battery bitmap
            bitmap.setPixels(pixels, 0, insertedBitmap.getWidth(), 13, 18, insertedBitmap.getWidth(),
                    insertedBitmap.getHeight());

            insertedBitmap = getBitmapForNumber(resources, secondnumber);
            // create an array as buffer for the colors
            pixels = new int[insertedBitmap.getWidth() * insertedBitmap.getHeight()];
            // fill the buffer with the digit image data
            insertedBitmap.getPixels(pixels, 0, insertedBitmap.getWidth(), 0, 0, insertedBitmap.getWidth(),
                    insertedBitmap.getHeight());
            // copy the image data to the main battery bitmap
            bitmap.setPixels(pixels, 0, insertedBitmap.getWidth(), 24, 18, insertedBitmap.getWidth(),
                    insertedBitmap.getHeight());

            insertedBitmap.recycle();
        }

        return bitmap;
    }

    /**
     * Returns the bitmap for the given digit
     * 
     * @param resources
     *            the resource of the application
     * @param number
     *            the number to return the bitmap for
     * @return the bitmap for the number (or null of not in 0..9)
     */
    private static Bitmap getBitmapForNumber(Resources resources, int number)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        switch (number)
        {
            case 0:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_0, options);
            case 1:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_1, options);
            case 2:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_2, options);
            case 3:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_3, options);
            case 4:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_4, options);
            case 5:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_5, options);
            case 6:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_6, options);
            case 7:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_7, options);
            case 8:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_8, options);
            case 9:
                return BitmapFactory.decodeResource(resources, R.drawable.batterynumber_9, options);
        }
        return null;
    }

    // 自定义图标时使用。此方法直接把文字画在图片上
    public static Bitmap getIcon(Context paramContext, Bitmap srcBitmap, int level)
    {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        int[] pixels = new int[width * height];
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // 从原位图中复制像素
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        DisplayMetrics displayMetrics = paramContext.getResources().getDisplayMetrics();

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        // 抗锯齿
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(13.0F * displayMetrics.density);

        Canvas canvas = new android.graphics.Canvas(newBitmap);
        canvas.drawText(level == 100 ? "F" : level + "", 16.0F * displayMetrics.density,
                22.0F * displayMetrics.density, paint);
        return newBitmap;
    }
}
