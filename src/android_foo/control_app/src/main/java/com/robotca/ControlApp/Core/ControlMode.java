package com.robotca.ControlApp.Core;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.Plans.Exploration;
import com.robotca.ControlApp.Core.Plans.LaneDetection;
import com.robotca.ControlApp.Core.Plans.ParkCarPlan;
import com.robotca.ControlApp.Core.Plans.RobotPlan;
import com.robotca.ControlApp.Core.Plans.RoundTripWithObstaclesPlan;
import com.robotca.ControlApp.Core.Plans.SimpleRoundTripPlan;

/**
 * Enum for different ways to control the Robot.
 *
 * Created by Michael Brunson on 2/12/16.
 */
public enum ControlMode {
    Joystick (true, "Remote Control"), // Joystick control
    RoundTripWithoutObstacles (true, "Follow Wall"), // Round trip around the corridor without obstacles
    RoundTripWithObstacles (true, "Roundtrip w. Obstacles"), // Round trip around the corridor with obstacles
    ParkCar(true, "Park Car"), // Park the car
    LaneDetection(true, "Lane Detection"), // Lane Detection & lane following
    Exploration(true, "Exploration"); // Autonomous exploration


    // Whether the user directly controls the Robot in this mode
    public final boolean USER_CONTROLLED;

    public final String NAME;

    /**
     * Creates a ControlMode.
     * @param userControlled Whether the user controls the Robot directly in this mode.
     */
    ControlMode(boolean userControlled, String name)
    {
        USER_CONTROLLED = userControlled;
        NAME = name;
    }

    /**
     * Creates a RobotPlan for the specified ControlMode if one exists.
     * @param controlApp The ControlApp
     * @param controlMode The ControlMode
     * @return A RobotPlan for the ControlMode or null if none exists
     */
    public static RobotPlan getRobotPlan(ControlApp controlApp, ControlMode controlMode) {

        RobotPlan plan;

        switch (controlMode) {
            /*case RoundTripWithoutObstacles: plan = new SimpleRoundTripPlan(controlApp); break;
            case RoundTripWithObstacles: plan = new RoundTripWithObstaclesPlan(controlApp); break;
            case ParkCar: plan = new ParkCarPlan(controlApp); break;
            case LaneDetection: plan = new LaneDetection(controlApp); break;
            case Exploration: plan = new Exploration(controlApp); break;*/
            default: plan = null; break;
        }

        return plan;
    }
}
