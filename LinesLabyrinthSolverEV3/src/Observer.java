public class Observer { 
	private float x_pos, x_pos_est, x_rate_est, x_pos_est_old, x_rate_est_old;
	private float l1 = -0.6f;
	private float l2 = -3.0f;
	private float T; //Abtastzeit

	public Observer(float sampleTime) {
		T=sampleTime;
		x_pos = 0;
		x_pos_est = 0;
		x_rate_est = 0;
		x_pos_est_old = 0;
		x_rate_est_old = 0;
	}
	public float update(float value){
		x_pos_est=x_pos_est_old+T*x_rate_est_old+l1*(x_pos_est_old-value);
		x_rate_est=x_rate_est_old+l2*(x_pos_est_old-value);
		x_pos_est_old=x_pos_est;
		x_rate_est_old=x_rate_est;
		return x_rate_est;
	}
	public float getEstimated_x_pos(){
		return x_pos_est;
	}
	public float getEstimated_x_rate(){
		return x_rate_est;
	}

}
