package behaviors;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
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
	private ArrayList<Cell> nonUrgentVictims;
	private Grid grid;
	private Socket ac;
	
	public ExitBehavior(PilotRobot myRobot, Socket agentClient, PilotMonitor pilotMonitor, PCMonitor pcMonitor, Grid grid) {
		this.myRobot = myRobot;
		this.pilotMonitor = pilotMonitor;
		this.pcMonitor = pcMonitor;
		this.grid = grid;
		this.ac = agentClient;
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		potentialVictims = grid.getPotentialVictims();
		nonUrgentVictims = grid.getNonUrgentVictims();
		return potentialVictims.isEmpty() && nonUrgentVictims.isEmpty() && grid.getCurrentCell() == grid.getCell(0, 0);
	}

	public final void action() {
		suppressed = false;
		
		Sound.beepSequence();
		BrickFinder.getLocal().getLED().setPattern(1);
		Button.waitForAnyPress();
		
		pilotMonitor.terminate();
		myRobot.closeRobot();
		if (pcMonitor != null) {
			pcMonitor.terminate();
		}
		try {
			ac.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
