package behaviors;

import java.util.ArrayList;

import colourSensorModel.ColourSampleChart;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.PilotRobot;

public class ScanVictimBehavior implements Behavior{
	private boolean suppressed = false;
	private PilotRobot myRobot;
	private ColourSampleChart csc;
	private Grid grid;
	private ArrayList<Cell> potentialVictims;
	
	public ScanVictimBehavior(PilotRobot myRobot, ColourSampleChart csc, Grid grid) {
		this.myRobot = myRobot;
		this.csc = csc;
		this.grid = grid;
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		potentialVictims = grid.getPotentialVictims();
		return potentialVictims.contains(grid.getCurrentCell());
	}

	public final void action() {
		suppressed = false;
		
		String leftColor = csc.findColor(myRobot.getLeftColor(), true);
		String rightColor = csc.findColor(myRobot.getRightColor(), false);
		
		if (leftColor.equals("White") && rightColor.equals("White")) {
			// no victim
			grid.getCurrentCell().setStatus(0);
		} else if (leftColor.equals("Cyan") && rightColor.equals("Cyan")) {
			// non-urgent victim
			grid.getCurrentCell().setStatus(2);
		} else if (leftColor.equals("Burgandy") || rightColor.equals("Burgandy")) {
			// urgent victim
			grid.getCurrentCell().setStatus(3);
		}
	}
}
