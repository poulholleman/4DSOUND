// node makedoc.js --path "/Users/pholleman/Google Drive/_development/_GitHub/LASG-4D" --ext=.java
// node makedoc.js --path /Users/pholleman/Desktop/temp/LASG-4D --ext=.java

/*!
 * \author Poul Holleman
 */

package ExcitorBehavior;

import processing.core.*;
import oscP5.*;
import netP5.*;
import peasy.*;
import java.util.*;

public class excitorBehaviorEngine extends PApplet {

	String appVersion = "v0.0.4";

	PeasyCam cam;

	OscP5 oscar;
	NetAddress FOUR_D_ENGINE;
	NetAddress MASTER_LAPTOP;
	NetAddress MAX_PATCH;
	NetAddress ENCEFALO;

	int sphereUnitVertexCount = 6;
	int attractorCount = 3;

	float attractorForce = 0.0003f;
	boolean showExcitors = true;
	boolean showAttractors = true;
	boolean showActuators = true;
	boolean showAttractorShape = true;

	boolean sendExcPosDimTo4Dengine = false;
	boolean sendExcLifespanTo4Dengine = false;

	int[] IRthresholds = { 400, 400, 400 };
	int[] IRvalues = new int[3];

	float masterIntensity = 1;

	PVector origin = new PVector(0, 0);

	boolean gate = true;

	// Sphere Unit Info: { Triad, Unit, posX, posY, posZ }
	float sphereUnitInfo[][] = { //
			{ 1, 1, -3, 2, -0.5f }, //
			{ 1, 2, -1.5f, 2, 0.5f }, //
			{ 1, 3, -3, 2, 2 }, //
			{ 2, 1, -0.5f, 2, -1.5f }, //
			{ 2, 2, 0.5f, 2, 1 }, //
			{ 2, 3, -0.5f, 2, 3 }, //
			{ 3, 1, 2, 2, -1.5f }, //
			{ 3, 3, 2, 2, 3 }, //
			{ 4, 1, 4, 2, -1 }, //
			{ 4, 3, 4, 2, 2.5f }, //
			{ 5, 1, 5, 2, -0.5f }, //
			{ 5, 2, 5.25f, 2, 0.5f }, //
			{ 5, 3, 5, 2, 1.5f } //
	};

	// Sensor Info: { posX, posY, posZ }
	float sensorInfo[][] = { //
			{ 0.5f, 2, 1 }, //
			{ -2.5f, 2, 0 }, //
			{ 4, 2, 1 } //
	};

	ExcitorSystem excitorSystem;
	SensorSystem sensorSystem;
	SphereUnitSystem sphereUnitSystem;
	AttractorSystem attractorSystem;
	ActuatorSystem actuatorSystem;

	public void setup() {
		// fullScreen(P3D);

		frameRate(60);

		// cam = new PeasyCam(this, width/2, height/2, 0, 800);

		oscar = new OscP5(this, 3000); // listen port
		FOUR_D_ENGINE = new NetAddress("127.0.0.1", 2000);
		MASTER_LAPTOP = new NetAddress("10.14.4.181", 3002);
		MAX_PATCH = new NetAddress("127.0.0.1", 4000);
		ENCEFALO = new NetAddress("127.0.0.1", 6000);
		// ENCEFALO = new NetAddress("10.14.4.124", 5000);
		// FOUR_D_ENGINE = new NetAddress("10.14.4.134", 2000);
		// MASTER_LAPTOP = new NetAddress("10.14.4.181", 3001);

		actuatorSystem = new ActuatorSystem();
		excitorSystem = new ExcitorSystem();
		sphereUnitSystem = new SphereUnitSystem();
		attractorSystem = new AttractorSystem();
		sensorSystem = new SensorSystem();

		for (int i = 0; i < attractorCount; i++) {
			attractorSystem.addAttractor(new PVector(random(4), random(4)));
		}

		initOscIn();

		strokeWeight(2);
	}

