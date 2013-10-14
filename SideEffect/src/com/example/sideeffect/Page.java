package com.example.sideeffect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.nodes.Document;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Page extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	EditText query_;
	Button searchBtn_;
	MyHttpGet myHttp_;
	LinearLayout linear_;
	WebServer web;
	public static final String ARG_SECTION_NUMBER = "section_number";

	public Page() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myHttp_ = new MyHttpGet();
		web = new WebServer();
		View rootView = inflater.inflate(R.layout.fragment_main_dummy,
				container, false);
		query_ = (EditText) rootView.findViewById(R.id.searchEdit);
		searchBtn_ = (Button) rootView.findViewById(R.id.searchBtn);
		linear_ = (LinearLayout) rootView.findViewById(R.id.list);
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			    
			    String query = query_.getText().toString();
				if (query.isEmpty()) {
					TextView text = new TextView(getActivity().getApplicationContext());
					text.setText("Input medicine name");
					linear_.removeAllViews();
					linear_.addView(text);
					return;
				}
				if(web.getMedicine().compareTo(query) != 0){
					web.setMedicine(query);
					Log.d("Hussain","not");
				}
				final BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>(1);
				// final BlockingQueue<String> str = new
				// LinkedBlockingQueue<String>(1);

				class Fetch extends AsyncTask<String, Void, String> {

					@Override
					protected String doInBackground(String... arg0) {
						Document doc = myHttp_.getInternetData(arg0[0]);
						queue.add(doc);
						return null;
					}
				}
				class Process extends AsyncTask<LinearLayout, Void, String> {

					LinearLayout linear;

					@Override
					protected String doInBackground(LinearLayout... arg0) {
						linear = arg0[0];
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

					protected void onPostExecute(String parameter) {
						linear.removeAllViews();
						if (parameter.contains("No Results")) {
							TextView textview = new TextView(getActivity()
									.getApplicationContext());
							textview.setText(parameter);
							linear.removeAllViews();
							linear.addView(textview);
							return;
						}
						String split[] = parameter.split("\n");
						for (String src : split) {
							if (!src.isEmpty()) {
								CheckBox check = new CheckBox(getActivity()
										.getApplicationContext());
								// check.setTextColor(Color.BLACK);
								check.setText(src);
								linear.addView(check);
							}
						}
						Button save = new Button(getActivity()
								.getApplicationContext());
						save.setText("Save");
						save.setId(9999);

						linear.addView(save);

						OnClickListener listener2 = new OnClickListener() {

							@Override
							public void onClick(View v) {
								class Push
										extends
										AsyncTask<LinearLayout, Void, String> {

									@Override
									protected String doInBackground(
											LinearLayout... params) {
										String outStream = "";
										LinearLayout ll = params[0];
										for (int i = 0; i < ll
												.getChildCount() - 1; i++) {
											CheckBox cb = (CheckBox) ll
													.getChildAt(i);
											if (cb.isChecked()) {
												outStream = cb.getText()
														.toString();
												web.add(outStream);
											}

										}
										return null;
									}

								}
								Push myTask3 = new Push();
								myTask3.execute(linear);
							}

						};
						save.setOnClickListener(listener2);
					}
				}
				Fetch myTask1 = new Fetch();
				myTask1.execute(query_.getText().toString());
				Process myTask2 = new Process();
				myTask2.execute(linear_);

			}
		};
		searchBtn_.setOnClickListener(listener);
		return rootView;
	}
}
