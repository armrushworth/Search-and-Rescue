package monitors;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.*;
import main.Grid;

public class PilotMonitor extends Thread {
	private volatile boolean running = true;
	private GraphicsLCD lcd;
	private Grid grid;

	public PilotMonitor(Grid grid) {
		this.setDaemon(true);
		lcd = LocalEV3.get().getGraphicsLCD();
		this.grid = grid;
	}

	public void run() {
		while (running) {
			lcd.clear();
			lcd.setFont(Font.getSmallFont());
			updateMap();

			try {
				sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				running = false;
			}
		}
	}

	// draw the occupancy grid used to map the environment
	private void updateMap() {
		int rowCounter = 1;
		lcd.drawString("+---+---+---+---+---+---+", 0, 0, 0);

		for (int y = grid.getGridHeight() - 1; y >= 0; y--) {
			String rowString = "|";
			for (int x = 0; x < grid.getGridWidth(); x++) {
				// display the robot's current position
				if (grid.getCurrentCell() == grid.getCell(x, y)) {
					rowString += " R ";
					
				// display if the cell is occupied
				} else if (grid.getCell(x, y).isBlocked()) {
					rowString += "|||";
				
				// display if the cell is a potential victim
				} else if (grid.getCell(x, y).getStatus() == 1) {
					rowString += " ? ";
					
				// display if the cell is a potential victim
				} else if (grid.getCell(x, y).getStatus() == 2) {
					rowString += " P ";
					
				// display if the cell is empty
				} else {
					rowString += "   ";
				}
				rowString += "|";
			}
			
			lcd.drawString(rowString, 0, (rowCounter) * 10, 0);
			lcd.drawString("+---+---+---+---+---+---+", 0, (rowCounter + 1) *10, 0);
			rowCounter += 2;
		}
	}

	public final void terminate() {
		running = false;
	}
}
