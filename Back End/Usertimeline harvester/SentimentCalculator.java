import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.ViewResults;
import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

/*Author: Shivam Dhole
  Student No: 741507
*/

/*
This is the code for Harvesting followers and company timeline tweets using Twitter Streaming API.
The list of followers is added to an arraylist. These are follower's twitter username. 
The streaming API takes a username and returns maximum 3200 recent tweets from its timeline.
The tweets are captured and stored in CouchDB.
Changing the followers list to Company username list will get tweets from compay's timeline.
Ling Pipe Sentiment analysis runs on the tweets first and then it is saved to CouchDB.
*/


public class SentimentCalculator {

	private static Database db;
// when see this just change the files names
	public static void main(String[] args) throws IOException {
		SentimentClassifier sentClassifier = new SentimentClassifier();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("xxxxxxxxxx");
		cb.setOAuthConsumerSecret("xxxxxxxxxx");
		cb.setOAuthAccessToken("xxxxxx-xxxxxxxxxx");
		cb.setOAuthAccessTokenSecret("xxxxxxxxxx");
		// ************ Code for db connection **********************
		Session dbSession = new Session("xxx.xxx.xxx.xxx", 5984);
		String dbname = "newtweets";
		db = dbSession.getDatabase(dbname);
		// *********** db code ends *******************************
		// *********** code for getting usernames ****************
		File namefile = new File("bcorpusernames.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(namefile), "UTF8"));
		String name;
		ArrayList<String> list = new ArrayList<String>();
		while ((name = br.readLine()) != null) {
			list.add(name);
		}
		br.close();

		// *********** usernames saved in array ******************
		// ************userid and company Hashmap****************
		File follandcomp = new File("followersandcompany.txt");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(follandcomp), "UTF8"));
		String name1;
		HashMap<Long, ArrayList<String>> companylist = new HashMap<>();
		while ((name1 = br1.readLine()) != null) {
			String[] split1 = name1.split("\t");
			String[] company = split1[1].split(",");
			ArrayList<String> values = new ArrayList<String>();
			for (String s : company) {
				values.add(s);
			}
			companylist.put(Long.parseLong(split1[0]), values);

		}
		br1.close();
		// ************ Hash map created ******************
		// **************** getting 3200 tweets start ************************
		System.out.println(companylist.size());
		Twitter twitterStream = new TwitterFactory(cb.build()).getInstance();
		for (String str: list) {
			System.out.println(str);
			for (int pageno = 1; pageno < 17; pageno++) {
				List statuses = null;
				Paging page = new Paging(pageno, 200);// page number, tweets per
														// page
				try {
					statuses = twitterStream.getUserTimeline(str, page);
				} catch (TwitterException e) {
				}
				if (statuses != null) {
					for (int i = 0; i < statuses.size(); i++) {
						Status status = (Status) statuses.get(i);
						User user = status.getUser();
						long id = user.getId();
						String username = status.getUser().getScreenName();
						String language = status.getLang();
						String country = null, placename = null, placetype = null;
						if (status.getPlace() != null) {
							country = status.getPlace().getCountry();// eg
																		// australia
							placename = status.getPlace().getName();// eg sydney
							placetype = status.getPlace().getPlaceType();// eg
																			// city
						}
						String[] withheld = status.getWithheldInCountries();
						long tweetid = status.getId();
						String text = status.getText();
						Date createdat = status.getCreatedAt();
						long[] contributors = status.getContributors();
						boolean isretweeted = status.isRetweeted();
						UserMentionEntity[] userment = status.getUserMentionEntities();
						String lingsenti = sentClassifier.classify(text);
						Boolean rel = null;
						for (UserMentionEntity u : userment) {
							if (list.contains(u.toString())) {
								rel = true;
								break;
							} else {
								rel = false;
							}
						}
						// storing tweets
						// ************************** Saving to database
						// ***************

						Document doc = new Document();
						doc.setId(String.valueOf(tweetid));
						doc.put("username", username);
						doc.put("country", country);
						doc.put("placename", placename);
						doc.put("placetype", placetype);
						doc.put("language", language);
						doc.put("lingsentiment", lingsenti);
						doc.put("company", companylist.get(id));
						doc.put("relevant", rel);
						doc.put("text", text);
						doc.put("withheld", withheld);
						doc.put("createdat", createdat);
						doc.put("contributors", contributors);
						doc.put("usermentions", userment);
						doc.put("isretweeted", isretweeted);
						Boolean b = false;
						doc.put("python", b);
						try{
							db.saveDocument(doc);
						}
						catch(Exception e){
							//System.out.println(e);
						}
						
						count++;

					}
				} // end of if statuses not eq null
			}// end of page loop
		} // end of list for loop
	}// end main

}// end class
