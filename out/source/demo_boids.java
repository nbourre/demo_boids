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

boolean debug = false;

public void setup () {
  
  currentTime = millis();
  previousTime = millis();
  
  flock = new ArrayList<Mover>();
  
  for (int i = 0; i < flockSize; i++) {
    Mover m = new Mover(new PVector(random(0, width), random(0, height)), new PVector(random (-2, 2), random(-2, 2)));
    m.fillColor = color(random(255), random(255), random(255));
    flock.add(m);
  }

  flock.get(0).debug = true;
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

public void keyPressed() {
  switch (key) {
    case 'd':
      flock.get(0).debug = !flock.get(0).debug;
      break;
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
  float topSpeed = 5;
  float topSteer = 0.05f;
  
  float mass = 1;
  
  float theta = 0;
  float r = 10; // Rayon du boid
  
  float radiusSeparation = 10 * r;
  float radiusAlignment = 20 * r;
  float radiusCohesion = 30 * r;

  float weightSeparation = 2;
  float weightAlignment = 1;
  float weightCohesion = 1;
  
  PVector steer;
  PVector sumAlignment;
  PVector sumCohesion;

  PVector zeroVector = new PVector(0, 0);
  

  boolean debug = false;
  int msgCount = 0;
  String debugMessage = "";
  
  Mover () {
    location = new PVector();
    velocity = new PVector();
    acceleration = new PVector();
  }
  
  Mover (PVector loc, PVector vel) {
    this.location = loc;
    this.velocity = vel;
    this.acceleration = new PVector (0 , 0);
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
    PVector alignment = align(boids);
    PVector cohesion = cohesion(boids);
    
    separation.mult(weightSeparation);
    alignment.mult(weightSeparation);
    cohesion.mult(weightCohesion);

    applyForce(separation);
    applyForce(alignment);
    applyForce(cohesion);
  }
  
  
  public void update(float deltaTime) {
    checkEdges();

    velocity.add (acceleration);
    velocity.limit(topSpeed);

    theta = velocity.heading() + radians(90);

    location.add (velocity);

    acceleration.mult (0);      
  }
  
  public void display() {
    noStroke();
    fill (fillColor);
    
    pushMatrix();
    translate(location.x, location.y);
    rotate (theta);
    
    beginShape(TRIANGLES);
      vertex(0, -r * 2);
      vertex(-r, r * 2);
      vertex(r, r * 2);
    
    endShape();
    
    popMatrix();
    
    if (debug || this.debug) {
      renderDebug();
    }
  }
  
  public PVector separate (ArrayList<Mover> boids) {
    if (steer == null) {
      steer = new PVector(0, 0, 0);
    }
    else {
      steer.setMag(0);
    }
    
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

  public PVector align (ArrayList<Mover> boids) {

    if (sumAlignment == null) {
      sumAlignment = new PVector();      
    } else {
      sumAlignment.mult(0);
    }

    int count = 0;

    for (Mover other : boids) {
      float d = PVector.dist(this.location, other.location);

      if (d > 0 && d < radiusAlignment) {
        sumAlignment.add(other.velocity);
        count++;
      }
    }

    if (count > 0) {
      sumAlignment.div((float)count);
      sumAlignment.setMag(topSpeed);

      PVector steer = PVector.sub(sumAlignment, this.velocity);
      steer.limit(topSteer);

      return steer;
    } else {
      return zeroVector;
    }
  }

   // Méthode qui calcule et applique une force de braquage vers une cible
  // STEER = CIBLE moins VITESSE
  public PVector seek (PVector target) {
    // Vecteur différentiel vers la cible
    PVector desired = PVector.sub (target, this.location);
    
    // VITESSE MAXIMALE VERS LA CIBLE
    desired.setMag(topSpeed);
    
    // Braquage
    PVector steer = PVector.sub (desired, velocity);
    steer.limit(topSteer);
    
    return steer;    
  }

  public PVector cohesion (ArrayList<Mover> boids) {
    if (sumCohesion == null) {
      sumCohesion = new PVector();      
    } else {
      sumCohesion.mult(0);
    }

    int count = 0;

    for (Mover other : boids) {
      float d = PVector.dist(location, other.location);

      if (d > 0 && d < radiusCohesion) {
        sumCohesion.add(other.location);
        count++;
      }
    }

    if (count > 0) {
      sumCohesion.div(count);

      return seek(sumCohesion);
    } else {
      return zeroVector;
    }
    
  }
  
  public void applyForce (PVector force) {
    PVector f;
    
    if (mass != 1)
      f = PVector.div (force, mass);
    else
      f = force;
   
    this.acceleration.add(f);    
  }
  
  public void renderDebug() {
    pushMatrix();
      noFill();
      translate(location.x, location.y);
      
      strokeWeight(1);
      stroke (100, 0, 0);
      ellipse (0, 0, radiusSeparation, radiusSeparation);

      stroke (0, 100, 0);
      ellipse (0, 0, radiusAlignment, radiusAlignment);

      stroke (0, 0, 200);
      ellipse (0, 0, radiusCohesion, radiusCohesion);
      
    popMatrix();

    if (msgCount % 60 == 0) {
      msgCount = 0;

      if (debugMessage != "") {
        println(debugMessage);
        debugMessage = "";
      }
    }

    msgCount++;
  }
}
  public void settings() {  fullScreen(P2D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "demo_boids" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}