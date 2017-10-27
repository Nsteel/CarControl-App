package com.robotca.ControlApp.Core;


import nav_msgs.Odometry;
import sensor_msgs.Range;
import sensor_msgs.Imu;
import sensor_msgs.MagneticField;
import sensor_msgs.BatteryState;

/**
 * Created by Nicolas Acero on 17.10.16.
 */

public class CarTelemetryWrapper {

    // The most recent values measured by the ultrasonic sensors
    private Range usLeft;
    private Range usRight;
    private Range usFront;

    // The most recent IMU message
    private Imu imu;

    // The most recent MagneticField message
    private MagneticField mag;

    // The most recent voltage measurements of the batteries
    private BatteryState vdBat;
    private BatteryState vsBat;

    // The most recent Odometry
    private Odometry odometry;

    public Range getUsLeft() {
        return usLeft;
    }

    public void setUsLeft(Range usLeft) {
        this.usLeft = usLeft;
    }

    public Range getUsRight() {
        return usRight;
    }

    public void setUsRight(Range usRight) {
        this.usRight = usRight;
    }

    public Range getUsFront() {
        return usFront;
    }

    public void setUsFront(Range usFront) {
        this.usFront = usFront;
    }

    public Imu getImu() {
        return imu;
    }

    public void setImu(Imu imu) {
        this.imu = imu;
    }

    public MagneticField getMag() {
        return mag;
    }

    public void setMag(MagneticField mag) {
        this.mag = mag;
    }

    public BatteryState getVdBat() {
        return vdBat;
    }

    public void setVdBat(BatteryState vdBat) {
        this.vdBat = vdBat;
    }

    public BatteryState getVsBat() {
        return vsBat;
    }

    public void setVsBat(BatteryState vsBat) {
        this.vsBat = vsBat;
    }

    public Odometry getOdometry() {
        return odometry;
    }

    public void setOdometry(Odometry odometry) {

        this.odometry = odometry;
    }
}
