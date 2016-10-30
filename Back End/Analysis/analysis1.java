import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lightcouch.CouchDbClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/*Author: Shivam Dhole
  Student No: 741507
*/

/*
This code is part of analysis. The sentiment value of each tweet is collected.
The tweets are collected companywise. The average sentiment is calculted and sentiment before and
after certification date is calcuated. The location of the tweets is also calculated and stored as national
or international tweets with respect to host country of Company.
The output is stored in two location. One in cloud and one in local.
*/

public class analysis1 {
	public static void main(String[] args) throws IOException, ParseException {
		
		// ************ Reading all bcorp names **********************
		File namefile = new File("bcorpusernamesforviews.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(namefile), "UTF8"));
		String name;
		ArrayList<String> list = new ArrayList<String>();
		while ((name = br.readLine()) != null) {
			list.add(name);
		}
		br.close();
		// ************** Reading bcorps names done *****************
		// ************** Getting dates of certification ***********
		File datefile = new File("bcorp.csv");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(datefile), "UTF8"));
		br1.readLine();
		String line;
		HashMap<String, String> countrylist = new HashMap<String, String>();
		HashMap<String, String> datelist = new HashMap<String, String>();
		HashMap<String, String> companylist = new HashMap<String, String>();
		while ((line = br1.readLine()) != null) {
			String[] split = line.split(",");
			datelist.put(split[1], split[3]);
			countrylist.put(split[1], split[4]);
			companylist.put(split[1], split[0]);
		}
		br1.close();
		// ************** Dates in memory ***************************
		// ********** Connecting to database ***************************
		CouchDbClient dbClient = new CouchDbClient("newtweets", true, "http", "xxx.xxx.xxx.xxx", 5984, null, null);
		CouchDbClient dbremote = new CouchDbClient("analysis", true, "http", "xxx.xxx.xxx.xxx", 5984, null, null);
		CouchDbClient dblocal = new CouchDbClient("analysis", true, "http", "localhost", 5984, null, null);
		// ********** Connected to database and view initialized *******
		String viewname = "shivam/";
		for (String l : list) {
			String view = viewname.concat(l);
			List<JsonObject> allDocs = dbClient.view(view).includeDocs(true).query(JsonObject.class);
			System.out.println(l + " > " + allDocs.size());
			//**************** Variable area ***********************
			double totaltweets = allDocs.size(),after =0,before =0,national = 0,location = 0;
			double lpos = 0, lneg = 0, lneu = 0, nltkpos = 0, nltkneg = 0;
			double tb = 0.0;
			
			double blpos = 0, blneg = 0, blneu = 0, bnltkpos = 0, bnltkneg = 0;
			double btb = 0.0;
			
			double alpos = 0, alneg = 0, alneu = 0, anltkpos = 0, anltkneg = 0;
			double atb = 0.0;
			//**************** Variable area ends *****************
			for (JsonObject j : allDocs) {
				if(j.has("country")){
					location++;
					String country = j.get("country").getAsString();
					if(country.equals(countrylist.get(l))){
						national++;
					}
				}
				String n = j.get("NLTKsentiment").getAsString();
				String li = j.get("lingsentiment").getAsString();
				if (li.equals("pos")) {
					lpos++;
				}
				if (li.equals("neg")) {
					lneg++;
				}
				if (li.equals("neu")) {
					lneu++;
				}
				if (n.equals("pos")) {
					nltkpos++;
				}
				if (n.equals("neg")) {
					nltkneg++;
				}
				double d = j.get("textblobpolarity").getAsDouble();
				tb = tb + d;
				// *********** 3 sentiment test case end ****************
				//*********** Date wise sentiment start *****************
				JsonObject createdat = j.get("createdat").getAsJsonObject();
				JsonElement time = createdat.get("time");
				
				DateFormat format = new SimpleDateFormat("MMddyyHHmmss");//MMddyyHHmmss
				Date date = format.parse(time.toString());
				DateFormat in = new SimpleDateFormat("dd/MM/yyyy");
				Date certidate = in.parse(datelist.get(l));
				
				if(date.before(certidate)){
					String bn = j.get("NLTKsentiment").getAsString();
					String bl = j.get("lingsentiment").getAsString();
					before++;
					if (bl.equals("pos")) {
						blpos++;
					}
					if (bl.equals("neg")) {
						blneg++;
					}
					if (bl.equals("neu")) {
						blneu++;
					}
					if (bn.equals("pos")) {
						bnltkpos++;
					}
					if (bn.equals("neg")) {
						bnltkneg++;
					}
					btb = btb + d;
				}
				if(date.after(certidate) || date.equals(certidate)){
					String an = j.get("NLTKsentiment").getAsString();
					String al = j.get("lingsentiment").getAsString();
					after++;
					if (al.equals("pos")) {
						alpos++;
					}
					if (al.equals("neg")) {
						alneg++;
					}
					if (al.equals("neu")) {
						alneu++;
					}
					if (an.equals("pos")) {
						anltkpos++;
					}
					if (an.equals("neg")) {
						anltkneg++;
					}
					atb = atb + d;
				}
				//*********** Date wise sentiment end *******************
			} // end of alldocs for loop
			//**************** Calculate and store to db **********************
			Map<String, Object> output = new HashMap<String,Object>();
			output.put("_id", l);
			output.put("username", l);
			output.put("company", companylist.get(l));
			output.put("NLTKpos",percentage(nltkpos,totaltweets) );
			output.put("NLTKposBefore",percentage(bnltkpos,before) );
			output.put("NLTKposAfter",percentage(anltkpos,after) );
			output.put("NLTKneg",percentage(nltkneg,totaltweets) );
			output.put("NLTKnegBefore", percentage(bnltkneg,before));
			output.put("NLTKnegAfter", percentage(anltkneg,after));
			output.put("Lingpos", percentage(lpos,totaltweets));
			output.put("LingposBefore", percentage(blpos,before));
			output.put("LingposAfter", percentage(alpos,after));
			output.put("Lingneg", percentage(lneg,totaltweets));
			output.put("LingnegBefore", percentage(blneg,before));
			output.put("LingnegAfter", percentage(alneg,after));
			output.put("Lingneu", percentage(lneu,totaltweets));
			output.put("LingneuBefore", percentage(blneu,before));
			output.put("LingneuAfter", percentage(alneu,after));
			output.put("Textblob", average(tb,totaltweets));
			output.put("TextblobBefore", average(btb,before));
			output.put("TextblobAfter", average(atb,after));
			output.put("national", percentage(national, location));
			dbremote.save(output);
			dblocal.save(output);
			//**************** storing ends ***********************************
			//break;// coment this later
		} // end of list for loop
		//System.out.println(temp);
	}// end of main
	
	public static float percentage(double no,double total){
		if(total == 0 || no == 0){
			return 0;
		}
		float ans = (float)((no*100)/total);
		return ans;
		
	}
	public static float average(double no, double total){
		if(total == 0){
			return 0;
		}
		float avg = (float)(no/total);
		return avg;
	}
}// end of class
