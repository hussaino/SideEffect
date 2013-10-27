package com.example.sideeffect;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MyHttpProfile {

	String result = "No Results found\n-Check spelling\n-Check internet connection\n";

	public Document getInternetData(String query) {

		String url = "http://localhost:" + query;
		Document doc = null;

		try {
			doc = Jsoup.connect(url).get();

			// result = outStream;

		} catch (IOException e) {
			e.printStackTrace();
			doc = new Document("fail");
		}

		return doc;
	}

	public String parse(Document doc) {
		if (doc.baseUri() == "fail") {
			return result;
		}

		String outStream = "";
		Elements effects;
		effects = doc.getElementsByTag("body");
		for (Element src : effects) {
			outStream += src.toString();
		}
		return outStream;
	}

}
