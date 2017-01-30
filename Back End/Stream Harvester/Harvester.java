import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

/*Author: Shivam Dhole
  Student No: 741507
*/

/*
This is the code for Harvesting tweets using Twitter Streaming API.
The list of companies is added to an array. These are company's twitter username. 
The streaming API tracks these usernames. If any tweet related to the usernames is found, the tweet is captured and stored in CouchDB.
Ling Pipe Sentiment analysis runs on the tweets first and then it is saved to CouchDB.
*/

public class Harvester {

	private static Database db;

	public static void main(String[] args) throws IOException {
		final SentimentClassifier sentClassifier = new SentimentClassifier();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("xxxxxx");
		cb.setOAuthConsumerSecret("xxxxxx");
		cb.setOAuthAccessToken("xxxx-xxxxxxxx");
		cb.setOAuthAccessTokenSecret("xxxxxx");
		// ************ Code for db connection **********************
		Session dbSession = new Session("xxx.xxx.xxx.xxx", 5984);
		String dbname = "newtweets";
		db = dbSession.getDatabase(dbname);
		// *********** db code ends *******************************
		//*********** initializing writer **************
		final PrintWriter writer = new PrintWriter("newfollowers.txt", "UTF-8");
		// ************* Initializing writer ends ****************
		// *********** code for getting usernames ****************
		File namefile = new File("bcorpusernames.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(namefile), "UTF8"));
		String name;
		ArrayList<String> list = new ArrayList<String>();
		while ((name = br.readLine()) != null) {
			list.add(name);
		}
		String[] usernames = new String[list.size()];
		usernames = list.toArray(usernames);
		br.close();
		// *********** usernames saved in array ******************
		// ************userid and company Hashmap****************
		File follandcomp = new File("followersandcompany.txt");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(follandcomp), "UTF8"));
		String name1;
		final HashMap<Long, ArrayList<String>> companylist = new HashMap<Long, ArrayList<String>>();
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
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {
				// Geting tweets
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
				Boolean rel = true;
				if(companylist.containsKey(id) == false){
					writer.println(id);
				}
				// storing tweets
				// ************************** Saving to database ***************

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
				try {
					db.saveDocument(doc);
				} catch (Exception e) {
				}

			}

		};
		FilterQuery fq = new FilterQuery();
		fq.track(usernames);
		twitterStream.addListener(listener);
		twitterStream.filter(fq);
	}

}
