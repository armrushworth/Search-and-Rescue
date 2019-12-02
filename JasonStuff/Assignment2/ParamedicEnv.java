// Environment code for project doctor2018

import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import main.Cell;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.PrintWriter;
import java.util.logging.*;

public class ParamedicEnv extends Environment {
	
    public static final int GSize = 6; // The bay is a 6x6 grid
    public static final int HOSPITAL  = 8; // hospital code in grid model
    public static final int VICTIM  = 16; // victim code in grid model
    public Cell[] victims = new Cell[5];
    public Cell[] obstacles = new Cell[4];
    public Cell hospital;
    public boolean dataSent = false;
    
    // Create objects for visualising the bay.  
    // This is based on the Cleaning Robots code.
    private PrintWriter out; 

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
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
                		victims[i] = new Cell(x,y);
                	}
                }
            } else if (action.getFunctor().equals("addObstacle")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                for (int i = 0; i < obstacles.length; i++) {
                	if (obstacles[i] == null) {
                		obstacles[i] = new Cell(x,y);
                	}
                }
            } else if (action.getFunctor().equals("addHospital")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                hospital = new Cell(x,y);
            } else if (action.getFunctor().equals("sendData")) {
            	sendMapData();
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
		ServerSocket agentSystem = null;
		Socket agentClient = null;
		try {
			System.out.println("Awaiting client Brick..");
			agentSystem = new ServerSocket(1235);
			agentClient = agentSystem.accept();
			System.out.println("Connected");
			out = new PrintWriter(agentClient.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < victims.length; i++) {
			out.println(victims[i].getCoordinates().x + "," + (victims[i].getCoordinates().y));
		}
		for (int i = 0; i < obstacles.length; i++) {
			out.println(obstacles[i].getCoordinates().x + "," + (obstacles[i].getCoordinates().y));
		}
		out.println(hospital.getCoordinates().x + "," + (hospital.getCoordinates().y));
		
    }
    

    // ======================================================================
}
