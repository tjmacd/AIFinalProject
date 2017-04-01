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
	
	public static final int PAGE_MAX = 5;
	public static final int PAGE_SIZE = 30;
	
	protected Twitter twitter;
	protected HashMap<String, Customer> customers;
	
	public TwitterAgent(){
		
		customers = new HashMap<String, Customer>();
		twitter = getTwitterInstance();
		
	}
	
	public Twitter getTwitterInstance(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("HNRjCyacLDdlZhyplxj2NWYAa")
		  .setOAuthConsumerSecret("oozNoCph3vWJ8bXdvJEMKSCuBPGBDjVPdW2nisZ9L6HcKTN3GP")
		  .setOAuthAccessToken("839143326020419584-0KiuS4zCJwnw8WgieHLHob73d08GnzF")
		  .setOAuthAccessTokenSecret("opQ1EtQ2mznMchWSuXg8VRtJoaVWpSQNI5sOK8eV4uJHm");
	    TwitterFactory tf = new TwitterFactory(cb.build());
	    Twitter twitter = tf.getInstance();
	    
	    return twitter;
	}
	
	public boolean isCustomer(Customer possibleCustomer){
		boolean result = false;
		
		if(!customers.containsKey(possibleCustomer.getUsername())){
			return result;
		}
		
		// get decision based on ifProductFrequency
		boolean criteria1 = ifProductFrequency(possibleCustomer);
		
		// here we can add other functions to test user for customership then we can return
		// logical "OR" of these results meaning that if they fulfill one of the functions they are a customer
		// if they fulfill none they are not a customer. We might not even need more functions...
		
		result = criteria1;
		
		return result;
		
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
        	
        	String key = status.getUser().getScreenName();
        	
        	// add new customer if we haven't seen him/her already
			if (!possibleCustomers.containsKey(key)) {
				
				App.log("Processing timeline for user: "+key);
				
				List<Status> tweets = new ArrayList<>();
				
				for(int i = 1; i <= PAGE_MAX; i++){
					Paging page = new Paging(i, PAGE_SIZE);
					List<Status> timelineResults = twitter.getUserTimeline(key, page);
					
					if(timelineResults.size() > 0){
						tweets.addAll(timelineResults);
					}
				}
				
				Customer newPossibleCust = new Customer(key, tweets);
				
				possibleCustomers.put(key, newPossibleCust);
        	}
        	
        }
        
        this.customers.putAll(possibleCustomers);
	}
	
	public Set<String> getUsers(){
		return customers.keySet();
	}
	
	public Customer getUser(String key){
		return customers.get(key);
	}
	
	public static void main(String[] args){
		
		TwitterAgent ta = new TwitterAgent();
		
		try {
			ta.createTrainingData();
			System.out.println(ta.getUsers());
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
