package com.robotca.ControlApp.Dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.R;


/**
 * Dialog for adding or editing a Robot.
 * <p/>
 * Created by Michael Brunson on 1/23/16.
 */
public class AddEditRobotDialogFragment extends DialogFragment {

    /**
     * Bundle key for position
     */
    public static final String POSITION_KEY = "POSITION_KEY";

    // Temporary RobotInfo
    private RobotInfo mInfo = new RobotInfo();

    // Use this instance of the interface to deliver action events
    private DialogListener mListener;

    // EditTexts for editing the RobotInfo
    private EditText mNameEditTextView;
    private EditText mMasterUriEditTextView;
    private View mAdvancedOptionsView;
    private EditText mMotorTopicEditTextView;
    private EditText mLaserScanTopicEditTextView;
    private EditText mCameraTopicEditTextView;
    private EditText mSteeringTopicEditTextView;
    private EditText mUSLeftTopicEditTextView;
    private EditText mUSRightTopicEditTextView;
    private EditText mUSFrontTopicEditTextView;
    private EditText mIMUTopicEditTextView;
    private EditText mMAGTopicEditTextView;
    private EditText mVdBatTopicEditTextView;
    private EditText mVsBatTopicEditTextView;
    private EditText mOdometryTopicEditTextView;
    private EditText mSensorDataTopicEditTextView;
    private EditText mCarInfoTopicEditTextView;
    private EditText mModeControlTopicEditTextView;
    private CheckBox mReverseLaserScanCheckBox;
    private CheckBox mInvertXAxisCheckBox;
    private CheckBox mInvertYAxisCheckBox;
    private CheckBox mInvertAngularVelocityCheckBox;