	public void draw() {

		// randomly generate Excitors for Demo Andi
//		float chance = 0;
//		if (frameCount % 60 == 0) {
//			chance = random(1);
//			if (chance < 0.25)
//				sensorSystem.sensors.get((int) (random(3))).trigger();
//		}

		background(25);

		pushMatrix();
		translate(width / 2, height / 2);

		sensorSystem.run();
		attractorSystem.run();
		excitorSystem.run();
		actuatorSystem.run();
		sphereUnitSystem.run();

		oscOut();
		display();
	}

	class Actuator {

		PVector location, locationPix;
		int index;
		float intensity, intensityRelease;
		boolean showText = true;

		Actuator(int _index, PVector _location) {
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

			intensity *= masterIntensity;
			intensity -= intensityRelease;

			if (intensity < 0)
				intensity = 0;
		}

		public void display() {

			locationPix = convertVectorToPixelSpace(location);

			if (showActuators) {
				noStroke();
				fill(intensity * 200 + 55, 150);
				ellipse(locationPix.x, locationPix.y, 25, 25);
			}

			if (showText) {
				fill(255);
				textAlign(CENTER, CENTER);
				textSize(10);
				text(index, locationPix.x, locationPix.y);
			}
		}
	}

	class ActuatorSystem {

		ArrayList<Actuator> actuators;

		ActuatorSystem() {
			actuators = new ArrayList<Actuator>();
		}

		public void addActuator(int _index, PVector _location) {
			actuators.add(new Actuator(_index, _location));
			// actuatorCount += 1;
		}

		public void run() {
			OscMessage actuatorIntensities = new OscMessage("/from_ExcitorBehaviour/actuatorIntensities");
			Iterator<Actuator> it = actuators.iterator();
			while (it.hasNext()) {
				Actuator a = it.next();
				a.run();
				actuatorIntensities.add(a.intensity);
			}
			oscar.send(actuatorIntensities, MAX_PATCH);
		}
	}

	class Attractor {

		PVector location, locationPix, mouse;
		int size, index;
		Boolean dragged, show;

		Attractor(int _index, PVector _location) {
			index = _index;
			location = _location;
			size = 20;
			dragged = false;
		}

		public void run() {
			mouse = new PVector(mouseX - width / 2, mouseY - height / 2);
			dragged();
			if (showAttractors)
				display();
		}

		public void display() {
			if (dragged) {
				location = mouse.copy().mult(0.01f);
			}
			locationPix = convertVectorToPixelSpace(location);
			stroke(0, 255, 0);
			noFill();
			if (dragged)
				fill(0, 255, 0);
			ellipse(locationPix.x, locationPix.y, size, size);
		}

		public void dragged() {
			if (!dragged && mousePressed && locationPix.dist(mouse) <= size / 2) {
				dragged = true;
			} else {
				if (!mousePressed)
					dragged = false;
			}
		}
	}

	class AttractorSystem {

		ArrayList<Attractor> attractors;

		AttractorSystem() {
			attractors = new ArrayList<Attractor>();
		}

		public void addAttractor(PVector _location) {
			attractors.add(new Attractor(attractors.size(), _location));
		}

		public void run() {
			if (showAttractorShape) {

				Iterator<Attractor> it = attractors.iterator();
				while (it.hasNext()) {
					Attractor a = it.next();
					a.run();
				}

				noStroke();
				fill(0, 255, 0, 10);
				beginShape();
				for (int i = 0; i < attractors.size(); i++) {
					vertex(attractors.get(i).locationPix.x, attractors.get(i).locationPix.y);
				}
				endShape(CLOSE);
			}
		}
	}

	class Excitor {

		PVector location, locationPix, velocity, acceleration, force;
		float threshold, curvature, speedLimit, mass, lifespan;
		int index;

		Excitor(int _index, PVector _location) {
			index = _index;
			location = _location.copy();
			velocity = new PVector();
			acceleration = new PVector();
			mass = random(1);
			threshold = random(2);
			curvature = 4;
			speedLimit = 0.05f;
			lifespan = 255f;
		}

		public void run() {
			update();
			applyAtrractorForce();
			excite();
			checkEdges();
			if (showExcitors)
				display();
			oscOut();
		}

		public void update() {
			velocity.add(acceleration);
			velocity.limit(speedLimit);
			location.add(velocity);
			locationPix = convertVectorToPixelSpace(location);
			acceleration.mult(0);
			lifespan -= 0.5f;
		}

