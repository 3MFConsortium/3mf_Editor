package com.qualitylogic.x3mf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StartUp {

	static Log logger = LogFactory.getLog(Class.class);
	public static Boolean brunCli = false;
	public static Boolean brunVal = true;
	public static Boolean brunStruct = true;
	public static Boolean brunIfFail = false;
	public static Boolean bHideWarnCLI = false;
	public static Boolean bnoOverwrite = false;
	public static String oneFile = "";
	public static String fileList = "";

	public static void main(String[] args) {
		
		//See if a process is already running.
		if(SystemUtils.IS_OS_WINDOWS){
			try {
				String line;
				Process p =Runtime.getRuntime().exec(System.getenv("windir") 
						+"\\system32\\"+"tasklist.exe /nh /fi \"SessionName eq Console\" /fi \"Windowtitle eq 3MF Edit And Test\"");
				BufferedReader input =  new BufferedReader(new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					System.out.println(line);
					if(line.contains("java")){
						System.out.println("3MF Edit And Test already running, exiting.");
						System.exit(0);
					}
				}
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		//Setup the command line with a -O option style

		Options options = new Options();

		Option runCliOption = new Option("C", "runCli", false, "run as cli");
		Option oneFileOption = new Option("F", "oneFile", true, "evaluate a file");
		Option fileListOption = new Option("L", "fileList", true, "evaluate a list of files");
		Option runValOption = new Option("V", "validation", false, "run validation only");
		Option runStructOption = new Option("S", "structure", false, "run structural check only (overrides -V)");
		Option runIfFailOption = new Option("I", "ifFail", false, "run structural even if validation fails");
		Option noWarnOption = new Option("W", "hideWarn", false, "do not display warnings");
		Option noOverwriteOption = new Option("O", "noOverwrite", false, "do not overwrite results, use versioning");

		options.addOption(runCliOption);
		options.addOption(oneFileOption);
		options.addOption(fileListOption);
		options.addOption(runValOption);
		options.addOption(runStructOption);
		options.addOption(runIfFailOption);
		options.addOption(noWarnOption);
		options.addOption(noOverwriteOption);

		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption(runCliOption.getOpt())) {
				brunCli = true;
				logger.info("3MF to be run via command line as specified by: -C");
			}
			if (line.hasOption(oneFileOption.getOpt())) {
				oneFile = line.getOptionValue(oneFileOption.getOpt());
				logger.info("3MF file to be evaluated as specified by: -F = " + oneFile);
			}
			if (line.hasOption(fileListOption.getOpt())) {
				fileList = line.getOptionValue(fileListOption.getOpt());
				logger.info("3MF list of files to be evaluated as specified by: -L = " + fileList);
			}
			if (line.hasOption(runValOption.getOpt())) {
				brunStruct = false;
				logger.info("Only validation will be run as specified by: -V");
			}
			if (line.hasOption(runStructOption.getOpt())) {
				brunVal = false;
				logger.info("Only structural check will be run as specified by: -S");
				if(!brunStruct){
					brunStruct = true;
					logger.info("Validation only was set with -V, but canceled as specified by: -S");
				}
			}
			if (line.hasOption(runIfFailOption.getOpt())) {
				brunIfFail = true;
				logger.info("Failed validation will not stop structural check as specified by: -I");
			}
			if (line.hasOption(noWarnOption.getOpt())) {
				bHideWarnCLI = true;
				logger.info("Warnings hidden as specified by: -W");
			}
			if (line.hasOption(noOverwriteOption.getOpt())) {
				bnoOverwrite = true;
				logger.info("Overwriting disabled, using versioning as specified by: -O");
			}

		} catch (Exception e) {
			logger.error("usage exception", e);
			System.exit(-1);
		}

		System.setProperty("jdk.xml.maxOccurLimit", "20000");
		
		MainFrame mf = new MainFrame();
		mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mf.start();
	}

}
