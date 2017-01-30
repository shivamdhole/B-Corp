import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/*Author: Shivam Dhole
  Student No: 741507
*/

/*
This code takes a twitter username from the file and finds its followers using
Twitter's REST API. The program collects 15 followers/ minute. After collecting
15 followers program sleeps for 15 mins and it goes on till all followers are collected.
*/

public class VMfollowers {

	public static void main(String[] args) throws InterruptedException, IOException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("xxxxxxx");
		cb.setOAuthConsumerSecret("xxxxxxx");
		cb.setOAuthAccessToken("xxx-xxxxxxx");
		cb.setOAuthAccessTokenSecret("xxxxxxx");
		
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		// *********** code for getting usernames ****************
		File namefile = new File("bcorpuserfoll.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(namefile), "UTF8"));
		String name;
		while ((name = br.readLine()) != null) {
			String username = name;
			try {
				long cursor = -1;
				IDs ids;
				int count = 15;
				do {
					ids = twitter.getFollowersIDs(username, cursor, count);
					for (long id : ids.getIDs()) {
						User user = twitter.showUser(id);
						System.out.println(id + "," + user.getName() + "," + user.getScreenName() + "," + username);
					}
					Thread.sleep(900000);// program sleeps for 15 mins

				} while ((cursor = ids.getNextCursor()) != 0);

				System.exit(0);
			} catch (TwitterException te) {
				te.printStackTrace();
				System.out.println("Failed to get followers' ids: " + te.getMessage());
				System.exit(-1);
			}
		}

		br.close();
		// *********** usernames printed in console ******************

	}// end
		// main

}// end class
