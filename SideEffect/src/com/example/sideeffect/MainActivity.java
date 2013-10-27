package com.example.sideeffect;

import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.nodes.Document;

import android.util.FloatMath;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.drm.DrmStore.Action;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, SensorEventListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	static WebServer web;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mGyroscope;
	private LocationManager mGPS;
	float gravity[];
	float linear_acceleration[];
	int disregard;
	private static boolean activityVisible;
	String port, effecturl, sensorurl;
	Page page;
	Profile profile;
	SensorClass sensorclass;
	ToggleButton toggle;
	long oldstamp;
	static boolean enableSensor;
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float[] deltaRotationVector;
	private float timestamp;
	private float rotationCurrent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mGPS = (LocationManager) this
				.getSystemService(getApplicationContext().LOCATION_SERVICE);
		mGPS.getLastKnownLocation(mGPS.GPS_PROVIDER);
		linear_acceleration = new float[3];
		gravity = new float[3];
		deltaRotationVector = new float[4];
		rotationCurrent = 1;
		disregard = 0;
		port = "5678";
		oldstamp = 0;
		effecturl = "effects";
		sensorurl = "sensor";
		page = new Page();
		enableSensor = true;
		profile = new Profile();
		sensorclass = new SensorClass();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		web = new WebServer();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mGyroscope,
				SensorManager.SENSOR_DELAY_NORMAL);
		activityVisible = true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem portSet = menu.getItem(0);

		OnMenuItemClickListener menuListener = new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				showNoticeDialog();
				return true;
			}

		};
		portSet.setOnMenuItemClickListener(menuListener);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			if (position == 0) {
				Fragment fragment = new Page();
				Bundle args = new Bundle();
				args.putInt(Page.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				fragment.setRetainInstance(true);
				return fragment;
			} else if (position == 1) {
				Fragment fragment = new Profile();
				Bundle args = new Bundle();
				args.putString("url", port + "/" + effecturl);
				args.putInt(Profile.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				fragment.setRetainInstance(true);
				return fragment;
			} else {
				Fragment fragment = new SensorClass();
				Bundle args = new Bundle();
				args.putString("url", port + "/" + sensorurl);
				args.putInt(SensorClass.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				fragment.setRetainInstance(true);
				// toggle = (ToggleButton)
				// fragment.getActivity().findViewById(R.id.toggle);
				return fragment;
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	protected void onPause() {
		activityVisible = false;
		super.onPause();
	}

	@Override
	public void onAccuracyChanged(android.hardware.Sensor arg0, int arg1) {

	}

	class WebSensor extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			web.addSensor(params[0]);
			return null;
		}

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor source = event.sensor;

		// This timestep's delta rotation to be multiplied by the current
		// rotation
		// after computing it from the gyro sample data.
		if (timestamp != 0) {
			final float dT = (event.timestamp - timestamp) * NS2S;
			// Axis of the rotation sample, not normalized yet.
			float axisX = event.values[0];
			float axisY = event.values[1];
			float axisZ = event.values[2];

			// Calculate the angular speed of the sample
			float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY
					* axisY + axisZ * axisZ);

			// Normalize the rotation vector if it's big enough to get the axis
			if (omegaMagnitude > 1) {
				axisX /= omegaMagnitude;
				axisY /= omegaMagnitude;
				axisZ /= omegaMagnitude;
			}

			// Integrate around this axis with the angular speed by the timestep
			// in order to get a delta rotation from this sample over the
			// timestep
			// We will convert this axis-angle representation of the delta
			// rotation
			// into a quaternion before turning it into the rotation matrix.
			float thetaOverTwo = omegaMagnitude * dT / 2.0f;
			float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
			float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
			deltaRotationVector[0] = sinThetaOverTwo * axisX;
			deltaRotationVector[1] = sinThetaOverTwo * axisY;
			deltaRotationVector[2] = sinThetaOverTwo * axisZ;
			deltaRotationVector[3] = cosThetaOverTwo;
		}
		timestamp = event.timestamp;
		float[] deltaRotationMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(deltaRotationMatrix,
				deltaRotationVector);
		// User code should concatenate the delta rotation we computed with the
		// current rotation
		// in order to get the updated rotation.
		if (deltaRotationMatrix[0] < 0.98)
			rotationCurrent = deltaRotationMatrix[0];

		if (!enableSensor) {
			return;
		}
		if (source == mAccelerometer) {
			final float alpha = (float) 0.8;
			disregard++;
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];

			if (toggle != null) {
				if (!toggle.isChecked()) {
					return;
				}
			}
			if (event.timestamp - oldstamp < 1000000000L) {
				return;
			}
			if (disregard > 6
					&& rotationCurrent != 0
					&& (Math.abs(linear_acceleration[0]) > 10
							|| Math.abs(linear_acceleration[1]) > 10 || Math
							.abs(linear_acceleration[2]) > 10)) {
				// web.addSensor();
				WebSensor mytask = new WebSensor();
				oldstamp = event.timestamp;
				Log.d("Hussain",
						"(" + rotationCurrent + ","
								+ Math.abs(linear_acceleration[0]) + ","
								+ Math.abs(linear_acceleration[1]) + ","
								+ Math.abs(linear_acceleration[2]) + ")");
				rotationCurrent = 0;
				mytask.execute("("
						+ mGPS.getLastKnownLocation(mGPS.GPS_PROVIDER)
								.getLatitude()
						+ ":"
						+ mGPS.getLastKnownLocation(mGPS.GPS_PROVIDER)
								.getLongitude() + ")");
				if (activityVisible) {
					call();
				}
			}
		}

	}

	private void call() {
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:911"));
			startActivity(callIntent);
		} catch (ActivityNotFoundException e) {
			Log.e("Hussain", "Call failed", e);
		}
	}

	public static boolean isActivityVisible() {
		return activityVisible;
	}

	public void showNoticeDialog() {
		// Create an instance of the dialog fragment and show it

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Get the layout inflater
		LayoutInflater inflater = getLayoutInflater();

		final View view = inflater.inflate(R.layout.dialog, null);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view)
				// Add action buttons
				.setPositiveButton("Set",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								TextView textview = (TextView) view
										.findViewById(R.id.port_url);
								if (!textview.getText().toString().isEmpty()) {
									port = textview.getText().toString();
								}
								textview = (TextView) view
										.findViewById(R.id.effects_url);
								if (!textview.getText().toString().isEmpty()) {
									effecturl = textview.getText().toString();
								}
								textview = (TextView) view
										.findViewById(R.id.sensor_url);
								if (!textview.getText().toString().isEmpty()) {
									sensorurl = textview.getText().toString();
								}
								if (web.localSetPort(Integer.decode(port),
										effecturl, sensorurl)) {
									Toast.makeText(getApplicationContext(),
											"Success", Toast.LENGTH_SHORT)
											.show();
								} else {
									Toast.makeText(
											getApplicationContext(),
											"Cannot reset port after use\nP.S. other URLs are taken",
											Toast.LENGTH_SHORT).show();
								}

							}

						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
		builder.create();
		builder.show();
	}

	public static class Page extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		EditText query_;
		Button searchBtn_;
		MyHttpGet myHttp_;
		LinearLayout linear_;
		public static final String ARG_SECTION_NUMBER = "section_number";

		public Page() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			myHttp_ = new MyHttpGet();
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			query_ = (EditText) rootView.findViewById(R.id.searchEdit);
			searchBtn_ = (Button) rootView.findViewById(R.id.searchBtn);
			linear_ = (LinearLayout) rootView.findViewById(R.id.list);
			OnClickListener listener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					InputMethodManager imm = (InputMethodManager) getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,
							0);

					String query = query_.getText().toString();
					if (query.isEmpty()) {
						TextView text = new TextView(getActivity()
								.getApplicationContext());
						text.setText("Input medicine name");
						linear_.removeAllViews();
						linear_.addView(text);
						return;
					}
					if (web.getMedicine().compareTo(query) != 0) {
						web.setMedicine(query);
					}
					final BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>(
							1);
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

	public static class SensorClass extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		MyHttpProfile myHttp_;
		TextView textview_;
		Button button_;
		String url;
		public static final String ARG_SECTION_NUMBER = "section_number";

		public SensorClass() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			url = getArguments().getString("url");
			myHttp_ = new MyHttpProfile();
			View rootView = inflater.inflate(R.layout.fragment_sensor,
					container, false);
			button_ = (Button) rootView.findViewById(R.id.fallBtn);
			textview_ = (TextView) rootView.findViewById(R.id.textViewSensor);
			ToggleButton toggle = (ToggleButton) rootView
					.findViewById(R.id.toggle);
			OnCheckedChangeListener toggleListener = new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						enableSensor = true;
					} else {
						enableSensor = false;
					}

				}
			};
			toggle.setOnCheckedChangeListener(toggleListener);
			OnClickListener listener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					final BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>(
							1);

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

						protected void onPostExecute(String parameter) {
							if (parameter.contains("No Results")) {
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
	}
}
