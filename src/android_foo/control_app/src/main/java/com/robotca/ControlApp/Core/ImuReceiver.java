package com.robotca.ControlApp.Core;

import org.ros.message.MessageListener;

import sensor_msgs.Imu;


/**
 * Created by Nicolas Acero.
 */

public class ImuReceiver implements MessageListener<Imu> {
    Imu sensorData;

    @Override
    public void onNewMessage(Imu sensorData) {
        this.sensorData = sensorData;
    }

    public Imu getSensorData() {
        return sensorData;
    }
}
