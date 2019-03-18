package com.andres.bolita;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class Hole extends View {

    private Bitmap image;
    private Point center;
    private float x, y, side;
    private Rect bounds;

    public Hole (Context context, float x, float y) {
        super(context);

        this.x = x;
        this.y = y;

        image = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hole3, null);

        side = image.getWidth();

        center = new Point((int) (x + side / 2), (int) (y + side / 2));

        bounds = new Rect((int) x + 20, (int) y + 20, (int) (x + side - 60), (int) (y + side - 60));
    }


    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(image, x, y, null);
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    public float getSide() { return side; }

    public Point getCenter() { return center; }

    public Bitmap getImage() { return image; }

    public Rect getBounds() { return bounds; }
}
