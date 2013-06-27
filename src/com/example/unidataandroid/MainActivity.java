package com.example.unidataandroid;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity
implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnTouchListener{
	
	//declare the widgets
	private TextView tvMainWelcome,
					 tvMainProductType,
					 tvMainProduct,
					 tvMainLocation,
					 tvMainLat,
					 tvMainLon,
					 tvMainTimeStart,
					 tvMainTimeEnd,
					 tvMainVariables;
	private Spinner spinMainProductType,
					spinMainProduct;
	private EditText etMainLat,
					 etMainLon,
					 etMainTimeStart,
					 etMainTimeEnd;
	private GoogleMap mapMainLocation;
	private CheckBox[] cbs = new CheckBox[4];
	private Button bMainSubmit;
	
	//parent layout is the background
	private RelativeLayout parentLayout;
	
	// These settings are the same as the settings for the map. They will in fact give you updates at
	// the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(16)    // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	//location client for the map
	private LocationClient myLocationClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		initForm();
		myLocationClient.connect();
		//declare form variables
//		double lat, lon;
		Calendar startCalendar = new GregorianCalendar();
		Calendar endCalendar = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		//map stuff
		if(myLocationClient == null)
		{
			System.out.println("NULL!");
		}
		if(!myLocationClient.isConnected())
		{
			System.out.println("CONNECT!");
		}
		if (myLocationClient != null && myLocationClient.isConnected()) {
		    double lat = myLocationClient.getLastLocation().getLatitude();
			double lon = myLocationClient.getLastLocation().getLongitude();
		    etMainLat.setText(Double.toString(lat));
			etMainLon.setText(Double.toString(lon));
		  }
		//spinner for product type
		
		//spinner for product
		
		//starting time
		etMainTimeStart.setText(dateFormat.format(startCalendar.getTime()));
		
		//ending time
		endCalendar.add(Calendar.DATE, 2);
		etMainTimeEnd.setText(dateFormat.format(endCalendar.getTime()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
	  super.onResume();
	  setUpMapIfNeeded();
	  setUpLocationClientIfNeeded();
	  myLocationClient.connect();
	}

	@Override
	public void onPause() {
	  super.onPause();
	  if (myLocationClient != null) {
	    myLocationClient.disconnect();
	  }
	}
	
	@Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v == parentLayout)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            View focusView = getCurrentFocus();
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            if(focusView instanceof EditText)
                focusView.clearFocus();
            return true;
        }
        return false;
    }

	public void initForm()
	{
		//link the everything to the xml counterparts
		tvMainWelcome     = (TextView)findViewById(R.id.textview_welcome);
		tvMainProductType = (TextView)findViewById(R.id.textview_product_type);
		tvMainProduct     = (TextView)findViewById(R.id.textview_product);
		tvMainLocation    = (TextView)findViewById(R.id.textview_location);
		tvMainLat    = (TextView)findViewById(R.id.textview_lat);
		tvMainLon    = (TextView)findViewById(R.id.textview_lon);
		tvMainTimeStart   = (TextView)findViewById(R.id.textview_time_start);
		tvMainTimeEnd     = (TextView)findViewById(R.id.textview_time_end);
		tvMainVariables   = (TextView)findViewById(R.id.textview_variables);
		spinMainProductType = (Spinner)findViewById(R.id.spinner_product_type);
		spinMainProduct     = (Spinner)findViewById(R.id.spinner_product);
		etMainLat       = (EditText)findViewById(R.id.edittext_lat);
		etMainLon       = (EditText)findViewById(R.id.edittext_lon);
		etMainTimeStart = (EditText)findViewById(R.id.edittext_time_start);
		etMainTimeEnd   = (EditText)findViewById(R.id.edittext_time_end);
		cbs[0] = (CheckBox)findViewById(R.id.checkBox1);
		cbs[1] = (CheckBox)findViewById(R.id.checkBox2);
		cbs[2] = (CheckBox)findViewById(R.id.checkBox3);
		cbs[3] = (CheckBox)findViewById(R.id.checkBox4);
		bMainSubmit = (Button)findViewById(R.id.button_submit);
		//map requires special setup
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		
		//setting up the touch listener to the background
		//otherwise, the page will default to the first edittext in the layout
		parentLayout = (RelativeLayout)findViewById(R.id.parent);
		parentLayout.setOnTouchListener(this);
	}

	//from here down is borrowed from the Google Maps API sample code
	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mapMainLocation == null) {
	      // Try to obtain the map from the SupportMapFragment.
	      mapMainLocation = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map))
	             .getMap();
	      // Check if we were successful in obtaining the map.
	      if (mapMainLocation != null) {
	        mapMainLocation.setMyLocationEnabled(true);
	      }
	    }
	  }
	
	private void setUpLocationClientIfNeeded() {
	    if (myLocationClient == null) {
	      myLocationClient = new LocationClient(
	          getApplicationContext(),
	          this,  // ConnectionCallbacks
	          this); // OnConnectionFailedListener
	    }
	  }
	
	/**
	   * Button to get current Location. This demonstrates how to get the current Location as required,
	   * without needing to register a LocationListener.
	   */
	public void showMyLocation(View view) {
	  if (myLocationClient != null && myLocationClient.isConnected()) {
	    double lat = myLocationClient.getLastLocation().getLatitude();
		double lon = myLocationClient.getLastLocation().getLongitude();
	    etMainLat.setText(Double.toString(lat));
		etMainLon.setText(Double.toString(lon));
	  }
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
	  // do nothing
	}
	/**
	 * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
	  myLocationClient.requestLocationUpdates(
	      REQUEST,
	      this);  // LocationListener
	}
	/**
	 * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
	  // Do nothing
	}
	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
	  // Do nothing
	}
}
