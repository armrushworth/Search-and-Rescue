package particleFilter;

import java.awt.Point;
import java.util.HashMap;
import java.util.Random;

/**
 * 
 * @author James Daniels
 * Particle filter algorithm used for localisation of the Ev3 bot.
 * Initial state will be 4 particles for every Grid position for North, West, South and east orientations.
 * So there will be 7*8*4 = 224 particles in total.
 * I'm using 7 and 8 as he width an height boundaries so i can treat the arena wall as landmarks.
 * If a particle is placed on a landmark its probability is set to 0;
 */
public class ParticleFilter {
  
  //An array of particles used to estimate the robots currents position.
  Particle[] particles;
  
  //The total number of particles this is used incase we need to change the number of particles used.
  public int totalParticles;
  HashMap<String,Integer> particlesPerCell;
  String bestCell;
  
  //Random number generator used for selecting the fittest particles.
  Random randNum = new Random();
  
  /**
   * Constructor.
   * @param tp The total number of particles to use.
   * @param landmarks Known locations of obstacles that the ultrasound sensor will pick up.
   * @param width Width of the grid the particles can be placed on.
   * @param height Height of the grid the particles can be placed on.
   */
  public ParticleFilter(Point[] landmarks, int width, int height) {
    this.totalParticles = width*height*4;
    particles = new Particle[totalParticles];
    int particleCounter = 0;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++ ) {
        particles[particleCounter] = new Particle(landmarks, width, height, i, j, 0);//north
        particles[particleCounter+1] = new Particle(landmarks, width, height, i, j, 90); //east
        particles[particleCounter+2] = new Particle(landmarks, width, height, i, j, 180); //south
        particles[particleCounter+3] = new Particle(landmarks, width, height, i, j, 270); //west
        particleCounter += 4;
      }
    }
  }
  
  public void move(double turn, int forward) {
    for (int i = 0; i < particles.length; i++) {
      try {
        particles[i].move(turn, forward);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public void resample(float measurement) {
    Particle[] newParticles = new Particle[totalParticles];
    
    for (int i = 0; i < totalParticles; i++) {
    	double weight = particles[i].calculateWeight(measurement);
    	System.out.println(weight);
    }
    
    float B = 0f;
    
    Particle best = getBestParticle();
    int index = (int) randNum.nextFloat() * totalParticles;
    for (int i = 0; i < totalParticles; i++) {
      B += randNum.nextFloat() * 2f * best.weight;
      while (B > particles[index].weight) {
        B -= particles[index].weight;
        index = circle(index + 1, totalParticles);
      }
      newParticles[i] = new Particle(particles[index].landmarks,
                                     particles[index].worldWidth,
                                     particles[index].worldHeight,
                                     particles[index].x,
                                     particles[index].y,
                                     particles[index].orientation);
      newParticles[i].setWeight(particles[index].weight);
    }
    particles = newParticles;
  }
  
  private int circle(int num, int length) {
    while (num > length - 1) {
      num -= length;
    }
    while (num < 0) {
      num += length;
    }
    return num;
  }
  
  public Particle getBestParticle() {
    Particle particle = particles[0];
    for (int i = 0; i < totalParticles; i++) {
      if (particles[i].weight > particle.weight) {
        particle = particles[i];
      }
    }
    return particle;
  }
  
  
  public void calculateBestCell() {
	particlesPerCell = new HashMap<String,Integer>();
	int max = 1;
	String temp = "";
	for (int i = 0; i < totalParticles; i++) {
		if (particlesPerCell.get(particles[i].x + "," + particles[i].y + "," + (int)(particles[i].orientation)) != null) {
			int count = particlesPerCell.get(particles[i].x + "," + particles[i].y + "," + (int)(particles[i].orientation));
			count++;
			particlesPerCell.put(particles[i].x + "," + particles[i].y + "," + (int)(particles[i].orientation), count);
			if (count > max) {
				max = count;
				temp = particles[i].x + "," + particles[i].y + "," + (int)(particles[i].orientation);
			}
		} else {
			particlesPerCell.put(particles[i].x + "," + particles[i].y + "," + (int)(particles[i].orientation), 1);
		}
	}
	bestCell = temp;
  }
  
  public String getBestCell() {
	  return bestCell;
  }
  
  public int getBestNumOfParticles() {
	  return particlesPerCell.get(bestCell);
  }
  
}
