import java.util.List;

import twitter4j.Status;

public class Customer {
	
	String username;
	List<Status> tweets;
	
	public Customer(String username, List<Status> tweets){
		this.username = username;
		this.tweets = tweets;
	}
	
	public List<Status> getTweets(){
		return this.tweets;
	}

	public String getUsername() {
		return this.username;
	}
	
}
