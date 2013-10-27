package com.example.sideeffect;

import static spark.Spark.get;
import static spark.Spark.setPort;

import java.util.Calendar;

import android.util.Log;
import android.widget.Toast;
import spark.Request;
import spark.Response;
import spark.Route;

public class WebServer {

	String list;
	String drug;
	String sensor, init;
	int count;
	String effectsurl;
	String sensorurl;

	public WebServer() {

		setPort(5678);
		list = "";
		drug = "";
		count = 0;
		sensor = "";
		init = "Number of Falls: <b>" + count + "</b><br><br><br>";
		effectsurl = "/effects";
		sensorurl = "/sensor";
	}

	public void setMedicine(String arg) {
		drug = arg;
		list = "Medicine Name: <b>" + drug
				+ "</b><br><br><br>Side Effects Encountered:<br><br>";
	}

	public String getMedicine() {
		return drug;
	}

	public boolean localSetPort(int arg1, String arg2, String arg3) {

		effectsurl = "/" + arg2;
		sensorurl = "/" + arg3;
		try{
			setPort(arg1);
		}catch(Error e){
			e.printStackTrace();
		}catch(Exception e2){
			e2.printStackTrace();
			return false;
		}
		return true;
	}

	public void add(String args) {
		if (!list.contains(args)) {
			list += args;
			list += "<br>";
			list += java.text.DateFormat.getDateTimeInstance().format(
					Calendar.getInstance().getTime());
			list += "<br><br>";
		} else {
			String str = args;
			str += "<br>";
			str += java.text.DateFormat.getDateTimeInstance().format(
					Calendar.getInstance().getTime());
			list = list.replace(args, str);
		}
		get(new Route(effectsurl) {
			@Override
			public Object handle(Request request, Response response) {
				// Log.d("Hussain",response.body());
				return "<font size=\"5\">" + list + "</font>";
			}
		});
	}

	public void addSensor(String arg) {

		String str = "Location: " + arg;
		str += "<br>";
		str += java.text.DateFormat.getDateTimeInstance().format(
				Calendar.getInstance().getTime());
		str += "<br>";
		sensor += str;
		init = init.replace(Integer.toString(count),
				Integer.toString(count + 1));
		sensor += "<br>";
		count++;
		get(new Route(sensorurl) {
			@Override
			public Object handle(Request request, Response response) {
				// Log.d("Hussain",response.body());
				return "<font size=\"5\">" + init + sensor + "</font>";
			}
		});
	}

}
