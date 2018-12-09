package ExcitorBehavior;
import processing.core.PVector;

class AttractorSystem {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;

AttractorSystem(excitorBehaviorEngine excitorBehaviorEngine) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	for (int i=0; i<this.excitorBehaviorEngine.attractorCount; i++) {
      this.excitorBehaviorEngine.attractor[i] = new Attractor(this.excitorBehaviorEngine, new PVector(this.excitorBehaviorEngine.random(8)-4, this.excitorBehaviorEngine.random(4)-2));
    }
  }

  public void display() {
    if (this.excitorBehaviorEngine.showAttractorShape) {
      for (int i=0; i<this.excitorBehaviorEngine.attractorCount; i++) {
        this.excitorBehaviorEngine.attractor[i].run();
      }
      this.excitorBehaviorEngine.noStroke();
      this.excitorBehaviorEngine.fill(255, 50);
      this.excitorBehaviorEngine.beginShape();
      for (int i=0; i<this.excitorBehaviorEngine.attractor.length; i++) {
        this.excitorBehaviorEngine.vertex(this.excitorBehaviorEngine.attractor[i].locationPix.x, this.excitorBehaviorEngine.attractor[i].locationPix.y);
      }
      this.excitorBehaviorEngine.endShape(excitorBehaviorEngine.CLOSE);
    }
  }
}