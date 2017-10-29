package com.robotca.ControlApp.Core;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Core.Plans.RobotPlan;
import com.robotca.ControlApp.R;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import geometry_msgs.Point;
import geometry_msgs.Quaternion;
import nav_msgs.Odometry;
import sensor_msgs.Imu;
import std_msgs.Int16;
import sensor_msgs.Range;

import sensor_msgs.CompressedImage;
import sensor_msgs.LaserScan;
import sensor_msgs.Range;
import sensor_msgs.BatteryState;
import sensor_msgs.MagneticField;

/**
 * Manages receiving data from, and sending commands to, a connected Robot.
 *
 * Created by Michael Brunson on 2/13/16.
 */
public class RobotController implements NodeMain, Savable {

    // Logcat Tag
    private static final String TAG = "RobotController";

    // The parent Context
    private final ControlApp context;

    // Whether the RobotController has been initialized
    private boolean initialized;

    // Timer for periodically publishing velocity commands
    private Timer publisherTimer;

    // Publisher for Motor level
    private Publisher<Int16> motorPublisher;

    // Publisher for Steering level
    private Publisher<Int16> steeringPublisher;

    // Contains the current motor level to be published
    private Int16 currentMotorLevel;

    // Contains the current steering level to be published
    private Int16 currentSteeringLevel;

    // Indicates when a Command message should be published
    private boolean publishCommands;

    // Publisher for mode_control
    private Publisher<std_msgs.String> modeControlPublisher;

    // The most recent modeControl
    private std_msgs.String modeControl;
    // Subscriber to LaserScan data
    private Subscriber<LaserScan> laserScanSubscriber;
    // The most recent LaserScan
    private LaserScan laserScan;
    // Lock for synchronizing accessing and receiving the current LaserScan
    private final Object laserScanMutex = new Object();
    // Subscriber to US Sensor data
    private Subscriber<Range> usLeftSubscriber;
    // The most recent US Sensor data
    private Range usLeft;
    // Subscriber to US Sensor data
    private Subscriber<Range> usRightSubscriber;
    // The most recent US Sensor data
    private Range usRight;
    // Subscriber to US Sensor data
    private Subscriber<Range> usFrontSubscriber;
    // The most recent US Sensor data
    private Range usFront;
    // Subscriber to Imu data
    private Subscriber<Imu> imuSubscriber;
    // The most recent imu data
    private Imu imu;
    // Subscriber to magnetometer data
    private Subscriber<MagneticField> magSubscriber;
    // The most recent magnetometer data
    private MagneticField mag;
    // Subscriber to the voltage of the drive battery
    private Subscriber<BatteryState> vdbatSubscriber;
    // The most recent voltage of the drive battery
    private BatteryState vdbat;
    // Subscriber to the voltage of the system battery
    private Subscriber<BatteryState> vsbatSubscriber;
    // The most recent voltage of the system battery
    private BatteryState vsbat;
    // Lock for synchronizing accessing and receiving the current US data
    private final Object usLeftMutex = new Object();
    // Lock for synchronizing accessing and receiving the current US data
    private final Object usRightMutex = new Object();
    // Lock for synchronizing accessing and receiving the current US data
    private final Object usFrontMutex = new Object();
    // Lock for synchronizing accessing and receiving the current imu data
    private final Object imuMutex = new Object();
    // Lock for synchronizing accessing and receiving the current magnetometer data
    private final Object magMutex = new Object();
    // Lock for synchronizing accessing and receiving the current vdbat data
    private final Object vdbatMutex = new Object();
    // Lock for synchronizing accessing and receiving the current vsbat data
    private final Object vsbatMutex = new Object();
    // Subscriber to Odometry data
    private Subscriber<Odometry> odometrySubscriber;
    // The most recent Odometry
    private Odometry odometry;
    // Lock for synchronizing accessing and receiving the current Odometry
    private final Object odometryMutex = new Object();

    // The most recent CarTelemetryWrapper
    private final CarTelemetryWrapper carTelemetryWrapper = new CarTelemetryWrapper();
    // Lock for synchronizing accessing and receiving the current CarTelemetryWrapper
    private final Object carTelemetryWrapperMutex = new Object();

