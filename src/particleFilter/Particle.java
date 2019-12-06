package particleFilter;

import java.awt.Point;
import java.util.Random;

import javax.rmi.CORBA.Util;

/**
 * 
 * @author James Daniels
 * Each particle represents a possible state the robot could be in.
 * Each particle has a probability that the robot is currently in that state.
 * Making use of the markov assumption we can use sensory data to take a even
 *   Distributed set of probabilities and and update them until one dominant state occurs.
 * This is achieved using the Gaussian function to calculate a particles probability of 
 *   being in the robots actual state given previous sensor readings.
 */
public class Particle {
  
  //The uncertainity of the ultrasound sensor
  public final double senseNoise =  1;
  
  //Represent the x and y coordiante of the particle on the grid.
  public int x, y;
  
  //Represents the orientation of the particle in radians.
  public double orientation;
  
  //Boundaries of the arena.
  public int worldHeight, worldWidth;
  
  //The probability that this particle is in the actual state.
  public int weight = 0;
  
  //A list of known location that the sensor will pickup.
  public Point[] landmarks;
  
//  private Random random;
  
  /**
   * Constructor.
   * @param landmarks A list of known locations (x,y) that the ultrasound sensor can detect.
   * @param width of the arena.
   * @param height of the arena.
   * @param x coordinate.
   * @param y coordinate.
   * @param orientation of the particle in radians.
   */
  public Particle(Point[] landmarks, int width, int height, int x, int y, double orientation) {
    this.landmarks = landmarks;
    this.worldHeight = height;
    this.worldWidth = width;
    this.x = x;
    this.y = y;
    this.orientation = orientation;
//    random = new Random();
  }
  
  /**
   * SET method for particle.
   * @param x coordinate
   * @param y coordinate
   * @param o orientation of the particle in radians.
   * @param prob Probability of being in the correct state.
   */
  public void setWeight(int weight) {
    this.weight = weight;
  }
  
  /**
   * Moves the particle to the new estimated state
   * Called when the robot pilot moves.
   * @param turn The angle of rotation in this move.
   * @param forward The distance moved forward by the robot.
   * @throws Exception checks if a negative value for forward has been entered.
   */
  public void move(double turn, int forward) throws Exception {
    if (forward < 0) {
      throw new Exception("Robot cannot move backwards");
    }
    orientation = circle(turn + orientation, 360);
    x += Math.cos(orientation) * forward;
    y += Math.cos(orientation) * forward;
  }
  
  private double circle(double num, double length) {
	    while (num > length - 1) {
	      num -= length;
	    }
	    while (num < 0) {
	      num += length;
	    }
	    return num;
	  }
  
  public double calculateWeight(float measurement) {
    double prob = 1.0;
    float angle = 0;
    int numberOfCellsInSensorReading = (int) (measurement / 25);
    for (int i = 0; i < landmarks.length; i++) {
    	//If particle is on the landmark this is an impossible position for the robot so return a weight of 0
      if (x == landmarks[i].x && y == landmarks[i].y) {
        prob = 0;
        return 0;
      //calculate how many landmarks the particle could detect with the recent sensor reading.
      } else {
        //calculate sensor distance reading in number of cells and round up.
        int sensorReadingInCells = (int) Math.ceil(measurement/25) + 1;
        
//        //Distance between robot and landmark.
//        float distA = (float) distance(x, y, landmarks[i].x, landmarks[i].y);
//        //distB would be just the ultrasound sensor reading value.
//        //distC is the distance between the obstacle that caused the sensor reading and the landmark
//        distC = 0;
        
        //check if the particle is facing north, east, south or west.
        //north
        if (orientation == 360 || orientation == 0) {
          if ((y + numberOfCellsInSensorReading) == landmarks[i].y && x == landmarks[i].x) {
        	  weight++;
          }
        //south
        } else if (orientation == 180) {
        	if ((y - numberOfCellsInSensorReading) == landmarks[i].y && x == landmarks[i].x) {
          	  weight++;
            }
        //east
        } else if (orientation == 90) {
        	if (y == landmarks[i].y && (x + numberOfCellsInSensorReading) == landmarks[i].x) {
          	  weight++;
            }
        //west
        } else if (orientation == 270) {
        	if (y == landmarks[i].y && (x - numberOfCellsInSensorReading) == landmarks[i].x) {
            	  weight++;
              }
        }
      }
    }
    return weight;
  }
  
//  private float cosineRule(float a, float b, float c) {
//    if (a == 0 || b == 0) return 0;
//    return (float) Math.acos((
//        Math.pow(a,2) + 
//        Math.pow(b,2) -
//        Math.pow(c, 2)) /
//        2*a*b);
//  }
//  private double distance(int x1, int y1, int x2, int y2) {
//	  x1 *= 25;
//	  y1 *= 25;
//	  x2 *= 25;
//	  y2 *= 25;
//    return Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2), 2));
//  }
  
//  public double[] measure(double orientation, double x, double y) {
//	  
//  }
  
//	/**
//	 * Gaussian probabilty density function
//	 * 
//	 * @param mu
//	 * @param sigma
//	 * @param x
//	 * @return probability
//	 */
//	public final static double gaussian(double mu, double sigma, double x) {
//		return (1 / (sigma * Math.sqrt(2.0 * Math.PI))) * Math.exp(-0.5 * Math.pow(((x - mu) / sigma), 2));
//	}
  
//  private double calWeight(double particle, double sensor) {
//	  if (particle == 0 && sensor == 0) return 2;
//	  if (Math.abs(sensor - particle) == 0) return 2;
//	  return Math.round(10000*1/Math.pow(Math.abs(sensor - particle),2))/10000;
//  }
   
}
