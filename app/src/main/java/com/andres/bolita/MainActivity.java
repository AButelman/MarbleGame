package com.andres.bolita;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    private static final float TRESHOLD = 0.05f;

    public static final int SIZE_PERCENTAGE_OF_SCREEN_WIDTH = 10;
    public static final float DEACELERATION = 13f;
    public static final float BORDERS_REBOUND_ABSORPTION = 2.5f;

    public static int screenWidth, screenHeight;

    private AbsoluteLayout layout;

    private LevelFactory levelFactory;
    private Level level;

    public static Ball ball;
    public static Hole hole;

    public static ArrayList<Obstacle> obstacles;

    private TextView sensorInfo;
    private SensorManager sensorManager;
    private Sensor magneticField, accelerometer;

    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];

    private float pitch, roll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getDisplayDimentions();

        layout = (AbsoluteLayout) findViewById(R.id.layout);

        // ball = (Ball) findViewById(R.id.ball);

        levelFactory = new LevelFactory(this);
        setNextLevel();

        initializeSensors();
    }

    private void setNextLevel() {

        level = levelFactory.getNextLevel();

        if (level != null) {

            ball = level.getBall();
            layout.addView(ball);
            ball.initialize(this);

            // WE HAVE TO INITIALIZE THE HOLES BEFORE THE OBSTACLES
            hole = level.getHole();
            layout.addView(hole, 1);

            obstacles = level.getObstacles();
            addObstaclesToLayout();
        } else {
            // IF THERE IS NOT A NEXT LEVEL, WE FINISH THE GAME (ADD AN END SCREEN)

            finish();
        }

    }

    public void nextLevel() {

        ball.animate().setDuration(10);
        ball.animate().scaleX(1f);
        ball.animate().scaleY(1f);
        ball.reset();

        layout.removeView(hole);
        layout.removeView(ball);
        removeObstaclesFromLayout();
        setNextLevel();
    }

    private void removeObstaclesFromLayout() {
        for (Obstacle obstacle : obstacles) {
            layout.removeView(obstacle);
        }
    }

    private void addObstaclesToLayout() {
        for (Obstacle obstacle : obstacles) {
            layout.addView(obstacle);

            if (obstacle instanceof MovingObstacle) {
                MovingObstacle movingObstacle = (MovingObstacle) obstacle;
                movingObstacle.startAnimation();
            }
        }
    }

    private void getDisplayDimentions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.e("App", "No hay acelerómetro.");
        }

        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Log.e("App", "No hay sensor magnético.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                return;
        }

        float[] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData);

        float orientationValues[] = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        }

        pitch = orientationValues[1];
        roll = orientationValues[2];

        if (Math.abs(pitch) < TRESHOLD) pitch = 0;
        if (Math.abs(roll) < TRESHOLD) roll = 0;

        ball.update(pitch, roll);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public ArrayList<Obstacle> getObstacles() { return obstacles; }
}
