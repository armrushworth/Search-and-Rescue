package behaviors;

import java.util.ArrayList;

import colourSensorModel.ColourSampleChart;
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
	
	public SelectDestinationBehavior(PilotRobot myRobot, PCMonitor pcMonitor, ColourSampleChart csc, Grid grid, ArrayList<Cell> route, ArrayList<Cell> path) {
		pathFinder = new PathFinder(grid.getGrid());
		this.myRobot = myRobot;
		this.pcMonitor = pcMonitor;
		this.csc = csc;
		this.grid = grid;
		this.route = route;
		this.path = path;
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
		
		// reached a potential victim
		if (potentialVictims.contains(grid.getCurrentCell())) {
			myRobot.setRGBMode();
			leftColor = csc.findColor(myRobot.getLeftColor(), true);
			rightColor = csc.findColor(myRobot.getRightColor(), false);
			if ((leftColor.equals("White") || leftColor.equals("Yellow")) && (rightColor.equals("White") || rightColor.equals("Yellow"))) {
				// no victim
				grid.getCurrentCell().setStatus(0);
			} else if (leftColor.equals("Cyan") && rightColor.equals("Cyan")) {
				// non-urgent victim
				grid.getCurrentCell().setStatus(2);
			} else if (leftColor.equals("Burgundy") && rightColor.equals("Burgundy")) {
				// urgent victim
				grid.getCurrentCell().setStatus(3);
				route.clear();
				pathFinder.findPath(path, grid.getCurrentCell(), grid.getCell(0, 0));
				destination = grid.getCell(0, 0);
			}
		}
		
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
						grid.getCurrentCell().setStatus(0);
						route.clear();
						
					// travel to non-urgent victim
					} else {
						hungarianMethod = new HungarianMethod(grid, nonUrgentVictims);
						route = hungarianMethod.findRoute();
					}
				}
				
			// dropped urgent victims off at this hospital
			} else if (route.isEmpty()) {
				hungarianMethod = new HungarianMethod(grid, potentialVictims);
				route = hungarianMethod.findRoute();
			}
			
			destination = route.isEmpty() ? grid.getCell(0, 0) : route.remove(0);
			pathFinder.findPath(path, grid.getCurrentCell(), destination);
		}
		
		pcMonitor.setRoute(route);
		pcMonitor.setPath(path);
		pcMonitor.setDestination(destination);
	}
}