package ExcitorBehavior;
import processing.core.PVector;

class Sensor {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;
PVector location;
  float intensity;
  int index, fillAlpha, t;

  Sensor(excitorBehaviorEngine excitorBehaviorEngine, int _index, PVector _location) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	index = _index;
    location = _location.copy();
    intensity = 1;
    fillAlpha = 0;
  }

  public void run() {
    if ((this.excitorBehaviorEngine.millis()-t) < 100) {
      fillAlpha = 255;
    } else {
      fillAlpha = 0;
    }

    this.excitorBehaviorEngine.fill(255, 0, 0, fillAlpha);
    this.excitorBehaviorEngine.stroke(intensity*200+50, 0, 0);
    PVector locationPix = this.excitorBehaviorEngine.convertVectorToPixelSpace(location);
    this.excitorBehaviorEngine.ellipse(locationPix.x, locationPix.y, 30, 30);
    this.excitorBehaviorEngine.textAlign(excitorBehaviorEngine.CENTER, excitorBehaviorEngine.CENTER);
    this.excitorBehaviorEngine.fill(255);
    this.excitorBehaviorEngine.textSize(14);
    this.excitorBehaviorEngine.text(index+1, locationPix.x, locationPix.y);
  }

  public void trigger() {
    this.excitorBehaviorEngine.excitorSystem.addExcitor(this.excitorBehaviorEngine.sensorSystem.sensorLocations[index]);
    t = this.excitorBehaviorEngine.millis();
  }
}