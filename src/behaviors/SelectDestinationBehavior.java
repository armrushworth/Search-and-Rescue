package behaviors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import colourSensorModel.ColourSampleChart;
import lejos.hardware.BrickFinder;
import lejos.hardware.LED;
import lejos.hardware.Sound;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.HungarianMethod;
import main.PathFinder;
import main.PilotRobot;
import monitors.PCMonitor;

public class SelectDestinationBehavior implements Behavior {
	private boolean suppressed = false;
	private HungarianMethod hungarianMethod;
	private PathFinder pathFinder;
	private PilotRobot myRobot;
	private PCMonitor pcMonitor;
	private ColourSampleChart csc;
	private Grid grid;
	private ArrayList<Cell> potentialVictims;
	private ArrayList<Cell> nonUrgentVictims;
	private ArrayList<Cell> route;
	private ArrayList<Cell> path;
	private Cell destination;
	private String leftColor;
	private String rightColor;
	private Socket agentClient;
	private LED led;
	
	public SelectDestinationBehavior(PilotRobot myRobot, Socket agentClient, PCMonitor pcMonitor, ColourSampleChart csc, Grid grid, ArrayList<Cell> route, ArrayList<Cell> path) {
		pathFinder = new PathFinder(grid.getGrid());
		this.myRobot = myRobot;
		this.pcMonitor = pcMonitor;
		this.agentClient = agentClient;
		this.csc = csc;
		this.grid = grid;
		this.route = route;
		this.path = path;
		this.led = BrickFinder.getLocal().getLED();
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		return path.isEmpty();
	}

	public final void action() {
		suppressed = false;
		potentialVictims = grid.getPotentialVictims();
		nonUrgentVictims = grid.getNonUrgentVictims();
		if (grid.getCurrentCell().getStatus() == 1) {
			// reached a potential victim
			myRobot.setRGBMode();
			//make sure both sensors read the same colour
			do {
				leftColor = csc.findColor(myRobot.getLeftColor(), true);
				rightColor = csc.findColor(myRobot.getRightColor(), false);
			} while (!leftColor.equals(rightColor));
			//check if the detected colour is a victim
			try {
				//Tell the jason enviroment that a patient is found at X,Y and its colour.
				PrintWriter out = new PrintWriter(agentClient.getOutputStream(), true);
				//Prints out the variables for the jason enviroments new percept. X,Y,C.
				out.println(grid.getCurrentCell().getCoordinates().x + ","
							+ grid.getCurrentCell().getCoordinates().y + ","
							+ leftColor.toLowerCase());
				BufferedReader in = new BufferedReader(new InputStreamReader(agentClient.getInputStream()));
				//wait for agent response
				while (!in.ready()) {
					Thread.sleep(1000);
				}
				String paramedicResponse = in.readLine();
				//check response and respond accordingly
				if (paramedicResponse.equals("NotCritical")) {
					//Not urgent
					Sound.beep();
					grid.getCurrentCell().setStatus(2);
				} else if (paramedicResponse.equals("Critical")) {
				//urgent
					led.setPattern(8);
					Sound.twoBeeps();
					grid.getCurrentCell().setStatus(4);
					route.clear();
					pathFinder.findPath(path, grid.getCurrentCell(), grid.getCell(0, 0));
					destination = grid.getCell(0, 0);
				} else if (paramedicResponse.equals("NoVictim")) {
					//no victim
					led.setPattern(4);
					Sound.buzz();
					grid.getCurrentCell().setStatus(0);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				pcMonitor.sendError(e);
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				pcMonitor.sendError(e);
			} 
		}
//		if (potentialVictims.contains(grid.getCurrentCell())) {
//			myRobot.setRGBMode();
//			leftColor = csc.findColor(myRobot.getLeftColor(), true);
//			rightColor = csc.findColor(myRobot.getRightColor(), false);
//			if ((leftColor.equals("White") || leftColor.equals("Yellow")) && (rightColor.equals("White") || rightColor.equals("Yellow"))) {
//				// no victim
//				Sound.buzz();
//				grid.getCurrentCell().setStatus(0);
//			} else if (leftColor.equals("Cyan") && rightColor.equals("Cyan")) {
//				// non-urgent victim
//				Sound.beep();
//				grid.getCurrentCell().setStatus(2);
//			} else if (leftColor.equals("Burgundy") && rightColor.equals("Burgundy")) {
//				// urgent victim
//				Sound.twoBeeps();
//				grid.getCurrentCell().setStatus(4);
//				route.clear();
//				pathFinder.findPath(path, grid.getCurrentCell(), grid.getCell(0, 0));
//				destination = grid.getCell(0, 0);
//			}
//		}
		
		if (path.isEmpty()) {
			potentialVictims = grid.getPotentialVictims();
			nonUrgentVictims = grid.getNonUrgentVictims();
			
			if (potentialVictims.isEmpty()) {
				// no more victims to pick up
				if (nonUrgentVictims.isEmpty()) {
					pathFinder.findPath(path, grid.getCurrentCell(), grid.getCell(0, 0));
				} else {
					// pick up non-urgent victim
					if (nonUrgentVictims.contains(grid.getCurrentCell())) {
						Sound.twoBeeps();
						led.setPattern(9);
						grid.getCurrentCell().setStatus(3);
						route.clear();
						
					// travel to non-urgent victim
					} else {
						led.setPattern(4);
						hungarianMethod = new HungarianMethod(grid, nonUrgentVictims);
						route = hungarianMethod.findRoute();
					}
				}
				
			// dropped urgent victims off at this hospital
			} else if (route.isEmpty()) {
				led.setPattern(4);
				hungarianMethod = new HungarianMethod(grid, potentialVictims);
				route = hungarianMethod.findRoute();
			}
			
			destination = route.isEmpty() ? grid.getCell(0, 0) : route.remove(0);
			pathFinder.findPath(path, grid.getCurrentCell(), destination);
		}
		//wipe screen
	    for (int i = 0; i < 8; i++) {
	    	System.out.println("");
	    }
		pcMonitor.setRoute(route);
		pcMonitor.setPath(path);
		pcMonitor.setDestination(destination);
	}
}