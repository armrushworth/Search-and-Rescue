package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import behaviors.ExitBehavior;
import behaviors.MoveBehavior;
import behaviors.SelectDestinationBehavior;
import colourSensorModel.ColourSampleChart;
import lejos.hardware.Sound;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import monitors.PCMonitor;
import monitors.PilotMonitor;

public class Main {
	private static final int PORT = 1234; // server port between pc client and robot
	private static ServerSocket server; // server socket used between robot and pc client.
	private static boolean useColourChart = true;
	private static ArrayList<Cell> route = new ArrayList<Cell>();
	private static ArrayList<Cell> path = new ArrayList<Cell>();
	private static ArrayList<Cell> potentialVictims = new ArrayList<Cell>();
	
	public static void main(String[] args) {
		//prevents really annoying bugs when comparing labelled colour samples.
	    System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	    Socket agentClient = null;
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
		try {
			System.out.println("Awaiting client 1..");
			server = new ServerSocket(PORT);
			Socket client = server.accept();
			System.out.println("Awaiting client 2..");
			ServerSocket errorServer = new ServerSocket(1111);
			Socket errorClient = errorServer.accept();
			
			pcMonitor = new PCMonitor(client, errorClient, myRobot, grid, potentialVictims, csc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		pcMonitor.start();
		if (csc.recentError != null) {
			pcMonitor.sendError(csc.recentError);
		}

		
		if (!useColourChart) {
			myRobot.resetGyro();
		}
		
		// start the pilot monitor
		PilotMonitor myMonitor = new PilotMonitor(grid);
		myMonitor.start();
		
		try {
			System.out.println("Awaiting client 3..");
			ServerSocket agentServer = new ServerSocket(1235);
			agentClient = agentServer.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(agentClient.getInputStream()));
		    Cell[] victims = new Cell[5];
		    Cell[] obstacles = new Cell[4];
		    Cell hospital;
		    String coords;
			for (int i = 0; i < victims.length; i++) {
				coords = in.readLine();
				int x = Integer.parseInt(coords.split(",")[0]);
				int y = Integer.parseInt(coords.split(",")[1]);
				grid.getCell(x, y).setStatus(1);
				potentialVictims.add(grid.getCell(x, y));
			}
			for (int i = 0; i < obstacles.length; i++) {
				coords = in.readLine();
				int x = Integer.parseInt(coords.split(",")[0]);
				int y = Integer.parseInt(coords.split(",")[1]);
				grid.getCell(x, y).setIsBlocked();
			}
			coords = in.readLine();
			hospital = new Cell(Integer.parseInt(coords.split(",")[0]),Integer.parseInt(coords.split(",")[1]));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			pcMonitor.sendError(e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			pcMonitor.sendError(e);
			e.printStackTrace();
		}
		
		// TODO replace with AgentSpeak logic
//		int bayNumber = 4;
//		switch (bayNumber) {
//			case 1:
//				grid.getCell(1, 1).setIsBlocked();
//				grid.getCell(1, 4).setIsBlocked();
//				grid.getCell(4, 1).setIsBlocked();
//				grid.getCell(4, 4).setIsBlocked();
//				grid.getCell(0, 5).setStatus(1);
//				grid.getCell(2, 0).setStatus(1);
//				grid.getCell(2, 2).setStatus(1);
//				grid.getCell(2, 4).setStatus(1);
//				grid.getCell(5, 4).setStatus(1);
//				break;
//			case 2:
//				grid.getCell(1, 3).setIsBlocked();
//				grid.getCell(1, 5).setIsBlocked();
//				grid.getCell(2, 4).setIsBlocked();
//				grid.getCell(3, 3).setIsBlocked();
//				grid.getCell(0, 5).setStatus(1);
//				grid.getCell(2, 3).setStatus(1);
//				grid.getCell(2, 5).setStatus(1);
//				grid.getCell(3, 1).setStatus(1);
//				grid.getCell(5, 5).setStatus(1);
//				break;
//			case 3:
//				grid.getCell(2, 2).setIsBlocked();
//				grid.getCell(3, 2).setIsBlocked();
//				grid.getCell(4, 3).setIsBlocked();
//				grid.getCell(5, 0).setIsBlocked();
//				grid.getCell(0, 2).setStatus(1);
//				grid.getCell(1, 5).setStatus(1);
//				grid.getCell(2, 3).setStatus(1);
//				grid.getCell(4, 5).setStatus(1);
//				grid.getCell(5, 1).setStatus(1);
//				break;
//			case 4:
//				grid.getCell(0, 3).setIsBlocked();
//				grid.getCell(1, 2).setIsBlocked();
//				grid.getCell(1, 4).setIsBlocked();
//				grid.getCell(3, 3).setIsBlocked();
//				grid.getCell(0, 2).setStatus(1);
//				grid.getCell(0, 4).setStatus(1);
//				grid.getCell(1, 3).setStatus(1);
//				grid.getCell(4, 3).setStatus(1);
//				grid.getCell(5, 5).setStatus(1);
//		}
		
		Sound.beepSequenceUp();
		
		// set up the behaviours for the arbitrator and construct it
		Behavior b1 = new MoveBehavior(myRobot, grid, path, csc);
		Behavior b2 = new SelectDestinationBehavior(myRobot, agentClient, pcMonitor, csc, grid, route, path);
		Behavior b3 = new ExitBehavior(myRobot, agentClient, myMonitor, pcMonitor, grid);
		Behavior [] behaviorArray = {b1, b2, b3};
		Arbitrator arbitrator = new Arbitrator(behaviorArray);
		arbitrator.go();
	}
}
