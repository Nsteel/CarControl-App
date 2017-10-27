package com.robotca.ControlApp.Core;

import org.ros.message.MessageListener;

import sensor_msgs.BatteryState;


/**
 * Created by Nicolas Acero.
 */

public class BatteryStateReceiver implements MessageListener<BatteryState> {
    BatteryState sensorData;

    @Override
    public void onNewMessage(BatteryState sensorData) {
        this.sensorData = sensorData;
    }

    public BatteryState getSensorData() {
        return sensorData;
    }
}
