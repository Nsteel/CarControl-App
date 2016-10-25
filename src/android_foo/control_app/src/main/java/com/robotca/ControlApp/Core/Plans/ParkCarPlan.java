package com.robotca.ControlApp.Core.Plans;

import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.Core.Utils;

import org.ros.rosjava_geometry.Vector3;

/**
 * ParkCarPlan
 *
 */
public class ParkCarPlan extends RobotPlan {

    private final ControlApp controlApp;

    private static final String TAG = "ParkCarPlan";

    /**
     * Creates a ParkCarPlan for the specified ControlApp.
     * @param controlApp The ControlApp
     */
    public ParkCarPlan(ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    @Override
    public ControlMode getControlMode() {
        return ControlMode.SimpleWaypoint;
    }

    @Override
    protected void start(RobotController controller) throws Exception {

    }
}
