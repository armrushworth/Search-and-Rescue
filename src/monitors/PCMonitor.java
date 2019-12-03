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

	private ArrayList<Cell> potentialVictims = new ArrayList<Cell>();
	private ArrayList<Cell> nonUrgentVictims = new ArrayList<Cell>();
	
	private Grid grid;
	
	private ColourSampleChart csc;
	
	private ArrayList<Cell> route = new ArrayList<Cell>();
	private Cell destination = null;
	private ArrayList<Cell> path = new ArrayList<Cell>();

	public PCMonitor(Socket client, Socket errorSocket, PilotRobot robot, Grid grid, ColourSampleChart csc) {
		this.client = client;
		this.errorSocket = errorSocket;
		this.robot = robot;
		this.grid = grid;
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
			
			// output sensor information
			out.println(robot.getDistance());
			out.println(robot.getAngle());
			out.println(csc.findColor(robot.getLeftColor(),true));
			out.println(csc.findColor(robot.getRightColor(),false));
			
			// ouptut movement information
			if (robot.getPilot().isMoving()) {
				out.println("Moving");
			} else {
				out.println("Stationary");
			}
			out.println(robot.getPilot().getMovement().getMoveType());
			out.println(robot.getAngle());
			
			// output victim information
			potentialVictims = grid.getPotentialVictims();
			if (!potentialVictims.isEmpty()) {
				String potentialVictimsOutput = "";
				for (Cell cell : potentialVictims) {
					potentialVictimsOutput += "(" + cell.getCoordinates().x + ", " + cell.getCoordinates().y + "), ";
				}
				out.println(potentialVictimsOutput.substring(0, potentialVictimsOutput.length() - 2));
			} else {
				out.println("null");
			}
			
			nonUrgentVictims = grid.getNonUrgentVictims();
			if (!nonUrgentVictims.isEmpty()) {
				String nonUrgentVictimsOutput = "";
				for (Cell cell : nonUrgentVictims) {
					nonUrgentVictimsOutput += "(" + cell.getCoordinates().x + ", " + cell.getCoordinates().y + "), ";
				}
				out.println(nonUrgentVictimsOutput.substring(0, nonUrgentVictimsOutput.length() - 2));
			} else {
				out.println("null");
			}
			
			// output the route
			if (!route.isEmpty()) {
				String routeOutput = destination != null ? "(" + destination.getCoordinates().x + ", " + destination.getCoordinates().y + "), " : "";
				for (Cell cell : route) {
					routeOutput += "(" + cell.getCoordinates().x + ", " + cell.getCoordinates().y + "), ";
				}
				out.println(routeOutput.substring(0, routeOutput.length() - 2));
			} else {
				out.println("null");
			}
			
			// output the destination
			out.println(destination != null ? "(" + destination.getCoordinates().x + ", " + destination.getCoordinates().y + ")" : "null");
			
			// output the path
			if (!path.isEmpty()) {
				String pathOutput = (destination != null) ? "(" + destination.getCoordinates().x + ", " + destination.getCoordinates().y + "), " : "";
				for (Cell cell : path) {
					pathOutput += "(" + cell.getCoordinates().x + ", " + cell.getCoordinates().y + "), ";
				}
				out.println(pathOutput.substring(0, pathOutput.length() - 2));
			} else {
				out.println("null");
			}
			
			out.println(grid.getCurrentCell().getCoordinates().x + "," + grid.getCurrentCell().getCoordinates().y);
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