		public void oscOut() {
			OscMessage excitorPosition = new OscMessage("/from_ExcitorBehaviour/excitor/" + index + "/position");
			excitorPosition.add(location.x); // width
			excitorPosition.add(2); // height
			excitorPosition.add(location.y); // depth
			oscar.send(excitorPosition, ENCEFALO);

			if (sendExcPosDimTo4Dengine) {
				OscMessage excitorPosition4Dpos = new OscMessage("/source" + index + "/position");
				excitorPosition4Dpos.add(location.x); // width
				excitorPosition4Dpos.add(2); // height
				excitorPosition4Dpos.add(location.y); // depth
				oscar.send(excitorPosition4Dpos, FOUR_D_ENGINE);

				OscMessage excitorRadius4Ddim = new OscMessage("/source" + index + "/dimensions");
				excitorRadius4Ddim.add(threshold+1);
				excitorRadius4Ddim.add(threshold+1);
				excitorRadius4Ddim.add(threshold+1);
				oscar.send(excitorRadius4Ddim, FOUR_D_ENGINE);
			}

			OscMessage excitorRadius = new OscMessage("/from_ExcitorBehaviour/excitor/" + index + "/radius");
			excitorRadius.add(threshold);
			oscar.send(excitorRadius, ENCEFALO);

			OscMessage excitorLifespan = new OscMessage("/from_ExcitorBehaviour/excitor/" + index + "/lifespan");
			excitorLifespan.add(lifespan);
			oscar.send(excitorLifespan, ENCEFALO);

//			if (sendExcLifespanTo4Dengine) {
//				OscMessage excitorLifespan4Dgain = new OscMessage("/source" + index + "/gain");
//				excitorLifespan4Dgain.add(lifespan / 255.0f);
//				oscar.send(excitorLifespan4Dgain, FOUR_D_ENGINE);
//			}
		}

		public void applyAtrractorForce() {
			for (int i = 0; i < attractorSystem.attractors.size(); i++) {
				PVector force = attractorSystem.attractors.get(i).location.copy();
				force.sub(location);
				force.normalize();
				force.mult(attractorForce);
				applyForce(force);
			}
		}

		public void display() {
			stroke(lifespan, 50);
			fill(155, 155, 0, 10 * (lifespan / 255));
			ellipse(locationPix.x, locationPix.y, threshold * 200, threshold * 200);
		}

		public void excite() {
			for (int i = 0; i < actuatorSystem.actuators.size(); i++) {
				float distance = location.dist(actuatorSystem.actuators.get(i).location);
				if (distance <= threshold) {
					float forceScalar = pow(1 - (distance / threshold), curvature) * (lifespan / 255);
					actuatorSystem.actuators.get(i).intensity += forceScalar;
					// forceScalar is added in order to accumulate the excitations
					// of multiple Excitors within one Actuator
					// TODO needs fine tuning regarding the intensity slope
				}
			}
		}

		public void applyForce(PVector _force) {
			PVector force = _force.copy();
			force.mult(mass + 0.5f);
			acceleration.add(force);
		}

		public void checkEdges() {
			if (locationPix.x < -(width / 2)) {
				locationPix.x = width / 2;
				location.x = locationPix.x * 0.01f;
			}
			if (locationPix.x > width / 2) {
				locationPix.x = -(width / 2);
				location.x = locationPix.x * 0.01f;
			}
			if (locationPix.y < -(height / 2)) {
				locationPix.y = height / 2;
				location.y = locationPix.y * 0.01f;
			}
			if (locationPix.y > height / 2) {
				locationPix.y = -(height / 2);
				location.y = locationPix.y * 0.01f;
			}
		}

		public boolean isDead() {
			if (lifespan <= 0.0f) {
				return true;
			} else {
				return false;
			}
		}
	}

	class ExcitorSystem {

		ArrayList<Excitor> excitors;
		// PVector origin;

		ExcitorSystem() {
			excitors = new ArrayList<Excitor>();
		}

		public void addExcitor(PVector _location) {
			excitors.add(new Excitor(excitors.size() + 1, _location));
		}

