import java.io.File;
import java.io.IOException;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LMClassifier;
import com.aliasi.util.AbstractExternalizable;

/*Source: https://github.com/johnbyron/TwitterSentiment/blob/master/classifierLoader.java */

/*Author: Shivam Dhole
  Student No: 741507
*/

/*
This code loads the classifier from classifier.txt file and calculates sentiment of a given text
using LingPipe. The classify function takes a small input text and returns the sentiment of the text as Positive, Negative or Neutral.
*/

public class SentimentClassifier {
	String[] categories;
	LMClassifier senticlass;

	public SentimentClassifier() {
		try {
			sentimentclass = (LMClassifier) AbstractExternalizable.readObject(new File("classifier.txt"));
			categories = sentimentclass.categories();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String classify(String text) {
		ConditionalClassification classification = sentimentclass.classify(text);
		return classification.bestCategory();
	}
}