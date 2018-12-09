package ExcitorBehavior;
import java.util.ArrayList;
import java.util.Iterator;

import processing.core.PVector;

class ActuatorSystem {

	private final excitorBehaviorEngine excitorBehaviorEngine;
	ArrayList<Actuator> actuators;

	ActuatorSystem(excitorBehaviorEngine excitorBehaviorEngine) {
		this.excitorBehaviorEngine = excitorBehaviorEngine;
		actuators = new ArrayList<Actuator>();
	}

	public void addActuator(int _index, PVector _location) {
		actuators.add(new Actuator(this.excitorBehaviorEngine, _index, _location));
		this.excitorBehaviorEngine.actuatorCount += 1;
	}

	public void run() {
		Iterator<Actuator> it = actuators.iterator();
		while (it.hasNext()) {
			Actuator a = it.next();
			a.run();
		}
	}
}