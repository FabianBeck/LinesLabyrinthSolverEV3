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
import lejos.robotics.SampleProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.*;
import lejos.hardware.sensor.EV3ColorSensor;

public class LinesLabyrinthSolver {
	static int north = 0;
	static int west = 1;
	static int south = 2;
	static int east = 3;

	static float stripLength = 0.017f;
	static GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();

	static void drawState(String Name, float EncPos, float lvNavi) {
		lcd.clear();
		lcd.drawString("State: " + Name, 0, 15, 0);
		lcd.drawString("EncPos: " + EncPos, 0, 30, 0);
		lcd.drawString("lvNavi: " + lvNavi, 0, 45, 0);

		// lcd.drawString("crossing: "
		// +crossArr[0]+" "+crossArr[1]+" "+crossArr[2],0,15,0);
	}
	public static void main(String[] args) throws InterruptedException {
		BTConnection btcon = new BTConnection();
		btcon.sendPacket(1, 0, 0);
		
		FileOutputStream out = null; // declare outside the try block
		File data = new File("meas.dat");
		try {
			out = new FileOutputStream(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DataOutputStream dataOut = new DataOutputStream(out);

		EV3ColorSensor lsNavi = new EV3ColorSensor(SensorPort.S3);
		lsNavi.setFloodlight(true);
		SampleProvider spNavi = lsNavi.getRedMode();

		Knot startingKnot = new Knot();
		Vector<Integer> crossing = new Vector<Integer>();
		crossing.add(north);
		startingKnot.addOptions(crossing);
		crossing.removeAllElements();
		Knot lastKnot = startingKnot;
		Edge currentEdge = startingKnot.getNextOption();
		// dataOut.writeUTF("lvNavi \n");
		int i = 0;

		float[] lvNaviArr = new float[spNavi.sampleSize()];
		float lvNavi = 0;
		float lightThreshold = 0.18f;

		LineFollowerThreadObject LineFollowerThread = new LineFollowerThreadObject();
		LineFollowerThread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		LineFollowerThread.startFollowing();

		while (!Button.ESCAPE.isDown()) {
			i++;
			LineFollowerThread.startFollowing();
			// Wait for reading
			int l = 0;
			do {

				spNavi.fetchSample(lvNaviArr, 0);
				lvNavi = lvNaviArr[0];
				if (l % 30 == 0)
					drawState("Wait for reading", 0, lvNavi);
				l++;
			} while (lvNavi > lightThreshold);
			LineFollowerThread.rightMotor.resetTachoCount();
			float EncPos = 0;
			// read bits
			do {
				EncPos = (float) (LineFollowerThread.rightMotor.getTachoCount()
						/ 360f * Math.PI * 0.055);
				drawState("Wait for first bit to read", EncPos, lvNavi);
			} while (EncPos < stripLength);
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
						drawState("Read bit: " + (j + 1), EncPos, lvNavi);
				}
				if (lvNaviSum / k > lightThreshold) {
					// TODO:crossing.addElement((lastKnot.getParentEdge().direction+j+2)%4);

				}
			}
			// drive to begin of crossing
			do {

				EncPos = (float) (LineFollowerThread.rightMotor.getTachoCount()
						/ 360f * Math.PI * 0.055);
				spNavi.fetchSample(lvNaviArr, 0);
				lvNavi = lvNaviArr[0];
				if (l % 30 == 0)
					drawState("drive to begin of crossing", EncPos, lvNavi);
				l++;
			} while ((EncPos < 5 * stripLength) || (lvNavi < lightThreshold));
			LineFollowerThread.stopFollowing();
			/*
			 * while(!Button.ENTER.isDown()&&!Button.ESCAPE.isDown()){
			 * EncPos=(float) (LineFollowerThread.rightMotor.getTachoCount()/
			 * 360f*Math.PI*0.055); spNavi.fetchSample(lvNaviArr, 0);
			 * lvNavi=lvNaviArr[0]; drawState("reached crossing:", EncPos,
			 * lvNavi);
			 * lcd.drawString(String.valueOf(crossArr[0])+String.valueOf
			 * (crossArr[1])+String.valueOf(crossArr[2]),0,60,0);
			 * Button.RIGHT.waitForPressAndRelease(); }
			 */

			drawState("reached crossing:", EncPos, lvNavi);
			String directions = "";
			for (int j = 0; j < crossing.size(); j++) {
				directions += crossing.get(j);
				// TODO:currentEdge.
			}

			lcd.drawString("Directions:" + directions, 0, 60, 0);
			Button.UP.waitForPressAndRelease();

			Knot newKnot = new Knot();
			// TODO:newKnot.addParentEdge(currentEdge);
			newKnot.addOptions(crossing);

			// logging
			// dataOut.writeUTF(String.valueOf(lvNavi)+"\n");
			// dataOut.flush();

		}

		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
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
}
