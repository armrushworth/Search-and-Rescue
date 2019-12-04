package behaviors;

import java.awt.Point;
import java.util.ArrayList;

import colourSensorModel.ColourSampleChart;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.PilotRobot;

public class MoveBehavior implements Behavior {
	private boolean suppressed = false;
	private PilotRobot myRobot;
	private MovePilot myPilot;
	private Grid grid;
	private ArrayList<Cell> path;
	private ColourSampleChart csc;
	
	private final int HEADING_NORTH = 0;
	private final int HEADING_WEST = -90;
	private final int HEADING_EAST = 90;
	private final int HEADING_SOUTH = 180;
	
	public MoveBehavior(PilotRobot myRobot, Grid grid, ArrayList<Cell> path, ColourSampleChart csc) {
		this.myRobot = myRobot;
		myPilot = myRobot.getPilot();
		this.grid = grid;
		this.path = path;
		this.csc = csc;
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		return true;
	}

	public final void action() {
		suppressed = false;
		
		int heading;
		Point coordinates = path.get(0).getCoordinates();
		if (coordinates.x - grid.getCurrentCell().getCoordinates().x > 0) {
			heading = HEADING_EAST;
		} else if (coordinates.x - grid.getCurrentCell().getCoordinates().x < 0) {
			heading = HEADING_WEST;
		} else if (coordinates.y - grid.getCurrentCell().getCoordinates().y > 0) {
			heading = HEADING_NORTH;
		} else {
			heading = HEADING_SOUTH;
		}
		rotate(heading);
		myRobot.setHeading(heading);
		
		Cell destination = path.remove(0);
		
		myRobot.setColorIDMode();
		myPilot.setLinearSpeed(5);
		
		if (grid.getCurrentCell().getStatus() != 4) {
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
					
					myRobot.resetGyro();
					
					// continue travel
					myPilot.travel(16.5, true);
					if (myRobot.getDistance() < 5) {
						myPilot.stop();
					}
				}
			}
		} else {
			myRobot.resetGyro();
			myPilot.travel(25);
		}
		
		// set current cell of grid object
		grid.setCurrentCell(grid.getCell(destination.getCoordinates().x, destination.getCoordinates().y));
	}

	public void rotate(int heading) {
		myPilot.rotate(getHeadingError(heading));
		double remainingHeadingError = getHeadingError(heading);
		if (remainingHeadingError <= -1 || remainingHeadingError >= 1) {
			rotate(heading);
		}
	}
	
	/**
	 * Calculates the rotation required to be heading in the correct direction for a given destination.
	 * @param destination the heading of the destination from the current position
	 * @return the amount of rotation required
	 */
	public double getHeadingError(int destination) {
		double initial = myRobot.getAngle() + myRobot.getHeading();
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
