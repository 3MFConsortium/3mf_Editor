package com.qualitylogic.x3mf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyData {

	static Log logger = LogFactory.getLog(Class.class);
	public String pcurrentDir = "/";
	private boolean bcurrentDir = false;
	public String pschemaLoc = "/";
	private boolean bschemaLoc = false;
	public String pticketLoc = "";
	public String pprinterUrl = "";
	public String psns= "http://schemas.microsoft.com/3dmanufacturing/slice/2015/07";
	public String ppns= "http://schemas.microsoft.com/3dmanufacturing/production/2015/06";
	public String ppindent= "4";
	private boolean bppindent = false;
	public int iErrMaxStructure = 9;
	private boolean biErrMaxStructure = false;
	public int iErrMaxValidataion = 9;
	private boolean biErrMaxValidation = false;
	public boolean bStructureChkWerrors = false;
	private boolean bschkwerrors = false;
	public String hVersion = "1.0"; 
	public String sValx = "MWEyMjRjMWE3Y2Q3NGE4M2E1ZmNiZWY5MjMxNDQyYjM6NTNlOWEyZGUxMjQ4NGMwZjhiNDRkMTM5Y2RiOTEyODU=";
	public Double bedBuffer = 0.1;
	public String alignOrigin = "origin";
	public Boolean align_out_of_bounds_only = false;
	public String username = "none";
	public String password = "none";
	

	public PropertyData() {
	}
	public void readTxt(){
		try {
			String workingDir = System.getProperty("user.dir");
			logger.info("Current working directory : " + workingDir);
			File f3mfTxt=new File("3MF.txt");
			if(f3mfTxt.exists()){
				logger.info("Found " + f3mfTxt.getAbsolutePath().toString() + " for property settings.");
				FileReader fro = new FileReader("3MF.txt" );
				BufferedReader bro = new BufferedReader( fro );
				String stringFromFile = "";
				//read in the file and drop the comments
				String tmpString;
				int ib;
				int ie;
				while( stringFromFile != null ) // end of the file
				{
					stringFromFile = bro.readLine( );  // read next line
					if(stringFromFile == null)break;
					if(stringFromFile.trim().startsWith("#"))continue;
					if(stringFromFile.trim().startsWith("pcurrentDir ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							pcurrentDir  = tmpString;
							pcurrentDir = pcurrentDir.replaceAll("\\\\", "/");
							if(!Files.exists(Paths.get(pcurrentDir))){
								pcurrentDir ="/";
								logger.info("Failed to load pcurrentDir with " + tmpString + "\nDue to the path not being found.");
							}
						}catch (Exception e){
							logger.info("Failed to load pcurrentDir with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
					//pschemaLoc
					if(stringFromFile.trim().startsWith("pschemaLoc ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							pschemaLoc  = tmpString;
							pschemaLoc = pschemaLoc.replaceAll("\\\\", "/");
							if(pschemaLoc != "" && !Files.exists(Paths.get(pschemaLoc))){
								pschemaLoc ="";
								logger.info("Failed to load pschemaLoc with " + tmpString + "\nDue to the path not being found.");
							}
						}catch (Exception e){
							logger.info("Failed to load pschemaLoc with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
					if(stringFromFile.trim().startsWith("pticketLoc ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							pticketLoc  = tmpString;
							pticketLoc = pticketLoc.replaceAll("\\\\", "/");
							if(pticketLoc != "" && !Files.exists(Paths.get(pticketLoc))){
								pticketLoc ="";
								logger.info("Failed to load pticketLoc with " + tmpString + "\nDue to the path not being found.");
								JOptionPane.showMessageDialog(null, "Info: Could not find " + tmpString + " for api printing, creating and using default jobticket.xml from 3MF Edit jar file."   
										, "Info: Using default job ticket.", JOptionPane.INFORMATION_MESSAGE);
								try {
								} catch (Exception e) {
									JOptionPane.showMessageDialog(null, "Error: Could not write " + pticketLoc
											+ " after extracting default jobticket from the 3MF Edit jar file." + e.getMessage()   
											, "File I/O error: creating job ticket.", JOptionPane.ERROR_MESSAGE);
								}
							}
						}catch (Exception e){
							logger.info("Failed to load pticketLoc with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
					if(stringFromFile.trim().startsWith("pprinterUrl ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						pprinterUrl  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("hVersion ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						hVersion  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("sValx ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						sValx  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("alignOrigin ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						alignOrigin  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("username ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						username  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("password ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						password  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("psns ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						psns  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("ppns ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						ppns  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("ppindent ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						ppindent  = tmpString;
						continue;
					}
					if(stringFromFile.trim().startsWith("iErrMaxStructure ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							iErrMaxStructure = Integer.parseInt(tmpString.trim());
						}catch (Exception e){
							logger.info("Failed to load iErrMaxStructure with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
					if(stringFromFile.trim().startsWith("iErrMaxValidataion ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							iErrMaxValidataion = Integer.parseInt(tmpString.trim());
						}catch (Exception e){
							logger.info("Failed to load iErrMaxValidataion with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
					if(stringFromFile.trim().startsWith("bedBuffer ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							bedBuffer = Double.parseDouble(tmpString.trim());
						}catch (Exception e){
							logger.info("Failed to load bedBuffer with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
					if(stringFromFile.trim().startsWith("bStructureChkWerrors ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							bStructureChkWerrors = Boolean.parseBoolean(tmpString);
						}catch (Exception e){
							logger.info("Failed to load bStructureChkWerrors with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
					if(stringFromFile.trim().startsWith("align_out_of_bounds_only ")){
						ib=stringFromFile.indexOf("\"");
						ie=stringFromFile.indexOf("\"",ib+1);
						if(ib == -1 || ie == -1){
							logger.info("Error, missing quote(s) in: " + stringFromFile);
							continue;
						}
						tmpString=stringFromFile.substring(ib+1, ie);
						try{
							align_out_of_bounds_only = Boolean.parseBoolean(tmpString);
						}catch (Exception e){
							logger.info("Failed to load align_out_of_bounds_only with " + tmpString + "\nDue to: " + e.getMessage());
						}
						continue;
					}
				}
				bro.close( );
				fro.close();
			} else {
				logger.info("3MF.txt not found for settings.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void writeTxt(){
		try {
			bcurrentDir = false;
			bschemaLoc = false;
			bppindent=false;
			biErrMaxStructure=false;
			biErrMaxValidation=false;
			bschkwerrors = false;
			File f3mfTxt=new File("3MF.txt");
			if(!f3mfTxt.exists()){
				f3mfTxt.createNewFile();
			}
			logger.info("Found " + f3mfTxt.getAbsolutePath().toString() + " to save property settings.");
			String el = System.getProperty("line.separator");
			FileReader fro = new FileReader("3MF.txt" );
			BufferedReader bro = new BufferedReader( fro );
			String stringFromFile = "";
			String saveFile = "";
			int ib;
			int ie;
			while( stringFromFile != null ) // end of the file
			{
				stringFromFile = bro.readLine( );  // read next line
				if(stringFromFile == null)break;
				if(stringFromFile.trim().startsWith("#")){ 
					saveFile += stringFromFile + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("pcurrentDir ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + pcurrentDir + stringFromFile.substring(ie) + el;
					bcurrentDir=true;
					continue;
				}
				if(stringFromFile.trim().startsWith("pschemaLoc ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + pschemaLoc + stringFromFile.substring(ie) + el;
					bschemaLoc=true;
					continue;
				}
				if(stringFromFile.trim().startsWith("pticketLoc ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + pticketLoc + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("pprinterUrl ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + pprinterUrl + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("hVersion ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + hVersion + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("sValx ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + sValx + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("alignOrigin ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + alignOrigin + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("username ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + username + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("password ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + password + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("psns ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					continue;
				}
				if(stringFromFile.trim().startsWith("ppns ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					continue;
				}
				if(stringFromFile.trim().startsWith("ppindent ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + ppindent + stringFromFile.substring(ie) + el;
					bppindent=true;
					continue;
				}
				if(stringFromFile.trim().startsWith("iErrMaxStructure ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + Integer.toString(iErrMaxStructure) + stringFromFile.substring(ie) + el;
					biErrMaxStructure=true;
					continue;
				}
				if(stringFromFile.trim().startsWith("iErrMaxValidataion ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + Integer.toString(iErrMaxValidataion) + stringFromFile.substring(ie) + el;
					biErrMaxValidation=true;
					continue;
				}
				if(stringFromFile.trim().startsWith("bedBuffer ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					saveFile += stringFromFile.substring(0, ib+1) + bedBuffer.toString() + stringFromFile.substring(ie) + el;
					continue;
				}
				if(stringFromFile.trim().startsWith("bStructureChkWerrors ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					String sTrue = "false";
					if (bStructureChkWerrors) sTrue="true";
					saveFile += stringFromFile.substring(0, ib+1) + sTrue + stringFromFile.substring(ie) + el;
					bschkwerrors=true;
					continue;
				}
				if(stringFromFile.trim().startsWith("align_out_of_bounds_only ")){
					ib=stringFromFile.indexOf("\"");
					ie=stringFromFile.indexOf("\"",ib+1);
					if(ib == -1 || ie == -1){
						logger.info("Error, missing quote(s) in: " + stringFromFile);
						continue;
					}
					String sTrue = "false";
					if (align_out_of_bounds_only) sTrue="true";
					saveFile += stringFromFile.substring(0, ib+1) + sTrue + stringFromFile.substring(ie) + el;
					continue;
				}
				//write out the lines we do not understand
				saveFile += stringFromFile + el;
			}
			if(!bcurrentDir){
				saveFile +="# Use RELOAD PROPERTIES FILE menu option after editing this file"  + el;
				saveFile +="# Working directory"  + el;
				saveFile += "   pcurrentDir = \"" + pcurrentDir + "\"" + el;
			}
			if(!bschemaLoc){
				saveFile +="# Schema location"  + el;
				saveFile += "   pschemaLoc = \"" + pschemaLoc + "\"" + el;
			}
			if(!bppindent){
				saveFile +="# Indents used for pretty Printing"  + el;
				saveFile += "   ppindent = \"" + ppindent + "\"" + el;
			}
			if(!biErrMaxStructure){
				saveFile +="# Maximum errors reported for 3MF file structure validation"  + el;
				saveFile += "   iErrMaxStructure = \"" + Integer.toString(iErrMaxStructure) + "\"" + el;
			}
			if(!biErrMaxValidation){
				saveFile +="# Maximum errors for reported for 3MF schema validation"  + el;
				saveFile += "   iErrMaxValidataion = \"" + Integer.toString(iErrMaxValidataion) + "\"" + el;
			}
			if(!bschkwerrors){
				saveFile +="# If true, do structure checks even if there are schema validation errors"  + el;
				String sTrue = "false";
				if (bStructureChkWerrors) sTrue="true";
				saveFile += "   bStructureChkWerrors = \"" + sTrue + "\"" + el;
			}
			bro.close();
			fro.close();
			FileWriter fw = new FileWriter("3MF.txt");
			fw.write(saveFile);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
