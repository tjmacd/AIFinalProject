import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterAgent {
	
	Twitter twitter;
	private HashMap<String, Customer> users;
	
	public TwitterAgent(){
		
		users = new HashMap<String, Customer>();
		
	}
	
	public boolean isCustomer(Customer user){
		
		if(!users.containsKey(user.getUsername())){
			return false;
		}
		
		boolean result = false;
		
		// get decision based on ifProductFrequency
		boolean criteria1 = ifProductFrequency(user);
		
		// here we can add other functions to test user for customership then we can return
		// logical "OR" of these results meaning that if they fulfill one of the functions they are a customer
		// if they fulfill none they are not a customer. We might not even need more functions...
		
		return criteria1;
		
	}
	
	// Functions that determine if someone is a customer or not
	public boolean ifProductFrequency(Customer user){
		double threshold = 0.2;
		boolean result = false;
		
		// Get user tweets and scan if matches threshold then return true
		for(Status tweet: user.getTweets()){
			// add logic that will use TextProcessor
		}
		
		return result;
	}
	
	public void createTrainingData() throws TwitterException{
		// Create a Query object.
        Query query = new Query("@Microsoft");

        // Send API request to execute a search with the given query.
        QueryResult result = twitter.search(query);

        // Display search results.
        HashMap<String, Customer> possibleCustomers = new HashMap<String, Customer>();
        for (Status status : result.getTweets()) {
      	  //If the user is not in the hashmap, put it in there
      	  if(!possibleCustomers.containsKey(status.getUser().getScreenName())){
      		  List<Status> tweets = new ArrayList();
                int pageNumber = 1;
                while(true){
                	try {
        	        	int size = tweets.size(); 
        	        	Paging page = new Paging(pageNumber++, 100);
        	        	tweets.addAll(twitter.getUserTimeline(status.getUser().getScreenName(), page));
        	        	if (tweets.size() == size)
        	        		break;
                	} catch(TwitterException e) {
                		e.printStackTrace();
                	}
                }
      		  Customer newPossibleCust = new Customer(status.getUser().getScreenName(),tweets);
      	  }
        }
	}
	
	public Set<String> getUsers(){
		return users.keySet();
	}
	
	public Customer getUser(String key){
		return users.get(key);
	}
	
	public static void main(String[] args){
		
		TwitterAgent ta = new TwitterAgent();
		
		try {
			ta.createTrainingData();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
