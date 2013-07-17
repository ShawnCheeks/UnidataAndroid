package com.example.unidataandroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.xmlpull.v1.XmlPullParserException;

import com.example.unidataandroid.XMLParser.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Xml;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayActivity extends UnidataSuperActivity {

	/** The main dataset that includes all the series that go into a chart. */
	  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  /** The main renderer that includes all the renderers customizing a chart. */
	  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  /** The most recently added series. */
	  private XYSeries mCurrentSeries;
	  /** The most recently created renderer, customizing the current series. */
	  private XYSeriesRenderer mCurrentRenderer;
	  /** Button for creating a new series of data. */
	  private Button mNewSeries;
	  /** The chart view that displays the data. */
	  private GraphicalView mChartView;

	  @Override
	  protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    // save the current data, for instance when changing screen orientation
	    outState.putSerializable("dataset", mDataset);
	    outState.putSerializable("renderer", mRenderer);
	    outState.putSerializable("current_series", mCurrentSeries);
	    outState.putSerializable("current_renderer", mCurrentRenderer);
	  }

	  @Override
	  protected void onRestoreInstanceState(Bundle savedState) {
	    super.onRestoreInstanceState(savedState);
	    // restore the current data, for instance when changing the screen
	    // orientation
	    mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
	    mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
	    mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
	    mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
	  }

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_display);
	    
	    String xml;
	    
	    ArrayList<String> times = new ArrayList<String>();
		ArrayList<Integer> hours = new ArrayList<Integer>();
		ArrayList<Double> values = new ArrayList<Double>();
		String units = "";
		
		UnidataSuperActivity.setURL("http://thredds.ucar.edu/thredds/ncss/grid/grib/NCEP/GFS/CONUS_80km/best?var=Temperature_height_above_ground&latitude=40&longitude=-105&time_start=2013-07-08T00%3A00%3A00Z&time_end=2013-07-10T00%3A00%3A00Z&vertCoord=&accept=xml");
		new MyTask().execute();
		
		while(!super.getDone()){}
		
		xml = super.getXML();
	    try
	    {
	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	factory.setNamespaceAware(false);
	        XmlPullParser xpp = factory.newPullParser();

	        xpp.setInput(new StringReader (xml));
	        int eventType = xpp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            if(eventType == XmlPullParser.START_TAG) {
	            	if(xpp.getAttributeValue(null, "name") != null && xpp.getAttributeValue(null, "name").equals("date")){
	            		eventType = xpp.nextToken();
	            		xpp.getText();
	            		times.add(xpp.getText());
	            	} else if(xpp.getAttributeValue(null, "units")!=null && xpp.getAttributeValue(null, "units").equals("K")){
	            		if(units.equals(""))
	            			units = xpp.getAttributeValue(null, "units");
	            		eventType = xpp.nextToken();
	            		xpp.getText();
	            		values.add(Double.parseDouble(xpp.getText()));
	            	}
	            }
	            eventType = xpp.nextToken();
	       }
	        
	        hours = formatTimes(times);

	    }catch(XmlPullParserException e){
	    	e.printStackTrace();
	    }catch(IOException e){
	    	e.printStackTrace();
	    }

	    // set some properties on the main renderer
	    mRenderer.setApplyBackgroundColor(true);
	    mRenderer.setBackgroundColor(Color.argb(255, 0, 0, 0));
	    mRenderer.setAxisTitleTextSize(40);
	    mRenderer.setChartTitleTextSize(40);
	    mRenderer.setLabelsTextSize(40);
	    mRenderer.setLegendTextSize(40);
	    mRenderer.setMargins(new int[] {20, 20, 50, 30 });
	    mRenderer.setZoomButtonsVisible(false);
	    mRenderer.setPointSize(15);
	    mRenderer.setShowGrid(true);
	    mRenderer.setGridColor(Color.argb(255,255,255,255));
	    mRenderer.setZoomRate((float) .8);
	    
	    String seriesTitle = "Temperature";
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
        renderer.setDisplayChartValues(true);
        renderer.setDisplayChartValuesDistance(10);
        renderer.setColor(Color.argb(255, 255, 255, 0));
        mCurrentRenderer = renderer;
        
        Integer[] xs = hours.toArray(new Integer[hours.size()]);
        Double[] ys = values.toArray(new Double[values.size()]);
        
        for(int i=0; i < xs.length; i++)
        {
        	mCurrentSeries.add(xs[i].intValue(), ys[i].doubleValue());
        }
	  }

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
	  
	  public ArrayList<Integer> formatTimes(ArrayList<String> times)
		{
			Calendar startTime = Calendar.getInstance();
			startTime.set(Integer.parseInt(times.get(0).substring(0, 4)), 
						  Integer.parseInt(times.get(0).substring(5, 7)), 
						  Integer.parseInt(times.get(0).substring(8, 10)), 
						  Integer.parseInt(times.get(0).substring(11, 13)),
						  Integer.parseInt(times.get(0).substring(14, 16)),
					      Integer.parseInt(times.get(0).substring(17, 19)));
			
//			System.out.println(times.get(0).substring(0, 4));
//			System.out.println(times.get(0).substring(5, 7));
//			System.out.println(times.get(0).substring(8, 10));
//			System.out.println(times.get(0).substring(11, 13));
//			System.out.println(times.get(0).substring(14, 16));
//			System.out.println(times.get(0).substring(17, 19));
			
			ArrayList<Integer> timePassed = new ArrayList<Integer>();
			timePassed.add(0);
			
			for (int i=1; i<times.size(); i++)
			{
				Calendar thisTime = Calendar.getInstance();
				thisTime.set(Integer.parseInt(times.get(i).substring(0, 4)), 
						  	 Integer.parseInt(times.get(i).substring(5, 7)), 
						  	 Integer.parseInt(times.get(i).substring(8, 10)), 
						  	 Integer.parseInt(times.get(i).substring(11, 13)),
						  	 Integer.parseInt(times.get(i).substring(14, 16)),
						  	 Integer.parseInt(times.get(i).substring(17, 19)));
				
//				System.out.println(times.get(i).substring(0, 4));
//				System.out.println(times.get(i).substring(5, 7));
//				System.out.println(times.get(i).substring(8, 10));
//				System.out.println(times.get(i).substring(11, 13));
//				System.out.println(times.get(i).substring(14, 16));
//				System.out.println(times.get(i).substring(17, 19));
				
//				System.out.println(thisTime.getTimeInMillis() + "---" + startTime.getTimeInMillis());
				int differenceHours = (int) ((thisTime.getTimeInMillis() - startTime.getTimeInMillis()) / 3600000);
				
				timePassed.add(differenceHours);
			}
			
			return timePassed;
			
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}

		private class MyTask extends AsyncTask<Void, Void, Void>{
			
			String textResult;
			
		    
		    @Override
		    protected Void doInBackground(Void... params) {
		    	UnidataSuperActivity.setDone(false);
		    	
		        URL textUrl;
		        String address = UnidataSuperActivity.getURL();
		        try {
		         textUrl = new URL(address);

		         BufferedReader bufferReader 
		          = new BufferedReader(new InputStreamReader(textUrl.openStream()));
		         
		         String StringBuffer;
		         String stringText = "";
		         while ((StringBuffer = bufferReader.readLine()) != null) {
		          stringText += StringBuffer;   
		         }
		         bufferReader.close();

		         textResult = stringText;
		         
		        } catch (MalformedURLException e) {
		         e.printStackTrace();
		         textResult = e.toString();   
		        } catch (IOException e) {
		         e.printStackTrace();
		         textResult = e.toString();   
		        }
		        
		        UnidataSuperActivity.setXML(textResult);
		        UnidataSuperActivity.setDone(true);

		     return null;
		     
		    }
		    
		    @Override
		    protected void onPostExecute(Void result) {
		    	
		     super.onPostExecute(result);   
		    }
		}
}
