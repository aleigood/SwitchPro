package alei.switchpro.icon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class BitmapDrawable extends Drawable {
    private Bitmap mBitmap;

    BitmapDrawable(Bitmap paramBitmap) {
        this.mBitmap = paramBitmap;
    }

    public void draw(Canvas paramCanvas) {
        Bitmap localBitmap = this.mBitmap;
        paramCanvas.drawBitmap(localBitmap, 0.0F, 0.0F, null);
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public int getIntrinsicHeight() {
        return this.mBitmap.getHeight();
    }

    public int getIntrinsicWidth() {
        return this.mBitmap.getWidth();
    }

    public int getMinimumHeight() {
        return this.mBitmap.getHeight();
    }

    public int getMinimumWidth() {
        return this.mBitmap.getWidth();
    }

    public int getOpacity() {
        return -1;
    }

    public void setAlpha(int paramInt) {
    }

    public void setColorFilter(ColorFilter paramColorFilter) {
    }
}