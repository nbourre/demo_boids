int currentTime;
int previousTime;
int deltaTime;

ArrayList<Mover> flock;
int flockSize = 50;

void setup () {
  size (800, 600);
  currentTime = millis();
  previousTime = millis();
  
  initBoids();
}

void initBoids() {
  flock = new ArrayList<Mover>();
  
  for (int i = 0; i < flockSize; i++) {
    Mover m = new Mover(new PVector(random(0, width), random(0, height)), new PVector(random (-5, 5), random(-5, 5)));
    m.fillColor = color(random(255), random(255), random(255));
    flock.add(m);
  }
}

void draw () {
  currentTime = millis();
  deltaTime = currentTime - previousTime;
  previousTime = currentTime;

  
  update(deltaTime);
  display();  
}

/***
  The calculations should go here
*/
void update(int delta) {
  
  for (Mover m : flock) {
    m.flock(flock);
    m.update(delta);
  }
}

/***
  The rendering should go here
*/
void display () {
  background(0);
  
  for (Mover m : flock) {
    m.display();
  }
}

boolean debugMode = false;
color oldColor;

void keyPressed() {
  if (key == 'i') {
    debugMode = !debugMode;
    
    // Mets en évidence le dernier boids
    for (Mover m : flock) {
    
      if (debugMode) {
        if (m != flock.get(flock.size() - 1)) {
          m.setAlpha(100);
        } else {
          oldColor = m.fillColor;
          m.fillColor = color (0, 255, 0);
          m.setDebugMode(debugMode);
        }
      } else {
        if (m == flock.get(flock.size() - 1)) {
          m.fillColor = oldColor;
          m.setDebugMode(debugMode);
        }
        
        m.setAlpha(255);       
      }
    }
  }
  
  if (key == 'r') {
    initBoids();
  }
}
