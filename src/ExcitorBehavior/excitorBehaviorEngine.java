package ExcitorBehavior;

import processing.core.*;

import oscP5.*;
import netP5.*;
import peasy.*;
import java.util.Iterator;

import java.util.ArrayList;

public class excitorBehaviorEngine extends PApplet {

	PeasyCam cam;

	OscP5 oscar;
	NetAddress FOUR_D_ENGINE;
	NetAddress MASTER_LAPTOP;
	NetAddress MAX_PATCH;

	int sphereUnitVertexCount = 6;
	int actuatorCount = 0;
	int excitorCount = 0;
	int attractorCount = 3;
	int sensorCount = 7;

	float attractorForce = 0.001f;
	boolean showExcitors = true;
	boolean showAttractors = true;
	boolean showActuators = true;
	boolean showAttractorShape = true;

	float masterIntensity = 1;

	PVector origin = new PVector(0, 0);

	boolean gate = true;

//Sphere Unit Info: { Triad, Unit, posX, posY, posZ }
	float sphereUnitInfo[][] = { { 1, 1, -3, 2, -0.5f }, { 1, 2, -1.5f, 2, 0.5f }, { 1, 3, -3, 2, 2 },
			{ 2, 1, -0.5f, 2, -1.5f }, { 2, 2, 0.5f, 2, 1 }, { 2, 3, -0.5f, 2, 3 }, { 3, 1, 2, 2, -1.5f },
			{ 3, 3, 2, 2, 3 }, { 4, 1, 4, 2, -1 }, { 4, 3, 4, 2, 2.5f }, { 5, 1, 5, 2, -0.5f },
			{ 5, 2, 5.25f, 2, 0.5f }, { 5, 3, 5, 2, 1.5f } };

	SphereUnit[] sphereUnit = new SphereUnit[sphereUnitInfo.length];
	Excitor[] excitor = new Excitor[excitorCount];
	Attractor[] attractor = new Attractor[attractorCount];
	ExcitorSystem excitorSystem;
	Sensor[] sensor = new Sensor[sensorCount];
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
		MASTER_LAPTOP = new NetAddress("10.14.4.163", 3001);
		MAX_PATCH = new NetAddress("127.0.0.1", 4000);
		// FOUR_D_ENGINE = new NetAddress("10.14.4.134", 2000);
		// MASTER_LAPTOP = new NetAddress("10.14.4.163", 3001);

		actuatorSystem = new ActuatorSystem();
		excitorSystem = new ExcitorSystem();
		sensorSystem = new SensorSystem(new PVector(1, 1), sensorCount, 4, 2);
		sphereUnitSystem = new SphereUnitSystem();
		attractorSystem = new AttractorSystem();

		// for (int i=0; i<21; i++) {
		// actuatorSystem.addActuator(actuatorSystem.actuators.size(), new
		// PVector(random(16)-8, random(8)-4));
		// }