		public void run() {
			Iterator<Excitor> it = excitors.iterator();
			while (it.hasNext()) {
				Excitor e = it.next();
				e.run();
				if (e.isDead()) {
					it.remove();
				}
			}
		}
	}

	public void initOscIn() {

		// From Max Patch
		oscar.plug(this, "generateExcitor", "/to_ExcitorBehaviour/generateExcitor");
		oscar.plug(this, "setExcitorCurvature", "/to_ExcitorBehaviour/setExcitorCurvature");
		oscar.plug(this, "setExcitorThresholds", "/to_ExcitorBehaviour/setExcitorThresholds");
		oscar.plug(this, "setExcitorMasterIntensity", "/to_ExcitorBehaviour/setExcitorMasterIntensity");
		oscar.plug(this, "setAttractorForce", "/to_ExcitorBehaviour/setAttractorForce");
		oscar.plug(this, "setAttractorLocations", "/to_ExcitorBehaviour/setAttractorLocations");
		oscar.plug(this, "IR1thres", "/to_ExcitorBehaviour/sensor/1/setThreshold");
		oscar.plug(this, "IR2thres", "/to_ExcitorBehaviour/sensor/2/setThreshold");
		oscar.plug(this, "IR3thres", "/to_ExcitorBehaviour/sensor/3/setThreshold");

		// From Sensors
		oscar.plug(this, "IR1val", "/to_ExcitorBehaviour/sensor/1/value");
		oscar.plug(this, "IR2val", "/to_ExcitorBehaviour/sensor/2/value");
		oscar.plug(this, "IR3val", "/to_ExcitorBehaviour/sensor/3/value");

	}

	public void generateExcitor(int v) {
		// For some reason generating Excitors thru OSC at a fast pace (<1000ms) crashes
		// the app. Similar code with key press input (ref keyPressed()) does not crash
		// the app.
		sensorSystem.sensors.get(v - 1).trigger();
	}

	public void setExcitorCurvature(float v) {
		for (int i = 0; i < excitorSystem.excitors.size(); i++) {
			excitorSystem.excitors.get(i).curvature = v;
		}
	}

	public void setExcitorThresholds(int[] thresholds) {
		for (int i = 0; i < excitorSystem.excitors.size(); i++) {
			excitorSystem.excitors.get(i).threshold = thresholds[i];
		}
	}

	public void setExcitorMasterIntensity(float v) {
		masterIntensity = v;
	}

	public void setAttractorForce(float v) {
		attractorForce = v;
	}

	public void setAttractorLocations(float[] locations) {
		for (int i = 0; i < attractorSystem.attractors.size(); i++) {
			attractorSystem.attractors.get(i).location.x = locations[i * 2];
			attractorSystem.attractors.get(i).location.y = locations[i * 2 + 1];
		}
	}

	public void IR1val(int v) {
		IRvalues[0] = v;
		if (v > IRthresholds[0]) {
			if (gate)
				sensorSystem.sensors.get(0).trigger();
			gate = false;
		} else {
			gate = true;
		}
	}

	public void IR2val(int v) {
		IRvalues[1] = v;
		if (v > IRthresholds[1]) {
			if (gate)
				sensorSystem.sensors.get(1).trigger();
			gate = false;
		} else {
			gate = true;
		}
	}

	public void IR3val(int v) {
		IRvalues[2] = v;
		if (v > IRthresholds[2]) {
			if (gate)
				sensorSystem.sensors.get(2).trigger();
			gate = false;
		} else {
			gate = true;
		}
	}

	public void IR1thres(int v) {
		IRthresholds[0] = v;
	}

	public void IR2thres(int v) {
		IRthresholds[1] = v;
	}

	public void IR3thres(int v) {
		IRthresholds[2] = v;
	}

	public void oscOut() {
		OscMessage unitActuatorIntensities = new OscMessage("/4D/TRIAD1/UNIT1/INTENSITIES");

		for (int i = 0; i < actuatorSystem.actuators.size(); i++) {
			int triadIndex = PApplet.parseInt(sphereUnitInfo[i / sphereUnitVertexCount][0]);
			int unitIndex = PApplet.parseInt(sphereUnitInfo[i / sphereUnitVertexCount][1]);

			if (i % sphereUnitVertexCount == 0) {
				unitActuatorIntensities = new OscMessage(
						"/4D/TRIAD" + triadIndex + "/UNIT" + unitIndex + "/INTENSITIES");
			}

			unitActuatorIntensities.add(PApplet.parseInt(actuatorSystem.actuators.get(i).intensity * 150));

			if (i % sphereUnitVertexCount == sphereUnitVertexCount - 1) {
				oscar.send(unitActuatorIntensities, MASTER_LAPTOP);
			}
		}
	}

