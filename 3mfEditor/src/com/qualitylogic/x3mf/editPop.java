package com.qualitylogic.x3mf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qualitylogic.x3mf.MainFrame.Initiater;

//Popup for right-click in XML text

public class editPop extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Log logger = LogFactory.getLog(Class.class);
	public JPopupMenu popup;
	public Initiater initiater;

	public editPop(){

		//Populates menu items in popup, calls appropriate routine for selected item
		popup = new JPopupMenu();
		ActionListener menuListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(event.getActionCommand().contentEquals("Cut")){
					initiater.doEdit("Cut");
				}
				if(event.getActionCommand().contentEquals("Copy")){
					initiater.doEdit("Copy");
				}
				if(event.getActionCommand().contentEquals("Paste")){
					initiater.doEdit("Paste");
				}
				if(event.getActionCommand().contentEquals("Select All")){
					initiater.doEdit("Select All");
				}
				if(event.getActionCommand().contentEquals("Find")){
					initiater.doEdit("Find");
				}
				if(event.getActionCommand().contentEquals("Find Again")){
					initiater.doEdit("Find Again");
				}
				if(event.getActionCommand().contentEquals("Undo")){
					initiater.doEdit("Undo");
				}
				if(event.getActionCommand().contentEquals("Redo")){
					initiater.doEdit("Redo");
				}
				if(event.getActionCommand().contentEquals("Validate XML")){
					initiater.doEdit("Validate XML");
				}
			}
		};
		JMenuItem item;
		popup.add(item = new JMenuItem("Cut"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		item.setEnabled(false);
		popup.add(item = new JMenuItem("Copy"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.add(item = new JMenuItem("Paste"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.add(item = new JMenuItem("Select All"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.add(item = new JMenuItem("Find"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.add(item = new JMenuItem("Find Again"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.add(item = new JMenuItem("Undo"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.add(item = new JMenuItem("Redo"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		popup.add(item = new JMenuItem("Validate XML"));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);


		popup.setLabel("Justification");
		popup.setBorder(new BevelBorder(BevelBorder.RAISED));
		popup.addPopupMenuListener(new PopupCloseListener());


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
}

