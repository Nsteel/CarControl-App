package com.robotca.ControlApp.Core;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Container for information about connections to specific Robots.
 *
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotInfo implements Comparable<RobotInfo>, Savable {

    // Number of RobotInfos in storage
    private static int robotCount = 1;

    /** Bundle key for UUID */
    public static final String UUID_KEY = "UUID_KEY";
    /** Bundle key for robot name */
    public static final String ROBOT_NAME_KEY = "ROBOT_NAME_KEY";
    /** Bundle key for master URI */
    public static final String MASTER_URI_KEY = "MASTER_URI_KEY";
    /** Bundle key for motor level topic */
    public static final String MOTOR_TOPIC_KEY = "MOTOR_TOPIC_KEY";
    /** Bundle key for laser scan topic */
    public static final String LASER_SCAN_TOPIC_KEY = "LASER_SCAN_TOPIC_KEY";
    /** Bundle key for camera topic */
    public static final String CAMERA_TOPIC_KEY = "CAMERA_TOPIC_KEY";
    /** Bundle key for odometry topic */
    public static final String ODOMETRY_TOPIC_KEY = "ODOMETRY_TOPIC_KEY";
    /** Bundle key for sensorData topic */
    public static final String SENSOR_DATA_TOPIC_KEY = "SENSOR_DATA_TOPIC_KEY";
    /** Bundle key for mode_control topic */
    public static final String MODE_CONTROL_TOPIC_KEY = "MODE_CONTROL_TOPIC_KEY";
    /** Bundle key for car info topic */
    public static final String CAR_INFO_TOPIC_KEY = "CAR_INFO_TOPIC_KEY";
    /** Bundle key for steering level topic */
    public static final String STEERING_TOPIC_KEY = "STEERING_TOPIC_KEY";
    /** Bundle key for ultra sonic sensor left topic */
    public static final String US_LEFT_TOPIC_KEY = "US_LEFT_TOPIC_KEY";
    /** Bundle key for ultra sonic sensor right topic */
    public static final String US_RIGHT_TOPIC_KEY = "US_RIGHT_TOPIC_KEY";
    /** Bundle key for ultra sonic sensor front topic */
    public static final String US_FRONT_TOPIC_KEY = "US_FRONT_TOPIC_KEY";
    /** Bundle key for IMU topic */
    public static final String IMU_TOPIC_KEY = "IMU_TOPIC_KEY";
    /** Bundle key for magnetometer topic */
    public static final String MAG_TOPIC_KEY = "MAG_TOPIC_KEY";
    /** Bundle key for the topic of the voltage of the drive battery */
    public static final String VDBAT_TOPIC_KEY = "VDBAT_TOPIC_KEY";
    /** Bundle key for the topic of the voltage of the system battery */
    public static final String VSBAT_TOPIC_KEY = "VSBAT_TOPIC_KEY";
    /** Bundle key for reverse laser scan */
    public static final String REVERSE_LASER_SCAN_KEY = "REVERSE_LASER_SCAN_KEY";
    /** Bundle key for invert x-axis */
    public static final String INVERT_X_KEY = "INVERT_X_KEY";
    /** Bundle key for invert x-axis */
    public static final String INVERT_Y_KEY = "INVERT_Y_KEY";
    /** Bundle key for invert x-axis */
    public static final String INVERT_ANGULAR_VELOCITY_KEY = "INVERT_ANGULAR_VELOCITY_KEY";


    // UUID for this RobotInfo
    private UUID id = UUID.randomUUID();

    // Name of this RobotInfo
    private String name;
    // Master URI of this RobotInfo
    private String masterUriString;

    // Topic names
    private String motorTopic;
    private String steeringTopic;
    private String cameraTopic;
    private String laserTopic;
    private String odometryTopic;
    private String usLeftTopic;
    private String usRightTopic;
    private String usFrontTopic;
    private String imuTopic;
    private String magTopic;

    public String getVdBatTopic() {
        return vdBatTopic;
    }

    public void setVdBatTopic(String vdBatTopic) {
        this.vdBatTopic = vdBatTopic;
    }

    private String vdBatTopic;
    private String vsBatTopic;
    private String sensorDataTopic;
    private String carInfoTopic;
    private String modeControlTopic;
    private boolean reverseLaserScan;
    private boolean enableKinect;
    private boolean invertX;
    private boolean invertY;
    private boolean invertAngularVelocity;

    @SuppressWarnings("unused")
    private static final String TAG = "RobotInfo";

    /**
     * Default Constructor.
     */
    public RobotInfo() {
        //id = UUID.randomUUID();
        name = "Car" + robotCount++;
        masterUriString = "http://localhost:11311";
        motorTopic = "/uc_bridge/set_motor_level_msg";
        steeringTopic = "/uc_bridge/set_steering_level_msg";
        cameraTopic = "/kinect2/qhd/image_color/compressed";
        laserTopic = "/scan";
        odometryTopic = "/odom";
        usLeftTopic = "/uc_bridge/usl";
        usRightTopic = "/uc_bridge/usr";
        usFrontTopic = "/uc_bridge/usf";
        imuTopic = "/uc_bridge/imu";
        magTopic = "/uc_bridge/mag";
        vdBatTopic = "/uc_bridge/vdbat";
        vsBatTopic = "/uc_bridge/vsbat";
        modeControlTopic = "/carControlApp/mode_control";
        reverseLaserScan = false;
        invertX = false;
        invertY = false;
        invertAngularVelocity = false;
    }

//    public RobotInfo(String mName, String mMasterUri) {
//        this.name = mName;
//        this.masterUriString = mMasterUri;
//    }

    /**
     * Creates a RobotInfo.
     * @param id UUID
     * @param name Name to show when displaying this RobotInfo
     * @param masterUriString Master URI for this RobotInfo
     * @param motorTopic MotorLevel topic name for this RobotInfo
     * @param laserTopic LaserTopic name for this RobotInfo
     * @param cameraTopic CameraTopic name for this RobotInfo
     */
    public RobotInfo(UUID id, String name, String masterUriString, String motorTopic,
                     String laserTopic, String cameraTopic,
                     String odometryTopic, String modeControlTopic, String steeringTopic,
                     String usl, String usr, String usf, String imu,
                     String mag, String vdbat, String vsbat, boolean reverseLaserScan,
                     boolean invertX, boolean invertY, boolean invertAngularVelocity) {
        this.id = id;
        this.name = name;
        this.masterUriString = masterUriString;
        this.motorTopic = motorTopic;
        this.laserTopic = laserTopic;
        this.cameraTopic = cameraTopic;
        this.odometryTopic = odometryTopic;
        this.modeControlTopic = modeControlTopic;
        this.reverseLaserScan = reverseLaserScan;
        this.steeringTopic = steeringTopic;
        this.usLeftTopic = usl;
        this.usRightTopic = usr;
        this.usFrontTopic = usf;
        this.imuTopic = imu;
        this.magTopic = mag;
        this.vdBatTopic = vdbat;
        this.vsBatTopic = vsbat;
        this.invertX = invertX;
        this.invertY = invertY;
        this.invertAngularVelocity = invertAngularVelocity;
    }

    /**
     * @return UUID of this RobotInfo
     */
    public UUID getId(){return id;}

    /**
     * Sets the UUID of this RobotInfo
     * @param id The new UUID
     */
    public void setId(UUID id){ this.id = id; }

    /**
     * @return The ModeControlTopic name of this RobotInfo
     */
    public String getModeControlTopic() {
        return modeControlTopic;
    }

    /**
     * @return The CarInfoTopic name of this RobotInfo
     */
    public String getCarInfoTopic() {
        return carInfoTopic;
    }

    /**
     * Sets the CarInfoTopic for this RobotInfo.
     * @param carInfoTopic The new CarInfo
     */
    public void setCarInfoTopic(String carInfoTopic) {
        this.carInfoTopic = carInfoTopic;
    }
    
     /**
     * @return The SensorDataTopic name of this RobotInfo
     */
    public String getSensorDataTopic() {
        return sensorDataTopic;
    }

    /**
     * Sets the SensorDataTopic for this RobotInfo.
     * @param sensorDataTopic
     */
    public void setSensorDataTopic(String sensorDataTopic) {
        this.sensorDataTopic = sensorDataTopic;
    }

    public String getSteeringTopic() {
        return steeringTopic;
    }

    public void setSteeringTopic(String steeringTopic) {
        this.steeringTopic = steeringTopic;
    }

    public String getUsLeftTopic() {
        return usLeftTopic;
    }

    public void setUsLeftTopic(String usLeftTopic) {
        this.usLeftTopic = usLeftTopic;
    }

    public String getUsRightTopic() {
        return usRightTopic;
    }

    public void setUsRightTopic(String usRightTopic) {
        this.usRightTopic = usRightTopic;
    }

    public String getUsFrontTopic() {
        return usFrontTopic;
    }

    public void setUsFrontTopic(String usFrontTopic) {
        this.usFrontTopic = usFrontTopic;
    }

    public String getImuTopic() {
        return imuTopic;
    }

    public void setImuTopic(String imuTopic) {
        this.imuTopic = imuTopic;
    }

    public String getMagTopic() {
        return magTopic;
    }

    public void setMagTopic(String magTopic) {
        this.magTopic = magTopic;
    }

    public String getVsBatTopic() {
        return vsBatTopic;
    }

    public void setVsBatTopic(String vsBatTopic) {
        this.vsBatTopic = vsBatTopic;
    }

    /**
     * @return The OdometryTopic name of this RobotInfo
     */
    public String getOdometryTopic() {
        return odometryTopic;
    }

    /**
     * Sets the OdometryTopic for this RobotInfo.
     * @param odometryTopic The new JoystickTopic
     */
    public void setOdometryTopic(String odometryTopic) {
        this.odometryTopic = odometryTopic;
    }

    /**
     * @return The JoystickTopic name of this RobotInfo
     */
    public String getMotorTopic() {
        return motorTopic;
    }

    /**
     * Sets the JoystickTopic for this RobotInfo.
     * @param motorTopic The new JoystickTopic
     */
    public void setMotorTopic(String motorTopic) {
        this.motorTopic = motorTopic;
    }

    /**
     * @return The CameraTopic of this RobotInfo
     */
    public String getCameraTopic() {
        return cameraTopic;
    }

    /**
     * Sets the CameraTopic of this RobotInfo.
     * @param cameraTopic The new CameraTopic
     */
    public void setCameraTopic(String cameraTopic) {
        this.cameraTopic = cameraTopic;
    }

    /**
     * @return The LaserTopic of this RobotInfo
     */
    public String getLaserTopic() {
        return laserTopic;
    }

    /**
     * Sets the LaserTopic of this RobotInfo.
     * @param laserTopic The new LaserTopic
     */
    public void setLaserTopic(String laserTopic) {
        this.laserTopic = laserTopic;
    }

    /**
     * @return The Master URI of this RobotInfo
     */
    public String getMasterUri() {
        return masterUriString;
    }

    /**
     * Sets the Master URI of this RobotInfo.
     * @param mMasterUri The new Master URI
     */
    public void setMasterUri(String mMasterUri) {
        this.masterUriString = mMasterUri;
    }

    /**
     * @return The name of this RobotInfo
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this RobotInfo.
     * @param mName The new name
     */
    public void setName(String mName) {
        this.name = mName;
    }

    /**
     * @return The URI of Master URI of this RobotInfo
     */
    public URI getUri(){
        return URI.create(getMasterUri());
    }

    /**
     * @return If laser scan should be reversed
     */
    public boolean isReverseLaserScan() {
        return reverseLaserScan;
    }

    /**
     * Sets whether the laser scan should be reversed
     * @param reverseLaserScan Reverse if true, false otherwise
     */
    public void setReverseLaserScan(boolean reverseLaserScan) {
        this.reverseLaserScan = reverseLaserScan;
    }

    /**
     * @return If x-axis should be inverted
     */
    public boolean isInvertX() {
        return invertX;
    }

    /**
     * Sets whether to invert x-axis
     * @param invertX Invert if true, false otherwise
     */
    public void setInvertX(boolean invertX) {
        this.invertX = invertX;
    }

    /**
     * @return If y-axis should be inverted
     */
    public boolean isInvertY() {
        return invertY;
    }

    /**
     * Sets whether to invert y-axis
     * @param invertY Invert if true, false otherwise
     */
    public void setInvertY(boolean invertY) {
        this.invertY = invertY;
    }

    /**
     * @return If angular velocity should be inverted
     */
    public boolean isInvertAngularVelocity() {
        return invertAngularVelocity;
    }

    /**
     * Sets whether to invert angular velocity
     * @param invertAngularVelocity Invert if true, false otherwise
     */
    public void setInvertAngularVelocity(boolean invertAngularVelocity) {
        this.invertAngularVelocity = invertAngularVelocity;
    }

    /**
     * Compares this RobotInfo to another based on UUID.
     * @param another The other RobotInfo
     * @return The comparison result
     */
    @Override
    public int compareTo(@NonNull RobotInfo another) {

        if (this.getId() == null) {
            return -1;
        }

        if (another.getId() == null) {
            return 1;
        }

        return this.getId().compareTo(another.getId());
    }

    /**
     * Determines the correct value for robotCount.
     * @param list The list of loaded RobotInfos
     */
    public static void resolveRobotCount(List<RobotInfo> list)
    {
//        Log.d(TAG, "resolveRobotCount(" + list + ")");

        int max = 0;
        int val;

        for (RobotInfo info: list) {
            if (info.getName().startsWith("Car"))
            {
//                Log.d(TAG, "name = " + info.getName().substring(5));
                try {
                    val = Integer.parseInt(info.getName().substring(3));
                }
                catch (NumberFormatException e) {
                    val = -1;
                }

                if (val > max)
                    max = val;
            }
        }

        robotCount = max + 1;
    }

    /**
     * @return The robot count.
     */
    @SuppressWarnings("unused")
    static int getRobotCount()
    {
        return robotCount;
    }

    @Override
    public void load(@NonNull Bundle bundle) {
        id = UUID.fromString(bundle.getString(UUID_KEY, UUID.randomUUID().toString()));
        name = bundle.getString(ROBOT_NAME_KEY, "");
        masterUriString = bundle.getString(MASTER_URI_KEY, "http://localhost:11311");
        motorTopic = bundle.getString(MOTOR_TOPIC_KEY, "/uc_bridge/set_motor_level_msg");
        steeringTopic = bundle.getString(STEERING_TOPIC_KEY, "/uc_bridge/set_steering_level_msg");
        cameraTopic = bundle.getString(CAMERA_TOPIC_KEY, "/kinect2/qhd/image_color/compressed");
        laserTopic = bundle.getString(LASER_SCAN_TOPIC_KEY, "/scan");
        odometryTopic = bundle.getString(ODOMETRY_TOPIC_KEY, "/odom");
        usLeftTopic = bundle.getString(US_LEFT_TOPIC_KEY, "/uc_bridge/usl");
        usRightTopic = bundle.getString(US_RIGHT_TOPIC_KEY, "/uc_bridge/usr");
        usFrontTopic = bundle.getString(US_FRONT_TOPIC_KEY, "/uc_bridge/usf");
        imuTopic = bundle.getString(IMU_TOPIC_KEY, "/uc_bridge/imu");
        magTopic = bundle.getString(MAG_TOPIC_KEY, "/uc_bridge/mag");
        vdBatTopic = bundle.getString(VDBAT_TOPIC_KEY, "/uc_bridge/vdbat");
        vsBatTopic = bundle.getString(VSBAT_TOPIC_KEY, "/uc_bridge/vsbat");
        modeControlTopic = bundle.getString(MODE_CONTROL_TOPIC_KEY, "/carControlApp/mode_control");
        reverseLaserScan = bundle.getBoolean(REVERSE_LASER_SCAN_KEY, false);
        invertX = bundle.getBoolean(INVERT_X_KEY, false);
        invertY = bundle.getBoolean(INVERT_Y_KEY, false);
        invertAngularVelocity = bundle.getBoolean(INVERT_ANGULAR_VELOCITY_KEY, false);
    }

    public void load(@NonNull SharedPreferences prefs) {
        motorTopic = prefs.getString(RobotStorage.getPreferenceKey(MOTOR_TOPIC_KEY), "/uc_bridge/set_motor_level_msg");
        steeringTopic = prefs.getString(RobotStorage.getPreferenceKey(STEERING_TOPIC_KEY), "/uc_bridge/set_steering_level_msg");
        cameraTopic = prefs.getString(RobotStorage.getPreferenceKey(CAMERA_TOPIC_KEY), "/kinect2/qhd/image_color/compressed");
        laserTopic = prefs.getString(RobotStorage.getPreferenceKey(LASER_SCAN_TOPIC_KEY), "/scan");
        odometryTopic = prefs.getString(RobotStorage.getPreferenceKey(ODOMETRY_TOPIC_KEY), "/odom");
        usLeftTopic = prefs.getString(RobotStorage.getPreferenceKey(US_LEFT_TOPIC_KEY), "/uc_bridge/usl");
        usRightTopic = prefs.getString(RobotStorage.getPreferenceKey(US_RIGHT_TOPIC_KEY), "/uc_bridge/usr");
        usFrontTopic = prefs.getString(RobotStorage.getPreferenceKey(US_FRONT_TOPIC_KEY), "/uc_bridge/usf");
        imuTopic = prefs.getString(RobotStorage.getPreferenceKey(IMU_TOPIC_KEY), "/uc_bridge/imu");
        magTopic = prefs.getString(RobotStorage.getPreferenceKey(MAG_TOPIC_KEY), "/uc_bridge/mag");
        vdBatTopic = prefs.getString(RobotStorage.getPreferenceKey(VDBAT_TOPIC_KEY), "/uc_bridge/vdbat");
        vsBatTopic = prefs.getString(RobotStorage.getPreferenceKey(VSBAT_TOPIC_KEY), "/uc_bridge/vsbat");
        modeControlTopic = prefs.getString(RobotStorage.getPreferenceKey(MODE_CONTROL_TOPIC_KEY), "/carControlApp/mode_control");
        reverseLaserScan = prefs.getBoolean(RobotStorage.getPreferenceKey(REVERSE_LASER_SCAN_KEY), false);
        invertX = prefs.getBoolean(RobotStorage.getPreferenceKey(INVERT_X_KEY), false);
        invertY = prefs.getBoolean(RobotStorage.getPreferenceKey(INVERT_Y_KEY), false);
        invertAngularVelocity = prefs.getBoolean(RobotStorage.getPreferenceKey(INVERT_ANGULAR_VELOCITY_KEY), false);
    }

    @Override
    public void save(@NonNull Bundle bundle) {
        bundle.putString(UUID_KEY, id.toString());
        bundle.putString(ROBOT_NAME_KEY, name);
        bundle.putString(MASTER_URI_KEY, masterUriString);
        bundle.putString(MOTOR_TOPIC_KEY, motorTopic);
        bundle.putString(STEERING_TOPIC_KEY, steeringTopic);
        bundle.putString(CAMERA_TOPIC_KEY, cameraTopic);
        bundle.putString(LASER_SCAN_TOPIC_KEY, laserTopic);
        bundle.putString(ODOMETRY_TOPIC_KEY, odometryTopic);
        bundle.putString(US_LEFT_TOPIC_KEY, usLeftTopic);
        bundle.putString(US_RIGHT_TOPIC_KEY, usRightTopic);
        bundle.putString(US_FRONT_TOPIC_KEY, usFrontTopic);
        bundle.putString(IMU_TOPIC_KEY, imuTopic);
        bundle.putString(MAG_TOPIC_KEY, magTopic);
        bundle.putString(VDBAT_TOPIC_KEY, vdBatTopic);
        bundle.putString(VSBAT_TOPIC_KEY, vsBatTopic);
        bundle.putString(SENSOR_DATA_TOPIC_KEY, sensorDataTopic);
        bundle.putString(CAR_INFO_TOPIC_KEY, carInfoTopic);
        bundle.putString(MODE_CONTROL_TOPIC_KEY, modeControlTopic);
        bundle.putBoolean(REVERSE_LASER_SCAN_KEY, reverseLaserScan);
        bundle.putBoolean(INVERT_X_KEY, invertX);
        bundle.putBoolean(INVERT_Y_KEY, invertY);
        bundle.putBoolean(INVERT_ANGULAR_VELOCITY_KEY, invertAngularVelocity);
    }

    public void save(@NonNull SharedPreferences.Editor prefs) {
        prefs.putString(RobotStorage.getPreferenceKey(MOTOR_TOPIC_KEY), motorTopic);
        prefs.putString(RobotStorage.getPreferenceKey(STEERING_TOPIC_KEY), steeringTopic);
        prefs.putString(RobotStorage.getPreferenceKey(CAMERA_TOPIC_KEY), cameraTopic);
        prefs.putString(RobotStorage.getPreferenceKey(LASER_SCAN_TOPIC_KEY), laserTopic);
        prefs.putString(RobotStorage.getPreferenceKey(ODOMETRY_TOPIC_KEY), odometryTopic);
        prefs.putString(RobotStorage.getPreferenceKey(US_LEFT_TOPIC_KEY), usLeftTopic);
        prefs.putString(RobotStorage.getPreferenceKey(US_RIGHT_TOPIC_KEY), usRightTopic);
        prefs.putString(RobotStorage.getPreferenceKey(US_FRONT_TOPIC_KEY), usFrontTopic);
        prefs.putString(RobotStorage.getPreferenceKey(IMU_TOPIC_KEY), imuTopic);
        prefs.putString(RobotStorage.getPreferenceKey(MAG_TOPIC_KEY), magTopic);
        prefs.putString(RobotStorage.getPreferenceKey(VDBAT_TOPIC_KEY), vdBatTopic);
        prefs.putString(RobotStorage.getPreferenceKey(VSBAT_TOPIC_KEY), vsBatTopic);
        prefs.putString(RobotStorage.getPreferenceKey(SENSOR_DATA_TOPIC_KEY), sensorDataTopic);
        prefs.putString(RobotStorage.getPreferenceKey(CAR_INFO_TOPIC_KEY), carInfoTopic);
        prefs.putString(RobotStorage.getPreferenceKey(MODE_CONTROL_TOPIC_KEY), modeControlTopic);
        prefs.putBoolean(RobotStorage.getPreferenceKey(REVERSE_LASER_SCAN_KEY), reverseLaserScan);
        prefs.putBoolean(RobotStorage.getPreferenceKey(INVERT_X_KEY), invertX);
        prefs.putBoolean(RobotStorage.getPreferenceKey(INVERT_Y_KEY), invertY);
        prefs.putBoolean(RobotStorage.getPreferenceKey(INVERT_ANGULAR_VELOCITY_KEY), invertAngularVelocity);
    }
}
