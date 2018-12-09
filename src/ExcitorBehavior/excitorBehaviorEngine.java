package ExcitorBehavior;


import processing.core.*;

import oscP5.*;
import netP5.*;
import peasy.*;

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

	float attractorForce = 0.005f;
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

		actuatorSystem = new ActuatorSystem(this);
		excitorSystem = new ExcitorSystem(this);
		sensorSystem = new SensorSystem(this, new PVector(1, 1), sensorCount, 4, 2);
		sphereUnitSystem = new SphereUnitSystem(this);
		attractorSystem = new AttractorSystem(this);

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

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "ExcitorBehavior.excitorBehaviorEngine" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}
