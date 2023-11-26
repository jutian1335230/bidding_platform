package finalProject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Observer;

import com.google.gson.Gson;

import java.util.Observable;
import ClientPackage.Message;
@SuppressWarnings("deprecation")
class ClientHandler implements Runnable, Observer {

	private Server server;
	private Socket clientSocket;
	private BufferedReader fromClient;
	private PrintWriter toClient;
	private Gson gson;
	protected ClientHandler(Server server, Socket clientSocket) {
		gson = new Gson();
		this.server = server;
		this.clientSocket = clientSocket;
		try {
			fromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			toClient = new PrintWriter(this.clientSocket.getOutputStream());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		String input;
		try {
			while ((input = fromClient.readLine()) != null) {
				toClient.println(gson.toJson(server.processRequest(input)));
				toClient.flush();
			}
		} 
		catch (IOException e) { 
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Message message = (Message) arg;
		message.isUpdateThread = true;
		toClient.println(gson.toJson(message));
		toClient.flush();
		message.isUpdateThread = false;
	}
}