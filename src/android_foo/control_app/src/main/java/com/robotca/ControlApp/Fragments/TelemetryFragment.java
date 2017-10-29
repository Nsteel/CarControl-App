package com.robotca.ControlApp.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ros.internal.message.RawMessage;
import org.ros.message.MessageListener;

import geometry_msgs.Quaternion;

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

    private TextView xView, yView, vxView, vyView, speedView, axView, ayView, azView, gxView,
            gyView, gzView, yawView, mxView, myView, rsfView, rslView, rsrView, batterySystemView, batteryMotorView;

    //Time since last update
    private long lastUpdate;
    private static final long UPDATE_DELAY = 100;

    double lastX;
    double lastY;
    double lastVx;
    double lastVy;
    double lastSpeed;
    double lastAx;
    double lastAy;
    double lastAz;
    double lastGx;
    double lastGy;
    double lastGz;
    double lastMx;
    double lastMy;
    double lastYaw = Double.NaN;
    double lastRsf;
    double lastRsl;
    double lastRsr;
    double lastBatterySystem;
    double lastBatteryMotor;
    Quaternion lastOrientation;

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
            view = inflater.inflate(R.layout.fragment_telemetry, container, false);
            xView = (TextView) view.findViewById(R.id.x_telemetry);
            yView = (TextView) view.findViewById(R.id.y_telemetry);
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
            mxView = (TextView) view.findViewById(R.id.mx_telemetry);
            myView = (TextView) view.findViewById(R.id.my_telemetry);
            rsfView = (TextView) view.findViewById(R.id.rsf_telemetry);
            rslView = (TextView) view.findViewById(R.id.rsl_telemetry);
            rsrView = (TextView) view.findViewById(R.id.rsr_telemetry);
            batterySystemView = (TextView) view.findViewById(R.id.battery_system_telemetry);
            batteryMotorView = (TextView) view.findViewById(R.id.battery_motor_telemetry);
            lastUpdate = 0;
            updateUI();
        }
        return view;
    }

    /**
     * Updates this Fragment's speed display.
     */
    void updateUI()
    {
        if (view != null && !isDetached() && (System.currentTimeMillis() - lastUpdate) > UPDATE_DELAY) {
            lastUpdate = System.currentTimeMillis();
            lastX = (int) (lastX * 100.0) / 100.0;
            lastY = (int) (lastY * 100.0) / 100.0;
            lastVx = (int) (lastVx * 100.0) / 100.0;
            lastVy = (int) (lastVy * 100.0) / 100.0;
            lastSpeed = (int) (Math.sqrt(lastVx*lastVx + lastVy*lastVy) * 100.0) / 100.0;
            lastAx = (int) (lastAx * 100.0) / 100.0;
            lastAy = (int) (lastAy * 100.0) / 100.0;
            lastAz = (int) (lastAz * 100.0) / 100.0;
            lastGx = (int) (Math.toDegrees(lastGx) * 100.0) / 100.0;
            lastGy = (int) (Math.toDegrees(lastGy) * 100.0) / 100.0;
            lastGz = (int) (Math.toDegrees(lastGz) * 100.0) / 100.0;
            lastMx = lastMx * 1000000000.0; // µT conversion
            lastMx = (int) (lastMx * 100.0) / 100.0;
            lastMy = lastMy * 1000000000.0; // µT conversion
            lastMy = (int) (lastMy * 100.0) / 100.0;
            lastRsf = (int) (lastRsf* 100.0) / 100.0;
            lastRsl = (int) (lastRsl * 100.0) / 100.0;
            lastRsr = (int) (lastRsr * 100.0) / 100.0;
            lastBatterySystem = (int) (lastBatterySystem * 100.0) / 100.0;
            lastBatteryMotor = (int) (lastBatteryMotor * 100.0) / 100.0;

            // yaw (z-axis rotation)
            lastYaw = lastOrientation != null? Math.atan2(2.0 * (lastOrientation.getW() * lastOrientation.getZ() + lastOrientation.getX() * lastOrientation.getY()),
                    1.0 - 2.0 * (lastOrientation.getY() * lastOrientation.getY() + lastOrientation.getZ() * lastOrientation.getZ())) : lastYaw;
            lastYaw = (int) (Math.toDegrees(lastYaw) * 100.0) / 100.0;

            view.post(UPDATE_UI_RUNNABLE);
        }
    }

    @Override
    public void onNewMessage(CarTelemetryWrapper carTelemetryWrapper) {
        lastRsl = carTelemetryWrapper.getUsLeft() != null? carTelemetryWrapper.getUsLeft().getRange() : lastRsl;
        lastRsr = carTelemetryWrapper.getUsRight() != null? carTelemetryWrapper.getUsRight().getRange() : lastRsr;
        lastRsf = carTelemetryWrapper.getUsFront() != null? carTelemetryWrapper.getUsFront().getRange() : lastRsf;
        if (carTelemetryWrapper.getImu() != null) {
            lastAx = carTelemetryWrapper.getImu().getLinearAcceleration().getX();
            lastAy = carTelemetryWrapper.getImu().getLinearAcceleration().getY();
            lastAz = carTelemetryWrapper.getImu().getLinearAcceleration().getZ();
            lastGx = carTelemetryWrapper.getImu().getAngularVelocity().getX();
            lastGy = carTelemetryWrapper.getImu().getAngularVelocity().getY();
            lastGz = carTelemetryWrapper.getImu().getAngularVelocity().getZ();
        }

        if(carTelemetryWrapper.getOdometry() != null) {
            lastX = carTelemetryWrapper.getOdometry().getPose().getPose().getPosition().getX();
            lastY = carTelemetryWrapper.getOdometry().getPose().getPose().getPosition().getY();
            lastVx = carTelemetryWrapper.getOdometry().getTwist().getTwist().getLinear().getX();
            lastVy = carTelemetryWrapper.getOdometry().getTwist().getTwist().getLinear().getY();
            lastOrientation = carTelemetryWrapper.getOdometry().getPose().getPose().getOrientation();
        }

        if(carTelemetryWrapper.getMag() != null) {
            lastMx = carTelemetryWrapper.getMag().getMagneticField().getX();
            lastMy = carTelemetryWrapper.getMag().getMagneticField().getY();
        }

        lastBatteryMotor = carTelemetryWrapper.getVdBat() != null? carTelemetryWrapper.getVdBat().getVoltage() : lastBatteryMotor;
        lastBatterySystem = carTelemetryWrapper.getVsBat() != null? carTelemetryWrapper.getVsBat().getVoltage() : lastBatterySystem;

        updateUI();
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
                //if (distanceView != null)
                  //  distanceView.setText(String.format((String) getText(R.string.driven_distance_telemetry_string), lastDrivenDistance));

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

                // Update Magnetometer x
                if (mxView != null)
                    mxView.setText(String.format((String) getText(R.string.mx_telemetry_string), lastMx));

                // Update Magnetometer y
                if (myView != null)
                    myView.setText(String.format((String) getText(R.string.my_telemetry_string), lastMy));

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
