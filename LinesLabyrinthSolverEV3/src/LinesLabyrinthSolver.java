import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.acl.LastOwnerException;
import java.util.Vector;

import javax.naming.directory.DirContext;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.*;
import lejos.hardware.sensor.EV3ColorSensor;

public class LinesLabyrinthSolver {
	
	static int crossArr[] = { 0, 0, 0 };
	static float stripLength = 0.017f; // Klebebandbreite
	static float lightThreshold = 0.18f; //Grenzwert des Lichtsensors
	static GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
	static LineFollowerThreadObject LineFollowerThread;
	
	static float mindstormSpecificOffset = 0.125f; // [m] // Korrekturwert für jeden Mindstorm individuell zu testen
	static float diameter=0.055f; //Radurchmesser des jeweiligen Mindstorms
	static float wheelbase = 0.1f; // Radabstand
	static float wheelradius = 0.05f; //??? 
	
	
	
	public static void main(String[] args) throws InterruptedException {

		EV3ColorSensor lsNavi = new EV3ColorSensor(SensorPort.S3);
		lsNavi.setFloodlight(true);
		SampleProvider spNavi = lsNavi.getRedMode();

		// Initializing
		/**
		 * Initialisierung wie in Greenfoot. Zusätzlich 
		 * Werden noch die Lichtsensoren und der LinefollowerThread initialisiert, der für die Verfolgung der Linie verantwortlich ist.
		 * 
		 * **/
		boolean onWayBack = false;
		Knot currentKnot;
		Edge currentEdge;

		currentEdge = new Edge(0, null);
		currentKnot = new Knot(currentEdge);
		currentEdge.setChild(currentKnot);

		float[] lvNaviArr = new float[spNavi.sampleSize()];
		float lvNavi = 0;
		

		LineFollowerThread = new LineFollowerThreadObject();
		LineFollowerThread.start();
		LineFollowerThread.stopFollowing();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		// Main loop
		while (!Button.ESCAPE.isDown()) {
			// Button.RIGHT.waitForPressAndRelease();
			// Thread.sleep(500);
			//Sound.beep();
			LineFollowerThread.speedForward=0;
			LineFollowerThread.startFollowing();
			for (int i = 0; i <100; i++) {
				LineFollowerThread.speedForward++;
				LineFollowerThread.reset();
				Thread.sleep(10);
			}
			
			LineFollowerThread.speedForward=250;
			// drive to next crossing
			Thread.sleep(500);

			int l = 0;
			do {

				spNavi.fetchSample(lvNaviArr, 0);
				lvNavi = lvNaviArr[0];
				if (l % 30 == 0)
					drawState("Wait for reading", 0, lvNavi, currentEdge);
				l++;
			} while (lvNavi > lightThreshold);

			// reached next crossing
			LineFollowerThread.rightMotor.resetTachoCount();
			float EncPos = 0;

			if (!onWayBack) {
				// read bits
				do {
					EncPos = (float) (LineFollowerThread.rightMotor
							.getTachoCount() / 360f * Math.PI * 0.055);
					drawState("Wait for first bit to read", EncPos, lvNavi,
							currentEdge);
				} while (EncPos < stripLength);
				// Reading bits
				
				/**
				 * Auslesen der Klebebandmarkierungen:
				 * 
				 * 1. Reinitialisierung
				 * 2. Auslesen
				 * 3. Speichern
				 * 
				 */
				
				
				for (int i = 0; i < crossArr.length; i++) {
					crossArr[i] = 0; // Reinitialisierung
				}
				for (int j = 0; j < 3; j++) {
					/**
					 * Hier findet das eigentliche auslesen des Wertes statt, dafür wird die zurückgelegte Streke gespeichert, um zu wissen welcher Streifen gerade gelesen wird.
					 * um den Lichtsensorwerten den passenden Klebebandstreifen zuzuordnen.
					 * **/
					
					int k = 0;
					float lvNaviSum = 0;
					while (EncPos / stripLength < j + 2) {
						EncPos = (float) (LineFollowerThread.rightMotor
								.getTachoCount() / 360f * Math.PI * diameter);
						spNavi.fetchSample(lvNaviArr, 0);
						lvNavi = lvNaviArr[0];
						lvNaviSum += lvNavi;
						k++;
						if (k % 30 == 0)
							drawState("Read bit: " + (j + 1), EncPos, lvNavi,
									currentEdge);
					}
					
					// Speichern der Sensorwerte  in unsere Kreuzung
					if (lvNaviSum / k > lightThreshold) {
						int direction = 0;
						switch (j) {
						case 0:
							direction = local2global(3, currentEdge);
							break;
						case 1:
							direction = local2global(0, currentEdge);
							break;
						case 2:
							direction = local2global(1, currentEdge);
							break;
						}
						crossArr[j] = 1;
						currentKnot.addOption(direction);

					}
					// lcd.clear();
					// lcd.drawString("crossing: " + crossArr[0] + " "
					// + crossArr[1] + " " + crossArr[2], 0, 60, 0);

				}
			}
			// drive to middle of crossing
			
			do {
				
				/**
				 * Nachdem, man die Kreuzung ausgelesen hat muss noch weiter gefahren werden bis man auf der Kreuzung steht, dies geschieht hier.
				 * 
				 * 
				 * **/
				EncPos = (float) (LineFollowerThread.rightMotor.getTachoCount()
						/ 360f * Math.PI * diameter);
				if (l % 30 == 0)
					drawState("drive to begin of crossing", EncPos, lvNavi,
							currentEdge);

				l++;
			} while (EncPos < 5 * stripLength + mindstormSpecificOffset);

			LineFollowerThread.stopFollowing();
			
			
			/**
			 * Hier ist wieder der eigentliche Algorithmus aus Greenfoot zu finden
			 * **/
			// Button.ENTER.waitForPressAndRelease();
			Edge nextEdge = currentKnot.getNextOption();
			if (nextEdge != null) {
				// System.out.println("found option");
				onWayBack = false;
				rotate(currentEdge, nextEdge);
				currentEdge = nextEdge;
				currentKnot = nextEdge.getChild();
			} else {
				onWayBack = true;
				// System.out.println("onWayBack");
				nextEdge = currentKnot.getWayBack();
				rotate(currentEdge, nextEdge);
				currentKnot = nextEdge.getParent();
				currentEdge = nextEdge;
			}

		}

		
		lsNavi.close();
		LineFollowerThread.stopFollowing();
		LineFollowerThread.quitThread();
		try {
			LineFollowerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void rotate(Edge currentEdge, Edge targetEdge)
			throws InterruptedException {
		/**
		 * Rotatefunktion, entspricht der Rotatefunktion aus Greenfoot   
		 * 
		 * **/

		int diff = (targetEdge.direction - currentEdge.direction + 4) % 4;
		switch (diff) {
		case 1:
			turn(90);
			break;
		case 2:
			turn(-180);
			break;
		case 3:
			turn(-90);
			break;
		default:
			break;
		}
		return;
	}

	
	
	static void drawState(String Name, float EncPos, float lvNavi,
			Edge currentEdge) {
		
		/**
			Zeigt den aktuellen internen Zustand des Mindstorms an :
			-state: Was mache ich gerade
			EncPos: Zurückgelegte Strecke
			lvNavi: Wert des Lichtsensors
			cur.dir: aktuelle richtung 
		 */
		lcd.clear();
		lcd.drawString("State: " + Name, 0, 15, 0);
		lcd.drawString("EncPos: " + EncPos, 0, 30, 0);
		lcd.drawString("lvNavi: " + lvNavi, 0, 45, 0);
		lcd.drawString("cur.dir:" + currentEdge.direction, 0, 60, 0);

		lcd.drawString("cross: " + crossArr[0] + crossArr[1] + crossArr[2], 0,
				75, 0);
	}

	public static int local2global(int direction, Edge currentEdge) {
		// Einfache Umrechnungsmethode
		
		return (currentEdge.direction + direction) % 4;
	}
	
	public static void turn(int deg) throws InterruptedException {
		
		/** Hier wird sich um die die mitgegebene Gradzahl gedreht
		 *Dafür werden zuerst die Motoren initialisiert und dann um die Mindstormspezifischen Werte (Abhängig Radabstand und Radgröße) gedreht.
		 *
		 */
		
		Thread.sleep(1000);
		RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		lcd.drawString("turn: " + deg, 0, 90, 0);
		deg+=10;
		// Button.ENTER.waitForPressAndRelease();
		// Sound.beepSequenceUp();
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		
		int angle = (int) (2 * Math.PI * wheelbase * deg / (2 * Math.PI * wheelradius));

		leftMotor.rotate(angle, true);
		rightMotor.rotate(-angle, false);

		while (leftMotor.isMoving()
				|| rightMotor.isMoving()) {
			Thread.sleep(10);
		}
		leftMotor.stop();
		rightMotor.stop();
	
		leftMotor.close();
		rightMotor.close();

	}
}
