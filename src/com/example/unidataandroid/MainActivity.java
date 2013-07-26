package com.example.unidataandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
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

public class MainActivity extends UnidataSuperActivity
implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnTouchListener{
	
	//declare the widgets
	private Spinner spinMainProductType,
					spinMainProduct,
					spinMainVariables,
					spinMainTimeStart,
					spinMainTimeEnd;
					
	private EditText etMainLat,
					 etMainLon;
	
	private GoogleMap mapMainLocation;
	private Marker myMarker;
	
	private ArrayAdapter<?> productTypeAdapter,
							productAdapter,
							variableAdapter,
							timeStartAdapter,
							timeEndAdapter;
	
	private Calendar startCalendar = new GregorianCalendar();
	private Calendar endCalendar = new GregorianCalendar();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'");
	//parent layout is the background
	private RelativeLayout parentLayout;
	
	private String lat = "0", lon = "0";
	private String productType, product, variable, timeStart, timeEnd;
	
	private boolean productTypeSelected = false,
					productSelected = false,
					latSelected = false,
					lonSelected = false,
					startTimeSelected = false,
					endTimeSelected = false,
					variableSelected = false;
	
	private Variable[] modelVariables;
	
	
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
        variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.variables_empty));
        timeStartAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_start_empty));
        timeEndAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_end_empty));
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
        spinMainProduct.setAdapter(productAdapter);
        spinMainProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                product = spinMainProduct.getSelectedItem().toString();

                changeVariables();
                spinMainVariables.setAdapter(variableAdapter);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            	changeVariables();
            }
        });
        spinMainVariables.setAdapter(variableAdapter);
        spinMainVariables.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		variable = spinMainVariables.getSelectedItem().toString();
        		
        		changeTimeStart();
        		spinMainTimeStart.setAdapter(timeStartAdapter);
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
				changeTimeStart();
			}
        });
        spinMainTimeStart.setAdapter(timeStartAdapter);
        spinMainTimeStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		timeStart = spinMainTimeStart.getSelectedItem().toString();
        		
        		changeTimeEnd();
        		spinMainTimeEnd.setAdapter(timeEndAdapter);
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
				changeTimeEnd();
			}
        });
        spinMainTimeEnd.setAdapter(timeEndAdapter);
        spinMainTimeEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		timeStart = spinMainTimeStart.getSelectedItem().toString();
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
				changeTimeEnd();
			}
        });
		/*
		 * starting & ending times default
		 */
