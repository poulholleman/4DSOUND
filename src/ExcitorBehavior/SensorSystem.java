package ExcitorBehavior;
import processing.core.PVector;

class SensorSystem {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;
PVector[] sensorLocations;

  SensorSystem(excitorBehaviorEngine excitorBehaviorEngine, PVector _location, int _sensorCount, int _dimX, int _dimY) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	sensorLocations = this.excitorBehaviorEngine.polygon(_sensorCount, _dimX, _dimY);
    for (int i=0; i<this.excitorBehaviorEngine.sensorCount; i++) {
      sensorLocations[i] = sensorLocations[i].add(_location);
      this.excitorBehaviorEngine.sensor[i] = new Sensor(this.excitorBehaviorEngine, i, sensorLocations[i]);
    }
  }

  public void display() {
    for (int i=0; i<this.excitorBehaviorEngine.sensorCount; i++) {
      this.excitorBehaviorEngine.sensor[i].run();
    }
  }
}