package behaviors;

import java.awt.Point;
import java.util.ArrayList;

import colourSensorModel.ColourSampleChart;
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
		int cellCount = 1;
		Point coordinates = path.get(0).getCoordinates();
		if (coordinates.x - grid.getCurrentCell().getCoordinates().x > 0) {
			while (path.size() > cellCount && path.get(cellCount).getCoordinates().x > coordinates.x && path.get(cellCount).getCoordinates().y == coordinates.y) {
				cellCount++;
			}
			heading = HEADING_EAST;
		} else if (coordinates.x - grid.getCurrentCell().getCoordinates().x < 0) {
			while (path.size() > cellCount && path.get(cellCount).getCoordinates().x < coordinates.x && path.get(cellCount).getCoordinates().y == coordinates.y) {
				cellCount++;
			}
			heading = HEADING_WEST;
		} else if (coordinates.y - grid.getCurrentCell().getCoordinates().y > 0) {
			while (path.size() > cellCount && path.get(cellCount).getCoordinates().x == coordinates.x && path.get(cellCount).getCoordinates().y > coordinates.y) {
				cellCount++;
			}
			heading = HEADING_NORTH;
		} else {
			while (path.size() > cellCount && path.get(cellCount).getCoordinates().x == coordinates.x && path.get(cellCount).getCoordinates().y < coordinates.y) {
				cellCount++;
			}
			heading = HEADING_SOUTH;
		}
		rotate(heading);
		
		Cell destination = path.remove(0);
		for (int i = 1; i < cellCount; i++) {
			destination = path.remove(0);
		}
		
		myPilot.setLinearSpeed(10);
		myPilot.travel(25 * cellCount - 20);
		
		myPilot.setLinearSpeed(1);
		myPilot.travel(25, true);
		
		boolean hasBothCrossedLine = false;
		while (myPilot.isMoving() ) {
			Boolean leftonline = leftOnLine();
			Boolean rightonline = rightOnLine();
			if (!hasBothCrossedLine && leftonline && !rightonline) {
				myPilot.stop();
				myRobot.getRightWheel().getMotor().setSpeed(70);
				myRobot.getRightWheel().getMotor().forward();
				while (myRobot.getRightWheel().getMotor().isMoving()) {
					if (rightOnLine()) {
						myRobot.getRightWheel().getMotor().stop();
					}
				}
				hasBothCrossedLine = true;
			} else if (!hasBothCrossedLine && rightonline && !leftonline) {
				myPilot.stop();
				myRobot.getLeftWheel().getMotor().setSpeed(70);
				myRobot.getLeftWheel().getMotor().forward();
				while (myRobot.getLeftWheel().getMotor().isMoving()) {
					if (leftOnLine()) {
						myRobot.getLeftWheel().getMotor().stop();
					}
				}
				hasBothCrossedLine = true;
			}
			
			if (hasBothCrossedLine || (leftonline && rightonline)) {
				myPilot.stop();
				
				// continue travel
				myPilot.setLinearSpeed(10);
				myPilot.travel(16.5, true);
				while (myPilot.isMoving()) {
					if (myRobot.getDistance() < 5) {
						myPilot.stop();
					}
				}
			}
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
		double initial = myRobot.getAngle();
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
		return csc.findColor(myRobot.getLeftColor(), true).equals("Black");
	}
	
	public boolean rightOnLine() {
		return csc.findColor(myRobot.getRightColor(), false).equals("Black");
	}
}
