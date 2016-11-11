package com.robotca.ControlApp.Core.Plans;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;

/**
 * LaneDetectionPlan
 */
public class LaneDetection extends RobotPlan {

    private final ControlApp controlApp;

    private static final String TAG = "LaneDetectionPlan";


    /**
     * Creates a LaneDetectionPlan for the specified ControlApp.
     * @param controlApp The ControlApp
     */
    public LaneDetection(ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    @Override
    public ControlMode getControlMode() {
        return ControlMode.LaneDetection;
    }

    @Override
    protected void start(RobotController controller) throws Exception {
        //while(!isInterrupted())
            controller.setModeControl("Lane Detection");
    }
}
