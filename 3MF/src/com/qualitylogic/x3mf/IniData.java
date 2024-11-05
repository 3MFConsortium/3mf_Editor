package com.qualitylogic.x3mf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Read and write INI file. The INI file persists some runtime states between sessions
//Default values below are used if no INI exists

public class IniData {

	static Log logger = LogFactory.getLog(Class.class);
	public float psclWid = 0.45f;
	public float psclHgt = 0.66f;
	public int pscrLocX = 50;
	public int pscrLocY = 50;
	public int pscrSzHgt = 600;
	public int pscrSzWid = 800;
	public boolean pbSchemaChk = true;
	public boolean pbwire = false;
	public boolean pbnumb = false;
	public boolean pbmesh = false;
	public boolean pNoUUIDchk = true;
	public boolean bdoScroll = true;
	public boolean bdoWarn = true;
	public String lastFile = "";
	public String thumbDir = "";
	public String transSize = "30150.0, 219850.0, 33700.9, 366300.0, 5000.0, 252800.0";

	public IniData() {
	}
	public void readIni(){
		try {
			String workingDir = System.getProperty("user.dir");
			logger.info("Current working directory : " + workingDir);
			File f3mfIni=new File("3MF.ini");
			if(f3mfIni.exists()){
				logger.info("Found " + f3mfIni.getAbsolutePath().toString() + " to override settings.");
				FileReader fro = new FileReader("3MF.ini" );
				BufferedReader bro = new BufferedReader( fro );
				String stringFromFile = bro.readLine( );
				String overRide = "";
				//read in the file and drop the comments
				if( stringFromFile != null && !stringFromFile.trim().startsWith("#")) overRide += stringFromFile;
				while( stringFromFile != null ) // end of the file
				{
					stringFromFile = bro.readLine( );  // read next line
					if(stringFromFile != null && !stringFromFile.trim().startsWith("#")) overRide += stringFromFile;
				}
				String tmpString;
				bro.close( );
				fro.close();
				int ib;
				int ie;
				ib=overRide.indexOf("psclWid");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						psclWid = Float.parseFloat(tmpString);
					}catch (Exception e){
						logger.info("Failed to load psclWid with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("psclHgt");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						psclHgt = Float.parseFloat(tmpString);
					}catch (Exception e){
						logger.info("Failed to load psclHgt with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pscrLocX");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pscrLocX = Integer.parseInt(tmpString.trim());
					}catch (Exception e){
						logger.info("Failed to load pscrLocX with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pscrLocY");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pscrLocY = Integer.parseInt(tmpString.trim());
					}catch (Exception e){
						logger.info("Failed to load pscrLocY with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pscrSzHgt");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pscrSzHgt = Integer.parseInt(tmpString.trim());
					}catch (Exception e){
						logger.info("Failed to load pscrSzHgt with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pscrSzWid");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pscrSzWid = Integer.parseInt(tmpString.trim());
					}catch (Exception e){
						logger.info("Failed to load pscrSzWid with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pbSchemaChk");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pbSchemaChk = Boolean.parseBoolean(tmpString);
					}catch (Exception e){
						logger.info("Failed to load pbSchemaChk with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pbwire");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pbwire = Boolean.parseBoolean(tmpString);
					}catch (Exception e){
						logger.info("Failed to load pbwire with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pbnumb");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pbnumb = Boolean.parseBoolean(tmpString);
					}catch (Exception e){
						logger.info("Failed to load pbnumb with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pbmesh");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pbmesh = Boolean.parseBoolean(tmpString);
					}catch (Exception e){
						logger.info("Failed to load pbmesh with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("pNoUUIDchk");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						pNoUUIDchk = Boolean.parseBoolean(tmpString);
					}catch (Exception e){
						logger.info("Failed to load pNoUUIDchk with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("bdoScroll");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						bdoScroll = Boolean.parseBoolean(tmpString);
					}catch (Exception e){
						logger.info("Failed to load bdoScroll with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("bdoWarn");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					tmpString=overRide.substring(ib+1, ie);
					try{
						bdoWarn = Boolean.parseBoolean(tmpString);
					}catch (Exception e){
						logger.info("Failed to load bdoWarn with " + tmpString + "\nDue to: " + e.getMessage());
					}
				}
				ib=overRide.indexOf("lastFile");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					lastFile=overRide.substring(ib+1, ie);
				}
				ib=overRide.indexOf("thumbDir");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					thumbDir=overRide.substring(ib+1, ie);
				}
				ib=overRide.indexOf("transSize");
				if (ib != -1){
					ib=overRide.indexOf("\"",ib);
					ie=overRide.indexOf("\"",ib+1);
					transSize=overRide.substring(ib+1, ie);
				}
			} else {
				logger.info("3MF.ini not found to override settings.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void writeIni(){
		try {
			File f3mfIni=new File("3MF.ini");
			String el = System.getProperty("line.separator");
			String overRide = "";
			if(f3mfIni.exists()){
				logger.info("Found " + f3mfIni.getAbsolutePath().toString() + " to save override settings.");
				//read in the file to get the comments first.
				FileReader fro = new FileReader("3MF.ini" );
				BufferedReader bro = new BufferedReader( fro );
				String stringFromFile = bro.readLine( );
				if( stringFromFile != null && stringFromFile.trim().startsWith("#")) overRide += stringFromFile + el;
				while( stringFromFile != null ) // end of the file
				{
					stringFromFile = bro.readLine( );  // read next line
					if(stringFromFile != null && stringFromFile.trim().startsWith("#")) overRide += stringFromFile + el;
				}
				bro.close();
				fro.close();
			}
			if(overRide.contentEquals(""))overRide = "# 3MF initialization" + el;

			FileWriter fw = new FileWriter("3MF.ini");
			fw.write(overRide);
			overRide = "psclWid = \"" + Float.toString(psclWid) + "\"" + el;
			fw.write(overRide);
			overRide = "psclHgt = \"" + Float.toString(psclHgt) + "\"" + el;
			fw.write(overRide);
			overRide = "pscrLocX = \"" + Integer.toString(pscrLocX) + "\"" + el;
			fw.write(overRide);
			overRide = "pscrLocY = \"" + Integer.toString(pscrLocY) + "\"" + el;
			fw.write(overRide);
			overRide = "pscrSzHgt = \"" + Integer.toString(pscrSzHgt) + "\"" + el;
			fw.write(overRide);
			overRide = "pscrSzWid = \"" + Integer.toString(pscrSzWid) + "\"" + el;
			fw.write(overRide);
			String sTrue = "false";
			if (pbSchemaChk) sTrue="true";
			overRide = "pbSchemaChk = \"" + sTrue + "\"" + el;
			fw.write(overRide);
			sTrue = "false";
			if (pbwire) sTrue="true";
			overRide = "pbwire = \"" + sTrue + "\"" + el;
			fw.write(overRide);
			sTrue = "false";
			if (bdoScroll) sTrue="true";
			overRide = "bdoScroll = \"" + sTrue + "\"" + el;
			fw.write(overRide);
			sTrue = "false";
			if (bdoWarn) sTrue="true";
			overRide = "bdoWarn = \"" + sTrue + "\"" + el;
			fw.write(overRide);
			overRide = "lastFile = \"" + lastFile + "\"" + el;
			fw.write(overRide);
			overRide = "thumbDir = \"" + thumbDir + "\"" + el;
			fw.write(overRide);
			overRide = "transSize = \"" + transSize + "\"" + el;
			fw.write(overRide);
			sTrue = "false";
			if (pbnumb) sTrue="true";
			overRide = "pbnumb = \"" + sTrue + "\"" + el;
			fw.write(overRide);
			sTrue = "false";
			if (pbmesh) sTrue="true";
			overRide = "pbmesh = \"" + sTrue + "\"" + el;
			sTrue = "false";
			if (pNoUUIDchk) sTrue="true";
			overRide = "pNoUUIDchk = \"" + sTrue + "\"" + el;
			fw.write(overRide);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
