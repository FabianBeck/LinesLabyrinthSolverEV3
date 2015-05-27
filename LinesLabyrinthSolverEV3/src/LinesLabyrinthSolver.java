import lejos.hardware.Button;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.*;
import lejos.hardware.sensor.EV3ColorSensor;

public class LinesLabyrinthSolver {
	static EV3ColorSensor lsNavi;
	static float[] lvNaviArr = { 0 };
	static float lvNavi = 0;
	static SampleProvider spNavi;

	static GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
	static LineFollowerThreadObject LineFollowerThread;

	static Knot currentKnot;
	static Edge currentEdge;
	static boolean onWayBack = false;
	static int crossArr[] = { 0, 0, 0 };

	static float stripLength = 0.018f; // Klebebandbreite
	static float lightThreshold = 0.18f; // Grenzwert des Lichtsensors

	static float mindstormSpecificOffset = 0.125f; // [m]
	// Korrekturwert fuer jeden Mindstorm individuell zu testen

	static float wheeldiameter = 0.056f; // Radurchmesser
	static float wheelbase = 0.105f; // Radabstand

	public static void main(String[] args) throws InterruptedException {

		lsNavi = new EV3ColorSensor(SensorPort.S3);
		lsNavi.setFloodlight(true);
		spNavi = lsNavi.getRedMode();

		lvNaviArr = new float[spNavi.sampleSize()];
		// Initializing
		/**
		 * Initialisierung wie in Greenfoot. Zusaetzlich Werden noch die
		 * Lichtsensoren und der LinefollowerThread initialisiert, der fuer die
		 * Verfolgung der Linie verantwortlich ist.
		 * 
		 * **/

		currentEdge = new Edge(0, null);
		currentKnot = new Knot(currentEdge);
		currentEdge.setChild(currentKnot);

		LineFollowerThread = new LineFollowerThreadObject();
		LineFollowerThread.start();
		LineFollowerThread.stopFollowing();
		Thread.sleep(1000);

		// Main loop
		while (!Button.ESCAPE.isDown()) {

			// const accelerate to normal speed
			// without integral part
			LineFollowerThread.pidController.intBias = 0;
			LineFollowerThread.startFollowing();

			for (int i = 0; i < 100; i++) {
				LineFollowerThread.increaseSpeed(2.5f);
				Thread.sleep(10);
			}
			LineFollowerThread.pidController.intBias = 1;

			// drive to next crossing and readInformation:
			drawState("reading Information", 0, lvNavi, currentEdge);
			readInformation();

			LineFollowerThread.stopFollowing();

			/**
			 * Hier ist wieder der eigentliche Algorithmus aus Greenfoot zu
			 * finden
			 * **/

			Edge nextEdge = currentKnot.getNextOption();
			if (nextEdge != null) {
				// found option
				onWayBack = false;
				rotate(currentEdge, nextEdge);
				currentEdge = nextEdge;
				currentKnot = nextEdge.getChild();
			} else {
				// return to previous Crossing
				onWayBack = true;
				nextEdge = currentKnot.getWayBack();
				rotate(currentEdge, nextEdge);
				currentKnot = nextEdge.getParent();
				currentEdge = nextEdge;
			}

		}

		lsNavi.close();
		LineFollowerThread.stopFollowing();
		LineFollowerThread.quitThread();
		LineFollowerThread.join();
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
		 * Zeigt den aktuellen internen Zustand des Mindstorms an : -state: Was
		 * mache ich gerade EncPos: Zur�ckgelegte Strecke lvNavi: Wert des
		 * Lichtsensors cur.dir: aktuelle richtung
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

		/**
		 * Hier wird sich um die die mitgegebene Gradzahl gedreht Daf�r werden
		 * zuerst die Motoren initialisiert und dann um die
		 * Mindstormspezifischen Werte (Abh�ngig Radabstand und Radgr��e)
		 * gedreht.
		 *
		 */

		Thread.sleep(1000);
		RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		lcd.drawString("turn: " + deg, 0, 90, 0);
		// deg += 10;
		// Button.ENTER.waitForPressAndRelease();
		// Sound.beepSequenceUp();
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);

		int angle = (int) (2 * Math.PI * wheelbase * deg / (2 * Math.PI * wheeldiameter));

		leftMotor.rotate(angle, true);
		rightMotor.rotate(-angle, false);

		while (leftMotor.isMoving() || rightMotor.isMoving()) {
			Thread.sleep(10);
		}
		leftMotor.stop();
		rightMotor.stop();

		leftMotor.close();
		rightMotor.close();

	}

	public static float readEncoderPostion() {
		return (float) (LineFollowerThreadObject.rightMotor.getTachoCount()
				/ 360f * Math.PI * 0.055);
	}

	public static void readInformation() throws InterruptedException {

		Thread.sleep(250);
		int l = 0;
		do {

			spNavi.fetchSample(lvNaviArr, 0);
			lvNavi = lvNaviArr[0];
			if (l % 30 == 0)
				// drawState("Wait for reading", 0, lvNavi, currentEdge);
				l++;
		} while (lvNavi > lightThreshold);

		// reached next crossing
		LineFollowerThreadObject.rightMotor.resetTachoCount();
		float EncPos = 0;

		if (!onWayBack) {
			// read bits
			do {
				EncPos = readEncoderPostion();
				// drawState("Wait for first bit to read", EncPos, lvNavi,
				// currentEdge);
			} while (EncPos < stripLength);
			// Reading bits

			/**
			 * Auslesen der Klebebandmarkierungen:
			 * 
			 * 1. Reinitialisierung 2. Auslesen 3. Speichern
			 * 
			 */

			for (int i = 0; i < crossArr.length; i++) {
				crossArr[i] = 0; // Reinitialisierung
			}
			for (int j = 0; j < 3; j++) {
				/**
				 * Hier findet das eigentliche auslesen des Wertes statt, daf�r
				 * wird die zur�ckgelegte Streke gespeichert, um zu wissen
				 * welcher Streifen gerade gelesen wird. um den
				 * Lichtsensorwerten den passenden Klebebandstreifen zuzuordnen.
				 * **/

				int k = 0;
				float lvNaviSum = 0;
				while (EncPos / stripLength < j + 2) {
					EncPos = readEncoderPostion();
					spNavi.fetchSample(lvNaviArr, 0);
					lvNavi = lvNaviArr[0];
					lvNaviSum += lvNavi;
					k++;
					/*
					 * if (k % 30 == 0) drawState("Read bit: " + (j + 1),
					 * EncPos, lvNavi, currentEdge);
					 */
				}

				// Speichern der Sensorwerte in unsere Kreuzung
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

			}
		}
		// drive to middle of crossing

		do {

			/**
			 * Nachdem, man die Kreuzung ausgelesen hat muss noch weiter
			 * gefahren werden bis man auf der Kreuzung steht, dies geschieht
			 * hier.
			 * 
			 * 
			 * **/
			EncPos = readEncoderPostion();
			if (l % 100 == 0)
				drawState("drive to begin of crossing", EncPos, lvNavi,
						currentEdge);

			l++;
		} while (EncPos < 5 * stripLength + mindstormSpecificOffset);

	}

}
