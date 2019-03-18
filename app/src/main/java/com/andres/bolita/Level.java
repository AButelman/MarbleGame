package com.andres.bolita;

import android.content.Context;

import java.util.ArrayList;

public class Level {

    private static int NUMBER_OF_LEVELS;

    private Context context;

    int levelNumber;
    private Ball ball;
    private Hole hole;
    private ArrayList<Obstacle> obstacles;

    public Level(Context context, float ballX, float ballY, boolean centerBall, float holeX, float holeY) {

        this.context = context;

        NUMBER_OF_LEVELS++;
        this.levelNumber = NUMBER_OF_LEVELS;

        ball = new Ball(context, ballX, ballY, centerBall);
        hole = new Hole(context, holeX, holeY);

        obstacles = new ArrayList<Obstacle>();
    }

    public void addObstacle(Obstacle obstacle) {
        this.obstacles.add(obstacle);
    }

    public static int getNumberOfLevels() {
        return NUMBER_OF_LEVELS;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public Ball getBall() { return ball; }

    public Hole getHole() {
        return hole;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }
}
