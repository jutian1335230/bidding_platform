package FinalProject;

import java.util.*;
public class Main {
	public static void main (String [] args) {
		List<item> items = new ArrayList<>();
		items.add(new item("basketball", 3));
		items.add(new item("football", 4));
		items.add(new item("table", 10));
		
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
