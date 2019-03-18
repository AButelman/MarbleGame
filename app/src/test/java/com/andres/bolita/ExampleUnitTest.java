package com.andres.bolita;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(2, 1 + 1);
    }

    @Test
    public void checkNewAngle() {

        float newBallsAngle = Ball.calculateNewBallsAngle(90, 180);
        assertEquals(-3232, newBallsAngle);
    }
}