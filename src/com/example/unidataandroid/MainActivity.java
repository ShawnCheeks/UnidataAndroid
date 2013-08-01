/** MainActivity formats the URL containing the desired data
 * AChartEngine info: http://achartengine.org/ 
 * XMLPullParser info: http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
 * Commons IO info: http://commons.apache.org/
 * Google Maps Android API info: https://developers.google.com/
 */

/**
 * @author Shawn Cheeks
 * @version 8/1/2013
 * @contact cheeks5@marshall.edu
 */
package com.example.unidataandroid;

import java.io.File;
import java.io.IOException;
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
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

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
	
	//declare the GoogleMap stuff
	private GoogleMap mapMainLocation;
	private Marker myMarker;
	
	//declare the array adapters that will be used by the spinners
	private ArrayAdapter<?> productTypeAdapter,
							productAdapter,
							variableAdapter,
							timeStartAdapter,
							timeEndAdapter;
	
	//declare other important variables
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T00:00:00Z'");
	//parent layout is the background
	private RelativeLayout parentLayout;
	
	private String lat = "0", lon = "0";
	
	/* original plan was to use these variables as a way to transfer information to the URL...
	 * ended up never completely doing that, but the option is there if you'd like
	 */
	private String productType, product, variable, timeStart, timeEnd;
	
	/* my booleans that were intended to be the detection to make sure everything had been filled out
	 * also ended up neglected, but the framework is there for that bug fix
	 */
	private boolean productTypeSelected = false,
					productSelected = false,
					latSelected = false,
					lonSelected = false,
					startTimeSelected = false,
					endTimeSelected = false,
					variableSelected = false;
	
	// array of variables
	private Variable[] modelVariables;
	
	
	// These settings are the same as the settings for the map. They will in fact give you updates at
	// the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(16)    // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	//location client for the map
	private LocationClient myLocationClient;

	/* Android's version of the main method */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//hides the soft keyboard at app loading
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		
        //setContentView connects the app to the layout at the xml file activity_main.xml
        setContentView(R.layout.activity_main);
		
        //calls the method that will assign the widgets declared here with the appropriate widgets as defined in activity_main.xml
		initForm();
		//location client for location based services
		myLocationClient.connect();
		
		//this may or may not do anything since everything is UTC... but I have it there anyway
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
		 * also sets up the adapters that will fill the spinners
		 */		
		productTypeAdapter = ArrayAdapter.createFromResource(this, R.array.main_product_types, android.R.layout.simple_spinner_item);
        productTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
		productAdapter = ArrayAdapter.createFromResource(this, R.array.products_empty, android.R.layout.simple_spinner_item);        
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.variables_empty));
        
        timeStartAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_start_empty));
        
        timeEndAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_end_empty));
        
        /* the main product spinner */
        spinMainProductType.setAdapter(productTypeAdapter);
        spinMainProductType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productType = spinMainProductType.getSelectedItem().toString();
                /* once the item selected changes, it will update the options available in later spinners */
                changeProducts();
                spinMainProduct.setAdapter(productAdapter);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            	/* once the item selected changes, it will update the options available in later spinners*/
            	changeProducts();
            }
        });
        
        /* the products spinners */
        spinMainProduct.setAdapter(productAdapter);
        spinMainProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                product = spinMainProduct.getSelectedItem().toString();
                /* once the item selected changes, it will update the options available in later spinners */
                changeVariables();
                spinMainVariables.setAdapter(variableAdapter);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            	/* once the item selected changes, it will update the options available in later spinners */
            	changeVariables();
            }
        });
        
        /* the variables spinners */
        spinMainVariables.setAdapter(variableAdapter);
        spinMainVariables.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		variable = spinMainVariables.getSelectedItem().toString();
        		/* once the item selected changes, it will update the options available in later spinners */
        		changeTimeStart();
        		spinMainTimeStart.setAdapter(timeStartAdapter);
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
				/* once the item selected changes, it will update the options available in later spinners */
				changeTimeStart();
			}
        });
        
        /* the starting time spinner */
        spinMainTimeStart.setAdapter(timeStartAdapter);
        spinMainTimeStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		timeStart = spinMainTimeStart.getSelectedItem().toString();
        		/* once the item selected changes, it will update the options available in later spinners */
        		changeTimeEnd();
        		spinMainTimeEnd.setAdapter(timeEndAdapter);
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
				/* once the item selected changes, it will update the options available in later spinners */
				changeTimeEnd();
			}
        });
        
        /* the ending time spinner */
        spinMainTimeEnd.setAdapter(timeEndAdapter);
        spinMainTimeEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		timeStart = spinMainTimeStart.getSelectedItem().toString();
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
				changeTimeEnd();
			}
        });
	}
	
	/* initializes the form by linking the widgets declared here with the widgets as defined in activity_main.xml */
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
	
	/* changes the products according to the choice of product type */
	public void changeProducts()
	{
		/* hard coded to change based on NCEP */
		if(productType.equals("NCEP Model Data"))
		{
			/* changes the array used to fill the adapter */
			productAdapter = ArrayAdapter.createFromResource(this, R.array.products_ncep, android.R.layout.simple_spinner_dropdown_item);
			productTypeSelected = true;
		}
		else
		{
			productAdapter = ArrayAdapter.createFromResource(this, R.array.products_empty, android.R.layout.simple_spinner_dropdown_item);
			productTypeSelected = false;
		}
		
		/* (un)locks a lower level spinner once it is appropriate */
		if(!productTypeSelected)
			spinMainProduct.setClickable(false);
		else
			spinMainProduct.setClickable(true);
	}
	
	/* changes the variables according to the choice of product */
	public void changeVariables()
	{
		/* hard coded to change based on GFS CONUS 80km */
		if(product.equals("GFS CONUS 80km"))
		{
			UnidataSuperActivity.setModelURL("http://thredds.ucar.edu/thredds/ncss/grid/grib/NCEP/GFS/CONUS_80km/best/dataset.xml"); //hardcoded
			
			/* MyTask is the new thread that accesses the network
			 * In Android, network access must be done on a separate thread
			 * There is a way to override that requirement, but it is really, really, really frowned upon
			 * 
			 * sets the "done" variable to false so that the dummy loop will wait 
			 * until the MyTask thread sets the variable to true
			 * without this dummy while loop, the rest of this thread will continue on
			 * concurrently with the other thread, but it needs the info from that thread
			 * before it can do what it's supposed to do
			 * This probably isn't the best solution, but it worked, so I was happy with it
			 */
			super.setDone(false);
			new MyTask().execute();
			
			//calls the extractVariables method, which returns an array of variables that have been parsed out of the model's catalog XML
			modelVariables = extractVariables(); 
			
			// creates an array of strings that contain the descriptions of the variables, which will then populate the spinner
			String[] modelVariableNames = new String[modelVariables.length];
			for(int i=0; i<modelVariables.length; i++)
				modelVariableNames[i] = modelVariables[i].getDescription();
			
			//update the spinner accordingly
			variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, modelVariableNames);
			productSelected = true;
		}
		else
		{
			variableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.variables_empty));
			productSelected = false;
		}
		
		/* (un)locks a lower level spinner once it is appropriate */
		if(!productSelected){
			spinMainVariables.setClickable(false);
		}else {
			spinMainVariables.setClickable(true);
		}
	}
	
	/* changes the starting time accordingly */
	public void changeTimeStart(){
		if(productSelected){
			//gets the valid times from the superclass
			timeStartAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, super.getValidTimes());
			startTimeSelected = true;
		}
		else {
			timeStartAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_start_empty));
			startTimeSelected = true;
		}
		/* (un)locks a lower level spinner once it is appropriate */
		if(!productSelected){
			spinMainVariables.setClickable(false);
		}else {
			spinMainVariables.setClickable(true);
		}
	}
	
	/* changes the ending time accordingly */
	public void changeTimeEnd(){
		/* creates a new array in which the first element is the first element after the selected element in starting time
		 * ex: starting time is selected at index 34 of 99 of the valid times array, then ending time array will contain indices 35-99 of starting time array
		 */
		if(startTimeSelected){
			String[] startTimes = super.getValidTimes();
			String[] endTimes = Arrays.copyOfRange(startTimes, spinMainTimeStart.getSelectedItemPosition()+1, startTimes.length);
			timeEndAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, endTimes);
		}else{
			timeEndAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.time_end_empty));
		}
		
		/* (un)locks a lower level spinner once it is appropriate */
		if(!startTimeSelected){
			spinMainVariables.setClickable(false);
		}else {
			spinMainVariables.setClickable(true);
		}
	}
	
	/* default android method, code is default */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/* Android onResume method, code from Google Maps API Example */
	@Override
	protected void onResume() {
	  super.onResume();
	  setUpMapIfNeeded();
	  setUpLocationClientIfNeeded();
	  myLocationClient.connect();
	}

	/* Android onPause method, code from Google Maps API Example */
	@Override
	public void onPause() {
	  super.onPause();
	  if (myLocationClient != null) {
	    myLocationClient.disconnect();
	  }
	}
	
	/* onTouch method that will hide the soft keyboard if the background of the app is clicked */
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

	/* method for the CurrentLocation button's onClick method in the activity_main.xml */
	public void showEnteredLocation(View view)	{
		if(myMarker != null)
			myMarker.remove();
		LatLng loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
		myMarker = mapMainLocation.addMarker(new MarkerOptions().position(loc)
													 .title("Location Entered"));
		mapMainLocation.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,(float) 9));
	}

	/* formats the URL and calls the DisplayActivity to display the information provided at that URL */
	public void callDisplayActivity(View view)
	{		
		/* formatting the URL */
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
		
		//creates the intent that will launch the DisplayActivity, and then launches it
		Intent i = new Intent(this, DisplayActivity.class);
		startActivity(i);
	}
	
	/* extractVariables method that parses out the variables from the XML */
	public Variable[] extractVariables()
	{
		//dummy loop that will keep this thread from continuing while the MyTask thread pulls in the XML from the network
		while(!UnidataSuperActivity.getDone()){}
		
		ArrayList<Variable> variables = new ArrayList<Variable>();
		//name, desc, and unit are the component variables of the Variable variable
		String name, desc, unit;
		
		//parses through the xml and gets the variable types that we are interested in
		String modelXml = super.getModelXML();
	    try
	    {
	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	factory.setNamespaceAware(false);
	        XmlPullParser xpp = factory.newPullParser();
	        
	        xpp.setInput(new StringReader (modelXml));
	        int eventType = xpp.getEventType();
	        
	        /* while loop that parses through the XML document */
	        while(eventType != XmlPullParser.END_DOCUMENT) {
	        	/* the AttributeValue are where the NCSS stores what the value itself is 
            	 * the null check has to be there, otherwise it will throw a NullPointerException if the attribute
            	 * does not exist in that particular tag
            	 */
	        	if(xpp.getDepth()==2 && xpp.getAttributeValue(null, "name")!=null && xpp.getAttributeValue(null, "name").equals("time")){
	        		/* moves the parser to the value of the date attribute and adds it to the times arraylist */
	        		xpp.nextTag();
	        		super.setValidTimes(xpp.getAttributeValue(null, "value").substring(11));
	        	}
	        	if(xpp.getDepth()==3 && xpp.getAttributeValue(null, "shape")!=null && xpp.getAttributeValue(null, "shape").equals("time y x")){
	        		/* moves the parser to the value of the date attribute and adds it to the times arraylist */
	        		name = xpp.getAttributeValue(null, "name");
	        		desc = xpp.getAttributeValue(null, "desc");
        			boolean keepGoing = true;
	            	xpp.nextTag();
	            	/* parses out the details of the variable that we are interested in and adds it to the arrayList of variables */
	            	while(xpp.getDepth()==4 && keepGoing)
	            	{
	            		if(xpp.getAttributeValue(null, "name").equals("units"))
	            		{
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
	    
	    //returns the variables that were just parsed out
	    return variables.toArray(new Variable[variables.size()]);
	}
	
	/* the MyTask thread that accesses the network to get the XML */
	private class MyTask extends AsyncTask<Void, Void, Void>{
		
		/* textResult will end up storing the XML */
		String textResult;
	    
	    @Override
	    protected Void doInBackground(Void... params) {
	    	/* SuperActivity's done variable tells the other thread when this one has finished */
	    	UnidataSuperActivity.setDone(false);
	    	
	    	/* Gets the URL address of the data wanted
	    	 * URL comes from the Super, which is set by the MainActivity
	    	 */
	        URL textUrl;
	        String address = UnidataSuperActivity.getModelURL();
	        
	        /* Converts the URL into a temporary XML file, which then is converted into a String
	         * Uses the Commons IO library for FileUtils
	         */
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
	        
	        /* sets the XML String in the super class so it can be parsed in the main thread
	         * and then lets the main thread know it has finished
	         */
	        UnidataSuperActivity.setModelXML(textResult);
	        UnidataSuperActivity.setDone(true);

	     return null;
	    }
	    
	    /* required method.. doesn't do anything in this instance that I know of */
	    @Override
	    protected void onPostExecute(Void result) {
	    	
	     super.onPostExecute(result);   
	    }
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
	/*END OF GOOGLE MAPS CODE */
}
