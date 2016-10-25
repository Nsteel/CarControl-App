package com.robotca.ControlApp.Core;

import com.robotca.ControlApp.ControlApp;
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
    Joystick (true), // Joystick control
    SimpleWaypoint (false), // SimpleWaypoint control
    RoundTripWithoutObstacles (false), // Round trip around the corridor without obstacles
    RoundTripWithObstacles (false), // Round trip around the corridor with obstacles
    ParkCar(false); // Park the car

    // Whether the user directly controls the Robot in this mode
    public final boolean USER_CONTROLLED;

    /**
     * Creates a ControlMode.
     * @param userControlled Whether the user controls the Robot directly in this mode.
     */
    ControlMode(boolean userControlled)
    {
        USER_CONTROLLED = userControlled;
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
            case RoundTripWithoutObstacles: plan = new SimpleRoundTripPlan(controlApp); break;
            case RoundTripWithObstacles: plan = new RoundTripWithObstaclesPlan(controlApp); break;
            case ParkCar: plan = new ParkCarPlan(controlApp); break;
            default: plan = null; break;
        }

        return plan;
    }
}
