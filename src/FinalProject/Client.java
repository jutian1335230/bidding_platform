package FinalProject;

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
			
			Double msg = 1.0;
			while (true) {
				try {
					// ask user to enter a double
					System.out.print("Enter a number to bid: ");
					msg = scanner.nextDouble();
				} catch (Exception e) {
					scanner.next();
					System.out.println("Try again.");
					continue;
				}
				
				// send the bid to the server
				out.writeDouble(msg);
				out.flush();
				
				// read the server's response, and print it out.
				System.out.println("Client: The server says your bid is: " + in.readDouble());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		scanner.close();
	}
}
