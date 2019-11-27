package behaviors;
import java.awt.*;
import java.util.*;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.*;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.PathFinder;
import main.PilotRobot;
import monitors.PCMonitor;

public class SearchLocationBehavior implements Behavior {
	private boolean suppressed = false;
	private PilotRobot myRobot;
	private MovePilot myPilot;
	private OdometryPoseProvider opp;
	private Grid grid;
	private PathFinder pathFinder;
	private ArrayList<Cell> path = new ArrayList<Cell>();
	private PCMonitor pcMonitor;
	
	private static final int HEADING_NORTH = 0;
	private static final int HEADING_WEST = -90;
	private static final int HEADING_EAST = 90;
	private static final int HEADING_SOUTH = 180;
	
	public SearchLocationBehavior(PilotRobot myRobot, Grid grid, PCMonitor pcMonitor) {
		this.myRobot = myRobot;
		myPilot = myRobot.getPilot();
		opp = myRobot.getOdometryPoseProvider();
		
		this.grid = grid;
		pathFinder = new PathFinder(grid.getGrid());
		
		this.pcMonitor = pcMonitor;
	}
	
	public SearchLocationBehavior(PilotRobot myRobot, Grid grid) {
		this.myRobot = myRobot;
		myPilot = myRobot.getPilot();
		opp = myRobot.getOdometryPoseProvider();
		
		this.grid = grid;
		pathFinder = new PathFinder(grid.getGrid());
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		return true;
	}

	public final void action() {
		suppressed = false;
		Cell destination = null;
		
		// select a destination and build the path
		if (path.isEmpty()) {
			// TODO calculate destination
			destination = grid.getCell(5, 5);
			pcMonitor.setPath(path);
			path = pathFinder.findPath(grid.getCurrentCell(), destination);
			pcMonitor.setDestination(destination);
		}
			
		if (path != null) {
			// move to the next step
			Cell nextStep = path.remove(0);
			followPath(nextStep.getCoordinates());
		} else {
			destination.setIsBlocked();
		}
	}
	
	// if the gyroscope has accumilated an angle greater than 360 or less then -360 its returns a reset value.
	public float correctedGyroReading() {
		float gyroAngle = myRobot.getAngle();
		if (gyroAngle > 0) {
			if (gyroAngle/360 > 1) {
				gyroAngle -= 360*((int) gyroAngle/360);
			}
		} else {
			if (gyroAngle/360 < -1) {
				gyroAngle += 360*((int) gyroAngle/360);
			}
		}
		if (gyroAngle > 180) {
			gyroAngle = gyroAngle - 360;
		}
		if (gyroAngle < -180) {
			gyroAngle = gyroAngle + 360;
		}
		return gyroAngle;
	}
	
	/**
	 * Moves towards the coordinates of a neighbouring cell.
	 * @param x the x-coordinate of the cell to travel towards
	 * @param y the y-coordinate of the cell to travel towards
	 */
	public void followPath(Point coordinates) {
		float gyroAngle;
		OdometryPoseProvider opp = myRobot.getOdometryPoseProvider();
		//reset gyroScope since it accumilates degrees in direction of rotation.
		if (coordinates.x - opp.getPose().getX() > 0) {
			myPilot.rotate(getHeadingError(HEADING_EAST));
			gyroAngle = correctedGyroReading();
			
			//correct for error
			if (!(gyroAngle > 89 && gyroAngle < 91)){
				if (gyroAngle > 0) {
					myPilot.rotate(90-gyroAngle);
				} else {
					myPilot.rotate(-270-gyroAngle);
				}
			}
			
			opp.setPose(new Pose(opp.getPose().getX(), opp.getPose().getY(), HEADING_EAST));
		} else if (coordinates.x - opp.getPose().getX() < 0) {
			myPilot.rotate(getHeadingError(HEADING_WEST));
			gyroAngle = correctedGyroReading();
			
			//correct for error
			if (!(gyroAngle < -89 && gyroAngle > -91)){
				if (gyroAngle > 0) {
					myPilot.rotate(270-gyroAngle);
				} else {
					myPilot.rotate(-90-gyroAngle);
				}
			}
			
			opp.setPose(new Pose(opp.getPose().getX(), opp.getPose().getY(), HEADING_WEST));
		} else if (coordinates.y - opp.getPose().getY() > 0) {
			myPilot.rotate(getHeadingError(HEADING_NORTH));
			gyroAngle = correctedGyroReading();
			
			//correct for error
			if (!(gyroAngle < 1 && gyroAngle > -1 )){
				if (gyroAngle > 0) {
					myPilot.rotate(0-gyroAngle);
				} else {
					myPilot.rotate(-360-gyroAngle);
				}
			}
			
			opp.setPose(new Pose(opp.getPose().getX(), opp.getPose().getY(), HEADING_NORTH));
		} else {
			myPilot.rotate(getHeadingError(HEADING_SOUTH));
			gyroAngle = correctedGyroReading();
			
			//correct for error
			if (!(gyroAngle > 179 ||gyroAngle < -179 )){
				if (gyroAngle > 0) {
					myPilot.rotate(180-gyroAngle);
				} else {
					myPilot.rotate(-180-gyroAngle);
				}
			}
			
			opp.setPose(new Pose(opp.getPose().getX(), opp.getPose().getY(), HEADING_SOUTH));
		}
		
		myPilot.travel(25, true);
		
		boolean hasBothCrossedLine = false;
		while (myPilot.isMoving() ) {
			if (!hasBothCrossedLine && leftOnLine() && !rightOnLine()) {
				myPilot.stop();
				myRobot.getRightWheel().getMotor().setSpeed(100);
				myRobot.getRightWheel().getMotor().forward();
				while (myRobot.getRightWheel().getMotor().isMoving()) {
					if (rightOnLine()) {
						myRobot.getRightWheel().getMotor().stop();
					}
				}
			} else if (!hasBothCrossedLine && rightOnLine() && !leftOnLine()) {
				myPilot.stop();
				myRobot.getLeftWheel().getMotor().setSpeed(100);
				myRobot.getLeftWheel().getMotor().forward();
				while (myRobot.getLeftWheel().getMotor().isMoving()) {
					if (leftOnLine()) {
						myRobot.getLeftWheel().getMotor().stop();
					}
				}
			}
			
			if (!hasBothCrossedLine && leftOnLine() && rightOnLine()) {
				myPilot.stop();
				hasBothCrossedLine = true;
				
				// continue travel
				myPilot.travel(16.5, true);
				if (myRobot.getDistance() < 5) {
					myPilot.stop();
				}
			}
		}
		
		// set pose and current cell of grid object
		opp.setPose(new Pose(coordinates.x, coordinates.y, opp.getPose().getHeading()));
		grid.setCurrentCell(grid.getCell(coordinates.x, coordinates.y));
	}

	/**
	 * Calculates the rotation required to be heading in the correct direction for a given destination.
	 * @param destination the heading of the destination from the current position
	 * @return the amount of rotation required
	 */
	public double getHeadingError(int destination) {
		double initial = opp.getPose().getHeading();
		double diff = destination - initial;
		double absDiff = Math.abs(diff);

		if (absDiff <= 180) {
			return absDiff == 180 ? absDiff : diff;
		} else if (destination > initial) {
			return absDiff - 360;
		} else {
			return 360 - absDiff;
		}
	}
	
	public boolean leftOnLine() {
		return myRobot.getLeftColor()[0] == 7;
	}
	
	public boolean rightOnLine() {
		return myRobot.getRightColor()[0] == 7;
	}
}
