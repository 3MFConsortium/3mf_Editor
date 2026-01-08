package com.qualitylogic.x3mf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
//import javax.swing.SwingUtilities; Cam Edit: Comment back in
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qualitylogic.x3mf.MainFrame.Initiater;

//Popup for right-click on a directory or file name in 3MF tree view

public class dirPop extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Log logger = LogFactory.getLog(Class.class);
	public JPopupMenu popup;
	public String fileN;
	public String zipIn;
	public boolean addDir;
	public boolean copyFile;
	public boolean topDir;
	public String copyFileName;
	public String copyFileText;
	public String cDir;
	public Initiater initiater;
	public List<KeyInfo> keyinfo = null;

	// Listen for context menu selection, the call appropriate method to execute
	public dirPop(){

		popup = new JPopupMenu();
		ActionListener menuListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				if(event.getActionCommand().contentEquals("Add Directory")){
					addDirEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("Rename Directory")){
					renDirEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("Delete Directory")){
					delDirEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("New File")){
					newFileEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("Rename File")){
					renFileEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("Delete File")){
					delFileEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("Copy File")){
					copyFileEx();

				}
				if(event.getActionCommand().contentEquals("Paste File")){
					pasteFileEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("Insert File")){
					insertFileEx();

					initiater.sayHello();
				}
				if(event.getActionCommand().contentEquals("Display Encrypted File")){
					displayEncrypted();

					initiater.sayHello();
				}
			}
		};
		JMenuItem item;
		//icon example
		popup.add(item = new JMenuItem("Rename Directory"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		popup.add(item = new JMenuItem("Add Directory"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		
		popup.add(item = new JMenuItem("Delete Directory"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		popup.add(item = new JMenuItem("New File"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		popup.add(item = new JMenuItem("Rename File"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		popup.add(item = new JMenuItem("Delete File"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		popup.add(item = new JMenuItem("Copy File"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		popup.add(item = new JMenuItem("Paste File"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		if(copyFileName == null || copyFileName.contentEquals("")) copyFile=false;


		popup.add(item = new JMenuItem("Insert File"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);


		popup.add(item = new JMenuItem("Display Encrypted File"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		

		popup.setLabel("Justification");
		popup.setBorder(new BevelBorder(BevelBorder.RAISED));
		popup.addPopupMenuListener(new PopupCloseListener());


	}
	
	// Add new directory
	private void addDirEx(){
		String newDir = JOptionPane.showInputDialog(null,"Enter directory name:",
				"Add new directory to " + fileN,JOptionPane.QUESTION_MESSAGE);
		if(newDir == null || newDir.contentEquals("")){
			addDir = false;
			return;
		}
		newDir = newDir.replaceAll("\\\\", "/");
		if(newDir.contentEquals("/")){
			JOptionPane.showMessageDialog(null, "Empty directory entered"    
					, "New Directory Error:", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		if(!newDir.substring(0, 1).contentEquals("/") && !(fileN == "")){
			newDir = "/" + newDir;
		}
		if(newDir.lastIndexOf("/") != newDir.length()-1){
			newDir = newDir + "/";
		}

		if(checkZipDir(zipIn,fileN + newDir)){
			JOptionPane.showMessageDialog(null, "Duplicate name."    
					, "Add Directory Error:", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		addDir = addZipDir(zipIn, fileN, newDir);
	}

	// Rename directory
	private void renDirEx(){
		String newDir = (String) JOptionPane.showInputDialog(null,"Enter new directory name:",
				"Rename directory in " + fileN,JOptionPane.QUESTION_MESSAGE,null,null,getName(fileN));
		if(newDir == null || newDir.contentEquals("")){
			addDir = false;
			return;
		}
		newDir = newDir.replaceAll("\\\\", "/");
		if(newDir.contains("/")){
			JOptionPane.showMessageDialog(null, "Rename directories one level at a time. Do not include a /"    
					, "Rename Directory Error:", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		String slash = "/";
		if (fileN == "") slash = "";
		String repName;
		if(fileN.contains("/")){
			repName = fileN.substring(0, fileN.lastIndexOf("/"));
		}else{
			repName = fileN;
		}
		if(checkZipDir(zipIn,repName + slash + newDir)){
			JOptionPane.showMessageDialog(null, "Duplicate name."    
					, "Rename Directory Error:", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		addDir = renameZipDir(zipIn, fileN, newDir);
	}

	// Rename File
	private void renFileEx(){
		String newDir = (String) JOptionPane.showInputDialog(null,"Enter new file name:",
				"Rename file for " + fileN,JOptionPane.QUESTION_MESSAGE,null,null,getName(fileN));
		if(newDir == null || newDir.contentEquals("")){
			addDir = false;
			return;
		}
		if(!vFilename(newDir)){
			JOptionPane.showMessageDialog(null, "Invalid slash character in filename."    
					, "Rename File Error:", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		String slash = "/";
		if (fileN == "") slash = "";
		String repName;
		if(fileN.contains("/")){
			repName = fileN.substring(0, fileN.lastIndexOf("/"));
		}else{
			repName = fileN;
		}
		if(checkZipFile(zipIn,repName + slash + newDir)){
			JOptionPane.showMessageDialog(null, "Duplicate name."    
					, "Rename File Error:", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		renameZipFile(zipIn, fileN, newDir);
	}

	// Add new file
	private void newFileEx(){
		String newDir = JOptionPane.showInputDialog(null,"Enter new filename:",
				"New file in " + fileN,JOptionPane.QUESTION_MESSAGE);
		if(newDir == null || newDir.contentEquals("")){
			addDir = false;
			return;
		}
		if(!vFilename(newDir)){
			JOptionPane.showMessageDialog(null, "Invalid slash character in filename."    
					, "New File Error:", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		String slash = "/";
		if (fileN == "") slash = "";
		while(checkZipFile(zipIn,fileN + slash + newDir)){
			if(newDir.contains(".")){
				newDir = newDir.substring(0,newDir.lastIndexOf(".")) + "-copy" 
						+ newDir.substring(newDir.lastIndexOf("."));
			}else{
				newDir +="-copy";
			}
		}
		newFileZip(zipIn, fileN, slash + newDir,"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> ");

	}

	// Insert file selected from local disk drive
	private void insertFileEx(){
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(cDir));
		fileChooser.setPreferredSize(new Dimension(600, 400));
		int result = fileChooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			addDir = false;
			return;
		}
		if(!fileChooser.getSelectedFile().exists()){
			JOptionPane.showMessageDialog(null, "The file " + fileChooser.getSelectedFile() + " does not exist.\n"   
			, "File I/O error: error finding file.", JOptionPane.ERROR_MESSAGE);
			addDir = false;
			return;
		}
		File f = fileChooser.getSelectedFile();
		String slash = "/";
		if (fileN == "") slash = "";
		fileN += slash;
		insertFileZip(zipIn, fileN, f);

	}

	// Paste previously copied file
	private void pasteFileEx(){
		String localName = getName(copyFileName);
		String slash = "/";
		if (fileN == "") slash = "";
		while(checkZipFile(zipIn,fileN + slash + localName)){
			if(copyFileName.contains(".")){
				localName = localName.substring(0,localName.lastIndexOf(".")) + "-copy" 
						+ localName.substring(localName.lastIndexOf("."));
			}else{
				localName +="-copy";
			}
		}
		copyFileZip(zipIn, fileN, slash + localName);
	}

	// Delete directory
	private void delDirEx(){
		int ix = JOptionPane.showConfirmDialog(null,"Are you sure you want to delete everything in " + fileN,
				"Deleting " + fileN,JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(ix == JOptionPane.OK_OPTION)
			addDir = deleteZipDir(zipIn, fileN);
	}
	
	//Delete File
	private void delFileEx(){
		int ix = JOptionPane.showConfirmDialog(null,"Delete " + fileN,
				"Deleting " + fileN,JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
		if(ix == JOptionPane.OK_OPTION)
			deleteZipFile(zipIn, fileN);
	}
	
	// Copy file in buffer
	private void copyFileEx(){
		copyFileName = fileN;

	}

	// An inner class to show when popup events occur
	class PopupCloseListener implements PopupMenuListener {
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}

		public void popupMenuCanceled(PopupMenuEvent e) {
		}
	}
	
	
	// Control which items are enabled on menu - general
	public void dirEnabled(Boolean enable, Boolean bmod){
		popup.getComponent(0).setEnabled(enable);
		if(topDir)popup.getComponent(0).setEnabled(false);
		popup.getComponent(1).setEnabled(enable);
		popup.getComponent(2).setEnabled(enable);
		if(topDir)popup.getComponent(2).setEnabled(false);
		popup.getComponent(3).setEnabled(enable);
		popup.getComponent(4).setEnabled(!enable);
		popup.getComponent(5).setEnabled(!enable);
		popup.getComponent(6).setEnabled(!enable);
		if(enable && !(copyFileName == null) && !(copyFileName.contentEquals(""))){
			popup.getComponent(7).setEnabled(true);
		}else{
			popup.getComponent(7).setEnabled(false);
		}
		popup.getComponent(8).setEnabled(enable);
		popup.getComponent(9).setEnabled(false);
	}
	
	// Control which items are enabled on menu - Encrypted File
	public void dirEnabled(Boolean enable, Boolean bmod, Boolean benc){
		popup.getComponent(0).setEnabled(enable);
		if(topDir)popup.getComponent(0).setEnabled(false);
		popup.getComponent(1).setEnabled(enable);
		popup.getComponent(2).setEnabled(enable);
		if(topDir)popup.getComponent(2).setEnabled(false);
		popup.getComponent(3).setEnabled(enable);
		popup.getComponent(4).setEnabled(!enable);
		popup.getComponent(5).setEnabled(!enable);
		popup.getComponent(6).setEnabled(!enable);
		if(enable && !(copyFileName == null) && !(copyFileName.contentEquals(""))){
			popup.getComponent(7).setEnabled(true);
		}else{
			popup.getComponent(7).setEnabled(false);
		}
		popup.getComponent(8).setEnabled(enable);
		popup.getComponent(9).setEnabled(benc);
	}

	// Update or insert new file.
	private void newFileZip(String szipFile, String parent, String fileName, String text){

		//Update the file in the ztmp directory and then put it back into the 3mf.
		try{
			//Save the text to the file in the ztmp directory
			FileUtils.writeStringToFile(new File("ztmp/" + parent + fileName), text, "UTF-8", false);
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " with " + parent + fileName   
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
		}finally{
		}
		return;
	}

	// Copy file into buffer from decompressed zip
	private void copyFileZip(String szipFile, String parent, String fileName){

		try{
			//Make a copy of copyFileName to the parent + fileName
			if(!new File("ztmp/" + copyFileName).exists()){
				JOptionPane.showMessageDialog(null, "Error copying file: " + copyFileName + " no longer exists."   
						, "File Copy error:", JOptionPane.ERROR_MESSAGE);
			}
			FileUtils.copyFile(new File("ztmp/" + copyFileName), new File("ztmp/" + parent + fileName) );
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " with " + parent + fileName   
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
		}finally{
		}
		return;
	}

	//Insert file in decompressed zip from local storage
	private void insertFileZip(String szipFile, String parent, File f){
		//insert a file that was selected via a dialog
		try{
			FileUtils.copyFileToDirectory(f, new File("ztmp/" + parent));
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " with " + parent + f.getName()   
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
		}finally{
		}
		return;
	}
	
	// Add directory to decompressed zip
	private boolean addZipDir(String szipFile, String parent, String fileName){
		//Create a directory of parent + fileName
		try{
			Path path = Paths.get("ztmp/" + parent + fileName);
			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					//fail to create directory
					JOptionPane.showMessageDialog(null, "Error creating " + parent + fileName + " directory" +  "\n" + e.getMessage()  
					+ "\n", "File I/O error: creating directory.", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return false;
				}
			}
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " with " + parent + fileName    
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
			return false;
		}finally{
		}
		return true;
	}

	// Rename directory in decompressed zip
	private boolean renameZipDir(String szipFile, String parent, String fileName){
		//Rename the directory to parent + fileName.
		String repName;
		if(parent.contains("/")){
			repName = parent.substring(0, parent.lastIndexOf("/") + 1);
			repName += fileName;
		} else {
			repName = fileName;
		}
		try{
			new File("ztmp/" + parent).renameTo(new File("ztmp/" + repName));
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " with " + repName    
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
			return false;
		}finally{
		}
		return true;
	}

	// Rename file in decompressed zip
	private void renameZipFile(String szipFile, String parent, String fileName){
		//Rename a file
		String repName;
		if(parent.contains("/")){
			repName = parent.substring(0, parent.lastIndexOf("/") + 1);
			repName += fileName;
		} else {
			repName = fileName;
		}
		try{
			new File("ztmp/" + parent).renameTo(new File("ztmp/" + repName));
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " with " + repName    
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
			return;
		}finally{
		}
		return;
	}

	// Delete directory in decompressed zip
	private boolean deleteZipDir(String szipFile, String parent){
		//Delete a directory
		if(!(parent.endsWith("/"))){
			parent +="/";
		}
		
		try{
			FileUtils.deleteDirectory(new File("ztmp/" + parent));
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " deleting " + parent    
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
			return false;
		}finally{
		}
		return true;
	}
	
	// Delete file in decompressed zip
	private void deleteZipFile(String szipFile, String parent){
		//Delete a file
		try{
			FileUtils.deleteQuietly(new File("ztmp/" + parent));
			File zipFile = new File(szipFile); 
			ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");			
		}catch(IOException ex){
			ex.printStackTrace(); 
			JOptionPane.showMessageDialog(null, "Error Updating file: " + szipFile + " deleting " + parent    
					+ "\n" + ex.getMessage(), "File Update error:", JOptionPane.ERROR_MESSAGE);
			return;
		}finally{
		}
		return;
	}
	
	// Transpose back slashes
	private boolean vFilename(String s){
		s = s.replaceAll("\\\\", "/");
		if(s.contains("/")){
			return false;
		}
		return true;
	}
	
	// Get file name event if full path passed in
	private String getName(String infile){
		String repName;
		if(infile.contains("/")){
			repName = infile.substring(infile.lastIndexOf("/") + 1);
		} else {
			repName = infile;
		}
		return repName;

	}
	
	//Verify file exists in decompressed zip
	private boolean checkZipFile(String szipFile, String parent){
		//Does file exist.
		boolean bOut = false;
		if(new File("ztmp/" + parent).exists()) bOut = true;
		return bOut;
	}
	
	//Verify directory exists in decompressed zip
	private boolean checkZipDir(String szipFile, String parent){
		//Does directory exist
		boolean bOut = false;
		if(new File("ztmp/" + parent).exists()) bOut = true;
		return bOut;
	}
	
	// Display encrypted file
	private void displayEncrypted(){
		try{
			if(copyFileText.length() < 2) copyFileText = "Failed to decrypt.";
			//display the text
			final JTextArea edit = new JTextArea(30, 60);
			edit.setText(copyFileText);
			final JFrame frame = new JFrame("/" + fileN);
			frame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/resources/qe.png")));

			frame.getContentPane().add( new JScrollPane(edit), BorderLayout.CENTER );
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setExtendedState(JFrame.NORMAL);
	        frame.pack();
	        frame.setLocationRelativeTo( null );
	        frame.setVisible(true);				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}		
}