//		etMainTimeStart.setText(dateFormat.format(startCalendar.getTime()));
//		endCalendar.add(Calendar.DATE, 2);
//		etMainTimeEnd.setText(dateFormat.format(endCalendar.getTime()));
	}
	public void initForm()
	{
		//link the everything to the xml counterparts
		spinMainProductType = (Spinner)findViewById(R.id.spinner_product_type);
		spinMainProduct     = (Spinner)findViewById(R.id.spinner_product);
		spinMainVariables	= (Spinner)findViewById(R.id.spinner_variable);
		spinMainTimeStart 	= (Spinner)findViewById(R.id.spinner_time_start);
		spinMainTimeEnd		= (Spinner)findViewById(R.id.spinner_time_end);
		etMainLat       = (EditText)findViewById(R.id.edittext_lat);
		etMainLon       = (EditText)findViewById(R.id.edittext_lon);
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
			productAdapter = ArrayAdapter.createFromResource(this, R.array.products_ncep, android.R.layout.simple_spinner_dropdown_item);
			productTypeSelected = true;
		}
		else
		{
			productAdapter = ArrayAdapter.createFromResource(this, R.array.products_empty, android.R.layout.simple_spinner_dropdown_item);
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
			UnidataSuperActivity.setModelURL("http://thredds.ucar.edu/thredds/ncss/grid/grib/NCEP/GFS/CONUS_80km/best/dataset.xml");
			super.setDone(false);
			new MyTask().execute();
			modelVariables = extractVariables(); 
			String[] modelVariableNames = new String[modelVariables.length];
			for(int i=0; i<modelVariables.length; i++)
				modelVariableNames[i] = modelVariables[i].getDescription();
			
			variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, modelVariableNames);
			productSelected = true;
		}
		else
		{
			variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.variables_empty));
			productSelected = false;
		}
		
		if(!productSelected){
			spinMainVariables.setClickable(false);
		}else {
			spinMainVariables.setClickable(true);
		}
	}
	public void changeTimeStart(){
		if(productSelected){
			timeStartAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, super.getValidTimes());
			startTimeSelected = true;
		}
		else {
			timeStartAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_start_empty));
			startTimeSelected = true;
		}
		
		if(!productSelected){
			spinMainVariables.setClickable(false);
		}else {
			spinMainVariables.setClickable(true);
		}
	}
	public void changeTimeEnd(){
		if(startTimeSelected){
			String[] startTimes = super.getValidTimes();
			String[] endTimes = Arrays.copyOfRange(startTimes, spinMainTimeStart.getSelectedItemPosition()+1, startTimes.length);
			timeEndAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, endTimes);
		}else{
			timeEndAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_end_empty));
		}
		
		if(!startTimeSelected){
			spinMainVariables.setClickable(false);
		}else {
			spinMainVariables.setClickable(true);
		}
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

	public void callDisplayActivity(View view)
	{
//		System.out.println(modelVariables[spinMainVariables.getSelectedItemPosition()].getName());
//		System.out.println(etMainLat.getText().toString());
//		System.out.println(etMainLon.getText().toString());
//		System.out.println(etMainTimeStart.getText().toString());
//		System.out.println(etMainTimeEnd.getText().toString());
		
		String a,b,c,d,e;
		a=modelVariables[spinMainVariables.getSelectedItemPosition()].getName();
		b=etMainLat.getText().toString();
		c=etMainLon.getText().toString();
		d=spinMainTimeStart.getSelectedItem().toString().replace(":", "%3A");
		e=spinMainTimeEnd.getSelectedItem().toString().replace(":", "%3A");
		
		super.setURL("http://thredds.ucar.edu/thredds/ncss/grid/grib/NCEP/GFS/CONUS_80km/best" +
					 "?var=" + a +
					 "&latitude=" + b +
					 "&longitude=" + c +
					 "&time_start=" + d +
					 "&time_end=" + e +
					 "&vertCoord=&accept=xml");
		super.setSampleVariable(modelVariables[spinMainVariables.getSelectedItemPosition()]);
		Intent i = new Intent(this, DisplayActivity.class);
		startActivity(i);
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
	
	public Variable[] extractVariables()
	{
		while(!UnidataSuperActivity.getDone()){}
		
		ArrayList<Variable> variables = new ArrayList<Variable>();
		String name, desc, unit;
		String modelXml = super.getModelXML();
	    try
	    {
	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	factory.setNamespaceAware(false);
	        XmlPullParser xpp = factory.newPullParser();

	        xpp.setInput(new StringReader (modelXml));
	        int eventType = xpp.getEventType();
	        
	        while(eventType != XmlPullParser.END_DOCUMENT) {
	        	if(xpp.getDepth()==2 && xpp.getAttributeValue(null, "name")!=null && xpp.getAttributeValue(null, "name").equals("time")){
	        		xpp.nextTag();
	        		super.setValidTimes(xpp.getAttributeValue(null, "value").substring(11));
	        	}
	        	if(xpp.getDepth()==3 && xpp.getAttributeValue(null, "shape")!=null && xpp.getAttributeValue(null, "shape").equals("time y x")){
	        		System.out.println(xpp.getAttributeValue(null, "name") + "\n" + xpp.getAttributeValue(null, "desc"));
	        		name = xpp.getAttributeValue(null, "name");
	        		desc = xpp.getAttributeValue(null, "desc");
        			boolean keepGoing = true;
	            	xpp.nextTag();
	            	while(xpp.getDepth()==4 && keepGoing)
	            	{
	            		if(xpp.getAttributeValue(null, "name").equals("units"))
	            		{
	            			System.out.println(xpp.getAttributeValue(null, "value"));
	            			unit = xpp.getAttributeValue(null, "value");
	            			keepGoing = false;
	            			variables.add(new Variable(name, desc, unit));
	            		} else {
	            			xpp.nextTag();
	            		}	            				
	            	}
	        	}
	        	eventType = xpp.nextToken();
	        }

	    }catch(XmlPullParserException e){
	    	e.printStackTrace();
	    }catch(IOException e){
	    	e.printStackTrace();
	    }
	    
	    return variables.toArray(new Variable[variables.size()]);
	}
	
	private class MyTask extends AsyncTask<Void, Void, Void>{
		
		String textResult;
	    
	    @Override
	    protected Void doInBackground(Void... params) {
	    	UnidataSuperActivity.setDone(false);
	    	
	        URL textUrl;
	        String address = UnidataSuperActivity.getModelURL();
	        try {
	         textUrl = new URL(address);
	         
	         File testFile = File.createTempFile("temp", ".dat");
	         testFile.deleteOnExit();
	         FileUtils.copyURLToFile(textUrl, testFile);
	         textResult = FileUtils.readFileToString(testFile);
	         
	        } catch (MalformedURLException e) {
	         e.printStackTrace();
	         textResult = e.toString();   
	        } catch (IOException e) {
	         e.printStackTrace();
	         textResult = e.toString();   
	        }
	        
	        UnidataSuperActivity.setModelXML(textResult);
	        UnidataSuperActivity.setDone(true);

	     return null;
	     
	    }
	    
	    @Override
	    protected void onPostExecute(Void result) {
	    	
	     super.onPostExecute(result);   
	    }
	}
}
