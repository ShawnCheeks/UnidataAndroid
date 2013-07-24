package com.example.unidataandroid;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.support.v4.app.FragmentActivity;

public class UnidataSuperActivity extends FragmentActivity {

		private static String URL = "";
		private static String XML = "";
		private static String modelURL = "";
		private static String modelXML = "";
		private static Variable sampleVariable;
		private static String[] validTimes = new String[99];
		private static boolean DONE = false;
		
		/*
		 * Variable URL
		 */
		public static String getURL()
		{
			return URL;
		}
		
		public static void setURL(String url)
		{
			URL = url;
		}
		/*
		 * Variable XML
		 */
		public static String getXML()
		{
			return XML;
		}
		
		public static void setXML(String xml)
		{
			XML = xml;
		}
		/*
		 * Model URL
		 */
		public static String getModelURL()
		{
			return modelURL;
		}
		
		public static void setModelURL(String url)
		{
			modelURL = url;
		}
		/*
		 * Model XML
		 */
		public static String getModelXML()
		{
			return modelXML;
		}
		
		public static void setModelXML(String xml)
		{
			modelXML = xml;
		}
		/*
		 * Variable Type
		 */
		public static Variable getSampleVariable()
		{
			return sampleVariable;
		}
		
		public static void setSampleVariable(Variable var)
		{
			sampleVariable = var;
		}
		
		/*
		 * Array that contains all of the possible times
		 */
		public static String[] getValidTimes()
		{
			return validTimes;
		}
		
		public static void setValidTimes(String firstTime)
		{
			validTimes[0] = firstTime;
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:'00:00Z'");
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
			
			Calendar startTime = Calendar.getInstance();
			startTime.set(Integer.parseInt(validTimes[0].substring(0, 4)), 
						  Integer.parseInt(validTimes[0].substring(5, 7)), 
						  Integer.parseInt(validTimes[0].substring(8, 10)), 
						  Integer.parseInt(validTimes[0].substring(11, 13)),
						  Integer.parseInt(validTimes[0].substring(14, 16)),
					      Integer.parseInt(validTimes[0].substring(17, 19)));
			
			for(int i=1; i<validTimes.length; i++)
			{
				startTime.add(Calendar.HOUR, 6);
				System.out.println(startTime.get(Calendar.YEAR) + "\n" +
								   startTime.get(Calendar.MONTH) + "\n" +
								   startTime.get(Calendar.DAY_OF_MONTH) + "\n" +
								   startTime.get(Calendar.HOUR_OF_DAY) + "\n" +
								   startTime.get(Calendar.MINUTE) + "\n" +
								   startTime.get(Calendar.SECOND));
				validTimes[i] = dateFormat.format(startTime.getTime());
				System.out.println(validTimes[i]);
			}
		}
		
		/*
		 * Conditional 'Are we done yet?' variable
		 */
		public static boolean getDone()
		{
			return DONE;
		}
		
		public static void setDone(boolean done)
		{
			DONE = done;
		}
		
		
	}