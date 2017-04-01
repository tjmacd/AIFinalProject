import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitter API Example. Demonstrates interaction with the Twitter API using the
 * twitter4j library.
 */
public class App {

	public static final boolean DEBUG = true;
	
	public static void log(String msg){
		if(DEBUG){
			System.out.println("DEBUG: "+msg);
		}
	}
	
  /**
   * Main method.
   *
   * @param args
   * @throws TwitterException
   */
  public static void main(String[] args) throws TwitterException {

    // The TwitterFactory object provides an instance of a Twitter object
    // via the getInstance() method. The Twitter object is the API consumer.
    // It has the methods for interacting with the Twitter API.
	ConfigurationBuilder cb = new ConfigurationBuilder();
	cb.setDebugEnabled(true)
	  .setOAuthConsumerKey("HNRjCyacLDdlZhyplxj2NWYAa")
	  .setOAuthConsumerSecret("oozNoCph3vWJ8bXdvJEMKSCuBPGBDjVPdW2nisZ9L6HcKTN3GP")
	  .setOAuthAccessToken("839143326020419584-0KiuS4zCJwnw8WgieHLHob73d08GnzF")
	  .setOAuthAccessTokenSecret("opQ1EtQ2mznMchWSuXg8VRtJoaVWpSQNI5sOK8eV4uJHm");
    TwitterFactory tf = new TwitterFactory(cb.build());
    Twitter twitter = tf.getInstance();

    boolean keepItGoinFullSteam = true;
    do {
      // Main menu
      Scanner input = new Scanner(System.in);
      System.out.print("\n--------------------"
          + "\nH. Home Timeline\nS. Search\nU. User Search"
          + "\n--------------------"
          + "\nA. Get Access Token\nQ. Quit"
          + "\n--------------------\n> ");
      String choice = input.nextLine();

      try {
        
        // Home Timeline
        if (choice.equalsIgnoreCase("H")) {

          // Display the user's screen name.
          User user = twitter.verifyCredentials();
          System.out.println("\n@" + user.getScreenName() + "'s timeline:");

          // Display recent tweets from the Home Timeline.
          for (Status status : twitter.getHomeTimeline()) {
            System.out.println("\n@" + status.getUser().getScreenName()
                + ": " + status.getText());
          }

        }
        
        // Search
        else if (choice.equalsIgnoreCase("S")) {

          // Ask the user for a search string.
          System.out.print("\nSearch: ");
          String searchStr = input.nextLine();

          // Create a Query object.
          Query query = new Query(searchStr);

          // Send API request to execute a search with the given query.
          QueryResult result = twitter.search(query);

          // Display search results.
          for (Status status : result.getTweets()) {
            System.out.println("\n@" + status.getUser().getName() + ": "
                + status.getText());
          }

        }
        
        else if (choice.equalsIgnoreCase("U")) {
        	
        	// Ask the user for a search string.
            System.out.print("\nSearch for user: ");
            String searchStr = input.nextLine();
            
            List<Status> tweets = new ArrayList();
            int pageNumber = 1;
            while(true){
            	try {
    	        	int size = tweets.size(); 
    	        	Paging page = new Paging(pageNumber++, 100);
    	        	tweets.addAll(twitter.getUserTimeline(searchStr, page));
    	        	if (tweets.size() == size)
    	        		break;
            	} catch(TwitterException e) {
            		e.printStackTrace();
            	}
            }
        	Paging numOfTweets = new Paging(1,1);
        	List<Status> posts = twitter.getUserTimeline(searchStr, numOfTweets);
        	System.out.println("\nHere are all the posts by @" 
        	+ posts.get(0).getUser().getName() + ":");
        	for (Status status : tweets) {
                System.out.println(status.getText());
            }
        }
        
        // Get Access Token
        else if (choice.equalsIgnoreCase("A")) {

          // First, we ask Twitter for a request token.
          RequestToken reqToken = twitter.getOAuthRequestToken();
          System.out.println("\nRequest token: " + reqToken.getToken()
              + "\nRequest token secret: " + reqToken.getTokenSecret());

          AccessToken accessToken = null;
          while (accessToken == null) {

            // The authorization URL sends the request token to Twitter in order
            // to request an access token. At this point, Twitter asks the user
            // to authorize the request. If the user authorizes, then Twitter
            // provides a PIN.
            System.out.print("\nOpen this URL in a browser: "
                + "\n    " + reqToken.getAuthorizationURL() + "\n"
                + "\nAuthorize the app, then enter the PIN here: ");
            String pin = input.nextLine();
            try {
              // We use the provided PIN to get the access token. The access
              // token allows this app to access the user's account without
              // knowing his/her password.
              accessToken = twitter.getOAuthAccessToken(reqToken, pin);
            } catch (TwitterException te) {
              System.out.println(te.getMessage());
            }
          }
          System.out.println("\nAccess token: " + accessToken.getToken()
              + "\nAccess token secret: " + accessToken.getTokenSecret()
              + "\nSuccess!");

        }
        
        // Quit
        else if (choice.equalsIgnoreCase("Q")) {

          keepItGoinFullSteam = false;


        }
        
        // Bad choice
        else {

          System.out.println("Invalid option.");
        }

      } catch (IllegalStateException ise) {
        System.out.println(ise.getMessage());
      }

    } while (keepItGoinFullSteam == true);
  }
}