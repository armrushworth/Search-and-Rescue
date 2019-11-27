package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import behaviors.CollectNonCriticalVictimBehavior;
import behaviors.ExitBehavior;
import behaviors.ReturnToHospitalBehavior;
import behaviors.SearchLocationBehavior;
import colourSensorModel.ColourSampleChart;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import monitors.PCMonitor;
import monitors.PilotMonitor;

public class Main {
	private static final int PORT = 1234; // server port between pc client and robot
	private static ServerSocket server; // server socket used between robot and pc client.
	private static boolean usePCMonitor = true;
//	private static boolean useColourChart = false;
	private static ArrayList<Cell> potentialVictims = new ArrayList<Cell>();
	private static HungarianMethod hungarianMethod;
	private static ArrayList<Cell> route;
	
	public static void main(String[] args) {
		// Initialise the grid and robot
		Grid grid = new Grid();
		PilotRobot myRobot = new PilotRobot();
		
		//TODO test
//		if (useColourChart) {
//		  //Loads previously taken and saved samples
//		  File leftColourChartFile = new File("LeftColourChart.txt");
//          File rightColourChartFile = new File("RightColourChart.txt");
//		  ColourSampleChart csc = new ColourSampleChart(myRobot, leftColourChartFile, rightColourChartFile);
//		} else {
//		  //Generates new colour samples
//		  ColourSampleChart csc = new ColourSampleChart(myRobot);
//		}
		
		
		// start the pilot monitor
		PilotMonitor myMonitor = new PilotMonitor(grid);
		myMonitor.start();
		
		// start the pc monitor
		PCMonitor pcMonitor = null;
		if (usePCMonitor) {
			try {
				server = new ServerSocket(PORT);
				System.out.println("Awaiting client..");
				Socket client = server.accept();
				pcMonitor = new PCMonitor(client, myRobot, grid);
			} catch (IOException e) {
				e.printStackTrace();
			}
			pcMonitor.start();
		}
		
		// TODO replace with AgentSpeak logic
		int bayNumber = 1;
		switch (bayNumber) {
			case 1:
				grid.getCell(1, 1).setIsBlocked();
				grid.getCell(1, 4).setIsBlocked();
				grid.getCell(4, 1).setIsBlocked();
				grid.getCell(4, 4).setIsBlocked();
				potentialVictims.add(grid.getCell(0, 5));
				potentialVictims.add(grid.getCell(2, 0));
				potentialVictims.add(grid.getCell(2, 2));
				potentialVictims.add(grid.getCell(2, 4));
				potentialVictims.add(grid.getCell(5, 4));
				break;
			case 2:
				grid.getCell(1, 3).setIsBlocked();
				grid.getCell(1, 5).setIsBlocked();
				grid.getCell(2, 4).setIsBlocked();
				grid.getCell(3, 3).setIsBlocked();
				potentialVictims.add(grid.getCell(0, 5));
				potentialVictims.add(grid.getCell(2, 3));
				potentialVictims.add(grid.getCell(2, 5));
				potentialVictims.add(grid.getCell(3, 1));
				potentialVictims.add(grid.getCell(5, 5));
				break;
			case 3:
				grid.getCell(2, 2).setIsBlocked();
				grid.getCell(3, 2).setIsBlocked();
				grid.getCell(4, 3).setIsBlocked();
				grid.getCell(5, 0).setIsBlocked();
				potentialVictims.add(grid.getCell(0, 2));
				potentialVictims.add(grid.getCell(1, 5));
				potentialVictims.add(grid.getCell(2, 3));
				potentialVictims.add(grid.getCell(4, 5));
				potentialVictims.add(grid.getCell(5, 1));
				break;
			case 4:
				grid.getCell(0, 3).setIsBlocked();
				grid.getCell(1, 2).setIsBlocked();
				grid.getCell(1, 4).setIsBlocked();
				grid.getCell(3, 3).setIsBlocked();
				potentialVictims.add(grid.getCell(0, 2));
				potentialVictims.add(grid.getCell(0, 4));
				potentialVictims.add(grid.getCell(1, 3));
				potentialVictims.add(grid.getCell(4, 3));
				potentialVictims.add(grid.getCell(5, 5));
		}
		hungarianMethod = new HungarianMethod(grid, potentialVictims);
		route = hungarianMethod.findRoute();
		
		// set up the behaviours for the arbitrator and construct it
		Behavior b1 = usePCMonitor ? new SearchLocationBehavior(myRobot, grid, pcMonitor) : new SearchLocationBehavior(myRobot, grid);
		Behavior b2 = new CollectNonCriticalVictimBehavior();
		Behavior b3 = new ReturnToHospitalBehavior();
		Behavior b4 = usePCMonitor ? new ExitBehavior(myRobot, myMonitor, pcMonitor) : new ExitBehavior(myRobot, myMonitor);
		Behavior [] behaviorArray = {b1, b2, b3, b4};
		Arbitrator arbitrator = new Arbitrator(behaviorArray);
		arbitrator.go();
	}
}