    // Position of the RobotInfo in the list of RobotInfos
    private int mPosition = -1;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            mPosition = args.getInt(POSITION_KEY, -1);
            mInfo.load(args);
        }
    }

    // Override the Fragment.onAttach() method to instantiate the DialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DialogListener so we can send events to the host
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString()  + " must implement DialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_add_robot, null);
        mNameEditTextView = (EditText) v.findViewById(R.id.robot_name_edit_text);
        mMasterUriEditTextView = (EditText) v.findViewById(R.id.master_uri_edit_view);

        CheckBox mAdvancedOptionsCheckbox = (CheckBox) v.findViewById(R.id.advanced_options_checkbox_view);
        mAdvancedOptionsView = v.findViewById(R.id.advanced_options_view);
        mMotorTopicEditTextView = (EditText) v.findViewById(R.id.joystick_topic_edit_text);
        mLaserScanTopicEditTextView = (EditText) v.findViewById(R.id.laser_scan_edit_view);
        mCameraTopicEditTextView = (EditText) v.findViewById(R.id.camera_topic_edit_view);
        mOdometryTopicEditTextView = (EditText) v.findViewById(R.id.odometry_topic_edit_view);
        mSensorDataTopicEditTextView = (EditText) v.findViewById(R.id.sensorData_topic_edit_view);
        mCarInfoTopicEditTextView = (EditText) v.findViewById(R.id.carInfo_topic_edit_view);
        mModeControlTopicEditTextView = (EditText) v.findViewById(R.id.mode_control_topic_edit_view);
        mReverseLaserScanCheckBox = (CheckBox) v.findViewById(R.id.reverse_laser_scan_check_box);
        mInvertXAxisCheckBox = (CheckBox) v.findViewById(R.id.invert_x_axis_check_box);
        mInvertYAxisCheckBox = (CheckBox) v.findViewById(R.id.invert_y_axis_check_box);
        mInvertAngularVelocityCheckBox = (CheckBox) v.findViewById(R.id.invert_angular_velocity_check_box);
        mSteeringTopicEditTextView = (EditText) v.findViewById(R.id.steering_topic_edit_view);
        mUSLeftTopicEditTextView = (EditText) v.findViewById(R.id.usl_topic_edit_view);
        mUSRightTopicEditTextView = (EditText) v.findViewById(R.id.usr_topic_edit_view);
        mUSFrontTopicEditTextView = (EditText) v.findViewById(R.id.usf_topic_edit_view);
        mIMUTopicEditTextView = (EditText) v.findViewById(R.id.imu_topic_edit_view);
        mMAGTopicEditTextView = (EditText) v.findViewById(R.id.mag_topic_edit_view);
        mVdBatTopicEditTextView = (EditText) v.findViewById(R.id.vdbat_topic_edit_view);
        mVsBatTopicEditTextView = (EditText) v.findViewById(R.id.vsbat_topic_edit_view);

        mNameEditTextView.setText(mInfo.getName());
        mMasterUriEditTextView.setText(mInfo.getMasterUri());

        mAdvancedOptionsCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mAdvancedOptionsView.setVisibility(View.VISIBLE);
                } else {
                    mAdvancedOptionsView.setVisibility(View.GONE);
                }
            }
        });

        mMotorTopicEditTextView.setText(mInfo.getMotorTopic());
        mLaserScanTopicEditTextView.setText(mInfo.getLaserTopic());
        mCameraTopicEditTextView.setText(mInfo.getCameraTopic());
        mOdometryTopicEditTextView.setText(mInfo.getOdometryTopic());
        mSensorDataTopicEditTextView.setText(mInfo.getSensorDataTopic());
        mCarInfoTopicEditTextView.setText(mInfo.getCarInfoTopic());
        mModeControlTopicEditTextView.setText(mInfo.getModeControlTopic());
        mSteeringTopicEditTextView.setText(mInfo.getSteeringTopic());
        mUSLeftTopicEditTextView.setText(mInfo.getUsLeftTopic());
        mUSRightTopicEditTextView.setText(mInfo.getUsRightTopic());
        mUSFrontTopicEditTextView.setText(mInfo.getUsFrontTopic());
        mIMUTopicEditTextView.setText(mInfo.getImuTopic());
        mMAGTopicEditTextView.setText(mInfo.getMagTopic());
        mVdBatTopicEditTextView.setText(mInfo.getVdBatTopic());
        mVsBatTopicEditTextView.setText(mInfo.getVsBatTopic());
        mReverseLaserScanCheckBox.setChecked(mInfo.isReverseLaserScan());
        mInvertXAxisCheckBox.setChecked(mInfo.isInvertX());
        mInvertYAxisCheckBox.setChecked(mInfo.isInvertY());
        mInvertAngularVelocityCheckBox.setChecked(mInfo.isInvertAngularVelocity());

        builder.setTitle(R.string.add_edit_robot)
                .setView(v)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String name = mNameEditTextView.getText().toString().trim();
                        String masterUri = mMasterUriEditTextView.getText().toString().trim();
                        String joystickTopic = mMotorTopicEditTextView.getText().toString().trim();
                        String laserScanTopic = mLaserScanTopicEditTextView.getText().toString().trim();
                        String cameraTopic = mCameraTopicEditTextView.getText().toString().trim();
                        String odometryTopic = mOdometryTopicEditTextView.getText().toString().trim();
                        String sensorDataTopic = mSensorDataTopicEditTextView.getText().toString().trim();
                        String carInfoTopic = mCarInfoTopicEditTextView.getText().toString().trim();
                        String modeControlTopic = mModeControlTopicEditTextView.getText().toString().trim();
                        String steeringTopic = mSteeringTopicEditTextView.getText().toString().trim();
                        String usLeftTopic = mUSLeftTopicEditTextView.getText().toString().trim();
                        String usRightTopic = mUSRightTopicEditTextView.getText().toString().trim();
                        String usFrontTopic = mUSFrontTopicEditTextView.getText().toString().trim();
                        String imuTopic = mIMUTopicEditTextView.getText().toString().trim();
                        String magTopic = mMAGTopicEditTextView.getText().toString().trim();
                        String vdBatTopic = mVdBatTopicEditTextView.getText().toString().trim();
                        String vsBatTopic = mVsBatTopicEditTextView.getText().toString().trim();
                        boolean reverseLaserScan = mReverseLaserScanCheckBox.isChecked();
                        boolean invertX = mInvertXAxisCheckBox.isChecked();
                        boolean invertY = mInvertYAxisCheckBox.isChecked();
                        boolean invertAngVel = mInvertAngularVelocityCheckBox.isChecked();

                        if (masterUri.equals("")) {
                            Toast.makeText(getActivity(), "Master URI required", Toast.LENGTH_SHORT).show();
                        } else if (joystickTopic.equals("") || laserScanTopic.equals("") || cameraTopic.equals("")
                                || odometryTopic.equals("") || sensorDataTopic.equals("") || carInfoTopic.equals("") || modeControlTopic.equals("")) {
                            Toast.makeText(getActivity(), "All topic names are required", Toast.LENGTH_SHORT).show();
                        } else if (!name.equals("")) {
                            mListener.onAddEditDialogPositiveClick(new RobotInfo(mInfo.getId(), name,
                                    masterUri, joystickTopic, laserScanTopic, cameraTopic,
                                    odometryTopic, sensorDataTopic, carInfoTopic, modeControlTopic, steeringTopic, usLeftTopic,
                                    usRightTopic, usFrontTopic, imuTopic, magTopic, vdBatTopic, vsBatTopic, reverseLaserScan, invertX, invertY, invertAngVel), mPosition);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Robot name required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onAddEditDialogNegativeClick(AddEditRobotDialogFragment.this);
                dialog.cancel();
            }
        });

        return builder.create();
    }

    public interface DialogListener {
        void onAddEditDialogPositiveClick(RobotInfo info, int position);

        void onAddEditDialogNegativeClick(DialogFragment dialog);
    }

}
