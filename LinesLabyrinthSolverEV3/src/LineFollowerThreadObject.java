import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class LineFollowerThreadObject extends Thread {
    
	static float diameter= 0.055f; //Radurchmesser des jeweiligen Mindstorms
	
	boolean running = true;
	boolean quit = false;
	public float lvLine = 0;
	public float speedForward = 250; // degrees per second
	float MotorSpeedDiff = 0;
	static RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
	static RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3ColorSensor lsLine=new EV3ColorSensor(SensorPort.S2);
	PIDControllerLineFollowing pidController;
	GraphicsLCD lcd = LocalEV3.get().getGraphicsLCD();

	public void run() {
		
		pidController = new PIDControllerLineFollowing(0.04f, 0.22f);
		pidController.KP = 235f;
		pidController.KD = 0f;
		pidController.KI = 0.0f;

		lsLine.setFloodlight(true);
		SampleProvider spLine = lsLine.getRedMode();
		float[] lvLineArr = new float[spLine.sampleSize()];
		boolean motorsStopped = false;

		leftMotor.forward();
		rightMotor.forward();

		while (!quit) {
			while (running) {
				spLine.fetchSample(lvLineArr, 0);
				lvLine = lvLineArr[0];

				MotorSpeedDiff = pidController.calcMotorSpeed(lvLineArr[0]);
				float speedl = speedForward - MotorSpeedDiff;
				float speedr = speedForward + MotorSpeedDiff;
				if (speedl < 0) {
					leftMotor.backward();
				} else {
					leftMotor.forward();
				}
				if (speedr < 0) {
					rightMotor.backward();
				} else {
					rightMotor.forward();
				}
				leftMotor.setSpeed((int) speedl);
				rightMotor.setSpeed((int) speedr);
				motorsStopped=false;
			}
			if (!motorsStopped) {
				leftMotor.stop(true);
				rightMotor.stop(true);
				leftMotor.close();
				rightMotor.close();				
				motorsStopped=true;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//leftMotor.close();
		//rightMotor.close();

	}

	public void startFollowing() {
		pidController.resetPID();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);   
		rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);  
		running = true;
	}

	public void stopFollowing() {
		running = false;
	}

	public void quitThread() {
		running = false;
		quit = true;
	}

	public float getSpeed() {
		return speedForward;
	}

	public float getAngularRate() {
		return diameter * MotorSpeedDiff / 0.125f;
	}

	public void reset() {
		pidController.resetPID();
	}

}
