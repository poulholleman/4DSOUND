package ExcitorBehavior;
import processing.core.PVector;

class Excitor {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;
PVector location, locationPix, velocity, acceleration, force;
  float threshold, curvature, speedLimit, mass, lifespan;

  Excitor(excitorBehaviorEngine excitorBehaviorEngine, PVector _location) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	location = _location.copy();
    velocity = new PVector();
    acceleration = new PVector();
    mass = this.excitorBehaviorEngine.random(1);
    threshold = 1;
    curvature = 1;
    speedLimit = 3;
    lifespan = 255;
  }

  public void run() {   
    update();
    applyAtrractorForce();
    excite();
    checkEdges();
    if (this.excitorBehaviorEngine.showExcitors) display();
  }

  public void update() {   
    velocity.add(acceleration);
    velocity.limit(speedLimit);
    location.add(velocity);
    locationPix = this.excitorBehaviorEngine.convertVectorToPixelSpace(location);
    acceleration.mult(0);
    lifespan -= 0.5f;
  }

  public void applyAtrractorForce() {
    for (int i=0; i<this.excitorBehaviorEngine.attractor.length; i++) {
      PVector force = this.excitorBehaviorEngine.attractor[i].location.copy();
      force.sub(location);
      force.normalize();
      force.mult(this.excitorBehaviorEngine.attractorForce);
      applyForce(force);
    }
  }

  public void display() {
    this.excitorBehaviorEngine.stroke(lifespan, 50);
    this.excitorBehaviorEngine.noFill();
    this.excitorBehaviorEngine.ellipse(locationPix.x, locationPix.y, threshold*200, threshold*200);

    this.excitorBehaviorEngine.textAlign(excitorBehaviorEngine.CENTER, excitorBehaviorEngine.CENTER);
    this.excitorBehaviorEngine.textSize(14);
    this.excitorBehaviorEngine.fill(255);
  }

  public void excite() {
    for (int i=0; i<this.excitorBehaviorEngine.actuatorSystem.actuators.size(); i++) {
      float distance = location.dist(this.excitorBehaviorEngine.actuatorSystem.actuators.get(i).location);
      if (distance <= threshold) this.excitorBehaviorEngine.actuatorSystem.actuators.get(i).intensity += excitorBehaviorEngine.pow(1 - (distance/threshold), curvature) * 0.1f;// (lifespan/255);
    }
  }

  public void applyForce(PVector _force) {
    PVector force = _force.copy();
    force.mult(mass+0.5f);
    acceleration.add(force);
  }  
  public void checkEdges() {
    if (locationPix.x < -(this.excitorBehaviorEngine.width/2)) locationPix.x = this.excitorBehaviorEngine.width/2;
    if (locationPix.x > this.excitorBehaviorEngine.width/2) locationPix.x = -(this.excitorBehaviorEngine.width/2);
    if (locationPix.y < -(this.excitorBehaviorEngine.height/2)) locationPix.y = this.excitorBehaviorEngine.height/2;
    if (locationPix.y > this.excitorBehaviorEngine.height/2) locationPix.y = -(this.excitorBehaviorEngine.height/2);
  }

  public boolean isDead() {
    if (lifespan < 0.0f) {
      return true;
    } else {
      return false;
    }
  }
}