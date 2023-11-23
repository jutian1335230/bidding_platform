package finalProject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Observable;
//import java.sql.DriverManager;
import com.google.gson.Gson;
import ClientPackage.Message;
import ClientPackage.Message.messageType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

class Server extends Observable {
	public static ArrayList<Item> items = new ArrayList<>();
	public static void main(String[] args) {
		items.add(new Item("basketball", 3));
		items.add(new Item("table", 10));
		new Server().runServer();
    }

	private void runServer() {
		try {
			setUpNetworking();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
    }
 

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		ServerSocket serverSock = new ServerSocket(4242);
		while (true) {
			Socket clientSocket = serverSock.accept();
			ClientHandler handler = new ClientHandler(this, clientSocket);
			this.addObserver(handler);

			Thread t = new Thread(handler);
			t.start();
		}
	}

	protected Message processRequest(String input) {
		Gson gson = new Gson();
		Message message = gson.fromJson(input, Message.class);
	    if (message.type == messageType.LOGIN){
			processLogin(message);
		}
	    else if (message.type == messageType.NEWUSER){
	    	createNewUser(message);
		}
	    else if (message.type == messageType.BID){
			processBid(message);
		}
	    else if (message.type == messageType.ADDITEM) {
	    	addItem(message);
	    }
	    return message;
	}	
	protected void processLogin(Message message) {
		// close connection and statement automatically
        try (Connection connection = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false");
             Statement statement = connection.createStatement()) {

        	String sql = "SELECT * FROM users WHERE username = '" + message.username + "' AND password = '" + message.passwd + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            message.success = resultSet.next();

        } catch (SQLException e) {}
	}
	protected void createNewUser(Message message) {
		// close connection and statement automatically
        try (Connection connection = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false");
             Statement statement = connection.createStatement()) {

            String sql = "INSERT INTO users (username, password) VALUES ('" + message.username + "', '" + message.passwd + "')";
            statement.executeUpdate(sql);
            message.success = true;

        } catch (SQLException e) {}
	}
	protected void processBid(Message message) {
		Item item = null;
		for (Item it : items) {
			if (it.description.equals(message.description)) {
				item = it;
				break;
			}
		}
		synchronized(item) {
			if (item.closed) {
				message.description = "Sorry, auction for this item is closed";
			}
			else if (message.bid <= item.highest_bid) {
				message.description = "bid must be higher than the current bid $" + item.highest_bid;
			}
			else if (message.bid < item.minPrice) {
				message.description = "bid must be at least $" + item.minPrice;
			}
			else {
				item.highest_bid = message.bid;
				message.description = "Server received " + message.bid + " for item " + item.description;
				message.success = true;
			}
		}
	}
	protected void addItem(Message message) {
		try (Connection connection = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false");
			 PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO items (description, minprice, file, soldfrom, name) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

		    preparedStatement.setString(1, message.description);
		    preparedStatement.setDouble(2, message.price);
		    preparedStatement.setBytes(3, message.image);
		    preparedStatement.setString(4, message.username);
		    preparedStatement.setString(5, message.itemname);
		    preparedStatement.executeUpdate();
		    ResultSet keys = preparedStatement.getGeneratedKeys();
		    keys.next();
		    int key = keys.getInt(1);
		    System.out.println(key);
		    
		    if (message.duration != 0) {
			    new Thread(()-> {
			    	while (message.duration != 0) {
				    	try {
				    		Thread.sleep(1000);
				    	}
				    	catch (InterruptedException e){}
				    	message.duration--;
			    	}
			    	String sql = "UPDATE items SET isavailable = false WHERE id = " + key;
			    	try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()){
						statement.executeUpdate(sql);
						System.out.println("no longer available");
						//notify();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			    	
			    }).start();
		    }
		    message.success = true;
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }
	}
	private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

}