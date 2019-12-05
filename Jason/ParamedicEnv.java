// Environment code for project doctor2018

import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.asSyntax.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.io.PrintWriter;
import java.util.logging.*;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ParamedicEnv extends Environment {
    public String[] victims = new String[5];
    public String[] obstacles = new String[4];
    public String hospital;
    public boolean dataSent = false;
    public Socket socket;
    PCClient pc = new PCClient();
    
    // Create objects for visualising the bay.  
    // This is based on the Cleaning Robots code.
    private PrintWriter out; 

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        pc.start();
        while (!pc.connected) {
        	try {
        		Thread.sleep(500);
        	} catch (InterruptedException e) {
        		e.printStackTrace();
        	}
        }
        try {
			String ip = "192.168.70.163"; 
			System.out.println("Awaiting server Brick..");
			socket = new Socket(ip, 1235);
			if (socket.isConnected()) {
				System.out.println("Connected");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //addPercept(ASSyntax.parseLiteral("percept(demo)"));
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        try {
        	if (action.getFunctor().equals("addVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                for (int i = 0; i < victims.length; i++) {
                	if (victims[i] == null) {
                		victims[i] = x+","+y;
                		break;
                	}
                }
            } else if (action.getFunctor().equals("addObstacle")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                for (int i = 0; i < obstacles.length; i++) {
                	if (obstacles[i] == null) {
                		obstacles[i] = x+","+y;
                		break;
                	}
                }
            } else if (action.getFunctor().equals("addHospital")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                hospital = x+","+y;
            } else if (action.getFunctor().equals("sendData")) {
            	sendMapData();
            } else if (action.getFunctor().equals("patientCritical")){
            	out = new PrintWriter(socket.getOutputStream(), true);
            	out.println("Critical");
            	listenForResponse();
            } else if (action.getFunctor().equals("patientNotCritical")){
            	out = new PrintWriter(socket.getOutputStream(), true);
            	out.println("NotCritical");
            	listenForResponse();
            } else if (action.getFunctor().equals("noVictim")) {
            	out = new PrintWriter(socket.getOutputStream(), true);
            	out.println("NoVictim");
            	listenForResponse();
            } else {
                return true;
                // Note that technically we should return false here.  But that could lead to the
                // following Jason error (for example):
                // [ParamedicEnv] executing: addObstacle(2,2), but not implemented!
                // [paramedic] Could not finish intention: intention 6: 
                //    +location(obstacle,2,2)[source(doctor)] <- ... addObstacle(X,Y) / {X=2, Y=2, D=doctor}
                // This is due to the action failing, and there being no alternative.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
           
        informAgsEnvironmentChanged();
        return true;       
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
    
    public void sendMapData() {	
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Writer established");

		for (int i = 0; i < victims.length; i++) {
			out.println(victims[i]);
		}
		for (int i = 0; i < obstacles.length; i++) {
			out.println(obstacles[i]);
		}
		out.println(hospital);
		listenForResponse();
    }
    
    public void listenForResponse() {
		try {
			System.out.println("flag1");
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (!in.ready()) {
				Thread.sleep(1000);
			}
			System.out.println("flag2");
			String percept = in.readLine();
			System.out.println("flag3 " + "colour(" + percept + ")");
			clearPercepts();
			addPercept(ASSyntax.parseLiteral("colour(" + percept + ")"));
			System.out.println("flag4");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
    }
}
