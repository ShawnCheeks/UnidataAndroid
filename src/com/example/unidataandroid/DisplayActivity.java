/** UnidataSuperActivity stores the variables that need to be exchanged between activities and threads
 * AChartEngine info: http://achartengine.org/ 
 * XMLPullParser info: http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
 */

package com.example.unidataandroid;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParserException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class DisplayActivity extends UnidataSuperActivity {

	  /** The main dataset for the chart, can hold multiple series */
	  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  /** The main renderer that includes all the renderers customizing a chart. */
	  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  /** The most recently added series. */
	  private XYSeries mCurrentSeries;
	  /** The most recently created renderer, customizing the current series. */
	  private XYSeriesRenderer mCurrentRenderer;
	  /** The chart view that displays the data. */
	  private GraphicalView mChartView;

	  /* Android onSaveInstanceState method, code from AChartEngine example */
	  @Override
	  protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    // save the current data, for instance when changing screen orientation
	    outState.putSerializable("dataset", mDataset);
	    outState.putSerializable("renderer", mRenderer);
	    outState.putSerializable("current_series", mCurrentSeries);
	    outState.putSerializable("current_renderer", mCurrentRenderer);
	  }

	  /* Android onRestoreInstanceState method, code from AChartEngine example */
	  @Override
	  protected void onRestoreInstanceState(Bundle savedState) {
	    super.onRestoreInstanceState(savedState);
	    // restore the current data, for instance when changing the screen orientation
	    mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
	    mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
	    mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
	    mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
	  }

	  /* The Android version of the "main" method */
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_display);
	    
	    String xml;
	    
	    /* times will store the time strings.  ex: 2013-08-01T00:00:00Z
	     * hours will store the converted "time since" the first entry
	     * values will store the model data itself that we're interested in
	     */
	    ArrayList<String> times = new ArrayList<String>();
		ArrayList<Integer> hours = new ArrayList<Integer>();
		ArrayList<Double> values = new ArrayList<Double>();
		String units = "";
		
		/* MyTask is the new thread that accesses the network
		 * In Android, network access must be done on a separate thread
		 * There is a way to override that requirement, but it is really, really, really frowned upon
		 */
		new MyTask().execute();
		
		/* sets the "done" variable to false so that the dummy loop will wait 
		 * until the MyTask thread sets the variable to true
		 * without this dummy while loop, the rest of this thread will continue on
		 * concurrently with the other thread, but it needs the info from that thread
		 * before it can do what it's supposed to do
		 * This probably isn't the best solution, but it worked, so I was happy with it
		 */
		super.setDone(false);
		while(!super.getDone()){}
		
		/* gets the XML string from the super class (that was put there by the MyTask thread */
		xml = super.getXML();
		
		/* now for the XMLPullParsing, which uses the string of XML you just got from the super class 
		 * A lot of this came from the example at http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html
		 * just changing what I'm looking for 
		 */
	    try
	    {
	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	factory.setNamespaceAware(false);
	        XmlPullParser xpp = factory.newPullParser();
	        
	        xpp.setInput(new StringReader (xml));
	        int eventType = xpp.getEventType();
	        
	        /* while loop that parses through the XML document */
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            if(eventType == XmlPullParser.START_TAG) {
	            	/* the AttributeValue are where the NCSS stores what the value itself is 
	            	 * the null check has to be there, otherwise it will throw a NullPointerException if the attribute
	            	 * does not exist in that particular tag
	            	 */
	            	if(xpp.getAttributeValue(null, "name") != null && xpp.getAttributeValue(null, "name").equals("date")){
	            		/* moves the parser to the value of the date attribute and adds it to the times arraylist */
	            		eventType = xpp.nextToken();
	            		xpp.getText();
	            		times.add(xpp.getText());
	            	} else if(xpp.getAttributeValue(null, "units")!=null && xpp.getAttributeValue(null, "name").equals(super.getSampleVariable().getName())){
	            		/* moves the parser to the value of the variable you want, based on the units associated with that variable */
	            		if(units.equals(""))
	            			units = super.getSampleVariable().getUnits();
	            		eventType = xpp.nextToken();
	            		xpp.getText();
	            		values.add(Double.parseDouble(xpp.getText()));
	            	}
	            }
	            eventType = xpp.nextToken();
	       }
	        
	        /* calls the formatTimes method, which.. well.. formats the arrayList of times */
	        hours = formatTimes(times);

	    }catch(XmlPullParserException e){
	    	e.printStackTrace();
	    }catch(IOException e){
	    	e.printStackTrace();
	    }

	    /** this is where we get into the charting section
	     * this part is mostly copied from examples from AChartEngine,
	     * just with some tweaks to get the appearance how I like
	     * for better info than I could ever give you, see the AChartEngine JavaDoc:
	     * http://achartengine.org/content/javadoc/index.html
	     */
	    // set some properties on the main renderer
	    mRenderer.setApplyBackgroundColor(true);
	    mRenderer.setBackgroundColor(Color.argb(255, 0, 0, 0));
	    mRenderer.setAxisTitleTextSize(40);
	    mRenderer.setChartTitleTextSize(50);
	    mRenderer.setLabelsTextSize(35);
	    mRenderer.setLegendTextSize(40);
	    mRenderer.setMargins(new int[] {150, 100, 0, 20 });
	    mRenderer.setZoomButtonsVisible(false);
	    mRenderer.setPointSize(10);
	    mRenderer.setShowGrid(true);
	    mRenderer.setGridColor(Color.argb(255,255,255,255));
	    mRenderer.setZoomRate((float) .8);
	    mRenderer.setChartTitle(super.getSampleVariable().getDescription());
	    mRenderer.setXTitle("Hours From Start");
	    mRenderer.setYTitle(super.getSampleVariable().getUnits());
	    
	    
	    String seriesTitle = super.getSampleVariable().getDescription();
        // create a new series of data
        XYSeries series = new XYSeries(seriesTitle);
        mDataset.addSeries(series);
        mCurrentSeries = series;
        // create a new renderer for the new series
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // set some renderer properties
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setDisplayChartValues(false);
        renderer.setChartValuesTextSize(30);
        renderer.setLineWidth(5);
        renderer.setShowLegendItem(false);
        renderer.setDisplayChartValuesDistance(10);
        renderer.setColor(Color.argb(255, 255, 255, 0));
        mCurrentRenderer = renderer;
        
        /* converts the arrayLists I've been using so far to regular arrays that can be used by AChartEngine*/
        Integer[] xs = hours.toArray(new Integer[hours.size()]);
        Double[] ys = values.toArray(new Double[values.size()]);
        
        /* adds the coordinate points to the series in AChartEngine */
        for(int i=0; i < xs.length; i++)
        {
        	mCurrentSeries.add(xs[i].intValue(), ys[i].doubleValue());
        }
	  }

	 /* Android method to reset everything if the app is minimized and then resumed
	  * Code from an AChartEngine example
	  */
	  @Override
	  protected void onResume() {
	    super.onResume();
	    if (mChartView == null) {
	      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
	      mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
	      // enable the chart click events
	      mRenderer.setClickEnabled(true);
	      mRenderer.setSelectableBuffer(10);
	      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
	          LayoutParams.FILL_PARENT));
	    } else {
	      mChartView.repaint();
	    }
	  }
	  
	  /* formatTimes method that will convert the time strings (ex: 2013-08-01T00:00:00Z)
	   * to Calendar objects, and then return an ArrayList of integers of time
	   * since the first time (0 hour being the first entry, then times since 0 hour) */
	  public ArrayList<Integer> formatTimes(ArrayList<String> times)
		{
		  	/* creating the calendar object */
			Calendar startTime = Calendar.getInstance();
			/* setting the start time calendar object based on the first string */
			startTime.set(Integer.parseInt(times.get(0).substring(0, 4)), 
						  Integer.parseInt(times.get(0).substring(5, 7)), 
						  Integer.parseInt(times.get(0).substring(8, 10)), 
						  Integer.parseInt(times.get(0).substring(11, 13)),
						  Integer.parseInt(times.get(0).substring(14, 16)),
					      Integer.parseInt(times.get(0).substring(17, 19)));
			
			/* the arraylist that will be returned by this method */
			ArrayList<Integer> timePassed = new ArrayList<Integer>();
			timePassed.add(0);
			
			/* now, convert each other time string to a calendar object and then calculate the time difference
			 * between this one and 0 hour
			 */
			for (int i=1; i<times.size(); i++)
			{
				Calendar thisTime = Calendar.getInstance();
				thisTime.set(Integer.parseInt(times.get(i).substring(0, 4)), 
						  	 Integer.parseInt(times.get(i).substring(5, 7)), 
						  	 Integer.parseInt(times.get(i).substring(8, 10)), 
						  	 Integer.parseInt(times.get(i).substring(11, 13)),
						  	 Integer.parseInt(times.get(i).substring(14, 16)),
						  	 Integer.parseInt(times.get(i).substring(17, 19)));
				
				int differenceHours = (int) ((thisTime.getTimeInMillis() - startTime.getTimeInMillis()) / 3600000);
				
				timePassed.add(differenceHours);
			}
			
			return timePassed;			
		}

	  /* Android onCreateOptionsMenu - default code */
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
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
		        String address = UnidataSuperActivity.getURL();
		        
		        /* Converts the URL into a temporary XML file, which then is converted into a String */
		        try {
		         textUrl = new URL(address);
		         System.out.println(textUrl);
		         File testFile2 = File.createTempFile("temp2", ".dat");
		         testFile2.deleteOnExit();
		         FileUtils.copyURLToFile(textUrl, testFile2);
		         textResult = FileUtils.readFileToString(testFile2);
		         
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
		        UnidataSuperActivity.setXML(textResult);
		        UnidataSuperActivity.setDone(true);

		     return null;
		     
		    }
		    
		    /* required method.. doesn't do anything in this instance that I know of */
		    @Override
		    protected void onPostExecute(Void result) {
		    	
		     super.onPostExecute(result);   
		    }
		}
		
		/* method called by the onClick (see XML file activity_display.xml) feature of the return button
		 * all it does is close this activity, which will take the user back to the MainActivity
		 * which will let them submit a new report
		 */
		public void closeDisplayActivity(View view)
		{
			finish();
		}
}