		initOscIn();
	}

	public void draw() {
		background(25);

		pushMatrix();
		translate(width / 2, height / 2);

		sensorSystem.display();
		attractorSystem.display();

		for (int i = 0; i < attractor.length; i++) {
			attractor[i].run();
		}

		excitorSystem.run();
		actuatorSystem.run();

		for (int i = 0; i < sphereUnit.length; i++) {
			sphereUnit[i].run();
		}

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
			locationPix = convertVectorToPixelSpace(location);
			intensity *= masterIntensity;
			intensity -= intensityRelease;
			if (intensity < 0)
				intensity = 0;
		}

		public void display() {
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
			actuatorCount += 1;
		}

		public void run() {
			Iterator<Actuator> it = actuators.iterator();
			while (it.hasNext()) {
				Actuator a = it.next();
				a.run();
			}
		}
	}

	class Attractor {

		PVector location, locationPix, mouse;
		int size;
		Boolean dragged, show;

		Attractor(PVector _location) {
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

		AttractorSystem() {
			for (int i = 0; i < attractorCount; i++) {
				attractor[i] = new Attractor(new PVector(random(8) - 4, random(4) - 2));
			}
		}

		public void display() {
			if (showAttractorShape) {
				for (int i = 0; i < attractorCount; i++) {
					attractor[i].run();
				}
				noStroke();
				fill(255, 50);
				beginShape();
				for (int i = 0; i < attractor.length; i++) {
					vertex(attractor[i].locationPix.x, attractor[i].locationPix.y);
				}
				endShape(CLOSE);
			}
		}
	}

	class Excitor {

		PVector location, locationPix, velocity, acceleration, force;
		float threshold, curvature, speedLimit, mass, lifespan;

		Excitor(PVector _location) {
			location = _location.copy();
			velocity = new PVector();
			acceleration = new PVector();
			mass = random(1);
			threshold = 1;
			curvature = 1;
			speedLimit = 3;
			lifespan = 255;
		}

		public void run() {
			update();
			applyAtrractorForce();
			excite();
			checkEdges();
			if (showExcitors)
				display();
		}

		public void update() {
			velocity.add(acceleration);
			velocity.limit(speedLimit);
			location.add(velocity);
			locationPix = convertVectorToPixelSpace(location);
			acceleration.mult(0);
			lifespan -= 0.5f;
		}

		public void applyAtrractorForce() {
			for (int i = 0; i < attractor.length; i++) {
				PVector force = attractor[i].location.copy();
				force.sub(location);
				force.normalize();
				force.mult(attractorForce);
				applyForce(force);
			}
		}

		public void display() {
			stroke(lifespan, 50);
			noFill();
			ellipse(locationPix.x, locationPix.y, threshold * 200, threshold * 200);

			textAlign(CENTER, CENTER);
			textSize(14);
			fill(255);
		}

		public void excite() {
			for (int i = 0; i < actuatorSystem.actuators.size(); i++) {
				float distance = location.dist(actuatorSystem.actuators.get(i).location);
				if (distance <= threshold)
					actuatorSystem.actuators.get(i).intensity += pow(1 - (distance / threshold), curvature) * 0.1f;// (lifespan/255);
			}
		}

		public void applyForce(PVector _force) {
			PVector force = _force.copy();
			force.mult(mass + 0.5f);
			acceleration.add(force);
		}

		public void checkEdges() {
			if (locationPix.x < -(width / 2))
				locationPix.x = width / 2;
			if (locationPix.x > width / 2)
				locationPix.x = -(width / 2);
			if (locationPix.y < -(height / 2))
				locationPix.y = height / 2;
			if (locationPix.y > height / 2)
				locationPix.y = -(height / 2);
		}

		public boolean isDead() {
			if (lifespan < 0.0f) {
				return true;
			} else {
				return false;
			}
		}
	}

	class ExcitorSystem {

		ArrayList<Excitor> excitors;
		PVector origin;

		ExcitorSystem() {
			excitors = new ArrayList<Excitor>();
		}

		public void addExcitor(PVector _location) {
			excitors.add(new Excitor(_location));
			excitorCount += 1;
		}

		public void run() {
			Iterator<Excitor> it = excitors.iterator();
			while (it.hasNext()) {
				Excitor e = it.next();
				e.run();
				if (e.isDead()) {
					it.remove();
					excitorCount -= 1;
				}
			}
		}
	}

	public void initOscIn() {

		// From Max Patch
		oscar.plug(this, "setExcitorCurvature", "/setExcitorCurvature");
		oscar.plug(this, "setExcitorThresholds", "/setExcitorThresholds");
		oscar.plug(this, "setExcitorMasterIntensity", "/setExcitorMasterIntensity");
		oscar.plug(this, "setAttractorForce", "/setAttractorForce");
		oscar.plug(this, "setAttractorLocations", "/setAttractorLocations");

		// From Sensors
		oscar.plug(this, "IRvalueCentre", "/4D/SENSOR/CENTRE");
		oscar.plug(this, "IRvalueCorner", "/4D/SENSOR/CORNER");
	}

	public void setExcitorCurvature(float v) {
		for (int i = 0; i < excitor.length; i++) {
			excitor[i].curvature = v;
		}
	}

	public void setExcitorThresholds(int[] thresholds) {
		for (int i = 0; i < excitor.length; i++) {
			excitor[i].threshold = thresholds[i];
		}
	}

	public void setExcitorMasterIntensity(float v) {
		masterIntensity = v;
	}

	public void setAttractorForce(float v) {
		attractorForce = v;
	}

	public void setAttractorLocations(int[] locations) {
		for (int i = 0; i < attractor.length; i++) {
			attractor[i].location.x = locations[i * 2] * 100;
			attractor[i].location.y = (locations[i * 2] + 1) * 100;
		}
	}

	public void IRvalueCorner(int v) {
		println("IRvalueCorner: " + v);
		if (v > 400) {
			if (gate)
				sensor[2].trigger();
			gate = false;
		} else {
			gate = true;
		}
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

			unitActuatorIntensities.add(PApplet.parseInt(actuatorSystem.actuators.get(i).intensity * 255));

			if (i % sphereUnitVertexCount == sphereUnitVertexCount - 1) {
				oscar.send(unitActuatorIntensities, MASTER_LAPTOP);
			}
		}
	}

	class Sensor {

		PVector location;
		float intensity;
		int index, fillAlpha, t;

		Sensor(int _index, PVector _location) {
			index = _index;
			location = _location.copy();
			intensity = 1;
			fillAlpha = 0;
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
			ellipse(locationPix.x, locationPix.y, 30, 30);
			textAlign(CENTER, CENTER);
			fill(255);
			textSize(14);
			text(index + 1, locationPix.x, locationPix.y);
		}

		public void trigger() {
			excitorSystem.addExcitor(sensorSystem.sensorLocations[index]);
			t = millis();
		}
	}

	class SensorSystem {

		PVector[] sensorLocations;

		SensorSystem(PVector _location, int _sensorCount, int _dimX, int _dimY) {
			sensorLocations = polygon(_sensorCount, _dimX, _dimY);
			for (int i = 0; i < sensorCount; i++) {
				sensorLocations[i] = sensorLocations[i].add(_location);
				sensor[i] = new Sensor(i, sensorLocations[i]);
			}
		}

		public void display() {
			for (int i = 0; i < sensorCount; i++) {
				sensor[i].run();
			}
		}
	}

	class SphereUnit {

		PVector location, locationPix;
		int index;
		String id;
		PVector[] vertexLocations, vertexLocationsPix;
		float radius;

		SphereUnit(int _index, String _id, PVector _location) {
			index = _index;
			id = _id;
			location = _location;
			radius = 0.3f;
			vertexLocations = new PVector[sphereUnitVertexCount];

			for (int i = 0; i < sphereUnitVertexCount; i++) {
				vertexLocations[i] = new PVector();
				vertexLocations[i] = polygonVertexPoint(sphereUnitVertexCount, radius, radius, i);
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

		PVector[] sphereUnitLocations;

		SphereUnitSystem() {
			sphereUnitLocations = new PVector[sphereUnitInfo.length];
			for (int i = 0; i < sphereUnitInfo.length; i++) {
				String id = str(PApplet.parseInt(sphereUnitInfo[i][0])) + "-"
						+ str(PApplet.parseInt(sphereUnitInfo[i][1]));
				PVector location = new PVector(sphereUnitInfo[i][2], sphereUnitInfo[i][4]);
				sphereUnit[i] = new SphereUnit(i, id, location);
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
		text("actuator count: " + actuatorCount, 30, 50);
		text("excitor count: " + excitorCount, 30, 80);
	}

	public PVector[] polygon(int _vertexCount, float _width, float _height) {
		PVector[] vertices = new PVector[_vertexCount];
		for (int i = 0; i < _vertexCount; i++) {
			vertices[i] = new PVector();
			vertices[i].x = sin(TWO_PI / _vertexCount * i + PI) * _width;
			vertices[i].y = cos(TWO_PI / _vertexCount * i + PI) * _height;
		}
		return vertices;
	}

	public PVector polygonVertexPoint(int _vertexCount, float _width, float _height, int _index) {
		PVector vertex = new PVector();
		;
		vertex.x = sin(TWO_PI / _vertexCount * _index + PI) * _width;
		vertex.y = cos(TWO_PI / _vertexCount * _index + PI) * _height;
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
			for (int i = 0; i < attractor.length; i++) {
				attractor[i].location.x = random(width) - width / 2;
				attractor[i].location.y = random(height) - height / 2;
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

		switch (key) {
		case '1':
			sensor[0].trigger();
			break;
		case '2':
			sensor[1].trigger();
			break;
		case '3':
			sensor[2].trigger();
			break;
		case '4':
			sensor[3].trigger();
			break;
		case '5':
			sensor[4].trigger();
			break;
		case '6':
			sensor[5].trigger();
			break;
		case '7':
			sensor[6].trigger();
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
