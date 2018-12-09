package ExcitorBehavior;
import processing.core.PVector;

class SphereUnit {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;
PVector location, locationPix;
  int index;
  String id;
  PVector[] vertexLocations, vertexLocationsPix;
  float radius;

  SphereUnit(excitorBehaviorEngine excitorBehaviorEngine, int _index, String _id, PVector _location) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	index = _index;
    id = _id;
    location = _location;
    radius = 0.3f;
    vertexLocations = new PVector[this.excitorBehaviorEngine.sphereUnitVertexCount];

    for (int i=0; i<this.excitorBehaviorEngine.sphereUnitVertexCount; i++) { 
      vertexLocations[i] = new PVector();
      vertexLocations[i] = this.excitorBehaviorEngine.polygonVertexPoint(this.excitorBehaviorEngine.sphereUnitVertexCount, radius, radius, i);
      vertexLocations[i].add(location);
      this.excitorBehaviorEngine.actuatorSystem.addActuator(this.excitorBehaviorEngine.actuatorSystem.actuators.size(), vertexLocations[i]);
    }

    locationPix = this.excitorBehaviorEngine.convertVectorToPixelSpace(location);
    vertexLocationsPix = this.excitorBehaviorEngine.convertVectorToPixelSpaceArray(vertexLocations);
  }

  public void run() {
    display();
  }

  public void display() {
    this.excitorBehaviorEngine.noFill();
    this.excitorBehaviorEngine.stroke(100);
    this.excitorBehaviorEngine.beginShape();
    for (int i=0; i<this.excitorBehaviorEngine.sphereUnitVertexCount; i++) {
      this.excitorBehaviorEngine.vertex(vertexLocationsPix[i].x, vertexLocationsPix[i].y);
    }
    this.excitorBehaviorEngine.endShape(excitorBehaviorEngine.CLOSE);
    this.excitorBehaviorEngine.fill(255);
    this.excitorBehaviorEngine.textAlign(excitorBehaviorEngine.CENTER, excitorBehaviorEngine.CENTER);
    this.excitorBehaviorEngine.text(id, locationPix.x, locationPix.y);
  }
}