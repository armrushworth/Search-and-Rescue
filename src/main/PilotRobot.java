package main;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.Motor;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.*;

public class PilotRobot {
	private Brick ev3;
	private EV3UltrasonicSensor ultrasonicSensor;
	private EV3GyroSensor gyroSensor;
	private EV3ColorSensor leftColorSensor, rightColorSensor;
	private SampleProvider ultrasonicSampleProvider, gyroSampleProvider, leftColorSampleProvider, rightColorSampleProvider;
	private float[] ultrasonicSample, gyroSample, leftColorSample, rightColorSample;
	private MovePilot pilot;
	private Wheel leftWheel = WheeledChassis.modelWheel(Motor.B, 4.05).offset(-4.9);
	private Wheel rightWheel = WheeledChassis.modelWheel(Motor.D, 4.05).offset(4.9);
	
	// PilotRobot constructor=
	public PilotRobot() {
		ev3 = BrickFinder.getDefault();
		setupColorSensors();
		setupGyroSensor();
		setupUltrasonicSensor();
		setupPilot();
	}
	
	// set up the color sensors
	private void setupColorSensors() {
		leftColorSensor = new EV3ColorSensor(ev3.getPort("S1"));
		leftColorSampleProvider = leftColorSensor.getRGBMode();
		leftColorSample = new float[leftColorSampleProvider.sampleSize()];
		rightColorSensor = new EV3ColorSensor(ev3.getPort("S4"));
		rightColorSampleProvider = rightColorSensor.getRGBMode();
		rightColorSample = new float[rightColorSampleProvider.sampleSize()];
	}
	
	// set up the gyro sensor
	private void setupGyroSensor() {
		gyroSensor = new EV3GyroSensor(ev3.getPort("S3"));
		gyroSampleProvider = gyroSensor.getAngleMode();
		gyroSample = new float[gyroSampleProvider.sampleSize()];
	}

	// set up the ultrasonic sensor
	private void setupUltrasonicSensor() {
		ultrasonicSensor = new EV3UltrasonicSensor(ev3.getPort("S2"));
		ultrasonicSampleProvider = ultrasonicSensor.getDistanceMode();
		ultrasonicSample = new float[ultrasonicSampleProvider.sampleSize()];
	}

	// set up the pilot
	private void setupPilot() {
		Chassis myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);
		pilot = new MovePilot(myChassis);
		pilot.setLinearSpeed(3);
		pilot.setAngularSpeed(45);
	}
	
	// close the bumpers, ultrasonic sensor & gyro sensor
	public final void closeRobot() {
		ultrasonicSensor.close();
		gyroSensor.close();
	}
	
	// get the robots current angle
	public final float getAngle() {
		gyroSampleProvider.fetchSample(gyroSample, 0);
		float gyroAngle = gyroSample[0];
		while (gyroAngle < 0) gyroAngle += 360;
		while (gyroAngle > 360) gyroAngle -= 360;
		return gyroAngle;
	}
	
	// get the brick
	public Brick getBrick() {
		return ev3;
	}
	
	// get the distance from the ultrasonic sensor
	public final float getDistance() {
		ultrasonicSampleProvider.fetchSample(ultrasonicSample, 0);
		return ultrasonicSample[0] * 100;
	}
	
	// get the left color sample
	public final float[] getLeftColor() {
		leftColorSampleProvider.fetchSample(leftColorSample, 0);
		return leftColorSample;
	}
	
	// get the left wheel
	public Wheel getLeftWheel() {
		return leftWheel;
	}
	
	// get the pilot object from the robot
	public final MovePilot getPilot() {
		return this.pilot;
	}
	
	// get the left color sample
	public final float[] getRightColor() {
		rightColorSampleProvider.fetchSample(rightColorSample, 0);
		return rightColorSample;
	}
	
	// get the right wheel
	public Wheel getRightWheel() {
		return rightWheel;
	}
	
	// get the ultrasonic sensor
	public final EV3UltrasonicSensor getUltrasonicSensor() {
		return ultrasonicSensor;
	}
	
	// reset the gyro sensor
	public final void resetGyro() {
		gyroSensor.reset();
	}
}
