package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;

import org.ros.rosjava_geometry.Vector3;

/**
 * RoundTripWithObstaclesPlan
 *
 */
public class RoundTripWithObstaclesPlan extends RobotPlan {

    private final ControlApp controlApp;

    private static final String TAG = "RoundTripWithObstaclesPlan";

    /**
     * Creates a RoundTripWithObstaclesPlan for the specified ControlApp.
     * @param controlApp The ControlApp
     */
    public RoundTripWithObstaclesPlan(ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    @Override
    public ControlMode getControlMode() {
        return ControlMode.RoundTripWithObstacles;
    }

    @Override
    protected void start(RobotController controller) throws Exception {
        //while(!isInterrupted())
            controller.setModeControl("Roundtrip w. Obstacles");
    }
}
