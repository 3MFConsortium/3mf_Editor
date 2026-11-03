package com.qualitylogic.x3mf;

import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.io.IOException; 
import java.io.InputStream; 
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode; 
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Zip File Tools
 *  
 * @author Luxh 
 */ 
public class ZipFileUtil { 

	/**
	 * Zip files into zip format
	 *
	 * @param files
	 * Files to be compressed
	 * @param zipFilePath
	 * Compressed zip file path, such as "D: /test/aa.zip";
	 */ 
	public static boolean compressFile(String srcPath, String desPath, String desName, String base, String drop) throws IOException { 
		File srcFile = new File(srcPath); 
		if(!srcFile.exists()){ 
			System.out.println(srcPath+"source file does not exist!\r\n"); 
			return false; 
		} 
		if ("".equalsIgnoreCase(desName)) { 
			desName = srcFile.getName(); 
		} 
		ZipArchiveOutputStream zipOutputStream = null; 
		try{ 
			zipOutputStream = new ZipArchiveOutputStream(new FileOutputStream(desPath + "/" + desName)); 
			zipOutputStream.setUseZip64(Zip64Mode.AsNeeded); 

			compressFiles2ZipBase(srcFile, zipOutputStream, base, drop); 
		}finally{ 
			if(zipOutputStream!=null){ 
				zipOutputStream.close(); 
				zipOutputStream = null; 
			} 
		} 
		return true; 
	} 

