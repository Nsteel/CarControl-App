package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ros.message.MessageListener;

import nav_msgs.Odometry;
import pses_basis.CarInfo;
import pses_basis.SensorData;

import com.robotca.ControlApp.Core.CarTelemetryWrapper;
import com.robotca.ControlApp.R;

/**
 * Simple fragment showing info about the sensor data.
 * @author Nicolas Acero
 */
public class TelemetryFragment extends SimpleFragment implements MessageListener<CarTelemetryWrapper> {

    @SuppressWarnings("unused")
    private static final String TAG = "TelemetryFragment";

    private View view;

    private TextView xView, yView, distanceView, vxView, vyView, speedView, axView, ayView, azView, gxView,
            gyView, gzView, yawView, rsfView, rslView, rsrView, batterySystemView, batteryMotorView;

    // The most recent Odometry
    private Odometry odometry;
    // The most recent SensorData
    private SensorData sensorData;
    // The most recent CarInfo
    private CarInfo carInfo;

    //Time since last update
    private long lastUpdate;
    private static final long UPDATE_DELAY = 100;

    double lastX;
    double lastY;
    double lastDrivenDistance;
    double lastVx;
    double lastVy;
    double lastSpeed;
    double lastAx;
    double lastAy;
    double lastAz;
    double lastGx;
    double lastGy;
    double lastGz;
    double lastYaw;
    double lastRsf;
    double lastRsl;
    double lastRsr;
    double lastBatterySystem;
    double lastBatteryMotor;

    // Updates this Fragments UI on the UI Thread
    private final UpdateUIRunnable UPDATE_UI_RUNNABLE = new UpdateUIRunnable();

