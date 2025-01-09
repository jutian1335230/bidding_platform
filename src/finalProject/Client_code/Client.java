/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* <Tony Tian>
* <jt47232>
* <17610>
* Spring 2023
*/

package ClientPackage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import com.google.gson.Gson;
import ClientPackage.Message.messageType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Client extends Application{
	
	private static String host = "127.0.0.1";
	private static Gson gson;
	private BufferedReader fromServer;
	private PrintWriter toServer;
	private Socket socket;
	private Stage primaryStage;
	private String username;
	private ArrayList<String> itemnames = new ArrayList<>();
	private ArrayList<String> prizenames = new ArrayList<>();
	private boolean prizeViewing;
	private String history = "";
	public static void main(String[] args){
		launch(args);
	}
	
	@FXML
    private ImageView backToHomeBtn;
	@FXML
    void backToHomeBtnClicked(MouseEvent event) {
		setUpStage("Home.fxml");
    }
	
	// Home page
    @FXML
    private ImageView HomeBackground;
    
    @FXML
    private Hyperlink viewItemsLink;
    
	@FXML
    private Hyperlink InventoryLink;

    @FXML
    private Hyperlink addItemLink;

    @FXML
    private Hyperlink placeBidLink;
    
    @FXML
    private Label bankBalanceLabel;
    
    @FXML
    private Button QuitBtn;
    
    @FXML
    private Hyperlink historyLink;
    
    @FXML
    void historyLinkClicked(ActionEvent event) {
    	setUpStage("History.fxml");
    	historyTxt.setText(history);
    }
    
    @FXML
    void QuitBtnClicked(ActionEvent event) {
    	primaryStage.close();
    	try {
			socket.close();
		} catch (IOException e) {}
    }
    
    @FXML
    void InventoryLinkClicked(ActionEvent event) {
    	setUpStage("bid.fxml");
    	prizeViewing = true;
    	statusPrompt.setVisible(false);
		statusLabel.setVisible(false);
    	toServer.println(gson.toJson(new Message(username)));
		toServer.flush();
    }

    @FXML
    void addItemLinkClicked(ActionEvent event) {
    	setUpStage("addItem.fxml");
    }

    @FXML
    void placeBidLinkClicked(ActionEvent event) {
    	setUpStage("bid.fxml");
    	toServer.println(gson.toJson(new Message()));
		toServer.flush();
    }
    // history page
    @FXML
    private TextArea historyTxt;

	// login page
	@FXML
    private Hyperlink GuestLink;

    @FXML
    private Button LoginBtn;

    @FXML
    private Hyperlink NewUserLink;

    @FXML
    private PasswordField PasswdTxt;

    @FXML
    private TextField userTxt;
   
    
    @FXML
    void GuestLinkClicked(ActionEvent event) {
    	username = "GUEST";
    	setUpStage("Home.fxml");
    }

    @FXML
    void LoginBtnClicked(ActionEvent event){
    	toServer.println(gson.toJson(new Message(userTxt.getText(), PasswdTxt.getText(), messageType.LOGIN)));
		toServer.flush();
    }

    @FXML
    void NewUserLinkClicked(ActionEvent event){
    	setUpStage("newUser.fxml");
    }
    
    // search section
   
    @FXML
    private ListView<String> dropDownList;

    @FXML
    private TextField searchBar;

    @FXML
    private ImageView searchImage;

    
    @FXML
    void search(MouseEvent event) {
    	getItemInfo(searchBar.getText());
    }
    
    @FXML
    void listViewClicked(MouseEvent event) {
    	searchBar.setText(dropDownList.getSelectionModel().getSelectedItem());
    	getItemInfo(searchBar.getText());
    }
    
    @FXML
    void searchBarUpdate(KeyEvent event) {
    	dropDownList.setVisible(true);
    	dropDownList.getItems().clear();
    	if (event.getCode()==(KeyCode.ENTER)) {
    		getItemInfo(searchBar.getText());
    	}
    	if (searchBar.getText()==null ) {
    		if (prizeViewing) {
    			dropDownList.getItems().addAll(prizenames);
    		}
    		else {
    			dropDownList.getItems().addAll(itemnames);
    		}
    		
    	}
    	else {
    		if (prizeViewing) {
    			for (String name : prizenames) {
        			if (name.contains(searchBar.getText())) {
        				dropDownList.getItems().add(name);
        			}
        		}
    		}
    		else {
	    		for (String name : itemnames) {
	    			if (name.contains(searchBar.getText())) {
	    				dropDownList.getItems().add(name);
	    			}
	    		}
    		}
    	}
    	
    }
    private void getItemInfo(String itemname) {
    	dropDownList.setVisible(false);
    	searchImage.requestFocus();
		toServer.println(gson.toJson(new Message(username, itemname)));
		toServer.flush();
    }
    // bid page
    @FXML
    private Label ownerLabel;

    @FXML
    private Button acceptBtn;
    
    @FXML
    private Button bidBtn;

    @FXML
    private Label bidLabel;

    @FXML
    private TextField bidTxt;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label highestBidLabel;

    @FXML
    private Label highestBidderLabel;

    @FXML
    private ImageView itemImage;

    @FXML
    private Label minpriceLabel;

    @FXML
    private Label nameLabel;
    
    @FXML
    private Button removeItemBtn;

    @FXML
    private Label buyItNowLabel;
    
    @FXML
    private Label buyerLabel;

    @FXML
    private Label statusLabel;
    
    @FXML
    private Label statusPrompt;
    @FXML
    void acceptBid(ActionEvent event) {
    	toServer.println(gson.toJson(new Message(nameLabel.getText(), messageType.ACCEPTBID)));
		toServer.flush();
    }

    @FXML
    void removeItem(ActionEvent event) {
    	toServer.println(gson.toJson(new Message(nameLabel.getText(), messageType.REMOVEITEM)));
		toServer.flush();
    }

    @FXML
    void bidBtnClicked(ActionEvent event) {
    	
    	try {
			toServer.println(gson.toJson(new Message(nameLabel.getText(), username, Double.parseDouble(bidTxt.getText()))));
			toServer.flush();
    	}
    	catch (NumberFormatException e) {
    		showError("Please enter a numeric value for bid");
    	}
    	
    	
    }
    
    
    // new user sign in page
    @FXML
    private PasswordField confirmPasswdTxt;

    @FXML
    private PasswordField newPasswdTxt;

    @FXML
    private TextField newUserTxt;

    @FXML
    private Button signInBtn;
    
    @FXML
    private ImageView backBtn;

    @FXML
    void signInBtnClicked(ActionEvent event) {
    	String username = newUserTxt.getText();
    	String passwd = newPasswdTxt.getText();
    	if (username.equals("")) {
    		showError("Please Enter a username");
    	}
    	else if (username.equals("GUEST")) {
    		showError("User already exists");
    	}
    	else if (username.contains(" ")) {
    		showError("Username must not contain spaces");
    	}
    	else if (username.length() > 50) {
    		showError("Username must be at most 50 digits long");
    	}
    	else if (passwd.length() < 8) {
    		showError("Password must contain 8 characters");
    	}
    	else if (passwd.length() > 50) {
    		showError("Password must contain at most 50 characters");
    	}
    	else if (!containsSymbol(passwd)) {
    		showError("Password must contain a Symbol(~`! @#$%^&*()_-+={[}]|\\:;\"'<,>.?/)");
    	}
    	else if (!containsDigit(passwd)) {
    		showError("Password must contain a Number(0-9)");
    	}
    	else if (!containsLetter(passwd)) {
    		showError("Password must contain a letter");
    	}
    	else if (!passwd.equals(confirmPasswdTxt.getText())) {
    		showError("password and confirm password don't match");
    	}
    	else {
    		toServer.println(gson.toJson(new Message(newUserTxt.getText(), newPasswdTxt.getText(), messageType.NEWUSER)));
    		toServer.flush();
    	}
    }
    
    @FXML
    void backBtnClicked(MouseEvent event) {
    	setUpStage("login.fxml");
    }
    
    // addItem page
    @FXML
    private TextField descriptionTxt;
    
    @FXML
    private Hyperlink imageUploadLink;

    @FXML
    private TextField minpriceTxt;
    
    @FXML
    private TextField durationTxt;
    
    @FXML
    private TextField buyItNowTxt;
    
    @FXML
    private TextField itemnameTxt;
    
    @FXML
    void addBtnClicked(ActionEvent event) {
    	String description = descriptionTxt.getText();
    	String minprice = minpriceTxt.getText();
    	String itemname = itemnameTxt.getText();
    	if (description.equals("") || minprice.equals("") || itemname.equals("")) {
    		showError("Please fill in the required fields");
    	}
    	else if (itemname.contains(" ")) {
    		showError("Item name must not contain spaces");
    	}
    	else {
    		double price;
    		int duration = 0;
    		double buyItNow = 0;
    		try {
    			price = Double.parseDouble(minprice);
    			if (price <= 0) {
    				showError("The minimum starting price must be greater than 0");
    				return;
    			}
    		}
    		catch (NumberFormatException e) {
    			showError("Please enter a number for minimum starting price");
    			return;
    		}
    		if (!durationTxt.getText().equals("")) {
    			try {
    				duration = Integer.parseInt(durationTxt.getText());
    				if (duration <= 0) {
    					showError("Duration must be greater than 0");
        				return;
    				}
    			}
    			catch (NumberFormatException e) {
        			showError("Please enter an integer for duration");
        			return;
        		}
    		}
    		if (!buyItNowTxt.getText().equals("")) {
    			try {
    				buyItNow = Double.parseDouble(buyItNowTxt.getText());
    				if (buyItNow <= 0) {
    					showError("'Buy-It-Now' price must be greater than 0");
        				return;
    				}
    			}
    			catch (NumberFormatException e) {
        			showError("Please enter a number for 'Buy-It-Now' price");
        			return;
        		}
    		}
    		byte[] image = null;
    		if (!imageUploadLink.getText().equals("Upload Here")) {
				try {
					image = Files.readAllBytes(Paths.get(imageUploadLink.getText()));
				} catch (IOException e) {}
    		}
    		toServer.println(gson.toJson(new Message(description, price, image, duration, username, itemname, buyItNow)));
    		toServer.flush();
    		
    	}
    }
    @FXML
    void imageUploadLinkClicked(ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.bmp", "*.gif"));
    	File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            imageUploadLink.setText(file.getAbsolutePath());
        }
        else {
        	imageUploadLink.setText("Upload Here");
        }
    }

    
    
	
	public void start(Stage primaryStage) {
		Client client = new Client();
		try {
			client.setUpNetworking();
		} catch (Exception e) {}
		client.primaryStage = primaryStage;
		client.setUpStage("login.fxml");
	}
    
	
	private void setUpNetworking() throws Exception {
		gson = new Gson();
		socket = new Socket(host, 4242);
		fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		toServer = new PrintWriter(socket.getOutputStream());
		new Thread(new Runnable() {
			@Override
			public void run() {
				String input;
				try {
					while ((input = fromServer.readLine()) != null) {
						Message message = gson.fromJson(input, Message.class);
						Platform.runLater(() ->{
							if (message.type == messageType.BID) {
								if (message.isUpdateThread) {
									if (message.success) {
										updateItem(message, message.username + " bidded $" + message.bid + " on " + message.itemname);
										
									}
								}
								else if (message.success) {
									showConfirmation("Bid successful!");
								}
								else {
									showError(message.description);
								}
							}
							
							else if (message.type == messageType.NEWUSER) {
								if (message.success) {
									username = message.username;
									setUpStage("Home.fxml");
								}
								else {
									showError("User already exists");
								}
							}
							
							else if (message.type == messageType.LOGIN) {
								if (message.success) {
									username = message.username;
									setUpStage("Home.fxml");
								}
								else {
									showError("Your username or password is incorrect");
								}
							}
							
							else if (message.type == messageType.ADDITEM) {
								if (message.isUpdateThread) {
									updateItem(message, message.username + " added " + message.itemname + " to the auction");
									if (!prizeViewing) {
										updateSearch(message);
									}
								}
								else if (message.success) {
									showConfirmation(message.description);
								}
								else {
									showError(message.description);
								}
							}
							
							else if (message.type == messageType.GETDATA) {
								itemnames = message.itemnames;
								try {
									dropDownList.getItems().addAll(itemnames);
								}
								catch (Exception e) {}
							}
							else if (message.type == messageType.GETPRIZES) {
								prizenames = message.itemnames;
								try {
									dropDownList.getItems().addAll(prizenames);
								}
								catch (Exception e) {}
							}
							
							else if (message.type == messageType.GETMONEY) {
								if (message.money >= 0) {
									bankBalanceLabel.setText("Your bank balance: $" + message.money);
								}
								else {
									bankBalanceLabel.setText("Your bank balance: -$" + message.money * -1);
								}
							}
							
							else if (message.type == messageType.ACCEPTBID) {
								if (message.success) {
									if (message.isUpdateThread) {
										updateItem(message, message.username + " won the auction for " + message.itemname + " with a winning bid of $" + message.bid);
										if (username.equals(message.username) && prizeViewing) {
											updateSearch(message);
										}
									}
									else {
										showConfirmation("Item Sold!");
									}
								}
							}
							
							else if (message.type == messageType.REMOVEITEM) {
								if (message.isUpdateThread) {
									updateItem(message, message.itemname + " has been removed from the auction");
								}
								else {
									showConfirmation("Item removed");
								}
							}
							
							else if (message.type == messageType.GETITEM && message.success) {
								nameLabel.setText(message.itemname);
								descriptionLabel.setText(message.description);
								ownerLabel.setText(message.owner);
								minpriceLabel.setText(Double.toString(message.price));
								highestBidderLabel.setText(message.username);
								if (message.bid == 0) {
									highestBidLabel.setText("N/A");
								}
								else {
									highestBidLabel.setText(Double.toString(message.bid));
								}
								if (message.buyItNow != 0) {
									buyItNowLabel.setText(Double.toString(message.buyItNow));
								}
								else {
									buyItNowLabel.setText("N/A");
								}
								buyerLabel.setText(message.soldTo);
								if (message.image == null) {
									itemImage.setVisible(false);
								}
								else {
									itemImage.setVisible(true);
									itemImage.setImage(new Image(new ByteArrayInputStream(message.image)));
								}
								if (!message.isavailable || username.equals("GUEST")) {
									bidLabel.setVisible(false);
									bidTxt.setVisible(false);
									bidBtn.setVisible(false);
									acceptBtn.setVisible(false);
									removeItemBtn.setVisible(false);
								}
								else if (message.owner.equals(username)) {
									
									if (message.bid == 0) {
										acceptBtn.setVisible(false);
									}
									else {
										acceptBtn.setVisible(true);
									}
									removeItemBtn.setVisible(true);
									bidLabel.setVisible(false);
									bidTxt.setVisible(false);
									bidBtn.setVisible(false);
								}
								else {
									bidLabel.setVisible(true);
									bidTxt.setVisible(true);
									bidBtn.setVisible(true);
									acceptBtn.setVisible(false);
									removeItemBtn.setVisible(false);
								}
								
								if (message.isavailable) {
									statusLabel.setText("OPEN");
									statusLabel.setTextFill(Color.GREEN);
								}
								else {
									statusLabel.setText("CLOSED");
									statusLabel.setTextFill(Color.RED);
								}
							}
						});
					}
				} 
				catch (IOException e) {}
			}
		}).start();
	}
	private void setUpStage(String fileName) {
		primaryStage.close();
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("./resources/" + fileName));
		loader.setController(this);
		try {
			primaryStage.setScene(new Scene(loader.load()));
		} catch (IOException e) {}
		if (fileName.equals("Home.fxml")) {
			HomeBackground.setImage(new Image(getClass().getResource("./resources/Home.png").toExternalForm()));
			prizeViewing = false;
			if (username.equals("GUEST")) {
		    	addItemLink.setVisible(false);
		    	InventoryLink.setVisible(false);
		    	placeBidLink.setVisible(false);
		    	viewItemsLink.setVisible(true);
		    	bankBalanceLabel.setVisible(false);
		    	
			}
	    	else {
	    		toServer.println(gson.toJson(new Message(username, messageType.GETMONEY)));
	    		toServer.flush();
	    	}
		}
		if (backToHomeBtn != null) {
			backToHomeBtn.setImage(new Image(getClass().getResource("./resources/backBtn.png").toExternalForm()));
		}
		if (backBtn != null) {
			backBtn.setImage(new Image(getClass().getResource("./resources/backBtn.png").toExternalForm()));
		}
		if (searchImage != null) {
			searchImage.setImage(new Image(getClass().getResource("./resources/searchIcon.png").toExternalForm()));
		}
		primaryStage.show();
	}
	private void showError(String prompt) {
		new MediaPlayer(new Media(getClass().getResource("./resources/Error.mp3").toExternalForm())).play();
		Alert alert = new Alert(AlertType.ERROR, prompt, ButtonType.OK);
		alert.showAndWait();
	}
	private void showConfirmation(String prompt) {
		new MediaPlayer(new Media(getClass().getResource("./resources/notificationSound.mp3").toExternalForm())).play();
		Alert alert = new Alert(AlertType.CONFIRMATION, prompt, ButtonType.OK);
		alert.showAndWait();
	}
	private boolean containsDigit(String input) {
		for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
		return false;
	}
	private boolean containsSymbol(String input) {
		for (char c : input.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
		return false;
	}
	private boolean containsLetter(String input) {
		for (char c : input.toCharArray()) {
            if (Character.isAlphabetic(c)) {
                return true;
            }
        }
		return false;
	}
	private void updateItem(Message message, String prompt) {
		history += prompt + ";\n";
		if (historyTxt != null) {
			historyTxt.setText(history);
		}
		if (message.type != messageType.ADDITEM && nameLabel != null && nameLabel.getText().equals(message.itemname)) {
			getItemInfo(message.itemname);
		}
		if (message.type == messageType.ACCEPTBID && bankBalanceLabel != null) {
			if (username.equals(message.owner) || username.equals(message.username)) {
				toServer.println(gson.toJson(new Message(username, messageType.GETMONEY)));
	    		toServer.flush();
			}
		}
	}
	private void updateSearch(Message message) {
		if (prizeViewing) {
			prizenames.add(message.itemname);
		}
		else {
			itemnames.add(message.itemname);
		}
		try {
			if (message.itemname.contains(searchBar.getText())) {
				dropDownList.getItems().add(message.itemname);
			}
		}
		catch (Exception e){}
	}
	
}
