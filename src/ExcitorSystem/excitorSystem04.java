package ExcitorSystem;

import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import oscP5.*;
import netP5.*;
import peasy.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class excitorSystem04 extends PApplet {

	PeasyCam cam;

	OscP5 oscar;
	NetAddress FOUR_D_ENGINE;
	NetAddress MASTER_LAPTOP;
	NetAddress MAX_PATCH;

	int sphereUnitCount = 13;
	int vertexCount = 6;
	int actuatorCount = PApplet.parseInt(sphereUnitCount * vertexCount);

	int excitorCount = 13;
	int attractorCount = 3;

	float attractorForce = 0.005f;
	boolean showExcitors = false;
	boolean showAttractors = true;
	boolean showActuators = true;
	boolean showAttractorShape = true;

	float masterIntensity = 1;

//Grid actuatorGrid;
	Polygon[] polygon = new Polygon[sphereUnitCount];
	Actuator[] actuator = new Actuator[actuatorCount];
	Excitor[] excitor = new Excitor[excitorCount];
	Attractor[] attractor = new Attractor[attractorCount];

//ASphere Unit Info: { Triad, Unit, posX, posY, posZ }
	float sphereUnitInfo[][] = { { 1, 1, -3, 2, -0.5f }, { 1, 2, -1.5f, 2, 0.5f }, { 1, 3, -3, 2, 2 },
			{ 2, 1, -0.5f, 2, -1.5f }, { 2, 2, 0.5f, 2, 1 }, { 2, 3, -0.5f, 2, 3 }, { 3, 1, 2, 2, -1.5f },
			{ 3, 3, 2, 2, 3 }, { 4, 1, 4, 2, -1 }, { 4, 3, 4, 2, 2.5f }, { 5, 1, 5, 2, -0.5f }, { 5, 2, 5, 2, 0.5f },
			{ 5, 3, 5, 2, 1.5f } };

	public void setup() {
		// fullScreen(P3D);

		frameRate(60);

		// cam = new PeasyCam(this, width/2, height/2, 0, 800);

		oscar = new OscP5(this, 3000); // listen port
		FOUR_D_ENGINE = new NetAddress("127.0.0.1", 2000);
		MASTER_LAPTOP = new NetAddress("10.14.4.163", 3002);
		MAX_PATCH = new NetAddress("127.0.0.1", 4000);
		// FOUR_D_ENGINE = new NetAddress("10.14.4.134", 2000);
		// MASTER_LAPTOP = new NetAddress("10.14.4.163", 3001);

		// init Sphere Units as Polygons with Center Locations
		for (int i = 0; i < polygon.length; i++) {
			polygon[i] = new Polygon();
			polygon[i].index = str(PApplet.parseInt(sphereUnitInfo[i][0])) + "-"
					+ str(PApplet.parseInt(sphereUnitInfo[i][1]));
			polygon[i].location.x = sphereUnitInfo[i][2] * 100;
			polygon[i].location.y = sphereUnitInfo[i][4] * 100;
		}

		// init Actuators and base there Locations on Polygon Vertices
		for (int i = 0; i < actuator.length; i++) {
			actuator[i] = new Actuator();
			actuator[i].index = (i % 6) + 1;
			int polygonIndex = i / vertexCount;
			int actuatorIndex = i % vertexCount;
			PVector ref = polygon[polygonIndex].actuatorLocations[actuatorIndex].copy();
			actuator[i].location = ref.add(polygon[polygonIndex].location);
		}

		// init Excitors
		for (int i = 0; i < excitor.length; i++) {
			excitor[i] = new Excitor();
			excitor[i].index = i;
		}

		// init Attractors
		for (int i = 0; i < attractor.length; i++) {
			attractor[i] = new Attractor();
		}

		// init OSC
		initOscIn();
	}

	public void draw() {
		background(25);

		pushMatrix();
		translate(width / 2, height / 2);

		// Iterate through Attractors and Excitors and apply interactive Force
		for (int i = 0; i < attractor.length; i++) {
			attractor[i].run();
			for (int j = 0; j < excitor.length; j++) {
				PVector force = attractor[i].location.copy();
				force.sub(excitor[j].location);
				force.normalize();
				force.mult(attractorForce);
				excitor[j].applyForce(force);
			}
		}

		// Send OSC to 4DENGINE (in 4D format) and Master Laptop (as one list)
		OscMessage excitorPositions = new OscMessage("/excitorPositions");
		for (int i = 0; i < excitor.length; i++) {
			excitor[i].run();
			excitorPositions.add(excitor[i].index);
			excitorPositions.add(excitor[i].location.x);
			excitorPositions.add(excitor[i].location.y);

			OscMessage toSourcePos = new OscMessage("/source" + (i + 1) + "/position");
			toSourcePos.add(excitor[i].location.x * 0.01f);
			toSourcePos.add(2);
			toSourcePos.add(excitor[i].location.y * 0.01f);
			oscar.send(toSourcePos, FOUR_D_ENGINE);

			OscMessage toSourceDim = new OscMessage("/source" + (i + 1) + "/dimensions");
			toSourceDim.add(excitor[i].threshold / 30.0f);
			toSourceDim.add(excitor[i].threshold / 30.0f);
			toSourceDim.add(excitor[i].threshold / 30.0f);
			oscar.send(toSourceDim, MAX_PATCH);
		}
		oscar.send(excitorPositions, MAX_PATCH);

		// Display Actuators and send out OSC
		OscMessage actuatorIntensities = new OscMessage("/actuatorIntensities");
		OscMessage unitActuatorIntensities = new OscMessage("/4D/TRIAD1/UNIT1/INTENSITIES");
		for (int i = 0; i < actuator.length; i++) {
			int triadIndex = PApplet.parseInt(sphereUnitInfo[i / vertexCount][0]);
			int unitIndex = PApplet.parseInt(sphereUnitInfo[i / vertexCount][1]);

			if (i % 6 == 0) {
				unitActuatorIntensities = new OscMessage(
						"/4D/TRIAD" + triadIndex + "/UNIT" + unitIndex + "/INTENSITIES");
			}

			actuator[i].display();
			actuatorIntensities.add(actuator[i].intensity);
			unitActuatorIntensities.add(PApplet.parseInt(actuator[i].intensity * 255));
			actuator[i].resetIntensity();

			if (i % 6 == 5) {
				oscar.send(unitActuatorIntensities, MASTER_LAPTOP);
			}
		}
		oscar.send(actuatorIntensities, MAX_PATCH);

		if (showAttractorShape) {
			noStroke();
			fill(255, 50);
			beginShape();
			for (int i = 0; i < attractor.length; i++) {
				vertex(attractor[i].location.x, attractor[i].location.y);
			}
			vertex(attractor[0].location.x, attractor[0].location.y);
			endShape();
		}

		for (int i = 0; i < polygon.length; i++) {
			polygon[i].run();
		}

		// display framerate when beneath a threshold
		popMatrix();
		if (frameRate < 55) {
			textSize(12);
			fill(255, 0, 0);
			text(PApplet.parseInt(frameRate), 30, 30);
		}
	}

	class Actuator {

		PVector location;
		int index;
		float intensity;
		boolean showText = true;

		Actuator() {
		}

		public void display() {

			if (intensity > 1)
				intensity = 1;
			intensity *= masterIntensity;

			// ELLIPSE
			if (showActuators) {
				noStroke();
				fill(intensity * 200 + 55, 150);
				ellipse(location.x, location.y, 25, 25);
			}

			if (showText) {
				fill(255);
				textAlign(CENTER, CENTER);
				textSize(10);
				text(index, location.x, location.y);
			}
		}

		public void resetIntensity() {
			intensity = 0; // resets intensity after excitors accumulated this value
		}
	}

	class Attractor {

		PVector location, mouse;
		int size;
		Boolean dragged, show;

		Attractor() {
			location = new PVector(random(width) - width / 2, random(height) - height / 2);
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
				location = mouse;
			}
			stroke(0, 255, 0);
			noFill();
			if (dragged)
				fill(0, 255, 0);
			ellipse(location.x, location.y, size, size);
		}

		public void dragged() {
			if (!dragged && mousePressed && location.dist(mouse) <= size / 2) {
				dragged = true;
			} else {
				if (!mousePressed)
					dragged = false;
			}
		}
	}

	class Excitor {

		PVector location, velocity, acceleration, force;
		int index, threshold;
		float curvature, speedLimit, mass;

		Excitor() {
			location = new PVector(random(width), random(height));
			velocity = new PVector();
			acceleration = new PVector();
			mass = random(1);
			threshold = 60;
			curvature = 1;
			speedLimit = 3;
		}

		public void run() {
			// applyForce();
			update();
			excite();
			checkEdges();
			if (showExcitors)
				display();
		}

		public void update() {
			velocity.add(acceleration);
			velocity.limit(speedLimit);
			location.add(velocity);
			acceleration.mult(0);
		}

		public void display() {
			stroke(0);
			fill(0, 0, 255);
			// ellipse(location.x, location.y, 20, 20);
			stroke(200, 50);
			noFill();
			ellipse(location.x, location.y, threshold * 2, threshold * 2);

			textAlign(CENTER, CENTER);
			textSize(14);
			fill(255);
			text(index, location.x, location.y - 2);
		}

		public void excite() {
			for (int i = 0; i < actuatorCount; i++) {
				float distance = location.dist(actuator[i].location);
				threshold = abs(threshold);
				if (distance <= threshold)
					actuator[i].intensity += pow(1 - distance / threshold, curvature);
			}
		}

		public void applyForce(PVector _force) {
			PVector force = _force.copy();
			force.mult(mass + 0.5f);
			acceleration.add(force);
		}

		public void checkEdges() {
			if (location.x < -(width / 2))
				location.x = width / 2;
			if (location.x > width / 2)
				location.x = -(width / 2);
			if (location.y < -(height / 2))
				location.y = height / 2;
			if (location.y > height / 2)
				location.y = -(height / 2);
		}
	}

	public void initOscIn() {
		oscar.plug(this, "setExcitorCurvature", "/setExcitorCurvature");
		oscar.plug(this, "setExcitorThresholds", "/setExcitorThresholds");
		oscar.plug(this, "setExcitorMasterIntensity", "/setExcitorMasterIntensity");
		oscar.plug(this, "setAttractorForce", "/setAttractorForce");
		oscar.plug(this, "setAttractorLocations", "/setAttractorLocations");
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

	class Polygon {

		PVector location;
		String index;
		PVector[] actuatorLocations;
		float radius;

		Polygon() {
			location = new PVector(0, 0);
			radius = 30;
			actuatorLocations = new PVector[vertexCount];
			for (int i = 0; i < vertexCount; i++) {
				actuatorLocations[i] = new PVector();
				actuatorLocations[i].x = sin(TWO_PI / vertexCount * i + PI) * radius;
				actuatorLocations[i].y = cos(TWO_PI / vertexCount * i + PI) * radius;
			}
		}

		public void run() {
			display();
		}

		public void display() {
			pushMatrix();
			translate(location.x, location.y);
			noFill();
			stroke(100);
			beginShape();
			for (int i = 0; i < vertexCount; i++) {
				vertex(actuatorLocations[i].x, actuatorLocations[i].y);
			}
			endShape(CLOSE);
			fill(255);
			textAlign(CENTER, CENTER);
			text(index, 0, 0);
			popMatrix();
		}
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
	}

	public void settings() {
		size(1600, 800, P3D);
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "ExcitorSystem.excitorSystem04" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}