    public TelemetryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_telemetry, null);
            xView = (TextView) view.findViewById(R.id.x_telemetry);
            yView = (TextView) view.findViewById(R.id.y_telemetry);
            distanceView = (TextView) view.findViewById(R.id.driven_distance_telemetry);
            vxView = (TextView) view.findViewById(R.id.vx_telemetry);
            vyView = (TextView) view.findViewById(R.id.vy_telemetry);
            speedView = (TextView) view.findViewById(R.id.speed_telemetry);
            axView = (TextView) view.findViewById(R.id.ax_telemetry);
            ayView = (TextView) view.findViewById(R.id.ay_telemetry);
            azView = (TextView) view.findViewById(R.id.az_telemetry);
            gxView = (TextView) view.findViewById(R.id.gx_telemetry);
            gyView = (TextView) view.findViewById(R.id.gy_telemetry);
            gzView = (TextView) view.findViewById(R.id.gz_telemetry);
            yawView = (TextView) view.findViewById(R.id.yaw_telemetry);
            rsfView = (TextView) view.findViewById(R.id.rsf_telemetry);
            rslView = (TextView) view.findViewById(R.id.rsl_telemetry);
            rsrView = (TextView) view.findViewById(R.id.rsr_telemetry);
            batterySystemView = (TextView) view.findViewById(R.id.battery_system_telemetry);
            batteryMotorView = (TextView) view.findViewById(R.id.battery_motor_telemetry);
            lastUpdate = 0;
            updateUI(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        return view;
    }

    /**
     * Updates this Fragment's speed display.
     */
    void updateUI(final double x, final double y, final double distance, final double vx, final double vy, final double speed,
                  final double ax, final  double ay, final double az, final double gx, final double gy, final double gz,
                  final double yaw, final double rsf, final double rsl, final double rsr, final double batterySystem, final double batteryMotor)
    {
        if (!isDetached() && (System.currentTimeMillis() - lastUpdate) > UPDATE_DELAY) {
            lastUpdate = System.currentTimeMillis();
            lastX = (int) (x * 100.0) / 100.0;
            lastY = (int) (y * 100.0) / 100.0;
            lastDrivenDistance = (int) (distance * 100.0) / 100.0;
            lastVx = (int) (vx * 100.0) / 100.0;
            lastVy = (int) (vy * 100.0) / 100.0;
            lastSpeed = (int) (speed * 100.0) / 100.0;
            lastAx = (int) (ax * 100.0) / 100.0;
            lastAy = (int) (ay * 100.0) / 100.0;
            lastAz = (int) (az * 100.0) / 100.0;
            lastGx = (int) (Math.toDegrees(gx) * 100.0) / 100.0;
            lastGy = (int) (Math.toDegrees(gy) * 100.0) / 100.0;
            lastGz = (int) (Math.toDegrees(gz) * 100.0) / 100.0;
            lastYaw = (int) (Math.toDegrees(yaw) * 100.0) / 100.0;
            lastRsf = (int) (rsf * 100.0) / 100.0;
            lastRsl = (int) (rsl * 100.0) / 100.0;
            lastRsr = (int) (rsr * 100.0) / 100.0;
            lastBatterySystem = (int) (batterySystem * 100.0) / 100.0;
            lastBatteryMotor = (int) (batteryMotor * 100.0) / 100.0;

            view.post(UPDATE_UI_RUNNABLE);
        }
    }

    @Override
    public void onNewMessage(CarTelemetryWrapper carTelemetryWrapper) {
        sensorData = carTelemetryWrapper.getSensorData();
        odometry = carTelemetryWrapper.getOdometry();
        carInfo = carTelemetryWrapper.getCarInfo();
        if(odometry != null && view != null && sensorData != null && carInfo != null) updateUI(odometry.getPose().getPose().getPosition().getX(),
                odometry.getPose().getPose().getPosition().getY(), carInfo.getDrivenDistance(), odometry.getTwist().getTwist().getLinear().getX(),
                odometry.getTwist().getTwist().getLinear().getY(), carInfo.getSpeed(), sensorData.getAccelerometerX(),
                sensorData.getAccelerometerY(), sensorData.getAccelerometerZ(), sensorData.getAngularVelocityX(), sensorData.getAngularVelocityY(),
                sensorData.getAngularVelocityZ(), carInfo.getYaw(), sensorData.getRangeSensorFront(), sensorData.getRangeSensorLeft(),
                sensorData.getRangeSensorRight(), sensorData.getSystemBatteryVoltage(), sensorData.getMotorBatteryVoltage());
    }

    /*
 * Runnable for refreshing the Telemetry's UI.
 */
    private class UpdateUIRunnable implements Runnable
    {
        /**
         * Starts executing the active part of the class' code.
         */
        @Override
        public void run() {

            if (isDetached())
                return;

            try {

                // Update x
                if (xView != null)
                    xView.setText(String.format((String) getText(R.string.x_telemetry_string), lastX));

                // Update y
                if (yView != null)
                    yView.setText(String.format((String) getText(R.string.y_telemetry_string), lastY));

                // Update driven_distance
                if (distanceView != null)
                    distanceView.setText(String.format((String) getText(R.string.driven_distance_telemetry_string), lastDrivenDistance));

                // Update vx
                if (vxView != null)
                    vxView.setText(String.format((String) getText(R.string.v_x_telemetry_string), lastVx));

                // Update vy
                if (vyView != null)
                    vyView.setText(String.format((String) getText(R.string.v_y_telemetry_string), lastVy));

                // Update speed
                if (speedView != null)
                    speedView.setText(String.format((String) getText(R.string.speed_telemetry_string), lastSpeed));

                // Update ax
                if (axView != null)
                    axView.setText(String.format((String) getText(R.string.a_x_telemetry_string), lastAx));

                // Update ay
                if (ayView != null)
                    ayView.setText(String.format((String) getText(R.string.a_y_telemetry_string), lastAy));

                // Update az
                if (azView != null)
                    azView.setText(String.format((String) getText(R.string.a_z_telemetry_string), lastAz));

                // Update gx
                if (gxView != null)
                    gxView.setText(String.format((String) getText(R.string.g_x_telemetry_string), lastGx));

                // Update gy
                if (ayView != null)
                    gyView.setText(String.format((String) getText(R.string.g_y_telemetry_string), lastGy));

                // Update gz
                if (gzView != null)
                    gzView.setText(String.format((String) getText(R.string.g_z_telemetry_string), lastGz));

                // Update yaw
                if (yawView != null)
                    yawView.setText(String.format((String) getText(R.string.yaw_telemetry_string), lastYaw));

                // Update rsf
                if (rsfView != null)
                    rsfView.setText(String.format((String) getText(R.string.rsf_telemetry_string), lastRsf));

                // Update rsl
                if (rslView != null)
                    rslView.setText(String.format((String) getText(R.string.rsl_telemetry_string), lastRsl));

                // Update rsr
                if (rsrView != null)
                    rsrView.setText(String.format((String) getText(R.string.rsr_telemetry_string), lastRsr));

                // Update battery_system
                if (batterySystemView != null)
                    batterySystemView.setText(String.format((String) getText(R.string.battery_system_telemetry_string), lastBatterySystem));

                // Update battery_motor
                if (batteryMotorView != null)
                    batteryMotorView.setText(String.format((String) getText(R.string.battery_motor_telemetry_string), lastBatteryMotor));

                // Update battery level
                //if (vxView != null)
                //  vxView.setText(String.format((String) getText(R.string.percent_string), 100));

                // Update turn rate
                //if (turnrateView != null)
                  //  turnrateView.setText(String.format((String) getText(R.string.turnrate_string), turnrate));

                // Update WIFI icons
                //if (wifiStrengthView != null) {
                  //  wifiStrengthView.setImageResource(WIFI_ICONS[lastWifiImage]);
                //}

            } catch (IllegalStateException e) {
                // Ignore
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
