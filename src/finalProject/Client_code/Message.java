/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* <Tony Tian>
* <jt47232>
* <17610>
* Spring 2023
*/

package ClientPackage;
import java.util.ArrayList;

public class Message {
	public String username;
	public String passwd;
	public String itemname;
	public String owner;
	public String description;
	public double bid;
	public double price;
	public double buyItNow;
	public double money;
	public String soldTo;
	public int duration;
	public byte[] image;
	public ArrayList<String> itemnames;
	public boolean success;
	public boolean isavailable;
	public boolean isUpdateThread;
	public enum messageType {
		BID, LOGIN, NEWUSER, ADDITEM, GETDATA, GETITEM, REMOVEITEM, ACCEPTBID, GETPRIZES, GETMONEY
	};
	public messageType type;
	public Message(String username, String passwd, messageType type) {
		this.username = username;
		this.passwd = passwd;
		this.type = type;
	}
	public Message(String itemname, String username, double bid) {
		this.itemname = itemname;
		this.bid = bid;
		this.username = username;
		type = messageType.BID;
	}
	public Message(String description, double price, byte[] image, int duration, String username, String itemname, double buyItNow) {
		this.description = description;
		this.price = price;
		this.duration = duration;
		this.image = image;
		type = messageType.ADDITEM;
		this.username = username;
		this.itemname = itemname;
		this.buyItNow = buyItNow;
		
	}
	public Message() {
		type = messageType.GETDATA;
	}
	public Message(String name, messageType type) {
		this.type = type;
		if (type == messageType.GETMONEY) {
			username = name;
		}
		else {
			itemname = name;
		}
		
	}
	public Message(String username, String itemname) {
		this.username = username;
		this.itemname = itemname;
		type = messageType.GETITEM;
	}
	public Message(String username) {
		this.username = username;
		this.type = messageType.GETPRIZES;
	}
}
