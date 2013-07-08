package com.example.unidataandroid;

import java.io.IOException;
import java.io.InputStream;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DisplayActivity extends Activity {

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
	  
	  static final String URL = "http://api.androidhive.info/pizza/?format=xml";
	    // XML node keys
	    static final String KEY_ITEM = "item"; // parent node
	    static final String KEY_NAME = "name";
	    static final String KEY_COST = "cost";
	    static final String KEY_DESC = "description";

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
	    
	 // All static variables
	    	     
	    XMLParser parser = new XMLParser();
	    String xml = parser.getXmlFromUrl(URL); // getting XML
	    Document doc = parser.getDomElement(xml); // getting DOM element
	     
	    NodeList nl = doc.getElementsByTagName(KEY_ITEM);
	     
	    // looping through all item nodes <item>      
	    for (int i = 0; i < nl.getLength(); i++) {
	    	Element e = (Element)nl.item(i);
	        String name = parser.getValue(e, KEY_NAME); // name child value
	        String cost = parser.getValue(e, KEY_COST); // cost child value
	        String description = parser.getValue(e, KEY_DESC); // description child value
			System.out.println(name + " " + cost + " " + description);
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
        
        double[] xs = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
        double[] ys = {83,84,85,86,86,85,83,81,78,76,73,71,70,68,67,66,65,64,65,69,73,78,83,86};
        
        for(int i=0; i < xs.length; i++)
        {
        	mCurrentSeries.add(xs[i], ys[i]);
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
}
