package com.robotca.ControlApp.Core;

import org.ros.message.MessageListener;

import sensor_msgs.MagneticField;


/**
 * Created by Nicolas Acero.
 */

public class MagReceiver implements MessageListener<MagneticField> {
    MagneticField sensorData;

    @Override
    public void onNewMessage(MagneticField sensorData) {
        this.sensorData = sensorData;
    }

    public MagneticField getSensorData() {
        return sensorData;
    }
}
