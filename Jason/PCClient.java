import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class PCClient extends Thread {
	// bufferd reader this will connect to the socket PCMonitor.java uses
	private BufferedReader in;
	private BufferedReader errorIn;
	public boolean connected = false;
	
	//checks if thread is running.
	private volatile boolean running = true;
	
	// display robot data
	private JFrame myFrame = new JFrame("Robot progress");
	private JLabel lRobotStats; 
	private JLabel errorDisplay;
	private JLabel[] robotStates = new JLabel[36];
	private ImageIcon robotIcon;
	private ImageIcon empty;
	private ImageIcon unknown;
	private ImageIcon victim;
	private ImageIcon obstacle;
	
	@Override
	public void run() {
		// set window size
		myFrame.setResizable(false);
		myFrame.setSize(1240, 640);
		robotIcon = new ImageIcon(PCClient.class.getResource("/images/robot.png"));
		empty = new ImageIcon(PCClient.class.getResource("/images/empty.png"));
		unknown = new ImageIcon(PCClient.class.getResource("/images/unknown.png"));
		victim = new ImageIcon(PCClient.class.getResource("/images/victim.png"));
		obstacle = new ImageIcon(PCClient.class.getResource("/images/obstacle.png"));
		// create master panel
		JPanel masterPanel = new JPanel();
		masterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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
				"<html><div  style=\"padding: 20px\"><h1>Robot 14</h1>\n\n"
					+ "Victim information:<ol>"
						+ "<li>( , )</li>"
						+ "<li>( , )</li>"
						+ "<li>( , )</li>"
						+ "<li>( , )</li>"
						+ "<li>( , )</li>"
					+ "</ol>Navigation strategy:<ul>"
						+ "<li>Destination: </li>"
						+ "<li>Path: </li>"
						+ "<li>Route: </li>"
					+ "</ul>Sensor data:<ul>"
						+ "<li>Sonar distance: </li>"
						+ "<li>Gyro angle: </li>"
						+ "<li>Left colour: </li>"
						+ "<li>Right colour: </li>"
					+ "</ul>Movement information:<ul>"
						+ "<li>Status: </li>"
						+ "<li>Type: </li>"
						+ "<li>Heading: </li></ul>");
		errorDisplay.setText("No colour file errors");
		
		// ip of the robot
		String ip = "192.168.70.163"; 
		try {
			// create a new socket connection with the robots PCMonitor.java.
			Socket socket = new Socket(ip, 1234);
			System.out.println("Connected1");
			Socket errorSocket = new Socket(ip, 1111);
			System.out.println("Connected2");
			connected = true;
			
			// get Input from PCMonitor.
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			errorIn = new BufferedReader(new InputStreamReader(errorSocket.getInputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// constantly update values.
		while (running) {
			updateValues();
		}
	}
	
	/**
	 * Displays data about the robot as an HTML list in a java swing window.
	 */
	public void updateValues() {
		try {
			lRobotStats.setText(
					"<html><div  style=\"padding: 20px\"><h1>Robot 14</h1>\n"
						+ "Victim information:<ol>"
							+ "<li>" + in.readLine() + "</li>"
							+ "<li>" + in.readLine() + "</li>"
							+ "<li>" + in.readLine() + "</li>"
							+ "<li>" + in.readLine() + "</li>"
							+ "<li>" + in.readLine() + "</li>"
						+ "</ol>Navigation strategy:<ul>"
							+ "<li>Destination: " + in.readLine() + "</li>"
							+ "<li>Path: " + in.readLine() + "</li>"
							+ "<li>Route: " + in.readLine() + "</li>"
						+ "</ul>Sensor data:<ul>"
							+ "<li>Sonar distance: " + in.readLine() + "</li>"
							+ "<li>Gyro angle: " + in.readLine() + "</li>"
							+ "<li>Left colour: " + in.readLine() + "</li>"
							+ "<li>Right colour: " + in.readLine() + "</li>"
						+ "</ul>Movement information:<ul>"
							+ "<li>Status: " + in.readLine() + "</li>"
							+ "<li>Type: " + in.readLine() + "</li>"
							+ "<li>Heading: " + in.readLine() + "</li></ul>");
			
			// update display grid with robot position
			String currentCell = in.readLine();
			Point currentCellCoordinates = new Point(Integer.parseInt(currentCell.split(",")[0]), Integer.parseInt(currentCell.split(",")[1]));
			
			// update display grid with victim information
			String input = in.readLine();
			String[] potentialVictimsInput = input.split(",");
			
			int[][] potentialVictims = new int[5][3];
			for (int i = 0; i < 5; i++) {
				potentialVictims[i][0] = Integer.parseInt(potentialVictimsInput[i * 3 + 0]);
				potentialVictims[i][1] = Integer.parseInt(potentialVictimsInput[i * 3 + 1]);
				potentialVictims[i][2] = Integer.parseInt(potentialVictimsInput[i * 3 + 2]);
			}
			
			// update display with obstacles
			input = in.readLine();
			String[] obstaclesInput = input.split(",");
			
			int[][] obstacles = new int[4][2];
			for (int i = 0; i < 4; i++) {
				obstacles[i][0] = Integer.parseInt(obstaclesInput[i * 2 + 0]);
				obstacles[i][1] = Integer.parseInt(obstaclesInput[i * 2 + 1]);
			}
			
			for (int i = 0; i < robotStates.length; i++) {
				if (i == currentCellCoordinates.x + (5 - currentCellCoordinates.y) * 6) {
					robotStates[i].setIcon(robotIcon);
				} else if (i == obstacles[0][0] + (5 - obstacles[0][1]) * 6) {
					robotStates[i].setIcon(obstacle);
				} else if (i == obstacles[1][0] + (5 - obstacles[1][1]) * 6) {
					robotStates[i].setIcon(obstacle);
				} else if (i == obstacles[2][0] + (5 - obstacles[2][1]) * 6) {
					robotStates[i].setIcon(obstacle);
				} else if (i == obstacles[3][0] + (5 - obstacles[3][1]) * 6) {
					robotStates[i].setIcon(obstacle);
				} else if (i == potentialVictims[0][0] + (5 - potentialVictims[0][1]) * 6 && potentialVictims[0][2] == 1) {
					robotStates[i].setIcon(unknown);
				} else if (i == potentialVictims[1][0] + (5 - potentialVictims[1][1]) * 6 && potentialVictims[1][2] == 1) {
					robotStates[i].setIcon(unknown);
				} else if (i == potentialVictims[2][0] + (5 - potentialVictims[2][1]) * 6 && potentialVictims[2][2] == 1) {
					robotStates[i].setIcon(unknown);
				} else if (i == potentialVictims[3][0] + (5 - potentialVictims[3][1]) * 6 && potentialVictims[3][2] == 1) {
					robotStates[i].setIcon(unknown);
				} else if (i == potentialVictims[4][0] + (5 - potentialVictims[4][1]) * 6 && potentialVictims[4][2] == 1) {
					robotStates[i].setIcon(unknown);
				} else if (i == potentialVictims[0][0] + (5 - potentialVictims[0][1]) * 6 && potentialVictims[0][2] == 2) {
					robotStates[i].setIcon(victim);
				} else if (i == potentialVictims[1][0] + (5 - potentialVictims[1][1]) * 6 && potentialVictims[1][2] == 2) {
					robotStates[i].setIcon(victim);
				} else if (i == potentialVictims[2][0] + (5 - potentialVictims[2][1]) * 6 && potentialVictims[2][2] == 2) {
					robotStates[i].setIcon(victim);
				} else if (i == potentialVictims[3][0] + (5 - potentialVictims[3][1]) * 6 && potentialVictims[3][2] == 2) {
					robotStates[i].setIcon(victim);
				} else if (i == potentialVictims[4][0] + (5 - potentialVictims[4][1]) * 6 && potentialVictims[4][2] == 2) {
					robotStates[i].setIcon(victim);
				} else {
					robotStates[i].setIcon(empty);
				}
			}
			if (errorIn.ready()) {
				errorDisplay.setText(errorIn.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
			errorDisplay.setText(e.getMessage());
		}
	}
}


