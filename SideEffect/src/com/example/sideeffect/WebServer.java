package com.example.sideeffect;

import static spark.Spark.get;
import static spark.Spark.setPort;

import java.util.Calendar;

import spark.Request;
import spark.Response;
import spark.Route;

public class WebServer {

	String list;
	String drug;

	public WebServer() {

		setPort(5678);
		list = "";
		drug = "";
	}

	public void setMedicine(String arg) {
		drug = arg;
		list = "Medicine Name: <b>" + drug
				+ "</b><br><br><br>Side Effects Encountered:<br><br>";
	}

	public String getMedicine() {
		return drug;
	}

	public void add(String args) {
		if (!list.contains(args)) {
			list += args;
			list += "<br>";
			list += java.text.DateFormat.getDateTimeInstance().format(
					Calendar.getInstance().getTime());
			list += "<br><br>";
		}
		else{
			String str = args;
			str += "<br>";
			str += java.text.DateFormat.getDateTimeInstance().format(
					Calendar.getInstance().getTime());
			list = list.replace(args, str);
		}
		get(new Route("/") {
			@Override
			public Object handle(Request request, Response response) {
				// Log.d("Hussain",response.body());
				return "<font size=\"5\">" + list + "</font>";
			}
		});
	}

}
