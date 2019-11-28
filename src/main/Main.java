package main;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import behaviors.ExitBehavior;
import behaviors.MoveBehavior;
import colourSensorModel.ColourSampleChart;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import monitors.PCMonitor;
import monitors.PilotMonitor;

public class Main {
	private static final int PORT = 1234; // server port between pc client and robot
	private static ServerSocket server; // server socket used between robot and pc client.
	private static boolean usePCMonitor = true;
	private static boolean useColourChart = true;
	private static ArrayList<Cell> potentialVictims = new ArrayList<Cell>();
	private static HungarianMethod hungarianMethod;
	private static ArrayList<Cell> route;
	
	public static void main(String[] args) {
	    System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		// Initialise the grid and robot
		Grid grid = new Grid();
		PilotRobot myRobot = new PilotRobot();
		ColourSampleChart csc;
		//TODO test
		if (useColourChart) {
		  //Loads previously taken and saved samples
		  File leftColourChartFile = new File("LeftColourChart.txt");
          File rightColourChartFile = new File("RightColourChart.txt");
		  csc = new ColourSampleChart(myRobot, leftColourChartFile, rightColourChartFile);
		} else {
		  //Generates new colour samples
		  csc = new ColourSampleChart(myRobot);
		}
		
		// start the pc monitor
		PCMonitor pcMonitor = null;
		if (usePCMonitor) {
			try {
				System.out.println("Awaiting client 1..");
				server = new ServerSocket(PORT);
				Socket client = server.accept();
				System.out.println("Awaiting client 2..");
				ServerSocket errorServer = new ServerSocket(1111);
				Socket errorClient = errorServer.accept();
				
				pcMonitor = new PCMonitor(client, errorClient, myRobot, grid, csc);
			} catch (IOException e) {
				e.printStackTrace();
			}
			pcMonitor.start();
			pcMonitor.sendError(csc.recentError);

		}
		myRobot.resetGyro();
		// start the pilot monitor
		PilotMonitor myMonitor = new PilotMonitor(grid);
		myMonitor.start();
		
		// TODO replace with AgentSpeak logic
		int bayNumber = 4;
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
		Behavior b1 = usePCMonitor ? new MoveBehavior(myRobot, grid, route, pcMonitor, csc) : new MoveBehavior(myRobot, grid, route, csc);
		Behavior b2 = usePCMonitor ? new ExitBehavior(myRobot, route, myMonitor, pcMonitor) : new ExitBehavior(myRobot, route, myMonitor);
		Behavior [] behaviorArray = {b1, b2};
		Arbitrator arbitrator = new Arbitrator(behaviorArray);
		arbitrator.go();
	}
}
