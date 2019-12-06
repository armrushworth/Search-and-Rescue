package behaviors;

import java.awt.Point;
import java.util.ArrayList;

import colourSensorModel.ColourSampleChart;
import lejos.hardware.motor.Motor;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.PilotRobot;
import particleFilter.ParticleFilter;

public class Localiser {
	private boolean suppressed = false;
	private PilotRobot myRobot;
	private MovePilot myPilot;
	private Grid grid;
	private ColourSampleChart csc;
	private Cell[] obstacles;
	private int currentPosition;
	
	public Localiser(PilotRobot myRobot, Cell[] obstacles, Grid grid, ColourSampleChart csc) {
		this.myRobot = myRobot;
		this.myPilot = myRobot.getPilot();
		this.grid = grid;
		this.csc = csc;
		this.obstacles = obstacles;
	}
	



	public void localise() {
		String currentColour = csc.findColor(myRobot.getLeftColor(), true);
		if (currentColour.equals("yellow")) {
			grid.setCurrentCell(grid.getCell(0, 0));
			return;
		} else if (currentColour.equals("green")) {
			grid.setCurrentCell(grid.getCell(5, 0));
			return;
		}
		//the width and height are incremented by 1 since we want to include the walls as landmarks
		ArrayList<Point> landmarks = new ArrayList<Point>();
		for (int i = 0; i < 7; i++) {
			landmarks.add(new Point(0,i));
			landmarks.add(new Point(6,i));
			landmarks.add(new Point(i,0));
			landmarks.add(new Point(i,6));
		}
		for (int i = 0; i < this.obstacles.length; i ++) {
			int x = this.obstacles[i].getCoordinates().x;
			int y = this.obstacles[i].getCoordinates().y;
			landmarks.add(new Point(x,y));
		}
		Point landmarks2[] = new Point[landmarks.size()];
		landmarks2 = landmarks.toArray(landmarks2);
		//i set it to 7 by 7  so i can make the walls landmarks.
		ParticleFilter pf = new ParticleFilter(landmarks2, 7, 7);
		myRobot.setHeading(0);
		while (true) {
			pf.resample(getSensorSample());
			sensorRotateLeft();
			pf.move(-90, 0);
			pf.resample(getSensorSample());
			sensorRotateRight();
			pf.move(180, 0);
			pf.resample(getSensorSample());
			sensorRotateCentre();
			pf.move(-90, 0);
			rotate(-180);
			myRobot.setHeading(myRobot.getHeading() - 180);
			pf.move(180, 0);
			pf.resample(getSensorSample());
			sensorRotateLeft();
			pf.move(-90, 0);
			pf.resample(getSensorSample());
			sensorRotateRight();
			pf.move(180, 0);
			pf.resample(getSensorSample());
			sensorRotateCentre();
			rotate(-180);
			myRobot.setHeading(myRobot.getHeading() -180);
			pf.move(-270, 0);
			pf.calculateBestCell();
			System.out.println(pf.getBestCell() + " " + pf.getBestNumOfParticles());
			if (pf.getBestNumOfParticles() > pf.totalParticles*0.9) {
				String coords = pf.getBestCell();
				int x = Integer.parseInt(coords.split(",")[0]);
				int y = Integer.parseInt(coords.split(",")[1]);
				grid.setCurrentCell(grid.getCell(x-1, y-1));
				myRobot.setHeading(Integer.parseInt(coords.split(",")[2]));
				return;
			} else {
				while (myRobot.getDistance() < 25) {
					System.out.println("rotate");
					rotate(-90);
					myRobot.setHeading(myRobot.getHeading() -90);
					pf.move(-90, 0);
					
				}
				myPilot.travel(25);
				pf.move(0, 1);
			}
		}
	}
	
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
	
  private double circle(double num, double length) {
	    while (num > length - 1) {
	      num -= length;
	    }
	    while (num < 0) {
	      num += length;
	    }
	    return num;
	  }
  
	public void rotate(int heading) {
		myPilot.rotate(getHeadingError(heading));
		double remainingHeadingError = getHeadingError(heading);
		if (remainingHeadingError <= -1 || remainingHeadingError >= 1) {
			rotate(heading);
		}
	}
	
	public float getSensorSample() {
		float rawSample = myRobot.getDistance();
		if (Float.isInfinite(rawSample)) {
			myPilot.travel(-4);
			rawSample = myRobot.getDistance();
			myPilot.travel(4);
			if (Float.isInfinite(rawSample)) {
				return 125;
			}
		}
		return rawSample;
	}
	public void sensorRotateLeft() {
		Motor.C.rotateTo(95);
		currentPosition = (int) (myRobot.getAngle() + 90);
	}
	public void sensorRotateRight() {
		Motor.C.rotateTo(-95);
		currentPosition = (int) (myRobot.getAngle() - 90);
	}
	public void sensorRotateCentre() {
		Motor.C.rotateTo(0);
		currentPosition = (int) (myRobot.getAngle());
	}

}
