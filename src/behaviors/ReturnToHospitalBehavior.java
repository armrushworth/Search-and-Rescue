package behaviors;

import lejos.robotics.subsumption.Behavior;

public class ReturnToHospitalBehavior implements Behavior {
	private boolean suppressed = false;
	
	public ReturnToHospitalBehavior() {
		
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