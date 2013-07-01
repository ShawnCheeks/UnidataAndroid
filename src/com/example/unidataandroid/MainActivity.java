package com.example.unidataandroid;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import android.location.Location;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
//	private CheckBox[] cbs = new CheckBox[4];
	private GridView gvVariables;
	private Button bMainSubmit;
	
	private GoogleMap mapMainLocation;
	private Marker myMarker;
	
	private ArrayAdapter<?> productTypeAdapter,
							productAdapter,
							variableAdapter;
	
	private Calendar startCalendar = new GregorianCalendar();
	private Calendar endCalendar = new GregorianCalendar();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'");
	//parent layout is the background
	private RelativeLayout parentLayout;
	
	private String lat = "0", lon = "0";
	private String productType, product;
	
	private boolean productTypeSelected = false,
					productSelected = false,
					latSelected = false,
					lonSelected = false,
					startTimeSelected = false,
					endTimeSelected = false,
					variableSelected = false;
	
	
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
		/*
		 * set form variables
		 */
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		/*
		 * map stuff
		 * other map stuff is in showEnteredLocation, showMyLocation, setUpMapIfNeeded, and setUpLocationClientIfNeeded
		 */
		//setting up textchangedlisteners to update as the lat & lon values are edited manually
		etMainLat.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                lat = s.toString();
            }
        });		
		etMainLon.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                lon = s.toString();
            }
        });
		
		/*
		 * spinners for product type & product
		 * adaptive checkbox for variables
		 */		
		productTypeAdapter = ArrayAdapter.createFromResource(this, R.array.main_product_types, android.R.layout.simple_spinner_item);
        productTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		productAdapter = ArrayAdapter.createFromResource(this, R.array.products_empty, android.R.layout.simple_spinner_item);        
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, getResources().getStringArray(R.array.variables_empty));
        spinMainProductType.setAdapter(productTypeAdapter);
        spinMainProductType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productType = spinMainProductType.getSelectedItem().toString();
                changeProducts();
                spinMainProduct.setAdapter(productAdapter);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            	changeProducts();
            }
        });
        spinMainProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                product = spinMainProduct.getSelectedItem().toString();
                changeVariables();
                gvVariables.setAdapter(variableAdapter);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            	changeVariables();
            }
        });
        gvVariables.setAdapter(variableAdapter);
		/*
		 * starting & ending times default
		 */
		etMainTimeStart.setText(dateFormat.format(startCalendar.getTime()));
		endCalendar.add(Calendar.DATE, 2);
		etMainTimeEnd.setText(dateFormat.format(endCalendar.getTime()));
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
//		cbs[0] = (CheckBox)findViewById(R.id.checkBox1);
//		cbs[1] = (CheckBox)findViewById(R.id.checkBox2);
//		cbs[2] = (CheckBox)findViewById(R.id.checkBox3);
//		cbs[3] = (CheckBox)findViewById(R.id.checkBox4);
		gvVariables = (GridView)findViewById(R.id.gridview_variables);
		bMainSubmit = (Button)findViewById(R.id.button_location_show);
		//map requires special setup
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		
		//setting up the touch listener to the background
		//otherwise, the page will default to the first edittext in the layout
		parentLayout = (RelativeLayout)findViewById(R.id.parent);
		parentLayout.setOnTouchListener(this);
	}
	public void changeProducts()
	{
		if(productType.equals("NCEP Model Data"))
		{
			productAdapter = ArrayAdapter.createFromResource(this, R.array.products_ncep, android.R.layout.simple_spinner_item);
			productTypeSelected = true;
		}
		else
		{
			productAdapter = ArrayAdapter.createFromResource(this, R.array.products_empty, android.R.layout.simple_spinner_item);
			productTypeSelected = false;
		}
		
		if(!productTypeSelected)
			spinMainProduct.setClickable(false);
		else
			spinMainProduct.setClickable(true);
		
	}
	public void changeVariables()
	{
		if(product.equals("GFS CONUS 80km"))
		{
			variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, getResources().getStringArray(R.array.variables_gfs_conus_80km));
			productSelected = true;
		}
		else
		{
			variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.variables_empty));
			productSelected = false;
		}
		
		if(!productSelected)
			gvVariables.setClickable(false);
		else
			gvVariables.setClickable(true);
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

	public void showEnteredLocation(View view)	{
		if(myMarker != null)
			myMarker.remove();
		LatLng loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
		myMarker = mapMainLocation.addMarker(new MarkerOptions().position(loc)
													 .title("Location Entered"));
		mapMainLocation.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,(float) 9));
	}

	/*
	 * from here down is borrowed from the Google Maps API sample code
	 */
	private void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mapMainLocation == null) {
	      // Try to obtain the map from the SupportMapFragment.
	      mapMainLocation = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map))
	             .getMap();
	      mapMainLocation.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	      //set default camera to geographic center of contiguous US (Lebanon, KS: 39.83, -98.58) and zoom to fit US
	      //adjusted starting point to the east and south to move Maine and Florida out from under map buttons & text
	      mapMainLocation.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.33,-88.58),(float) 2.5));
	      mapMainLocation.addPolygon(new PolygonOptions().add(new LatLng(57.4843,-153.5889))
	    		  										 .add(new LatLng(11.7476,-153.5889))
	    		  										 .add(new LatLng(11.7476,-48.5984))
	    		  										 .add(new LatLng(57.4843,-48.5984))
	    		  										 .strokeWidth(10)
	    		  										 .fillColor(0x55ffffff));
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
	  DecimalFormat gpsFormat = new DecimalFormat("0.00000");
		if (myLocationClient != null && myLocationClient.isConnected()) {
	    lat = gpsFormat.format(myLocationClient.getLastLocation().getLatitude());
		lon = gpsFormat.format(myLocationClient.getLastLocation().getLongitude());
	    etMainLat.setText(lat);
		etMainLon.setText(lon);
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
