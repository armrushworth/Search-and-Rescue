package colourSensorModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import main.PilotRobot;


/**
 * 
 * @author James Daniels
 * Implementation of the Colour Sensor Model seen towards the end of lecture 14-15;
 * If the user wants to generate new reference data to train k nearest neighbour algorithm a boolean in Main is set to true.
 * The user is then prompted on the robots lcd screen to scan a particular colour.
 * These colours are set in the constant string array called colours seen below.
 * The robot will ask you to scan each colour #noOfReadings times (intended so scans in multiple conditions can occur)
 * Each scan consists of #sampleSizePerReading samples from the Ev3 colour sensor.
 * Every sample is added to one of 2 ArrayLists that represents a 3d graph xyz being red, green, blue respectively. 
 *   one for the left sensor and another for the right (I use two separate graphs to prevent differences in sensor noise etc becoming an issue)..
 * This list is then saved to textfile which can be reloaded so the user does'nt have to collect new scans everytime we execute Main().
 * The user must set the boolean value useColourFile in Main to true if they want to do this.
 * The robot can then take new samples and pass them to the {@link #findColor(float[])} method.
 *   where a k nearest neighbour algorithm will find the colour with the highest number nearest neighbours
 *   i.e. the colour with the highest probability of the latest scan.
 *
 */
public class ColourSampleChart {
  //Stores the RGB and labels of colour samples representing a 3D graph x, y, z being red, green, blue values.
  private ArrayList<LabelledColourSample> leftColourSamples = new ArrayList<LabelledColourSample>();
  private ArrayList<LabelledColourSample> rightColourSamples = new ArrayList<LabelledColourSample>();
  
  //TODO This is to be replace with however the robot collects colour samples.
  private PilotRobot pilotRobot;
  
  //Robot lcd screen display
  private GraphicsLCD lcd;
  
  //A list containing the colour labels of a new samples k nearest neighbours.
  private int[] nearestColours;
  
  //A list containing the vector distance of a new samples k nearest neighbours.
  private double[] nearestDistances;
  
  //The number of separate readings needed per colour
  private final int noOfScans = 4; 
  
  //The number of samples to take for ever scan.
  private final int sampleSizePerReading = 25;
  
  //All colours that need to be distinguished by the robot.
  private final String[] colours = {"Black", "White", "Cyan", "Burgundy", "Yellow", "Green"};
  
  //The value k in the k nearest neighbour algorithm 
  private int k; 

  
  /**
   * Constructor used when new data for the graph needs to be generated.
   * @param csm Will be replaced by PilorRobot or whichever class returns colour sensor readings.
   * @param s The sensor "LEFT" or "Right" being used with this chart.
   */
  public ColourSampleChart(PilotRobot pr) {
    //Sets k to a multiple of the number of colours that are clustered.
    k = colours.length*2;
    this.pilotRobot = pr;
    
    //Generates a new colour graph.
    takeReadings();
  }
  
  /**
   * Constructor used when using the colour graph stored in the colour file.
   * @param csm Will be replaced by PilorRobot or whichever class returns colour sensor readings.
   * @param savedColourChart The file used to store the colour graph generated from a previous scanning session.
   * @param s The sensor "LEFT" or "Right" being used with this chart.
   */
  public ColourSampleChart(PilotRobot pr, File savedLeftColourChart, File savedRightColourChart) {
    this.pilotRobot = pr;
    
    //Read left file
    //Initialise a buffered reader and read through the colour file and generate the colour graph/list.
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(savedLeftColourChart));
      String line = reader.readLine();
      while (line != null) {
        //rgbValues[0] will be the colours label.
        String[] rgbValues = line.split(",");
        float[] colours = new float[3];
        colours[0] = Float.parseFloat(rgbValues[1]); //red
        colours[1] = Float.parseFloat(rgbValues[2]); //green
        colours[2] = Float.parseFloat(rgbValues[3]); //blue
        
        //Create a #LabeledColourSample using the current line from the colour file.
        LabelledColourSample lcs = new LabelledColourSample( rgbValues[0], colours);
        
        //Add this labeled colour sample to the colour chart.
        this.leftColourSamples.add(lcs);
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    };
    
    //read right fle.
    try {
      reader = new BufferedReader(new FileReader(savedRightColourChart));
      String line = reader.readLine();
      while (line != null) {
        //rgbValues[0] will be the colours label.
        String[] rgbValues = line.split(",");
        float[] colours = new float[3];
        colours[0] = Float.parseFloat(rgbValues[1]); //red
        colours[1] = Float.parseFloat(rgbValues[2]); //green
        colours[2] = Float.parseFloat(rgbValues[3]); //blue
        
        //Create a #LabeledColourSample using the current line from the colour file.
        LabelledColourSample lcs = new LabelledColourSample( rgbValues[0], colours);
        
        //Add this labeled colour sample to the colour chart.
        this.rightColourSamples.add(lcs);
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    };
  }
  
  /**
   * Take colour readings for every colour the robot needs to distinguish.
   */
  public void takeReadings() {
    for (int i = 0; i < colours.length; i++) {
      reading(colours[i]);
    }
  }
  
