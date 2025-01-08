/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* <Tony Tian>
* <jt47232>
* <17610>
* Spring 2023
*/

package finalProject;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Observable;

import com.google.gson.Gson;

import finalProject.Message.messageType;
@SuppressWarnings("deprecation")
class Server extends Observable {
	public static void main(String[] args) {
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
	    else if (message.type == messageType.GETDATA) {
	    	getData(message);
	    }
	    else if (message.type == messageType.GETITEM) {
	    	getItem(message);
	    }
	    else if (message.type == messageType.REMOVEITEM) {
	    	removeItem(message);
	    }
	    else if (message.type == messageType.ACCEPTBID) {
	    	acceptBid(message);
	    }
	    else if (message.type == messageType.GETPRIZES) {
	    	getPrizes(message);
	    }
	    else if (message.type == messageType.GETMONEY) {
	    	getMoney(message);
	    }
	    return message;
	}	
	protected void processLogin(Message message) {
		// close connection and statement automatically
        try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {

        	String sql = "SELECT * FROM users WHERE username = '" + message.username + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
            	String storedHashedPassword = resultSet.getString("password");
                String storedSalt = resultSet.getString("salt");

                // Hash the input password with the stored salt
                String hashedPasswordToCheck = hashPassword(message.passwd, Base64.getDecoder().decode(storedSalt));
                message.success = hashedPasswordToCheck.equals(storedHashedPassword);
            }
            

        } catch (SQLException e) {}
	}

	protected void processBid(Message message) {
		try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {

			String sql = "SELECT * FROM items WHERE name = '" + message.itemname + "'";
			ResultSet resultset = statement.executeQuery(sql);
			resultset.next();
			double current_bid = resultset.getDouble("highestbid");
			double minprice = resultset.getDouble("minprice");
			double buyItNow = resultset.getDouble("buyitnow");
			if (!resultset.getBoolean("isavailable")) {
				message.description = "Sorry, this item is no longer available";
				return;
			}
			else if (message.bid < minprice) {
				message.description = "bid must be higher than the minimum price";
				return;
			}
			else if (message.bid <= current_bid) {
				message.description = "bid must be higher than the current highest bid";
				return;
			}
			else {
				sql = "UPDATE items SET highestbidder = '" + message.username + "', highestbid = " + message.bid + " WHERE name = '" + message.itemname + "'";
				statement.executeUpdate(sql);
				message.success = true;
				if (buyItNow != 0 && message.bid >= buyItNow) {
					message.type = messageType.ACCEPTBID;
					acceptBid(message);
				}
				else {
					setChanged();
					notifyObservers(message);
				}
			}
		}
		catch (SQLException e) {}
	}
	protected void addItem(Message message) {
		try (PreparedStatement preparedStatement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").prepareStatement("INSERT INTO items (description, minprice, file, soldfrom, name, soldto, buyitnow, highestbidder) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

		    preparedStatement.setString(1, message.description);
		    preparedStatement.setDouble(2, message.price);
		    preparedStatement.setBytes(3, message.image);
		    preparedStatement.setString(4, message.username);
		    preparedStatement.setString(5, message.itemname);
		    preparedStatement.setString(6, "");
		    preparedStatement.setDouble(7, message.buyItNow);
		    preparedStatement.setString(8, "");
		    preparedStatement.executeUpdate();
		    message.success = true;
		    message.description = "Item added successfully";;
		    setChanged();
			notifyObservers(message);
			if (message.duration != 0) { 
			    new Thread(()-> {
			    	while (message.duration != 0) {
				    	try {
				    		Thread.sleep(1000);
				    	}
				    	catch (InterruptedException e){}
				    	message.duration--;
			    	}
			    	String highestbidder = "";
			    	boolean isavailable = false;
			    	try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {
			    		String sql = "SELECT * FROM items WHERE name = '" + message.itemname + "'";
			    		ResultSet resultSet = statement.executeQuery(sql);
						resultSet.next();
						highestbidder = resultSet.getString("highestbidder");
						isavailable = resultSet.getBoolean("isavailable");
			    	}
			    	catch (Exception e) {}
			    	if (isavailable) {
						if (highestbidder.equals("")) {
							message.type = messageType.REMOVEITEM;
							removeItem(message);
						}
						else {
							message.type = messageType.ACCEPTBID;
							acceptBid(message);
						}
			    	}
			    }).start();
		    }
	    } catch (SQLException e) {
	    	message.description = "Item name already exists. Please use another name.";
	    }
		
	}
	protected void getData(Message message) {
		try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {

			String sql = "SELECT name FROM items";
            ResultSet resultSet = statement.executeQuery(sql);
            message.itemnames = new ArrayList<>();
            while (resultSet.next()) {
            	message.itemnames.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {}
	}
	protected void getItem(Message message) {
		try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {

			String sql = "SELECT * FROM items WHERE name = '" + message.itemname + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
            	message.itemname = resultSet.getString("name");
            	message.description = resultSet.getString("description");
            	message.owner = resultSet.getString("soldfrom");
            	message.price = resultSet.getDouble("minprice");
            	message.buyItNow = resultSet.getDouble("buyitnow");
            	message.username = resultSet.getString("highestbidder");
            	message.bid = resultSet.getDouble("highestbid");
            	message.image = resultSet.getBytes("file");
            	message.soldTo = resultSet.getString("soldto");
            	message.isavailable = resultSet.getBoolean("isavailable");
            	message.success = true;
            }
        } catch (SQLException e) {}
	}
	protected void removeItem(Message message) {
		try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {
			String sql = "UPDATE items SET isavailable = false WHERE name = '" + message.itemname + "'";
			statement.executeUpdate(sql);
			setChanged();
			notifyObservers(message);
		} catch (SQLException e) {}
		
	}
	protected void acceptBid(Message message) {
		try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {
			String sql = "SELECT * FROM items WHERE name = '" + message.itemname + "'";
			ResultSet resultSet = statement.executeQuery(sql);
			resultSet.next();
			String buyer = resultSet.getString("highestbidder");
			message.username = buyer;
			message.owner = resultSet.getString("soldfrom");
			double amount = resultSet.getDouble("highestbid");
			message.bid = amount;
			sql = "UPDATE items SET isavailable = false, soldto = '"+ buyer +"' WHERE name = '" + message.itemname + "'";
			statement.executeUpdate(sql);
			sql = "UPDATE users SET money = money - " + amount +" WHERE username = '" + buyer + "'";
			statement.executeUpdate(sql);
			sql = "UPDATE users SET money = money + " + amount +" WHERE username = '" + message.owner + "'";
			statement.executeUpdate(sql);
			message.success = true;
			setChanged();
			notifyObservers(message);
			
		}	
		catch (SQLException e) {}
	}
	protected void getPrizes(Message message) {
		try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {
			String sql = "SELECT name FROM items WHERE soldto = '" + message.username + "'";
			ResultSet resultSet = statement.executeQuery(sql);
			message.itemnames = new ArrayList<>();
			while (resultSet.next()) {
				message.itemnames.add(resultSet.getString("name"));
			}
		} catch (SQLException e) {}
	}
	protected void getMoney(Message message) {
		try (Statement statement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").createStatement()) {
			String sql = "SELECT money FROM users WHERE username = '" + message.username + "'";
			ResultSet resultSet = statement.executeQuery(sql);
			resultSet.next();
			message.money = resultSet.getDouble("money");
		} catch (SQLException e) {}
	}
	
	// add hash
	
	protected void createNewUser(Message message) {
        // Generate a random salt
		byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        // Hash the password with the salt
        String hashedPassword = hashPassword(message.passwd, salt);

        try (PreparedStatement preparedStatement = DriverManager.getConnection("jdbc:derby:C:/Apache/db-derby-10.14.2.0-bin/bin/userDB;create=false").prepareStatement("INSERT INTO users (username, password, salt) VALUES (?, ?, ?)")) {

            preparedStatement.setString(1, message.username);
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.setString(3, Base64.getEncoder().encodeToString(salt));

            preparedStatement.executeUpdate();
            message.success = true;

        } catch (SQLException e) {}
    }

    private String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

}