    // Subscriber to Image data
    private Subscriber<CompressedImage> imageSubscriber;
    // The most recent Image
    private CompressedImage image;
    // Lock for synchronizing accessing and receiving the current Image
    private final Object imageMutex = new Object();
    private MessageListener<CompressedImage> imageMessageReceived;

    // The currently running RobotPlan
    private RobotPlan motionPlan;
    // The currently paused RobotPlan
    private int pausedPlanId;

    // The node connected to the Robot on which data can be sent and received
    private ConnectedNode connectedNode;

    // Listener for LaserScans
    private final ArrayList<MessageListener<LaserScan>> laserScanListeners;
    // Listener for usLeft
    private ArrayList<MessageListener<Range>> usLeftListeners;
    // Listener for usRight
    private ArrayList<MessageListener<Range>> usRightListeners;
    // Listener for usFront
    private ArrayList<MessageListener<Range>> usFrontListeners;
    // Listener for imu
    private ArrayList<MessageListener<Imu>> imuListeners;
    // Listener for magnetometer
    private ArrayList<MessageListener<MagneticField>> magListeners;
    // Listener for vdbat
    private ArrayList<MessageListener<BatteryState>> vdbatListeners;
    // Listener for vsbat
    private ArrayList<MessageListener<BatteryState>> vsbatListeners;
    // Listener for Odometry
    private ArrayList<MessageListener<Odometry>> odometryListeners;
    // Listener for CarTelemetryWrapper
    private ArrayList<MessageListener<CarTelemetryWrapper>> carTelemetryWrapperListeners;


    // The Robot's starting position
    private static Point startPos;
    // The Robot's last recorded position
    private static Point currentPos;
    // The Robot's last recorded orientation
    private static Quaternion rotation;
    // The Robot's last recorded speed
    private static double speed;
    // The Robot's last recorded turn rate
    private static double turnRate;

    // Bundle ID for pausedPlan
    private static final String PAUSED_PLAN_BUNDLE_ID = "com.robotca.ControlApp.Core.RobotController.pausedPlan";

    // Constant for no motion plan
    private static final int NO_PLAN = -1;

    /**
     * Creates a RobotController.
     * @param context The Context the RobotController belongs to.
     */
    public RobotController(ControlApp context) {
        this.context = context;

        this.initialized = false;

        this.laserScanListeners = new ArrayList<>();
        this.usLeftListeners = new ArrayList<MessageListener<Range>>();
        this.usRightListeners = new ArrayList<MessageListener<Range>>();
        this.usFrontListeners = new ArrayList<MessageListener<Range>>();
        this.imuListeners = new ArrayList<MessageListener<Imu>>();
        this.magListeners = new ArrayList<MessageListener<MagneticField>>();
        this.vdbatListeners = new ArrayList<MessageListener<BatteryState>>();
        this.vsbatListeners = new ArrayList<MessageListener<BatteryState>>();
        this.odometryListeners = new ArrayList<MessageListener<Odometry>>();
        this.carTelemetryWrapperListeners = new ArrayList<MessageListener<CarTelemetryWrapper>>();

        pausedPlanId = NO_PLAN;

        startPos = null;
        currentPos = null;
        rotation = null;
    }

