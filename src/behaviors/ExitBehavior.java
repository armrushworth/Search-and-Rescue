package behaviors;
import java.util.ArrayList;

import lejos.hardware.Button;
import lejos.robotics.subsumption.Behavior;
import main.Cell;
import main.PilotRobot;
import monitors.PCMonitor;
import monitors.PilotMonitor;

public class ExitBehavior implements Behavior{
	
	private boolean suppressed = false;
	private PilotRobot myRobot;
	private ArrayList<Cell> route;
	private PilotMonitor pilotMonitor;
	private PCMonitor pcMonitor = null;
	
	public ExitBehavior(PilotRobot myRobot, ArrayList<Cell> route, PilotMonitor pilotMonitor, PCMonitor pcMonitor) {
		this.myRobot = myRobot;
		this.route = route;
		this.pilotMonitor = pilotMonitor;
		this.pcMonitor = pcMonitor;
	}
	
	public ExitBehavior(PilotRobot myRobot, ArrayList<Cell> route, PilotMonitor pilotMonitor) {
		this.myRobot = myRobot;
		this.route = route;
		this.pilotMonitor = pilotMonitor;
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		// TODO take control when all 3 victims have been found and back at hospital
		return route.isEmpty();
	}

	public final void action() {
		suppressed = false;
		myRobot.getBrick().getAudio().systemSound(0);
		while (Button.ESCAPE.isDown() && !suppressed) {
			pilotMonitor.terminate();
			myRobot.closeRobot();
			if (pcMonitor != null) {
				pcMonitor.terminate();
			}
			System.exit(0);
		}
	}
}