  /**
   *For the noOfScans needed for each colour take sampleSizePerReading samples from the colour sensor.
   *Generate a LabeledColourSample for every sample taken.
   *Add this to the colour graph.
   * @param colourName The colour the robot is currently taking samples for.
   */
  public void reading(String colourName) {
    //noOfScans denotes how many scans are performed for each colour.
    //The robot prompts the user to relocate the robot when a scan is complete
    for (int j = 0; j < noOfScans; j++) {
      System.out.println(colourName + " reading " + (j + 1)+ " of "+ noOfScans +" press any button to continue");
      Button.waitForAnyPress();
      //Sample size per reading is how many samples from the colour sensor are taken and plotted
      for (int i = 0; i < sampleSizePerReading; i++) {
          this.leftColourSamples.add(
              new LabelledColourSample(colourName, pilotRobot.getLeftColor()));
          this.rightColourSamples.add(
              new LabelledColourSample(colourName, pilotRobot.getRightColor()));
        
      }
    }
  }
  
  /**
   * This is whats called when the robot takes a colour sensor sample and needs to distinguish what it is.
   * @param colourSample A new colour sample the list contains the value for red, green and blue.
   * @param leftSensor Boolean true means this finding what colour the left sensor has sampled. false is the right sensor
   * @return The detected colour.
   */
  public String findColor(float[] colourSample, Boolean leftSensor) {
    nearestColours = new int[k];
    nearestDistances = new double[k];
    
    //Set the first k labelled colour samples in the graph to be the initial nearest neighbours.
    for (int i = 0; i < k; i++) {
      if (leftSensor) {
        nearestDistances[i] = leftColourSamples.get(i).getVectorDistance(colourSample);
        nearestColours[i] = i;
      } else {
        nearestDistances[i] = rightColourSamples.get(i).getVectorDistance(colourSample);
        nearestColours[i] = i;
      }

    }
    
    /*For every remaining labelled colour sample check if its vector distance is closer than any of the
     *  current nearest neighbours.*/
    //Both left and right colour sample lists will be the same length.
    for (int i = k; i < leftColourSamples.size(); i++) {
      double vectorDistance = Double.POSITIVE_INFINITY;
      if (leftSensor) {
        vectorDistance = leftColourSamples.get(i).getVectorDistance(colourSample);
      } else {
        vectorDistance = rightColourSamples.get(i).getVectorDistance(colourSample);
      }
      checkIfShorter(vectorDistance, i);
    }
    
    //Stores the number of times a certain colour is a nearest neighbour. 
    int[] totalNeighbours = new int[colours.length];
    //Initialise array values to 0
    for (int i = 0; i < totalNeighbours.length; i++) {
      totalNeighbours[i] = 0;
    }
    
    /*For every colour check if it occurs as a nearest neighbour.
     *For every time it does increment its associated totalNeighbours value.*/
    for (int i = 0; i < k; i++) {
      for (int j = 0; j < totalNeighbours.length; j++) {
        if (leftSensor) {
          //check if colour k is equal to nearest neighbour j.
          if (leftColourSamples.get(nearestColours[i]).getColourLabel().equals(colours[j])) {
            totalNeighbours[j]++;
          }
        } else {
          if (rightColourSamples.get(nearestColours[i]).getColourLabel().equals(colours[j])) {
            totalNeighbours[j]++;
          }
        }
      }
    }
    
    //keeps track of the highest nearest neighbour occurrence.
    int highestProbability = 0;
    
    //Keeps track of which colour has the highest nearest neighbour occurrence.
    int highestProbabilityIndex = 0;
    
    //For every colour find which colour has the highest occurrence of nearest neighbours
    for (int i = 0; i < colours.length; i++) {
      if (totalNeighbours[i] > highestProbability) {
        highestProbability = totalNeighbours[i];
        highestProbabilityIndex = i;
      }
    }
    
    //Return the colour with the highest number of nearest neighbours 
    return(colours[highestProbabilityIndex]);
  }
  
  /**
   * Compare a labelled colour samples vectorDistance with the current list of nearest neighbours.
   * @param vectorDistance The distance between the new sample and labelled colour samples plotting on the colour chart.
   * @param index The index of the labelled colour sample being compared.
   */
  public void checkIfShorter(double vectorDistance, int index) {
    for (int i = 0; i < k; i++) {
      if (vectorDistance < nearestDistances[i]) {
        
        //swap new nearestNeighbour with the current nearest neighbour at index i in the NN list.
        double tempDistance = nearestDistances[i];
        int tempIndex = nearestColours[i];
        nearestDistances[i] = vectorDistance;
        nearestColours[i] = index;
        
        //Check if the swapped out NN is smaller than any other items on the list.
        checkIfShorter(tempDistance,tempIndex);
        break;
      }
    }
  }
  
  /**
   * Every time a new data is collected for a fresh colour graph it gets saved to a text file.
   */
  public void saveChartToFile(Boolean leftSensor) {
    try {
      //Overwrite (Left|Right)ColourChart.txt.
      PrintWriter writer = null;
      if (leftSensor) {
        writer = new PrintWriter("LeftColourChart.txt", "UTF-8");
      } else {
        writer = new PrintWriter("RightColourChart.txt", "UTF-8");
      }
       
      
      //For every labelled colour sample write its label and RGB value as a list on one line of a txt files.
      for (int i = 0; i < leftColourSamples.size(); i++) {
        LabelledColourSample lcs = null;
        if (leftSensor) {
          lcs = leftColourSamples.get(i); 
        } else {
          lcs = rightColourSamples.get(i);
        }
         
        writer.println(
            lcs.getColourLabel() + "," +
            lcs.getRed() + "," +
            lcs.getGreen() + "," +
            lcs.getBlue());
      }
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
  

}
