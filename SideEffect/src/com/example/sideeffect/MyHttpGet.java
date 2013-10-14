package com.example.sideeffect;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MyHttpGet {

	String result = "No Results found\n-Check spelling\n-Check internet connection\n";

	public Document getInternetData(String query) {
		// queue = new LinkedBlockingQueue<Document>(2);
		if (query.contains(" ")) {
			query = query.replace(" ", "-");
		}

		String url = "http://www.drugs.com/sfx/" + query + "-side-effects.html";
		Document doc = null;

		try {
			doc = Jsoup.connect(url).get();

			// result = outStream;

		} catch (IOException e) {
			// TODO Auto-generated catch block

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
		boolean flag = false;
		Elements effects;
		effects = doc.getAllElements();
		for (Element src : effects) {
			if (src.tagName() == "i" && src.ownText().contains("More common"))
				flag = true;
			else if (src.tagName() == "i")
				flag = false;
			if (!src.ownText().isEmpty() && src.tagName() == "li" && flag) {// &&
																			// src.tagName()
																			// ==
																			// "li"){
				outStream += "\n" + src.ownText();
			}
		}
		return outStream;
	}

}
