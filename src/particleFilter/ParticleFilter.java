package particleFilter;

import java.awt.Point;
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
  int totalParticles;
  
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
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++ ) {
        particles[i] = new Particle(landmarks, width, height, i, j, 0);//north
        particles[i] = new Particle(landmarks, width, height, i, j, Math.PI/2); //east
        particles[i] = new Particle(landmarks, width, height, i, j, Math.PI); //south
        particles[i] = new Particle(landmarks, width, height, i, j, 3*Math.PI/2); //west
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
  
  public void resample(float[] measurement) {
    Particle[] newParticles = new Particle[totalParticles];
    
    for (int i = 0; i < totalParticles; i++) {
      particles[i].calculateProbability(measurement);
    }
    
    float B = 0f;
    
    Particle best = getBestParticle();
    int index = (int) randNum.nextFloat() * totalParticles;
    for (int i = 0; i < totalParticles; i++) {
      B += randNum.nextFloat() * 2f * best.probability;
      while (B > particles[index].probability) {
        B -= particles[index].probability;
        index = circle(index + 1, totalParticles);
      }
      newParticles[i] = new Particle(particles[index].landmarks,
                                     particles[index].worldWidth,
                                     particles[index].worldHeight,
                                     particles[index].x,
                                     particles[index].y,
                                     particles[index].orientation);
      newParticles[i].setProb(particles[index].probability);
    }
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
      if (particles[i].probability > particle.probability) {
        particle = particles[i];
      }
    }
    return particle;
  }
  
}
