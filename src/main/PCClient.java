package main;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class PCClient extends JFrame {
	// bufferd reader this will connect to the socket PCMonitor.java uses
	private static BufferedReader in;
	private static BufferedReader errorIn;
	
	// display robot data
	private static JFrame myFrame = new JFrame("Robot progress"); 
	private static JLabel lRobotStats; 
	private static JLabel errorDisplay;
	private static JLabel[] robotStates = new JLabel[36];
	private static ImageIcon robotIcon;
	private static ImageIcon empty;
	
	public static void main(String[] args) throws IOException {
		// set window size
		myFrame.setResizable(false);
		myFrame.setSize(1280, 640);
		robotIcon = new ImageIcon(PCClient.class.getResource("/images/robot.png"));
		empty = new ImageIcon(PCClient.class.getResource("/images/empty.png"));
		// create master panel
		JPanel masterPanel = new JPanel();
		masterPanel.setLayout(new GridLayout(1, 2));
		
		// create robot stats panel
		JPanel robotStats = new JPanel();
		robotStats.setLayout(new GridLayout(1,2));
		lRobotStats = new JLabel(); 
		errorDisplay = new JLabel();
		
		// create the occupancy grid panel
		JPanel occupancyGrid = new JPanel(new GridLayout(6,6));
		JPanel[][] gridPanels = new JPanel[6][6];
		
		int count = 0;
		for (int i = 5; i >= 0; i--) {
			for (int j = 0; j < 6; j++) {
				gridPanels[i][j] = new JPanel(new GridLayout(2, 1));
				robotStates[count] = new JLabel();
				gridPanels[i][j].add(robotStates[count]);
				
				gridPanels[i][j].add(new JLabel("(" + j + ", " + i + ")"));
				gridPanels[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
				occupancyGrid.add(gridPanels[i][j]);
				count++;
			}
		}
		
		// add label to panel 
		robotStats.add(lRobotStats); 
		robotStats.add(errorDisplay);
		masterPanel.add(robotStats);
		masterPanel.add(occupancyGrid);
		
		// add panel to frame 
		myFrame.add(masterPanel); 
		
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.setVisible(true); 
		lRobotStats.setText(
				"<html><h1>Robot 14</h1>\n"
					+ "Sensor data:<ul>"
						+ "<li>Sonar distance: </li>"
						+ "<li>Gyro angle: </li>"
						+ "<li>Left colour: </li>"
						+ "<li>Right colour: </li>"
					+ "</ul>Movement information:<ul>"
						+ "<li>Status: </li>"
						+ "<li>Type: </li>"
						+ "<li>Heading: </li>"
					+ "</ul>Navigation strategy:<ul>"
						+ "<li>Route: </li>"
						+ "<li>Next destination: </li>"
						+ "<li>Current path: </li>"
					+ "</ul>");
		errorDisplay.setText("No colour file errors");
		
		// ip of the robot
		String ip = "192.168.70.163"; 
		
		if (args.length > 0) {
			ip = args[0];
		}
		
		// create a new socket connection with the robots PCMonitor.java.
		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected1");
		Socket errorSocket = new Socket(ip, 1111);
		System.out.println("Connected2");
		
		// get Input from PCMonitor.
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		errorIn = new BufferedReader(new InputStreamReader(errorSocket.getInputStream()));
		
		// constantly update values.
		while (true) {
			updateValues();
		}
	}
	
	/**
	 * Displays data about the robot as an HTML list in a java swing window.
	 */
	public static void updateValues() {
		try {
			lRobotStats.setText(
					"<html><h1>Robot 14</h1>\n"
						+ "Sensor data:<ul>"
							+ "<li>Sonar distance: " + in.readLine() + "</li>"
							+ "<li>Gyro angle: " + in.readLine() + "</li>"
							+ "<li>Left colour: " + in.readLine() + "</li>"
							+ "<li>Right colour: " + in.readLine() + "</li>"
						+ "</ul>Movement information:<ul>"
							+ "<li>Status: " + in.readLine() + "</li>"
							+ "<li>Type: " + in.readLine() + "</li>"
							+ "<li>Heading: " + in.readLine() + "</li>"
						+ "</ul>Victim information:<ul>"
							+ "<li>Potential victims: " + in.readLine() + "</li>"
							+ "<li>Non-urgent victims: " + in.readLine() + "</li>"
						+ "</ul>Navigation strategy:<ul>"
							+ "<li>Route: " + in.readLine() + "</li>"
							+ "<li>Next destination: " + in.readLine() + "</li>"
							+ "<li>Current path: " + in.readLine() + "</li>"
						+ "</ul>");
			// update display grid with robot position
			String currentCell = in.readLine();
			int x = Integer.parseInt(currentCell.split(",")[0]);
			int y = Integer.parseInt(currentCell.split(",")[1]);
			for (int i = 0; i < robotStates.length; i++) {
				if (i == x + (5 - y) * 6) {
					robotStates[i].setIcon(robotIcon);
				} else {
					robotStates[i].setIcon(empty);
				}
			}
			if (errorIn.ready() && errorIn.readLine() != null) {
				errorDisplay.setText(errorIn.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorDisplay.setText(e.getMessage());
		}
	}
}


