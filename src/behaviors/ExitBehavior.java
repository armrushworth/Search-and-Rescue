package behaviors;

import java.util.ArrayList;

import lejos.hardware.Button;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.Grid;
import main.PilotRobot;
import monitors.PCMonitor;
import monitors.PilotMonitor;

public class ExitBehavior implements Behavior{
	
	private boolean suppressed = false;
	private PilotRobot myRobot;
	private PilotMonitor pilotMonitor;
	private PCMonitor pcMonitor = null;
	private ArrayList<Cell> potentialVictims;
	private Grid grid;
	
	public ExitBehavior(PilotRobot myRobot, PilotMonitor pilotMonitor, PCMonitor pcMonitor, Grid grid) {
		this.myRobot = myRobot;
		this.pilotMonitor = pilotMonitor;
		this.pcMonitor = pcMonitor;
		this.grid = grid;
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		potentialVictims = grid.getPotentialVictims();
		return potentialVictims.isEmpty() && grid.getCurrentCell() == grid.getCell(0, 0);
	}

	public final void action() {
		suppressed = false;
		
		myRobot.getBrick().getAudio().systemSound(0);
		while (Button.ESCAPE.isDown()) {
			pilotMonitor.terminate();
			myRobot.closeRobot();
			if (pcMonitor != null) {
				pcMonitor.terminate();
			}
			System.exit(0);
		}
	}
}
