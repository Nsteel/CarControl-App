package com.robotca.ControlApp.Core;

import org.ros.message.MessageListener;

import pses_basis.SensorData;

/**
 * Created by Nicolas Acero.
 */

public class SensorDataReceiver implements MessageListener<SensorData> {
    SensorData sensorData;

    @Override
    public void onNewMessage(SensorData sensorData) {
        this.sensorData = sensorData;
    }

    public SensorData getSensorData() {
        return sensorData;
    }
}
