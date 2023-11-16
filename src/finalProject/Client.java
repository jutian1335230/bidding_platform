package finalProject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	int port = 8000;
	String host = "localhost";
	DataInputStream in;
	DataOutputStream out;
	Socket socket;

	
	public void runme () {
		Scanner scanner = new Scanner(System.in);
		
		try {
			// Define client socket, and initialize in and out streams.
			socket = new Socket(host, port);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			double msg = 0;
			String description = null;
			while (true) {
				try {
					System.out.print("Enter an object to bid: ");
					description = scanner.next();
					System.out.print("Enter a number to bid: ");
					msg = scanner.nextDouble();
				} 
				catch (Exception e) {
					scanner.next();
					System.out.println("Try again.");
					continue;
				}
				// send the bid to the server
				out.writeUTF(description);
				out.flush();
				out.writeDouble(msg);
				out.flush();
				System.out.println(in.readUTF());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		scanner.close();
	}
}
