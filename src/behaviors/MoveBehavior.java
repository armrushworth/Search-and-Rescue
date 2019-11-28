package behaviors;
import java.awt.*;
import java.util.*;

import colourSensorModel.ColourSampleChart;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.*;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.PathFinder;
import main.PilotRobot;
import monitors.PCMonitor;

public class MoveBehavior implements Behavior {
	private boolean suppressed = false;
	private PilotRobot myRobot;
	private MovePilot myPilot;
	private OdometryPoseProvider opp;
	private Grid grid;
	private ArrayList<Cell> path;
	private ColourSampleChart csc;
	
	private static final int HEADING_NORTH = 0;
	private static final int HEADING_WEST = -90;
	private static final int HEADING_EAST = 90;
	private static final int HEADING_SOUTH = 180;
	
	public MoveBehavior(PilotRobot myRobot, Grid grid, ArrayList<Cell> path, ColourSampleChart csc) {
		this.myRobot = myRobot;
		myPilot = myRobot.getPilot();
		this.csc = csc;
		
		this.grid = grid;
		this.path = path;
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		return true;
	}

	public final void action() {
		suppressed = false;
		
		Cell nextStep = path.remove(0);
		followPath(nextStep.getCoordinates());
	}
	
	public void rotate(int heading) {
		float gyroAngle = myRobot.getAngle();
		myPilot.rotate(getHeadingError(heading));
		if (getHeadingError(heading) <= -1 || getHeadingError(heading) >= 1) {
			rotate(heading);
		}
	}
	
	public void followPath(Point coordinates) {		
		if (coordinates.x - opp.getPose().getX() > 0) {
			rotate(HEADING_EAST);
		} else if (coordinates.x - opp.getPose().getX() < 0) {
			rotate(HEADING_WEST);
		} else if (coordinates.y - opp.getPose().getY() > 0) {
			rotate(HEADING_NORTH);
		} else {
			rotate(HEADING_SOUTH);
		}
		
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
