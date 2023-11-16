package finalProject;

import java.util.*;

import javafx.application.Application;
import javafx.stage.Stage;
public class Main extends Application{
	public static void main (String [] args) {

		launch(args);
		
	}
	public void start(Stage primaryStage) {
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