	public static void compressFiles2ZipBase(File file, ZipArchiveOutputStream zaos, String base, String drop) throws IOException { 
		if (file != null) { 
			if(base.equals("")){ 
				base = file.getName(); 
			}else{ 
				base = base + File.separator + file.getName(); 
			}
			if(file.isDirectory()){ 
				File[] listOfFiles = file.listFiles();
				if(listOfFiles.length > 0){
					Arrays.sort(listOfFiles, new Comparator<File>() {
						private final Comparator<String> NATURAL_SORT = new WindowsExplorerComparator();

						@Override
						public int compare(File o1, File o2) {;
						return NATURAL_SORT.compare(o1.getName(), o2.getName());
						}
					});
					for(File f:listOfFiles){ 
						compressFiles2ZipBase(f, zaos, base, drop); 
					} 
				} else {
					//add the empty directory
					if(!drop.equals("")){
						base = base.substring(drop.length());
					}
					ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, base); 
					zaos.putArchiveEntry(zipArchiveEntry); 
					zaos.closeArchiveEntry();
				}
			}else{ 
				if(!drop.equals("")){
					base = base.substring(drop.length());
				}
				ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, base); 
				zaos.putArchiveEntry(zipArchiveEntry); 
				InputStream in = new FileInputStream(file);
				IOUtils.copy(in, zaos);
				zaos.closeArchiveEntry();
				in.close();
			} 
		} 
	} 
	public static class WindowsExplorerComparator implements Comparator<String> {

		private static final Pattern splitPattern = Pattern.compile("\\d+|\\.|\\s");

		@Override
		public int compare(String str1, String str2) {
			if (str1.startsWith("_") != str2.startsWith("_")) {
				return str1.startsWith("_") ? -1 : 1;
			}
			Iterator<String> i1 = splitStringPreserveDelimiter(str1).iterator();
			Iterator<String> i2 = splitStringPreserveDelimiter(str2).iterator();
			while (true) {
				//Til here all is equal.
				if (!i1.hasNext() && !i2.hasNext()) {
					return 0;
				}
				//first has no more parts -> comes first
				if (!i1.hasNext() && i2.hasNext()) {
					return -1;
				}
				//first has more parts than i2 -> comes after
				if (i1.hasNext() && !i2.hasNext()) {
					return 1;
				}

				String data1 = i1.next();
				String data2 = i2.next();
				int result;
				try {
					//If both datas are numbers, then compare numbers
					result = new BigInteger(data1).compareTo(new BigInteger(data2));
					//If numbers are equal than longer comes first
					if (result == 0) {
						result = -Integer.compare(data1.length(), data2.length());
					}
				} catch (NumberFormatException ex) {
					//compare text case insensitive
					result = data1.compareToIgnoreCase(data2);
				}

				if (result != 0) {
					return result;
				}
			}
		}

		private List<String> splitStringPreserveDelimiter(String str) {
			Matcher matcher = splitPattern.matcher(str);
			List<String> list = new ArrayList<String>();
			int pos = 0;
			while (matcher.find()) {
				list.add(str.substring(pos, matcher.start()));
				list.add(matcher.group());
				pos = matcher.end();
			}
			list.add(str.substring(pos));
			return list;
		}
	}

	//Switched away from this method for better read-only access in the second.

	/**
	 �* Unzip the zip file to the specified folder
	��*
	��* @param zipFilePath
	��* Zip file path, such as "D: /test/aa.zip"
	��* @param saveFileDir
	��* Extract the file storage path, such as "D: / test /"
	 */ 
	private static File extractionDestination(File targetDir, String name) throws IOException {
		// Treat both ZIP and Windows separators as path separators, on every platform.
		String normalized = name.replace('\\', '/');
		if (normalized.startsWith("/") || normalized.indexOf(':') >= 0) {
			throw new IOException("Unsafe archive entry: " + name);
		}
		File root = targetDir.getCanonicalFile();
		File destination = new File(root, normalized).getCanonicalFile();
		if (!destination.toPath().startsWith(root.toPath()) || destination.equals(root)) {
			throw new IOException("Archive entry escapes extraction directory: " + name);
		}
		return destination;
	}

	public static void decompressZipAlt(String zipFilePath, String saveFileDir) { 
		if (isEndsWithZip(zipFilePath)) { 
			File file = new File(zipFilePath); 
			if (file.exists()) {
				InputStream is = null; 
				// can read Zip archives 
				ZipArchiveInputStream zais = null; 
				try { 
					is = new FileInputStream(file);
					zais = new ZipArchiveInputStream(is,null,true,true); 
					ArchiveEntry archiveEntry = null; 
					// Zip package to read out each file
					//Then write the file to the specified folder
					while ((archiveEntry = zais.getNextEntry()) != null) { 
						if (archiveEntry.isDirectory()) {
							File curfile = extractionDestination(new File(saveFileDir), archiveEntry.getName());
							File parent = curfile.getParentFile();
							if (!parent.exists()) {
								parent.mkdirs();
							}
							if(!curfile.exists()){
								Files.createDirectories(curfile.toPath());
							}
							continue;
						}
						File curfile = extractionDestination(new File(saveFileDir), archiveEntry.getName());
						File parent = curfile.getParentFile();
						if (!parent.exists()) {
							parent.mkdirs();
						}
						try (OutputStream out = new FileOutputStream(curfile)) {
							IOUtils.copy(zais, out);
						}

					} 
				} catch (Exception e) { 
					e.printStackTrace();
					//throw new RuntimeException(e);
				} finally { 
					try { 
						if (zais != null) { 
							zais.close(); 
						} 
						if (is != null) { 
							is.close(); 
						} 
					} catch (IOException e) { 
						throw new RuntimeException(e); 
					} 
				} 
			} 
		} 
	} 

	/**
	 �* Unzip the zip file to the specified folder
	��*
	��* @param zipFilePath
	��* Zip file path, such as "D: /test/aa.zip"
	��* @param saveFileDir
	��* Extract the file storage path, such as "D: / test /"
	 */ 
	public static void decompressZip(String zipFilePath, String saveFileDir) { 
		File zipFile = new File(zipFilePath);
		File targetDir = new File(saveFileDir);
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile.getAbsoluteFile());
			final Enumeration<ZipArchiveEntry> entries = zip.getEntries();
			while(entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					File curfile = extractionDestination(targetDir, entry.getName());
					File parent = curfile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					if(!curfile.exists()){
						Files.createDirectories(curfile.toPath());
					}
					continue;
				}
				final File entryDestination = extractionDestination(targetDir, entry.getName());
				File parent = entryDestination.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				if(!entryDestination.exists()){
					Files.createFile(entryDestination.toPath());
				}
				try (InputStream in = zip.getInputStream(entry);
						OutputStream out = new FileOutputStream(entryDestination)) {
					IOUtils.copy(in, out);
				}
				//make sure everything is writable
				entryDestination.setWritable(true);
			}
		} catch (IOException e) {
			System.out.println("Caught an error with the primary decompress, trying the alternate.");
			if (zip!= null) try {
				zip.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			decompressZipAlt(zipFilePath, saveFileDir);
		} finally {
			if (zip!= null) try {
				zip.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} 

	/**
	 �* Unzip a File in the zip file to the specified folder
	��*
	��* @param zipFilePath
	��* Zip file path, such as "D: /test/aa.zip"
	��* @param saveFileDir
	��* Extract the file storage path, such as "D: / test /"
	��* @param file
	��* Extract the file, such as "_rels/.rels"
	 */ 
	public static void decompressZipFile(String zipFilePath, String saveFileDir, String file) { 
		File zipFile = new File(zipFilePath);
		File targetDir = new File(saveFileDir);
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile.getAbsoluteFile());
			final Enumeration<ZipArchiveEntry> entries = zip.getEntries();
			while(entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				if (!entry.getName().equals(file)) {
					continue;
				}
				final File entryDestination = extractionDestination(targetDir, entry.getName());
				File parent = entryDestination.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				if(!entryDestination.exists()){
					Files.createFile(entryDestination.toPath());
				}
				try (InputStream in = zip.getInputStream(entry);
						OutputStream out = new FileOutputStream(entryDestination)) {
					IOUtils.copy(in, out);
				}
				//make sure everything is writable
				entryDestination.setWritable(true);
				return;
			}
		} catch (IOException e) {
			System.out.println("Caught an error with the decompress.");
			//throw new RuntimeException(e);
		} finally {
			if (zip!= null) try {
				zip.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} 


	/**
��* Determine whether the file name to. Zip as the suffix
��*
��* @param fileName
��* Need to judge the file name
��* @return is a zip file returns true, otherwise returns false
	 */ 
	public static boolean isEndsWithZip(String fileName) { 
		boolean flag = false; 
		if (fileName != null && !"".equals(fileName.trim())) { 
			if (fileName.endsWith(".ZIP") || fileName.endsWith(".zip") || fileName.toLowerCase().endsWith(".3mf")) { 
				flag = true; 
			} 
		} 
		return flag; 
	} 


}
