package main;
import behaviors.ExitBehavior;
import behaviors.MoveBehavior;
import colourSensorModel.ColourSampleChart;

import java.io.*;
import java.net.*;
import lejos.robotics.subsumption.*;
import monitors.PCMonitor;
import monitors.PilotMonitor;

public class Main {
	private static final int PORT = 1234; // server port between pc client and robot
	private static ServerSocket server; // server socket used between robot and pc client.
	private static boolean usePCMonitor = true;
//	private static boolean useColourChart = false;
	
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
		
		// set up the behaviours for the arbitrator and construct it
		Behavior b1 = usePCMonitor ? new MoveBehavior(myRobot, grid, pcMonitor) : new MoveBehavior(myRobot, grid);
		Behavior b2 = usePCMonitor ? new ExitBehavior(myRobot, myMonitor, pcMonitor) : new ExitBehavior(myRobot, myMonitor);
		Behavior [] behaviorArray = {b1, b2};
		Arbitrator arbitrator = new Arbitrator(behaviorArray);
		arbitrator.go();
	}
}
