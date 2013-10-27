package com.example.sideeffect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.nodes.Document;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Profile extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	MyHttpProfile myHttp_;
	TextView textview_;
	Button button_;
	String url;
	public static final String ARG_SECTION_NUMBER = "section_number";

	public Profile() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		url = getArguments().getString("url");
		myHttp_ = new MyHttpProfile();
		View rootView = inflater.inflate(R.layout.fragment_download,
				container, false);
		button_ = (Button) rootView.findViewById(R.id.effectBtn);
		textview_ = (TextView) rootView.findViewById(R.id.textViewDownload);
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>(1);

				class Fetch extends AsyncTask<String, Void, String> {

					@Override
					protected String doInBackground(String... arg0) {
						Document doc = myHttp_.getInternetData(url);
						queue.add(doc);
						return null;
					}
				}
				class Process extends AsyncTask<TextView, Void, String> {

					TextView textview;

					@Override
					protected String doInBackground(TextView... arg0) {
						textview = arg0[0];
						Document doc;
						try {
							doc = queue.take();
							String out = myHttp_.parse(doc);
							if (!out.isEmpty())
								return out;

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						return "No Results found\n-Check spelling\n-Check internet connection\n";
					}
					protected void onPostExecute(String parameter){
						if(parameter.contains("No Results")){
							textview.setText(parameter);
							return;
						}
						textview.setText(Html.fromHtml(parameter));
					}
				}
				Fetch myTask1 = new Fetch();
				myTask1.execute();
				Process myTask2 = new Process();
				myTask2.execute(textview_);
			}
		};
		button_.setOnClickListener(listener);
		return rootView;
	}

	@Override
	public void onResume() {
		url = getArguments().getString("url");
		super.onResume();
	}
	
}
