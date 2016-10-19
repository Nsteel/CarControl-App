package com.robotca.ControlApp.Core;


import nav_msgs.Odometry;
import pses_basis.SensorData;

/**
 * Created by Nicolas Acero on 17.10.16.
 */

public class OdomSensorWrapper{

    // The most recent Sensor data
    private SensorData sensorData;
    // The most recent Odometry
    private Odometry odometry;

    public Odometry getOdometry() {
        return odometry;
    }

    public void setOdometry(Odometry odometry) {
        this.odometry = odometry;
    }

    public SensorData getSensorData() {
        return sensorData;
    }

    public void setSensorData(SensorData sensorData) {
        this.sensorData = sensorData;
    }
}
