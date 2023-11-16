package finalProject;

public class Item {
	String description;
	double minPrice;
	double highest_bid = 0;
	int time_left = 20;
	boolean closed = false;
	public Item(String description, double minPrice) {
		this.description = description;
		this.minPrice = minPrice;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (time_left > 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					time_left--;
				}
				closed = true;
			}
		}).start();
	}
	
}
