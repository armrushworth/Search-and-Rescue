package monitors;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import colourSensorModel.ColourSampleChart;
import main.Cell;
import main.Grid;
import main.PilotRobot;

// Used to send data about the robot to a PC client interface.
public class PCMonitor extends Thread {

	//Server socket between robot and client
	private Socket client;
	private Socket errorSocket;

	//checks if thread is running.
	private volatile boolean running = true;

	//Data output stream
	private PrintWriter out;
	private PrintWriter errorDisplay;

	//The actual robot.
	private PilotRobot robot;

	private ArrayList<Cell> potentialVictims;
	
	private Grid grid;
	
	private ColourSampleChart csc;
	
	private ArrayList<Cell> route = new ArrayList<Cell>();
	private Cell destination = null;
	private ArrayList<Cell> path = new ArrayList<Cell>();

	public PCMonitor(Socket client, Socket errorSocket, PilotRobot robot, Grid grid, ArrayList<Cell> potentialVictims, ColourSampleChart csc) {
		this.client = client;
		this.errorSocket = errorSocket;
		this.robot = robot;
		this.grid = grid;
		this.potentialVictims = potentialVictims;
		this.csc = csc;
		
		try {
			out = new PrintWriter(client.getOutputStream(), true);
			errorDisplay = new PrintWriter(errorSocket.getOutputStream(), true);
			
		} catch (IOException e) {
			e.printStackTrace();
			running = false;
		}
	}

	//run the thread
	public void run() {
		while (running) {
			// output victim information
			for (int i = 0; i < 5; i++) {
				if (!potentialVictims.isEmpty()) {
					String potentialVictimsOutput = "";
					potentialVictimsOutput += "(" + potentialVictims.get(i).getCoordinates().x + ", " + potentialVictims.get(i).getCoordinates().y + ") ";
					switch (potentialVictims.get(i).getStatus()) {
						case 1:
							potentialVictimsOutput += "Unknown";
							break;
						case 2:
							potentialVictimsOutput += "Non-urgent victim";
							break;
						case 3:
							potentialVictimsOutput += "Non-urgent victim (collected)";
							break;
						case 4:
							potentialVictimsOutput += "Urgent victim (collected)";
							break;
						default:
							potentialVictimsOutput += "Empty";
					}
					out.println(potentialVictimsOutput);
				} else {
					out.println("null");
				}
			}
			
			// output the destination
			out.println(destination != null ? "(" + destination.getCoordinates().x + ", " + destination.getCoordinates().y + ")" : "null");
			
			// output the path
			if (!path.isEmpty()) {
				String pathOutput = "";
				for (Cell cell : path) {
					pathOutput += "(" + cell.getCoordinates().x + ", " + cell.getCoordinates().y + "), ";
				}
				out.println(pathOutput.substring(0, pathOutput.length() - 2));
			} else {
				out.println(destination != null ? "(" + destination.getCoordinates().x + ", " + destination.getCoordinates().y + ")" : "null");
			}
			
			// output the route
			if (!route.isEmpty()) {
				String routeOutput = destination != null ? "(" + destination.getCoordinates().x + ", " + destination.getCoordinates().y + "), " : "";
				for (Cell cell : route) {
					routeOutput += "(" + cell.getCoordinates().x + ", " + cell.getCoordinates().y + "), ";
				}
				out.println(routeOutput.substring(0, routeOutput.length() - 2));
			} else {
				out.println(destination != null ? "(" + destination.getCoordinates().x + ", " + destination.getCoordinates().y + ")" : "null");
			}
			
			// output sensor information
			out.println(robot.getDistance());
			out.println(robot.getAngle());
			out.println(csc.findColor(robot.getLeftColor(),true));
			out.println(csc.findColor(robot.getRightColor(),false));
			
			// output movement information
			if (robot.getPilot().isMoving()) {
				out.println("Moving");
			} else {
				out.println("Stationary");
			}
			out.println(robot.getPilot().getMovement().getMoveType());
			
			// output the heading
			switch (robot.getHeading()) {
				case 0:
					out.println("North");
					break;
				case 90:
					out.println("East");
					break;
				case -90:
					out.println("West");
					break;
				default:
					out.println("South");
			}
			
			// output the current location of the robot
			out.println(grid.getCurrentCell().getCoordinates().x + "," + grid.getCurrentCell().getCoordinates().y);
			
			// ouptut victim information for the map
			String potentialVictimsOutput = "";
			for (Cell potentialVictim : potentialVictims) {
				potentialVictimsOutput += potentialVictim.toString() + "," + potentialVictim.getStatus();
			}
			out.println(potentialVictimsOutput);
			
			out.flush();
			
			try {
				sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				running = false;
			}
		}
	}
	
	public final void sendError(Exception error) {
			errorDisplay.println(error.getMessage());
	}
	
	public final void setDestination (Cell destination) {
		this.destination = destination;
	}
	
	public final void setPath (ArrayList<Cell> path) {
		this.path = path;
	}
	
	public final void setRoute (ArrayList<Cell> route) {
		this.route = route;
	}
	
	public final void terminate() {
		running = false;
	}
}
