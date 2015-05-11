import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class BTConnection {
	DataOutputStream outToClient;
	Socket connectionSocket;
	ServerSocket welcomeSocket;
	public BTConnection() throws InterruptedException {
		try {
			welcomeSocket= new ServerSocket(5555);
			LinesLabyrinthSolver.lcd.clear();
			LinesLabyrinthSolver.lcd.drawString("Waiting for Client", 0, 15, 0);
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
					connectionSocket.getInputStream()));
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			//PrintWriter outToClient =new PrintWriter(connectionSocket.getOutputStream(), true);
			outToClient.writeInt(4);//("Start"+'\n');
			outToClient.flush();
			outToClient.close();
			connectionSocket.close();
			//connectionSocket.close();
			LinesLabyrinthSolver.lcd.clear();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			LinesLabyrinthSolver.lcd.clear();
			LinesLabyrinthSolver.lcd.drawString("Connection Error", 0, 15, 0);
			Thread.sleep(1000);
			LinesLabyrinthSolver.lcd.clear();
		}
	}

	public void sendPacket(int parentKnot, int childKnot, int direction) {
		try {
			LinesLabyrinthSolver.lcd.clear();
			LinesLabyrinthSolver.lcd.drawString("reconnecting", 0, 15, 0);
			Socket connectionSocket = welcomeSocket.accept();
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			outToClient.writeInt(parentKnot);
			//outToClient.writeInt(childKnot);
			//outToClient.writeInt(direction);
			outToClient.flush();
			outToClient.close();
			connectionSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void closeBTConnection() {
		/*try {
			connectionSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

}
