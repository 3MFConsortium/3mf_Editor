package com.qualitylogic.x3mf;
import java.awt.AWTException;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.PrivateKey;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Transform3D;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.RootPaneContainer;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Point3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


import com.qualitylogic.x3mf.ZipFileUtil.WindowsExplorerComparator;

public class MainFrame extends JFrame implements ActionListener, DocumentListener {
	/**
	 * 
	 */
	static Log logger = LogFactory.getLog(Class.class);
	private static final long serialVersionUID = 1L;
	private JMenuItem exitI,cutI,copyI,findI,findaI,pasteI,selectI,
	saveI,saveAsI,getSchI,	closeI,loadI,lBakI,sBakI,unDoI, reDoI,upFontI,downFontI,viewFileI,
	pPrntI,chkSchI,chkSchZipI,chk3MfI,viewPropI,editPropI,doStructureI,	zip64I,helpI,aboutI;
	public String currentFile = null;
	public String currentDir = "/";
	public String mainFile = "";
	private String eleSearch = "";
	private File selectedFile;
	private JTree tree;
	private boolean zipdir = false;
	public String lastFile;
	public String thumbDir = "";
	public float sclWid = 0.45f;
	public float sclHgt = 0.66f;
	public int iSep = 10;
	public int scrLocX = 50;
	public int scrLocY = 50;
	public int scrSzHgt = 600;
	public int scrSzWid = 800;
	private boolean bChkError = false;
	private boolean bStructError = false;
	private int iErrMaxS = 9;
	private int iErrMaxV = 9;
	private boolean bSChkWerrors = false;
	private String sFind;
	private dirPop dPop = new dirPop();
	private editPop ePop = new editPop();
	private IniData inDat = new IniData();
	private PropertyData propDat = new PropertyData();
	private FileStructureCheck nfsc = new FileStructureCheck();
	private boolean xmlChanged = false;
	private boolean xmlCancel = false;
	private boolean bSetIcon;
	private String schemaLoc;
	private String ticketLoc;
	private String printerUrl;
	private boolean bSchemaChk;
	private boolean bSchemaFail;
	private String ppindent="4";
	private String sns;
	private String pns;
	private ActionListener undoAction;
	private ActionListener redoAction;
	private ActionListener clearUndoAction;
	private static String saveForConsole;
	public UndoManager undoManager=new UndoManager();
	private DefaultMutableTreeNode lastNode;
	public NodeData nd = new NodeData();
	public SpringLayout springLayout = new SpringLayout();
	public SpringLayout spl = new SpringLayout();
	public JScrollPane mfPane = new JScrollPane(getTree());
	public JEditorPane xmlEd = new XmlTextPane();
	private Initiater initiater = new Initiater();
	private Responder responder = new Responder();
	private Icon iClosed = new DefaultTreeCellRenderer().getClosedIcon();
	private List<SAXParseException> xsdErrorList = null;
	private List<SAXParseException> xsdErrorListRm = null;
	private Double dTrans = 0.0d;
	private String transSize = "33700.9, 366300.0, 30150.0, 219850.0, 5000.0, 252800.0";
	private Boolean bnotBatch = true;
	private Double bedBuffer = 0.1;
	private String alignOrigin = "origin";
	private Boolean align_out_of_bounds_only = false;
	private Boolean badjustMod = false;
	private Boolean bscalePass = false, badjFile = false, bnoChange = false;
	private String noChangePath = "";
	private int iadj=0, iscl = 0;
	private static String cusername = "none";
	private static String cpassword = "none";
	private String srel0 = "rel0";
	
	private List<KeyInfo> keyinfo = null;
	
	private Map<String, String> contentType = new HashMap<String, String>();

	private String fxmlSchemaReturn = "";
	private String fxmlErrorReturn = "";
	private String fxmlWarningReturn = "";
	private String sErr = "";

	public JCheckBox doScroll = new JCheckBox("Auto Scroll ");
	private boolean bdoScroll = true;
	public JCheckBox doWarn = new JCheckBox("Hide Warnings ");
	private boolean bdoWarn = true;
	public JCheckBox showAlpha = new JCheckBox("Show Alpha ");
	public String hVersion = "1.0";
	public String sValx = "MWEyMjRjMWE3Y2Q3NGE4M2E1ZmNiZWY5MjMxNDQyYjM6NTNlOWEyZGUxMjQ4NGMwZjhiNDRkMTM5Y2RiOTEyODU=";
	private Boolean bSkipBig = false;

	public JPanel img_ref = new JPanel(new BorderLayout(0, 0));
	private ImagePanel ibufRef = new ImagePanel();
	public JPanel img_alp = new JPanel(new BorderLayout(0, 0));
	private ImagePanel ibufAlp = new ImagePanel();

	JLabel xmlStat = new JLabel("");
	public DefaultCaret xmlcaret = (DefaultCaret)xmlEd.getCaret();
	public JTextArea txtStat = new JTextArea();
	public static JEditorPane txtConsole = new JEditorPane();

