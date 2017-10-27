package com.robotca.ControlApp.Core;

import org.ros.message.MessageListener;
import sensor_msgs.Range;


/**
 * Created by Nicolas Acero.
 */

public class UltraSonicSensorReceiver implements MessageListener<Range> {
    Range sensorData;

    @Override
    public void onNewMessage(Range sensorData) {
        this.sensorData = sensorData;
    }

    public Range getSensorData() {
        return sensorData;
    }
}
