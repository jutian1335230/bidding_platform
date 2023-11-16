package finalProject;

public class Item {
	String description;
	double minPrice;
	double highest_bid = 0;
	boolean sold = false;
	public Item(String description, double minPrice) {
		this.description = description;
		this.minPrice = minPrice;
	}
	
}
