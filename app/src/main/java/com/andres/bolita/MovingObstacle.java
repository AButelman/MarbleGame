package com.andres.bolita;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class MovingObstacle extends Obstacle implements Runnable{

    public enum Movement {

        LEFT(-1, 0, 0),
        RIGHT(1, 0, 0),
        UP(0, -1, 0),
        DOWN(0, 1, 0),
        CLOCK_WISE_ROTATION(0, 0, 1f),
        COUNTER_CLOCK_WISE_ROTATION(0, 0, -1f);

        private int horizontalMov, verticalMov;
        private float rotationMov;

        Movement(int horizontal, int vertical, float rotationMov) {
            this.horizontalMov = horizontal;
            this.verticalMov = vertical;
            this.rotationMov = rotationMov;
        }
    }

    private Movement movement;
    private float velocity, rotationVelocity, rotationAngle;
    private float rotationRadius;
    private int horizontalMov, verticalMov;
    private float rotationMov;

    public MovingObstacle(Context context, float x, float y, float width, float height, ObstacleTypes obstacleType,
                          Movement movement, float velocity, float rotationVelocity) {
        super(context, x, y, width, height, obstacleType);

        this.centerPivot();
        this.rotationAngle = 0;

        this.movement = movement;
        this.horizontalMov = movement.horizontalMov;
        this.verticalMov = movement.verticalMov;
        this.rotationMov = movement.rotationMov;

        this.velocity = velocity;
        this.rotationVelocity = rotationVelocity;

        if (this.isRotating()) {
            calculateRotationRadius();
        }

    }

    private void calculateRotationRadius() {

        rotationRadius = (float) Math.sqrt( (this.obstacleWidth / 2) * (this.obstacleWidth / 2) +
                                            (this.obstacleHeight / 2) * (this.obstacleHeight / 2) );
    }

    public void startAnimation() {

        Thread t = new Thread(this);
        t.start();
    }

    private void checkScreenBorders() {

        // Screen top
        if (bounds.top < 0) {
            Log.d("Rebote", "arriba");
            this.y = 0;
            this.verticalMov = -verticalMov;

        // Screen bottom
        } else if (bounds.bottom > MainActivity.screenHeight) {
            Log.d("Rebote", "abajo");
            this.y = MainActivity.screenHeight - obstacleHeight;
            this.verticalMov = -verticalMov;

        // Screen left
        } else if (this.getBounds().left < 0) {
            this.x = 0;
            this.horizontalMov = -horizontalMov;

        // Screen right
        } else if (this.getBounds().right > MainActivity.screenWidth) {
            this.x = MainActivity.screenWidth - obstacleWidth;
            this.horizontalMov = -horizontalMov;
        }

    }

    private void checkBall() {

        Ball ball = MainActivity.ball;
        Rect ballBounds = ball.getBounds();
        Point ballCenter = ball.getCenter();

        // The ball is at the bottom of the screen and the obstacle too
        if (ballBounds.bottom >= MainActivity.screenHeight - 1 && this.movement == Movement.DOWN && this.bounds.intersect(ballBounds)) {

            this.y = ballBounds.top - obstacleHeight - 1;
            this.verticalMov = -verticalMov;

            // The ball is at the top of the screen and the obstacle too
        } else if (ballBounds.top <= 1 && this.movement == Movement.UP && this.bounds.intersect(ballBounds)) {

            this.y = ballBounds.bottom + 1;
            this.verticalMov = -verticalMov;

            // The ball is at the right of the screen and the obstacle too
        } else if (ballBounds.right >= MainActivity.screenWidth - 1 && this.movement == Movement.RIGHT && this.bounds.intersect(ballBounds)) {

            this.x = ballBounds.left - obstacleWidth - 1;
            this.horizontalMov = -horizontalMov;

            // The ball is at the left of the screen and the obstacle too
        } else if (ballBounds.left <= 1 && this.movement == Movement.LEFT && this.bounds.intersect(ballBounds)) {

            this.y = ballBounds.right + 1;
            this.horizontalMov = -horizontalMov;
        }
    }

    private void checkHole() {
        Rect holeBounds = MainActivity.hole.getBounds();

        if (this.bounds.intersect(holeBounds)) {
            this.horizontalMov = -horizontalMov;
            this.verticalMov = -verticalMov;
        }
    }

    private void checkOtherObstacles() {

        ArrayList<Obstacle> obstacles = MainActivity.obstacles;
        for (Obstacle obstacle : obstacles) {

            if (!obstacle.equals(this)) {

                Rect otherBounds = obstacle.bounds;

                if (this.bounds.intersect(otherBounds)) {

                    if (obstacle instanceof MovingObstacle) {

                        MovingObstacle movingObstacle = (MovingObstacle) obstacle;

                        if (this.isTheMovementHorizontal() && movingObstacle.isTheMovementHorizontal()) {
                            horizontalMov = -horizontalMov;
                        } else if (this.isTheMovementVertical() && movingObstacle.isTheMovementVertical()) {
                            verticalMov = -verticalMov;
                        }

                        /* ESTO HAY QUE CAMBIARLO PARA QUE SÓLO CAMBIE LA DIRECCIÓN DEPENDIENDO DEL
                            LADO QUE REBOTE EL OBSTÁCULO
                         */
                        horizontalMov = -horizontalMov;
                        verticalMov = -verticalMov;

                    } else {

                        horizontalMov = -horizontalMov;
                        verticalMov = -verticalMov;
                    }

                }
            }

        }
    }

    private void centerPivot() {
        this.setPivotX(this.center.x);
        this.setPivotY(this.center.y);
    }

    @Override
    public void run() {

        while (true) {

            this.x += velocity * horizontalMov;
            this.y += velocity * verticalMov;

            if (!isRotating()) {

                this.calculateBounds();
                this.calculateCenter();
            }

            this.setRotation(rotationAngle);
            rotationAngle += rotationMov * rotationVelocity;

            if (rotationAngle > 359) rotationAngle = 0; else if (rotationAngle < 0) rotationAngle = 359;

            checkBall();
            checkHole();
            checkOtherObstacles();
            checkScreenBorders();

            this.postInvalidate();

            try {
                Thread.sleep(17);
            } catch (InterruptedException e) {}

        }
    }

    public int getHorizontalMov() { return horizontalMov; }
    public int getVerticalMov() { return verticalMov; }

    public boolean isTheMovementVertical() {
        boolean isVertical = false;

        if (movement == Movement.UP || movement == Movement.DOWN) {
            isVertical = true;
        }

        return isVertical;
    }

    public boolean isTheMovementHorizontal() {
        boolean isHorizontal = false;

        if (movement == Movement.LEFT || movement == Movement.RIGHT) {
            isHorizontal = true;
        }

        return isHorizontal;
    }

    public Movement getMovement() { return movement; }

    public boolean isRotating() {
        return movement == Movement.CLOCK_WISE_ROTATION || movement == Movement.COUNTER_CLOCK_WISE_ROTATION;
    }

    public float getRotationAngle() { return rotationAngle; }

    public float getAngularVelocity() { return rotationMov * rotationVelocity; }

    public float getRotationRadius() { return rotationRadius; }
}
