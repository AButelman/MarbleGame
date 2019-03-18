package com.andres.bolita;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import static android.content.Context.VIBRATOR_SERVICE;

public class Obstacle extends View implements Runnable {

    public static final float WOOD_BAR_REBOUND_ABSORPTION = 2.8f;
    public static final float ERASER_REBOUND_ABSORPTION = 1.3f;
    public static final float REBOUND_ABSORPTION_ON_VERTICES = 1.2f;

    public enum ObstacleTypes {

        WOOD_BAR_HORIZONTAL (R.drawable.wood_bar_horizontal, WOOD_BAR_REBOUND_ABSORPTION),
        WOOD_BAR_VERTICAL (R.drawable.wood_bar_vertical, WOOD_BAR_REBOUND_ABSORPTION),

        ERASER_HORIZONTAL (R.drawable.eraser_horizontal, ERASER_REBOUND_ABSORPTION),
        ERASER_VERTICAL (R.drawable.eraser_vertical, ERASER_REBOUND_ABSORPTION);

        private int resourceId;
        private float reboundAbsorption;

        ObstacleTypes(int id, float reboundAbsorption) {

            this.resourceId = id;
            this.reboundAbsorption = reboundAbsorption;
        }

    }

    Bitmap image;
    private ObstacleTypes obstacleType;
    protected float x, y, obstacleWidth, obstacleHeight, reboundAbsorption;
    protected Rect bounds;
    protected Rect surfaceRect;
    protected Paint paint;
    protected Point center;

    protected boolean isShaking;
    private boolean isHorizontalShake;
    private int shakingTimes;
    private int shakingDistance;
    private boolean isVibrating;

    public Obstacle(Context context, float x, float y, float width, float height, ObstacleTypes obstacleType) {
        super(context);

        this.x = x;
        this.y = y;
        this.obstacleWidth = width;
        this.obstacleHeight = height;
        this.obstacleType = obstacleType;
        this.reboundAbsorption = obstacleType.reboundAbsorption;

        this.isShaking = false;
        this.shakingTimes = 0;
        this.shakingDistance = 0;
        this.isVibrating = false;

        image = BitmapFactory.decodeResource(getContext().getResources(), obstacleType.resourceId, null);
        image = Bitmap.createScaledBitmap(image, (int) width, (int) height, true);
        // CAMBIAR EL TAMAÑO A LA IMAGEN

        center = new Point();

        calculateBounds();
        calculateCenter();
    }

    protected void calculateBounds() {

        bounds = new Rect((int) x , (int) y, (int) (x + obstacleWidth), (int) (y + obstacleHeight));
        surfaceRect = new Rect((int) x - 2, (int) y - 2, (int) (x + obstacleWidth + 4), (int) (y + obstacleHeight + 4));
    }

    protected void calculateCenter() {
        float centerX = this.x + this.obstacleWidth / 2;
        float centerY = this.y + this.obstacleHeight / 2;
        center.set((int) centerX, (int) centerY);
    }

    public void shake(boolean isHorizontalShake, float speed) {

        if (!isShaking) {

            this.isHorizontalShake = isHorizontalShake;
            this.shakingTimes = (int) (1.15 * speed);

            if (obstacleType == ObstacleTypes.WOOD_BAR_HORIZONTAL || obstacleType == ObstacleTypes.WOOD_BAR_VERTICAL) {
                shakingDistance = 4;
            } else if (obstacleType == ObstacleTypes.ERASER_HORIZONTAL || obstacleType == ObstacleTypes.ERASER_VERTICAL) {
                shakingDistance = 5;
            } else {
                // This is only to vibrate the phone when the obstacle is moving and won't shake but vibrate
                shakingDistance = 1;
            }

            // Vibrate the cellphone
            vibrate((int) (shakingDistance * shakingTimes * 3));

            // Only animate the shake if the obstacle is not moving
            if (!(this instanceof MovingObstacle)) {
                Thread t = new Thread(this);
                t.start();
            }
        }
    }

    private void vibrate(long milliseconds) {

        // Log.e("Vibrating for ", String.valueOf(milliseconds) + " ms.");

        if (!isVibrating) {

            Vibration vibration = new Vibration(milliseconds);
            Thread t = new Thread(vibration);
            t.start();
        }
    }

    class Vibration implements Runnable {

        private long milliseconds;

        public Vibration(long milliseconds) {
            this.milliseconds = milliseconds;
        }

        @Override
        public void run() {

            isVibrating = true;

            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).
                        vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(milliseconds);
            }

            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            isVibrating = false;
        }
    }

    @Override
    public void run() {

        isShaking = true;

        for (int i = 1; i <= this.shakingTimes; i++) {

            // Goes up 10 pixels
            for (int j = 1; j <= shakingDistance; j++) {

                if (isHorizontalShake) {

                    this.setX(this.getX() - 1);
                } else {

                    this.setY(this.getY() - 1);
                }

                this.postInvalidate();

                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {}
            }

            // Goes back to the start
            for (int j = 1; j <= shakingDistance; j++) {

                if (isHorizontalShake) {
                    this.setX(this.getX() + 1);
                } else {
                    this.setY(this.getY() + 1);
                }

                this.postInvalidate();

                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {}
            }

            // Goes down 10 pixels
            for (int j = 1; j <= shakingDistance; j++) {

                if (isHorizontalShake) {
                    this.setX(this.getX() + 1);
                } else {
                    this.setY(this.getY() + 1);
                }

                this.postInvalidate();

                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {}
            }

            // Goes back to the start
            for (int j = 1; j <= shakingDistance; j++) {

                if (isHorizontalShake) {
                    this.setX(this.getX() - 1);
                } else {
                    this.setY(this.getY() - 1);
                }

                this.postInvalidate();

                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {}
            }
        }

        isShaking = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(image, (int) x, (int) y, null);

        // TODO DIBUJA EL CENTRO, DESPUÉS SACAR!!!!!!!
        if ((this.obstacleType == ObstacleTypes.WOOD_BAR_HORIZONTAL || this.obstacleType == ObstacleTypes.WOOD_BAR_VERTICAL)
                && this instanceof MovingObstacle) {

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(8);
            canvas.drawPoint((int) this.center.x, (int) this.center.y, paint);
        }
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
        calculateBounds();
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
        calculateBounds();
    }

    public float getObstacleWidth() {
        return obstacleWidth;
    }

    public void setWidth(float width) {
        this.obstacleWidth = width;
        calculateBounds();
    }

    public float getObstacleHeight() {
        return obstacleHeight;
    }

    public void setHeight(float height) {
        this.obstacleHeight = height;
        calculateBounds();
    }

    public Rect getBounds() {
        return bounds;
    }

    public Rect getSurfaceRect() {
        return surfaceRect;
    }

    public float getReboundAbsorption() { return reboundAbsorption; }

    public Bitmap getImage() { return image; }

    public Point getCenter() { return center; }

    public boolean isShaking() { return isShaking; }

}
