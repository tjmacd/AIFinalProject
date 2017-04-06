import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
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
	protected List<Customer> customers;
	
	public TwitterAgent(){
		
		customers = new ArrayList<Customer>();
		twitter = getTwitterInstance();
		
	}
	
	/* 
	 * Initializes the ConfigurationBuilder with our API key and access credentials,
	 * as well as sets up a new TwitterFactory with those credentials, which is what
	 * we will be using to get all of our Twitter Data out of the queue.
	 */
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
	
	/*
	 * A function to build the files we need to teach our Neural Network. Generates two
	 * files, one which is a list of tweets made by customers and another of tweets made
	 * by non-customers. Customers are determined by if they contain a list of set words
	 * in their tweets. It then writes a file for each group, and a total file which is 
	 * a file containing all of the tweets. This function is to be used twice, once to
	 * grab your first Training data set and then a second time to grab a Test data set.
	 */
	public void buildFiles() throws TwitterException{
		//Set up the query for people @ing Microsoft
		Query query = new Query("@Microsoft");

        // Send API request to execute a search with the given query.
        QueryResult result = twitter.search(query);

        // Display search results.
        List<Customer> possibleCustomers = new ArrayList<Customer>();
        for (Status status : result.getTweets()) {
        	
        	String key = status.getUser().getScreenName();
        	
        	// add new customer if we haven't seen him/her already
			if (!possibleCustomers.contains(key)) {
				
				System.out.println("Processing timeline for user: "+key);
				
				List<Status> tweets = new ArrayList<>();
				
				for(int i = 1; i <= PAGE_MAX; i++){
					Paging page = new Paging(i, PAGE_SIZE);
					List<Status> timelineResults = twitter.getUserTimeline(key, page);
					
					if(timelineResults.size() > 0){
						tweets.addAll(timelineResults);
					}
				}
				
				Customer newPossibleCust = new Customer(key, tweets);
				
				possibleCustomers.add(newPossibleCust);
        	}   	
        }
        
        this.customers.addAll(possibleCustomers);
        
        //Set up two lists of customer and non-customer tweets
        List<Status> customerTweets = new ArrayList<Status>();
        List<Status> nonCustomerTweets = new ArrayList<Status>();
        
        //Loop through and add all customer tweets to customer list, and non-customer
        //tweets to non-customer list.
        for(int i = 0; i < customers.size(); i++){
    		for(int j = 0; j < customers.get(i).tweets.size(); j++){
    			boolean found = false;
    			TextProcessor tp = new TextProcessor();
    			HashMap<String, Integer> filter = tp.getWordOccurences(customers.get(i).tweets.get(j).getText());
    			List<String> filterWords = makeFilter();
    			for(int x = 0; x < filterWords.size(); x++){
    				if(filter.containsKey(filterWords.get(x))){
    					found = true;
    				}
    			}
    			if(found){
					customerTweets.add(customers.get(i).tweets.get(j));
				} else {
					nonCustomerTweets.add(customers.get(i).tweets.get(j));
				}
    		}
        }
        //Write the two files based on our two lists, as well as write a final file
        //for all of the tweets.
        try{
            PrintWriter writer0 = new PrintWriter("0.txt", "UTF-8");
            PrintWriter writer1 = new PrintWriter("1.txt", "UTF-8");
            PrintWriter writer = new PrintWriter("raw.txt", "UTF-8");
            for(int i = 0; i < customerTweets.size(); i++){
            	String filteredString = customerTweets.get(i).getText().replaceAll("\n", "");
            	filteredString = filteredString.replaceAll("RT [.]+:", "");
            	writer0.println(filteredString);
            	writer.println(filteredString);
            }
            writer0.close();
            for(int i = 0; i < nonCustomerTweets.size(); i++){
            	String filteredString = nonCustomerTweets.get(i).getText().replaceAll("\n", "");
            	filteredString = filteredString.replaceAll("RT [.]+:", "");
            	writer1.println(filteredString);
            	writer.println(filteredString);
            }
            writer1.close();
            writer.close();
        } catch (IOException e) {
           System.out.println(e);
        }
	}
	
	/*
	 * Defines the words we are filtering for. This determine what words mark 
	 * Microsoft Customers as being customers.
	 */
	public List<String> makeFilter(){
		List<String> filterList = new ArrayList<String>();
		filterList.add("visual");
		filterList.add("windows");
		filterList.add("direct3D");
		filterList.add("kinect");
		filterList.add("bing");
		filterList.add("silverlight");
		filterList.add("MSN");
		filterList.add("skype");
		filterList.add("access");
		filterList.add("excel");
		filterList.add("oneNote");
		filterList.add("outlook");
		filterList.add("powerPoint");
		filterList.add("publisher");
		filterList.add("visio");
		filterList.add("365");
		filterList.add("office");
		filterList.add("explorer");
		filterList.add("directX");
		filterList.add("edge");
		filterList.add("notepad");
		filterList.add("defender");
		filterList.add("paint");
		return filterList;
		
	}
	
	public static void main(String[] args) throws TwitterException{
		TwitterAgent ta = new TwitterAgent();
		ta.buildFiles();
		System.out.println("File Generation Complete");
	}
}
