import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class demo_boids extends PApplet {

int currentTime;
int previousTime;
int deltaTime;

ArrayList<Mover> flock;
int flockSize = 50;

public void setup () {
  
  currentTime = millis();
  previousTime = millis();
  
  flock = new ArrayList<Mover>();
  
  for (int i = 0; i < flockSize; i++) {
    Mover m = new Mover(new PVector(random(0, width), random(0, height)), new PVector(random (-5, 5), random(-5, 5)));
    m.fillColor = color(random(255), random(255), random(255));
    flock.add(m);
  }
}

public void draw () {
  currentTime = millis();
  deltaTime = currentTime - previousTime;
  previousTime = currentTime;

  
  update(deltaTime);
  display();  
}

/***
  The calculations should go here
*/
public void update(int delta) {
  
  for (Mover m : flock) {
    m.flock(flock);
    m.update(delta);
  }
}

/***
  The rendering should go here
*/
public void display () {
  background(0);
  
  for (Mover m : flock) {
    m.display();
  }
}
abstract class GraphicObject {
  PVector location;
  PVector velocity;
  PVector acceleration;
  
  int fillColor = color (255);
  int strokeColor = color (255);
  float strokeWeight = 1;
  
  public abstract void update(float deltaTime);
  
  public abstract void display();
  
}
class Mover extends GraphicObject {
  float topSpeed = 2;
  float topSteer = 0.03f;
  
  float theta = 0;
  float r = 10; // Rayon du boid
  
  float radiusSeparation = 10 * r;
  
  Mover () {
    location = new PVector();
    velocity = new PVector();
    acceleration = new PVector();
  }
  
  Mover (PVector loc, PVector vel) {
    
    
    this.location = loc;
    this.velocity = vel;
    this.acceleration = new PVector (0 , 0);
    this.topSpeed = 100;
  }
  
  public void checkEdges() {
    if (location.x < 0) {
      location.x = width - r;
    } else if (location.x + r> width) {
      location.x = 0;
    }
    
    if (location.y < 0) {
      location.y = height - r;
    } else if (location.y + r> height) {
      location.y = 0;
    }
  }
  
  public void flock (ArrayList<Mover> boids) {
    PVector separation = separate(boids);
    
    applyForce(separation);
  }

  public void update(float deltaTime) {
    checkEdges();
    
    velocity.add (acceleration);
    velocity.limit(topSpeed);
    location.add (velocity);

    acceleration.mult (0);      
  }
  
  public void display() {
    noStroke();
    fill (fillColor);
    
    theta = velocity.heading() + radians(90);
    
    pushMatrix();
    translate(location.x, location.y);
    rotate (theta);
    
    beginShape(TRIANGLES);
      vertex(0, -r * 2);
      vertex(-r, r * 2);
      vertex(r, r * 2);
    
    endShape();
    
    popMatrix();  
  }
  
  public PVector separate (ArrayList<Mover> boids) {
    PVector steer = new PVector(0, 0, 0);
    
    int count = 0;
    
    for (Mover other : boids) {
      float d = PVector.dist(location, other.location);
      
      if (d > 0 && d < radiusSeparation) {
        PVector diff = PVector.sub(location, other.location);
        
        diff.normalize();
        diff.div(d);
        
        steer.add(diff);
        
        count++;
      }
    }
    
    if (count > 0) {
      steer.div(count);
    }
    
    if (steer.mag() > 0) {
      steer.setMag(topSpeed);
      steer.sub(velocity);
      steer.limit(topSteer);
    }
    
    return steer;
  }
}
  public void settings() {  size (800, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "demo_boids" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
