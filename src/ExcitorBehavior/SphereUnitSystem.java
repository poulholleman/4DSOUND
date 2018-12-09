package ExcitorBehavior;
import processing.core.PApplet;
import processing.core.PVector;

class SphereUnitSystem {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;
PVector[] sphereUnitLocations;

  SphereUnitSystem(excitorBehaviorEngine excitorBehaviorEngine) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	sphereUnitLocations = new PVector[this.excitorBehaviorEngine.sphereUnitInfo.length];
    for (int i=0; i<this.excitorBehaviorEngine.sphereUnitInfo.length; i++) {
      String id = excitorBehaviorEngine.str(PApplet.parseInt(this.excitorBehaviorEngine.sphereUnitInfo[i][0]))+"-"+excitorBehaviorEngine.str(PApplet.parseInt(this.excitorBehaviorEngine.sphereUnitInfo[i][1]));
      PVector location = new PVector(this.excitorBehaviorEngine.sphereUnitInfo[i][2], this.excitorBehaviorEngine.sphereUnitInfo[i][4]);
      this.excitorBehaviorEngine.sphereUnit[i] = new SphereUnit(this.excitorBehaviorEngine, i, id, location);
    }
  }
}