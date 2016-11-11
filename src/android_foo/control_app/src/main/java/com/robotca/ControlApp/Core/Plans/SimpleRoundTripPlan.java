package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;

import org.ros.rosjava_geometry.Vector3;

/**
 * SimpleRoundTripPlan
 */
public class SimpleRoundTripPlan extends RobotPlan {

    private final ControlApp controlApp;

    private static final String TAG = "SimpleRoundTripPlan";


    /**
     * Creates a SimpleRoundTripPlan for the specified ControlApp.
     * @param controlApp The ControlApp
     */
    public SimpleRoundTripPlan(ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    @Override
    public ControlMode getControlMode() {
        return ControlMode.RoundTripWithoutObstacles;
    }

    @Override
    protected void start(RobotController controller) throws Exception {
        //while(!isInterrupted())
            controller.setModeControl("Follow Wall");
    }
}
