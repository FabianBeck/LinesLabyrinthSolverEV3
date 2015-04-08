import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lejos.hardware.Button;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.*;
import lejos.hardware.sensor.EV3ColorSensor;

public class main {
	public static void main(String[] args) throws IOException,
			InterruptedException {
		GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();
		FileOutputStream out = null; // declare outside the try block
		File data = new File("meas.dat");
		out = new FileOutputStream(data);
		DataOutputStream dataOut = new DataOutputStream(out);

		EV3ColorSensor lsLine = new EV3ColorSensor(SensorPort.S2);
		lsLine.setFloodlight(true);
		SampleProvider spLine = lsLine.getRedMode();
		EV3ColorSensor lsNavi = new EV3ColorSensor(SensorPort.S3);
		lsNavi.setFloodlight(true);
		SampleProvider spNavi = lsNavi.getRedMode();

		Knot startingKnot;
		// startingKnot.add

		dataOut.writeUTF("lvLine lvNavi \n");

		PIDControllerLineFollowing pidController = new PIDControllerLineFollowing(
				0.04f, 0.22f);
		pidController.KP = 260f;
		pidController.KD = 0f;
		pidController.KI = 0.04f;

		int i = 0;

		float[] lvLine = new float[spLine.sampleSize()];
		float[] lvNavi = new float[spLine.sampleSize()];

		float speedForward = 300; // degrees per second
		float MotorSpeedDiff = 0;

		RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
		RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		leftMotor.forward();
		rightMotor.forward();
		boolean running=true;
		boolean seen=false;
		while (!Button.ENTER.isDown()&&running) {
			i++;

			spLine.fetchSample(lvLine, 0);
			spNavi.fetchSample(lvNavi, 0);

			MotorSpeedDiff = pidController.calcMotorSpeed(lvLine[0]);
			float speedl=speedForward - MotorSpeedDiff;
			float speedr=speedForward + MotorSpeedDiff;
			if(speedl<0){
				leftMotor.backward();
			}else {
				leftMotor.forward();
			}
			if(speedr<0){
				rightMotor.backward();
			}else {
				rightMotor.forward();
			}
			leftMotor.setSpeed((int)(speedForward - MotorSpeedDiff));
			rightMotor.setSpeed((int)(speedForward + MotorSpeedDiff));
			// logging
			if(i%100==0){
			lcd.clear();
			lcd.drawString("lvLine: " + lvLine[0], 0, 0, 0);
			lcd.drawString("lvNavi: " + lvNavi[0], 0, 15, 0);
			lcd.drawString(
					"speedl: "
							+ String.valueOf((speedForward - MotorSpeedDiff)),
					0, 30, 0);
			lcd.drawString(
					"speedr: "
							+ String.valueOf((speedForward + MotorSpeedDiff)),
					0, 45, 0);
			lcd.drawString(
					"x_pos: " + String.valueOf(pidController.x_pos_error), 0,
					60, 0);
			lcd.drawString(
					"x_speed: " + String.valueOf(pidController.x_speed_error),
					0, 75, 0);
			}
			dataOut.writeUTF(String.valueOf(lvNavi[0])+"\n");
			dataOut.flush();
			if(lvNavi[0]<0.1){
				seen=true;
			}
			if(lvNavi[0]>0.1&&seen){
				running=false;
				leftMotor.stop(true);
				rightMotor.stop(true);
			}
			//dataOut.writeChar('\n');
			//+ " " + String.valueOf(lvNavi[0]));
			Thread.sleep(1);
			//Button.DOWN.waitForPressAndRelease();
		}
		leftMotor.close();
		rightMotor.close();
		out.close();
	}
}