	class Sensor {

		PVector location;
		float intensity;
		int fillAlpha, t;
		int index, size;

		Sensor(int _index, PVector _location) {
			index = _index;
			location = _location.copy();
			intensity = 1;
			fillAlpha = 0;
			size = 30;
		}

		public void run() {

			if ((millis() - t) < 100) {
				fillAlpha = 255;
			} else {
				fillAlpha = 0;
			}

			fill(255, 0, 0, fillAlpha);
			stroke(intensity * 200 + 50, 0, 0);
			PVector locationPix = convertVectorToPixelSpace(location);
			ellipse(locationPix.x, locationPix.y, size, size);
			textAlign(CENTER, CENTER);
			fill(255, 0, 0);
			textSize(14);
			text(index + 1, locationPix.x, locationPix.y - 4);
			textSize(12);
			text("val " + IRvalues[index], locationPix.x, locationPix.y + 20);
		}

		public void trigger() {
			excitorSystem.addExcitor(location);
			t = millis();
		}

	}

	class SensorSystem {

		ArrayList<Sensor> sensors;

		SensorSystem() {
			sensors = new ArrayList<Sensor>();

			for (int i = 0; i < sensorInfo.length; i++) {
				PVector location = new PVector(sensorInfo[i][0], sensorInfo[i][2]);
				sensors.add(new Sensor(sensors.size(), location));
			}
		}

		public void run() {
			Iterator<Sensor> it = sensors.iterator();
			while (it.hasNext()) {
				Sensor s = it.next();
				s.run();

			}
		}
	}

	class SphereUnit {

		PVector location, locationPix;
		String id;
		PVector[] vertexLocations, vertexLocationsPix;
		int vertexCount;
		float radius;

		SphereUnit(String _id, PVector _location) {
			id = _id;
			location = _location;
			radius = 0.3f;
			vertexCount = 6;
			vertexLocations = new PVector[vertexCount];

			for (int i = 0; i < vertexCount; i++) {
				vertexLocations[i] = new PVector();
				vertexLocations[i] = polygonVertexPoint(vertexCount, radius, radius, i);
				vertexLocations[i].add(location);
				actuatorSystem.addActuator(actuatorSystem.actuators.size(), vertexLocations[i]);
			}

			locationPix = convertVectorToPixelSpace(location);
			vertexLocationsPix = convertVectorToPixelSpaceArray(vertexLocations);
		}

		public void run() {
			display();
		}

		public void display() {
			noFill();
			stroke(100);
			beginShape();
			for (int i = 0; i < sphereUnitVertexCount; i++) {
				vertex(vertexLocationsPix[i].x, vertexLocationsPix[i].y);
			}
			endShape(CLOSE);
			fill(255);
			textAlign(CENTER, CENTER);
			text(id, locationPix.x, locationPix.y);
		}
	}

	class SphereUnitSystem {

		ArrayList<SphereUnit> sphereUnits;

		SphereUnitSystem() {
			sphereUnits = new ArrayList<SphereUnit>();

			for (int i = 0; i < sphereUnitInfo.length; i++) {
				String id = str((int) (sphereUnitInfo[i][0])) + "-" + str((int) (sphereUnitInfo[i][1]));
				PVector location = new PVector(sphereUnitInfo[i][2], sphereUnitInfo[i][4]);
				sphereUnits.add(new SphereUnit(id, location));
			}
		}

		public void run() {
			Iterator<SphereUnit> it = sphereUnits.iterator();
			while (it.hasNext()) {
				SphereUnit s = it.next();
				s.run();
			}

		}

	}

