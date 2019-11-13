/*
 * Copyright (c) 2019 Vulcan Robotics FTC Team 8375. All Rights Reserved.
 */

package org.firstinspires.ftc.team8375.Pathfinding;

public class BoardArray {
    private static int[][] array = {
            {0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0},
            {0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0},
            {1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1},
            {1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1},
            {1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1},
            {0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0},
            {0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0},
    };

    public static int getArray(int i, int k) {
        return array[i][k];
    }

}
