package ExcitorBehavior;

import processing.core.PVector;

class Actuator {
	
	private final excitorBehaviorEngine excitorBehaviorEngine;
	PVector location, locationPix;
	int index;
	float intensity, intensityRelease;
	boolean showText = true;

	Actuator(excitorBehaviorEngine excitorBehaviorEngine, int _index, PVector _location) {
		this.excitorBehaviorEngine = excitorBehaviorEngine;
		index = _index + 1;
		location = _location;
		intensityRelease = 0.01f;
	}

	public void run() {
		applyIntensity();
		display();
	}

	public void applyIntensity() {
		if (intensity > 1)
			intensity = 1;
		locationPix = excitorBehaviorEngine.convertVectorToPixelSpace(location);
		intensity *= excitorBehaviorEngine.masterIntensity;
		intensity -= intensityRelease;
		if (intensity < 0)
			intensity = 0;
	}

	public void display() {
		if (excitorBehaviorEngine.showActuators) {
			excitorBehaviorEngine.noStroke();
			excitorBehaviorEngine.fill(intensity * 200 + 55, 150);
			excitorBehaviorEngine.ellipse(locationPix.x, locationPix.y, 25, 25);
		}

		if (showText) {
			excitorBehaviorEngine.fill(255);
			excitorBehaviorEngine.textAlign(excitorBehaviorEngine.CENTER, excitorBehaviorEngine.CENTER);
			excitorBehaviorEngine.textSize(10);
			excitorBehaviorEngine.text(index, locationPix.x, locationPix.y);
		}
	}
}