	public void display() {
		// display framerate when beneath a threshold
		popMatrix();
		textAlign(LEFT);
		textSize(14);
		if (frameRate < 55) {
			fill(255, 0, 0);
			text(PApplet.parseInt(frameRate), 30, 20);
		}

		fill(255);
		text("sphere unit count: " + sphereUnitSystem.sphereUnits.size(), 30, 50);
		text("actuator count: " + actuatorSystem.actuators.size(), 30, 80);
		text("excitor count: " + excitorSystem.excitors.size(), 30, 110);
		text("attractor count: " + attractorSystem.attractors.size(), 30, 140);
		text("sensor count: " + sensorSystem.sensors.size(), 30, 170);

		text("excitor pos/radius to 4D pos/dim (key 'p'): " + sendExcPosDimTo4Dengine, 30, 240);
		text("excitor lifespan to 4D gain/scale (key 'l'): " + sendExcLifespanTo4Dengine, 30, 270);

		text("generate Excitors at Sensor locations (keys '1' '2' '3')", 30, 330);
		text("show/hide Excitors (key 'e')", 30, 360);
		text("generate random Attractor locations (key 'r')", 30, 390);
		text("show/hide Attractor field (key 's')", 30, 420);

		text("OSC", 30, 480);
		text("listen port: 3000", 30, 510);
		text("to 4DSOUND: 127.0.0.1 2000", 30, 540);
		text("to Master Laptop: 10.14.4.181 3001", 30, 570);
		text("to Max Patch: 127.0.0.1 4000", 30, 600);
		text("to Encefalo: 127.0.0.1 6000", 30, 630);

		fill(100);
		text("LASG Excitor Behaviour Demo " + appVersion, 30, height - 60);
		text("by Poul Holleman | 4DSOUND", 30, height - 30);

	}

	public PVector polygonVertexPoint(int _vertexCount, float _width, float _height, int _index) {
		PVector vertex = new PVector();
		;
		vertex.x = sin(TWO_PI / _vertexCount * -_index + PI) * _width; // multiplied by minus-index for CW arrangement
		vertex.y = cos(TWO_PI / _vertexCount * -_index + PI) * _height;
		return vertex;
	}

	public PVector convertVectorToPixelSpace(PVector input) {
		PVector coordinates = input.copy();
		return coordinates.mult(100);
	}

	public PVector[] convertVectorToPixelSpaceArray(PVector[] input) {
		PVector[] coordinates = new PVector[input.length];
		for (int i = 0; i < input.length; i++) {
			coordinates[i] = input[i].copy().mult(100);
		}
		return coordinates;
	}

	boolean rGate = false;

	public void keyReleased() {
		rGate = false;
	}

	public void keyPressed() {
		if (!rGate && key == 'r') {
			rGate = true;
			for (int i = 0; i < attractorSystem.attractors.size(); i++) {
				attractorSystem.attractors.get(i).location.x = random(10) - 5;
				attractorSystem.attractors.get(i).location.y = random(6) - 3;
			}
		}

		if (key == 'e') {
			if (showExcitors) {
				showExcitors = false;
			} else {
				showExcitors = true;
			}
		}

		if (key == 'a') {
			if (showAttractors) {
				showAttractors = false;
			} else {
				showAttractors = true;
			}
		}

		if (key == 'A') {
			if (showActuators) {
				showActuators = false;
			} else {
				showActuators = true;
			}
		}

		if (key == 's') {
			if (showAttractorShape) {
				showAttractorShape = false;
			} else {
				showAttractorShape = true;
			}
		}

		if (key == 'p') {
			if (sendExcPosDimTo4Dengine) {
				sendExcPosDimTo4Dengine = false;
			} else {
				sendExcPosDimTo4Dengine = true;
			}
		}

		if (key == 'l') {
			if (sendExcLifespanTo4Dengine) {
				sendExcLifespanTo4Dengine = false;
			} else {
				sendExcLifespanTo4Dengine = true;
			}
		}

		switch (key) {
		case '1':
			sensorSystem.sensors.get(0).trigger();
			break;
		case '2':
			sensorSystem.sensors.get(1).trigger();
			break;
		case '3':
			sensorSystem.sensors.get(2).trigger();
			break;
		}
	}

	public void settings() {
		size(1600, 800, P3D);
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "ExcitorBehavior.excitorBehaviorEngine" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}
