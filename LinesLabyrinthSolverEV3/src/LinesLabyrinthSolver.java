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
	static int north = 0;
	static int west = 1;
	static int south = 2;
	static int east = 3;
	static int crossArr[] = { 0, 0, 0 };
	static float stripLength = 0.017f;
	static GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
	static LineFollowerThreadObject LineFollowerThread;

	static void drawState(String Name, float EncPos, float lvNavi,
			Edge currentEdge) {
		lcd.clear();
		lcd.drawString("State: " + Name, 0, 15, 0);
		lcd.drawString("EncPos: " + EncPos, 0, 30, 0);
		lcd.drawString("lvNavi: " + lvNavi, 0, 45, 0);
		lcd.drawString("cur.dir:" + currentEdge.direction, 0, 60, 0);

		lcd.drawString("cross: " + crossArr[0] + crossArr[1] + crossArr[2], 0,
				75, 0);
	}

	public static int local2global(int direction, Edge currentEdge) {
		return (currentEdge.direction + direction) % 4;
	}

	public static int global2local(int direction, Edge currentEdge) {
		return 0;
	}

	public static void main(String[] args) throws InterruptedException {

		EV3ColorSensor lsNavi = new EV3ColorSensor(SensorPort.S3);
		lsNavi.setFloodlight(true);
		SampleProvider spNavi = lsNavi.getRedMode();

		// Initializing
		boolean onWayBack = false;
		Knot currentKnot;
		Edge currentEdge;

		currentEdge = new Edge(0, null);
		currentKnot = new Knot(currentEdge);
		currentEdge.setChild(currentKnot);

		float[] lvNaviArr = new float[spNavi.sampleSize()];
		float lvNavi = 0;
		float lightThreshold = 0.18f;

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
				for (int i = 0; i < crossArr.length; i++) {
					crossArr[i] = 0;
				}
				for (int j = 0; j < 3; j++) {
					int k = 0;
					float lvNaviSum = 0;
					while (EncPos / stripLength < j + 2) {
						EncPos = (float) (LineFollowerThread.rightMotor
								.getTachoCount() / 360f * Math.PI * 0.055);
						spNavi.fetchSample(lvNaviArr, 0);
						lvNavi = lvNaviArr[0];
						lvNaviSum += lvNavi;
						k++;
						if (k % 30 == 0)
							drawState("Read bit: " + (j + 1), EncPos, lvNavi,
									currentEdge);
					}
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
			float mindstormSpecificOffset = 0.125f; // [m]
			do {
				EncPos = (float) (LineFollowerThread.rightMotor.getTachoCount()
						/ 360f * Math.PI * 0.055);
				if (l % 30 == 0)
					drawState("drive to begin of crossing", EncPos, lvNavi,
							currentEdge);

				l++;
			} while (EncPos < 5 * stripLength + mindstormSpecificOffset);

			LineFollowerThread.stopFollowing();
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
		 * Diese Funktion berechnet euch bei Übergabe der aktuellen Kante und
		 * der Zielkante den Drehwinkel und führt die Drehung aus
		 **/

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

	public static void turn(int deg) throws InterruptedException {
		
		Thread.sleep(1000);
		RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		lcd.drawString("turn: " + deg, 0, 90, 0);
		deg+=10;
		float wheelbase = 0.1f;
		float wheelradius = 0.05f;
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
