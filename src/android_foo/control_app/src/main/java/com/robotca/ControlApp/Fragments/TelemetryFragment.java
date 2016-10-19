package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ros.message.MessageListener;

import nav_msgs.Odometry;
import pses_basis.SensorData;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.OdomSensorWrapper;
import com.robotca.ControlApp.Core.RobotController;
import com.robotca.ControlApp.R;

/**
 * Simple fragment showing info about the sensor data.
 * @author Nicolas Acero
 */
public class TelemetryFragment extends SimpleFragment implements MessageListener<OdomSensorWrapper> {

    @SuppressWarnings("unused")
    private static final String TAG = "TelemetryFragment";

    private View view;

    private TextView xView, yView, distanceView, vxView, vyView, speedView, axView, ayView, azView, gxView,
            gyView, gzView, yawView, rsfView, rslView, rsrView, batterySystemView, batteryMotorView;

    // The most recent Odometry
    private Odometry odometry;
    // The most recent sensorData
    private SensorData sensorData;
    // The robot controller
    private RobotController controller;

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
            updateUI(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        // Get robot controller
        try {
            controller = ((ControlApp) getActivity()).getRobotController();
        }
        catch(Exception ignore){
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
        if (!isDetached()) {
            lastX = x;
            lastY = y;
            lastDrivenDistance = distance;
            lastVx = vx;
            lastVy = vy;
            lastSpeed = speed;
            lastAx = ax;
            lastAy = ay;
            lastAz = az;
            lastGx = gx;
            lastGy = gy;
            lastGz = gz;
            lastYaw = yaw;
            lastRsf = rsf;
            lastRsl = rsl;
            lastRsr = rsr;
            lastBatterySystem = batterySystem;
            lastBatteryMotor = batteryMotor;

            view.post(UPDATE_UI_RUNNABLE);
        }
    }

    @Override
    public void onNewMessage(OdomSensorWrapper odomSensorWrapper) {
        sensorData = odomSensorWrapper.getSensorData();
        odometry = odomSensorWrapper.getOdometry();
        if(odometry != null && view != null && sensorData != null) updateUI(odometry.getPose().getPose().getPosition().getX(),
                odometry.getPose().getPose().getPosition().getY(), odometry.getPose().getPose().getPosition().getZ(), odometry.getTwist().getTwist().getLinear().getX(),
                odometry.getTwist().getTwist().getLinear().getY(), odometry.getTwist().getTwist().getLinear().getZ(), sensorData.getAccelerometerX(),
                sensorData.getAccelerometerY(), sensorData.getAccelerometerZ(), sensorData.getAngularVelocityX(), sensorData.getAngularVelocityY(),
                sensorData.getAngularVelocityZ(), controller.getHeading(), sensorData.getRangeSensorFront(), sensorData.getRangeSensorLeft(),
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
                double x = (int) (lastX * 100.0) / 100.0;
                double y = (int) (lastY * 100.0) / 100.0;
                double distance = (int) (lastDrivenDistance * 100.0) / 100.0;
                double vx = (int) (lastVx * 100.0) / 100.0;
                double vy = (int) (lastVy * 100.0) / 100.0;
                double speed = (int) (lastSpeed * 100.0) / 100.0;
                double ax = (int) (lastAx * 100.0) / 100.0;
                double ay = (int) (lastAy * 100.0) / 100.0;
                double az = (int) (lastAz * 100.0) / 100.0;
                double gx = (int) (Math.toDegrees(lastGx) * 100.0) / 100.0;
                double gy = (int) (Math.toDegrees(lastGy) * 100.0) / 100.0;
                double gz = (int) (Math.toDegrees(lastGz) * 100.0) / 100.0;
                double yaw = (int) (Math.toDegrees(lastYaw) * 100.0) / 100.0;
                double rsf = (int) (lastRsf * 100.0) / 100.0;
                double rsl = (int) (lastRsl * 100.0) / 100.0;
                double rsr = (int) (lastRsr * 100.0) / 100.0;
                double batterySystem = (int) (lastBatterySystem * 100.0) / 100.0;
                double batteryMotor = (int) (lastBatteryMotor * 100.0) / 100.0;

                // Update x
                if (xView != null)
                    xView.setText(String.format((String) getText(R.string.x_telemetry_string), x));

                // Update y
                if (yView != null)
                    yView.setText(String.format((String) getText(R.string.y_telemetry_string), y));

                // Update driven_distance
                if (distanceView != null)
                    distanceView.setText(String.format((String) getText(R.string.driven_distance_telemetry_string), distance));

                // Update vx
                if (vxView != null)
                    vxView.setText(String.format((String) getText(R.string.v_x_telemetry_string), vx));

                // Update vy
                if (vyView != null)
                    vyView.setText(String.format((String) getText(R.string.v_y_telemetry_string), vy));

                // Update speed
                if (speedView != null)
                    speedView.setText(String.format((String) getText(R.string.speed_telemetry_string), speed));

                // Update ax
                if (axView != null)
                    axView.setText(String.format((String) getText(R.string.a_x_telemetry_string), ax));

                // Update ay
                if (ayView != null)
                    ayView.setText(String.format((String) getText(R.string.a_y_telemetry_string), ay));

                // Update az
                if (azView != null)
                    azView.setText(String.format((String) getText(R.string.a_z_telemetry_string), az));

                // Update gx
                if (gxView != null)
                    gxView.setText(String.format((String) getText(R.string.g_x_telemetry_string), gx));

                // Update gy
                if (ayView != null)
                    gyView.setText(String.format((String) getText(R.string.g_y_telemetry_string), gy));

                // Update gz
                if (gzView != null)
                    gzView.setText(String.format((String) getText(R.string.g_z_telemetry_string), gz));

                // Update yaw
                if (yawView != null)
                    yawView.setText(String.format((String) getText(R.string.yaw_telemetry_string), yaw));

                // Update rsf
                if (rsfView != null)
                    rsfView.setText(String.format((String) getText(R.string.rsf_telemetry_string), rsf));

                // Update rsl
                if (rslView != null)
                    rslView.setText(String.format((String) getText(R.string.rsl_telemetry_string), rsl));

                // Update rsr
                if (rsrView != null)
                    rsrView.setText(String.format((String) getText(R.string.rsr_telemetry_string), rsr));

                // Update battery_system
                if (batterySystemView != null)
                    batterySystemView.setText(String.format((String) getText(R.string.battery_system_telemetry_string), batterySystem));

                // Update battery_motor
                if (batteryMotorView != null)
                    batteryMotorView.setText(String.format((String) getText(R.string.battery_motor_telemetry_string), batteryMotor));

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
