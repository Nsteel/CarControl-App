package com.robotca.ControlApp.Core.Plans;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.ControlMode;
import com.robotca.ControlApp.Core.RobotController;

/**
 * ExplorationPlan
 */
public class Exploration extends RobotPlan {

    private final ControlApp controlApp;

    private static final String TAG = "ExplorationPlan";


    /**
     * Creates a ExplorationPlan for the specified ControlApp.
     * @param controlApp The ControlApp
     */
    public Exploration(ControlApp controlApp) {
        this.controlApp = controlApp;
    }

    /**
     * @return The ControlMode for this RobotPlan
     */
    @Override
    public ControlMode getControlMode() {
        return ControlMode.Exploration;
    }

    @Override
    protected void start(RobotController controller) throws Exception {
        //while(!isInterrupted())
            controller.setModeControl("Exploration");
    }
}