    /**
     * Adds an Odometry listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addOdometryListener(MessageListener<Odometry> l) {
        return odometryListeners.add(l);
    }

    /**
     * Adds an CarTelemetryWrapper listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addCarTelemetryWrapperListener(MessageListener<CarTelemetryWrapper> l) {
        return carTelemetryWrapperListeners.add(l);
    }

    /**
     * Adds an usLeft listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addUsLeftListener(MessageListener<Range> l) {
        return usLeftListeners.add(l);
    }

    /**
     * Adds an usRight listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addUsRightListener(MessageListener<Range> l) {
        return usRightListeners.add(l);
    }

    /**
     * Adds an usFront listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addUsFrontListener(MessageListener<Range> l) {
        return usFrontListeners.add(l);
    }

    /**
     * Adds an imu listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addImuListener(MessageListener<Imu> l) {
        return imuListeners.add(l);
    }

    /**
     * Adds an magnetometer listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addMagListener(MessageListener<MagneticField> l) {
        return magListeners.add(l);
    }

    /**
     * Adds an vdbat listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addVdBatListener(MessageListener<BatteryState> l) {
        return vdbatListeners.add(l);
    }

    /**
     * Adds an vsbat listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addVsBatListener(MessageListener<BatteryState> l) {
        return vsbatListeners.add(l);
    }

    /**
     * Adds a LaserScan listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addLaserScanListener(MessageListener<LaserScan> l) {
        synchronized (laserScanListeners) {
            return laserScanListeners.add(l);
        }
    }

    public void setCurrentMotorLevel(Int16 currentMotorLevel) {
        this.currentMotorLevel = currentMotorLevel;
    }

    public void setCurrentSteeringLevel(Int16 currentSteeringLevel) {
        this.currentSteeringLevel = currentSteeringLevel;
    }

    public Int16 getCurrentMotorLevel() {
        return currentMotorLevel;
    }

    public Int16 getCurrentSteeringLevel() {
        return currentSteeringLevel;
    }

    public void setPublishCommands(boolean publishCommands) {
        this.publishCommands = publishCommands;
    }

    public void setModeControl(String modeControl) {
        Log.d("setModeControl", "set!");
        this.modeControl.setData(modeControl);
        refreshTopics();
    }

    /**
     * Removes a LaserScan listener.
     * @param l The listener
     * @return True if the listener was removed
     */
    public boolean removeLaserScanListener(MessageListener<LaserScan> l) {

        synchronized (laserScanListeners) {
            return laserScanListeners.remove(l);
        }
    }

