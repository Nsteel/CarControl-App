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
import pses_basis.CarInfo;
import pses_basis.Command;
import pses_basis.SensorData;
import sensor_msgs.CompressedImage;
import sensor_msgs.LaserScan;

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

    // Publisher for commands
    private Publisher<Command> commandPublisher;

    // Contains the current Command message to be published
    private Command currentCommand;

    // Indicates when a Command message should be published
    private boolean publishCommands;

    // Subscriber to LaserScan data
    private Subscriber<LaserScan> laserScanSubscriber;
    // The most recent LaserScan
    private LaserScan laserScan;
    // Lock for synchronizing accessing and receiving the current LaserScan
    private final Object laserScanMutex = new Object();
    // Subscriber to Sensor data
    private Subscriber<SensorData> sensorDataSubscriber;
    // The most recent Sensor data
    private SensorData sensorData;
    // Lock for synchronizing accessing and receiving the current Sensor data
    private final Object sensorDataMutex = new Object();
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

    // Subscriber to CarInfo
    private Subscriber<CarInfo> carInfoSubscriber;
    // The most recent CarInfo
    private CarInfo carInfo;
    // Lock for synchronizing accessing and receiving the current CarInfo
    private final Object carInfoMutex = new Object();

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
    // Listener for sensorData
    private ArrayList<MessageListener<SensorData>> sensorDataListeners;
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
        this.sensorDataListeners = new ArrayList<>();
        this.odometryListeners = new ArrayList<>();
        this.carTelemetryWrapperListeners = new ArrayList<>();

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
     * Adds an SensorData listener.
     * @param l The listener
     * @return True on success
     */
    public boolean addSensorDataListener(MessageListener<SensorData> l) {
        return sensorDataListeners.add(l);
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

    public void setCurrentCommand(Command currentCommand) {
        this.currentCommand = currentCommand;
    }

    public Command getCurrentCommand() {
        return currentCommand;
    }

    public void setPublishCommands(boolean publishCommands) {
        this.publishCommands = publishCommands;
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

        if (commandPublisher != null){
            commandPublisher.publish(currentCommand);
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
        if (currentCommand != null) {

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

            currentCommand.setMotorLevel((short) (linearVelocityX * scale));
            currentCommand.setSteeringLevel((short) -angularVelocityZ);
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
        String moveTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_joystick_topic_edittext_key),
                        context.getString(R.string.joy_topic));

        String laserScanTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_laserscan_topic_edittext_key),
                        context.getString(R.string.laser_scan_topic));

        String sensorDataTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_sensorData_topic_edittext_key),
                        context.getString(R.string.sensorData_topic));

        String odometryTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_odometry_topic_edittext_key),
                        context.getString(R.string.odometry_topic));

        String carInfoTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_carInfo_topic_edittext_key),
                        context.getString(R.string.carInfo_topic));

        String imageTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_camera_topic_edittext_key),
                        context.getString(R.string.camera_topic));

        // Refresh the Command Publisher
        if (commandPublisher == null
                || !moveTopic.equals(commandPublisher.getTopicName().toString())) {

            if (publisherTimer != null) {
                publisherTimer.cancel();
            }

            if (commandPublisher != null) {
                commandPublisher.shutdown();
            }

            // Start the Command publisher
            commandPublisher = connectedNode.newPublisher(moveTopic, Command._TYPE);
            currentCommand = commandPublisher.newMessage();

            publisherTimer = new Timer();
            publisherTimer.schedule(new TimerTask() {
                @Override
                public void run() { if (publishCommands) {
                    commandPublisher.publish(currentCommand);
                }
                }
            }, 0, 80);
            publishCommands = false;
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

        // Refresh the sensorData Subscriber
        if (sensorDataSubscriber == null
                || !sensorDataTopic.equals(sensorDataSubscriber.getTopicName().toString())) {

            if (sensorDataSubscriber != null)
                sensorDataSubscriber.shutdown();

            // Start the sensorData subscriber
            sensorDataSubscriber = connectedNode.newSubscriber(sensorDataTopic, SensorData._TYPE);
            sensorDataSubscriber.addMessageListener(new MessageListener<SensorData>() {
                @Override
                public void onNewMessage(SensorData sensorData) {
                    setSensorData(sensorData);
                }
            });
        }

        // Refresh the CarInfo Subscriber
        if (carInfoSubscriber == null
                || !carInfoTopic.equals(carInfoSubscriber.getTopicName().toString())) {

            if (carInfoSubscriber != null)
                carInfoSubscriber.shutdown();

            // Start the CarInfo subscriber
            carInfoSubscriber = connectedNode.newSubscriber(carInfoTopic, CarInfo._TYPE);
            carInfoSubscriber.addMessageListener(new MessageListener<CarInfo>() {
                @Override
                public void onNewMessage(CarInfo carInfo) {
                    setCarInfo(carInfo);
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

        if (commandPublisher != null) {
            commandPublisher.shutdown();
        }

        if (laserScanSubscriber != null) {
            laserScanSubscriber.shutdown();
        }

        if(odometrySubscriber != null){
            odometrySubscriber.shutdown();
        }

        if(sensorDataSubscriber != null){
            sensorDataSubscriber.shutdown();
        }

        if(carInfoSubscriber != null){
            carInfoSubscriber.shutdown();
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
     * @return The most recently received Odometry.
     */
    @SuppressWarnings("unused")
    public Odometry getOdometry() {
        synchronized (odometryMutex) {
            return odometry;
        }
    }

    /**
     * @return The most recently received SensorData.
     */
    @SuppressWarnings("unused")
    public SensorData getSensorData() {
        synchronized (sensorDataMutex) {
            return sensorData;
        }
    }

    /**
     * Sets the current SensorData.
     * @param sensorData The SensorData
     */
    public void setSensorData(SensorData sensorData){
        synchronized (sensorDataMutex){
            this.sensorData = sensorData;

            // Call the listener callbacks
            for (MessageListener<SensorData> listener: sensorDataListeners) {
                listener.onNewMessage(sensorData);
            }
            // set sensorData in carTelemetryWrapper
            carTelemetryWrapper.setSensorData(sensorData);
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

            // Record position TODO this should be moved to setCarInfo() but that's not being called for some reason
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

    /**
     * Sets the current CarInfo.
     * @param carInfo The CarInfo
     */
    public void setCarInfo(CarInfo carInfo){
        synchronized (carInfoMutex){
            this.carInfo = carInfo;
            carTelemetryWrapper.setCarInfo(carInfo);
            // set carTelemetryWrapper
            setCarTelemetryWrapper(carTelemetryWrapper);
        }

        //Log.d("RobotController", "CarInfo Set");
//        // Record position
//        if (startPos == null) {
//            startPos = carInfo.getPosition();
//        } else {
//            currentPos = carInfo.getPosition();
//        }
//        rotation = carInfo.getOrientation();
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