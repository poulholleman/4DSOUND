package ExcitorBehavior;
import java.util.ArrayList;
import java.util.Iterator;

import processing.core.PVector;

class ExcitorSystem {

  /**
	 * 
	 */
	private final excitorBehaviorEngine excitorBehaviorEngine;
ArrayList<Excitor> excitors;
  PVector origin;

  ExcitorSystem(excitorBehaviorEngine excitorBehaviorEngine) {
    this.excitorBehaviorEngine = excitorBehaviorEngine;
	excitors = new ArrayList<Excitor>();
  }

  public void addExcitor(PVector _location) {
    excitors.add(new Excitor(this.excitorBehaviorEngine, _location));
    this.excitorBehaviorEngine.excitorCount += 1;
  }

  public void run() {
    Iterator<Excitor> it = excitors.iterator();
    while (it.hasNext()) {
      Excitor e = it.next();
      e.run();
      if (e.isDead()) {
        it.remove();
        this.excitorBehaviorEngine.excitorCount -= 1;
      }
    }
  }
}