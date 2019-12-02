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
		
		myPilot.setLinearAcceleration(5);
		myPilot.setLinearSpeed(5);
		
		Cell destination = path.get(0);
		for (int i = 0; i < cellCount; i++) {
			destination = path.remove(0);
			
			if (i == cellCount - 1) {
				myPilot.travel(3.5);
			} else {
				myPilot.travel(25);
				coordinates = path.get(0).getCoordinates();
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
			}
		}
		
		boolean hasBothCrossedLine = false;
		while (!hasBothCrossedLine) {
			myPilot.setLinearSpeed(1);
			myPilot.travel(8.5, true);
			
			while (myPilot.isMoving() ) {
				Boolean leftonline = leftOnLine();
				Boolean rightonline = rightOnLine();
				RegulatedMotor wheel = null;
				if (!hasBothCrossedLine) {
					if (leftonline && !rightonline) {
						wheel = myRobot.getRightWheel().getMotor();
					} else if (rightonline && !leftonline) {
						wheel = myRobot.getLeftWheel().getMotor();
					}
					if (wheel != null) {
						myPilot.stop();
						wheel.setSpeed(50);
						wheel.forward();
						while (wheel.isMoving()) {
							if (leftOnLine()) {
								wheel.stop();
							}
						}
						hasBothCrossedLine = true;
					}
				}
				
				if (hasBothCrossedLine || (leftonline && rightonline)) {
					myPilot.stop();
					hasBothCrossedLine = true;
					
					if (myRobot.getAngle() >= -45 && myRobot.getAngle() <= 45) {
						myRobot.resetGyro();
					}
					
					// continue travel
					myPilot.setLinearSpeed(5);
					myPilot.travel(16.5, true);
					while (myPilot.isMoving()) {
						if (myRobot.getDistance() < 5) {
							myPilot.stop();
						}
					}
				}
			}
			if (!hasBothCrossedLine) {
				myPilot.setLinearSpeed(5);
				myPilot.travel(-18.5);
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
