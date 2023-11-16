package finalProject;

import java.util.*;
public class Main {
	public static void main (String [] args) {

		
		Server server = new Server();
		Client client = new Client();
		
		new Thread(new Runnable() {
			@Override 
			public void run() { 
				server.runme();
			}
		}).start();
		//for (int i = 0; i < 10; i++) {
			new Thread(new Runnable () {
				@Override
				public void run() {
					client.runme();
				}
			}).start();
		//}
	}
}
