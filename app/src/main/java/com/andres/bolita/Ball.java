package com.andres.bolita;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;


public class Ball extends AppCompatImageView {

    private MainActivity mainActivity;

    private final float divisor = 3.5f;

    private float newX, newY;
    private float speedX, speedY;
    private float radius;
    private boolean isFalling;
    private Point center;

    private Rect bounds;

    private boolean cancelNormalRotation;

    private boolean centerBall;

    public Ball(Context context, float x, float y, boolean centerBall) {

        super(context);
        this.setX(x);
        this.setY(y);

        this.centerBall = centerBall;
    }

    public Ball(Context context) {

        super(context);
    }

    public Ball(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public Ball(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(MainActivity mainActivity) {

        setMainActivity(mainActivity);
        this.setBackground(getResources().getDrawable(R.drawable.ball2));
        reset();
    }

    public void reset() {

        this.requestLayout();

        this.getLayoutParams().width = MainActivity.screenWidth * MainActivity.SIZE_PERCENTAGE_OF_SCREEN_WIDTH / 100;
        this.getLayoutParams().height = this.getLayoutParams().width;
        this.radius = this.getLayoutParams().height / 2;

        this.bounds = new Rect((int) this.getX(), (int) this.getY(), (int) (this.getX() + this.getWidth()),
                (int) (this.getY() + this.getHeight()));

        if (centerBall) {
            centerBall();
        }

        center = new Point((int) (this.getX() + radius),
                (int) (this.getY() + radius));

        updateCenterAndBounds();

        this.isFalling = false;
        this.speedX = 0;
        this.speedY = 0;

        this.postInvalidate();
    }

    public void centerBall() {
        this.setX(MainActivity.screenWidth / 2 - radius);
        this.setY(MainActivity.screenHeight - this.getLayoutParams().height);

    }

    private void updateCenterAndBounds() {
        center.x = (int) this.getX() + (int) radius;
        center.y = (int) this.getY() + (int) radius;

        bounds.set((int) this.getX(), (int) this.getY(),
                (int) (this.getX() + this.getWidth()), (int) (this.getY() + this.getHeight()));
    }

    public void update(float pitch, float roll) {

        if (!isFalling) {

            speedX = speedX + roll / MainActivity.DEACELERATION;
            speedY = speedY - pitch / MainActivity.DEACELERATION;

            newX = this.getX() + speedX;
            newY = this.getY() + speedY;
/*
newX = this.getX();
newY = this.getY();
*/
            checkObstacles();
            checkBorders();

            if (!cancelNormalRotation) {
                this.setRotation(calculateRotation(this.getRotation()));
            }

            this.setX(newX);
            this.setY(newY);
            this.postInvalidate();

            updateCenterAndBounds();

            checkIfEnteredHole();
        }
    }

    private void checkIfEnteredHole() {

        Point holeCenter = MainActivity.hole.getCenter();
        if (Math.abs(holeCenter.x - this.center.x) < radius &&
            Math.abs(holeCenter.y - this.center.y) < radius) {

            isFalling = true;

            this.animate().withEndAction(new Runnable() {
                @Override
                public void run() {

                    mainActivity.nextLevel();
                }
            });

            this.animate().setDuration(150);
            this.animate().rotation(rotateSeveralTimes());
            this.animate().scaleX(0.01f);
            this.animate().scaleY(0.01f);
            this.animate().xBy(speedX * 15);
            this.animate().yBy(speedY * 15);
        }
    }

    private float rotateSeveralTimes() {

        float rotation = this.getRotation();

        for (int i = 1; i <= 60; i++) {

            rotation = calculateRotation(rotation);
        }

        return rotation;
    }

    private void checkBorders() {

        if (newX <= 0) {
            newX = 0;
            speedX = -speedX / MainActivity.BORDERS_REBOUND_ABSORPTION;

        } else if (newX + this.getWidth() > MainActivity.screenWidth) {
            newX = MainActivity.screenWidth - this.getWidth();
            speedX = -speedX / MainActivity.BORDERS_REBOUND_ABSORPTION;
        }

        if (newY < 0) {
            newY = 0;
            speedY = -speedY / MainActivity.BORDERS_REBOUND_ABSORPTION;
        } else if (newY + this.getHeight() > MainActivity.screenHeight) {
            newY = MainActivity.screenHeight - this.getHeight();
            speedY = -speedY / MainActivity.BORDERS_REBOUND_ABSORPTION;
        }

    }

    private void checkObstacles() {

        ArrayList<Obstacle> obstacles = mainActivity.getObstacles();

        for (Obstacle obstacle : obstacles) {

            checkObstacle(obstacle);
        }
    }

    public static float calculateNewBallsAngle(double currentBallsAngle, float obstacleAngle) {
        float newBallsAngle = (float) (currentBallsAngle - obstacleAngle);
        if (newBallsAngle < 0) newBallsAngle = 359 + newBallsAngle;

        return newBallsAngle;
    }

    private void checkObstacle(Obstacle obstacle) {

        // ROTATING OBSTACLES HAVE A DIFFERENT TYPE OF COLLISION DETECTION
        if (obstacle instanceof MovingObstacle && ((MovingObstacle) obstacle).isRotating()) {

            MovingObstacle movingObstacle = (MovingObstacle) obstacle;

            // Check if the ball is inside the rectangle's rotation radius, and can collide with it
            // If not, we don't check for collisions

            float distanceFromBallsCenterToRectanglesCenter = findDistance(this.center.x, this.center.y,
                                                                            movingObstacle.center.x, movingObstacle.center.y);
            float obstacleRotationRadius = movingObstacle.getRotationRadius();

            // If the distance from the ball the rectangle's center is inferior to the sum of both radius, then it's inside the
            // colliding circle and we have to check for collisions
            if (distanceFromBallsCenterToRectanglesCenter < this.radius + obstacleRotationRadius) {

                double newBallsCenterX = Math.cos(Math.toRadians(-movingObstacle.getRotationAngle())) *
                                                    (this.center.x - movingObstacle.center.x) -
                        Math.sin(Math.toRadians(-movingObstacle.getRotationAngle())) *
                                    (this.center.y - movingObstacle.center.y) + movingObstacle.center.x;
                double newBallsCenterY  = Math.sin(Math.toRadians(-movingObstacle.getRotationAngle())) *
                                    (this.center.x - movingObstacle.center.x) +
                        Math.cos(Math.toRadians(-movingObstacle.getRotationAngle())) *
                                (this.center.y - movingObstacle.center.y) + movingObstacle.center.y;

                float rebound = 2;
                Rect obstacleBounds = movingObstacle.getBounds();
                double diffX, diffY;
                float reboundAccordingToCuadrantY, reboundAccordingToCuadrantX;

                reboundAccordingToCuadrantY = this.center.y < movingObstacle.center.y ? -1 : 1;
                reboundAccordingToCuadrantX = this.center.x < movingObstacle.center.x ? -1 : 1;

                // Lower bound
                if (obstacleBounds.contains((int) newBallsCenterX, (int) (newBallsCenterY - this.radius))) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle() - 90)) *
                                                    (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle() - 90)) *
                                                    (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());

                    obstacle.shake(false, (float) Math.abs(speedY));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {


                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;

                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;

                    }

                    Log.e("COLLISION!", "With BOTTOM");
                    // Upper bound
                } else if (obstacleBounds.contains((int) newBallsCenterX, (int) (newBallsCenterY + this.radius))) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle() + 90)) *
                                                    (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle() + 90)) *
                                                    (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());

                    obstacle.shake(false, (float) Math.abs(speedY));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {

                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;
                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;
                    }

                    Log.e("COLLISION!", "With TOP");
                    // Right bound
                } else if (obstacleBounds.contains((int) (newBallsCenterX - this.radius), (int) newBallsCenterY)) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle() + 180)) *
                                                    (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle() + 180)) *
                                                    (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());

                    obstacle.shake(false, (float) Math.abs(speedX));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {

                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;
                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;
                    }

                    Log.e("COLLISION!", "With RIGHT");
                    // Left bound
                } else if (obstacleBounds.contains((int) (newBallsCenterX + radius), (int) newBallsCenterY)) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle())) * movingObstacle.getAngularVelocity();
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle())) * movingObstacle.getAngularVelocity();

                    obstacle.shake(false, (float) Math.abs(speedX));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {

                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;
                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;
                    }

                    Log.e("COLLISION!", "With LEFT");

                    // Lower-left vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.left - newBallsCenterX), 2) +
                                        Math.pow(Math.abs(obstacleBounds.bottom - newBallsCenterY), 2))
                                < radius) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle() - 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle() - 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());

                    obstacle.shake(false, (float) Math.abs(speedY));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {


                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;

                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;

                    }

                    // Lower-right vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.right - newBallsCenterX), 2) +
                                        Math.pow(Math.abs(obstacleBounds.bottom - newBallsCenterY), 2))
                                < radius) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle() - 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle() - 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());

                    obstacle.shake(false, (float) Math.abs(speedY));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {


                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;

                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;

                    }

                    // Upper-left vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.left - newBallsCenterX), 2) +
                                        Math.pow(Math.abs(obstacleBounds.top - newBallsCenterY), 2))
                                < radius) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle() + 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle() + 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());

                    obstacle.shake(false, (float) Math.abs(speedY));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {

                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;
                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;
                    }


                    // Upper-right vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.right - newBallsCenterX), 2) +
                                        Math.pow(Math.abs(obstacleBounds.top - newBallsCenterY), 2))
                                < radius) {

                    diffY = Math.sin(Math.toRadians(movingObstacle.getRotationAngle() + 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());
                    diffX = Math.cos(Math.toRadians(movingObstacle.getRotationAngle() + 90)) *
                            (movingObstacle.getAngularVelocity() / movingObstacle.getReboundAbsorption());

                    obstacle.shake(false, (float) Math.abs(speedY));

                    if (movingObstacle.getMovement() == MovingObstacle.Movement.CLOCK_WISE_ROTATION) {

                        speedY = (float) (-diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (-diffX) + reboundAccordingToCuadrantX;
                    } else if (movingObstacle.getMovement() == MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION) {

                        speedY = (float) (diffY) + reboundAccordingToCuadrantY;
                        speedX = (float) (diffX) + reboundAccordingToCuadrantX;
                    }

                }
            }

        } else {
            // We only check for collisions if the ball is inside the obstacle surface, which is
            // a rectangle 8 pixels wider and taller than the obstacle

            // We only check collisions if the obstacle is not shaking from a previous collision
            Rect obstacleSurface = obstacle.getSurfaceRect();

            if (!obstacle.isShaking() && this.bounds.intersect(obstacleSurface)) {

                float rebound = 2;
                Rect obstacleBounds = obstacle.getBounds();

                // Lower bound
                if (obstacleBounds.contains(center.x, (int) this.getY())) {

                    newY = obstacleBounds.bottom + 1;
                    speedY = -speedY / obstacle.getReboundAbsorption();
                    obstacle.shake(false, Math.abs(speedY));
                    cancelNormalRotation = true;

                    // Upper bound
                } else if (obstacleBounds.contains(center.x, (int) (center.y + radius))) {

                    newY = obstacleBounds.top - radius * 2 - 1;
                    speedY = -speedY / obstacle.getReboundAbsorption();
                    obstacle.shake(false, Math.abs(speedY));
                    cancelNormalRotation = true;

                    // Right bound
                } else if (obstacleBounds.contains((int) this.getX(), center.y)) {

                    newX = obstacleBounds.right + 1;
                    speedX = -speedX / obstacle.getReboundAbsorption();
                    obstacle.shake(true, Math.abs(speedX));
                    cancelNormalRotation = true;

                    // Left bound
                } else if (obstacleBounds.contains((int) (center.x + radius), center.y)) {

                    newX = obstacleBounds.left - radius * 2 - 1;
                    speedX = -speedX / obstacle.getReboundAbsorption();
                    obstacle.shake(true, Math.abs(speedX));
                    cancelNormalRotation = true;


                    // Lower-left vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.left - center.x), 2) +
                                        Math.pow(Math.abs(obstacleBounds.bottom - center.y), 2))
                                < radius) {

                    newX = this.getX() - rebound;
                    newY = this.getY() + rebound;

                    speedX = -(Math.abs(speedX / Obstacle.REBOUND_ABSORPTION_ON_VERTICES));
                    speedY = Math.abs(speedY / Obstacle.REBOUND_ABSORPTION_ON_VERTICES);
                    obstacle.shake(false, Math.abs(speedY));

                    // Lower-right vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.right - center.x), 2) +
                                        Math.pow(Math.abs(obstacleBounds.bottom - center.y), 2))
                                < radius) {

                    newX = this.getX() + rebound;
                    newY = this.getY() + rebound;

                    speedX = Math.abs(speedX / Obstacle.REBOUND_ABSORPTION_ON_VERTICES);
                    speedY = Math.abs(speedY / Obstacle.REBOUND_ABSORPTION_ON_VERTICES);
                    obstacle.shake(false, Math.abs(speedY));

                    // Upper-left vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.left - center.x), 2) +
                                        Math.pow(Math.abs(obstacleBounds.top - center.y), 2))
                                < radius) {

                    newX = this.getX() - rebound;
                    newY = this.getY() - rebound;

                    speedX = -(Math.abs(speedX / Obstacle.REBOUND_ABSORPTION_ON_VERTICES));
                    speedY = -(Math.abs(speedY / Obstacle.REBOUND_ABSORPTION_ON_VERTICES));
                    obstacle.shake(false, Math.abs(speedY));

                    // Upper-right vertix
                } else if (
                        Math.sqrt(
                                Math.pow(Math.abs(obstacleBounds.right - center.x), 2) +
                                        Math.pow(Math.abs(obstacleBounds.top - center.y), 2))
                                < radius) {

                    newX = this.getX() + rebound;
                    newY = this.getY() - rebound;

                    speedX = Math.abs(speedX / Obstacle.REBOUND_ABSORPTION_ON_VERTICES);
                    speedY = -(Math.abs(speedY / Obstacle.REBOUND_ABSORPTION_ON_VERTICES));
                    obstacle.shake(false, Math.abs(speedY));
                }

                // Cancel normal rotation if we are on the surface of the obstacle
                float rotation = this.getRotation();

                // Lower bound
                if (obstacleSurface.contains(center.x, (int) this.getY())) {

                    rotation -= speedX / divisor;
                    cancelNormalRotation = true;

                    // Upper bound
                } else if (obstacleSurface.contains(center.x, (int) (center.y + radius))) {

                    rotation += speedX / divisor;
                    cancelNormalRotation = true;

                    // Right bound
                } else if (obstacleSurface.contains((int) this.getX(), center.y)) {

                    rotation += speedY / divisor;
                    cancelNormalRotation = true;

                    // Left bound
                } else if (obstacleSurface.contains((int) (center.x + radius), center.y)) {

                    rotation -= speedY / divisor;
                    cancelNormalRotation = true;

                } else {
                    cancelNormalRotation = false;
                }

                if (rotation < 0) {
                    rotation = 359;
                } else if (rotation >= 360) {
                    rotation = 0;
                }

                this.setRotation(rotation);
            }
        }
    }

    private boolean checkCollisionOnRotatingObstacle(MovingObstacle obstacle) {
        boolean collision = false;

        float obstacleAngleDegrees = obstacle.getRotation();
        float obstacleAngleRadians = (float) Math.toRadians(obstacleAngleDegrees);
        float obstacleCenterX = obstacle.getCenter().x;
        float obstacleCenterY = obstacle.getCenter().y;
        float ballX = this.getX();
        float ballY = this.getY();

        // Rotate circle's center point back
        double unrotatedCircleX = Math.cos(obstacleAngleRadians) * (ballX - obstacleCenterX) -
                Math.sin(obstacleAngleRadians) * (ballY - obstacleCenterY) + obstacleCenterX;
        double unrotatedCircleY  = Math.sin(obstacleAngleRadians) * (ballX - obstacleCenterX) +
                Math.cos(obstacleAngleRadians) * (ballY - obstacleCenterY) + obstacleCenterY;

// Closest point in the rectangle to the center of circle rotated backwards(unrotated)
        double closestX, closestY;

// Find the unrotated closest x point from center of unrotated circle
        if (unrotatedCircleX  < obstacle.x)
            closestX = obstacle.x;
        else if (unrotatedCircleX  > obstacle.x + obstacle.obstacleWidth)
            closestX = obstacle.x + obstacle.obstacleWidth;
        else
            closestX = unrotatedCircleX ;

// Find the unrotated closest y point from center of unrotated circle
        if (unrotatedCircleY < obstacle.y)
            closestY = obstacle.y;
        else if (unrotatedCircleY > obstacle.y + obstacle.obstacleHeight)
            closestY = obstacle.y + obstacle.obstacleHeight;
        else
            closestY = unrotatedCircleY;

// Determine collision

        float distance = findDistance(unrotatedCircleX , unrotatedCircleY, closestX, closestY);
        if (distance < this.radius)
            collision = true; // Collision
        else
            collision = false;

        return collision;
    }

    public float findDistance(double fromX, double fromY, double toX, double toY){
        double a = Math.abs(fromX - toX);
        double b = Math.abs(fromY - toY);

        return (float) Math.sqrt((a * a) + (b * b));
    }

    private float calculateRotation(float oldRotation) {
        float rotation = oldRotation;

        float verticalMiddle = MainActivity.screenWidth / 2;
        float horizontalMiddle = MainActivity.screenHeight / 2;

        float margin = 100;

        if (this.getY() < horizontalMiddle - margin) {

            rotation -= speedX / divisor;
        } else if (this.getY() > horizontalMiddle + margin){

            rotation += speedX / divisor;
        }

        if (this.getX() < verticalMiddle - margin) {

            rotation += speedY / divisor;

        } else if (this.getX() > verticalMiddle + margin){

            rotation -= speedY / divisor;
        }

        if (rotation < 0) {
            rotation = 359;
        } else if (rotation >= 360) {
            rotation = 0;
        }

        return rotation;
    }

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
        this.speedY = speedY;
    }

    public boolean isFalling() { return isFalling; }

    public void setFalling(boolean isFalling) { this.isFalling = isFalling; }

    public Point getCenter() { return center; }

    public void setMainActivity(MainActivity mainActivity) { this.mainActivity = mainActivity; }

    public float getRadius() { return radius; }

    public Rect getBounds() { return bounds; }
}
