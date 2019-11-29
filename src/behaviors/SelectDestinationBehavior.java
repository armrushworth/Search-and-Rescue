package behaviors;

import java.util.ArrayList;

import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.HungarianMethod;
import main.PathFinder;
import monitors.PCMonitor;

public class SelectDestinationBehavior implements Behavior {
	private boolean suppressed = false;
	private HungarianMethod hungarianMethod;
	private PathFinder pathFinder;
	private PCMonitor pcMonitor;
	private Grid grid;
	private ArrayList<Cell> route;
	private ArrayList<Cell> path;
	private Cell destination;
	
	public SelectDestinationBehavior(PCMonitor pcMonitor, Grid grid, ArrayList<Cell> route, ArrayList<Cell> path) {
		pathFinder = new PathFinder(grid.getGrid());
		this.pcMonitor = pcMonitor;
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
		ArrayList<Cell> potentialVictims = grid.getPotentialVictims();
		
		if (route.isEmpty() && !potentialVictims.isEmpty()) {
			hungarianMethod = new HungarianMethod(grid, potentialVictims);
			route = hungarianMethod.findRoute();
		}
		if (potentialVictims.isEmpty()) {
			pathFinder.findPath(path, grid.getCurrentCell(), grid.getCell(0, 0));
		}
		if (path.isEmpty()) {
			destination = route.remove(0);
			pathFinder.findPath(path, grid.getCurrentCell(), destination);

			pcMonitor.setPath(path);
			pcMonitor.setDestination(destination);
		}
	}
}