public class PIDControllerLineFollowing {
	public float x_pos_error, x_speed_error;
	public float KP = 0, KD = 0, KI = 0;
	public float intBias;
	private float integral;
	private float T;
	private Observer derivativeObserv;
	private float _setPoint;

	public PIDControllerLineFollowing(float sampleTime, float setPoint) {
		T = sampleTime;
		_setPoint = setPoint;
		derivativeObserv = new Observer(sampleTime);
		integral = 0;

	}

	public float calcMotorSpeed(float inputValue) {
		// calc x_pos from lightValue
		x_pos_error = (float) (inputValue - _setPoint);
		x_speed_error = derivativeObserv.update(x_pos_error);
		x_pos_error = derivativeObserv.getEstimated_x_pos();
		integral += T * x_pos_error*intBias;
		if (integral > 1000) {
			integral = 1000;
		}
		if (integral < -1000) {
			integral = -1000;
		}
		float u=KP * (x_pos_error + KI * integral + KD * x_speed_error);
		
		// limit output
		if(Math.abs(u)>70){
			u=70*Math.signum(u);
		}
		return u; 

	}
	public void resetPID(){
		integral=0;
	}

}
