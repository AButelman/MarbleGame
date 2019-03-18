package com.andres.bolita;

import android.content.Context;

import java.util.ArrayList;

public class LevelFactory {

    private Context context;
    private ArrayList<Level> levels;

    public LevelFactory(Context context) {
        this.context = context;
        levels = generateLevels();
    }

    public Level getNextLevel() {
        Level nextLevel = null;

        if (levels.size() > 0) {
            nextLevel = levels.remove(0);
        }

        return nextLevel;
    }

    public ArrayList<Level> generateLevels() {

        ArrayList<Level> levels = new ArrayList<Level>();

        Level level1 = new Level(context, 700, 1800, false,0, 0);
        level1.addObstacle(new MovingObstacle(context, 200, 800, 700, 100, Obstacle.ObstacleTypes.WOOD_BAR_HORIZONTAL,
                            MovingObstacle.Movement.COUNTER_CLOCK_WISE_ROTATION, 5f, 0.5f));

        Level level2 = new Level(context, 400, 900, false, 100, 100);
        level2.addObstacle(new Obstacle(context, 100, 400, 100, 300, Obstacle.ObstacleTypes.ERASER_VERTICAL));

        Level level3 = new Level(context, 0, 0, true, 200, 500);
        level3.addObstacle(new Obstacle(context, 500, 800, 200, 80, Obstacle.ObstacleTypes.WOOD_BAR_HORIZONTAL));
        level3.addObstacle(new Obstacle(context, 500, 1000, 200, 100, Obstacle.ObstacleTypes.ERASER_HORIZONTAL));

        Level level4 = new Level(context, 600, 1000, false, 300, 100);
        level4.addObstacle(new MovingObstacle(context,100, 900, 300, 90,
                Obstacle.ObstacleTypes.ERASER_HORIZONTAL, MovingObstacle.Movement.DOWN, 10f, 0));
        level4.addObstacle(new MovingObstacle(context, 700, 120, 40, 500,
                Obstacle.ObstacleTypes.WOOD_BAR_VERTICAL, MovingObstacle.Movement.CLOCK_WISE_ROTATION, 5f, 1.5f));

        levels.add(level1);
        levels.add(level2);
        levels.add(level3);
        levels.add(level4);

        return levels;
    }
}
