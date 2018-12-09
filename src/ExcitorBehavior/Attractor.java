package ExcitorBehavior;
import processing.core.PVector;

class Attractor {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;
PVector location, locationPix, mouse;
  int size;
  Boolean dragged, show;

  Attractor(excitorBehaviorEngine excitorBehaviorEngine, PVector _location) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	location = _location;
    size = 20;
    dragged = false;
  }

  public void run() {
    mouse = new PVector(this.excitorBehaviorEngine.mouseX-this.excitorBehaviorEngine.width/2, this.excitorBehaviorEngine.mouseY-this.excitorBehaviorEngine.height/2);
    dragged();
    if (this.excitorBehaviorEngine.showAttractors) display();
  }

  public void display() {
    if (dragged) {
      location = mouse.copy().mult(0.01f);
    }
    locationPix = this.excitorBehaviorEngine.convertVectorToPixelSpace(location);
    this.excitorBehaviorEngine.stroke(0, 255, 0);
    this.excitorBehaviorEngine.noFill();
    if (dragged) this.excitorBehaviorEngine.fill(0, 255, 0);
    this.excitorBehaviorEngine.ellipse(locationPix.x, locationPix.y, size, size);
  }

  public void dragged() {   
    if (!dragged && this.excitorBehaviorEngine.mousePressed && locationPix.dist(mouse)<=size/2) {
      dragged = true;
    } else { 
      if (!this.excitorBehaviorEngine.mousePressed) dragged = false;
    }
  }
}