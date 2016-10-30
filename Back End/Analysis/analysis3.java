import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.lightcouch.CouchDbClient;
import com.google.gson.JsonObject;

/*Author: Shivam Dhole
  Student No: 741507
*/


/*This code is final part of analysis. The code calculates impact by distributing 
the companies for each sentiment analysis method into positive or negative components.
This code also creates list of companies who have major positive or negative impact of certification.
The result is printed in console. The further observations are recorded using this output.
*/

public class analysis3 {

	public static void main(String[] args) throws IOException {
		CouchDbClient dbClient = new CouchDbClient("analysis", true, "http", "localhost", 5984, null, null);

		List<JsonObject> allDocs = dbClient.view("_all_docs").includeDocs(true).query(JsonObject.class);
		System.out.println("all docs > " + allDocs.size());
		ArrayList<String> comp = new ArrayList<>();
		// Variables for TextBlob
		Map<String, Double> tbmajorpos = new HashMap<String, Double>();// major impact list
		Map<String, Double> tbmajorneg = new HashMap<String, Double>();// major impact list
		double tbb, tba;// store sentiment value of company
		int tbfalse = 0, tbtrue = 0, tbfalseten = 0, tbtrueten = 0;// counters
		
		// Variables for NLTK
		Map<String, Double> nltkmajorpos = new HashMap<String, Double>();// major impact list
		Map<String, Double> nltkmajorneg = new HashMap<String, Double>();// major impact list
		double nltkb,nltka;// store sentiment value of company
		int nfalse = 0, ntrue =0, nfalseten =0,ntrueten=0;// counters
		
		// Variables for Ling Pipe
		Map<String, Double> lingmajorpos = new HashMap<String, Double>();// major impact list
		Map<String, Double> lingmajorneg = new HashMap<String, Double>();// major impact list
		double lb,la,lnega,lnegb;// store sentiment value of company
		int lfalse=0,ltrue=0,lfalseten=0,ltrueten=0;// counters
		
		for (JsonObject j : allDocs) {// Getting all DOcs from Database

			// Analysis of Textblob starts
			tbb = j.get("TextblobBefore").getAsDouble();
			tba = j.get("TextblobAfter").getAsDouble();
			if (tbb > tba) { // overall impact negative
				tbfalse++;
			}else {// overall impact positive 
				tbtrue++;
			}
			if ((tbb) > tba) {// 10% or 5% threshold impact
				if (tbb - tba > 0.1) {
					tbfalseten++;
				} else {
					tbtrueten++;
				}
			} else {
				tbtrueten++;
			}
			if (tbb > tba) { // major negative
				tbmajorneg.put(j.get("company").getAsString(), Math.abs(tbb - tba));
				
			}
			if (tbb < tba) {// major positive
				tbmajorpos.put(j.get("company").getAsString(), Math.abs(tba - tbb));
				
			}
			// Analysis of textblob ends
			//**************************************************************
			// Analysis of NLTK Starts
			nltkb = j.get("NLTKposBefore").getAsDouble();
			nltka = j.get("NLTKposAfter").getAsDouble();
			if (nltkb > nltka) { // overall impact 
				nfalse++;
			}else{
				ntrue++;
			}
			if ((nltkb) > nltka) {// 10% or 5% threshold impact
				if ((nltkb - nltka) > 5) {
					nfalseten++;
				} else {
					ntrueten++;
				}
			} else {
				ntrueten++;
			}
			if (nltkb > nltka){// major negative
				nltkmajorneg.put(j.get("company").getAsString(), nltkb - nltka);		
			}
			if (nltkb < nltka) {// major positive
				nltkmajorpos.put(j.get("company").getAsString(), nltka - nltkb);	
			}
			// Analysis of NLTK ends here
			//*****************************************************************
			// Analysis of Ling starts here
			lb = j.get("LingposBefore").getAsDouble();
			la = j.get("LingposAfter").getAsDouble();
			lnegb = j.get("LingnegBefore").getAsDouble();
			lnega = j.get("LingnegAfter").getAsDouble();
			if (lb > la) { // overall impact 
				lfalse++;
			}else{
				ltrue++;
			}
			if ((lb) > la) {// 10% or 5% threshold impact
				if ((lb - la) > 5) {
					lfalseten++;
				} else {
					ltrueten++;
				}
			} else {
				ltrueten++;
			}
			if (lb > la && lnegb < lnega){// major negative
				lingmajorneg.put(j.get("company").getAsString(), lnega - lnegb);		
			}
			if (lb < la && lnegb > lnega) {// major positive
				lingmajorpos.put(j.get("company").getAsString(), la - lb);	
			}
		} // end of document for loop
		
		// Output of Textblob Start
		System.out.println("Textblob True>" + tbtrue + " False>" + tbfalse);
		System.out.println("Textblob 5%   True>" + tbtrueten + " False>" + tbfalseten);
		System.out.println("Positive "+tbmajorpos.size());
		for (Entry<String, Double> entry1 : tbmajorpos.entrySet()) {
			System.out.println("\t" +entry1.getKey() + "\t" + entry1.getValue());
		}
		System.out.println("Negative "+tbmajorneg.size());
		for (Entry<String, Double> entry1 : tbmajorneg.entrySet()) {
			System.out.println("\t" +entry1.getKey() + "\t" + entry1.getValue());
		}
		System.out.println("Total"+ (tbmajorpos.size()+tbmajorneg.size()));
		// Output of Textblob ends
		
		// Output for NLTK Starts
		System.out.println("Positive "+nltkmajorpos.size());
		System.out.println("NLTK  True"+ntrue+" False "+nfalse);
		System.out.println("NLTK 5%  True"+ntrueten+" False "+nfalseten);
		for (Entry<String, Double> entry1 : nltkmajorpos.entrySet()) {
			System.out.println("\t" +entry1.getKey() + "\t" + entry1.getValue());
		}
		System.out.println("Negative " + nltkmajorneg.size());
		for (Entry<String, Double> entry1 : nltkmajorneg.entrySet()) {
			System.out.println("\t" +entry1.getKey() + "\t" + entry1.getValue());
		}
		System.out.println("Total"+ (nltkmajorpos.size()+nltkmajorneg.size()));
		// Output for NLTK Ends
		
		// Output for Ling Starts
		System.out.println("Positive "+lingmajorpos.size());
		System.out.println("Ling  True"+ltrue+" False "+lfalse);
		System.out.println("Ling 5%  True"+ltrueten+" False "+lfalseten);
		for (Entry<String, Double> entry1 : lingmajorpos.entrySet()) {
			System.out.println("\t" +entry1.getKey() + "\t" + entry1.getValue());
		}
		System.out.println("Negative " + lingmajorneg.size());
		for (Entry<String, Double> entry1 : lingmajorneg.entrySet()) {
			System.out.println("\t" +entry1.getKey() + "\t" + entry1.getValue());
		}
		System.out.println("Total"+ (lingmajorpos.size()+lingmajorneg.size()));
		
	}// end main
	

}// end class
