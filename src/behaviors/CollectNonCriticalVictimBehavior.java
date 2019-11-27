package behaviors;

import lejos.robotics.subsumption.Behavior;

public class CollectNonCriticalVictimBehavior implements Behavior {
	private boolean suppressed = false;
	
	public CollectNonCriticalVictimBehavior() {
		
	}
	
	public final void suppress() {
		suppressed = true;
	}
	
	public final boolean takeControl() {
		// TODO take control when at critical victim or non-critical victim when all other critical victims have been found
		return false;
	}

	public final void action() {
		
	}
}