	public JScrollPane img_refPane = new JScrollPane(img_ref,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	public JScrollPane img_alpPane = new JScrollPane(img_alp,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	public JScrollPane statPane = new JScrollPane(txtStat,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	public JScrollPane xmlPane = new JScrollPane(xmlEd,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	public JScrollPane consolePane = new JScrollPane(txtConsole,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	public JButton btnCH = new JButton("");
	public JButton btnCl = new JButton("Clear");
	public JCheckBox doSchema = new JCheckBox("Validate on save");
	public static JCheckBox bwire = new JCheckBox("Wireframe ");
	public static JCheckBox bnumb = new JCheckBox("Triangle #s ");
	public static JCheckBox bmesh = new JCheckBox("Mesh Wire ");
	public static JCheckBox bNoUUIDchk = new JCheckBox("Ignore UUID ");
	public MainFrame() {
		keyinfo = new ArrayList<KeyInfo>();
		xsdErrorList = new ArrayList<>(); 
		xsdErrorListRm = new ArrayList<>(); 
		txtConsole.setEditable(false);
		txtConsole.setContentType("text/html");
		txtConsole.setText("");
		txtConsole.addHyperlinkListener(new HyperlinkListener(){
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType()== HyperlinkEvent.EventType.ACTIVATED){
					int offset = Integer.parseInt(e.getDescription().toString().trim());
					gotoStartOfLine(xmlEd, offset);
					xmlEd.requestFocusInWindow();
				}
			}});

		getContentPane().setBackground(new Color(102, 205, 170));
		doScroll.setBackground(new Color(102, 205, 170));
		doWarn.setBackground(new Color(102, 205, 170));
		showAlpha.setBackground(new Color(102, 205, 170));
		showAlpha.setToolTipText("Display the alpha channel as grayscale with black transparent and white opaque.");
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/resources/qe.png")));
		setTitle("3MF Edit and Test");
		getContentPane().setLayout(springLayout);
		mfPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		getContentPane().add(mfPane);
		getContentPane().add(xmlPane);
		getContentPane().add(statPane);
		getContentPane().add(consolePane);
		getContentPane().add(img_refPane);
		getContentPane().add(img_alpPane);
		getContentPane().add(doScroll);
		getContentPane().add(doWarn);
		getContentPane().add(showAlpha);
		showAlpha.setVisible(false);

		img_ref.add(ibufRef);
		img_refPane.setVisible(false);
		img_refPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		img_alp.add(ibufAlp);
		img_alpPane.setVisible(false);
		img_alpPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		xmlEd.getDocument().addDocumentListener(this);
		xmlEd.getDocument().addUndoableEditListener(undoManager);
		undoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undoManager.undo();
			}
		};
		redoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undoManager.redo();
			}
		};
		clearUndoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undoManager.clearUndo();
			}
		};
		xmlEd.registerKeyboardAction(undoAction, KeyStroke.getKeyStroke(
				KeyEvent.VK_Z, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		xmlEd.registerKeyboardAction(redoAction, KeyStroke.getKeyStroke(
				KeyEvent.VK_Y, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
		xmlEd.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				xmlcaret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
				if (e.getButton() == MouseEvent.NOBUTTON) {
				} else if (e.getButton() == MouseEvent.BUTTON1) {
				} else if (e.getButton() == MouseEvent.BUTTON2) {
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					ePop.initiater = initiater;
					Point pos = e.getPoint();
					
					//Blanks out options that are not currently available, then displays popup menu0
					if(xmlEd.getSelectedText() == null) {
						ePop.popup.getComponent(0).setEnabled(false);
						ePop.popup.getComponent(1).setEnabled(false);
					} else {
						ePop.popup.getComponent(0).setEnabled(true);
						ePop.popup.getComponent(1).setEnabled(true);
					}
					if(!undoManager.canUndo()) {
						ePop.popup.getComponent(6).setEnabled(false);
					} else {
						ePop.popup.getComponent(6).setEnabled(true);
					}
					if(!undoManager.canRedo()) {
						ePop.popup.getComponent(7).setEnabled(false);
					} else {
						ePop.popup.getComponent(7).setEnabled(true);
					}
					ePop.popup.show(xmlEd, pos.x, pos.y);
					
				}
			}
		});

		xmlEd.addCaretListener(new CaretListener() {
			// Each time the caret is moved, it will trigger the listener and its method caretUpdate.
			// It will then pass the event to the update method including the source of the event (which is our textarea control)
			public void caretUpdate(CaretEvent e) {

				int linenum = getRow(e.getDot(), (JTextComponent)e.getSource());

				int columnnum = getColumn(e.getDot(), (JTextComponent)e.getSource());

				updateStatus(linenum, columnnum);
			}
		});

		//added this caret change to defeat the autoscroll in editor so that
		//I could set the scroll in the scroll pane.
		AdjustmentListener listener = new MyAdjustmentListener();
		xmlPane.getHorizontalScrollBar().addAdjustmentListener(listener);
		xmlPane.getVerticalScrollBar().addAdjustmentListener(listener);
		//Validate schema check box
		getContentPane().add(doSchema);
		doSchema.setOpaque(false);
		doSchema.setFocusPainted( false );
		doSchema.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(doSchema.isSelected())
					bSchemaChk=true;
				else
					bSchemaChk=false;
			}
		});

		getContentPane().add(bNoUUIDchk);
		bNoUUIDchk.setOpaque(false);
		bNoUUIDchk.setFocusPainted( false );
		bNoUUIDchk.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			}
		});

		getContentPane().add(btnCH);
		getContentPane().add(btnCl);
		btnCH.setPreferredSize(new Dimension(iSep, iSep));
		btnCH.setBackground(Color.GREEN);
		btnCH.setCursor(new Cursor(Cursor.MOVE_CURSOR));

		getContentPane().add(xmlStat);

		//This is the button in the middle of the 4 panels that allow us to resize the panels.
		//It is a component I can drag around and use to resize 
		btnCH.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				String txt = txtStat.getText();
				if (txt == null || txt.length()==0){
					txt=" ";
				}
				txtStat.setText(txt);
				float mv = 0.01f;
				if(e.getX() < -2){
					sclWid = sclWid - (mv * sclWid);
					if(sclWid < 0.1f) sclWid = 0.1f;
				} else if (e.getX() > 2){
					sclWid = sclWid + (mv * sclWid);
					if(sclWid > 0.9f) sclWid = 0.9f;
				}
				if(e.getY() < -2){
					sclHgt = sclHgt - (mv * sclHgt);
					if(sclHgt < 0.1f) sclHgt = 0.1f;
				} else if (e.getY() > 2){
					sclHgt = sclHgt + (mv * sclHgt);
					if(sclHgt > 0.9f) sclHgt = 0.9f;
				}

				setupFrame();
				getContentPane().validate();
				getContentPane().repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}
		});
		//The menu items
		cutI = new JMenuItem("Cut");
		cutI.setMnemonic('u');
		copyI = new JMenuItem("Copy");
		copyI.setMnemonic('C');
		findI = new JMenuItem("Find");
		findI.setMnemonic('F');
		findaI = new JMenuItem("Find Again");
		findaI.setMnemonic('A');
		pasteI = new JMenuItem("Paste");
		pasteI.setMnemonic('P');
		selectI = new JMenuItem("Select All");
		selectI.setMnemonic('S');
		saveI = new JMenuItem("Save");
		saveI.setMnemonic('S');
		getSchI = new JMenuItem("Set Schema Location");
		getSchI.setMnemonic('e');		
		saveAsI = new JMenuItem("Save As");
		saveAsI.setMnemonic('A');
		closeI = new JMenuItem("Close");
		closeI.setMnemonic('C');
		loadI = new JMenuItem("Load");
		loadI.setMnemonic('L');
		sBakI = new JMenuItem("Update Backup");
		sBakI.setMnemonic('U');
		lBakI = new JMenuItem("Load Backup");
		lBakI.setMnemonic('B');
		exitI = new JMenuItem("Exit");
		exitI.setMnemonic('x');
		upFontI = new JMenuItem("Increase Font");
		upFontI.setMnemonic('I');
		unDoI = new JMenuItem("Undo");
		unDoI.setMnemonic('U');
		reDoI = new JMenuItem("ReDo");
		reDoI.setMnemonic('R');
		downFontI = new JMenuItem("Decrease Font");
		downFontI.setMnemonic('D');
		viewFileI = new JMenuItem("View 3MF");
		viewFileI.setMnemonic('V');
		viewPropI = new JMenuItem("Edit Properties File");
		viewPropI.setMnemonic('E');
		editPropI = new JMenuItem("Reload Properties File");
		editPropI.setMnemonic('R');
		doStructureI = new JMenuItem("Structure Check Override Off");
		doStructureI.setMnemonic('r');
		pPrntI = new JMenuItem("PrettyPrint XML in editor");
		pPrntI.setMnemonic('P');
		chkSchI = new JMenuItem("Schema Check, Part Being Edited");
		chkSchI.setMnemonic('a');
		chkSchZipI = new JMenuItem("Schema Check, All Parts");
		chkSchZipI.setMnemonic('c');
		chk3MfI = new JMenuItem("Validate Package, Schema and Structure");
		chk3MfI.setMnemonic('k');
		helpI = new JMenuItem("User's Guide");
		helpI.setMnemonic('U');
		aboutI = new JMenuItem("About");
		aboutI.setMnemonic('A');
		zip64I = new JMenuItem("Save 3mf to zip64");
		zip64I.setMnemonic('Z');

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
		setJMenuBar(menuBar);

		JMenu fileM = new JMenu("File");
		fileM.setHorizontalAlignment(SwingConstants.LEFT);
		fileM.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileM);

		fileM.add(loadI);
		fileM.add(saveI);
		fileM.add(saveAsI);
		fileM.add(closeI);
		fileM.add(sBakI);
		fileM.add(lBakI);
		fileM.add(getSchI);
		fileM.add(doStructureI);
		fileM.add(exitI);

		JMenu mntmEdit = new JMenu("Edit");
		mntmEdit.setHorizontalAlignment(SwingConstants.LEFT);
		mntmEdit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(mntmEdit);

		mntmEdit.add(cutI);
		mntmEdit.add(copyI);
		mntmEdit.add(pasteI);        
		mntmEdit.add(selectI);
		mntmEdit.add(findI);
		mntmEdit.add(findaI);
		mntmEdit.add(unDoI);
		mntmEdit.add(reDoI);

		JMenu mntmView = new JMenu("View");
		mntmView.setHorizontalAlignment(SwingConstants.LEFT);
		mntmView.setMnemonic(KeyEvent.VK_V);
		menuBar.add(mntmView);
		mntmView.add(upFontI);
		mntmView.add(downFontI);

		JMenu mntmTools = new JMenu("Tools");
		mntmTools.setHorizontalAlignment(SwingConstants.LEFT);
		mntmTools.setMnemonic(KeyEvent.VK_T);
		menuBar.add(mntmTools);
		mntmTools.add(viewFileI);
		mntmTools.add(viewPropI);
		mntmTools.add(editPropI);
		mntmTools.add(pPrntI);
		mntmTools.add(chkSchI);
		mntmTools.add(chkSchZipI);
		mntmTools.add(chk3MfI);
		zip64I.setEnabled(false);
		zip64I.setVisible(false);

		JMenu mntmHelp = new JMenu("Help");
		mntmHelp.setHorizontalAlignment(SwingConstants.LEFT);
		mntmHelp.setMnemonic(KeyEvent.VK_H);
		mntmHelp.add(helpI);
		mntmHelp.add(aboutI);
		menuBar.add(mntmHelp);

		//setup the keystroke shortcuts
		helpI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		saveI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveAsI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		chkSchI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		chkSchZipI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		chk3MfI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		closeI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		loadI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		sBakI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		lBakI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		exitI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		cutI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		copyI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		findI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		findaI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		pasteI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		selectI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		unDoI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		reDoI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		upFontI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, ActionEvent.CTRL_MASK));
		downFontI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, ActionEvent.CTRL_MASK));
		viewFileI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, ActionEvent.CTRL_MASK));
		viewPropI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		editPropI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		pPrntI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		
		

		//add the menu items to the listener
		aboutI.addActionListener(this);
		helpI.addActionListener(this);
		saveI.addActionListener(this);
		saveAsI.addActionListener(this);
		closeI.addActionListener(this);
		loadI.addActionListener(this);
		sBakI.addActionListener(this);
		lBakI.addActionListener(this);
		getSchI.addActionListener(this);
		exitI.addActionListener(this);
		cutI.addActionListener(this);
		copyI.addActionListener(this);
		findI.addActionListener(this);
		findaI.addActionListener(this);
		pasteI.addActionListener(this);
		selectI.addActionListener(this);
		unDoI.addActionListener(this);
		reDoI.addActionListener(this);
		upFontI.addActionListener(this);
		downFontI.addActionListener(this);
		viewFileI.addActionListener(this);
		viewPropI.addActionListener(this);
		editPropI.addActionListener(this);
		zip64I.addActionListener(this);
		doStructureI.addActionListener(this);
		pPrntI.addActionListener(this);
		chkSchI.addActionListener(this);
		chkSchZipI.addActionListener(this);
		chk3MfI.addActionListener(this);


		btnCl.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				saveForConsole = "";
				txtConsole.setText("");
			} 
		} );
		Action action = new AbstractAction("Clear Console") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				saveForConsole = "";
				txtConsole.setText("");
			}

		};

		doScroll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(doScroll.isSelected()){
					bdoScroll=true;
					DefaultCaret caret = (DefaultCaret)txtConsole.getCaret();
					caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
				}else{
					bdoScroll=false;
					DefaultCaret caret = (DefaultCaret)txtConsole.getCaret();
					caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
				}
			}
		});

		doWarn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(doWarn.isSelected()){
					bdoWarn=true;
				}else{
					bdoWarn=false;
				}
			}
		});

		//toggle whether the regular or alpha image is shown. This is only available when an image has an alpha channel.
		showAlpha.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(showAlpha.isSelected()){
					img_refPane.setVisible(false);
					img_alpPane.setVisible(true);
				}else{
					img_refPane.setVisible(true);
					img_alpPane.setVisible(false);
				}
			}
		});

		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift control W"));
		btnCl.getActionMap().put("clear", action);
		btnCl.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				(KeyStroke) action.getValue(Action.ACCELERATOR_KEY), "clear");

		setupFrame();
	}

	private void setupFrame(){
		//The initial frame setup
		img_refPane.setVisible(false);
		img_alpPane.setVisible(false);
		xmlPane.setVisible(true);
		Spring sWid = springLayout.getConstraint(SpringLayout.EAST, getContentPane());
		Spring sHgt = springLayout.getConstraint(SpringLayout.SOUTH, getContentPane());
		springLayout.getConstraints(mfPane).setWidth(Spring.scale(sWid,sclWid));
		springLayout.getConstraints(mfPane).setHeight(Spring.scale(sHgt,sclHgt));

		springLayout.putConstraint(SpringLayout.NORTH, mfPane, iSep * 3, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, mfPane, iSep, SpringLayout.WEST, getContentPane());

		springLayout.putConstraint(SpringLayout.NORTH, xmlPane, iSep * 3, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, xmlPane, -iSep, SpringLayout.EAST, getContentPane());

		springLayout.putConstraint(SpringLayout.WEST, xmlPane, iSep, SpringLayout.EAST, mfPane);


		springLayout.putConstraint(SpringLayout.WEST, statPane, iSep, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, statPane, -iSep * 3, SpringLayout.SOUTH, getContentPane());

		springLayout.putConstraint(SpringLayout.SOUTH, consolePane, -iSep * 3, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, consolePane, 0, SpringLayout.EAST, xmlPane);

		springLayout.putConstraint(SpringLayout.WEST, xmlPane, iSep, SpringLayout.EAST, mfPane);



		springLayout.putConstraint(SpringLayout.SOUTH, xmlPane, 0, SpringLayout.SOUTH, mfPane);


		springLayout.putConstraint(SpringLayout.NORTH, statPane, iSep, SpringLayout.SOUTH, xmlPane);
		springLayout.putConstraint(SpringLayout.EAST, statPane, 0, SpringLayout.EAST, mfPane);

		springLayout.putConstraint(SpringLayout.NORTH, consolePane, 0, SpringLayout.NORTH, statPane);
		springLayout.putConstraint(SpringLayout.WEST, consolePane, 0, SpringLayout.WEST, xmlPane);

		springLayout.putConstraint(SpringLayout.NORTH, btnCH, 0, SpringLayout.SOUTH, mfPane);
		springLayout.putConstraint(SpringLayout.WEST, btnCH, 0, SpringLayout.EAST, mfPane);
		springLayout.putConstraint(SpringLayout.NORTH, xmlStat, 6, SpringLayout.SOUTH, consolePane);
		springLayout.putConstraint(SpringLayout.EAST, xmlStat, 0, SpringLayout.EAST, xmlPane);
		springLayout.putConstraint(SpringLayout.EAST, doSchema, 0, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, doSchema, 0, SpringLayout.NORTH, xmlPane);
		springLayout.putConstraint(SpringLayout.EAST, bNoUUIDchk, 0, SpringLayout.WEST, doSchema);
		springLayout.putConstraint(SpringLayout.SOUTH, bmesh, 0, SpringLayout.NORTH, xmlPane);
		springLayout.putConstraint(SpringLayout.SOUTH, bNoUUIDchk, 0, SpringLayout.NORTH, xmlPane);

		springLayout.putConstraint(SpringLayout.NORTH, img_refPane, 0, SpringLayout.NORTH, xmlPane);
		springLayout.putConstraint(SpringLayout.SOUTH, img_refPane, 0, SpringLayout.SOUTH, xmlPane);
		springLayout.putConstraint(SpringLayout.EAST, img_refPane, 0, SpringLayout.EAST, xmlPane);
		springLayout.putConstraint(SpringLayout.WEST, img_refPane, 0, SpringLayout.WEST, xmlPane);
		springLayout.putConstraint(SpringLayout.NORTH, img_alpPane, 0, SpringLayout.NORTH, xmlPane);
		springLayout.putConstraint(SpringLayout.SOUTH, img_alpPane, 0, SpringLayout.SOUTH, xmlPane);
		springLayout.putConstraint(SpringLayout.EAST, img_alpPane, 0, SpringLayout.EAST, xmlPane);
		springLayout.putConstraint(SpringLayout.WEST, img_alpPane, 0, SpringLayout.WEST, xmlPane);
		springLayout.putConstraint(SpringLayout.WEST, btnCl, 0, SpringLayout.WEST, consolePane);
		springLayout.putConstraint(SpringLayout.NORTH, btnCl, 2, SpringLayout.SOUTH, consolePane);
		springLayout.putConstraint(SpringLayout.SOUTH, doScroll, -2, SpringLayout.SOUTH, btnCl);
		springLayout.putConstraint(SpringLayout.WEST, doScroll, 2, SpringLayout.EAST, btnCl);
		springLayout.putConstraint(SpringLayout.SOUTH, doWarn, -2, SpringLayout.SOUTH, btnCl);
		springLayout.putConstraint(SpringLayout.WEST, doWarn, 2, SpringLayout.EAST, doScroll);
		springLayout.putConstraint(SpringLayout.SOUTH, showAlpha, -2, SpringLayout.SOUTH, btnCl);
		springLayout.putConstraint(SpringLayout.WEST, showAlpha, 2, SpringLayout.EAST, doWarn);

	}
	@Override
	public void dispose() {
		//save settings on exit
		setIni();
		File ztmp = new File("ztmp");
		try {
			FileUtils.cleanDirectory(ztmp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ztmp = new File("imagetmp");
		try {
			FileUtils.cleanDirectory(ztmp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.dispose();
		System.exit(0);
	}
	public void start(){
		int inset = 50;

		Path path = Paths.get("zips");
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				//fail to create directory
				if(!StartUp.brunCli){
					logger.error("Error creating zips directory, zip64 disabled.");
					e.printStackTrace();
					zipdir = false;
				} else {
					JOptionPane.showMessageDialog(null, "Error creating zips directory, zip64 disabled." +  "\n" + e.getMessage()  
					+ "\n", "File I/O error: creating zips directory.", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					zipdir = false;
				}
			}
		} else {
			zipdir = true;
		}
		path = Paths.get("zips/temp");
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				//fail to create directory
				if(!StartUp.brunCli){
					JOptionPane.showMessageDialog(null, "Error creating zips directory, zip64 disabled." +  "\n" + e.getMessage()  
					+ "\n", "File I/O error: creating zips directory.", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					zipdir = false;
				} else {
					logger.error("Error creating zips directory, zip64 disabled.");
					e.printStackTrace();
					zipdir = false;
				}
			}
		} else {
			zipdir = true;
		}
		path = Paths.get("ztmp");
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				//fail to create directory
				if(!StartUp.brunCli){
					JOptionPane.showMessageDialog(null, "Error creating ztmp directory." +  "\n" + e.getMessage()  
					+ "\n", "File I/O error: creating ztmp directory.", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				} else {
					logger.error( "Error creating ztmp directory.");
					e.printStackTrace();
				}
			}
		}
		path = Paths.get("imagetmp");
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				//fail to create directory
				if(!StartUp.brunCli){
					JOptionPane.showMessageDialog(null, "Error creating imagetmp directory." +  "\n" + e.getMessage()  
					+ "\n", "File I/O error: creating imagetmp directory.", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				} else {
					logger.error( "Error creating imagetmp directory.");
					e.printStackTrace();
				}
			}
		}
		if(StartUp.brunCli){
			path = Paths.get("cliResults");
			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					//fail to create directory
					logger.error("Error creating cliResults directory.");
					e.printStackTrace();
				}
			}
		}
		initiater.addListener(responder);
		getIni();
		if(!StartUp.brunCli){
			this.setBounds(inset, inset, scrSzWid, scrSzHgt);
			this.setLocation(scrLocX, scrLocY);
			this.setVisible(true);
			setupFrame();
			getContentPane().validate();
			getContentPane().repaint();
			this.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					//another close method, clean up ztmp, save settings.
					setIni();
					File ztmp = new File("ztmp");
					try {
						FileUtils.cleanDirectory(ztmp);
					} catch (IOException e) {
						e.printStackTrace();
					}
					ztmp = new File("imagetmp");
					try {
						FileUtils.cleanDirectory(ztmp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			//cli execution:
			logger.info("Running via CLI");

			if(StartUp.fileList.equals("") && StartUp.oneFile.equals("")){
				logger.error("CLI specified, but neither a file or the file list has been specified. Exiting.");
				super.dispose();
				System.exit(0);

			}

			//run a file and/or list of files.
			if(!StartUp.oneFile.equals("")){
				fxmlErrorReturn = "";
				fxmlWarningReturn = "";
				fxmlSchemaReturn = "";
				sErr = "";
				Boolean bchk = cliOpenFile(StartUp.oneFile);
				if(!bchk){
					File ztmp = new File("ztmp");
					try {
						FileUtils.cleanDirectory(ztmp);
					} catch (IOException e) {
						e.printStackTrace();
					}
					super.dispose();
					System.exit(0);
				}
				bChkError = false;
				txtConsole.setText("");
				saveForConsole = "";
				String pf = "Pass";
				String szTime = ztimeA(System.currentTimeMillis());
				if(StartUp.brunVal){
					chkSchZip();
					if(bChkError){
						pf = "Fail";
					}
				}
				if(StartUp.brunStruct){
					if(!bChkError || StartUp.brunIfFail){
						chk3MfStructure();
						if(!fxmlErrorReturn.equals("")){
							pf = "Fail";
						}
						if(!fxmlWarningReturn.equals("")){
							if(pf.equals("Pass")) pf = "Warning";
						}

					} else {
						saveForConsole += "<br>Aborting file structure check due to schema violations!<br>";
						addToConsole(saveForConsole);
					}
				}
				String addIssue = "";
				if(!fxmlSchemaReturn.equals("")){
					int ie = 0;
					ie = fxmlSchemaReturn.indexOf("</Issue>")+8;
					addIssue = fxmlSchemaReturn.substring(0,ie);
					addIssue = addIssue.replace("<Issue", "<PrimaryIssue");
					addIssue = addIssue.replace("</Issue", "</PrimaryIssue");
					fxmlSchemaReturn = addIssue + fxmlSchemaReturn.substring(ie); 

				} else if(!sErr.equals("")){
					//note: we get transform errors for 3mf color that do not apply
					//an error is returned, but not the issues.
					if(sErr.startsWith("E")){
						int ib = 0;
						int ie = 0;
						String tst = "\"" + sErr + "\"";
						ib = fxmlErrorReturn.indexOf(tst);
						if(ib > -1){
							ib = fxmlErrorReturn.substring(0,ib).lastIndexOf("<Issue");
							ie = fxmlErrorReturn.indexOf("</Issue>",ib)+8;
							addIssue = fxmlErrorReturn.substring(ib,ie);
							addIssue = addIssue.replace("<Issue", "<PrimaryIssue");
							addIssue = addIssue.replace("</Issue", "</PrimaryIssue");
							fxmlErrorReturn = addIssue + fxmlErrorReturn.substring(0,ib) + fxmlErrorReturn.substring(ie); 
						}
					}else{
						int ib = 0;
						int ie = 0;
						String tst = "\"" + sErr + "\"";
						ib = fxmlWarningReturn.indexOf(tst);
						if(ib > -1){
							ib = fxmlWarningReturn.substring(0,ib).lastIndexOf("<Issue");
							ie = fxmlWarningReturn.indexOf("</Issue>",ib)+8;
							addIssue = fxmlWarningReturn.substring(ib,ie);
							addIssue = addIssue.replace("<Issue", "<PrimaryIssue");
							addIssue = addIssue.replace("</Issue", "</PrimaryIssue");
							fxmlWarningReturn = addIssue + fxmlWarningReturn.substring(0,ib) + fxmlWarningReturn.substring(ie); 
						}
					}
				}
				//Write txtConsole to html result file.
				String nameOut = "cliResults/" + selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")+ 1) + "html";
				String xmlOut = "cliResults/" + selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")+ 1) + "xml";
				//
				if(StartUp.bnoOverwrite){
					//check for the file, if it exists, then find a version that does not.
					if(new File(nameOut).exists()){
						int icnt = 1;
						while(new File(nameOut.replace(".html", "_" + icnt + ".html")).exists()){
							icnt++;
						}
						nameOut = nameOut.replace(".html", "_" + icnt + ".html");

					}
					if(new File(xmlOut).exists()){
						int icnt = 1;
						while(new File(xmlOut.replace(".xml", "_" + icnt + ".xml")).exists()){
							icnt++;
						}
						xmlOut = xmlOut.replace(".xml", "_" + icnt + ".xml");

					}
				}
				String xout = "<?xml version=\"1.0\" encoding=\"UTF-8\"  standalone=\"no\"?>";
				xout += "<TestResult file=\"" + selectedFile.getName() + "\" dateTime=\"" + szTime + "\" result=\"" + pf + "\">";
				xout += fxmlSchemaReturn + fxmlErrorReturn + fxmlWarningReturn;
				xout +="</TestResult>";
				String xmlResult = formatXmlString(xout);
				if(xmlResult.equals("")){
					saveForConsole += "<br>Failed the XML format of the structural check for errors!<br>";
					addToConsole(saveForConsole);
					logger.error("Failed the XML format of the structural check for errors!");
				} else {
					xout = xmlResult;
				}
				try {
					FileUtils.writeStringToFile(new File(nameOut), txtConsole.getText(), "UTF-8");
					FileUtils.writeStringToFile(new File(xmlOut), xout, "UTF-8");
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			//loop through the file list and run each
			if(!StartUp.fileList.equals("")){
				if(!new File(StartUp.fileList).exists()){
					logger.error("File list specified, but not found for: " + StartUp.fileList + "  Exiting");
					super.dispose();
					System.exit(0);
				}
				try {
					String failed = "";
					String startTime = ztimeA(System.currentTimeMillis());
					String startTimeF = "_" + startTime.replaceAll(":", "_");
					startTimeF = startTimeF.replace("-", "_");
					if(new File("cliResults/failures.html").exists()){
						FileUtils.deleteQuietly(new File("cliResults/failures.html"));
					}
					List<String> fileNames = FileUtils.readLines(new File(StartUp.fileList), "UTF-8");
					for (String fileIn:fileNames){
						fileIn = fileIn.trim();
						//skip empty lines and commented ones.
						if(fileIn.equals("") || fileIn.startsWith("#"))
							continue;
						Boolean bchk = cliOpenFile(fileIn);
						if(!bchk){
							File ztmp = new File("ztmp");
							try {
								FileUtils.cleanDirectory(ztmp);
							} catch (IOException e) {
								e.printStackTrace();
							}
							continue;
						}
						fxmlErrorReturn = "";
						fxmlWarningReturn = "";
						fxmlSchemaReturn = "";
						sErr = "";
						String pf = "Pass";
						String szTime = ztimeA(System.currentTimeMillis());
						bChkError = false;
						bStructError = false;
						txtConsole.setText("");
						saveForConsole = "";
						if(StartUp.brunVal){
							chkSchZip();
							if(bChkError){
								pf = "Fail";
							}
						}
						if(StartUp.brunStruct){
							if(!bChkError || StartUp.brunIfFail){
								chk3MfStructure();
								if(!fxmlErrorReturn.equals("")){
									pf = "Fail";
								}
								if(!fxmlWarningReturn.equals("")){
								if(pf.equals("Pass")) pf = "Warning";
								}
							} else {
								saveForConsole += "<br>Aborting file structure check due to schema violations!<br>";
								addToConsole(saveForConsole);
							}
						}
						String addIssue = "";
						if(!fxmlSchemaReturn.equals("")){
							int ie = 0;
							ie = fxmlSchemaReturn.indexOf("</Issue>")+8;
							addIssue = fxmlSchemaReturn.substring(0,ie);
							addIssue = addIssue.replace("<Issue", "<PrimaryIssue");
							addIssue = addIssue.replace("</Issue", "</PrimaryIssue");
							fxmlSchemaReturn = addIssue + fxmlSchemaReturn.substring(ie); 

						} else if(!sErr.equals("")){
							if(sErr.startsWith("E")){
								int ib = 0;
								int ie = 0;
								String tst = "\"" + sErr + "\"";
								ib = fxmlErrorReturn.indexOf(tst);
								if(ib > -1){
									ib = fxmlErrorReturn.substring(0,ib).lastIndexOf("<Issue");
									ie = fxmlErrorReturn.indexOf("</Issue>",ib)+8;
									addIssue = fxmlErrorReturn.substring(ib,ie);
									addIssue = addIssue.replace("<Issue", "<PrimaryIssue");
									addIssue = addIssue.replace("</Issue", "</PrimaryIssue");
									fxmlErrorReturn = addIssue + fxmlErrorReturn.substring(0,ib) + fxmlErrorReturn.substring(ie); 
								}
							}else{
								int ib = 0;
								int ie = 0;
								String tst = "\"" + sErr + "\"";
								ib = fxmlWarningReturn.indexOf(tst);
								if(ib > -1){
									ib = fxmlWarningReturn.substring(0,ib).lastIndexOf("<Issue");
									ie = fxmlWarningReturn.indexOf("</Issue>",ib)+8;
									addIssue = fxmlWarningReturn.substring(ib,ie);
									addIssue = addIssue.replace("<Issue", "<PrimaryIssue");
									addIssue = addIssue.replace("</Issue", "</PrimaryIssue");
									fxmlWarningReturn = addIssue + fxmlWarningReturn.substring(0,ib) + fxmlWarningReturn.substring(ie);
								}
							}
						}
						//Write txtConsole to html result file.
						String nameOut = "cliResults/" + selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")+ 1) + "html";
						String xmlOut = "cliResults/" + selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")+ 1) + "xml";
						if(StartUp.bnoOverwrite){
							//check for the file, if it exists, then find a version that does not.
							if(new File(nameOut).exists()){
								int icnt = 1;
								while(new File(nameOut.replace(".html", "_" + icnt + ".html")).exists()){
									icnt++;
								}
								nameOut = nameOut.replace(".html", "_" + icnt + ".html");

							}
							if(new File(xmlOut).exists()){
								int icnt = 1;
								while(new File(xmlOut.replace(".xml", "_" + icnt + ".xml")).exists()){
									icnt++;
								}
								xmlOut = xmlOut.replace(".xml", "_" + icnt + ".xml");

							}
						}
						String xout = "<?xml version=\"1.0\" encoding=\"UTF-8\"  standalone=\"no\"?>";
						xout += "<TestResult file=\"" + selectedFile.getName() + "\" dateTime=\"" + szTime + "\" result=\"" + pf + "\">";
						xout += fxmlSchemaReturn + fxmlErrorReturn + fxmlWarningReturn;
						xout +="</TestResult>";
						String xmlResult = formatXmlString(xout);
						if(xmlResult.equals("")){
							saveForConsole += "<br>Failed the XML format of the structural check for errors!<br>";
							addToConsole(saveForConsole);
							logger.error("Failed the XML format of the structural check for errors!");
						} else {
							xout = xmlResult;
						}
						FileUtils.writeStringToFile(new File(nameOut), txtConsole.getText(), "UTF-8");
						FileUtils.writeStringToFile(new File(xmlOut), xout, "UTF-8");
						failed += xout.substring(xout.indexOf("<T"));
						if(bChkError || bStructError){
							FileUtils.writeStringToFile(new File("cliResults/failures.html"),
									"<br><a href='" + nameOut.replace("cliResults/", "") + "' target='_new'>View errors for " + nameOut 
									+ "</a>" + ", <a href='" + xmlOut.replace("cliResults/", "") + "' target='_new'>" + xmlOut + "</a>"
									, "UTF-8",true);
						}
					}
					if(failed.length()>0){
						String xout = "<TestResults dateTime=\"" + startTime + "\">" + failed +
								"</TestResults>";
						String xmlResult = formatXmlString(xout);
						if(xmlResult.equals("")){
							saveForConsole += "<br>Failed the XML format of the structural check for errors!<br>";
							logger.error("Failed the XML format of the structural check for errors!");
							addToConsole(saveForConsole);
						} else {
							xout = xmlResult;
						}
						FileUtils.writeStringToFile(new File("cliResults/XmlAggregate" + startTimeF + ".xml"), xout, "UTF-8");
						FileUtils.writeStringToFile(new File("cliResults/failures.html"),
								"<br><a href='XmlAggregate" + startTimeF + ".xml' target='_new'>XML summary of errors: cliResults/XmlAggregate" + startTimeF + ".xml" 
										+ "</a>" 
										, "UTF-8",true);

					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			File ztmp = new File("ztmp");
			try {
				FileUtils.cleanDirectory(ztmp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("Finished");
			super.dispose();
			System.exit(0);


		}

	}
	private Boolean cliOpenFile(String fileIn){
		if(new File(fileIn).exists()){
			selectedFile = new File(fileIn);
		} else {
			logger.error("Failed to find file " + fileIn);
			return false;
		}
		currentFile = selectedFile.getAbsolutePath();
		currentFile = currentFile.replaceAll("\\\\", "/");
		currentDir = currentFile.substring(0, currentFile.lastIndexOf("/")+1);
		unzip3mf();
		xmlEd.setText("");
		xmlStat.setText("");
		txtStat.setText("");
		xmlChanged = false;
		return true;

	}
	public JTree getTree(){
		//The tree for the 3mf file
		if(currentFile == null){
			mainFile = "";
			DefaultMutableTreeNode top = new DefaultMutableTreeNode("Empty");
			tree = new JTree(top);
			return tree;
		}
		mainFile = selectedFile.getName();
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(selectedFile.getName());
		//Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);
		createNodes(top);
		tree.setShowsRootHandles(true);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(new TreeSelectionListener(){public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					tree.getLastSelectedPathComponent();
			xmlcaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			if (node == null) return;

			if (node.isLeaf()) {
				try{
					if(xmlCancel){
						xmlCancel=false;
						return;
					}
					if(xmlChanged){
						//ask if file should be saved after changes
						int n = SaveDialog();
						//0 = yes, 1 = no, 2 = cancel
						if(n == 0){
							if(bSchemaChk){
								vCheckSchema();
								if(bSchemaFail){
									n = sfSaveDialog();
									if(n != 0){
										xmlCancel = true;
										DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
										TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
										tree.scrollPathToVisible(tpath);
										tree.setSelectionPath(tpath);
										return;
									}
								}
							}
							TreeNode[] path = lastNode.getPath();
							//This path includes the filename that was prepended to each entry
							String parent = "";
							String slash = "/";
							String upFile ="";
							upFile += path[path.length-1];
							for(int j=0;j<path.length-1;j++){
								if(j>0){
									parent += path[j] + slash;
								}
							}
							if(parent.contentEquals("/")) parent = "";
							updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
							xmlChanged = false;
						}else if (n==1){
							//Do nothing and move on (don't need this, but maybe in the future...)
						}else{
							xmlCancel = true;
							DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
							TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
							tree.scrollPathToVisible(tpath);
							tree.setSelectionPath(tpath);
							return;
						}
					}
					if(!(lastNode == null)){
						nd.addNode(xFilePath(lastNode), xmlEd.getCaretPosition());						
					}
					lastNode = node;
					String fileN = xFilePath(node);
					File getFile = new File("ztmp/" + fileN);
					BasicFileAttributes attr = Files.readAttributes(getFile.toPath(), BasicFileAttributes.class);
					xmlEd.setText("");
					xmlChanged = false;
					xmlStat.setText("");
					txtStat.setText("");
					clearUndoAction.actionPerformed(null);
					try {
						txtStat.setText("");
						//Need to update here for adding in the PNG files
						if(getFile.isDirectory()) 
							return;
						JFrame.getFrames()[0].setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						tree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						byte[] tmp=new byte[4];
						InputStream in = FileUtils.openInputStream(getFile);
						if(in == null){
							JFrame.getFrames()[0].setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							return;
						}
						in.read(tmp, 0, 4);
						in.close();
						String tst = new String(tmp);
						boolean bNotXml = false;
						if(tst.substring(1, 4).startsWith("PNG") ||
								tmp[0] == 0xFF || tmp[0] == -1 ||
								tst.substring(0, 3).startsWith("BM") )bNotXml = true;
						String cstat = "";
						if (bNotXml){
							cstat = "\n" + chkImage(getFile);
						}
						txtStat.setText("File: " + getFile.getName() + "\nSize: " + attr.size() + " bytes" 
								+ "\nLast Modified: "+ ztimeA(attr.lastModifiedTime().toMillis())
								+ cstat);
						statPane.getViewport().setViewPosition(new Point(0, 0));
						bSkipBig = false;
						xmlEd.setEnabled(true);
						if(bNotXml){
							//it is not xml, so try loading an image.
							BufferedImage fin= null, fout=null;
							xmlPane.setVisible(false);
							img_refPane.setVisible(true);
							img_alpPane.setVisible(false);
							showAlpha.setSelected(false);
							try {
								fout = getAlpIn(getFile);
								if(fout != null){
									showAlpha.setVisible(true);
									ibufAlp.setImage(fout);
									img_alp.setPreferredSize(ibufAlp.getPreferredSize());
								}
								else
									showAlpha.setVisible(false);

								InputStream isIn = FileUtils.openInputStream(getFile);
								fin = ImageIO.read(isIn);
								isIn.close();
								ibufRef.setImage(fin);
								img_ref.setPreferredSize(ibufRef.getPreferredSize());
								xmlPane.revalidate();
								getContentPane().validate();
								getContentPane().repaint();
								JFrame.getFrames()[0].setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
								return;
							} catch (IOException e1) {
								//It was not an image, so though it to the editor
								logger.info(getFile.getName() + " is not a recognizable image or proper XML. Opening in editor.");
							}
						} else if(attr.size()> 75000000){
							//we have size issues since it takes multiple bytes to add color/font to XML.
							getContentPane().validate();
							getContentPane().repaint();
							JFrame.getFrames()[0].setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							if(!StartUp.brunCli){
								JOptionPane.showMessageDialog(null, "Part is too large for editor: " + getFile.getName()   
								, "Part too large for editor.", JOptionPane.WARNING_MESSAGE);
							}
							bSkipBig = true;
							xmlEd.setEnabled(false);
							xmlPane.setVisible(true);
							img_refPane.setVisible(false);
							img_alpPane.setVisible(false);
							return;
						}
						xmlPane.setVisible(true);
						img_refPane.setVisible(false);
						img_alpPane.setVisible(false);



						xmlEd.setText(FileUtils.readFileToString(getFile, "UTF-8"));

						int iCur = nd.getCursor(fileN);
						int iHscr = nd.getHscroll(fileN);
						int iVscr = nd.getVscroll(fileN);
						xmlEd.setCaretPosition(iCur);
						xmlPane.getHorizontalScrollBar().setValue(iHscr);
						xmlPane.getVerticalScrollBar().setValue(iVscr);
						xmlPane.getViewport().setViewPosition(new Point(iHscr, iVscr));
						Rectangle caretRect=xmlEd.getUI().modelToView(xmlEd,xmlEd.getCaretPosition());
						xmlEd.getCaret().setMagicCaretPosition(caretRect.getLocation());
						xmlChanged = false;
						clearUndoAction.actionPerformed(null);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					} finally {
						JFrame.getFrames()[0].setCursor(Cursor.getDefaultCursor());
						tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						if(xmlPane.isVisible()){
							xmlEd.requestFocusInWindow();
							//Need this to avoid an issue where the cursor is not initialized in 
							//the editor window and text is entered backwards or the highlighted
							//text is delete, then highlighted on the remaining text
							try {
								Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
								double width = screenSize.getWidth();
								double height = screenSize.getHeight();
								if(!(width == 3840.0 && height == 2160.0)){
									Robot r = new Robot();
									Point ips = MouseInfo.getPointerInfo().getLocation();
									Point ipx = xmlEd.getLocationOnScreen();
									Point ip = xmlEd.getCaret().getMagicCaretPosition();
									int ix = ipx.x;
									int iy = ipx.y;
									if(ip != null){
										ix += ip.x;
										iy += ip.y;
									}
									//4k screen issues. This did not work as a fix
									//								for(int i=0;i<10000;i++){
									//									r.mouseMove(ix, iy);
									//									Point ipt = MouseInfo.getPointerInfo().getLocation();
									//									if(ipt.x == ix  && ipt.y == iy)break;
									//								}
									r.mouseMove(ix, iy);
									r.mousePress( InputEvent.BUTTON1_MASK );
									r.mouseRelease( InputEvent.BUTTON1_MASK );
									r.mouseMove(ips.x, ips.y);
								}
							} catch (AWTException e1) {
								e1.printStackTrace();
							}
						}
					}
				} catch (OutOfMemoryError em) {
					em.printStackTrace();
					JOptionPane.showMessageDialog(null, "Out of Memory. Saving may truncate the file."   
							, "Out of Memory.", JOptionPane.ERROR_MESSAGE);

				}catch(IOException ex){
					JFrame.getFrames()[0].setCursor(Cursor.getDefaultCursor());
					tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					ex.printStackTrace(); 
				}
			} else {
				if(xmlChanged){
					//ask if file should be saved after changes
					int n = SaveDialog();
					//0 = yes, 1 = no, 2 = cancel
					if(n == 0){
						if(bSchemaChk){
							vCheckSchema();
							if(bSchemaFail){
								n = sfSaveDialog();
								if(n != 0){
									xmlCancel = true;
									DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
									TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
									tree.scrollPathToVisible(tpath);
									tree.setSelectionPath(tpath);
									return;
								}
							}
						}
						TreeNode[] path = lastNode.getPath();
						//This path includes the filename that was prepended to each entry
						String parent = "";
						String slash = "/";
						String upFile ="";
						upFile += path[path.length-1];
						for(int j=0;j<path.length-1;j++){
							if(j>0){
								parent += path[j] + slash;
							}
						}
						if(parent.contentEquals("/")) parent = "";
						updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
						xmlChanged = false;
					}else if (n==1){
						//Do nothing and move on (don't need this, but maybe in the future...)
					}else{
						xmlCancel = true;
						DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
						TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
						tree.scrollPathToVisible(tpath);
						tree.setSelectionPath(tpath);
						return;
					}
				}
				txtStat.setText("");
				xmlEd.setText("");
				xmlEd.setEnabled(false);
				xmlPane.setVisible(true);
				xmlChanged = false;
				img_refPane.setVisible(false);
				clearUndoAction.actionPerformed(null);
			}
		}
		});
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.NOBUTTON) {
				} else if (e.getButton() == MouseEvent.BUTTON1) {
				} else if (e.getButton() == MouseEvent.BUTTON2) {
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						//get the leaf name
						//get the full path minus the prepended file header
						String lfile = locPath(path);
						try {
							if(xmlChanged){
								//ask if file should be saved after changes
								int n = SaveDialog();
								//0 = yes, 1 = no, 2 = cancel
								if(n == 0){
									if(bSchemaChk){
										vCheckSchema();
										if(bSchemaFail){
											n = sfSaveDialog();
											if(n != 0){
												xmlCancel = true;
												DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
												TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
												tree.scrollPathToVisible(tpath);
												tree.setSelectionPath(tpath);
												return;
											}
										}
									}
									nd.clear();
									TreeNode[] pathi = lastNode.getPath();
									//This path includes the filename that was prepended to each entry
									String parent = "";
									String slash = "/";
									String upFile ="";
									upFile += pathi[pathi.length-1];
									for(int j=0;j<pathi.length-1;j++){
										if(j>0){
											parent += pathi[j] + slash;
										}
									}
									if(parent.contentEquals("/")) parent = "";
									updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
									xmlChanged = false;
								}else if (n==1){
									//Do nothing and move on (don't need this, but maybe in the future...)
								}else{
									xmlCancel = true;
									DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
									TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
									tree.scrollPathToVisible(tpath);
									tree.setSelectionPath(tpath);
									return;
								}
							}
							tree.scrollPathToVisible(path);
							tree.setSelectionPath(path);
							dPop.topDir=false;
							if(path.getPath().length == 1) dPop.topDir=true;
							File getFile = new File("ztmp/" + lfile);
							BasicFileAttributes attr = Files.readAttributes(getFile.toPath(), BasicFileAttributes.class);

							//Check for null
							//null is a directory in the zip with files
							//isDirectory true is an empty directory
							//not null and not a directory is a file, though an empty file created by the utils
							//is not really different from a directory entry as the / on the end tends to get lost.

							//dPop is a right-click popup
							dPop.keyinfo = keyinfo;
							if(getFile == null || getFile.isDirectory()){
								txtStat.setText(" ");
								//								zip.close();
								dPop.dirEnabled(true,false);
								dPop.zipIn = currentFile;
								dPop.fileN=lfile;
								dPop.initiater = initiater;
								dPop.cDir = currentDir;
								dPop.popup.show(mfPane, e.getX(), e.getY());
							}else {
								String cstat = "";
								cstat = "\n" + chkImage(getFile);
								txtStat.setText("File: " + getFile.getName() + "\nSize: " + attr.size() + " bytes" 
										+ "\nLast Modified: "+ ztimeA(attr.lastModifiedTime().toMillis())
										+ cstat);
								statPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
								String txtin = xmlEd.getText();
								if(txtin.startsWith("%3McF")){
									KeyInfo keyitem = CheckKeyInfo("/" + lfile);
									if(keyitem != null){
										DeCryptModel dcry = new DeCryptModel();
										dcry.sAAD = keyitem.getAAD();
										dcry.bSha256 = keyitem.getSha256();
										dcry.FileLoc = "ztmp" + keyitem.getFile();
										dcry.sCT = keyitem.getCT();
										dcry.sIV = keyitem.getIV();
										dcry.sTAG = keyitem.getTAG();
										dcry.bNoComp = keyitem.getNoComp();
										String dtext = "";
										try {
											dtext = dcry.DoDecrypt();
										} catch (Exception ec) {
											dtext = "";
										}
										txtin = dtext;
									} else {
										txtin = "Decryption key not found, possibly due to keystore not referenced in _rels/.rels file.";
									}
								}
								String xmlTmp = txtin;
								String xmlResult = formatXmlString(xmlTmp);
								if(xmlResult.contentEquals("")){
									//do anything for failed here?
								} else {
									txtin = xmlResult;
									xmlTmp = "";
									xmlResult = "";
								}
								boolean bmodel = false;
								if(lfile.toLowerCase().endsWith(".model") && txtin.contains("<object")){
									bmodel=true;
								}
								if(xmlEd.getText().startsWith("%3McF")){
									dPop.dirEnabled(false, bmodel, true);
								} else {
									dPop.dirEnabled(false, bmodel);
								}
								dPop.cDir = currentDir;
								dPop.zipIn = currentFile;
								dPop.fileN=lfile;
								dPop.initiater = initiater;
								//Option for trying to display the models.

								dPop.copyFileText=txtin;
								txtin = "";
								dPop.popup.show(mfPane, e.getX(), e.getY());
								

							}
							attr = null;
						} catch (IOException  e1) {
							logger.info("Failed on checking entry for " + lfile + "\n" + e1.getMessage());
							e1.printStackTrace();
						}		        	  }
				}

			}
		});
		tree.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int x = (int) e.getPoint().getX();
				int y = (int) e.getPoint().getY();
				TreePath path = tree.getPathForLocation(x, y);
				if (path == null) {
					tree.setCursor(Cursor.getDefaultCursor());
				} else {
					tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}

		});
		return tree;
	}
	private void createNodes(DefaultMutableTreeNode top) {
		//lets make a tree of the 3mf file structure
		try{
			tree.setCellRenderer(new DefaultTreeCellRenderer() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean selected, boolean expanded,
						boolean isLeaf, int row, boolean focused) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
					String s = xFilePath(node);
					if (nd.getDir(s)){

						setClosedIcon(getDefaultClosedIcon());
						setOpenIcon(getDefaultOpenIcon());
						setLeafIcon(iClosed);
					}else{
						setClosedIcon(getDefaultClosedIcon());
						setLeafIcon(getDefaultLeafIcon());
						setOpenIcon(getDefaultOpenIcon());
					}
					Component c = super.getTreeCellRendererComponent(tree, value,
							selected, expanded, isLeaf, row, focused);
					return c;
				}
			});
			File file = new File("ztmp");
			//a dir is rather a strange thing in a zip as it is just a placeholder when you have an empty directory
			//most directories in a zip are implicitly created by the path.
			//On a disk, the directory must exist first, before a file can be added. In a zip, a directory may never be created as
			//a zip is just a collection of files, so a dir is just a special empty file with a flag set.
			Collection<File> files = FileUtils.listFilesAndDirs(file,TrueFileFilter.TRUE, TrueFileFilter.TRUE);
			File[] listOfFiles = files.toArray(new File[files.size()]);
			Arrays.sort(listOfFiles, new Comparator<File>() {
				private final Comparator<String> NATURAL_SORT = new WindowsExplorerComparator();

				@Override
				public int compare(File o1, File o2) {;
				return NATURAL_SORT.compare(o1.getName(), o2.getName());
				}
			});
			for(File f:listOfFiles){ 
				DefaultMutableTreeNode node = null;
				DefaultMutableTreeNode pnode = null;
				String fileName =  f.getAbsolutePath();
				fileName = fileName.replaceAll("\\\\", "/");
				if(fileName.endsWith("/ztmp"))continue;
				if(f.isDirectory() && f.listFiles().length >0)continue;
				fileName = fileName.substring(fileName.lastIndexOf("ztmp/")+5);
				bSetIcon = false;
				if(fileName.endsWith("/") || f.isDirectory())bSetIcon = true;
				TreeNode[] path = top.getPath();
				fileName = path[0] + "/" + fileName;
				String[] parts = fileName.split("/");
				String dirPart = "";
				Boolean found = false;
				for (int i=1; i<parts.length;i++){
					dirPart = parts[i];
					found=false;
					if(parts.length == 2){
						//Need to add indicator for file, directory... ?How to tag a node
						top.add(new DefaultMutableTreeNode(dirPart));
						nd.addNode(dirPart, 0);
						nd.addNodeDir(dirPart, bSetIcon);
					}else{
						//determine if an existing element matches the same depth
						TreeModel model = tree.getModel();
						DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
						@SuppressWarnings("unchecked")
						Enumeration<TreeNode> en = rootNode.preorderEnumeration();
						while (en.hasMoreElements())
						{
							node = (DefaultMutableTreeNode) en.nextElement();
							path = node.getPath();
							//if i is greater than or equal to the length, nothing further to find here
							if (i >= path.length ) continue;
							//save the parent after the first level
							if(path[i].toString().contentEquals(dirPart)){
								boolean upNode = true;
								for(int ix=1;ix<i;ix++){
									if(!(path[ix].toString().contentEquals(parts[ix].toString()))){
										upNode=false;
									}
								}
								if(upNode){
									pnode = node;
									found=true;
									break;
								}
							}
						}
						if(!found){
							//another method left here as a reminder of other ways to get a path.
							//							String fileN = "";
							//							String slash = "";
							//							for(int j=0;j<path.length;j++){
							//								if(j>0){
							//									fileN += slash + path[j];
							//									slash="/";
							//								}
							//							}
							if(i==1){
								top.add(new DefaultMutableTreeNode(dirPart));
								pnode = top.getLastLeaf();
								nd.addNode(xFilePath(pnode), 0);
								nd.addNodeDir(xFilePath(pnode), bSetIcon);
							}else{
								pnode.add(new DefaultMutableTreeNode(dirPart));
								pnode = pnode.getLastLeaf();
								nd.addNode(xFilePath(pnode), 0);
								nd.addNodeDir(xFilePath(pnode), bSetIcon);
							}

						}

					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace(); 
		}

	}
	public void insertUpdate(DocumentEvent e)
	{
		xmlChanged = true;
	}
	public void removeUpdate(DocumentEvent e)
	{
		xmlChanged = true;
	}
	public void changedUpdate(DocumentEvent e)
	{
		xmlChanged = true;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		//The menu action items, a lengthy list.
		//This method is common across the utilities.
		JMenuItem choice = (JMenuItem) e.getSource();
		if (choice == saveI)
		{
			if(currentFile == null || currentFile == "")return;
			if(bSchemaChk && !bSkipBig){
				vCheckSchema();
				if(bSchemaFail){
					int n = sfSaveDialog();
					if(n != 0)
						return;
				}
			}
			TreeNode[] path = lastNode.getPath();
			//This path includes the filename that was prepended to each entry
			String parent = "";
			String slash = "/";
			String upFile ="";
			upFile += path[path.length-1];
			for(int j=0;j<path.length-1;j++){
				if(j>0){
					parent += path[j] + slash;
				}
			}
			if(parent.contentEquals("/")) parent = "";
			if( upFile.toLowerCase().endsWith(".png")
					|| upFile.toLowerCase().endsWith(".jpg")
					|| upFile.toLowerCase().endsWith(".jpeg")
					) return; //don't save thumbnails
			if(upFile.endsWith("/"))return;//don't try to save a directory
			updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
			xmlChanged = false;
		}else if (choice == saveAsI){
			if(currentFile == null || currentFile == "")return;
			if(bSchemaChk && !bSkipBig){
				vCheckSchema();
				if(bSchemaFail){
					int n = sfSaveDialog();
					if(n != 0)
						return;
				}
			}
			if(xmlChanged){
				int n = SaveDialog();
				//0 = yes, 1 = no, 2 = cancel
				if(n == 0){
					TreeNode[] path = lastNode.getPath();
					//This path includes the filename that was prepended to each entry
					String parent = "";
					String slash = "/";
					String upFile ="";
					upFile += path[path.length-1];
					for(int j=0;j<path.length-1;j++){
						if(j>0){
							parent += path[j] + slash;
						}
					}
					if(parent.contentEquals("/")) parent = "";
					updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
					xmlChanged = false;
				}else if (n==1){
					//Do nothing and move on (don't need this, but maybe in the future...)
				}else{
					xmlCancel = true;
					DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
					TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
					tree.scrollPathToVisible(tpath);
					tree.setSelectionPath(tpath);
					return;
				}
			}
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File(currentFile));
			fileChooser.setSelectedFile(new File(currentFile));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("3D Model Files", "3mf");
			fileChooser.setFileFilter(filter);
			fileChooser.setPreferredSize(new Dimension(600, 400));
			int result = fileChooser.showSaveDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File f = fileChooser.getSelectedFile();
				if(f.exists()){
					int iresult = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?",
							"Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
					switch(iresult){
					case JOptionPane.YES_OPTION:
						break;
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.CLOSED_OPTION:
						return;
					case JOptionPane.CANCEL_OPTION:
						return;
					}
				}
				Component glassPane = ((RootPaneContainer)mfPane.getTopLevelAncestor()).getGlassPane();
				glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				glassPane.setVisible(true);
				selectedFile = fileChooser.getSelectedFile();
				Path from = Paths.get(currentFile);
				Path to = Paths.get(selectedFile.getAbsolutePath());
				try {
					Files.copy(from,  to, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Error creating Save As file: " + selectedFile.getAbsolutePath() + "\n" + e1.getMessage()  
					+ "\n", "File I/O error: creating Save As file.", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
					glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					glassPane.setVisible(false);
					return;
				}
				currentFile = selectedFile.getAbsolutePath();
				currentFile = currentFile.replaceAll("\\\\", "/");
				currentDir = currentFile.substring(0, currentFile.lastIndexOf("/")+1);
				lastFile = currentFile;
				//make a backup of the new save as file.
				String backFile = currentFile + ".bak";
				from = Paths.get(currentFile);
				to = Paths.get(backFile);
				try {
					Files.copy(from,  to, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Error creating backup file: " + backFile + "\n" + e1.getMessage()  
					+ "\nLoad proceeding without backup.", "File I/O error: creating backup file.", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace(); 
				}
				xmlEd.setText("");
				xmlChanged = false;
				clearUndoAction.actionPerformed(null);
				mfPane.setViewportView(getTree());
				setupFrame();
				getContentPane().validate();
				getContentPane().repaint();
				glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				glassPane.setVisible(false);

			}
		}else if (choice == closeI){
			if(xmlChanged){
				int n = SaveDialog();
				//0 = yes, 1 = no, 2 = cancel
				if(n == 0){
					if(bSchemaChk){
						vCheckSchema();
						if(bSchemaFail){
							n = sfSaveDialog();
							if(n != 0){
								xmlCancel = true;
								DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
								TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
								tree.scrollPathToVisible(tpath);
								tree.setSelectionPath(tpath);
								return;
							}
						}
					}
					TreeNode[] path = lastNode.getPath();
					//This path includes the filename that was prepended to each entry
					String parent = "";
					String slash = "/";
					String upFile ="";
					upFile += path[path.length-1];
					for(int j=0;j<path.length-1;j++){
						if(j>0){
							parent += path[j] + slash;
						}
					}
					if(parent.contentEquals("/")) parent = "";
					updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
					xmlChanged = false;
					nd.clear();
				}else if (n==1){
					//Do nothing and move on (don't need this, but maybe in the future...)
				}else{
					xmlCancel = true;
					DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
					TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
					tree.scrollPathToVisible(tpath);
					tree.setSelectionPath(tpath);
					return;
				}
			}
			currentFile = null;
			xmlEd.setText("");
			clearUndoAction.actionPerformed(null);
			txtStat.setText("");
			txtConsole.setText("");
			xmlChanged = false;
			zip64I.setEnabled(false);
			mfPane.setViewportView(getTree());
			setupFrame();
			getContentPane().validate();
			getContentPane().repaint();
		}else if (choice == loadI){
			Component glassPane = ((RootPaneContainer)mfPane.getTopLevelAncestor()).getGlassPane();
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			glassPane.setVisible(true);
			if(xmlChanged){
				//ask if file should be saved after changes
				int n = SaveDialog();
				//0 = yes, 1 = no, 2 = cancel
				if(n == 0){
					if(bSchemaChk){
						vCheckSchema();
						if(bSchemaFail){
							n = sfSaveDialog();
							if(n != 0){
								xmlCancel = true;
								DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
								TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
								tree.scrollPathToVisible(tpath);
								tree.setSelectionPath(tpath);
								glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								glassPane.setVisible(false);
								return;
							}
						}
					}
					nd.clear();
					TreeNode[] path = lastNode.getPath();
					//This path includes the filename that was prepended to each entry
					String parent = "";
					String slash = "/";
					String upFile ="";
					upFile += path[path.length-1];
					for(int j=0;j<path.length-1;j++){
						if(j>0){
							parent += path[j] + slash;
						}
					}
					if(parent.contentEquals("/")) parent = "";
					updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
					xmlChanged = false;
				}else if (n==1){
					//Do nothing and move on (don't need this, but maybe in the future...)
				}else{
					xmlCancel = true;
					//logger.info("cancel " + lastNode.getPath()[lastNode.getPath().length-1]);
					DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
					TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
					tree.scrollPathToVisible(tpath);
					tree.setSelectionPath(tpath);
					glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					glassPane.setVisible(false);
					return;
				}
			}
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("3D Model Files", "3mf");
			fileChooser.setFileFilter(filter);
			fileChooser.setPreferredSize(new Dimension(600, 400));

			if(!lastFile.equals("")){
				fileChooser.setCurrentDirectory(new File(lastFile));
				fileChooser.setSelectedFile(new File(lastFile));
				fileChooser.ensureFileIsVisible(new File(lastFile));
			}else {
				if(currentDir != "")fileChooser.setCurrentDirectory(new File(currentDir));
			}
			int result = fileChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				if(!fileChooser.getSelectedFile().exists()){
					JOptionPane.showMessageDialog(null, "The file " + fileChooser.getSelectedFile() + " does not exist.\n"   
							, "File I/O error: error finding file.", JOptionPane.ERROR_MESSAGE);
					getContentPane().validate();
					getContentPane().repaint();
					glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					glassPane.setVisible(false);
					return;
				}
				selectedFile = fileChooser.getSelectedFile();
				currentFile = selectedFile.getAbsolutePath();
				String backFile = currentFile + ".bak";
				Path from = Paths.get(currentFile);
				Path to = Paths.get(backFile);
				try {
					Files.copy(from,  to, StandardCopyOption.REPLACE_EXISTING);
					//Which is faster
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Error creating backup file: " + backFile + "\n" + e1.getMessage()  
					+ "\nLoad proceeding without backup.", "File I/O error: creating backup file.", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace(); 
				}
				currentFile = currentFile.replaceAll("\\\\", "/");
				lastFile = currentFile;
				currentDir = currentFile.substring(0, currentFile.lastIndexOf("/")+1);
				unzip3mf();
				
				xmlEd.setText("");
				xmlStat.setText("");
				txtStat.setText("");
				xmlChanged = false;
				clearUndoAction.actionPerformed(null);
				zip64I.setEnabled(true);
				mfPane.setViewportView(getTree());
				setupFrame();
				getContentPane().validate();
				getContentPane().repaint();
				//Check for thumbnail in rels/.rels directory and display, if available. Otherwise, skip
				checkThumb();
				glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				glassPane.setVisible(false);

			}
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			glassPane.setVisible(false);
		}else if (choice == sBakI){
			Component glassPane = ((RootPaneContainer)mfPane.getTopLevelAncestor()).getGlassPane();
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			glassPane.setVisible(true);
			if(xmlChanged){
				//ask if file should be saved after changes
				int n = SaveDialog();
				//0 = yes, 1 = no, 2 = cancel
				if(n == 0){
					if(bSchemaChk){
						vCheckSchema();
						if(bSchemaFail){
							n = sfSaveDialog();
							if(n != 0){
								xmlCancel = true;
								DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
								TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
								tree.scrollPathToVisible(tpath);
								tree.setSelectionPath(tpath);
								glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								glassPane.setVisible(false);
								return;
							}
						}
					}
					nd.clear();
					TreeNode[] path = lastNode.getPath();
					//This path includes the filename that was prepended to each entry
					String parent = "";
					String slash = "/";
					String upFile ="";
					upFile += path[path.length-1];
					for(int j=0;j<path.length-1;j++){
						if(j>0){
							parent += path[j] + slash;
						}
					}
					if(parent.contentEquals("/")) parent = "";
					updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
					xmlChanged = false;
				}else if (n==1){
					//Do nothing and move on (don't need this, but maybe in the future...)
				}else{
					xmlCancel = true;
					DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
					TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
					tree.scrollPathToVisible(tpath);
					tree.setSelectionPath(tpath);
					glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					glassPane.setVisible(false);
					return;
				}
			}
			String backFile = currentFile + ".bak";
			Path from = Paths.get(currentFile);
			Path to = Paths.get(backFile);
			try {
				Files.copy(from,  to, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Error creating backup file: " + backFile + "\n" + e1.getMessage()  
				+ "\nLoad proceeding without backup.", "File I/O error: creating backup file.", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace(); 
			}
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			glassPane.setVisible(false);
		}else if (choice == lBakI){
			Component glassPane = ((RootPaneContainer)mfPane.getTopLevelAncestor()).getGlassPane();
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			glassPane.setVisible(true);
			String backFile = currentFile + ".bak";
			Path to = Paths.get(currentFile);
			Path from = Paths.get(backFile);
			try {
				Files.copy(from,  to, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Error creating backup file: " + backFile + "\n" + e1.getMessage()  
				+ "\nLoad proceeding without backup.", "File I/O error: creating backup file.", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace(); 
			}
			unzip3mf();
			xmlEd.setText("");
			xmlChanged = false;
			clearUndoAction.actionPerformed(null);
			zip64I.setEnabled(true);
			mfPane.setViewportView(getTree());
			setupFrame();
			getContentPane().validate();
			getContentPane().repaint();
			//Check for thumbnail in rels/.rels directory and display, if available. Otherwise, skip
			checkThumb();
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			glassPane.setVisible(false);

		}
		else if (choice == getSchI){
			getSchemaLoc();
			setIni();
		}
		else if (choice == exitI)
			dispose();
		else if (choice == cutI)
		{
			xmlEd.cut();
		}
		else if (choice == copyI){
			xmlEd.copy();
		}
		else if (choice == findI){
			int startFrom = xmlEd.getCaretPosition()+1;
			String inTxt = xmlEd.getSelectedText();

			sFind = (String) JOptionPane.showInputDialog(null,"Enter text to find:",
					"Find Text",JOptionPane.QUESTION_MESSAGE,null,null,inTxt);
			if(sFind == null || sFind == "") return;
			javax.swing.text.Document d = xmlEd.getDocument();
			String content = null;
			try {
				content = d.getText(0, d.getLength()).toLowerCase();
			} catch (BadLocationException e1) {
				logger.info(e1.getMessage());
				return;
			}
			sFind=sFind.toLowerCase();
			int ifnd = content.indexOf(sFind, startFrom);
			if (ifnd == -1){
				//wrap the search and try again
				ifnd = content.indexOf(sFind, 0);
				if(ifnd == -1){
					Toolkit.getDefaultToolkit().beep();
				}else{
					xmlEd.setCaretPosition(ifnd);
				}
			}else{
				xmlEd.setCaretPosition(ifnd);
			}
		}
		else if (choice == findaI){
			if(sFind == null || sFind == ""){
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			int startFrom = xmlEd.getCaretPosition()+1;
			javax.swing.text.Document d = xmlEd.getDocument();
			String content = null;
			try {
				content = d.getText(0, d.getLength()).toLowerCase();
			} catch (BadLocationException e1) {
				logger.info(e1.getMessage());
				return;
			}
			sFind=sFind.toLowerCase();
			int ifnd = content.indexOf(sFind, startFrom);
			if (ifnd == -1){
				//wrap the search and try again
				ifnd = content.indexOf(sFind, 0);
				if(ifnd == -1){
					Toolkit.getDefaultToolkit().beep();
				}else{
					xmlEd.setCaretPosition(ifnd);
				}
			}else{
				xmlEd.setCaretPosition(ifnd);
			}
		}
		else if (choice == pasteI)
			xmlEd.paste();
		else if (choice == selectI){
			xmlEd.selectAll();
		}
		else if (choice == aboutI){
			AboutDialog ad = new AboutDialog();
			ad.setVisible(true);
		}
		else if (choice == helpI){
			try {
				Desktop desktop = null;

				File mfFile = new File("3MF_Editor_Guide_v1_00.pdf");
				if(!mfFile.exists()){
					try {
						URL inputUrl = getClass().getResource("/resources/3MF_Editor_Guide_v1_00.pdf");
						File dest = new File("3MF_Editor_Guide_v1_00.pdf");
						FileUtils.copyURLToFile(inputUrl, dest);					} catch (Exception e1) {
							JOptionPane.showMessageDialog(null, "Error: creating 3MF_Editor_Guide_v1_00.pdf for api documentation. " + e1.getMessage() + "\nExiting."   
									, "File I/O error: creating documentation.", JOptionPane.ERROR_MESSAGE);
							dispose();
						}
				}

				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					desktop.open(mfFile);
				} else {
					JOptionPane.showMessageDialog(null,
							"Error launching file: 3MF_Editor_Guide_v1_00.pdf\ntxt does not appear to be supported.",
							"File Launch error: Not supported.", JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception e1) {
				logger.info("No 3MF_Editor_Guide_v1_00.pdf file is available to open");
			}

		}
		else if (choice == chkSchI){
			bSchemaFail = false;
			if(!xmlEd.getText().trim().startsWith("<?xml")){
				addToConsole("<br>" +
						xFilePath(lastNode) + " does not appear to start with \"<?xml\"<br>");
				return;
			}
			//This path includes the filename that was prepended to each entry
			String upFile = xFilePath(lastNode);
			if(!upFile.startsWith("[Content_Types].")){
				String ext = upFile.substring(upFile.lastIndexOf(".")+1);
				String typ = contentType.get(ext);
				if(typ == null) typ = "";
				if (typ.equals("application/vnd.ms-printing.printticket+xml")){
					addToConsole("<br>" +
							upFile + " will not be evaluated as the type is a Print Ticket.<br>");
					return;
				}
			}
			if(!xmlValid(xmlEd.getText())){
				bSchemaFail = true;
				JOptionPane.showMessageDialog(null, "Error validating xml for  " + xFilePath(lastNode),   
						"Xml Validation Failure", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (choice == chkSchZipI){
			if(xmlChanged){
				int n = SaveDialog();
				//0 = yes, 1 = no, 2 = cancel
				if(n == 0){
					TreeNode[] path = lastNode.getPath();
					//This path includes the filename that was prepended to each entry
					String parent = "";
					String slash = "/";
					String upFile ="";
					upFile += path[path.length-1];
					for(int j=0;j<path.length-1;j++){
						if(j>0){
							parent += path[j] + slash;
						}
					}
					if(parent.contentEquals("/")) parent = "";
					updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
					xmlChanged = false;
				}else if (n==1){
					//Do nothing and move on (don't need this, but maybe in the future...)
				}else{
					xmlCancel = true;
					//logger.info("cancel " + lastNode.getPath()[lastNode.getPath().length-1]);
					DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
					TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
					tree.scrollPathToVisible(tpath);
					tree.setSelectionPath(tpath);
					return;
				}
			}
			chkSchZip();
		}
		else if (choice == chk3MfI){
			if(xmlChanged){
				int n = SaveDialog();
				//0 = yes, 1 = no, 2 = cancel
				if(n == 0){
					TreeNode[] path = lastNode.getPath();
					//This path includes the filename that was prepended to each entry
					String parent = "";
					String slash = "/";
					String upFile ="";
					upFile += path[path.length-1];
					for(int j=0;j<path.length-1;j++){
						if(j>0){
							parent += path[j] + slash;
						}
					}
					if(parent.contentEquals("/")) parent = "";
					updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
					xmlChanged = false;
				}else if (n==1){
					//Do nothing and move on (don't need this, but maybe in the future...)
				}else{
					xmlCancel = true;
					DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
					TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
					tree.scrollPathToVisible(tpath);
					tree.setSelectionPath(tpath);
					return;
				}
			}
			chkSchZip();
			if(!bChkError || bSChkWerrors){
				chk3MfStructure();
			} else {
				saveForConsole += "<br>Aborting file structure check due to schema violations!<br>";
				addToConsole(saveForConsole);
			}
		}

		else if (choice == pPrntI){
			Component glassPane = ((RootPaneContainer)mfPane.getTopLevelAncestor()).getGlassPane();
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			glassPane.setVisible(true);
			String xmlTmp = xmlEd.getText();
			String xmlResult = formatXmlString(xmlTmp);
			if(xmlResult.contentEquals("")){
				JOptionPane.showMessageDialog(null, "Error pretty printing: " + currentFile,   
						"Pretty Print Failure", JOptionPane.ERROR_MESSAGE);

			}else{
				xmlEd.setText(xmlResult);
				clearUndoAction.actionPerformed(null);
			}
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			glassPane.setVisible(false);
		}

		else if (choice == unDoI)
			undoAction.actionPerformed(null);
		else if (choice == reDoI)
			redoAction.actionPerformed(null);
		else if (choice == upFontI){
			Font f = xmlEd.getFont();
			Font nf = f.deriveFont(f.getSize2D()+1.0f);
			xmlEd.setFont(nf);
			f = txtStat.getFont();
			nf = f.deriveFont(f.getSize2D()+1.0f);
			txtStat.setFont(nf);
			f = txtConsole.getFont();
			nf = f.deriveFont(f.getSize2D()+1.0f);
			txtConsole.setFont(nf);
			f = tree.getFont();
			nf = f.deriveFont(f.getSize2D()+1.0f);
			tree.setFont(nf);
		}
		else if (choice == downFontI){
			Font f = xmlEd.getFont();
			Font nf = f.deriveFont(f.getSize2D()-1.0f);
			xmlEd.setFont(nf);
			f = txtStat.getFont();
			nf = f.deriveFont(f.getSize2D()-1.0f);
			txtStat.setFont(nf);
			f = txtConsole.getFont();
			nf = f.deriveFont(f.getSize2D()-1.0f);
			txtConsole.setFont(nf);
			f = tree.getFont();
			nf = f.deriveFont(f.getSize2D()-1.0f);
			tree.setFont(nf);
		}
		else if (choice == viewFileI){
			try {
				Desktop desktop = null;

				File mfFile = new File(currentFile);

				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					desktop.open(mfFile);
				} else {
					JOptionPane.showMessageDialog(null, "Error launching file: " + currentFile   
							+ "\n3mf does not appear to be supported.", "File Launch error: Not supported.",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception e1) {
				logger.info("No log file is available to open");
			}

		}
		else if (choice == viewPropI){
			setIni();
			try {
				Desktop desktop = null;

				File mfFile = new File("3MF.txt");

				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					desktop.open(mfFile);
				} else {
					JOptionPane.showMessageDialog(null,
							"Error launching file: 3MF.txt\ntxt does not appear to be supported.",
							"File Launch error: Not supported.", JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception e1) {
				logger.info("No 3MF.txt file is available to open");
			}

		}
		else if (choice == editPropI){
			getPropDat();
		}
		else if (choice == zip64I){
			zip64_3mf();
		}
		else if (choice == doStructureI){
			if(doStructureI.getText().contentEquals("Structure Check Override Off")){
				bSChkWerrors=false;
				doStructureI.setText("Structure Check Override On");
			}else{
				bSChkWerrors=true;
				doStructureI.setText("Structure Check Override Off");
			}
			setIni();
		}
	}

	
		

	public String getNodeString(Node node) {
		try {
			StringWriter writer = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
			String output = writer.toString();
			return output.substring(output.indexOf("?>") + 2);//remove <?xml version="1.0" encoding="UTF-8"?>
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return node.getTextContent();
	}

	

	private void saveUpdate(){
		if(currentFile == null || currentFile == "")return;
		//save the current file if not current
		if(xmlChanged){
			TreeNode[] path = lastNode.getPath();
			//This path includes the filename that was prepended to each entry
			String parent = "";
			String slash = "/";
			String upFile ="";
			upFile += path[path.length-1];
			for(int j=0;j<path.length-1;j++){
				if(j>0){
					parent += path[j] + slash;
				}
			}
			if(parent.contentEquals("/")) parent = "";
			if(!upFile.endsWith("/")){
				updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
			}
			xmlChanged = false;
		}
		//we are creating the M_ set which is an adaptation of the first test set that included slicestack
		currentFile = currentFile.replaceAll("\\\\", "/");
		String cname = currentFile.substring(currentFile.lastIndexOf("/")+1);
		if(cname.startsWith("M_"))
			return;
		cname = "M_" + cname;
		selectedFile = new File(currentFile.substring(0,currentFile.lastIndexOf("/")+1)+cname);
		Path from = Paths.get(currentFile);
		Path to = Paths.get(selectedFile.getAbsolutePath());
		try {
			Files.copy(from,  to, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		currentFile = selectedFile.getAbsolutePath();
		currentFile = currentFile.replaceAll("\\\\", "/");
		currentDir = currentFile.substring(0, currentFile.lastIndexOf("/")+1);
		//make a backup of the new save as file.
		String backFile = currentFile + ".bak";
		from = Paths.get(currentFile);
		to = Paths.get(backFile);
		try {
			Files.copy(from,  to, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			e1.printStackTrace(); 
		}
		xmlEd.setText("");
		xmlChanged = false;
		clearUndoAction.actionPerformed(null);
		mfPane.setViewportView(getTree());
		setupFrame();
		getContentPane().validate();
		getContentPane().repaint();

	}
	
	//Save file
	public int SaveDialog(){
		Object[] options = {"Yes,",	"No", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
				"The XML was modified. Do you want to save?",
				"Unsaved XML",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[2]);

		return n;
	}
	//Save with bad schema?
	public int sfSaveDialog(){
		Object[] options = {"Yes,",	"No", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
				"Do you want to continue to save with a schema validation error?",
				"Invalid XML",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[2]);

		return n;
	}
	//Don't do a schema check unless the file starts as an xml document
	public void vCheckSchema(){
		bSchemaFail = false;
		if(!xmlEd.getText().trim().startsWith("<?xml")){
			return;
		}
		String upFile = xFilePath(lastNode);
		if(!upFile.startsWith("[Content_Types].")){
			String ext = upFile.substring(upFile.lastIndexOf(".")+1);
			String typ = contentType.get(ext);
			if(typ == null) typ = "";
			if (typ.equals("application/vnd.ms-printing.printticket+xml")){
				return;
			}
		}
		if(!xmlValid(xmlEd.getText())){
			bSchemaFail = true;
			JOptionPane.showMessageDialog(null, "Error validating xml for  " + xFilePath(lastNode),   
					"Xml Validation Failure", JOptionPane.ERROR_MESSAGE);
		}

	}
	//Set the schema location
	private void getSchemaLoc(){
		if(StartUp.brunCli)
			return;
		JFileChooser fc = new JFileChooser();
		if(schemaLoc == ""){
			fc.setCurrentDirectory(new File(currentDir));
		}else{
			fc.setCurrentDirectory(new File(schemaLoc));
		}
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Get Schema Directory");
		int returnVal = fc.showSaveDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			if(!fc.getSelectedFile().exists()){
				JOptionPane.showMessageDialog(null, "The directory " + fc.getSelectedFile() + " does not exist.\n"   
						, "File I/O error: error finding directory.", JOptionPane.ERROR_MESSAGE);
				return;
			}
			File yourFolder = fc.getSelectedFile();
			schemaLoc = yourFolder.getAbsolutePath();
		}
	}
	//zip64 option for saving 3mf
	private void zip64_3mf(){
		//clear zips/temp
		try {
			if(!zipdir){
				JOptionPane.showMessageDialog(null, "The directory zips could not be created. Try restarting to use this.\n"   
						, "File I/O error: error creating directory.", JOptionPane.ERROR_MESSAGE);
				return;
			}
			File ztmp = new File("zips/temp");
			FileUtils.cleanDirectory(ztmp);
			ZipFileUtil.decompressZipAlt(currentFile,"zips/temp/");
			String name = currentFile.substring(currentFile.lastIndexOf("/"));
			FileUtils.deleteQuietly(new File("zips/" + name));
			ZipFileUtil.compressFile("zips/temp/", "zips/", name, "", "temp/");
			FileUtils.cleanDirectory(ztmp);
		} catch (IOException e) {
			e.printStackTrace();
			saveForConsole += "<font color='red'>" + e.getMessage() + "<br>Error creating zip64 for " + currentFile + "</font><br>";
			addToConsole(saveForConsole);
			return;
		}; 
	}
	//unzip a 3mf
	private void unzip3mf(){
		//clear zips/temp
		try {
			File ztmp = new File("ztmp");
			if(!ztmp.exists()){
				Files.createDirectories(ztmp.toPath());
			} else {
				FileUtils.cleanDirectory(ztmp);
			}
			ZipFileUtil.decompressZip(currentFile,"ztmp");
			// CLI opening does not build the thumbnail tree, which also loads keys.
			// Load them here so schema and structure validation can decrypt parts.
			findKeystore();
			checkRel0();
			loadContent();
			//ZipFileUtil.decompressZipAlt(currentFile,"ztmp");
		} catch (IOException e) {
			e.printStackTrace();
			saveForConsole += "<font color='red'>" + e.getMessage() + "<br>Error creating ztmp for " + currentFile + "</font><br>";
			addToConsole(saveForConsole);
			return;
		}; 
	}
	
	
	//check the schema for all xml files in the zip
	private void chkSchZip(){
		try{
			bChkError = false;
			saveForConsole="";
			File file = new File("ztmp");
			Collection<File> files = FileUtils.listFiles(file,TrueFileFilter.TRUE, TrueFileFilter.TRUE);
			File[] listOfFiles = files.toArray(new File[files.size()]);
			for(File f:listOfFiles){
				if (f == null)continue;
				//limit the size of what we try to validate. I have passed with 341MB.
				BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
				if(attr.size()> 514215936 ){
					if(!StartUp.brunCli){
						JOptionPane.showMessageDialog(null, "Part is too large for validation: " + f.getName()   
						, "Part too large for validation.", JOptionPane.WARNING_MESSAGE);
					} else {
						logger.info("Part is too large for validation: " + f.getName());
					}
					continue;
				}
				String unzipped = FileUtils.readFileToString(f, "UTF-8");
				if (unzipped.startsWith("%3McF")) {
					//see if we can decrypt
					String floc = f.getPath().substring(4); //skip the ztmp
					//See if we have this in keyinfo
					KeyInfo keyitem = CheckKeyInfo(floc);
					if(keyitem != null){
						DeCryptModel dcry = new DeCryptModel();
						dcry.sAAD = keyitem.getAAD();
						dcry.bSha256 = keyitem.getSha256();
						dcry.bNoComp = keyitem.getNoComp();
						dcry.FileLoc = "ztmp" + keyitem.getFile();
						dcry.sCT = keyitem.getCT();
						dcry.sIV = keyitem.getIV();
						dcry.sTAG = keyitem.getTAG();
						try {
							String dtext = dcry.DoDecrypt();
							if(!dtext.toLowerCase().startsWith(("<?xml"))){
							} else {
								unzipped = dtext;
								saveForConsole += "<font color='green'><br>Decrypted " + floc  + "</font>";
								addToConsole(saveForConsole);
							}
						} catch (Exception e) {
							if(!StartUp.brunCli){
								saveForConsole += "<font color='red'><br>Decryption failure for " + floc  + "<br>XML validation aborted for file " + floc + "</font><br>";
							} else {
								logger.info("Encrypted Part failed decryption: " + f.getName());
							}
							bChkError = true;
						}
					}
				}
				if(unzipped.startsWith("<?xml")){
					boolean chk = xmlValidCon(unzipped,f.getName());
					if(!chk) bChkError = true;
				}
			}
			System.gc();
			addToConsole(saveForConsole);
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	//run the structure check
	private void chk3MfStructure(){
		MenuSelectionManager.defaultManager().clearSelectedPath();
		Component glassPane = ((RootPaneContainer)mfPane.getTopLevelAncestor()).getGlassPane();
		glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		glassPane.setVisible(true);
		try{
			nfsc = new FileStructureCheck();
			nfsc.fCurrentFile = currentFile;
			nfsc.fSchemaLoc = schemaLoc;
			nfsc.iErrMaxStr = iErrMaxS;
			nfsc.keyinfo = keyinfo;
			nfsc.checkFile();
			String nsecure = " which appears to be using encryption.";
			if(nfsc.bnonSecure){
				nsecure = " which appears to not be using encryption.";
			}
			if(nfsc.fErrorReturn != ""){
				bStructError = true;
				saveForConsole += "<font color='red'>" + nfsc.fErrorReturn + "<br><br>Structural check failure on " + currentFile + nsecure + "</font><br>";
				addToConsole(saveForConsole);
				fxmlErrorReturn = nfsc.fxmlErrorReturn.replace("<br>", "");
			} else {
				saveForConsole += "<font color='green'><br>No structural errors found in " + currentFile + nsecure + "</font><br>";
				addToConsole(saveForConsole);
			}
			//if the editor has bdoWarn unchecked and it is not a cli run, or the cli has hide warnings set, skip
			if(nfsc.fWarningReturn != "" && ((!bdoWarn && !StartUp.brunCli) || (!StartUp.bHideWarnCLI && StartUp.brunCli)) ){
				bStructError = true;
				saveForConsole += "<font color='DAA520'><br><br>Warnings (may be intentional edits)<br>" + nfsc.fWarningReturn + "<br><br>Structural check warning on " + currentFile + "</font><br>";
				addToConsole(saveForConsole);
				fxmlWarningReturn = nfsc.fxmlWarningReturn.replace("<br>", "");
			}
			sErr = nfsc.sErr;
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			glassPane.setVisible(false);

		}
	}

	/**
	 *Update the file in the ztmp directory and then put it back into the 3mf.
	 * 
	 *
	 * @param szipFile The 3mf file to be updated
	 * @param parent The path for the file with an ending /
	 * @param fileName The file being updated
	 * @param text The text to place in the file 
	 * @return none.
	 */
	private void updateFileNZip(String szipFile, String parent, String fileName, String text){
		if(bSkipBig)return;
		Component glassPane = ((RootPaneContainer)mfPane.getTopLevelAncestor()).getGlassPane();
		glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		glassPane.setVisible(true);
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
			findKeystore();
			glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			glassPane.setVisible(false);
		}

		return;
	}
	//initialize all saved variables
	private void getIni(){
		getPropDat();
		//The ini has values like the screen sizing
		inDat.readIni();
		scrSzHgt = inDat.pscrSzHgt;
		scrSzWid = inDat.pscrSzWid;
		sclWid = inDat.psclWid;
		sclHgt = inDat.psclHgt;
		scrLocX = inDat.pscrLocX;
		scrLocY = inDat.pscrLocY;
		bSchemaChk = inDat.pbSchemaChk;
		lastFile = inDat.lastFile;
		thumbDir = inDat.thumbDir;
		transSize = inDat.transSize;
		doSchema.setSelected(bSchemaChk);
		bwire.setSelected(inDat.pbwire);
		bnumb.setSelected(inDat.pbnumb);
		bmesh.setSelected(inDat.pbmesh);
		bNoUUIDchk.setSelected(inDat.pNoUUIDchk);
		bdoScroll = inDat.bdoScroll;
		if(bdoScroll){
			if(!doScroll.isSelected()){
				doScroll.doClick();
			} else {
				DefaultCaret caret = (DefaultCaret)txtConsole.getCaret();
				caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			}
		}else{
			if(doScroll.isSelected()){
				doScroll.doClick();
			} else {
				DefaultCaret caret = (DefaultCaret)txtConsole.getCaret();
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			}
		}
		bdoWarn = inDat.bdoWarn;
		if(bdoWarn){
			if(!doWarn.isSelected()){
				doWarn.doClick();
			}
		}else{
			if(doWarn.isSelected()){
				doWarn.doClick();
			}
		}
		setIni();
	}
	//save the variables we care about
	private void setIni(){
		if(xmlChanged){
			//ask if file should be saved after changes
			int n = SaveDialog();
			//0 = yes, 1 = no, 2 = cancel
			if(n == 0){
				if(bSchemaChk){
					vCheckSchema();
					if(bSchemaFail){
						n = sfSaveDialog();
						if(n != 0){
							xmlCancel = true;
							DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
							TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
							tree.scrollPathToVisible(tpath);
							tree.setSelectionPath(tpath);
							return;
						}
					}
				}
				TreeNode[] path = lastNode.getPath();
				//This path includes the filename that was prepended to each entry
				String parent = "";
				String slash = "/";
				String upFile ="";
				upFile += path[path.length-1];
				for(int j=0;j<path.length-1;j++){
					if(j>0){
						parent += path[j] + slash;
					}
				}
				if(parent.contentEquals("/")) parent = "";
				updateFileNZip(currentFile, parent, upFile, xmlEd.getText());
				xmlChanged = false;
			}else if (n==1){
				//Do nothing and move on (don't need this, but maybe in the future...)
			}else{
				xmlCancel = true;
				DefaultTreeModel m_model = new DefaultTreeModel(lastNode.getRoot());
				TreePath tpath = new TreePath(m_model.getPathToRoot(lastNode));
				tree.scrollPathToVisible(tpath);
				tree.setSelectionPath(tpath);
				return;
			}
		}

		propDat.pcurrentDir = currentDir;
		propDat.pschemaLoc = schemaLoc;
		propDat.pticketLoc = ticketLoc;
		propDat.pprinterUrl = printerUrl;
		propDat.psns = sns;
		propDat.ppns = pns;
		propDat.ppindent = ppindent;
		propDat.iErrMaxStructure = iErrMaxS;
		propDat.iErrMaxValidataion = iErrMaxV;
		propDat.bStructureChkWerrors = bSChkWerrors;
		propDat.hVersion = hVersion;
		propDat.sValx = sValx;
		propDat.bedBuffer = bedBuffer;
		propDat.align_out_of_bounds_only = align_out_of_bounds_only;
		propDat.alignOrigin = alignOrigin;
		propDat.username = cusername;
		propDat.password = cpassword;
		propDat.writeTxt();
		int itmp = this.getBounds().height;
		if(itmp > 0)
			inDat.pscrSzHgt = itmp;
		itmp = this.getBounds().width;
		if(itmp > 0)
			inDat.pscrSzWid = itmp;
		itmp = this.getBounds().x;
		if(itmp > 0)
			inDat.pscrLocX = itmp;
		itmp = this.getBounds().y;
		if(itmp > 0)
			inDat.pscrLocY = itmp;
		inDat.psclWid = sclWid;
		inDat.psclHgt = sclHgt;
		inDat.pbSchemaChk = bSchemaChk;
		if(bwire.isSelected())
			inDat.pbwire = true;
		else
			inDat.pbwire = false;
		if(bnumb.isSelected())
			inDat.pbnumb = true;
		else
			inDat.pbnumb = false;
		if(bmesh.isSelected())
			inDat.pbmesh = true;
		else
			inDat.pbmesh = false;
		if(bNoUUIDchk.isSelected())
			inDat.pNoUUIDchk = true;
		else
			inDat.pNoUUIDchk = false;
		inDat.bdoScroll = bdoScroll;
		inDat.bdoWarn = bdoWarn;
		if(lastFile == null) lastFile = "";
		inDat.lastFile = lastFile;
		inDat.thumbDir = thumbDir;
		inDat.transSize = transSize;
		inDat.writeIni();
	}
	//Get the properties. The user can change the properties. Values like the screen location are set in the ini and not generally exposed to the user
	private void getPropDat(){
		propDat.readTxt();
		currentDir = propDat.pcurrentDir;
		schemaLoc = propDat.pschemaLoc;
		ticketLoc = propDat.pticketLoc;
		printerUrl = propDat.pprinterUrl;
		hVersion = propDat.hVersion;
		sValx = propDat.sValx;
		sns = propDat.psns;
		pns = propDat.ppns;
		cusername = propDat.username;
		cpassword = propDat.password;
		ppindent = propDat.ppindent;
		iErrMaxS = propDat.iErrMaxStructure;
		iErrMaxV = propDat.iErrMaxValidataion;
		bSChkWerrors = propDat.bStructureChkWerrors;
		bedBuffer = propDat.bedBuffer;
		align_out_of_bounds_only = propDat.align_out_of_bounds_only;
		alignOrigin = propDat.alignOrigin;
		if(bSChkWerrors){
			doStructureI.setText("Structure Check Override Off");
		} else {
			doStructureI.setText("Structure Check Override On");
		}
	}
	public void loadContent(){
		try {
			contentType.clear();
			File cfile = new File("ztmp/[Content_Types].xml");
			if(!cfile.exists()){
				saveForConsole += "<font color='red'><br>Failed to locate the [Content_Types].xml file.</font><br>";
				addToConsole(saveForConsole);
				logger.info("Failed to locate the [Content_Types].xml file." );
				return;
			}
			String xfile = FileUtils.readFileToString(cfile, "UTF-8");
			if(!xfile.trim().toLowerCase().startsWith("<?xml")){
				saveForConsole += "<font color='red'><br>Failed to locate the [Content_Types].xml file with an xml header in " + currentFile + "</font><br>";
				addToConsole(saveForConsole);
				logger.info("Failed to locate the [Content_Types].xml file with an xml header in " + currentFile );
				return;
			}
	
			//check content types:
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);//needed to use getElementsByTagNameNS
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc;
				doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xfile.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			NodeList nodeList2 = doc.getElementsByTagName("*");
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node node = nodeList2.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if(node.getNodeName().equals("Default")){
						String ext = node.getAttributes().getNamedItem("Extension").getTextContent();
						String typ = node.getAttributes().getNamedItem("ContentType").getTextContent();
						contentType.put(ext,typ);
					}
				}
			}
		} catch (SAXException | IOException |ParserConfigurationException e) {
			saveForConsole += "<font color='red'><br>Failed to part the [Content_Types].xml file with an xml header in " + currentFile + "</font><br>";
			addToConsole(saveForConsole);
			logger.info("Failed to parse the [Content_Types].xml file with an xml header in " + currentFile );
			return;
		}

	}
	//run an xml validation for a string
	public boolean xmlValid(String tmp) {
		//
		boolean outXml=false;
		String uWarn = "";
		int schemaErrorCount = 0;
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema"); 
			if(schemaLoc.length()<1){
				getSchemaLoc();
			}
			if(schemaLoc.length()<1){
				addToConsole("<br>Schema location not entered.<br>");
				logger.error("Schema location not entered.");
				return false;
			}

			//Check for types and relationships
			int ifst = tmp.indexOf(">");
			ifst = tmp.indexOf("<", ifst)+1;
			String aXsd = "/qli_3MF.xsd";
			if(tmp.substring(ifst,ifst + 5).toLowerCase().contentEquals("types")) aXsd="/opc-contentTypes.xsd";
			if(tmp.substring(ifst,ifst + 14).toLowerCase().contentEquals("coreproperties")) aXsd="/opc-coreProperties.xsd";
			if(tmp.substring(ifst,ifst + 13).toLowerCase().contentEquals("relationships")) aXsd="/opc-relationships.xsd";
			if(tmp.substring(ifst,ifst + 8).toLowerCase().contentEquals("keystore")) aXsd="/qli_SecureContent.xsd";
			if (!(new File(schemaLoc + aXsd).exists())){
				addToConsole("<br>Could not find schema in: " + schemaLoc + "<br>");
				logger.error("Could not find schema in: " + schemaLoc);
				return false;
			}
			File schemaLocation = new File(schemaLoc + aXsd);
			Schema schemaGrammar = schemaFactory.newSchema(schemaLocation ); 

			Validator schemaValidator = schemaGrammar.newValidator(); 
			xsdErrorList.clear();
			schemaValidator.setErrorHandler(new XSDValidationLoggingErrorHandler()); 
			// validate xml instance against the grammar. 
			schemaValidator.validate(new StreamSource(new StringReader(tmp))); 
			boolean bWarnUUID = false;
			uWarn = "";
			if(bNoUUIDchk.isSelected()){
				xsdErrorListRm.clear();
				for (SAXParseException saxParseException : xsdErrorList) {
					if(saxParseException.getMessage().contains("Attribute 'UUID' must appear")){
						bWarnUUID = true;
						xsdErrorListRm.add(saxParseException);
					} else if (saxParseException.getMessage().contains("Attribute 'UUID' belonging to namespace")) {
						bWarnUUID = true;
						xsdErrorListRm.add(saxParseException);
					}
				}
				if (!xsdErrorListRm.isEmpty()) {
					for (SAXParseException saxParseException : xsdErrorListRm) {
						xsdErrorList.remove(saxParseException);
					}					
				}
			}
			if(bWarnUUID){
				uWarn = "<font color='DAA520'><br>XML validation for \"" 
						+ xFilePath(lastNode) + "\" ignoring UUID errors.<br></font>";
			}
			if (!xsdErrorList.isEmpty()) { 
				StringBuilder xsdErrorText = new StringBuilder(200) 
						.append("<font color='red'>The following validation errors were found for  \"" 
								+ xFilePath(lastNode) + "\":") 
						.append("<br>");
				int ict = 0;
				if(xsdErrorList.size() > iErrMaxV){
					List<String> errList = new ArrayList<String>();
					for (SAXParseException saxParseException : xsdErrorList) {
						if(saxParseException.getMessage().contains("Invalid content was found starting with element") && 
						   !saxParseException.getMessage().split("}")[0].contains("http://schemas.microsoft.com/3dmanufacturing/")) {
								schemaErrorCount++;
						}
						errList.add(saxParseException.getMessage() + "|" + saxParseException.getLineNumber());
					}
					xsdErrorText.append("<br>Found " + Integer.toString(xsdErrorList.size()) 
					+ " errors. Using the short, grouped form.<br>");
					String type = "";
					Collections.sort(errList);
					for (String sErr:errList){
						ict++;
						String[] sErrType = sErr.split(":");
						String cur = sErrType[0];
						if (!type.equals(cur)){
							ict = 0;
							type = cur;
							String[] sErrType2 = sErr.split("\\|");
							xsdErrorText.append("<br>line: <a href=\"" + sErrType2[1]
									+ "\">" + sErrType2[1] + "</a>:" + sErrType2[0] + "<br>"); 
						} else if(ict<iErrMaxV){
							String[] sErrType2 = sErr.split("\\|");
							xsdErrorText.append("line: <a href=\"" + sErrType2[1]
									+ "\">" + sErrType2[1] + "</a>:" + sErrType2[0] + "<br>"); 
						}
					}
				} else {
					ict = 0;
					String sError="";
					for (SAXParseException saxParseException : xsdErrorList) { 
						if(saxParseException.getMessage().contains("Invalid content was found starting with element") && 
								   !saxParseException.getMessage().split("}")[0].contains("http://schemas.microsoft.com/3dmanufacturing/")) {
							schemaErrorCount++;
						}
						ict++;
						if(ict>iErrMaxV)sError = "<br>More than " + Integer.toString(iErrMaxV) + " errors found, exiting.";
						xsdErrorText.append("line: ") 
						.append("<a href=\"")
						.append(saxParseException.getLineNumber())
						.append("\">")
						.append(saxParseException.getLineNumber()) 
						.append("</a>:") 
						.append(saxParseException.getMessage()) 
						.append(sError)
						.append("<br>");
						if (ict > iErrMaxV){
							break;
						}
					} 
				}
				if(xsdErrorList.size() == schemaErrorCount) {
					xsdErrorText.append("</font>");
					if(!bdoWarn) {
						String errorText = xsdErrorText.toString().replace("color='red'", "color='DAA520'");
						addToConsole(errorText);
						outXml=true;
						return outXml;
					}
					outXml=true;
				}else {
					xsdErrorText.append("</font>");
					addToConsole(uWarn + xsdErrorText.toString());
					return false;
				}
			} else { 
				outXml=true;
			}

		} catch (SAXException e) {
			addToConsole("uWarn + <font color='red'><br>Error in XML validation for \"" 
					+ xFilePath(lastNode) + "\": "  +e.getMessage() + "<br></font>");
			return false;
		} catch (IOException e) { 
			addToConsole("uWarn + <font color='red'><br>IO Error in XML validation for \"" 
					+ xFilePath(lastNode) + "\": "  +e.getMessage() + "<br></font>");
			return false;
		} 
		addToConsole(uWarn + "<font color='green'><br>XML validation for \"" 
				+ xFilePath(lastNode) + "\" did not return any errors.<br></font>");
		return outXml;
	}

	public boolean xmlValidCon(String tmp, String name) {
		
		//named schema validation
		String upFile = name;
		if(!upFile.startsWith("[Content_Types].")){
			String ext = upFile.substring(upFile.lastIndexOf(".")+1);
			String typ = contentType.get(ext);
			if(typ == null) typ = "";
			if (typ.equals("application/vnd.ms-printing.printticket+xml")){
				saveForConsole += "<font color='green'><br>XML validation for \"" 
						+ name + "\" will not be evaluated as the type is a Print Ticket.<br></font>";
				return true;
			}
		}
		int schemaErrorCount = 0;
		boolean outXml=false;
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema"); 
			if(schemaLoc.length()<1){
				getSchemaLoc();
			}
			if(schemaLoc.length()<1){
				addToConsole("<br>Schema location not entered.<br>");
				return false;
			}

			//Check for types and relationships
			int ifst = tmp.indexOf(">");
			ifst = tmp.indexOf("<", ifst)+1;
			String aXsd = "/qli_3MF.xsd";
			if(tmp.substring(ifst,ifst + 5).toLowerCase().contentEquals("types")) aXsd="/opc-contentTypes.xsd";
			if(tmp.substring(ifst,ifst + 14).toLowerCase().contentEquals("coreproperties")) aXsd="/opc-coreProperties.xsd";
			if(tmp.substring(ifst,ifst + 13).toLowerCase().contentEquals("relationships")) aXsd="/opc-relationships.xsd";
			if(tmp.substring(ifst,ifst + 8).toLowerCase().contentEquals("keystore")) aXsd="/qli_SecureContent.xsd";
			if (!(new File(schemaLoc + aXsd).exists())){
				addToConsole("<br>Could not find schema in: " + schemaLoc + "<br>");
				fxmlSchemaReturn += "<Issue  issueID=\"SC_000\" severity=\"critical\" class=\"schema\" type=\"file  error\">";
				fxmlSchemaReturn +="Could not find schema in: " + schemaLoc;
				fxmlSchemaReturn += "</Issue>";
				return false;
			}
			File schemaLocation = new File(schemaLoc + aXsd);
			Schema schemaGrammar = schemaFactory.newSchema(schemaLocation ); 

			Validator schemaValidator = schemaGrammar.newValidator(); 
			xsdErrorList.clear();
			schemaValidator.setErrorHandler(new XSDValidationLoggingErrorHandler()); 
			// validate xml instance against the grammar. 
			schemaValidator.validate(new StreamSource(new StringReader(tmp))); 
			boolean bWarnUUID = false;
			if(bNoUUIDchk.isSelected()){
				xsdErrorListRm.clear();
				for (SAXParseException saxParseException : xsdErrorList) {
					if(saxParseException.getMessage().contains("Attribute 'UUID' must appear")){
						bWarnUUID = true;
						xsdErrorListRm.add(saxParseException);
					} else if (saxParseException.getMessage().contains("Attribute 'UUID' belonging to namespace")) {
						bWarnUUID = true;
						xsdErrorListRm.add(saxParseException);
					}
				}
				if (!xsdErrorListRm.isEmpty()) {
					for (SAXParseException saxParseException : xsdErrorListRm) {
						xsdErrorList.remove(saxParseException);
					}					
				}
			}
			if(bWarnUUID){
				saveForConsole += "<font color='DAA520'><br>XML validation for \"" 
						+ name + "\" ignoring UUID errors.<br></font>";
			}
			if (!xsdErrorList.isEmpty()) { 
				StringBuilder xsdErrorText = new StringBuilder(200) 
						.append("<font color='red'><br>The following validation errors were found for  \"" 
								+ name + "\":") 
						.append("<br>"); 
				int ict = 0;
				if(xsdErrorList.size() > iErrMaxV){
					List<String> errList = new ArrayList<String>();
					for (SAXParseException saxParseException : xsdErrorList) {
						errList.add(saxParseException.getMessage() + "|" + saxParseException.getLineNumber());
						if(saxParseException.getMessage().contains("Invalid content was found starting with element") && 
								   !saxParseException.getMessage().split("}")[0].contains("http://schemas.microsoft.com/3dmanufacturing/")) {
							schemaErrorCount++;
						}
					}	
					xsdErrorText.append("<br>Found " + Integer.toString(xsdErrorList.size()) 
					+ " errors. Using the short, grouped form.<br>");
					String type = "";
					Collections.sort(errList);
					for (String sErr:errList){
						ict++;
						String[] sErrType = sErr.split(":");
						String cur = sErrType[0];
						if (!type.equals(cur)){
							ict = 0;
							type = cur;
							String[] sErrType2 = sErr.split("\\|");
							xsdErrorText.append("<br>line: " + sErrType2[1]
									+ " " + sErrType2[0] + "<br>"); 
							fxmlSchemaReturn += "<Issue  issueID=\"SC_000\" severity=\"critical\" class=\"schema\" type=\"validation  error\">";
							fxmlSchemaReturn +="line: " + sErrType2[1]
									+ " " + sErrType2[0] + " In " + name;
							fxmlSchemaReturn += "</Issue>";
						} else if(ict<iErrMaxV){
							String[] sErrType2 = sErr.split("\\|");
							xsdErrorText.append("line: " + sErrType2[1]
									+ " " + sErrType2[0] + "<br>"); 
							fxmlSchemaReturn += "<Issue  issueID=\"SC_000\" severity=\"critical\" class=\"schema\" type=\"validation  error\">";
							fxmlSchemaReturn +="line: " + sErrType2[1]
									+ " " + sErrType2[0] + " In " + name;
							fxmlSchemaReturn += "</Issue>";
						}
					}
				} else {
					ict = 0;
					String sError="";
					for (SAXParseException saxParseException : xsdErrorList) { 
						if (saxParseException.getMessage().contains(
								"Invalid content was found starting with element") && 
								   !saxParseException.getMessage().split("}")[0].contains("http://schemas.microsoft.com/3dmanufacturing/")) {
							schemaErrorCount++;
						}
						ict++;
						if(ict>iErrMaxV)sError = "<br>More than " + Integer.toString(iErrMaxV) + " errors found, exiting.";
						xsdErrorText.append("line: ") 
						.append(saxParseException.getLineNumber())
						.append(" ")
						.append(saxParseException.getMessage()) 
						.append(sError)
						.append("<br>");
						fxmlSchemaReturn += "<Issue  issueID=\"SC_000\" severity=\"critical\" class=\"schema\" type=\"validation  error\">";
						fxmlSchemaReturn +="line: " + saxParseException.getLineNumber()
						+ " " + saxParseException.getMessage() + " In " + name;
						fxmlSchemaReturn += "</Issue>";
						if (ict > iErrMaxV){
							break;
						}
					} 
				}
				if(xsdErrorList.size() == schemaErrorCount) {
					xsdErrorText.append("</font>");
					if(!bdoWarn) {
						String errorText = xsdErrorText.toString().replace("color='red'", "color='DAA520'");
						saveForConsole += errorText;
					}
					outXml=true;
				}else {
					xsdErrorText.append("</font>");
					saveForConsole += xsdErrorText.toString();
					return false;
				}
			} else { 
				outXml=true;
			}

		} catch (SAXException e) {
			saveForConsole += "<font color='red'><br>Error in XML validation for \"" 
					+ name + "\": "  +e.getMessage() + "<br></font>";
			return false;
		} catch (IOException e) { 
			saveForConsole += "<font color='red'><br>IO Error in XML validation for \"" 
					+ name + "\": "  +e.getMessage() + "<br></font>";
			return false;
		} 
		saveForConsole += "<font color='green'><br>XML validation for \"" 
				+ name + "\" did not return any errors.<br></font>";
		return outXml;
	}

	public static Boolean getWire(){
		if(bwire.isSelected())
			return true;
		else
			return false;
	}
	public static Boolean getNumb(){
		if(bnumb.isSelected())
			return true;
		else
			return false;
	}
	public static Boolean getMesh(){
		if(bmesh.isSelected())
			return true;
		else
			return false;
	}
	public static Boolean getNoUUIDchk(){
		if(bNoUUIDchk.isSelected())
			return true;
		else
			return false;
	}
	//text will be added to the console. The expected input is html.
	private static void addToConsole(String txt){
		txtConsole.setContentType("text/html");
		txtConsole.setText(txt);
	}
	//build our list of schema validation errors.
	private class XSDValidationLoggingErrorHandler implements ErrorHandler { 

		@Override 
		public void error(SAXParseException exception) throws SAXException { 
			xsdErrorList.add(exception); 
		} 

		@Override 
		public void fatalError(SAXParseException exception) throws SAXException { 
			xsdErrorList.add(exception); 
		} 

		@Override 
		public void warning(SAXParseException exception) throws SAXException { 
			xsdErrorList.add(exception); 
		} 

	} 
	//for undo manager
	class MyCompoundEdit extends CompoundEdit {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		boolean isUnDone=false;
		public int getLength() {
			return edits.size();
		}

		public void undo() throws CannotUndoException {
			super.undo();
			isUnDone=true;
		}
		public void redo() throws CannotUndoException {
			super.redo();
			isUnDone=false;
		}
		public boolean canUndo() {
			return edits.size()>0 && !isUnDone;
		}

		public boolean canRedo() {
			return edits.size()>0 && isUnDone;
		}

	}
	//The undo. It has a few issues with the xmleditor making a large paste look like a lot of smaller ones.
	class UndoManager extends AbstractUndoableEdit implements UndoableEditListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String lastEditName=null;
		ArrayList<MyCompoundEdit> edits=new ArrayList<MyCompoundEdit>();
		MyCompoundEdit current;
		int pointer=-1;

		public void undoableEditHappened(UndoableEditEvent e) {
			UndoableEdit edit=e.getEdit();
			if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
				AbstractDocument.DefaultDocumentEvent event=(AbstractDocument.DefaultDocumentEvent)edit;
				int start=event.getOffset();
				int len=event.getLength();
				boolean isNeedStart=false;
				if(event.getType().equals(DocumentEvent.EventType.CHANGE) ||
						event.getType().equals(DocumentEvent.EventType.INSERT) ){
					try {
						String text=event.getDocument().getText(start, len);
						if (text.contains("\n"))
							isNeedStart=true;
					} catch (BadLocationException e1) {
						Toolkit.getDefaultToolkit().beep();
						logger.info("undoableEditHappened: Problem getting text\n" + e1.getMessage());
					}
				}

				if (current==null) {
					isNeedStart=true;
				}
				else if (lastEditName==null || !lastEditName.equals(edit.getPresentationName())) {
					isNeedStart=true;
				}

				while (pointer<edits.size()-1) {
					edits.remove(edits.size()-1);
					isNeedStart=true;
				}
				if (isNeedStart) {
					createCompoundEdit();
				}

				current.addEdit(edit);
				lastEditName=edit.getPresentationName();
			}
		}

		public void createCompoundEdit() {
			if (current==null) {
				current= new MyCompoundEdit();
			}
			else if (current.getLength()>0) {
				current= new MyCompoundEdit();
			}

			edits.add(current);
			pointer++;
		}

		public void undo() throws CannotUndoException {
			if (!canUndo()) {
				xmlChanged = false;
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			MyCompoundEdit u=edits.get(pointer);
			u.undo();
			pointer--;
			if (!canUndo()) {
				xmlChanged = false;
			}

		}

		public void redo() throws CannotUndoException {
			if (!canRedo()) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}

			pointer++;
			MyCompoundEdit u=edits.get(pointer);
			u.redo();

		}

		public void clearUndo() throws CannotUndoException {
			pointer=-1;
			edits.clear();
			lastEditName=null;

		}

		public boolean canUndo() {
			return pointer>=0;
		}

		public boolean canRedo() {
			return edits.size()>0 && pointer<edits.size()-1;
		}

	}
	//get the path for a tree node (3mf file)
	private String xFilePath(DefaultMutableTreeNode node){
		TreeNode[] path = node.getPath();
		//This path includes the filename that was prepended to each entry
		//The filename is dropped.
		String fileN = "";
		String slash = "";
		for(int j=0;j<path.length;j++){
			if(j>0){
				fileN += slash + path[j];
				slash="/";
			}
		}
		return fileN;
	}
	private String locPath(TreePath path){
		//This path includes the filename that was prepended to each entry
		//The filename is dropped.
		String fileN = "";
		String slash = "";
		Object[] pathx = path.getPath();
		for(int j=0;j<pathx.length;j++){
			if(j>0){
				fileN += slash + pathx[j];
				slash="/";
			}
		}
		return fileN;
	}
	//The status in the lower right of the frame
	private void updateStatus(int linenumber, int columnnumber) {
		xmlStat.setText("Line: " + linenumber + " Column: " + columnnumber);
	}

	//The row for the cursor in the current editor
	private int getRow(int pos, JTextComponent editor) {
		int rn = (pos==0) ? 1 : 0;
		int caretPosition = editor.getCaretPosition();
		Element root = editor.getDocument().getDefaultRootElement();

		rn = root.getElementIndex( caretPosition ) + 1;
		return rn;
	}

	//move to the start of a line
	public static void gotoStartOfLine(JTextComponent component, int line)
	{
		Element root = component.getDocument().getDefaultRootElement();
		line = Math.max(line, 1);
		line = Math.min(line, root.getElementCount());
		int startOfLineOffset = root.getElement( line - 1 ).getStartOffset();
		component.setCaretPosition( startOfLineOffset );
	}

	//get the current column
	private int getColumn(int pos, JTextComponent editor) {
		try {
			return pos-Utilities.getRowStart(editor, pos)+1;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	//monitor the scroll
	class MyAdjustmentListener implements AdjustmentListener {
		public void adjustmentValueChanged(AdjustmentEvent evt) {
			Adjustable source = evt.getAdjustable();
			if (evt.getValueIsAdjusting()) {
				return;
			}
			int orient = source.getOrientation();
			if (orient == Adjustable.HORIZONTAL) {
				if(!(lastNode == null)){
					nd.addNodeH(xFilePath(lastNode), evt.getValue());
				}
			} else {
				if(!(lastNode == null)){
					nd.addNodeV(xFilePath(lastNode), evt.getValue());
				}
			}
		}
	}

	//pretty print XML
	private String formatXmlString(String file){
		try{
			if(file == null || file.contentEquals("")) return "";
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(new InputSource(new ByteArrayInputStream(file.getBytes("utf-8"))));

			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space(.)='']",
					document,
					XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);
				node.getParentNode().removeChild(node);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", ppindent);

			StringWriter stringWriter = new StringWriter();
			StreamResult streamResult = new StreamResult(stringWriter);

			transformer.transform(new DOMSource(document), streamResult);

			String result = stringWriter.toString();
			return result;
		} catch (SAXException | IOException | ParserConfigurationException 
				| TransformerFactoryConfigurationError | TransformerException | XPathExpressionException e) {
			logger.info("Pretty Print error: " + e.getMessage());
			return "";
		}

	}

	//reload the frame
	public void rLoad(){
		if(StartUp.brunCli)
			return;

		xmlEd.setText("");
		xmlChanged = false;
		clearUndoAction.actionPerformed(null);
		mfPane.setViewportView(getTree());
		setupFrame();
		getContentPane().validate();
		getContentPane().repaint();
		return;
	}

	//passback for console messages
	public static void statusUpdate(String txt) {
		if(saveForConsole == null)saveForConsole = "";
		saveForConsole += txt;
		addToConsole(saveForConsole);
	}

	//event driven listener. Consumes resources if used too much so mostly replaced by the passback call type above.
	//Works pretty well for the editor. The main issue was with the Test Manager and looping where too many events were stacked up.
	interface HelloListener {
		public void someoneSaidHello();
		public void editOption(String opt);
		public void statusUpdate(String txt);
	}

	// Someone who says "Hello"
	class Initiater {
		private List<HelloListener> listeners = new ArrayList<HelloListener>();

		public void addListener(HelloListener toAdd) {
			listeners.add(toAdd);
		}

		public void sayHello() {

			// Notify everybody that may be interested.
			for (HelloListener hl : listeners)
				hl.someoneSaidHello();
		}
		public void doEdit(String opt) {
			for (HelloListener hl : listeners){
				hl.editOption(opt);
			}				
		}
		public void doStatus(String txt) {
			for (HelloListener hl : listeners){
				hl.statusUpdate(txt);;
			}				
		}
	}

	// Someone interested in "Hello" events
	class Responder implements HelloListener {
		@Override
		public void someoneSaidHello() {
			rLoad();
		}
		@Override
		public void editOption(String opt) {
			if(opt.contentEquals("Cut")){
				cutI.doClick();
			}
			if(opt.contentEquals("Copy")){
				copyI.doClick();
			}
			if(opt.contentEquals("Paste")){
				pasteI.doClick();
			}
			if(opt.contentEquals("Select All")){
				selectI.doClick();
			}
			if(opt.contentEquals("Find")){
				findI.doClick();
			}
			if(opt.contentEquals("Find Again")){
				findaI.doClick();
			}
			if(opt.contentEquals("Undo")){
				unDoI.doClick();
			}
			if(opt.contentEquals("Redo")){
				reDoI.doClick();
			}
			if(opt.contentEquals("Validate XML")){
				chkSchI.doClick();
			}

		}
		@Override
		public void statusUpdate(String txt) {
			if(saveForConsole == null)saveForConsole = "";
			saveForConsole += txt;
			addToConsole(saveForConsole);
		}
	}

	//utc time returned
	public String ztimeA(Long fDate){
		Calendar cal2 = Calendar.getInstance();
		SimpleDateFormat SDF =
				new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );

		//        // Zulu is UTC 24-hour time, no DST.
		cal2.setTimeInMillis(fDate);
		String milliformat = SDF.format( cal2.getTime() );
		// convert from milliseconds to seconds
		// by chopping off last 3 digits and period
		String zulu = milliformat.substring( 0, 19 ) + 'Z';
		return zulu;
	}

	//resize an image for display
	public  BufferedImage resizeImage(BufferedImage image, int width, int height) {
		int type=0;
		type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height,type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	//put up a thumbnail. The panel sits on top of the editor when active and is hidded otherwise.
	public class ImagePanel extends JPanel {

		private static final long serialVersionUID = 1L;
		Image i;

		public Dimension getPreferredSize() {
			return i == null ? new Dimension(200, 200) : new Dimension(i.getWidth(null), i.getHeight(null));
		}
		public void paintImage(Graphics g, Image i)
		{	
			super.paint(g);
			g.drawImage(i, 0, 0, null);
		}

		public void paint(Graphics g)
		{	
			paintImage(g, i);
		}

		public void setImage(Image i)
		{	
			this.i = i;
		}
	}

	private String chkImage(File file){
		// create an image input stream from the specified file
		try {

			ImageInputStream iis;
			iis = ImageIO.createImageInputStream(file);
			// get all currently registered readers that recognize the image format
			Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
			if (!iter.hasNext()) {
				iis.close();
				return "";
			}

			// get the first reader
			ImageReader reader = iter.next();
			iis.close();
			BufferedImage fin= null;
			fin = ImageIO.read(file);
			ColorModel cm = fin.getColorModel();
			String hasAlpha = ".";
			if(cm.hasAlpha())
				hasAlpha = " with Alpha channel.";
			return reader.getFormatName() + " with " + cm.getNumColorComponents() + " color components" + hasAlpha;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}


	/**
	 * Get a grayscale image for a file with alpha channel
	 *
	 * @param File file to load 
	 * @return BufferedImage containing a rotated image from a reference file.
	 */
	@SuppressWarnings("rawtypes")
	private BufferedImage getAlpIn(File file){
		BufferedImage fin= null, fout=null;
		try {
			fin = ImageIO.read(file);
			if(!fin.getColorModel().hasAlpha()){
				return null;
			}
			int width1 = fin.getWidth(null);
			int height1 = fin.getHeight(null);

			//not all images are byte, so this is a problem
			if(fin.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE){
				final byte[] pixref = ((DataBufferByte) fin.getRaster().getDataBuffer()).getData();

				int iRefBpr = pixref.length/(width1 * height1); //bytes per row
				fout = new BufferedImage(height1, width1, BufferedImage.TYPE_BYTE_GRAY);
				byte[] outByte = new byte[width1*height1];
				int refStride = iRefBpr *  width1;
				int outStride = width1;
				for (int y = 0; y < height1; y++) {
					int ibr = refStride * y;
					int ibo = outStride * y;
					for (int x = 0; x < width1; x++) {
						outByte[ibo + x] = pixref[ibr +(x*iRefBpr)+0];
					}
				}
				fout.setData(Raster.createRaster(fout.getSampleModel(), new DataBufferByte(outByte, outByte.length), null) );
			} else {
				ColorModel grayscaleColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
						new int[] {16}, false, false, ComponentColorModel.BITMASK, DataBuffer.TYPE_USHORT);
				fout = new BufferedImage(grayscaleColorModel, fin.getAlphaRaster(), false, new Hashtable());

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fout;
	}

	//local computer information
	private String getComputerName()
	{
		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			return env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			return env.get("HOSTNAME");
		else
			return "Unknown Computer";
	}

	//Memory stats.
	public static String logMem(){
		int mb = 1024*1024;

		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();
		String tst = "";
		tst += "##### Heap utilization statistics [MB] #####   ";
		//Print used memory
		tst += "Used Memory:" 
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb + ", ";

		//Print free memory
		tst += "Free Memory:" 
				+ runtime.freeMemory() / mb + ", ";

		//Print total available memory
		tst += "Total Memory:" + runtime.totalMemory() / mb + ", ";

		//Print Maximum available memory
		tst += "Max Memory:" + runtime.maxMemory() / mb + ", ";
		logger.info(tst);
		return tst;
	}

	//Create an about screen
	class AboutDialog extends JDialog {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public AboutDialog() {
			setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			getContentPane().setLayout(spl);
			setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/resources/qe.png")));
			setTitle("About 3MF Edit and Test on " + getComputerName());
			JPanel bg = new JPanel();
			Image backgroundx = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/resources/QAbout1.png"));
			JLabel background = new JLabel(new ImageIcon(backgroundx));
			bg.add(background);
			Spring sWid = spl.getConstraint(SpringLayout.EAST, getContentPane());
			Spring sHgt = spl.getConstraint(SpringLayout.SOUTH, getContentPane());
			spl.getConstraints(background).setWidth(Spring.scale(sWid,1.0f));
			spl.getConstraints(background).setHeight(Spring.scale(sHgt,1.0f));

			JLabel name = new JLabel("<html><b><font size='5'>3MF Editor</b></font><br><font size='4'>Copyright 3MF Consortium<br>Version 1.13</font></html>");
			JButton close = new JButton("Close");
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					dispose();
				}
			});
			spl.putConstraint(SpringLayout.NORTH, close, 20, SpringLayout.NORTH, getContentPane());
			spl.putConstraint(SpringLayout.WEST, close, -80, SpringLayout.EAST, getContentPane());
			spl.putConstraint(SpringLayout.NORTH, name, 20, SpringLayout.NORTH, getContentPane());
			spl.putConstraint(SpringLayout.WEST, name, 40, SpringLayout.WEST, getContentPane());

			//close.setAlignmentX(0.5f);
			add(close);
			add(name);
			add(bg);
			setModalityType(ModalityType.APPLICATION_MODAL);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screenSize.width / 2);
			int y = (screenSize.height / 2);
			setSize(690, 410);
			setLocation(x, y);
		}
	}
	private void checkRel0(){
		try {
			//We are not trying to debug the structure here. If we do not find something, just return.
			File file = new File("ztmp/_rels/.rels");
			if(!file.exists()) return;

			String xfile;
			xfile = FileUtils.readFileToString(file, "UTF-8");
			if(!xfile.trim().toLowerCase().startsWith("<?xml"))	return;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);//needed to use getElementsByTagNameNS
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xfile.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if(node.getNodeName().equals("Relationship")){
						String type = node.getAttributes().getNamedItem("Type").getTextContent();						
						if(type.contentEquals("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel")){
							srel0 = node.getAttributes().getNamedItem("Id").getTextContent().trim();
							break;								
						}
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	private void checkThumb(){
		try {
			//We are not trying to debug the structure here. If we do not find something, just return.
			File file = new File("ztmp/_rels/.rels");
			if(!file.exists()) return;

			keyinfo.clear();
			findKeystore();
			String xfile, fFile = "";
			xfile = FileUtils.readFileToString(file, "UTF-8");
			if(!xfile.trim().toLowerCase().startsWith("<?xml"))	return;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);//needed to use getElementsByTagNameNS
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xfile.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if(node.getNodeName().equals("Relationship")){
						String type = node.getAttributes().getNamedItem("Type").getTextContent();						
						if(type.contentEquals("http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail") ||
								type.contentEquals("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dtexture")){
							fFile = node.getAttributes().getNamedItem("Target").getTextContent().trim().replaceAll("\\\\", "/");
							break;								
						}
					}
				}
			}
			if(fFile.equals("")) return;
			//go to the root so this always refreshes.
			TreeNode root = (TreeNode)tree.getModel().getRoot();
			tree.scrollPathToVisible(new TreePath(root));
			tree.setSelectionPath(new TreePath(root));

			fFile = selectedFile.getName() + fFile;

			//find the path for this in our tree.
			TreePath fThumb = findByName(tree, fFile.split("/"));
			if(fThumb == null)return;

			tree.scrollPathToVisible(fThumb);
			tree.setSelectionPath(fThumb);

		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	//locate the keystore, if it exists.
	//if found, load the resource data for decrypting files.
	private void findKeystore(){
		// Never retain a previous package's keys, even if relationships are missing.
		keyinfo.clear();
		try {
			//We are not trying to debug the structure here. If we do not find something, just return.
			File file = new File("ztmp/_rels/.rels");
			if(!file.exists()) return;
			String xfile, fFile = "";
			xfile = FileUtils.readFileToString(file, "UTF-8");
			if(!xfile.trim().toLowerCase().startsWith("<?xml"))	return;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);//needed to use getElementsByTagNameNS
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xfile.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			NodeList nl = doc.getElementsByTagName("Relationship");
			for (int ic = 0; ic < nl.getLength(); ic++) {
				Node defN = nl.item(ic);
				String ltarg = defN.getAttributes().getNamedItem("Target").getTextContent();
				String ltype = defN.getAttributes().getNamedItem("Type").getTextContent();
				if(ltype.toLowerCase().endsWith("/keystore")){
					//found the keystore, now get the xml from the file
					File kfile = new File("ztmp" + ltarg);
					if(!kfile.exists()) return;
					fFile = FileUtils.readFileToString(kfile, "UTF-8");
					if(!fFile.trim().toLowerCase().startsWith("<?xml"))	return;
					Document doc2 = dBuilder.parse(new InputSource(new ByteArrayInputStream(fFile.getBytes("utf-8"))));
					doc2.getDocumentElement().normalize();
					NodeList nl2 = doc2.getElementsByTagName("resourcedatagroup");
				    String sCT = "";
				    //first pass to get the ct value, second pass to get the keys
					for (int ic2 = 0; ic2 < nl2.getLength(); ic2++) {
					    boolean bSha256 = false;
						for (int k=1; k<3; k++){
							//check for <accessright><kekparams attribute digestmethod> for sha256
							NodeList childList = nl2.item(ic2).getChildNodes();
							for (int j = 0; j < childList.getLength(); j++) {
								Node cnode = childList.item(j);
								if(k == 1){
									//find a ct that we can decrypt
									if (cnode.getNodeName().equals("accessright")) {
										NodeList accList = cnode.getChildNodes();
										for (int j2 = 0; j2 < accList.getLength(); j2++) {
											Node anode = accList.item(j2);
											if (anode.getNodeName().equals("kekparams")) {
												if(anode.getAttributes().getNamedItem("digestmethod") != null){
													if(anode.getAttributes().getNamedItem("digestmethod").getTextContent().trim().contains("sha256")){
														bSha256 = true;
													}
												}
											}
											if (anode.getNodeName().equals("cipherdata")) {
												NodeList dList = anode.getChildNodes();
												for (int j3 = 0; j3 < dList.getLength(); j3++) {
													Node dnode = dList.item(j3);
													if (dnode.getNodeName().contains("CipherValue")) {
														sCT = dnode.getTextContent().trim();
													}
												}
											}
										}
									
										try {
											//we just need the first good cipher
											//see if we can decrypt, is so, good to go and break
											DeCryptModel dcry = new DeCryptModel();
											PrivateKey pkey = dcry.getKey();
									        byte[] ct = Base64.getDecoder().decode(sCT);
											byte[] skey = dcry.pemdecrypt(pkey, ct, bSha256);
											if(skey.length > 2){
												//probably a good key.
												break;
											}
										} catch (Exception e) {
											//did not work, try again.
										}
									}
								} else {
									if (cnode.getNodeName().equals("resourcedata")) {
										KeyInfo keyin = new KeyInfo(); 
									    String sIV = "";
									    String sAAD = "";
									    String sTAG = "";
									    String sFileLoc = "";
										if(cnode.getAttributes().getNamedItem("path") != null){
											sFileLoc = cnode.getAttributes().getNamedItem("path").getTextContent().trim();
										}
										boolean bNoComp = false;
										NodeList cekList = cnode.getChildNodes();
										for (int j2 = 0; j2 < cekList.getLength(); j2++) {
											Node anode = cekList.item(j2);
											if (anode.getNodeName().equals("cekparams")) {
												//get compression from here? <cekparams compression="deflate"
												if(anode.getAttributes().getNamedItem("compression") != null){
													if(anode.getAttributes().getNamedItem("compression").getTextContent().trim().contains("none")){
														bNoComp = true;
													}
												}
												
												NodeList dList = anode.getChildNodes();
												for (int j3 = 0; j3 < dList.getLength(); j3++) {
													Node dnode = dList.item(j3);
													if (dnode.getNodeName().equals("iv")) {
														sIV = dnode.getTextContent().trim();
													}
													if (dnode.getNodeName().equals("tag")) {
														sTAG = dnode.getTextContent().trim();
													}
													if (dnode.getNodeName().equals("aad")) {
														sAAD = dnode.getTextContent().trim();
													}
												}										
											}
										}
										keyin.setAAD(sAAD);
										keyin.setCT(sCT);
										keyin.setFile(sFileLoc);
										keyin.setIV(sIV);
										keyin.setSha256(bSha256);
										keyin.setNoComp(bNoComp);
										keyin.setTAG(sTAG);
										keyinfo.add(keyin);
									}
								}
							}//end of childlist check
						}
					}
				}
			}

		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	// Finds the path in tree as specified by the node array. The node array is a sequence
	// of nodes where nodes[0] is the root and nodes[i] is a child of nodes[i-1].
	// Comparison is done using Object.equals(). Returns null if not found.
	public TreePath find(JTree tree, Object[] nodes) {
		TreeNode root = (TreeNode)tree.getModel().getRoot();
		return find2(tree, new TreePath(root), nodes, 0, false);
	}

	// Finds the path in tree as specified by the array of names. The names array is a
	// sequence of names where names[0] is the root and names[i] is a child of names[i-1].
	// Comparison is done using String.equals(). Returns null if not found.
	public TreePath findByName(JTree tree, String[] names) {
		TreeNode root = (TreeNode)tree.getModel().getRoot();
		return find2(tree, new TreePath(root), names, 0, true);
	}
	private TreePath find2(JTree tree, TreePath parent, Object[] nodes, int depth, boolean byName) {
		TreeNode node = (TreeNode)parent.getLastPathComponent();
		Object o = node;

		// If by name, convert node to a string
		if (byName) {
			o = o.toString();
		}

		// If equal, go down the branch
		if (o.equals(nodes[depth])) {
			// If at end, return match
			if (depth == nodes.length-1) {
				return parent;
			}

			// Traverse children
			if (node.getChildCount() >= 0) {
				for (Enumeration<?> e=node.children(); e.hasMoreElements(); ) {
					TreeNode n = (TreeNode)e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					TreePath result = find2(tree, path, nodes, depth+1, byName);
					// Found a match
					if (result != null) {
						return result;
					}
				}
			}
		}

		// No match at this branch
		return null;
	}
	
	private void fixThumb(){
		try {
			File file = new File("ztmp/_rels/.rels");
			if(!file.exists()){
				saveForConsole += "<font color='red'><br>Failed to locate the _rels/.rels file.</font><br>";
				addToConsole(saveForConsole);
				logger.info("Failed to locate the _rels/.rels file." );
				return;
			}
			String xfile, fFile = "";
			xfile = FileUtils.readFileToString(file, "UTF-8");
			if(!xfile.trim().toLowerCase().startsWith("<?xml"))	return;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);//needed to use getElementsByTagNameNS
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xfile.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("*");
			Boolean bUpdate = false;
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if(node.getNodeName().equals("Relationship")){
						String type = node.getAttributes().getNamedItem("Type").getTextContent();						
						if(type.contentEquals("http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail") ||
								type.contentEquals("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dtexture")){
							fFile = node.getAttributes().getNamedItem("Target").getTextContent().trim().replaceAll("\\\\", "/");
							if(fFile.equals("")) return;
							//if the name of the thumbnail starts with P_ or PX_ (the underscore being the critical item)
							//Then rename the thumbnail in the rels file and in the directory.
							String pname = fFile.substring(fFile.lastIndexOf("/")+ 1);
							pname = pname.substring(0, pname.indexOf((".")));
							if(pname.substring(1,2).equals("_") || pname.substring(2,3).equals("_")){
								String pcname = currentFile.substring(currentFile.lastIndexOf("/")+ 1);
								pcname = pcname.substring(0, pcname.indexOf((".")));
								if(pcname.equals(pname)){
									return;
								}
								logger.info("renameing thumb " + pname + " to " + pcname);
								String nFile = fFile.replace(pname, pcname);
								node.getAttributes().getNamedItem("Target").setTextContent(nFile);
								bUpdate = true;
								new File("ztmp" + fFile).renameTo(new File("ztmp" + nFile));

							}
							break;								
						}
					}
				}
			}
			if(bUpdate){
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				transformer.transform(source, result);
				logger.info("Zipping " + currentFile);
				File zipFile = new File(currentFile);
				ZipFileUtil.compressFile("ztmp" , zipFile.getParent(), zipFile.getName(), "", "ztmp/");
				xmlEd.setText("");
				xmlChanged = false;
				clearUndoAction.actionPerformed(null);
				mfPane.setViewportView(getTree());
				setupFrame();
				getContentPane().validate();
				getContentPane().repaint();
				checkThumb();
			}
		
		} catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}
	

	private void exThumb(){
		try {
			File file = new File("ztmp/_rels/.rels");
			if(!file.exists()){
				saveForConsole += "<font color='red'><br>Failed to locate the _rels/.rels file.</font><br>";
				addToConsole(saveForConsole);
				logger.info("Failed to locate the _rels/.rels file." );
				return;
			}
			
			File fThumb = new File(currentDir + "Thumb");
			if(!fThumb.exists()) fThumb.mkdir();
			
			String xfile, fFile = "";
			xfile = FileUtils.readFileToString(file, "UTF-8");
			if(!xfile.trim().toLowerCase().startsWith("<?xml"))	return;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);//needed to use getElementsByTagNameNS
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xfile.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if(node.getNodeName().equals("Relationship")){
						String type = node.getAttributes().getNamedItem("Type").getTextContent();						
						if(type.contentEquals("http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail") ||
								type.contentEquals("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dtexture")){
							fFile = node.getAttributes().getNamedItem("Target").getTextContent().trim().replaceAll("\\\\", "/");
							if(fFile.equals("")) return;
							//if the name of the thumbnail starts with P_ or PX_ (the underscore being the critical item)
							//Then rename the thumbnail in the rels file and in the directory.
							logger.info("Copying thumb " + fFile + " to Thumb directory");
							saveForConsole += "Copying thumb " + fFile + " to Thumb directory<br>";
							addToConsole(saveForConsole);
							FileUtils.copyFileToDirectory(new File("ztmp" + fFile), fThumb);
							break;								
						}
					}
				}
			}
		
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
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
	public static String getUser(){
		return cusername;
	}
	public static String getPassword(){
		return cpassword;
	}
	private KeyInfo CheckKeyInfo(String floc){
		String lfloc = floc.replaceAll("\\\\", "/");
		KeyInfo key = null;
		KeyInfo keychk = null;
		for (int i=0; i<keyinfo.size(); i++){
			keychk = keyinfo.get(i);
			if(lfloc.equals(keychk.getFile())){
				key = keychk;
				break;
			}
		}
		
		return key;
	}
	
	public boolean canCopy(){
		if(xmlEd.getSelectionStart() == xmlEd.getSelectionEnd()){
			return false;
		}
		else{
			return true;
		}
		
	}
	
}


