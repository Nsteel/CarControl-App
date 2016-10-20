package com.robotca.ControlApp.Core;


import nav_msgs.Odometry;
import pses_basis.CarInfo;
import pses_basis.SensorData;

/**
 * Created by Nicolas Acero on 17.10.16.
 */

public class CarTelemetryWrapper {

    // The most recent Sensor data
    private SensorData sensorData;
    // The most recent Odometry
    private Odometry odometry;
    // The most recent carInfo;
    private CarInfo carInfo;

    public CarInfo getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(CarInfo carInfo) {
        this.carInfo = carInfo;
    }
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
