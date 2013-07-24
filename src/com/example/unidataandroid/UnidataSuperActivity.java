package com.example.unidataandroid;

import android.support.v4.app.FragmentActivity;

public class UnidataSuperActivity extends FragmentActivity {

		private static String URL = "";
		private static String XML = "";
		private static String modelURL = "";
		private static String modelXML = "";
		private static Variable sampleVariable;
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