    /**
     * Initializes the RobotController.
     * @param nodeMainExecutor The NodeMainExecutor on which to execute the NodeConfiguration.
     * @param nodeConfiguration The NodeConfiguration to execute
     */
    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration) {
        nodeMainExecutor.execute(this, nodeConfiguration.setNodeName("android/robot_controller"));
    }

    /**
     * Runs the specified RobotPlan on the Robot.
     * @param plan The RobotPlan
     */
    public void runPlan(RobotPlan plan) {
        stop(true);
        pausedPlanId = NO_PLAN;

        publishCommands = true;

        motionPlan = plan;
        if (motionPlan != null)
            motionPlan.run(this);
    }

    /**
     * Attempts to resume a stopped RobotPlan.
     * @return True if a RobotPlan was resumed
     */
    public boolean resumePlan() {
        if (pausedPlanId != NO_PLAN) {
            Log.d(TAG, "Resuming paused plan");
            runPlan(ControlMode.getRobotPlan(context, ControlMode.values()[pausedPlanId]));
            return true;
        }

        return false;
    }

    /**
     * @return The current RobotPlan
     */
    public RobotPlan getMotionPlan() {
        return motionPlan;
    }

    /**
     * @return Whether there is a paused motion plan
     */
    public boolean hasPausedPlan() {
        return pausedPlanId != NO_PLAN;
    }

    /**
     * Stops the Robot's current motion and any RobotPlan that may be running.
     *
     * @return True if a resumable RobotPlan was cancelled
     */
    public boolean stop() {
        return stop(true);
    }

    /**
     * Stops the Robot's current motion and optionally any RobotPlan that may be running.
     *
     * @param cancelMotionPlan Whether to cancel the current motion plan
     *
     * @return True if a resumable RobotPlan was cancelled
     */
    public boolean stop(boolean cancelMotionPlan) {

        if (cancelMotionPlan || pausedPlanId == NO_PLAN) {
            pausedPlanId = NO_PLAN;

            if (motionPlan != null) {
                motionPlan.stop();

                if (motionPlan.isResumable()) {
                    pausedPlanId = motionPlan.getControlMode() == null ? NO_PLAN : motionPlan.getControlMode().ordinal();
                }

                motionPlan = null;
            }
        }

        publishCommands = false;
        publishCommand(0.0, 0.0, 0.0);

        if (motorPublisher != null && steeringPublisher != null){
            motorPublisher.publish(currentMotorLevel);
            steeringPublisher.publish(currentSteeringLevel);
        }

        return pausedPlanId != NO_PLAN && cancelMotionPlan;
    }

    /**
     * Sets the next values of the next Command to publish.
     * @param linearVelocityX Linear velocity in the x direction
     * @param linearVelocityY Linear velocity in the y direction
     * @param angularVelocityZ Angular velocity about the z axis
     */
    public void publishCommand(double linearVelocityX, double linearVelocityY, double angularVelocityZ) {
        if (currentMotorLevel != null && currentSteeringLevel != null) {

            float scale = 1.0f;

            try {
                // Safe Mode
                if (context.getWarningSystem().isSafemodeEnabled() && linearVelocityX >= 0.0) {
                    scale = (float) Math.pow(1.0f - context.getHUDFragment().getWarnAmount(), 2.0);
                }
            }
            catch(Exception e){
                scale = 0;
                Log.e("Emergency Stop", e.getMessage());
            }

            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefs_invert_x_axis_key), false)){
                linearVelocityX *= -1;
            }

            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefs_invert_y_axis_key), false)){
                linearVelocityY *= -1;
            }

            if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefs_invert_angular_velocity_key), false)){
                angularVelocityZ *= -1;
            }

            currentMotorLevel.setData(((short) (linearVelocityX * scale)));
            currentSteeringLevel.setData(((short) -angularVelocityZ));
        } else {
            Log.w("Emergency Stop", "currentCommand is null");
        }
    }

    /**
     * Same as above, but forces the velocity to be published.
     * @param linearVelocityX Linear velocity in the x direction
     * @param linearVelocityY Linear velocity in the y direction
     * @param angularVelocityZ Angular velocity about the z axis
     */
    public void forceVelocity(double linearVelocityX, double linearVelocityY,
                              double angularVelocityZ) {
        publishCommands = true;
        publishCommand(linearVelocityX, linearVelocityY, angularVelocityZ);
    }

    /**
     * @return The default node name for the RobotController
     */
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/robot_controller");
    }

    /**
     * Callback for when the RobotController is connected.
     * @param connectedNode The ConnectedNode the RobotController is connected through
     */
    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        initialize();
    }

    /*
     * Initializes the RobotController.
     */
    public void initialize() {
        if (!initialized && connectedNode != null) {

            // Start the topics
            refreshTopics();

            initialized = true;
        }
    }

    /**
     * Refreshes all topics, recreating them if there topic names have been changed.
     */
    public void refreshTopics() {

        // Get the correct topic names
        String motorLevelTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_joystick_topic_edittext_key),
                        context.getString(R.string.motor_topic));

        String laserScanTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_laserscan_topic_edittext_key),
                        context.getString(R.string.laser_scan_topic));

        String odometryTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_odometry_topic_edittext_key),
                        context.getString(R.string.odometry_topic));
        String steeringLevelTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_steering_topic_edittext_key),
                        context.getString(R.string.steering_topic));
        String usLeftTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_usl_topic_edittext_key),
                        context.getString(R.string.usl_topic));
        String usRightTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_usr_topic_edittext_key),
                        context.getString(R.string.usr_topic));
        String usFrontTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_usf_topic_edittext_key),
                        context.getString(R.string.usf_topic));
        String imuTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_imu_topic_edittext_key),
                        context.getString(R.string.imu_topic));
        String magTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_mag_topic_edittext_key),
                        context.getString(R.string.mag_topic));
        String vdbatTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_vdbat_topic_edittext_key),
                        context.getString(R.string.vdbat_topic));
        String vsbatTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_vsbat_topic_edittext_key),
                        context.getString(R.string.vsbat_topic));
        String modeControlTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_mode_control_topic_edittext_key), context.getString(R.string.mode_control_topic));
        String imageTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_camera_topic_edittext_key),
                        context.getString(R.string.camera_topic));

        // Refresh the Command Publisher
        if (motorPublisher == null
                || !motorLevelTopic.equals(motorPublisher.getTopicName().toString())
                || steeringPublisher == null || !steeringLevelTopic.equals((steeringPublisher.getTopicName().toString()))) {

            if (publisherTimer != null) {
                publisherTimer.cancel();
            }

            if (motorPublisher != null) {
                motorPublisher.shutdown();
            }

            if(steeringPublisher != null) {
                steeringPublisher.shutdown();
            }

            // Start the Command publisher
            motorPublisher = connectedNode.newPublisher(motorLevelTopic, Int16._TYPE);
            steeringPublisher = connectedNode.newPublisher(steeringLevelTopic, Int16._TYPE);
            currentMotorLevel = motorPublisher.newMessage();
            currentSteeringLevel = steeringPublisher.newMessage();

            publisherTimer = new Timer();
            publisherTimer.schedule(new TimerTask() {
                @Override
                public void run() { if (publishCommands) {
                    motorPublisher.publish(currentMotorLevel);
                    steeringPublisher.publish(currentSteeringLevel);
                    Log.d("Publisher", "New motor and steering levels published!");
                    }
                }
            }, 0, 5);
            publishCommands = false;
        }

        // Refresh the Mode Control Publisher
        if (modeControlPublisher == null
                || !modeControlTopic.equals(modeControlPublisher.getTopicName().toString())) {

            if (modeControlPublisher != null) {
                modeControlPublisher.shutdown();
            }

            // Start the Mode Control publisher
            modeControlPublisher = connectedNode.newPublisher(modeControlTopic, std_msgs.String._TYPE);
            modeControl = modeControlPublisher.newMessage();
        }
        else if(modeControlPublisher != null) {
            modeControlPublisher.publish(modeControl);
        }

        // Refresh the LaserScan Subscriber
        if (laserScanSubscriber == null
                || !laserScanTopic.equals(laserScanSubscriber.getTopicName().toString())) {

            if (laserScanSubscriber != null)
                laserScanSubscriber.shutdown();

            // Start the LaserScan subscriber
            laserScanSubscriber = connectedNode.newSubscriber(laserScanTopic, LaserScan._TYPE);
            laserScanSubscriber.addMessageListener(new MessageListener<LaserScan>() {
                @Override
                public void onNewMessage(LaserScan laserScan) {
                    setLaserScan(laserScan);
                }
            });
        }

        // Refresh the Odometry Subscriber
        if (odometrySubscriber == null
                || !odometryTopic.equals(odometrySubscriber.getTopicName().toString())) {

            if (odometrySubscriber != null)
                odometrySubscriber.shutdown();

            // Start the Odometry subscriber
            odometrySubscriber = connectedNode.newSubscriber(odometryTopic, Odometry._TYPE);
            odometrySubscriber.addMessageListener(new MessageListener<Odometry>() {
                @Override
                public void onNewMessage(Odometry odometry) {
                    setOdometry(odometry);
                }
            });
        }

        // Refresh the usLeft Subscriber
        if (usLeftSubscriber == null
                || !usLeftTopic.equals(usLeftSubscriber.getTopicName().toString())) {

            if (usLeftSubscriber != null)
                usLeftSubscriber.shutdown();

            // Start the usLeft subscriber
            usLeftSubscriber = connectedNode.newSubscriber(usLeftTopic, Range._TYPE);
            usLeftSubscriber.addMessageListener(new MessageListener<Range>() {
                @Override
                public void onNewMessage(Range value) {
                    setUsLeft(value);
                }
            });
        }

        // Refresh the usRight Subscriber
        if (usRightSubscriber == null
                || !usRightTopic.equals(usRightSubscriber.getTopicName().toString())) {

            if (usRightSubscriber != null)
                usRightSubscriber.shutdown();

            // Start the usRight subscriber
            usRightSubscriber = connectedNode.newSubscriber(usRightTopic, Range._TYPE);
            usRightSubscriber.addMessageListener(new MessageListener<Range>() {
                @Override
                public void onNewMessage(Range value) {
                    setUsRight(value);
                }
            });
        }

        // Refresh the usFront Subscriber
        if (usFrontSubscriber == null
                || !usFrontTopic.equals(usFrontSubscriber.getTopicName().toString())) {

            if (usFrontSubscriber != null)
                usFrontSubscriber.shutdown();

            // Start the usFront subscriber
            usFrontSubscriber = connectedNode.newSubscriber(usFrontTopic, Range._TYPE);
            usFrontSubscriber.addMessageListener(new MessageListener<Range>() {
                @Override
                public void onNewMessage(Range value) {
                    setUsFront(value);
                }
            });
        }

        // Refresh the imu Subscriber
        if (imuSubscriber == null
                || !imuTopic.equals(imuSubscriber.getTopicName().toString())) {

            if (imuSubscriber != null)
                imuSubscriber.shutdown();

            // Start the imu subscriber
            imuSubscriber = connectedNode.newSubscriber(imuTopic, Imu._TYPE);
            imuSubscriber.addMessageListener(new MessageListener<Imu>() {
                @Override
                public void onNewMessage(Imu value) {
                    setImu(value);
                }
            });
        }

        // Refresh the mag Subscriber
        if (magSubscriber == null
                || !magTopic.equals(magSubscriber.getTopicName().toString())) {

            if (magSubscriber != null)
                magSubscriber.shutdown();

            // Start the mag subscriber
            magSubscriber = connectedNode.newSubscriber(magTopic, MagneticField._TYPE);
            magSubscriber.addMessageListener(new MessageListener<MagneticField>() {
                @Override
                public void onNewMessage(MagneticField value) {
                    setMag(value);
                }
            });
        }

        // Refresh the vdbat Subscriber
        if (vdbatSubscriber == null
                || !vdbatTopic.equals(vdbatSubscriber.getTopicName().toString())) {

            if (vdbatSubscriber != null)
                vdbatSubscriber.shutdown();

            // Start the vdbat subscriber
            vdbatSubscriber = connectedNode.newSubscriber(vdbatTopic, BatteryState._TYPE);
            vdbatSubscriber.addMessageListener(new MessageListener<BatteryState>() {
                @Override
                public void onNewMessage(BatteryState value) {
                    setVdBat(value);
                }
            });
        }

        // Refresh the vsbat Subscriber
        if (vsbatSubscriber == null
                || !vsbatTopic.equals(vsbatSubscriber.getTopicName().toString())) {

            if (vsbatSubscriber != null)
                vsbatSubscriber.shutdown();

            // Start the vsbat subscriber
            vsbatSubscriber = connectedNode.newSubscriber(vsbatTopic, BatteryState._TYPE);
            vsbatSubscriber.addMessageListener(new MessageListener<BatteryState>() {
                @Override
                public void onNewMessage(BatteryState value) {
                    setVsBat(value);
                }
            });
        }

        if(imageSubscriber == null || !imageTopic.equals(imageSubscriber.getTopicName().toString())){
            if(imageSubscriber != null)
                imageSubscriber.shutdown();

            imageSubscriber = connectedNode.newSubscriber(imageTopic, CompressedImage._TYPE);

            imageSubscriber.addMessageListener(new MessageListener<CompressedImage>() {
                @Override
                public void onNewMessage(CompressedImage image) {
                    setImage(image);
                    synchronized (imageMutex) {
                        if (imageMessageReceived != null) {
                            imageMessageReceived.onNewMessage(image);
                        }
                    }
                }
            });
        }
    }

    /**
     * Shuts down all topics.
     */
    public void shutdownTopics() {
        if(publisherTimer != null) {
            publisherTimer.cancel();
        }

        if (motorPublisher != null) {
            motorPublisher.shutdown();
        }

        if (modeControlPublisher != null) {
            modeControlPublisher.shutdown();
        }

        if (laserScanSubscriber != null) {
            laserScanSubscriber.shutdown();
        }

        if(odometrySubscriber != null){
            odometrySubscriber.shutdown();
        }

        if(usLeftSubscriber != null){
            usLeftSubscriber.shutdown();
        }

        if(usRightSubscriber != null){
            usRightSubscriber.shutdown();
        }

        if(usFrontSubscriber != null){
            usFrontSubscriber.shutdown();
        }

        if(imuSubscriber != null){
            imuSubscriber.shutdown();
        }

        if(magSubscriber != null){
            magSubscriber.shutdown();
        }

        if(vdbatSubscriber != null){
            vdbatSubscriber.shutdown();
        }

        if(vsbatSubscriber != null){
            vsbatSubscriber.shutdown();
        }
    }

    /**
     * Callback for when the RobotController is shutdown.
     * @param node The Node
     */
    @Override
    public void onShutdown(Node node) {
        shutdownTopics();
    }

    /**
     * Callback for when the shutdown is complete.
     * @param node The Node
     */
    @Override
    public void onShutdownComplete(Node node) {
        this.connectedNode = null;
    }

    /**
     * Callback indicating an error has occurred.
     * @param node The Node
     * @param throwable The error
     */
    @Override
    public void onError(Node node, Throwable throwable) {
        Log.e(TAG, "", throwable);
    }

    /**
     * @return The most recently received LaserScan
     */
    public LaserScan getLaserScan() {
        synchronized (laserScanMutex) {
            return laserScan;
        }
    }

    /**
     * Sets the current LaserScan.
     * @param laserScan The LaserScan
     */
    protected void setLaserScan(LaserScan laserScan) {
        synchronized (laserScanMutex) {
            this.laserScan = laserScan;

            boolean invertLaserScan = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.prefs_reverse_angle_reading_key), false);

            if(invertLaserScan) {
                float[] ranges = this.laserScan.getRanges();

                for (int i = 0; i < this.laserScan.getRanges().length / 2; i++) {
                    float range = ranges[i];
                    ranges[i] = ranges[ranges.length - i - 1];
                    ranges[ranges.length - i - 1] = range;
                }
            }
        }

        // Call the listener callbacks
        synchronized (laserScanListeners) {
            for (MessageListener<LaserScan> listener : laserScanListeners) {
                listener.onNewMessage(laserScan);
            }
        }
    }

    /**
     * Sets the current ultrasonic value.
     * @param usLeft The US value
     */
    public void setUsLeft(Range usLeft){
        synchronized (usLeftMutex){
            this.usLeft = usLeft;

            // Call the listener callbacks
            for (MessageListener<Range> listener: usLeftListeners) {
                listener.onNewMessage(usLeft);
            }
            // set usLeft in carTelemetryWrapper
            carTelemetryWrapper.setUsLeft(usLeft);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "SensorData Set");
    }

    /**
     * Sets the current ultrasonic value.
     * @param usRight The US value
     */
    public void setUsRight(Range usRight){
        synchronized (usRightMutex){
            this.usRight = usRight;

            // Call the listener callbacks
            for (MessageListener<Range> listener: usRightListeners) {
                listener.onNewMessage(usRight);
            }
            // set usRight in carTelemetryWrapper
            carTelemetryWrapper.setUsRight(usRight);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "SensorData Set");
    }

    /**
     * Sets the current ultrasonic value.
     * @param usFront The US value
     */
    public void setUsFront(Range usFront){
        synchronized (usFrontMutex){
            this.usFront = usFront;

            // Call the listener callbacks
            for (MessageListener<Range> listener: usFrontListeners) {
                listener.onNewMessage(usFront);
            }
            // set usFront in carTelemetryWrapper
            carTelemetryWrapper.setUsFront(usFront);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "SensorData Set");
    }

    /**
     * Sets the current Imu data.
     * @param imu The Imu data
     */
    public void setImu(Imu imu){
        synchronized (imuMutex){
            this.imu = imu;

            // Call the listener callbacks
            for (MessageListener<Imu> listener: imuListeners) {
                listener.onNewMessage(imu);
            }
            // set Imu in carTelemetryWrapper
            carTelemetryWrapper.setImu(imu);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "SensorData Set");
    }

    /**
     * Sets the current magnetometer data.
     * @param mag The magnetometer data
     */
    public void setMag(MagneticField mag){
        synchronized (magMutex){
            this.mag = mag;

            // Call the listener callbacks
            for (MessageListener<MagneticField> listener: magListeners) {
                listener.onNewMessage(mag);
            }
            // set Mag in carTelemetryWrapper
            carTelemetryWrapper.setMag(mag);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "SensorData Set");
    }

    /**
     * Sets the current voltage of the drive battery.
     * @param vdbat The voltage of the drive battery
     */
    public void setVdBat(BatteryState vdbat){
        synchronized (vdbatMutex){
            this.vdbat = vdbat;

            // Call the listener callbacks
            for (MessageListener<BatteryState> listener: vdbatListeners) {
                listener.onNewMessage(vdbat);
            }
            // set vdbat in carTelemetryWrapper
            carTelemetryWrapper.setVdBat(vdbat);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "SensorData Set");
    }

    /**
     * Sets the current voltage of the system battery.
     * @param vsbat The voltage of the system battery
     */
    public void setVsBat(BatteryState vsbat){
        synchronized (vsbatMutex){
            this.vsbat = vsbat;

            // Call the listener callbacks
            for (MessageListener<BatteryState> listener: vsbatListeners) {
                listener.onNewMessage(vsbat);
            }
            // set vsbat in carTelemetryWrapper
            carTelemetryWrapper.setVsBat(vsbat);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "SensorData Set");
    }

    /**
     * Sets the current Odometry.
     * @param odometry The Odometry
     */
    protected void setOdometry(Odometry odometry) {

        synchronized (odometryMutex) {
            this.odometry = odometry;

            // Call the listener callbacks
            for (MessageListener<Odometry> listener: odometryListeners) {
                listener.onNewMessage(odometry);
            }

            if (startPos == null) {
                startPos = odometry.getPose().getPose().getPosition();
            } else {
                currentPos = odometry.getPose().getPose().getPosition();
            }
            rotation = odometry.getPose().getPose().getOrientation();

            // Record speed and turnrate
            speed = odometry.getTwist().getTwist().getLinear().getX();
            turnRate = odometry.getTwist().getTwist().getAngular().getZ();
            // set odomometry in CarTelemetryWrapper
            carTelemetryWrapper.setOdometry(odometry);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }
        //Log.d("RobotController", "Odometry Set");
    }

    /**
     * Sets the current CarTelemetryWrapper.
     * @param carTelemetryWrapper The CarTelemetryWrapper
     */
    public void setCarTelemetryWrapper(CarTelemetryWrapper carTelemetryWrapper){
        synchronized (carTelemetryWrapperMutex){
            // Call the listener callbacks
            for (MessageListener<CarTelemetryWrapper> listener: carTelemetryWrapperListeners) {
                listener.onNewMessage(carTelemetryWrapper);
            }
        }

        //Log.d("RobotController", "CarTelemetryWrapper Set");
    }


    public int getMotorLimit() {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.prefs_motor_limit_key), "600"));
    }

    /**
     * Load from a Bundle.
     *
     * @param bundle The Bundle
     */
    @Override
    public void load(@NonNull Bundle bundle) {
        pausedPlanId = bundle.getInt(PAUSED_PLAN_BUNDLE_ID, NO_PLAN);
    }

    /**
     * Save to a Bundle.
     *
     * @param bundle The Bundle
     */
    @Override
    public void save(@NonNull Bundle bundle) {
        bundle.putInt(PAUSED_PLAN_BUNDLE_ID, pausedPlanId);
    }

    /**
     * @return The Robot's last reported x position
     */
    public static double getX() {
        if (currentPos == null)
            return 0.0;
        else
            return currentPos.getX() - startPos.getX();
    }

    /**
     * @return The Robot's last reported y position
     */
    public static double getY() {
        if (currentPos == null)
            return 0.0;
        else
            return currentPos.getY() - startPos.getY();
    }

    /**
     * @return The Robot's last reported heading in radians
     */
    public static double getHeading() {
        if (rotation == null)
            return 0.0;
        else
            return Utils.getHeading(org.ros.rosjava_geometry.Quaternion.fromQuaternionMessage(rotation));
    }

    /**
     * @return The Robot's last reported speed in the range [-1, 1].
     */
    public static double getSpeed() {
        return speed;
    }

    /**
     * @return The Robot's last reported turn rate in the range[-1, 1].
     */
    public static double getTurnRate() {
        return turnRate;
    }


    public void setCameraMessageReceivedListener(MessageListener<CompressedImage> cameraMessageReceived) {
        this.imageMessageReceived = cameraMessageReceived;
    }

    public void setImage(CompressedImage image) {
        synchronized (imageMutex) {
            this.image = image;
        }
    }

    public CompressedImage getImage(){
        synchronized (imageMutex) {
            return this.image;
        }
    }
}