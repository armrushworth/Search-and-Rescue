package particleFilter;

import java.awt.Point;
import java.util.Random;

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
  public double probability = 0;
  
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
  public void setProb(double prob) {
    this.probability = prob;
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
    orientation += turn;
    x += Math.cos(orientation) * forward;
    y += Math.cos(orientation) * forward;
  }
  
  public double calculateProbability(float[] measurement) {
    double prob = 1.0;
    for (int i = 0; i < landmarks.length; i++) {
      if (x == landmarks[i].x && y == landmarks[i].y) {
        prob = 0;
        return 0;
      } else {
        //calculate sensor distance reading in number of cells and round up.
        int sensorReadingInCells = (int) Math.ceil(measurement[0]/25);
        
        //Distance between robot and landmark.
        float distA = (float) distance(x, y, landmarks[i].x, landmarks[i].y);
        //distB would be just the ultrasound sensor reading value.
        //distC is the distance between the obstacle that caused the sensor reading and the landmark
        float distC = 0;
        
        //check if the particle is facing north, east, south or west.
        //north
        if (orientation == 0 || orientation == 2*Math.PI) {
          distC = (float) distance(x, y + sensorReadingInCells, landmarks[i].x, landmarks[i].y);
        //south
        } else if (orientation == Math.PI) {
          distC = (float) distance(x, y - sensorReadingInCells, landmarks[i].x, landmarks[i].y);
        //east
        } else if (orientation == Math.PI/2) {
          distC = (float) distance(x + sensorReadingInCells, y, landmarks[i].x, landmarks[i].y);
        //west
        } else if (orientation == 3*Math.PI/2) {
          distC = (float) distance(x - sensorReadingInCells, y, landmarks[i].x, landmarks[i].y);
        }
        
        float angle = cosineRule(distA, measurement[0], distC);

        //we need to calculate 2 gaussian functions and times them  together to get weight/probability for the particle
        //2 functions for the distance and angle diffrence between the landmark and sensor reading.
        prob *= gaussian(distC, measurement[0]) * gaussian(0, angle);
      }
    }
    probability = prob;
    return prob;
  }
  private float cosineRule(float a, float b, float c) {
    return (float) Math.acos((
        Math.pow(a,2) + 
        Math.pow(b,2) -
        Math.pow(c, 2)) /
        2*a*b);
  }
  private double distance(int x1, int y1, int x2, int y2) {
    return Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2), 2));
  }
  
  /**
   * Gaussian function for calculating probability.
   * Equation (sigma * squareRoot(2pi))^-1 * e^(-1/2((x-mu)/sigma)^2).
   * @param mu distance between landmark and particle.
   * @param x measured distance.
   * @return result of the function.
   */
  private double gaussian(double mu, double x) {
    return ((1/(this.orientation * Math.sqrt(2*Math.PI))) *
        Math.exp(-(
            Math.pow(((x - mu)/senseNoise), 2))/2));
  }
   
}
