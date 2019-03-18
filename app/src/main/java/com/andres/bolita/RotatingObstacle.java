package com.andres.bolita;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.shapes.Shape;
import android.util.Log;

import com.andres.bolita.Obstacle;

public class RotatingObstacle extends Obstacle {

    private static final int INITIAL_ROTATION = 0;
    private double rotationAngle;
    private double lastPointAngle;
    private double lastPointRadius;

    private float[] points;
    private float strokeWidth;

    public RotatingObstacle(Context context, float x, float y, float width, float height) {
        super(context, x, y, width, height, null);

        rotationAngle = (float) Math.toRadians(INITIAL_ROTATION);
        points = new float[8];
        points[0] = this.getX();
        points[1] = this.getY();

        lastPointAngle =  Math.atan(this.getObstacleHeight() / this.getObstacleWidth());
        lastPointRadius =       Math.sqrt(
                                        Math.pow(this.getObstacleWidth(), 2) + Math.pow(this.getObstacleHeight(), 2));

        calculatePoints();

        strokeWidth = this.getObstacleHeight();
    }

    public void rotate(float degrees) {
        rotationAngle += degrees;

        if (rotationAngle > 359) {
            rotationAngle -= 359;
        } else if (rotationAngle < 0) {
            rotationAngle += 359;
        }

        calculatePoints();

        /*
        Log.d("aX", String.valueOf(points[0]));
        Log.d("aY", String.valueOf(points[1]));
        Log.d("bX", String.valueOf(points[2]));
        Log.d("bY", String.valueOf(points[3]));
        Log.d("cX", String.valueOf(points[4]));
        Log.d("cY", String.valueOf(points[5]));
        Log.d("dX", String.valueOf(points[6]));
        Log.d("dY", String.valueOf(points[7]));
           */

        this.postInvalidate();
    }

    private void calculatePoints() {

        points[2] = (int) (points[0] + Math.cos(rotationAngle) * this.getObstacleWidth());
        points[3] = (int) (points[1] + Math.sin(rotationAngle) * this.getObstacleWidth());

        points[4] = (int) (points[0] + Math.cos(rotationAngle + 300) * this.getObstacleHeight());
        points[5] = (int) (points[1] + Math.sin(rotationAngle + 300) * this.getObstacleHeight());

        points[6] = (int) (points[0] + Math.cos(rotationAngle - lastPointAngle) * this.lastPointRadius);
        points[7] = (int) (points[1] + Math.sin(rotationAngle - lastPointAngle) * this.lastPointRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setStrokeWidth(strokeWidth);
        canvas.drawLine(points[0], points[1], points[2], points[3], paint);

        paint.setStrokeWidth(2);
        canvas.drawLine(points[0], points[1], points[4], points[5], paint);

        paint.setStrokeWidth(strokeWidth);
        canvas.drawLine(points[4], points[5], points[6], points[7], paint);

        paint.setStrokeWidth(2);
        canvas.drawLine(points[2], points[3], points[6], points[7], paint);
    }

    public double getRotationAngle() { return rotationAngle; }

    public Path getLowerBound() {

        Path path = new Path();
        path.moveTo(points[0], points[1]);
        path.lineTo(points[1], points[2]);

        return path;
    }
}
