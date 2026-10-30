package com.qualitylogic.x3mf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FileStructureCheck {
	static Log logger = LogFactory.getLog(Class.class);
	public String fCurrentFile = "";
	public String fSchemaLoc = "";
	public String fErrorReturn = "";
	public String fWarningReturn = "";
	public String fxmlErrorReturn = "";
	public String fxmlWarningReturn = "";
	public List<KeyInfo> keyinfo = null;
	public int iErrMaxStr = 9;
	public int iWrnMaxStr = 9;
	public boolean bnonSecure = true;
	private String sContent;
	private NodeList nType_default;
	private NodeList nType_override;
	private NodeList nRelationship;
	private boolean bLocate = false;
	private String sModel = "";
	private String sOverModel = "";
	private List<String> aRelFiles = new ArrayList<String>();
	private List<String> sOverRide = new ArrayList<String>();
	private List<String> sOverRideType = new ArrayList<String>();
	private List<String> entryNames = new ArrayList<String>();
	private List<Boolean> bentryNames = new ArrayList<Boolean>();
	private List<NodeList> laRelNodes = new ArrayList<NodeList>();
	private List<String> conTypes = new ArrayList<String>();
	private List<String> relTypes = new ArrayList<String>();
	private List<ArrayList<String>> extTypes = new ArrayList<ArrayList<String>>();
	private List<String> idUuid = new ArrayList<String>();
	private List<String> fileUuid = new ArrayList<String>();
	private List<String> stackId = new ArrayList<String>();
	private List<String> stackFileId = new ArrayList<String>();
	private List<String> nlist = new ArrayList<String>();
	private List<String> namelist = new ArrayList<String>();
	private List<String> objIdList = new ArrayList<String>();
	private List<String> objIdListRef = new ArrayList<String>();
	List<String> encryptTargsAll = new ArrayList<String>();
	private Integer ierCnt = 0;
	private Integer iwrCnt = 0;
	private boolean bwarnOnce = false;
	private static boolean bdoProduction = false;

	// private Map<String, Node> objectNodeList = new HashMap<String, Node>();
	private List<String> requiredExtensions = new ArrayList<String>();

	private List<String> ssIdList = new ArrayList<String>();
	private List<String> ssIdListRef = new ArrayList<String>();
	private List<String> basematerialsId = new ArrayList<String>();
	private List<String> texture2dId = new ArrayList<String>();
	private List<String> texture2dgroupId = new ArrayList<String>();
	private List<String> texture2dgroupTexId = new ArrayList<String>();
	private List<String> compositematerialsId = new ArrayList<String>();
	private List<String> compositematerialsIdIndicies = new ArrayList<String>();
	private List<String> colorgroupId = new ArrayList<String>();
	private List<String> multipropertiesId = new ArrayList<String>();
	private List<String> mustPreserve = new ArrayList<String>();
	private List<String> contentTypes = new ArrayList<String>();
	private Map<Integer, String> meshModId = new HashMap<Integer, String>();
	private Map<Integer, Boolean> meshBeamId = new HashMap<Integer, Boolean>();

	private Map<String, Integer> idWithCount = new HashMap<String, Integer>();
	private Map<String, String> idWithName = new HashMap<String, String>();
	private Map<String, Boolean> idUsed = new HashMap<String, Boolean>();
	private Map<String, NodeList> relationshipRels = new HashMap<String, NodeList>();
	private Map<String, String> targType = new HashMap<String, String>();
	private Map<String, String> targType_id = new HashMap<String, String>();
	private List<String> targLocation = new ArrayList<String>();
	private int iTexType = 4;
	private int iEncryptType = 8;
	private String srel0 = "rel0";

	private List<String> transformErrorLoc = new ArrayList<String>();
	private Boolean b2dSlices = false;
	private Boolean bKeystoreFound = false;
	private boolean bchkReqExtS = false;
	private boolean bchkReqExtP = false;
	private static String snsc = "";
	private static String pnsc = "";
	public static String sns = "http://schemas.microsoft.com/3dmanufacturing/slice/2015/07";
	public static String pns = "http://schemas.microsoft.com/3dmanufacturing/production/2015/06";
	public static String bns = "http://schemas.microsoft.com/3dmanufacturing/beamlattice/2017/02";
	public static String scns = "http://schemas.microsoft.com/3dmanufacturing/securecontent/2019/04";
	public static String bons = "http://schemas.microsoft.com/3dmanufacturing/booleanoperations/2023/07";
	public static String dns = "http://schemas.3mf.io/3dmanufacturing/displacement/2023/10";
	private Integer iErr = 1000;
	public String sErr = "";
	List<String> errList = Arrays.asList("ER_001", "ER_107", "ER_005", "ER_003", "ER_009", "ER_011", "ER_012", "ER_028",
			"ER_048", "ER_055", "ER_059", "ER_046", "ER_049", "ER_051", "ER_054", "ER_102", "ER_103", "ER_101",
			"ER_114", "ER_043", "ER_044", "ER_045", "ER_047", "ER_058", "ER_029", "ER_030", "ER_031", "ER_032",
			"ER_033", "ER_034", "ER_035", "ER_036", "ER_050", "ER_057", "ER_061", "ER_063", "ER_064", "ER_065",
			"ER_066", "ER_082", "ER_083", "ER_084", "ER_087", "ER_094", "ER_095", "ER_096", "ER_109", "ER_110",
			"ER_111", "ER_080", "ER_086", "ER_089", "ER_090", "ER_091", "ER_092", "ER_093", "ER_098", "ER_112",
			"ER_113", "ER_099", "ER_100", "ER_020", "ER_007", "ER_008", "ER_013", "ER_056", "ER_060",
			"ER_062", "ER_078", "ER_079", "ER_081", "ER_085", "ER_088", "ER_097", "ER_004", "ER_010", "ER_015",
			"ER_016", "ER_017", "ER_018", "ER_019", "ER_021", "WR_022", "ER_023", "ER_024", "ER_025", "ER_026",
			"ER_027", "ER_071", "ER_072", "ER_073", "ER_074", "ER_075", "ER_076", "ER_104", "ER_105", "ER_106",
			"ER_077", "ER_052", "ER_053", "ER_041", "ER_042", "ER_039", "ER_040", "ER_070", "ER_108", "ER_133",
			"ER_134", "ER_135", "ER_136", "ER_137", "ER_138", "ER_139", "WR_005", "WR_006", "WR_008", "WR_007",
			"WR_009", "WR_010", "WR_011", "WR_002", "WR_003", "WR_004", "WR_012", "WR_013", "WR_001", "WR_014",
			"WR_015", "WR_016", "WR_017", "WR_018", "WR_019", "WR_023", "WR_024", "WR_025", "ER_176");

	public FileStructureCheck() {
	}

	public void checkFile() {
		checkRel0();
		iWrnMaxStr = iErrMaxStr;

		conTypes.add("application/vnd.ms-package.3dmanufacturing-3dmodel+xml"); // •
																				// 3D/2D
																				// Model
		conTypes.add("application/vnd.ms-printing.printticket+xml"); // •
																		// PrintTicket
		conTypes.add("application/vnd.openxmlformats-package.core-properties+xml"); // •
																					// Core
																					// Properties
																					// part
		conTypes.add("application/vnd.openxmlformats-package.relationships+xml"); // •
																					// Relationships
																					// part
		conTypes.add("application/vnd.ms-package.3dmanufacturing-3dmodeltexture"); // •
																					// Texture
		conTypes.add("image/png"); // • Thumbnail
		conTypes.add("image/jpeg"); // • Thumbnail
		conTypes.add("application/vnd.ms-package.3dmanufacturing-keystore+xml"); // • Secure KeyStore

		relTypes.add("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel");
		relTypes.add("http://schemas.microsoft.com/3dmanufacturing/2013/01/printticket");
		relTypes.add("http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties");
		relTypes.add("");
		relTypes.add("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dtexture");
		relTypes.add("http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail");
		relTypes.add("http://schemas.openxmlformats.org/package/2006/relationships/mustpreserve");
		relTypes.add("http://schemas.microsoft.com/3dmanufacturing/2019/04/keystore");
		relTypes.add("http://schemas.openxmlformats.org/package/2006/relationships/encryptedfile");
		iTexType = relTypes.indexOf("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dtexture"); // for
																										// texture
																										// target
																										// check
		iEncryptType = relTypes.indexOf("http://schemas.openxmlformats.org/package/2006/relationships/encryptedfile");
		// 3dtexture can be either an image/png or an image/jpeg

		// Create empty ext types to save a position for overwriting
		// This will be loaded when reading the content, thus if they have not
		// been enumerated, they will not be filled
		// These will line up with the conTypes (7,8 align with relTypes 6 (-1
		// for the real index))

		// learned that more than one extension can be assigned for a contype,
		// so we need a list for each.
		for (int iz = 0; iz < 9; iz++) {
			extTypes.add(new ArrayList<String>());
		}

		idUuid.clear();
		fileUuid.clear();
		stackId.clear();
		stackFileId.clear();
		transformErrorLoc.clear();
		targType.clear();
		targType_id.clear();
		targLocation.clear();
		mustPreserve.clear();
		meshModId.clear();
		meshBeamId.clear();
		contentTypes.clear();
		encryptTargsAll.clear();
		b2dSlices = false;
		fErrorReturn = "";
		fxmlErrorReturn = "";
		fWarningReturn = "";
		fxmlWarningReturn = "";
		bKeystoreFound = false;
		sErr = "";
		if (fCurrentFile == "") {
			fErrorReturn += "<br>ER_001: The current file has not been set.<br>";
			fxmlErrorReturn += "<Issue  issueID=\"ER_001\" severity=\"critical\" class=\"structure\" type=\"file  error\">";
			fxmlErrorReturn += "The current file has not been set.";
			fxmlErrorReturn += "</Issue>";
			errChk("ER_001");
			return;
		}
		String sRootModel = "";
		ierCnt = 0;
		iwrCnt = 0;
		// 1) does the zip contain a [Content_Types].xml
		try (ZipFile zfile = new ZipFile(fCurrentFile)) {
			boolean bContent = false;
			boolean bRels = false;
			Enumeration<? extends ZipEntry> entries = zfile.entries();
			while (entries.hasMoreElements()) {
				System.gc();
				ZipEntry entry = entries.nextElement();
				if (entry.getSize() > 200000000) {
					if (!StartUp.brunCli) {
						JOptionPane.showMessageDialog(null,
								"Part is too large for structural check: " + entry.getName(),
								"Part too large for structural check.", JOptionPane.WARNING_MESSAGE);
					}
					fWarningReturn += "<br>WR_008: " + entry.getName() + " was skipped as too large.<br>";
					fxmlWarningReturn += "<Issue  issueID=\"WR_008\" severity=\"info\" class=\"structure\" type=\"memory limit\">";
					fxmlWarningReturn += entry.getName() + " was skipped as too large.";
					fxmlWarningReturn += "</Issue>";
					errChk("WR_008");
					iwrCnt++;
					if (iwrCnt > iWrnMaxStr) {
						fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
						return;
					}
					continue;
				}

				String name = entry.getName();
				entryNames.add("/" + name);
				if (name.endsWith(".rels") || name.endsWith("/"))
					bentryNames.add(true);
				else
					bentryNames.add(false);

				{
					// do the keystore check
					InputStream zin = zfile.getInputStream(entry);
					if (zin == null)
						continue;// Directory
					sContent = IOUtils.toString(zin, "UTF-8");
					if (sContent.trim().toLowerCase().startsWith("<?xml")) {
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document doc = dBuilder
								.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
						doc.getDocumentElement().normalize();
						Element root = doc.getDocumentElement();
						if (root != null) {
							String rootname = root.getNodeName();
							if (rootname.toLowerCase().equals("keystore")) {
								if (!bKeystoreFound) {
									fWarningReturn += "<br>WR_019: A keystore file was located but a relationship was not defined in the _rels/.rels file. The file is being treated as not secure.<br>";
									fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
									fxmlWarningReturn += "A keystore file was located but a relationship was not defined in the _rels/.rels file. The file is being treated as not secure..";
									fxmlWarningReturn += "</Issue>";
									errChk("WR_019");
									iwrCnt++;
									if (iwrCnt > iWrnMaxStr) {
										fWarningReturn += "<br>More than " + iWrnMaxStr
												+ " warnings found, stopping..<br>";
										zin.close();
										return;
									}

								}
								// validate that any consumerindex has a consumer to point to.
								NodeList nConsumer = doc.getElementsByTagName("consumer");
								int iConsumerCount = nConsumer.getLength();
								NodeList naccessright = doc.getElementsByTagName("accessright");
								for (int ic = 0; ic < naccessright.getLength(); ic++) {
									Node defN = naccessright.item(ic);
									if (defN.getAttributes().getNamedItem("consumerindex") != null) {
										String rid = defN.getAttributes().getNamedItem("consumerindex").getTextContent()
												.trim();
										if (Integer.parseInt(rid) >= iConsumerCount) {
											fErrorReturn += "<br>ER_029: Invalid consumerindex: " + rid
													+ " for element accessright in file: " + name
													+ " Must have a coresponding consumer to index into.";
											fxmlErrorReturn += "<Issue  issueID=\"ER_029\" severity=\"high\" class=\"structure\" type=\"ID error\">";
											fxmlErrorReturn += "Invalid consumerindex: " + rid
													+ " for element accessright in file: " + name
													+ " Must have a coresponding consumer to index into.";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_029");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}

										}
									}
								}
								NodeList nl2 = doc.getElementsByTagName("resourcedatagroup");
								String sCT = "";
								// first pass to get the ct value, second pass to get the keys
								for (int ic2 = 0; ic2 < nl2.getLength(); ic2++) {
									boolean bSha256 = false;
									// check for <accessright><kekparams attribute digestmethod> for sha256
									NodeList childList = nl2.item(ic2).getChildNodes();
									for (int j = 0; j < childList.getLength(); j++) {
										Node cnode = childList.item(j);
										// find a ct that we can decrypt
										if (cnode.getNodeName().equals("accessright")) {
											String rid = "with no consumerindex found";
											if (cnode.getAttributes().getNamedItem("consumerindex") != null) {
												rid = "consumerindex " + cnode.getAttributes()
														.getNamedItem("consumerindex").getTextContent().trim();
											}
											NodeList accList = cnode.getChildNodes();
											for (int j2 = 0; j2 < accList.getLength(); j2++) {
												Node anode = accList.item(j2);
												if (anode.getNodeName().equals("kekparams")) {
													if (anode.getAttributes().getNamedItem("digestmethod") != null) {
														if (anode.getAttributes().getNamedItem("digestmethod")
																.getTextContent().trim().contains("sha256")) {
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
												// we just need the first good cipher
												// see if we can decrypt, is so, good to go and break
												DeCryptModel dcry = new DeCryptModel();
												PrivateKey pkey = dcry.getKey();
												byte[] ct = Base64.getDecoder().decode(sCT);
												byte[] skey = dcry.pemdecrypt(pkey, ct, bSha256);
												if (skey.length < 2) {
													fWarningReturn += "<br>WR_019: CipherValue decryption failure in "
															+ name + " for " + rid + ".<br>";
													fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
													fxmlWarningReturn += "CipherValue decryption failure in " + name
															+ " for " + rid + ".";
													fxmlWarningReturn += "</Issue>";
													errChk("WR_019");
													iwrCnt++;
													if (iwrCnt > iWrnMaxStr) {
														fWarningReturn += "<br>More than " + iWrnMaxStr
																+ " warnings found, stopping..<br>";
														zin.close();
														return;
													}

												}
											} catch (Exception e) {
												// did not work, try again.
												fWarningReturn += "<br>WR_019: CipherValue decryption failure in "
														+ name + " for " + rid + ".<br>";
												fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
												fxmlWarningReturn += "CipherValue decryption failure in " + name
														+ " for " + rid + ".";
												fxmlWarningReturn += "</Issue>";
												errChk("WR_019");
												iwrCnt++;
												if (iwrCnt > iWrnMaxStr) {
													fWarningReturn += "<br>More than " + iWrnMaxStr
															+ " warnings found, stopping..<br>";
													zin.close();
													return;
												}
											}
										}
									}
								} // end of childlist check
									// duplicate resourcedata check and valid pointers
								NodeList nl = doc.getElementsByTagName("resourcedata");
								List<String> rdpath = new ArrayList<String>();
								for (int ic = 0; ic < nl.getLength(); ic++) {
									Node defN = nl.item(ic);
									String lpath = defN.getAttributes().getNamedItem("path").getTextContent().trim();
									if (rdpath.contains(lpath)) {
										// there should only be on valid path for each resourcedata
										fErrorReturn += "<br>ER_112: Duplicate resourcedata path in keystore: " + lpath
												+ " in file: " + name;
										fxmlErrorReturn += "<Issue  issueID=\"ER_004\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
										fxmlErrorReturn += "Duplicate resourcedata path in keystore: " + lpath
												+ " in file: " + name;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_112");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else {
										rdpath.add(lpath);
									}
									File file = new File("ztmp" + lpath);
									if (!file.exists()) {
										fErrorReturn += "<br>ER_028: " + lpath
												+ " was not found for the keystore resourcedata.<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_028\" severity=\"critical\" class=\"structure\" type=\"file error\">";
										fxmlErrorReturn += lpath + " was not found for the keystore resourcedata.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_028");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else {
										InputStream zin3 = zfile.getInputStream(zfile.getEntry(lpath.substring(1)));
										sContent = IOUtils.toString(zin3, "UTF-8");
										if (!sContent.startsWith("%3McF")) {
											// this is encrypted, move on for now.
											fErrorReturn += "<br>ER_028: " + lpath
													+ " was found for the keystore resourcedata but does not appear to be properly encrypted.<br>";
											fxmlErrorReturn += "<Issue  issueID=\"ER_028\" severity=\"critical\" class=\"structure\" type=\"file error\">";
											fxmlErrorReturn += lpath
													+ " was found for the keystore resourcedata but does not appear to be properly encrypted.";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_028");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												zin3.close();
												return;
											}
										}
										zin3.close();
									}

								}
							}
						}
					}
				}

				// must have [Content_Types].xml
				if (name.endsWith(".rels") && !name.endsWith("/.rels")) {
					aRelFiles.add(name);
					InputStream zin = zfile.getInputStream(entry);
					if (zin == null)
						continue;// Directory
					sContent = IOUtils.toString(zin, "UTF-8");
					if (sContent.trim().toLowerCase().startsWith("<?xml")) {
						bContent = true;
					} else {
						fErrorReturn += "<br>ER_003: " + name + " was found but did not start with <?xml.<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_003\" severity=\"critical\" class=\"structure\" type=\"file error\">";
						fxmlErrorReturn += name + " was found but did not start with <?xml.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_003");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
						continue;
					}
					// ran the validation earlier, so if it starts with <?xml,
					// then the it must have validated (of course we setup an
					// override for this)
					// next step
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder
							.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
					doc.getDocumentElement().normalize();
					NodeList nl = doc.getElementsByTagName("Relationship");
					relationshipRels.put(name, nl);
					nlist.clear();
					String keystorecheck = "";
					String encryptedfilecheck = "";
					List<String> encryptTargs = new ArrayList<String>();
					for (int ic = 0; ic < nl.getLength(); ic++) {
						Node defN = nl.item(ic);
						String rid = defN.getAttributes().getNamedItem("Id").getTextContent().trim();
						nlist.add(rid);
						String ltarg = defN.getAttributes().getNamedItem("Target").getTextContent();
						String ltype = defN.getAttributes().getNamedItem("Type").getTextContent();
						if (ltype.toLowerCase().contains("keystore"))
							keystorecheck = ltarg;
						String parent = name.substring(0, name.lastIndexOf("/_rels"));
						if (!parent.substring(0, 1).equals("/"))
							parent = "/" + parent;
						if (ltype.toLowerCase().contains("keystore"))
							keystorecheck = name + "|" + ltarg;
						boolean bnotencrypt = true;
						if (ltype.toLowerCase().contains("encryptedfile")) {
							encryptedfilecheck = name + "|" + ltarg;
							encryptTargs.add(name + "|" + ltarg);
							encryptTargsAll.add(name + "|" + ltarg);
							bnotencrypt = false;
						}
						if (targType.containsKey(name + "|" + ltarg) && !keystorecheck.contentEquals(name + "|" + ltarg)
								&& !encryptedfilecheck.contentEquals(name + "|" + ltarg)) {
							fErrorReturn += "<br>ER_112: Duplicate Relationship Target: " + ltarg + " in file: " + name;
							fxmlErrorReturn += "<Issue  issueID=\"ER_004\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += "Duplicate Relationship Target: " + ltarg + " in file: " + name;
							fxmlErrorReturn += "</Issue>";
							errChk("ER_112");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								zin.close();
								return;
							}
						}
						// do not overwrite non-encrypted types with the encrypt type
						if (bnotencrypt)
							targType.put(name + "|" + ltarg, ltype);
						targType_id.put(name + "|" + ltarg + "|" + rid, ltype);
						// add the target and the parent directory for checks on
						// paths
						targLocation.add(parent + "|" + ltarg);
						if (relTypes.get(6).equals(ltype)) {
							if (mustPreserve.indexOf(ltarg) == -1) {
								mustPreserve.add(ltarg);
							}
						}
					}
					// Check to see if any encrypted targets do not have a an assigned model type
					// and encrypted file
					for (String schk : encryptTargs) {
						String[] splitChk = schk.split("\\|");
						if (!targType.containsKey(schk)) {
							fErrorReturn += "<br>ER_112: Missing Relationship Target: Encryption specified, but a type was not defined for "
									+ splitChk[1];
							fxmlErrorReturn += "<Issue  issueID=\"ER_004\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += "Missing Relationship Target: Encryption specified, but a type was not defined for "
									+ splitChk[1];
							fxmlErrorReturn += "</Issue>";
							errChk("ER_112");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}

						// we have some practical file size limits at this time
						if (zfile.getEntry(splitChk[1].substring(1)).getSize() > 200000000) {
							continue;
						}
						InputStream zin2 = zfile.getInputStream(zfile.getEntry(splitChk[1].substring(1)));
						sContent = IOUtils.toString(zin2, "UTF-8");
						if (sContent.startsWith("%3McF")) {
							// see if we can decrypt
							String floc = splitChk[1];
							// See if we have this in keyinfo
							KeyInfo keyitem = CheckKeyInfo(floc);
							if (keyitem != null) {
								DeCryptModel dcry = new DeCryptModel();
								dcry.sAAD = keyitem.getAAD();
								dcry.bSha256 = keyitem.getSha256();
								dcry.bNoComp = keyitem.getNoComp();
								dcry.FileLoc = "ztmp" + keyitem.getFile();
								dcry.sCT = keyitem.getCT();
								dcry.sIV = keyitem.getIV();
								dcry.sTAG = keyitem.getTAG();
								try {
									dcry.DoDecrypt();
								} catch (Throwable e) {
									fWarningReturn += "<br>WR_019: decryption failure in " + floc + ".<br>";
									fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
									fxmlWarningReturn += "decryption failure in " + floc + ".";
									fxmlWarningReturn += "</Issue>";
									errChk("WR_019");
									iwrCnt++;
									if (iwrCnt > iWrnMaxStr) {
										fWarningReturn += "<br>More than " + iWrnMaxStr
												+ " warnings found, stopping..<br>";
										zin.close();
										zin2.close();
										return;
									}
								}
							}
						} else {
							fErrorReturn += "<br>ER_028: " + splitChk[1]
									+ " was found but does not appear to be properly encrypted.<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_028\" severity=\"critical\" class=\"structure\" type=\"file error\">";
							fxmlErrorReturn += splitChk[1] + " was found but does not appear to be properly encrypted.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_028");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								zin.close();
								zin2.close();
								return;
							}
						}
						zin2.close();
					}
					Set<String> tst = findDuplicates(nlist);
					boolean bdup = false;
					for (String stst : tst) {
						bdup = true;
						fErrorReturn += "<br>ER_004: Duplicate Relationship id: " + stst + " in file: " + name;
						fxmlErrorReturn += "<Issue  issueID=\"ER_004\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "Duplicate Relationship id: " + stst + " in file: " + name;
						fxmlErrorReturn += "</Issue>";
						errChk("ER_004");
					}
					if (bdup) {
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							zin.close();
							return;
						}
					}

					laRelNodes.add(nl);
					zin.close();
				}
				if (name.contentEquals("[Content_Types].xml")) {
					bentryNames.set(entryNames.indexOf("/" + name), true);
					InputStream zin = zfile.getInputStream(entry);
					if (zin == null)
						continue;// Directory
					sContent = IOUtils.toString(zin, "UTF-8");
					if (sContent.trim().toLowerCase().startsWith("<?xml")) {
						bContent = true;
					} else {
						fErrorReturn += "<br>ER_005: [Content_Types].xml was found but did not start with <?xml.<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_005\" severity=\"critical\" class=\"structure\" type=\"file  error\">";
						fxmlErrorReturn += "[Content_Types].xml was found but did not start with <?xml.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_005");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// ran the validation earlier, so if it starts with <?xml,
					// then the it must have validated
					// next step
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder
							.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
					doc.getDocumentElement().normalize();
					nType_default = doc.getElementsByTagName("Default");
					nType_override = doc.getElementsByTagName("Override");
					zin.close();
					// Find the extension for ~model
					for (int ic = 0; ic < nType_default.getLength(); ic++) {
						Node defN = nType_default.item(ic);
						String tstContent = defN.getAttributes().getNamedItem("ContentType").getTextContent().trim();
						if (!contentTypes.contains(tstContent))
							contentTypes.add(tstContent);
						if (defN.getAttributes().getNamedItem("ContentType").getTextContent()
								.contentEquals("application/vnd.ms-package.3dmanufacturing-3dmodel+xml")) {
							sModel = defN.getAttributes().getNamedItem("Extension").getTextContent().toLowerCase();
						}
						if (conTypes.indexOf(defN.getAttributes().getNamedItem("ContentType").getTextContent()) == -1) {
							fWarningReturn += "<br>WR_019: The content type: "
									+ defN.getAttributes().getNamedItem("ContentType").getTextContent()
									+ "<br>Does not appear to match the known types:<br>";
							for (String kn : conTypes) {
								fWarningReturn += kn + "<br>";
							}
							fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
							fxmlWarningReturn += "The content type: "
									+ defN.getAttributes().getNamedItem("ContentType").getTextContent()
									+ "Does not appear to match the known types:";
							String cm = "";
							for (String kn : conTypes) {
								fxmlWarningReturn += cm + kn;
								cm = ", ";
							}
							fxmlWarningReturn += "</Issue>";
							errChk("WR_019");
							iwrCnt++;
							if (iwrCnt > iWrnMaxStr) {
								fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
								return;
							}
						} else {
							// get the index of the conType to set the extension
							// in extTypes
							int ix = conTypes
									.indexOf(defN.getAttributes().getNamedItem("ContentType").getTextContent());
							String ext = defN.getAttributes().getNamedItem("Extension").getTextContent().toLowerCase();
							if (extTypes.get(ix).size() == 0) {
								extTypes.get(ix).add(ext);
							} else {
								if (extTypes.get(ix).indexOf(ext) == -1) {
									extTypes.get(ix).add(ext);
								} else {
									fErrorReturn += "<br>ER_108: The ContentType extension " + ext
											+ " has already been defined in [Content_Types].xml.<br>";
									fxmlErrorReturn += "<Issue  issueID=\"ER_108\" severity=\"moderate\" class=\"structure\" type=\"content type error\">";
									fxmlErrorReturn += "The ContentType extension " + ext
											+ " has already been defined in [Content_Types].xml.";
									fxmlErrorReturn += "</Issue>";
									errChk("ER_108");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
							}
						}
					}
					// Find the overrides for ~model
					for (int ic = 0; ic < nType_override.getLength(); ic++) {
						Node defN = nType_override.item(ic);
						String stp = defN.getAttributes().getNamedItem("ContentType").getTextContent().trim();
						if (stp == null || stp.length() == 0) {
							sOverRideType.add("n/a");
							fErrorReturn += "<br>ER_007: The ContentType was empty in an Override in [Content_Types].xml.<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_007\" severity=\"moderate\" class=\"structure\" type=\"content type error\">";
							fxmlErrorReturn += "The ContentType was empty in an Override in [Content_Types].xml.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_007");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						} else {
							if (!contentTypes.contains(stp))
								contentTypes.add(stp);
							if (stp.contentEquals("application/vnd.ms-package.3dmanufacturing-3dmodel+xml")) {
								sOverModel = defN.getAttributes().getNamedItem("PartName").getTextContent().trim();
							}
							sOverRideType.add(stp);
						}
						String pn = defN.getAttributes().getNamedItem("PartName").getTextContent().trim();

						// Checks if the PartName is a relative directory (starts with a slash)
						if (pn.charAt(0) != '/') {
							fErrorReturn += "<br>ER_132: The PartName in an Override in [Content_Types].xml is not a relative path.<br>Please add a"
									+ " forward slash '/' to the front of the PartName " + pn;
							fxmlErrorReturn += "<Issue  issueID=\"ER_132\" severity=\"moderate\" class=\"structure\" type=\"content type error\">";
							fxmlErrorReturn += "The PartName in an Override in [Content_Types].xml.<br> is not a relative path. Please add a"
									+ " forward slash '/' to the front of the PartName";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_132");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}

						// Checks if the PartName is actually present in the .zip file
						if (zfile.getEntry(pn.substring(1)) == null) {
							fWarningReturn += "<br>WR_020: The file " + pn
									+ " in [Content_Types].xml was not found in the .zip file";
							fxmlWarningReturn += "<Issue  issueID=\"WR_020\" severity=\"info\" class=\"structure\" type=\"content type error\">";
							fxmlWarningReturn += "The file " + pn
									+ "  in [Content_Types].xml was not found in the .zip file";
							fxmlWarningReturn += "</Issue>";
							errChk("WR_020");
							iwrCnt++;
							if (iwrCnt > iWrnMaxStr) {
								fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
								zin.close();
								return;
							}
						}

						Node tempNode = defN.getParentNode();
						// System.out.println(tempNode.getNodeName());
						if (pn == null || pn.length() == 0) {
							sOverRide.add("n/a");
							fErrorReturn += "<br>ER_008: The PartName was not found in an Override in [Content_Types].xml.<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_008\" severity=\"moderate\" class=\"structure\" type=\"content type error\">";
							fxmlErrorReturn += "The PartName was not found in an Override in [Content_Types].xml.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_008");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						} else {
							sOverRide.add(pn.toLowerCase());
						}
					}
				}
				// must have _rels/.rels
				if (name.contentEquals("_rels/.rels")) {
					InputStream zin = zfile.getInputStream(entry);
					if (zin == null)
						continue;// Directory
					sContent = IOUtils.toString(zin, "UTF-8");
					if (sContent.trim().toLowerCase().startsWith("<?xml")) {
						bRels = true;
					} else {
						fErrorReturn += "<br>ER_009: _rels/.rels was found but did not start with <?xml.<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_009\" severity=\"critical\" class=\"structure\" type=\"file error\">";
						fxmlErrorReturn += "_rels/.rels was found but did not start with <?xml.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_009");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// ran the validation earlier, so if it starts with <?xml,
					// then the it must have validated
					// next step
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder
							.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
					doc.getDocumentElement().normalize();
					nRelationship = doc.getElementsByTagName("Relationship");
					NodeList nl = doc.getElementsByTagName("Relationship");
					nlist.clear();
					String keystorecheck = "";
					String encryptedfilecheck = "";
					for (int ic = 0; ic < nl.getLength(); ic++) {
						Node defN = nl.item(ic);
						String rid = defN.getAttributes().getNamedItem("Id").getTextContent().trim();
						nlist.add(rid);
						String ltarg = defN.getAttributes().getNamedItem("Target").getTextContent();
						String ltype = defN.getAttributes().getNamedItem("Type").getTextContent();
						if (ltype.contentEquals(relTypes.get(iEncryptType))) {
							fErrorReturn += "<br>ER_010: encryptedfile relationship found in _rels/.rels which is not allowed.";
							fxmlErrorReturn += "<Issue  issueID=\"ER_010\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += "encryptedfile relationship found in _rels/.rels which is not allowed.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_010");
						}
						if (ltype.toLowerCase().contains("keystore")) {
							keystorecheck = ltarg;
							bKeystoreFound = true;
						}
						if (ltype.toLowerCase().contains("encryptedfile"))
							encryptedfilecheck = ltarg;
						if (targType.containsKey("/_rels/.rels|" + ltarg) && !keystorecheck.contentEquals(ltarg)
								&& !encryptedfilecheck.contentEquals(ltarg)) {
							fErrorReturn += "<br>ER_112: Duplicate Relationship Target: " + ltarg + " in file: " + name;
							fxmlErrorReturn += "<Issue  issueID=\"ER_004\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += "Duplicate Relationship Target: " + ltarg + " in file: " + name;
							fxmlErrorReturn += "</Issue>";
							errChk("ER_112");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
						targType.put("/_rels/.rels|" + ltarg, ltype);
						targType_id.put("/_rels/.rels|" + ltarg + "|" + rid, ltype);
						targLocation.add("/|" + ltarg);
						if (relTypes.get(6).equals(ltype)) {
							if (mustPreserve.indexOf(ltarg) == -1) {
								mustPreserve.add(ltarg);
							}
						}
					}
					Set<String> tst = findDuplicates(nlist);
					boolean bdup = false;
					for (String stst : tst) {
						bdup = true;
						fErrorReturn += "<br>ER_010: Duplicate Relationship id: " + stst + " in file: " + name;
						fxmlErrorReturn += "<Issue  issueID=\"ER_010\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "Duplicate Relationship id: " + stst + " in file: " + name;
						fxmlErrorReturn += "</Issue>";
						errChk("ER_010");
					}
					if (bdup) {
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					zin.close();
				}
				// Check for objects with s:meshresolution="lowres" and p:path
				if (!bchkReqExtP || !bchkReqExtS) {
					if (entry.getName().toLowerCase().endsWith(".jpg")
							|| entry.getName().toLowerCase().endsWith(".jpeg")
							|| entry.getName().toLowerCase().endsWith(".png"))
						continue;

					InputStream zin = zfile.getInputStream(entry);
					if (zin == null)
						continue;// Directory
					sContent = IOUtils.toString(zin, "UTF-8");
					if (sContent.trim().toLowerCase().startsWith("<?xml")) {
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						dbFactory.setNamespaceAware(true);// needed to use
															// getElementsByTagNameNS
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document doc = dBuilder
								.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
						doc.getDocumentElement().normalize();
						// check object for meshresolution
						NodeList nobjx = doc.getElementsByTagName("object");
						for (int ic = 0; ic < nobjx.getLength(); ic++) {
							if (nobjx.item(ic).getAttributes().getNamedItemNS(sns, "meshresolution") != null) {
								String smx = nobjx.item(ic).getAttributes().getNamedItemNS(sns, "meshresolution")
										.getTextContent().trim();
								if (smx.toLowerCase().contentEquals("lowres"))
									bchkReqExtS = true;
							}
						}
						// check item for path
						nobjx = doc.getElementsByTagName("item");
						for (int ic = 0; ic < nobjx.getLength(); ic++) {
							if (nobjx.item(ic).getAttributes().getNamedItemNS(pns, "path") != null) {
								bchkReqExtP = true;
							}
						}
						nobjx = doc.getElementsByTagName("component");
						for (int ic = 0; ic < nobjx.getLength(); ic++) {
							if (nobjx.item(ic).getAttributes().getNamedItemNS(pns, "path") != null) {
								bchkReqExtP = true;
							}
						}
					}
				}
			}
			if (!bContent) {
				fErrorReturn += "<br>ER_011: [Content_Types].xml was not found.<br>";
				fxmlErrorReturn += "<Issue  issueID=\"ER_011\" severity=\"critical\" class=\"structure\" type=\"file error\">";
				fxmlErrorReturn += "[Content_Types].xml was not found.";
				fxmlErrorReturn += "</Issue>";
				errChk("ER_011");
				ierCnt++;
				if (ierCnt > iErrMaxStr) {
					fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
					return;
				}
			}
			if (!bRels) {
				fErrorReturn += "<br>ER_012: _rels/.rels was not found.<br>";
				fxmlErrorReturn += "<Issue  issueID=\"ER_012\" severity=\"critical\" class=\"structure\" type=\"file error\">";
				fxmlErrorReturn += "_rels/.rels was not found.";
				fxmlErrorReturn += "</Issue>";
				errChk("ER_012");
				ierCnt++;
				if (ierCnt > iErrMaxStr) {
					fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
					return;
				}
			}
			if (sModel == "" && sOverModel == "") {
				fErrorReturn += "<br>ER_013: An extension for application/vnd.ms-package.3dmanufacturing-3dmodel+xml was not found.<br>";
				fxmlErrorReturn += "<Issue  issueID=\"ER_013\" severity=\"moderate\" class=\"structure\" type=\"content type error\">";
				fxmlErrorReturn += "An extension for application/vnd.ms-package.3dmanufacturing-3dmodel+xml was not found.";
				fxmlErrorReturn += "</Issue>";
				errChk("ER_013");
				ierCnt++;
				if (ierCnt > iErrMaxStr) {
					fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
					return;
				}
			}
			// validate that if keystore is present that must preserve is there as well.
			for (Map.Entry<String, String> entry : targType_id.entrySet()) {
				// relTypes 7 is keystore, 6 is must preserve
				if (entry.getValue().contentEquals(relTypes.get(7))) {
					bnonSecure = false;
					String targ = entry.getKey();
					String[] tst = targ.split("\\|");
					boolean bfnd = false;
					for (Map.Entry<String, String> entrychk : targType_id.entrySet()) {
						String targ_id = entrychk.getKey();
						String[] tst_id = targ_id.split("\\|");
						if (tst[1].contentEquals(tst_id[1]) && entrychk.getValue().contentEquals(relTypes.get(6))) {
							bfnd = true;
						}
					}
					if (!bfnd) {
						fErrorReturn += "<br>ER_012: keystore relationship specified, but a mustpreserve was not for the file: "
								+ tst[1] + "<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_012\" severity=\"critical\" class=\"structure\" type=\"file error\">";
						fxmlErrorReturn += "keystore relationship specified, but a mustpreserve was not for the file: "
								+ tst[1];
						fxmlErrorReturn += "</Issue>";
						errChk("ER_012");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}

					}
					// found a keystore relationship, is there a content type #7 ?
					// conTypes.get(7)
					if (!contentTypes.contains(conTypes.get(7))) {
						fErrorReturn += "<br>ER_012: keystore relationship specified, but a ContentType for keystore was not found in the file: "
								+ tst[1] + "<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_012\" severity=\"critical\" class=\"structure\" type=\"file error\">";
						fxmlErrorReturn += "keystore relationship specified, but a ContentType for keystore was not found in the file: "
								+ tst[1];
						fxmlErrorReturn += "</Issue>";
						errChk("ER_012");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

				} // end of keystore relationship check
			}
			// validate that all file extensions appear in [Content_Types].xml
			entries = zfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.getSize() > 200000000) {
					continue;
				}
				String name = entry.getName();
				// ignore directories
				if (name.endsWith("/"))
					continue;
				if (name.contentEquals("[Content_Types].xml"))
					continue;
				bLocate = false;
				if (nType_default != null) {
					for (int ic = 0; ic < nType_default.getLength(); ic++) {
						Node defN = nType_default.item(ic);
						if (name.toLowerCase().endsWith(
								defN.getAttributes().getNamedItem("Extension").getTextContent().toLowerCase())) {
							bLocate = true;
							break;
						}
					}
				}
				// System.out.println(name);
				for (int i = 0; i > mustPreserve.size(); i++) {
					// System.out.println(mustPreserve.get(i).toString());
				}
				if (mustPreserve.indexOf("/" + name) > -1) {
					bLocate = true;
				}
				// System.out.println(overRideFileCheck(name));
				if (!bLocate && overRideFileCheck("/" + name)) {
					bLocate = true;
				}
				if (!bLocate) {
					fWarningReturn += "<br>WR_024: Could not find an extension for " + name
							+ " in [Content_Types].xml<br>";
					fxmlWarningReturn += "<Issue  issueID=\"WR_024\" severity=\"info\" class=\"structure\" type=\"content type warning\">";
					fxmlWarningReturn += "Could not find an extension for " + name + " in [Content_Types].xml";
					fxmlWarningReturn += "</Issue>";
					errChk("WR_024");
					iwrCnt++;
					if (iwrCnt > iWrnMaxStr) {
						fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
						return;
					}
				}
			}
			// validate the Relationship(s) exist from the _rels/.rels file.
			bLocate = false;
			for (int ic = 0; ic < nRelationship.getLength(); ic++) {
				Node defN = nRelationship.item(ic);
				backSlashWarn(defN.getAttributes().getNamedItem("Target").getTextContent().trim(), "_rels/.rels");
				String fFile = defN.getAttributes().getNamedItem("Target").getTextContent().trim().replaceAll("\\\\",
						"/");
				if (entryNames.indexOf(fFile) == -1) {
					fErrorReturn += "<br>ER_015: Could not find file " + fFile + " as specified in _rels/.rels<br>";
					fxmlErrorReturn += "<Issue  issueID=\"ER_015\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
					fxmlErrorReturn += "Could not find file " + fFile + " as specified in _rels/.rels";
					fxmlErrorReturn += "</Issue>";
					errChk("ER_015");
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				} else {
					bentryNames.set(entryNames.indexOf(fFile), true);
				}
				String fType = defN.getAttributes().getNamedItem("Type").getTextContent().trim();
				if (relTypes.indexOf(fType) == -1) {
					fErrorReturn += "<br>ER_016: Type " + fType + " in _rels/.rels is not one of the known types.<br>"
							+ "<br>Does not appear to match the known types:<br>";
					for (String kn : relTypes) {
						fErrorReturn += kn + "<br>";
					}
					fxmlErrorReturn += "<Issue  issueID=\"ER_016\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
					fxmlErrorReturn += "Type " + fType + " in _rels/.rels is not one of the known types.<br>"
							+ "<br>Does not appear to match the known types: ";
					String cm = "";
					for (String kn : relTypes) {
						fxmlErrorReturn += cm + kn;
						cm = ", ";
					}
					fxmlErrorReturn += "</Issue>";
					errChk("ER_016");
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				}
				int ix = relTypes.indexOf(fType);
				if (ix != -1 && !overRideFileCheck(fFile)) {
					if (ix == 5 || ix == 4) {
						// Thumbnail, could be 5,6
						boolean bTyp = false;
						for (int i = 5; i < 7; i++) {
							for (String ext : extTypes.get(i)) {
								if (fFile.toLowerCase().endsWith(ext)) {
									bTyp = true;
								}
							}
						}
						if (!bTyp) {
							fErrorReturn += "<br>ER_017: " + fFile
									+ " in _rels/.rels does not appear to end with a known image extension.<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_017\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += fFile
									+ " in _rels/.rels does not appear to end with a known image extension.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_017");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
					} else {
						boolean bTyp = false;
						for (String ext : extTypes.get(ix)) {
							if (fFile.toLowerCase().endsWith(ext)) {
								bTyp = true;
							}
						}
						if (mustPreserve.indexOf(fFile) > -1) {
							bTyp = true;
						}
						if (!bTyp) {
							fErrorReturn += "<br>ER_018: " + fFile
									+ " in _rels/.rels does not appear to end with extension " + extTypes.get(ix)
									+ "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_018\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += fFile + " in _rels/.rels does not appear to end with extension "
									+ extTypes.get(ix);
							fxmlErrorReturn += "</Issue>";
							errChk("ER_018");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
					}
				}
				String rId = defN.getAttributes().getNamedItem("Id").getTextContent().trim();
				if (rId.contentEquals(srel0)) {
					bLocate = true;
					if (!fFile.toLowerCase().endsWith(sModel) && !overRideFileCheck(fFile)) {
						fErrorReturn += "<br>ER_019: The target " + fFile + " with Id " + srel0
								+ " in _rels/.rels does not appear to end with the expected extension as required.<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_019\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "The target " + fFile + " with Id " + srel0
								+ " in _rels/.rels does not appear to end with the expected extension as required.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_019");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					sRootModel = fFile;

				}
			}
			if (!bLocate) {
				fErrorReturn += "<br>ER_020: Well known Id " + srel0 + " not found  in _rels/.rels as required.<br>";
				fxmlErrorReturn += "<Issue  issueID=\"ER_020\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
				fxmlErrorReturn += "Well known Id " + srel0 + " not found  in _rels/.rels as required.";
				fxmlErrorReturn += "</Issue>";
				errChk("ER_020");
				ierCnt++;
				if (ierCnt > iErrMaxStr) {
					fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
					return;
				}
			}
			// validate the Relationship(s) exist for other .rels file(s).
			int idc = 0;
			for (NodeList nl : laRelNodes) {
				for (int ic = 0; ic < nl.getLength(); ic++) {
					Node defN = nl.item(ic);
					backSlashWarn(defN.getAttributes().getNamedItem("Target").getTextContent().trim(),
							aRelFiles.get(idc));
					String fFile = defN.getAttributes().getNamedItem("Target").getTextContent().trim()
							.replaceAll("\\\\", "/");
					if (entryNames.indexOf(fFile) == -1) {
						fErrorReturn += "<br>ER_021: Could not find file " + fFile + " as specified in "
								+ aRelFiles.get(idc) + "<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_021\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "Could not find file " + fFile + " as specified in " + aRelFiles.get(idc);
						fxmlErrorReturn += "</Issue>";
						errChk("ER_021");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					} else {
						bentryNames.set(entryNames.indexOf(fFile), true);
					}
					String fType = defN.getAttributes().getNamedItem("Type").getTextContent().trim();
					if (relTypes.indexOf(fType) == -1) {
						fWarningReturn += "<br>WR_022: Type " + fType + " in " + aRelFiles.get(idc)
								+ " is not one of the known types.<br>"
								+ "<br>Does not appear to match the known types:<br>";
						for (String kn : relTypes) {
							fWarningReturn += kn + "<br>";
						}
						fWarningReturn += "<Issue  issueID=\"WR_022\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
						fWarningReturn += "Type " + fType + " in " + aRelFiles.get(idc)
								+ " is not one of the known types." + "Does not appear to match the known types:";
						String cm = "";
						for (String kn : relTypes) {
							fWarningReturn += cm + kn;
							cm = ", ";
						}
						fWarningReturn += "</Issue>";
						errChk("WR_022");
						iwrCnt++;
						if (iwrCnt > iWrnMaxStr) {
							fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
							return;
						}
					}

					int ix = relTypes.indexOf(fType);
					if (ix != -1 && !overRideFileCheck(fFile)) {
						if (ix == 5 || ix == 4) {
							// Thumbnail, could be 5,6
							boolean bTyp = false;
							for (int i = 5; i < 7; i++) {
								for (String ext : extTypes.get(i)) {
									if (fFile.toLowerCase().endsWith(ext)) {
										bTyp = true;
									}
								}
							}
							if (!bTyp) {
								fErrorReturn += "<br>ER_023" + fFile + " in " + aRelFiles.get(idc)
										+ " does not appear to end with a known image extension.<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_023\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
								fxmlErrorReturn += fFile + " in " + aRelFiles.get(idc)
										+ " does not appear to end with a known image extension.";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_023");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						} else {
							boolean bTyp = false;
							for (String ext : extTypes.get(ix)) {
								if (fFile.toLowerCase().endsWith(ext)) {
									bTyp = true;
								}
							}
							// ix == 8 is an encrypted which has duplicates
							if (!bTyp && ix != 8) {
								fErrorReturn += "<br>ER_024" + fFile + " in " + aRelFiles.get(idc)
										+ " does not appear to end with extension " + extTypes.get(ix) + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_024\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
								fxmlErrorReturn += fFile + " in " + aRelFiles.get(idc)
										+ " does not appear to end with extension " + extTypes.get(ix);
								fxmlErrorReturn += "</Issue>";
								errChk("ER_024");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
							if (ix == 2) {
								fErrorReturn += "<br>ER_025" + fFile + " in " + aRelFiles.get(idc)
										+ " with core-properties must appear in the  .rels file.<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_025\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
								fxmlErrorReturn += fFile + " in " + aRelFiles.get(idc)
										+ " with core-properties must appear in the  .rels file.";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_025");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
					}
				}
				idc++;
			}

			// Validate that the rels are properly named i.e.:
			// 3D/_rels/3Dmodel.model.rels
			// 3D/3dmodel.model
			for (String sRel : aRelFiles) {
				if (!sRel.contains("/_rels/") && !sRel.startsWith("_rels/")) {
					fErrorReturn += "<br>ER_026: Could not find _rels/ in " + sRel
							+ " as specified for .rels files.<br>";
					fxmlErrorReturn += "<Issue  issueID=\"ER_026\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
					fxmlErrorReturn += "Could not find _rels/ in " + sRel + " as specified for .rels files";
					fxmlErrorReturn += "</Issue>";
					errChk("ER_026");
					return;
				}
				String tfind = sRel.substring(0, sRel.indexOf("_rels"))
						+ sRel.substring(sRel.lastIndexOf("/") + 1, sRel.lastIndexOf(".rels"));
				if (entryNames.indexOf("/" + tfind) == -1) {
					fErrorReturn += "<br>ER_027: Could not find file " + tfind
							+ " as expected from the .rels relationship to " + sRel + "<br>";
					fxmlErrorReturn += "<Issue  issueID=\"ER_027\" severity=\"moderate\" class=\"structure\" type=\"invalid relationship\">";
					fxmlErrorReturn += "Could not find file " + tfind + " as expected from the .rels relationship to "
							+ sRel;
					fxmlErrorReturn += "</Issue>";
					errChk("ER_027");
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				}

			}

			// Check the model files to see if sliceref exists and if so, check
			// the slicepath
			// If we need the namespace, the xmlns in root model (pointed to by
			// root rel) would be:
			// xmlns:namespace=...
			// 3/22/17 Now adding in material checks which will not appear with
			// slice refs.

			for (String ename : entryNames) {
				boolean bCheckPath = false;
				// is the current file in the loop a model file either by
				// .extension or an override file
				if (ename.toLowerCase().endsWith("." + sModel) || (overRideFileCheck(ename))) {

					if ((overRideFileCheck(ename))) {
						if (!sOverRideType.get(sOverRide.indexOf(ename.toLowerCase()))
								.contentEquals("application/vnd.ms-package.3dmanufacturing-3dmodel+xml")) {
							continue;
						}
					}
					// we have some practical file size limits at this time
					if (zfile.getEntry(ename.substring(1)).getSize() > 200000000) {
						continue;
					}

					// Does it start with <?xml
					InputStream zin = zfile.getInputStream(zfile.getEntry(ename.substring(1)));
					sContent = IOUtils.toString(zin, "UTF-8");
					if (sContent.startsWith("%3McF")) {
						// do the check that the file was typed as encrypted
						Boolean bfnde = false;
						for (String schk : encryptTargsAll) {
							String[] splitChk = schk.split("\\|");
							if (splitChk[1].equals(ename)) {
								bfnde = true;
								break;
							}
						}
						if (!bfnde) {
							fWarningReturn += "<br>WR_019: The file " + ename
									+ " is encrypted but an encryptedfile relationship was not specified.<br>";
							fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
							fxmlWarningReturn += "The file " + ename
									+ " is encrypted but an encryptedfile relationship was not specified.<br>";
							fxmlWarningReturn += "</Issue>";
							errChk("WR_019");
							iwrCnt++;
							if (iwrCnt > iWrnMaxStr) {
								fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
								zin.close();
								return;
							}
						}

						sContent = "";
						// see if we can decrypt
						String floc = ename;
						// See if we have this in keyinfo
						KeyInfo keyitem = CheckKeyInfo(floc);
						if (keyitem != null) {
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
								if (!dtext.toLowerCase().startsWith(("<?xml"))) {
									zin.close();
									continue;
								} else {
									sContent = dtext;
								}
							} catch (Exception e) {
								fWarningReturn += "<br>WR_019: decryption failure in " + floc + ".<br>";
								fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
								fxmlWarningReturn += "decryption failure in " + floc + ".";
								fxmlWarningReturn += "</Issue>";
								errChk("WR_019");
								iwrCnt++;
								if (iwrCnt > iWrnMaxStr) {
									fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
									zin.close();
									return;
								}
								continue;
							}
						}
					}
					if (sContent.trim().toLowerCase().startsWith("<?xml")) {
						bContent = true;
					} else {
						fErrorReturn += "<br>ER_028: " + ename + " was found but did not start with <?xml.<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_028\" severity=\"critical\" class=\"structure\" type=\"file error\">";
						fxmlErrorReturn += ename + " was found but did not start with <?xml.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_028");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							zin.close();
							return;
						}
						continue;
					}
					zin.close();
					if (ename.contentEquals(sRootModel))
						bCheckPath = true;
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					dbFactory.setNamespaceAware(true);// needed to use
														// getElementsByTagNameNS
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder
							.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
					doc.getDocumentElement().normalize();

					// gets a list of the uuids and stackids, expanded to
					// include basematerials if present
					stackId.clear();
					stackFileId.clear();
					ssIdList.clear();
					basematerialsId.clear();
					texture2dId.clear();
					texture2dgroupId.clear();
					texture2dgroupTexId.clear();
					compositematerialsId.clear();
					compositematerialsIdIndicies.clear();
					colorgroupId.clear();
					multipropertiesId.clear();

					idWithCount.clear();
					idWithName.clear();
					idUsed.clear();

					fillUuid(doc, ename);

					// if they are empty, they will kick out with no duplicates
					// check for duplicate basematerials ids.
					Set<String> tstbasem = findDuplicates(basematerialsId);
					for (String smid : tstbasem) {
						fErrorReturn += "<br>ER_029: Duplicate basematerials id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_029\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate basematerials id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_029");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					Set<String> tstcolorgroup = findDuplicates(colorgroupId);
					for (String smid : tstcolorgroup) {
						fErrorReturn += "<br>ER_030: Duplicate colorgroup id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_030\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate colorgroup id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_030");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					// check for duplicate texture2d ids.
					Set<String> tsttexture2d = findDuplicates(texture2dId);
					for (String smid : tsttexture2d) {
						fErrorReturn += "<br>ER_031: Duplicate texture2d id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_031\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate texture2d id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_031");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					// check for duplicate texture2dgroup ids.
					Set<String> tsttexture2dgroup = findDuplicates(texture2dgroupId);
					for (String smid : tsttexture2dgroup) {
						fErrorReturn += "<br>ER_032: Duplicate texture2dgroup id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_032\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate texture2dgroup id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_032");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					// check for duplicate compositematerials ids.
					Set<String> tstcompositematerials = findDuplicates(compositematerialsId);
					for (String smid : tstcompositematerials) {
						fErrorReturn += "<br>ER_033: Duplicate compositematerials id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_033\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate compositematerials id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_033");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					// check for duplicate multiproperties ids.
					Set<String> tstmultiproperties = findDuplicates(multipropertiesId);
					for (String smid : tstmultiproperties) {
						fErrorReturn += "<br>ER_034: Duplicate multiproperties id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_034\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate multiproperties id: " + smid + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_034");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					// check for unique slicestackid in part
					// check for the duplicate sliceStack ids
					Set<String> tst = null;

					// stackId(s) were found in the fillUuid above.
					tst = findDuplicates(stackId);
					for (String sUid : tst) {
						int ic = 0;
						for (String oUid : stackId) {
							if (oUid.contentEquals(sUid)) {
								fErrorReturn += "<br>ER_035: Duplicate slicestack id: " + oUid + " in file: "
										+ stackFileId.get(ic)
										+ " Must be unique across the resources in the model part.";
								fxmlErrorReturn += "<Issue  issueID=\"ER_035\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "Duplicate slicestack id: " + oUid + " in file: "
										+ stackFileId.get(ic)
										+ " Must be unique across the resources in the model part.";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_035");
							}
							ic++;
						}
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					// check for duplicate ids across all
					List<String> combList = new ArrayList<String>();
					combList.addAll(stackId);
					combList.addAll(basematerialsId);
					combList.addAll(texture2dId);
					combList.addAll(texture2dgroupId);
					combList.addAll(compositematerialsId);
					combList.addAll(colorgroupId);
					combList.addAll(multipropertiesId);
					tst = findDuplicates(combList);
					for (String s : tst) {
						String sfnd = "";
						String comma = "";
						if (stackId.indexOf(s) > -1) {
							sfnd += comma + " slicestack";
							comma = ",";
						}
						if (basematerialsId.indexOf(s) > -1) {
							sfnd += comma + " basematerials";
							comma = ",";
						}
						if (texture2dId.indexOf(s) > -1) {
							sfnd += comma + " texture2d";
							comma = ",";
						}
						if (texture2dgroupId.indexOf(s) > -1) {
							sfnd += comma + " texture2dgroup";
							comma = ",";
						}
						if (compositematerialsId.indexOf(s) > -1) {
							sfnd += comma + " compositematerials";
							comma = ",";
						}
						if (colorgroupId.indexOf(s) > -1) {
							sfnd += comma + " colorgroup";
							comma = ",";
						}
						if (multipropertiesId.indexOf(s) > -1) {
							sfnd += comma + " multiproperties";
							comma = ",";
						}

						fErrorReturn += "<br>ER_036: Duplicate id: " + s + " in " + sfnd + ". Located in file: " + ename
								+ ". Id must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_036\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate id: " + s + " in " + sfnd + ". Located in file: " + ename
								+ ". Id must be unique across the resources in the model part";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_036");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					if (ename.contentEquals(sRootModel)) {
						bdoProduction = false;
						Set<String> prefs = findPrefix(doc);
						NodeList n1 = doc.getElementsByTagName("model");
						Node n2 = n1.item(0);
						if (n2.getAttributes().getNamedItem("thumbnail") != null) {
							String thumbPath = n2.getAttributes().getNamedItem("thumbnail").getTextContent().trim()
									.replaceAll("\\\\", "/");
							;
							if (entryNames.indexOf(thumbPath) == -1) {
								fErrorReturn += "<br>ER_052: Could not find file " + thumbPath
										+ " as specified in the thumbnail attribute path in " + ename + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_052\" severity=\"low\" class=\"structure\" type=\"file error\">";
								fxmlErrorReturn += "Could not find file " + thumbPath
										+ " as specified in the thumbnail attribute path in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_052");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
						if (n2.getAttributes().getNamedItem("requiredextensions") != null) {
							String mpr = n2.getAttributes().getNamedItem("requiredextensions").getTextContent().trim();
							String[] mprs = mpr.split("\\s+");
							for (int mm = 0; mm < mprs.length; mm++) {
								requiredExtensions.add(mprs[mm]);
							}
							for (String p : prefs) {
								for (int j = 0; j < mprs.length; j++) {
									if (p.contentEquals(mprs[j]))
										bdoProduction = true;
								}
							}
						}

						if (n2.getAttributes().getNamedItem("recommendedextensions") != null) {
							String recex = n2.getAttributes().getNamedItem("recommendedextensions").getTextContent()
									.trim();
							String[] recextension = recex.split("\\s+");
							for (int mm = 0; mm < recextension.length; mm++) {
								String current = recextension[mm].toString().trim();
								for (int nn = 0; nn < requiredExtensions.size(); nn++) {
									String check = requiredExtensions.get(nn).toString().trim();
									if (current.equalsIgnoreCase(check) && !current.equalsIgnoreCase("")) {
										fErrorReturn += "<br>ER_129: extension " + current
												+ " found in both required and recommended extension list";
										fxmlErrorReturn += "<Issue  issueID=\"ER_129\" severity=\"low\" class=\"structure\" type=\"file error\">";
										fxmlErrorReturn += "extension " + current
												+ " found in both required and recommended extension list";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_129");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
							}
						}

						if (bchkReqExtS || bchkReqExtP) {
							if (n2.getAttributes().getNamedItem("requiredextensions") == null) {
								fWarningReturn += "<br>WR_012: requiredextensions attribute not found in " + ename
										+ ".<br>";
								fxmlWarningReturn += "<Issue  issueID=\"WR_012\" severity=\"info\" class=\"structure\" type=\"Required Extensions\">";
								fxmlWarningReturn += "requiredextensions attribute not found in " + ename + ".";
								fxmlWarningReturn += "</Issue>";
								errChk("WR_012");
								iwrCnt++;
								if (iwrCnt > iWrnMaxStr) {
									fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
									return;
								}
							} else {
								String mpr = n2.getAttributes().getNamedItem("requiredextensions").getTextContent()
										.trim();
								String[] mprs = mpr.split("\\s+");
								boolean bout = false;
								for (String p : prefs) {
									if ((bchkReqExtS && p.contentEquals(snsc))
											|| (bchkReqExtP && p.contentEquals(pnsc))) {
										boolean bfnd = false;
										for (int j = 0; j < mprs.length; j++) {
											if (p.contentEquals(mprs[j]))
												bfnd = true;
										}
										if (!bfnd) {
											bout = true;
											fWarningReturn += "<br>WR_013: Could not find prefix " + p
													+ " in model attribute requiredextensions in " + ename + "<br>";
											fxmlWarningReturn += "<Issue  issueID=\"WR_013\" severity=\"info\" class=\"structure\" type=\"Required Extensions\">";
											fxmlWarningReturn += "Could not find prefix " + p
													+ " in model attribute requiredextensions in " + ename;
											fxmlWarningReturn += "</Issue>";
											errChk("WR_013");
											iwrCnt++;
											if (iwrCnt > iWrnMaxStr) {
												fWarningReturn += "<br>More than " + iWrnMaxStr
														+ " warnings found, stopping..<br>";
												return;
											}
										}
									}
								}
								if (bout) {
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
							}
						}
						n1 = doc.getElementsByTagName("build");
						for (int ic = 0; ic < n1.getLength(); ic++) {
							n2 = n1.item(ic);
							if (n2.getAttributes().getNamedItemNS(pns, "UUID") == null) {
								if (!MainFrame.getNoUUIDchk() && bdoProduction) {
									fErrorReturn += "<br>ER_039: UUID attribute for build not found in " + ename
											+ ".<br>";
									fxmlErrorReturn += "<Issue  issueID=\"ER_039\" severity=\"low\" class=\"structure\" type=\"UUID Error\">";
									fxmlErrorReturn += "UUID attribute for build not found in " + ename + ".";
									fxmlErrorReturn += "</Issue>";
									errChk("ER_039");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								} else if (!bwarnOnce && bdoProduction) {
									fWarningReturn += "<br>Structural validation ignoring UUID errors<br>";
									bwarnOnce = true;
								}
							} else {
								String buuid = n2.getAttributes().getNamedItemNS(pns, "UUID").getTextContent().trim();
								if (buuid.length() < 1) {
									if (!MainFrame.getNoUUIDchk()) {
										fErrorReturn += "<br>ER_040: UUID attribute for build found empty in " + ename
												+ ".<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_040\" severity=\"low\" class=\"structure\" type=\"UUID Error\">";
										fxmlErrorReturn += "UUID attribute for build found empty in " + ename + ".";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_040");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else if (!bwarnOnce) {
										fWarningReturn += "<br>Structural validation ignoring UUID errors<br>";
										bwarnOnce = true;
									}
								}
							}
						}
						// end of root model check
					} else {
						// This is not the root model, so make sure path does
						// not appear
						NodeList nodeList = doc.getElementsByTagName("*");
						String nStat = "";
						for (int i = 0; i < nodeList.getLength(); i++) {
							Node node = nodeList.item(i);
							// checking for thumbnails in non-root model objects
							if (node.getNodeName().equals("object")) {
								if (node.getAttributes().getNamedItem("id") != null) {
								}
								if (node.getAttributes().getNamedItem("thumbnail") != null) {
									backSlashWarn(
											node.getAttributes().getNamedItem("thumbnail").getTextContent().trim(),
											ename);
									nStat = node.getAttributes().getNamedItem("thumbnail").getTextContent().trim()
											.replaceAll("\\\\", "/");
									if (nStat.equals(""))
										continue;
									// String bid =
									// node.getAttributes().getNamedItem("id").getTextContent().trim();
									// fWarningReturn += "<br>Thumbnail
									// reference in non-root model part. Most
									// Consumers will ignore. Found thumbnail
									// reference: " + nStat
									// + " in object with id: " + bid + ", in
									// file: " + ename + "." ;
									// Now that we know there is a thumbnail in
									// an object, does it have a .rels file
									// aRelFiles does not have the leading /
									String frels = ename.substring(1, ename.lastIndexOf("/") + 1) + "_rels/"
											+ ename.substring(ename.lastIndexOf("/") + 1) + ".rels";
									if (aRelFiles.indexOf(frels) == -1) {
										fErrorReturn += "<br>ER_041: Thumbnail reference in model part should have corresponding .rels file, none found for file: "
												+ ename + ".";
										fxmlErrorReturn += "<Issue  issueID=\"ER_041\" severity=\"low\" class=\"structure\" type=\"invalid relationship\">";
										fxmlErrorReturn += "Thumbnail reference in model part should have corresponding .rels file, none found for file: "
												+ ename + ".";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_041");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else {
										// does the reference exist in the rels
										// file.
										NodeList nl = relationshipRels.get(frels);
										Boolean fndr = false;
										for (int ic = 0; ic < nl.getLength(); ic++) {
											Node defN = nl.item(ic);
											if (defN.getAttributes().getNamedItem("Target") != null) {
												if (defN.getAttributes().getNamedItem("Target").getTextContent().trim()
														.equals(nStat)) {
													fndr = true;
													break;
												}
											}
										}
										if (!fndr) {
											fErrorReturn += "<br>ER_042: Thumbnail reference in model part should have corresponding Target in .rels file, none found for file: "
													+ frels + ".";
											fxmlErrorReturn += "<Issue  issueID=\"ER_042\" severity=\"low\" class=\"structure\" type=\"invalid relationship\">";
											fxmlErrorReturn += "Thumbnail reference in model part should have corresponding Target in .rels file, none found for file: "
													+ frels + ".";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_042");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
								}
							}
							if (node.getNodeType() == Node.ELEMENT_NODE
									&& !node.getLocalName().contains("texture2d")
									&& !node.getLocalName().contains("displacement2d")) {
								if (node.getAttributes().getNamedItem("path") != null) {
									backSlashWarn(node.getAttributes().getNamedItem("path").getTextContent().trim(),
											ename);
									nStat = node.getAttributes().getNamedItem("path").getTextContent().trim()
											.replaceAll("\\\\", "/");
									if (nStat != null) {
										fErrorReturn += "<br>ER_043: Path " + nStat + " found in " + ename
												+ " which is not the root model.<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_043\" severity=\"high\" class=\"structure\" type=\"path error\">";
										fxmlErrorReturn += "Path " + nStat + " found in " + ename
												+ " which is not the root model.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_043");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
								if (node.getAttributes().getNamedItemNS(sns, "path") != null) {
									backSlashWarn(
											node.getAttributes().getNamedItemNS(sns, "path").getTextContent().trim(),
											ename);
									nStat = node.getAttributes().getNamedItemNS(sns, "path").getTextContent().trim()
											.replaceAll("\\\\", "/");
									if (nStat != null) {
										fErrorReturn += "<br>ER_044: Path " + nStat + " found in " + ename
												+ " which is not the root model.<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_044\" severity=\"high\" class=\"structure\" type=\"path error\">";
										fxmlErrorReturn += "Path " + nStat + " found in " + ename
												+ " which is not the root model.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_044");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
								if (node.getAttributes().getNamedItemNS(pns, "path") != null) {
									backSlashWarn(
											node.getAttributes().getNamedItemNS(pns, "path").getTextContent().trim(),
											ename);
									nStat = node.getAttributes().getNamedItemNS(pns, "path").getTextContent().trim()
											.replaceAll("\\\\", "/");
									if (nStat != null) {
										fErrorReturn += "<br>ER_045: Path " + nStat + " found in " + ename
												+ " which is not the root model.<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_045\" severity=\"high\" class=\"structure\" type=\"path error\">";
										fxmlErrorReturn += "Path " + nStat + " found in " + ename
												+ " which is not the root model.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_045");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
							}
						}
						// end of model check that is not root model
					}
					NodeList nl = doc.getElementsByTagNameNS(sns, "sliceref");
					for (int ic = 0; ic < nl.getLength(); ic++) {
						Node defN = nl.item(ic);
						backSlashWarn(defN.getAttributes().getNamedItem("slicepath").getTextContent().trim(), ename);
						String fFile = defN.getAttributes().getNamedItem("slicepath").getTextContent().trim()
								.replaceAll("\\\\", "/");
						if (entryNames.indexOf(fFile) == -1) {
							fErrorReturn += "<br>ER_046: Could not find file " + fFile + " as specified in " + ename
									+ "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_046\" severity=\"high\" class=\"structure\" type=\"file error\">";
							fxmlErrorReturn += "Could not find file " + fFile + " as specified in " + ename;
							fxmlErrorReturn += "</Issue>";
							errChk("ER_046");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
						if (ename.contentEquals(fFile)) {
							fErrorReturn += "<br>ER_047: The slicepath " + fFile + " as specified in " + ename
									+ " is referencing itself.<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_047\" severity=\"high\" class=\"structure\" type=\"path error\">";
							fxmlErrorReturn += "The slicepath " + fFile + " as specified in " + ename
									+ " is referencing itself.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_047");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}

						}
						String slcId = defN.getAttributes().getNamedItem("slicestackid").getTextContent().trim();
						if (zfile.getEntry(fFile.substring(1)).getSize() > 200000000) {
							continue;
						}

						if (zfile.getEntry(fFile.substring(1)) != null) {
							zin = zfile.getInputStream(zfile.getEntry(fFile.substring(1)));
							sContent = IOUtils.toString(zin, "UTF-8");
							if (sContent.startsWith("%3McF")) {
								// see if we can decrypt
								String floc = fFile;
								// See if we have this in keyinfo
								KeyInfo keyitem = CheckKeyInfo(floc);
								if (keyitem != null) {
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
										if (!dtext.toLowerCase().startsWith(("<?xml"))) {

										} else {
											sContent = dtext;
										}
									} catch (Exception e) {

										fWarningReturn += "<br>WR_019: decryption failure in " + floc + ".<br>";
										fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
										fxmlWarningReturn += "decryption failure in " + floc + ".";
										fxmlWarningReturn += "</Issue>";
										errChk("WR_019");
										iwrCnt++;
										if (iwrCnt > iWrnMaxStr) {
											fWarningReturn += "<br>More than " + iWrnMaxStr
													+ " warnings found, stopping..<br>";
											zin.close();
											return;
										}
									}
								}

							}
							if (!sContent.trim().toLowerCase().startsWith("<?xml")) {
								fErrorReturn += "<br>ER_048: " + fFile + " was found but did not start with <?xml.<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_048\" severity=\"critical\" class=\"structure\" type=\"file error\">";
								fxmlErrorReturn += fFile + " was found but did not start with <?xml.";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_048");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
							zin.close();
						} else {
							fErrorReturn += "<br>ER_049: " + fFile + " was not found in " + fCurrentFile + "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_049\" severity=\"high\" class=\"structure\" type=\"file error\">";
							fxmlErrorReturn += fFile + " was not found in " + fCurrentFile;
							fxmlErrorReturn += "</Issue>";
							errChk("ER_049");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}

						}
						ssIdListRef.add(fFile + "|" + slcId);
						b2dSlices = true;
						DocumentBuilderFactory dbFactoryx = DocumentBuilderFactory.newInstance();
						dbFactoryx.setNamespaceAware(true);// needed to use
															// getElementsByTagNameNS
						DocumentBuilder dBuilderx = dbFactoryx.newDocumentBuilder();
						Document docx = dBuilderx
								.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
						docx.getDocumentElement().normalize();
						NodeList nlx = docx.getElementsByTagNameNS(sns, "slicestack");
						boolean fnd = false;
						for (int icx = 0; icx < nlx.getLength(); icx++) {
							Node defNx = nlx.item(icx);
							if (slcId.contentEquals(defNx.getAttributes().getNamedItem("id").getTextContent())) {
								fnd = true;
							}
						}
						if (!fnd) {
							fErrorReturn += "<br>ER_050: The slicestack id \"" + slcId + "\" was not found in " + fFile
									+ " as referenced in " + ename + ".<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_050\" severity=\"high\" class=\"structure\" type=\"ID error\">";
							fxmlErrorReturn += "The slicestack id \"" + slcId + "\" was not found in " + fFile
									+ " as referenced in " + ename + ".";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_050");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
					}
					if (bCheckPath) {
						NodeList nodeList = doc.getElementsByTagName("*");
						String nStat = "";
						for (int i = 0; i < nodeList.getLength(); i++) {
							Node node = nodeList.item(i);
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								if (node.getAttributes().getNamedItem("path") != null) {
									backSlashWarn(node.getAttributes().getNamedItem("path").getTextContent().trim(),
											sRootModel);
									nStat = node.getAttributes().getNamedItem("path").getTextContent().trim()
											.replaceAll("\\\\", "/");
									if (nStat != null) {
										if (entryNames.indexOf(nStat) == -1) {
											fErrorReturn += "<br>ER_051: Could not find file " + nStat
													+ " as specified in the path in " + sRootModel + "<br>";
											fxmlErrorReturn += "<Issue  issueID=\"ER_051\" severity=\"high\" class=\"structure\" type=\"file error\">";
											fxmlErrorReturn += "Could not find file " + nStat
													+ " as specified in the path in " + sRootModel;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_051");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										String schk = sRootModel.substring(0, sRootModel.lastIndexOf("/"));
										if (!targLocation.contains(schk + "|" + nStat)) {
											fErrorReturn += "<br>ER_113: Root model reference for " + nStat
													+ " not found in associated rels file as specified in the path in "
													+ sRootModel + "<br>";
											fxmlErrorReturn += "<Issue  issueID=\"ER_113\" severity=\"high\" class=\"structure\" type=\"path error\">";
											fxmlErrorReturn += "Root model reference for " + nStat
													+ " not found in associated rels file as specified in the path in "
													+ sRootModel;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_113");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
								}
							}
						}

						bCheckPath = false;
					}
					nl = doc.getElementsByTagName("item");
					NodeList nObj = doc.getElementsByTagName("object");
					if (nObj != null) {
						for (int ic = 0; ic < nObj.getLength(); ic++) {
							// System.out.println(nObj.item(ic).getNodeName());
							if (nObj.item(ic) == null)
								continue;
							String bid = nObj.item(ic).getAttributes().getNamedItem("id").getTextContent().trim();
							objIdList.add(ename + "|" + bid);
							// objectNodeList.put(bid, nObj.item(ic));
							String fFile = "";
							Boolean bchkRid = chkResourcesId(bid, "object", ename);
							if (!bchkRid) {
								idWithCount.put(bid, 0);
								idUsed.put(bid, false);
								idWithName.put(bid, "object");
							}
							if (nObj.item(ic).getAttributes().getNamedItem("thumbnail") != null) {
								backSlashWarn(
										nObj.item(ic).getAttributes().getNamedItem("thumbnail").getTextContent().trim(),
										ename);
								fFile = nObj.item(ic).getAttributes().getNamedItem("thumbnail").getTextContent().trim()
										.replaceAll("\\\\", "/");
							}
							if (fFile != "") {
								if (entryNames.indexOf(fFile) == -1) {
									fErrorReturn += "<br>ER_052: Could not find file " + fFile
											+ " as specified in the thumbnail attribute path in " + ename + "<br>";
									fxmlErrorReturn += "<Issue  issueID=\"ER_052\" severity=\"low\" class=\"structure\" type=\"file error\">";
									fxmlErrorReturn += "Could not find file " + fFile
											+ " as specified in the thumbnail attribute path in " + ename;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_052");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
							}
							// objects with components should not have a pid or
							// pindex
							if (nObj.item(ic).getAttributes().getNamedItem("pid") != null) {
								NodeList childList = nObj.item(ic).getChildNodes();
								for (int j = 0; j < childList.getLength(); j++) {
									Node cnode = childList.item(j);
									if (cnode.getNodeName().equals("components")) {
										String pidl = nObj.item(ic).getAttributes().getNamedItem("pid").getTextContent()
												.trim();
										fErrorReturn += "<br>ER_109: Pid may not be specified in objects that contain components. Found pid="
												+ pidl + " in object with id=" + bid + "" + ename + "<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_109\" severity=\"high\" class=\"structure\" type=\"ID Error\">";
										fxmlErrorReturn += "Pid may not be specified in objects that contain components. Found pid="
												+ pidl + " in object with id=" + bid + "" + ename;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_109");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
							}
							if (nObj.item(ic).getAttributes().getNamedItem("pindex") != null) {
								NodeList childList = nObj.item(ic).getChildNodes();
								for (int j = 0; j < childList.getLength(); j++) {
									Node cnode = childList.item(j);
									if (cnode.getNodeName().equals("components")) {
										String pidl = nObj.item(ic).getAttributes().getNamedItem("pindex")
												.getTextContent().trim();
										fErrorReturn += "<br>ER_110: Pindex may not be specified in objects that contain components. Found pindex="
												+ pidl + " in object with id=" + bid + "" + ename + "<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_110\" severity=\"high\" class=\"structure\" type=\"ID Error\">";
										fxmlErrorReturn += "Pindex may not be specified in objects that contain components. Found pindex="
												+ pidl + " in object with id=" + bid + "" + ename;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_110");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
							}

						}
					}
					for (int ic = 0; ic < nl.getLength(); ic++) {
						// get a summary of object items to check for a hang
						Node defN = nl.item(ic);
						String fFile = "";

						// Collect planar-transform violations for the slice-only check below.
						if (defN.getAttributes().getNamedItem("transform") != null) {
							String stran = defN.getAttributes().getNamedItem("transform").getTextContent().trim()
									.replaceAll(" +", " ");
							String[] tstNum = stran.split("\\s+");
							// # 2, 5, 6, 7 must be a zero
							// # 8 must be a 1.0
							try {
								if (Float.parseFloat(tstNum[2]) != 0.f) {
									transformErrorLoc
											.add("ER_071: Invalid " + tstNum[2] + " for m02 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[5]) != 0.f) {
									transformErrorLoc
											.add("ER_072: Invalid " + tstNum[5] + " for m12 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[6]) != 0.f) {
									transformErrorLoc
											.add("ER_073: Invalid " + tstNum[6] + " for m20 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[7]) != 0.f) {
									transformErrorLoc
											.add("ER_074: Invalid " + tstNum[7] + " for m21 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[8]) != 1.f) {
									transformErrorLoc
											.add("ER_075: Invalid " + tstNum[8] + " for m22 found (should be 1.0) in "
													+ stran + " for transform in " + ename);
								}
							} catch (Exception et1) {
								transformErrorLoc.add("ER_076: Could not parse number contained in " + stran
										+ " transform in " + ename);
							}

						}

						if (defN.getAttributes().getNamedItem("thumbnail") != null) {
							backSlashWarn(defN.getAttributes().getNamedItem("thumbnail").getTextContent().trim(),
									ename);
							fFile = defN.getAttributes().getNamedItem("thumbnail").getTextContent().trim()
									.replaceAll("\\\\", "/");
						}
						if (fFile != "") {
							if (entryNames.indexOf(fFile) == -1) {
								fErrorReturn += "<br>ER_053: Could not find file " + fFile
										+ " as specified in the thumbnail attribute path in " + ename + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_053\" severity=\"low\" class=\"structure\" type=\"file error\">";
								fxmlErrorReturn += "Could not find file " + fFile
										+ " as specified in the thumbnail attribute path in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_053");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
							String schk = ename.substring(0, ename.lastIndexOf("/"));
							if (!targLocation.contains(schk + "|" + fFile)) {
								fErrorReturn += "<br>ER_113: Thumbnail Reference for " + fFile
										+ " not found in associated rels file as specified in the path in " + ename
										+ "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_113\" severity=\"high\" class=\"structure\" type=\"path error\">";
								fxmlErrorReturn += "Thumbnail Reference for " + fFile
										+ " not found in associated rels file as specified in the path in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_113");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
						fFile = "";
						if (defN.getAttributes().getNamedItemNS(pns, "path") != null) {
							backSlashWarn(defN.getAttributes().getNamedItemNS(pns, "path").getTextContent().trim(),
									ename);
							fFile = defN.getAttributes().getNamedItemNS(pns, "path").getTextContent().trim()
									.replaceAll("\\\\", "/");
						}
						String objectid = defN.getAttributes().getNamedItem("objectid").getTextContent().trim();
						if (fFile != "") {
							if (entryNames.indexOf(fFile) == -1) {
								fErrorReturn += "<br>ER_054: Could not find file " + fFile
										+ " as specified in the item attribute path in " + ename + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_054\" severity=\"high\" class=\"structure\" type=\"file error\">";
								fxmlErrorReturn += "Could not find file " + fFile
										+ " as specified in the item attribute path in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_054");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							} else {
								String schk = ename.substring(0, ename.lastIndexOf("/"));
								if (!targLocation.contains(schk + "|" + fFile)) {
									fErrorReturn += "<br>ER_113: PNS File Reference Check for " + fFile
											+ " not found in associated rels file as specified in the path in " + ename
											+ "<br>";
									fxmlErrorReturn += "<Issue  issueID=\"ER_113\" severity=\"high\" class=\"structure\" type=\"path error\">";
									fxmlErrorReturn += "PNS File Reference Check for " + fFile
											+ " not found in associated rels file as specified in the path in " + ename;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_113");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
								// Check object id in path
								// Don't do this if the the file is an override
								// and not XML.
								boolean bchk = true;
								if (overRideFileCheck(fFile)) {
									if (!sOverRideType.get(sOverRide.indexOf(fFile.toLowerCase()))
											.contentEquals("application/vnd.ms-package.3dmanufacturing-3dmodel+xml")) {
										bchk = false;
									}
								}

								if (bchk && zfile.getEntry(fFile.substring(1)).getSize() < 200000000) {
									InputStream ziny = zfile.getInputStream(zfile.getEntry(fFile.substring(1)));
									sContent = IOUtils.toString(ziny, "UTF-8");
									if (sContent.startsWith("%3McF")) {
										sContent = "";
										// see if we can decrypt
										String floc = fFile;
										// See if we have this in keyinfo
										KeyInfo keyitem = CheckKeyInfo(floc);
										if (keyitem != null) {
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
												if (!dtext.toLowerCase().startsWith(("<?xml"))) {

												} else {
													sContent = dtext;
												}
											} catch (Exception e) {

												fWarningReturn += "<br>WR_019: decryption failure in " + floc + ".<br>";
												fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
												fxmlWarningReturn += "decryption failure in " + floc + ".";
												fxmlWarningReturn += "</Issue>";
												errChk("WR_019");
												iwrCnt++;
												if (iwrCnt > iWrnMaxStr) {
													fWarningReturn += "<br>More than " + iWrnMaxStr
															+ " warnings found, stopping..<br>";
													ziny.close();
													return;
												}
											}
										}

									}
									if (!sContent.trim().toLowerCase().startsWith("<?xml")) {
										fErrorReturn += "<br>ER_055: " + fFile
												+ " was found but did not start with <?xml.<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_055\" severity=\"critical\" class=\"structure\" type=\"file error\">";
										fxmlErrorReturn += fFile + " was found but did not start with <?xml.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_055");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									ziny.close();
									if (!fFile.toLowerCase().endsWith(sModel) && !overRideFileCheck(fFile)) {
										fErrorReturn += "<br>ER_056: " + fFile
												+ " was found but does not have a model extension.";
										fxmlErrorReturn += "<Issue  issueID=\"ER_056\" severity=\"moderate\" class=\"structure\" type=\"content type error\">";
										fxmlErrorReturn += fFile + " was found but does not have a model extension.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_056");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									Document docy = dBuilder.parse(
											new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
									docy.getDocumentElement().normalize();
									NodeList nObjy = docy.getElementsByTagName("object");
									boolean fndObj = false;
									for (int ico = 0; ico < nObjy.getLength(); ico++) {
										if (objectid.contentEquals(
												nObjy.item(ico).getAttributes().getNamedItem("id").getTextContent())) {
											fndObj = true;
											objIdListRef.add(fFile + "|" + objectid);
										}
									}
									if (!fndObj) {
										fErrorReturn += "<br>ER_057: Did not find an object id for item with objectid="
												+ objectid + " in " + ename + "<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_057\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "Did not find an object id for item with objectid="
												+ objectid + " in " + ename;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_057");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}

									}
								}
							}
						} else if (objectid != null) {
							boolean fndObj = false;
							for (int ico = 0; ico < nObj.getLength(); ico++) {
								if (objectid.contentEquals(
										nObj.item(ico).getAttributes().getNamedItem("id").getTextContent())) {
									fndObj = true;
									objIdListRef.add(ename + "|" + objectid);
								}
							}
							if (!fndObj) {
								fErrorReturn += "<br>ER_057: Did not find an object id for item with objectid="
										+ objectid + " in " + ename + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_057\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "Did not find an object id for item with objectid=" + objectid
										+ " in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_057");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
					}

					nl = doc.getElementsByTagName("component");
					nObj = doc.getElementsByTagName("object");
					for (int ic = 0; ic < nl.getLength(); ic++) {
						Node defN = nl.item(ic);

						// Collect planar-transform violations for the slice-only check below.
						if (defN.getAttributes().getNamedItem("transform") != null) {
							String stran = defN.getAttributes().getNamedItem("transform").getTextContent().trim()
									.replaceAll(" +", " ");
							String[] tstNum = stran.split("\\s+");
							// # 2, 5, 6, 7 must be a zero
							// # 8 must be a 1.0
							try {
								if (Float.parseFloat(tstNum[2]) != 0.f) {
									transformErrorLoc
											.add("ER_071: Invalid " + tstNum[2] + " for m02 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[5]) != 0.f) {
									transformErrorLoc
											.add("ER_072: Invalid " + tstNum[5] + " for m12 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[6]) != 0.f) {
									transformErrorLoc
											.add("ER_073: Invalid " + tstNum[6] + " for m20 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[7]) != 0.f) {
									transformErrorLoc
											.add("ER_074: Invalid " + tstNum[7] + " for m21 found (should be 0.0) in "
													+ stran + " for transform in " + ename);
								}
								if (Float.parseFloat(tstNum[8]) != 1.f) {
									transformErrorLoc
											.add("ER_075: Invalid " + tstNum[8] + " for m22 found (should be 1.0) in "
													+ stran + " for transform in " + ename);
								}
							} catch (Exception et1) {
								transformErrorLoc.add("ER_076: Could not parse number contained in " + stran
										+ " transform in " + ename);
							}

						}

						String fFile = "";
						if (defN.getAttributes().getNamedItemNS(pns, "path") != null) {
							backSlashWarn(defN.getAttributes().getNamedItemNS(pns, "path").getTextContent().trim(),
									ename);
							fFile = defN.getAttributes().getNamedItemNS(pns, "path").getTextContent().trim()
									.replaceAll("\\\\", "/");
						}
						String objectid = defN.getAttributes().getNamedItem("objectid").getTextContent().trim();
						if (fFile != "") {
							if (entryNames.indexOf(fFile) == -1) {
								fErrorReturn += "<br>ER_058: Could not find file " + fFile
										+ " as specified in the component attribute path in " + ename + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_058\" severity=\"high\" class=\"structure\" type=\"path error\">";
								fxmlErrorReturn += "Could not find file " + fFile
										+ " as specified in the component attribute path in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_058");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							} else {
								String schk = ename.substring(0, ename.lastIndexOf("/"));
								if (!targLocation.contains(schk + "|" + fFile)) {
									fErrorReturn += "<br>ER_113: Component Reference for " + fFile
											+ " not found in associated rels file as specified in the path in " + ename
											+ "<br>";
									fxmlErrorReturn += "<Issue  issueID=\"ER_113\" severity=\"high\" class=\"structure\" type=\"path error\">";
									fxmlErrorReturn += "Component Reference for " + fFile
											+ " not found in associated rels file as specified in the path in " + ename;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_113");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
								// Check object id in path
								// Don't do this if the the file is an override
								// and not XML.
								boolean bchk = true;
								if (overRideFileCheck(fFile)) {
									if (!sOverRideType.get(sOverRide.indexOf(fFile.toLowerCase()))
											.contentEquals("application/vnd.ms-package.3dmanufacturing-3dmodel+xml")) {
										bchk = false;
									}
								}
								if (bchk && zfile.getEntry(fFile.substring(1)).getSize() < 200000000) {
									InputStream ziny = zfile.getInputStream(zfile.getEntry(fFile.substring(1)));
									sContent = IOUtils.toString(ziny, "UTF-8");
									if (sContent.startsWith("%3McF")) {
										sContent = "";
										// see if we can decrypt
										String floc = fFile;
										// See if we have this in keyinfo
										KeyInfo keyitem = CheckKeyInfo(floc);
										if (keyitem != null) {
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
												if (!dtext.toLowerCase().startsWith(("<?xml"))) {

												} else {
													sContent = dtext;
												}
											} catch (Exception e) {

												fWarningReturn += "<br>WR_019: decryption failure in " + floc + ".<br>";
												fxmlWarningReturn += "<Issue  issueID=\"WR_019\" severity=\"info\" class=\"structure\" type=\"content type error\">";
												fxmlWarningReturn += "decryption failure in " + floc + ".";
												fxmlWarningReturn += "</Issue>";
												errChk("WR_019");
												iwrCnt++;
												if (iwrCnt > iWrnMaxStr) {
													fWarningReturn += "<br>More than " + iWrnMaxStr
															+ " warnings found, stopping..<br>";
													ziny.close();
													return;
												}
											}
										}

									}
									if (!sContent.trim().toLowerCase().startsWith("<?xml")) {
										fErrorReturn += "<br>ER_059: " + fFile
												+ " was found but did not start with <?xml.<br>";
										fxmlErrorReturn += "<Issue  issueID=\"ER_059\" severity=\"critical\" class=\"structure\" type=\"file error\">";
										fxmlErrorReturn += fFile + " was found but did not start with <?xml.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_059");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									ziny.close();
									if (!fFile.toLowerCase().endsWith(sModel) && !overRideFileCheck(fFile)) {
										fErrorReturn += "<br>ER_060: " + fFile
												+ " was found but does not have a model extension.";
										fxmlErrorReturn += "<Issue  issueID=\"ER_060\" severity=\"moderate\" class=\"structure\" type=\"content type error\">";
										fxmlErrorReturn += fFile + " was found but does not have a model extension.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_060");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									if (sContent.trim().toLowerCase().startsWith("<?xml")) {
										Document docy = dBuilder.parse(
												new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
										docy.getDocumentElement().normalize();
										NodeList nObjy = docy.getElementsByTagName("object");
										boolean fndObj = false;
										for (int ico = 0; ico < nObjy.getLength(); ico++) {
											if (objectid.contentEquals(nObjy.item(ico).getAttributes()
													.getNamedItem("id").getTextContent())) {
												fndObj = true;
												objIdListRef.add(fFile + "|" + objectid);
											}
										}
										if (!fndObj) {
											fErrorReturn += "<br>ER_061: Did not find an object id for component with objectid="
													+ objectid + " in " + ename + "<br>";
											fxmlErrorReturn += "<Issue  issueID=\"ER_061\" severity=\"high\" class=\"structure\" type=\"ID error\">";
											fxmlErrorReturn += "Did not find an object id for component with objectid="
													+ objectid + " in " + ename;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_061");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
								}
							}
						} else if (objectid != null) {
							// local object referenced in a component.
							boolean fndObj = false;
							for (int ico = 0; ico < nObj.getLength(); ico++) {
								if (objectid.contentEquals(
										nObj.item(ico).getAttributes().getNamedItem("id").getTextContent())) {
									fndObj = true;
									objIdListRef.add(ename + "|" + objectid);
									// does the object precede the component in
									// the node list?
									NodeList nodeList = doc.getElementsByTagName("*");
									String nStat = "";
									for (int i = 0; i < nodeList.getLength(); i++) {
										Node node = nodeList.item(i);
										if (node.getNodeType() == Node.ELEMENT_NODE) {
											if (node.getNodeName().contentEquals("object")) {
												nStat = node.getAttributes().getNamedItem("id").getTextContent().trim();
												if (nStat.contentEquals(objectid)) {
													break;
												}
											}
											if (node.getNodeName().contentEquals("component")) {
												nStat = node.getAttributes().getNamedItem("objectid").getTextContent()
														.trim();
												if (nStat.contentEquals(objectid)) {
													//
													fErrorReturn += "<br>ER_062: Component with objectid=" + objectid
															+ " in " + ename
															+ " appears to precede the defining object.<br>";
													fxmlErrorReturn += "<Issue  issueID=\"ER_062\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
													fxmlErrorReturn += "Component with objectid=" + objectid + " in "
															+ ename + " appears to precede the defining object.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_062");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
													break;
												}
											}
										}
									}
								}
							}
							if (!fndObj) {
								fErrorReturn += "<br>ER_063: Did not find an object id for component with objectid="
										+ objectid + " in " + ename + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_063\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "Did not find an object id for component with objectid=" + objectid
										+ " in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_063");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
					}
					nl = doc.getElementsByTagNameNS(sns, "slicestack");
					nObj = doc.getElementsByTagName("object");
					boolean fndObj = false;
					String sId = "";
					for (int ico = 0; ico < nObj.getLength(); ico++) {
						Node defOb = nObj.item(ico);
						if (defOb.getAttributes().getNamedItemNS(sns, "slicestackid") != null) {
							sId = defOb.getAttributes().getNamedItemNS(sns, "slicestackid").getTextContent().trim();
							ssIdListRef.add(ename + "|" + sId);
							for (int ic = 0; ic < nl.getLength(); ic++) {
								Node defN = nl.item(ic);
								if (sId.contentEquals(defN.getAttributes().getNamedItem("id").getTextContent()))
									fndObj = true;
							}
							if (!fndObj) {
								fErrorReturn += "<br>ER_064: Did not find an slicestack id for object with slicestackid="
										+ sId + " in " + ename + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_064\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "Did not find an slicestack id for object with slicestackid=" + sId
										+ " in " + ename;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_064");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
					}
					nl = doc.getElementsByTagNameNS(sns, "slicestack");
					nlist.clear();
					namelist.clear();
					for (int ic = 0; ic < nl.getLength(); ic++) {
						Node defN = nl.item(ic);
						nlist.add(defN.getAttributes().getNamedItem("id").getTextContent());
						namelist.add(" in slicestack, file: " + ename);
					}
					nl = doc.getElementsByTagName("object");
					for (int ic = 0; ic < nl.getLength(); ic++) {
						Node defN = nl.item(ic);
						nlist.add(defN.getAttributes().getNamedItem("id").getTextContent());
						namelist.add(" in object, file: " + ename);
					}
					tst = findDuplicates(nlist);
					boolean bdup = false;
					for (String stst : tst) {
						bdup = true;
						int ic = 0;
						for (String oUid : nlist) {
							if (oUid.contentEquals(stst)) {
								fErrorReturn += "<br>ER_065: Duplicate id " + oUid + namelist.get(ic)
										+ " Must be unique across the resources in the model part.";
								fxmlErrorReturn += "<Issue  issueID=\"ER_065\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "Duplicate id " + oUid + namelist.get(ic)
										+ " Must be unique across the resources in the model part.";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_065");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
							ic++;
						}
					}
					if (bdup) {
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>more than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					nl = doc.getElementsByTagName("object");
					nlist.clear();
					for (int ic = 0; ic < nl.getLength(); ic++) {
						Node defN = nl.item(ic);
						nlist.add(defN.getAttributes().getNamedItem("id").getTextContent());
					}
					tst.clear();
					tst = findDuplicates(nlist);
					for (String stst : tst) {
						bdup = true;
						fErrorReturn += "<br>ER_066: Duplicate object id: " + stst + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_066\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Duplicate object id: " + stst + " in file: " + ename
								+ " Must be unique across the resources in the model part.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_066");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					if (bdup) {
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
				} // end of models looping
			}
			for (int i = 0; i < bentryNames.size(); i++) {
				if (!bentryNames.get(i)) {
					String addCom = "";
					// we have some practical file size limits at this time
					if (zfile.getEntry(entryNames.get(i).substring(1)).getSize() > 200000000) {
						// continue;
					} else {
						InputStream zin2 = zfile.getInputStream(zfile.getEntry(entryNames.get(i).substring(1)));
						sContent = IOUtils.toString(zin2, "UTF-8");
						if (sContent.startsWith("%3McF")) {
							// this is encrypted, move on for now.
							addCom = " and the files is encrypted.";
						}
						zin2.close();
					}

					logger.info("Entry " + entryNames.get(i));
					fWarningReturn += "<br>WR_009: Did not find a target for " + entryNames.get(i) + addCom + "<br>";
					fxmlWarningReturn += "<Issue  issueID=\"WR_009\" severity=\"info\" class=\"structure\" type=\"orphaned item\">";
					fxmlWarningReturn += "Did not find a target for " + entryNames.get(i) + addCom;
					fxmlWarningReturn += "</Issue>";
					errChk("WR_009");
					iwrCnt++;
					if (iwrCnt > iWrnMaxStr) {
						fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
						return;
					}
				}
			}
			// check for hanging objects
			for (int i = 0; i < objIdList.size(); i++) {
				if (objIdListRef.indexOf(objIdList.get(i)) == -1) {
					String[] tst = objIdList.get(i).trim().split("\\|");
					fWarningReturn += "<br>WR_010: Could not find a use/reference for object id " + tst[1]
							+ " located in file " + tst[0] + "<br>";
					fxmlWarningReturn += "<Issue  issueID=\"WR_010\" severity=\"info\" class=\"structure\" type=\"orphaned item\">";
					fxmlWarningReturn += "Could not find a use/reference for object id " + tst[1] + " located in file "
							+ tst[0];
					fxmlWarningReturn += "</Issue>";
					errChk("WR_010");
					iwrCnt++;
					if (iwrCnt > iWrnMaxStr) {
						fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
						return;
					}
				}
			}
			// check for hanging slicestack id
			logger.debug(ssIdList);
			logger.debug(ssIdListRef);
			for (int i = 0; i < ssIdList.size(); i++) {
				if (ssIdListRef.indexOf(ssIdList.get(i)) == -1) {
					String[] tst = ssIdList.get(i).trim().split("\\|");
					fWarningReturn += "<br>WR_011: Could not find a use/reference for slicestack id " + tst[1]
							+ " located in file " + tst[0] + "<br>";
					fxmlWarningReturn += "<Issue  issueID=\"WR_011\" severity=\"info\" class=\"structure\" type=\"orphaned item\">";
					fxmlWarningReturn += "Could not find a use/reference for slicestack id " + tst[1]
							+ " located in file " + tst[0];
					fxmlWarningReturn += "</Issue>";
					errChk("WR_011");
					iwrCnt++;
					if (iwrCnt > iWrnMaxStr) {
						fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
						return;
					}
				}
			}
			// check for the duplicate UUIDs
			Set<String> tst = findDuplicates(idUuid);
			for (String sUid : tst) {
				int ic = 0;
				boolean bfnd = false;
				for (String oUid : idUuid) {
					if (oUid.contentEquals(sUid)) {
						if (!MainFrame.getNoUUIDchk()) {
							fErrorReturn += "<br>ER_070: Duplicate UUID: " + oUid + " in file: " + fileUuid.get(ic)
									+ " Must be unique across package.";
							fxmlErrorReturn += "<Issue  issueID=\"ER_070\" severity=\"low\" class=\"structure\" type=\"UUID error\">";
							fxmlErrorReturn += "Duplicate UUID: " + oUid + " in file: " + fileUuid.get(ic)
									+ " Must be unique across package.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_070");
							bfnd = true;
						} else if (!bwarnOnce) {
							fWarningReturn += "<br>Structural validation ignoring UUID errors<br>";
							bwarnOnce = true;
						}
					}
					ic++;
				}
				if (bfnd) {
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				}
			}
			// Check for 2d slices with bad transforms
			if (b2dSlices && transformErrorLoc.size() > 0) {
				for (String trs : transformErrorLoc) {
					// Slice-only restrictions must affect XML and error status only
					// when slice data is present, just like the HTML diagnostics.
					String code = trs.substring(0, 6);
					String message = trs.substring(8).replace("&", "&amp;")
							.replace("<", "&lt;").replace(">", "&gt;");
					fxmlErrorReturn += "<Issue issueID=\"" + code
							+ "\" severity=\"moderate\" class=\"structure\" type=\"invalid transform\">"
							+ message + "</Issue>";
					errChk(code);
					fErrorReturn += "<br>" + trs;
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				}
			}

		} catch (ArrayIndexOutOfBoundsException | NullPointerException | IOException | SAXException
				| ParserConfigurationException ex) {
			fErrorReturn += "<br>ER_077: Caught an exception: " + ex.getMessage() + "<br>";
			fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
			fxmlErrorReturn += "Caught an exception: " + ex.getMessage();
			fxmlErrorReturn += "</Issue>";
			errChk("ER_077");
			ex.printStackTrace();
			ierCnt++;
			if (ierCnt > iErrMaxStr) {
				fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
				return;
			}
		}
		return;
	}

	// get lists of specified elements from models
	private void fillUuid(Document doc, String file) {
		// Create tables for the scalar calculations
		String[] verticesColumnNames = { "objectID", "index", "x", "y", "z" };
		DefaultTableModel verticesTable = new DefaultTableModel(0, verticesColumnNames.length);
		// verticesTable.setColumnIdentifiers(verticesColumnNames);

		String[] triangleColumnNames = { "objectID", "did", "index", "d1", "d2", "d3", "v1", "v2", "v3" };
		DefaultTableModel triangleTable = new DefaultTableModel(0, triangleColumnNames.length);
		// triangleTable.setColumnIdentifiers(triangleColumnNames);

		String[] disp2dGroupColumnNames = { "groupID", "index", "nid", "n" };
		DefaultTableModel disp2dGroupTable = new DefaultTableModel(0, disp2dGroupColumnNames.length);
		// disp2dGroupTable.setColumnIdentifiers(disp2dGroupColumnNames);

		String[] normVectorGroupColumnNames = { "normVectorID", "index", "x", "y", "z" };
		DefaultTableModel normVectorGroupTable = new DefaultTableModel(0, normVectorGroupColumnNames.length);
		// normVectorGroupTable.setColumnIdentifiers(normVectorGroupColumnNames);

		NodeList nodeList = doc.getElementsByTagName("*");
		String nUuid = "";
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getAttributes().getNamedItemNS(pns, "UUID") != null) {
					// System.out.println(node.getNodeName());
					// System.out.println(node.getAttributes().getNamedItemNS(pns,
					// "UUID").getTextContent());
					nUuid = node.getAttributes().getNamedItemNS(pns, "UUID").getTextContent().trim();
					idUuid.add(nUuid);
					fileUuid.add(file);
				}
				if (node.getNodeName().contains("slicestack")) {
					nUuid = node.getAttributes().getNamedItem("id").getTextContent().trim();
					stackId.add(nUuid);
					stackFileId.add(file);
					ssIdList.add(file + "|" + nUuid);
					Boolean bchkRid = chkResourcesId(nUuid, "slicestack", file);
					if (!bchkRid) {
						idWithCount.put(nUuid, 0);
						idUsed.put(nUuid, false);
						idWithName.put(nUuid, "slicestack");
					}
				}
				if (node.getNodeName().equals("basematerials")) {
					String bid = node.getAttributes().getNamedItem("id").getTextContent().trim();
					basematerialsId.add(bid);
					List<String> names = new ArrayList<String>();
					int icnt = 0;
					NodeList childList = node.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node cnode = childList.item(j);
						if (cnode.getNodeName().equals("base")) {
							names.add(cnode.getAttributes().getNamedItem("name").getTextContent());
							icnt++;
						}
					}
					Boolean bchkRid = chkResourcesId(bid, "basematerials", file);
					if (!bchkRid) {
						idWithCount.put(bid, icnt);
						idUsed.put(bid, false);
						idWithName.put(bid, "basematerials");
					}
					if (icnt > 0) {
						Set<String> tstname = findDuplicates(names);
						for (String smid : tstname) {
							fWarningReturn += "<br>WR_001: Duplicate base name: " + smid + " in basematerials id: "
									+ bid + ", in file: " + file + ". Should be unique.";
							fxmlWarningReturn += "<Issue  issueID=\"WR_001\" severity=\"info\" class=\"structure\" type=\"uniqueness warning\">";
							fxmlWarningReturn += "Duplicate base name: " + smid + " in basematerials id: " + bid
									+ ", in file: " + file + ". Should be unique.";
							fxmlWarningReturn += "</Issue>";
							errChk("WR_001");
							iwrCnt++;
							if (iwrCnt > iWrnMaxStr) {
								fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
								return;
							}
						}

					}
				}
				// Colorgroup
				if (node.getNodeName().endsWith("colorgroup")) {
					String bid = node.getAttributes().getNamedItem("id").getTextContent().trim();
					colorgroupId.add(bid);
					int icnt = 0;
					NodeList childList = node.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node cnode = childList.item(j);
						if (cnode.getNodeName().endsWith("color")) {
							icnt++;
						}
					}
					Boolean bchkRid = chkResourcesId(bid, "colorgroup", file);
					if (!bchkRid) {
						idWithCount.put(bid, icnt);
						idUsed.put(bid, false);
						idWithName.put(bid, "colorgroup");
					}
				}
				// now texture2d
				if (node.getNodeName().endsWith("texture2d")) {
					String bid = node.getAttributes().getNamedItem("id").getTextContent().trim();
					texture2dId.add(bid);
					Boolean bchkRid = chkResourcesId(bid, "texture2d", file);
					if (!bchkRid) {
						idWithCount.put(bid, 0);
						idUsed.put(bid, false);
						idWithName.put(bid, "texture2d");
					}
					// check that the file specified by the path has a texture
					// target.
					String schk = file.substring(0, file.lastIndexOf("/"));
					String lpath = node.getAttributes().getNamedItem("path").getTextContent().trim();
					String ltarg = targType.get(file + "|" + lpath);
					if (ltarg != null && relTypes.indexOf(ltarg) != iTexType) {
						fErrorReturn += "<br>ER_111: texture2d path: " + lpath + ", with id: " + bid + ", in file: "
								+ file + ", has an target of " + ltarg
								+ " which is an invalid relationship for textures.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_111\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "texture2d path: " + lpath + ", with id: " + bid + ", in file: " + file
								+ ", has an target of " + ltarg + " which is an invalid relationship for textures.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_111");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}

					}
					if (!targLocation.contains(schk + "|" + lpath)) {
						fErrorReturn += "<br>ER_113: Texture2d Reference for " + lpath
								+ " not found in associated rels file as specified in the path in " + file + "<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_113\" severity=\"high\" class=\"structure\" type=\"path error\">";
						fxmlErrorReturn += "Texture2d Reference for " + lpath
								+ " not found in associated rels file as specified in the path in " + file;
						fxmlErrorReturn += "</Issue>";
						errChk("ER_113");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					if (node.getAttributes().getNamedItem("box") != null) {
						String nStat = node.getAttributes().getNamedItem("box").getTextContent().trim();
						String[] splt = nStat.split(" ");
						// check all box attributes at once as we only need one
						// warning.
						if (Double.parseDouble(splt[0]) != 0.0 || Double.parseDouble(splt[1]) != 0.0
								|| Double.parseDouble(splt[2]) != 1.0 || Double.parseDouble(splt[3]) != 1.0) {

							fWarningReturn += "<br>WR_018: texture2d box attribute in " + file
									+ " present with non-default values, may be ignored by consumers.<br>";
							fxmlWarningReturn += "<Issue  issueID=\"WR_018\" severity=\"info\" class=\"structure\" type=\"bounds limit\">";
							fxmlWarningReturn += "texture2d box attribute in " + file
									+ " present with non-default values, may be ignored by consumers.";
							fxmlWarningReturn += "</Issue>";
							errChk("WR_008");
							iwrCnt++;
							if (iwrCnt > iWrnMaxStr) {
								fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
								return;
							}

						}

					}

				}
				// now texture2dgroup
				if (node.getNodeName().endsWith("texture2dgroup")) {
					String bid = node.getAttributes().getNamedItem("id").getTextContent().trim();
					texture2dgroupId.add(bid);
					Boolean bchkRid = chkResourcesId(bid, "texture2dgroup", file);
					if (!bchkRid) {
						idWithName.put(bid, "texture2dgroup");
					}
					String tbid = node.getAttributes().getNamedItem("texid").getTextContent().trim();
					texture2dgroupTexId.add(tbid);
					// at this point in looping through the nodes, the texture2d
					// id should have already been saved.
					if (texture2dId.indexOf(tbid) == -1) {
						fErrorReturn += "<br>ER_078: texture2dgroup texid: " + tbid + ", in texture2dgroup id: " + bid
								+ ", in file: " + file + " appears before the definition of the texture2d.";

						fxmlErrorReturn += "<Issue  issueID=\"ER_078\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
						fxmlErrorReturn += "texture2dgroup texid: " + tbid + ", in texture2dgroup id: " + bid
								+ ", in file: " + file + " appears before the definition of the texture2d.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_078");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					} else {
						idUsed.put(tbid, true);

					}
					// tex2coord
					int icnt = 0;
					NodeList childList = node.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node cnode = childList.item(j);
						if (cnode.getNodeName().endsWith("tex2coord")) {
							icnt++;
						}
					}
					idWithCount.put(bid, icnt);
				}
				// now compositematerials
				if (node.getNodeName().endsWith("compositematerials")) {
					String bid = node.getAttributes().getNamedItem("id").getTextContent().trim();
					compositematerialsId.add(bid);
					String tbid = node.getAttributes().getNamedItem("matid").getTextContent().trim();
					// at this point in looping through the nodes, the
					// basematerials id should have already been saved.
					if (basematerialsId.indexOf(tbid) == -1) {
						Boolean bchkRid = chkResourcesId(bid, "compositematerials", file);
						if (!bchkRid) {
							idWithCount.put(bid, 0);
							idUsed.put(bid, false);
							idWithName.put(bid, "compositematerials");
						}
						fErrorReturn += "<br>ER_079: compositematerials matid: " + tbid + ", in compositematerials id: "
								+ bid + ", in file: " + file + " appears before the definition of the basematerials.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_079\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
						fxmlErrorReturn += "compositematerials matid: " + tbid + ", in compositematerials id: " + bid
								+ ", in file: " + file + " appears before the definition of the basematerials.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_079");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					} else {
						idUsed.put(tbid, true);
						String mtbid = node.getAttributes().getNamedItem("matindices").getTextContent().trim();
						compositematerialsIdIndicies.add(mtbid);
						String[] sind = mtbid.split("\\s+");
						Boolean bchkRid = chkResourcesId(bid, "compositematerials", file);
						if (!bchkRid) {
							idWithName.put(bid, "compositematerials");
						}
						int idxCnt = idWithCount.get(tbid);
						for (String sis : sind) {
							Integer is = Integer.parseInt(sis);
							if (is >= idxCnt) {
								fErrorReturn += "<br>ER_080: compositematerials matid: " + tbid
										+ ", in compositematerials id: " + bid + ", in file: " + file
										+ " with matindices: " + mtbid + ", has an index of " + sis
										+ " which exceeds the base count in the basematerials.";
								fxmlErrorReturn += "<Issue  issueID=\"ER_080\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
								fxmlErrorReturn += "compositematerials matid: " + tbid + ", in compositematerials id: "
										+ bid + ", in file: " + file + " with matindices: " + mtbid
										+ ", has an index of " + sis
										+ " which exceeds the base count in the basematerials.";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_080");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
						// check the composite values now
						NodeList childList = node.getChildNodes();
						int icnt = 1;
						for (int j = 0; j < childList.getLength(); j++) {
							Node cnode = childList.item(j);
							if (cnode.getNodeName().endsWith("composite")) {
								String sval = cnode.getAttributes().getNamedItem("values").getTextContent().trim();
								Float ftot = 0.f;
								String[] svals = sval.split("\\s+");
								for (String sv : svals) {
									ftot += Float.parseFloat(sv);
								}
								if (Math.round(ftot * 1000000.0) / 1000000.0 != 1.0f) {
									fWarningReturn += "<br>WR_004: #" + icnt
											+ " composite, under compositematerials id: " + bid + ", in file: " + file
											+ ", with values " + sval + " and a summation of "
											+ Math.round(ftot * 1000000.0) / 1000000.0 + " does not sum to 1.0";
									fxmlWarningReturn += "<Issue  issueID=\"WR_004\" severity=\"info\" class=\"structure\" type=\"range warning\">";
									fxmlWarningReturn += "#" + icnt + " composite, under compositematerials id: " + bid
											+ ", in file: " + file + ", with values " + sval + " and a summation of "
											+ Math.round(ftot * 1000000.0) / 1000000.0 + " does not sum to 1.0";
									fxmlWarningReturn += "</Issue>";
									errChk("WR_004");
									iwrCnt++;
									if (iwrCnt > iWrnMaxStr) {
										fWarningReturn += "<br>More than " + iWrnMaxStr
												+ " warnings found, stopping..<br>";
										return;
									}
								}
								if (svals.length != sind.length) {
									fWarningReturn += "<br>WR_005: #" + icnt
											+ " composite, under compositematerials id: " + bid + ", in file: " + file
											+ " with matindices " + mtbid + " has " + svals.length
											+ " indicies which does not match the count in matindicies.";
									fxmlWarningReturn += "<Issue  issueID=\"WR_005\" severity=\"info\" class=\"structure\" type=\"index warning\">";
									fxmlWarningReturn += "#" + icnt + " composite, under compositematerials id: " + bid
											+ ", in file: " + file + " with matindices " + mtbid + " has "
											+ svals.length + " indicies which does not match the count in matindicies.";
									fxmlWarningReturn += "</Issue>";
									errChk("WR_005");
									iwrCnt++;
									if (iwrCnt > iWrnMaxStr) {
										fWarningReturn += "<br>More than " + iWrnMaxStr
												+ " warnings found, stopping..<br>";
										return;
									}
								}
								icnt++;
							}
							idWithCount.put(bid, icnt - 1);
						}

					}
				}
				// now multiproperties
				if (node.getNodeName().endsWith("multiproperties")) {
					String bid = node.getAttributes().getNamedItem("id").getTextContent().trim();
					multipropertiesId.add(bid);
					Boolean bchkRid = chkResourcesId(bid, "multiproperties", file);
					if (!bchkRid) {
						idWithName.put(bid, "multiproperties");
					}
					idUsed.put(bid, false);
					String pids = node.getAttributes().getNamedItem("pids").getTextContent().trim();
					String[] spids = pids.split("\\s+");
					int ibaseComp = 0;
					int icolorGroup = 0;
					int multiCount = 0;
					for (String sp : spids) {
						String name = idWithName.get(sp);
						if (name == null) {
							fErrorReturn += "<br>ER_081: multiproperties with id: " + bid + ", pids reference: " + sp
									+ ", in file: " + file + " does not yet exist.";
							fxmlErrorReturn += "<Issue  issueID=\"ER_081\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
							fxmlErrorReturn += "multiproperties with id: " + bid + ", pids reference: " + sp
									+ ", in file: " + file + " does not yet exist.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_081");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						} else {
							if (name.equals("basematerials") || name.equals("compositematerials")) {
								ibaseComp++;
								// System.out.println(ibaseComp);
								if (multiCount >= 1) {
									fErrorReturn += "Basematerials or Compositematerials is above first layer in multiproperties";
								}
							}
							if (name.equals("colorgroup"))
								icolorGroup++;
							if (name.equals("multiproperties")) {
								fErrorReturn += "<br>ER_082: multiproperties with id: " + bid + ", pids reference: "
										+ sp + ", in file: " + file
										+ " has referenced another multiproperties id, which is not allowed.";
								fxmlErrorReturn += "<Issue  issueID=\"ER_082\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "multiproperties with id: " + bid + ", pids reference: " + sp
										+ ", in file: " + file
										+ " has referenced another multiproperties id, which is not allowed.";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_082");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
						multiCount++;
					}
					if (ibaseComp > 1) {
						fErrorReturn += "<br>ER_083: multiproperties with id: " + bid + ", pids: " + pids
								+ ", in file: " + file
								+ " has more than one reference to a basematerials and/or compositematerials, which is not allowed.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_083\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "multiproperties with id: " + bid + ", pids: " + pids + ", in file: " + file
								+ " has more than one reference to a basematerials and/or compositematerials, which is not allowed.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_083");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					if (icolorGroup > 1) {
						fErrorReturn += "<br>ER_084: multiproperties with id: " + bid + ", pids: " + pids
								+ ", in file: " + file
								+ " has more than one reference to a colorgroup, which is not allowed.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_084\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "multiproperties with id: " + bid + ", pids: " + pids + ", in file: " + file
								+ " has more than one reference to a colorgroup, which is not allowed.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_084");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// check the multi pindices now
					NodeList childList = node.getChildNodes();
					int icnt = 1;
					boolean bdoOnce = true;
					for (int j = 0; j < childList.getLength(); j++) {
						Node cnode = childList.item(j);
						if (cnode.getNodeName().endsWith("multi")) {
							String sind = cnode.getAttributes().getNamedItem("pindices").getTextContent().trim();
							String[] spind = sind.split("\\s+");
							if (spind.length != spids.length && bdoOnce) {
								bdoOnce = false;
								fWarningReturn += "<br>WR_006: #" + icnt + " multi, under multiproperties id: " + bid
										+ ", in file: " + file + ", with " + spind.length
										+ " indices which does not match the count in pids of " + pids + ".";
								fxmlWarningReturn += "<Issue  issueID=\"WR_006\" severity=\"info\" class=\"structure\" type=\"index warning\">";
								fxmlWarningReturn += "#" + icnt + " multi, under multiproperties id: " + bid
										+ ", in file: " + file + ", with " + spind.length
										+ " indices which does not match the count in pids of " + pids + ".";
								fxmlWarningReturn += "</Issue>";
								errChk("WR_006");
								iwrCnt++;
								if (iwrCnt > iWrnMaxStr) {
									fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
									return;
								}
							}
							// check that the value for the index is less than
							// the count for the id.
							int ic = 0;
							for (String tst : spind) {
								if (ic < spids.length) {
									Integer ichk = idWithCount.get(spids[ic]);
									if (ichk == null) {
										fErrorReturn += "<br>ER_085: #" + icnt
												+ " multi, under multiproperties with id: " + bid + ", pids id: "
												+ spids[ic] + ", with index: " + tst + ", in file: " + file
												+ " does not exist yet.";
										fxmlErrorReturn += "<Issue  issueID=\"ER_085\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
										fxmlErrorReturn += "#" + icnt + " multi, under multiproperties with id: " + bid
												+ ", pids id: " + spids[ic] + ", with index: " + tst + ", in file: "
												+ file + " does not exist yet.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_085");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else {
										idUsed.put(spids[ic], true);
										if (Integer.parseInt(tst) >= ichk) {
											fErrorReturn += "<br>ER_086: #" + icnt
													+ " multi, under multiproperties with id: " + bid + ", pids id: "
													+ spids[ic] + ", with index: " + tst + ", in file: " + file
													+ " exceeds the number of items in " + idWithName.get(spids[ic])
													+ ".";
											fxmlErrorReturn += "<Issue  issueID=\"ER_086\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "#" + icnt + " multi, under multiproperties with id: "
													+ bid + ", pids id: " + spids[ic] + ", with index: " + tst
													+ ", in file: " + file + " exceeds the number of items in "
													+ idWithName.get(spids[ic]) + ".";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_086");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
									ic++;
								}
							}
							icnt++;
						}
					}
					idWithCount.put(bid, icnt - 1);
				}

				// check for pid/pindex in object
				if (node.getNodeName().equals("object")) {
					if (node.getAttributes().getNamedItem("pindex") != null
							&& node.getAttributes().getNamedItem("pid") == null) {
						fErrorReturn += "<br>ER_087: object with id: "
								+ node.getAttributes().getNamedItem("id").getTextContent() + " and "
								+ node.getAttributes().getNamedItem("pindex") + " but missing a defined pid"
								+ ", in file: " + file;
						fxmlErrorReturn += "<Issue  issueID=\"ER_087\" severity=\"hig\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "object with id: " + node.getAttributes().getNamedItem("id").getTextContent()
								+ " and " + node.getAttributes().getNamedItem("pindex") + " but missing a defined pid"
								+ ", in file: " + file;
						fxmlErrorReturn += "</Issue>";
						errChk("ER_087");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}

					}
					if (node.getAttributes().getNamedItem("pid") != null) {
						String pid = node.getAttributes().getNamedItem("pid").getTextContent().trim();
						String name = idWithName.get(pid);
						if (name == null) {
							fErrorReturn += "<br>ER_088: object with id: "
									+ node.getAttributes().getNamedItem("id").getTextContent() + ", pid: " + pid
									+ ", in file: " + file + " does not yet exist.";
							fxmlErrorReturn += "<Issue  issueID=\"ER_088\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
							fxmlErrorReturn += "object with id: "
									+ node.getAttributes().getNamedItem("id").getTextContent() + ", pid: " + pid
									+ ", in file: " + file + " does not yet exist.";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_088");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						} else {
							idUsed.put(pid, true);
							if (node.getAttributes().getNamedItem("pindex") != null) {
								Integer ichk = idWithCount.get(pid);
								String tst2 = node.getAttributes().getNamedItem("pindex").getTextContent().trim();
								if (Integer.parseInt(tst2) >= ichk) {
									fErrorReturn += "<br>ER_089: object with id: "
											+ node.getAttributes().getNamedItem("id").getTextContent() + ", pid: " + pid
											+ ", in file: " + file + " has a pindex of " + tst2
											+ " which exceeds the index count of " + ichk + " for " + name + ".";
									fxmlErrorReturn += "<Issue  issueID=\"ER_089\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "object with id: "
											+ node.getAttributes().getNamedItem("id").getTextContent() + ", pid: " + pid
											+ ", in file: " + file + " has a pindex of " + tst2
											+ " which exceeds the index count of " + ichk + " for " + name + ".";
									fxmlErrorReturn += "</Issue>";
									errChk("ER_089");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
							}
						}
					}
				}

				// now mesh/vertices/triangles
				// and check for beamlattice 4/16/2020
				if (node.getNodeName().equals("mesh")) {

					String pidChk = "";
					String pindexChk = "";
					List<Double> vertx = new ArrayList<Double>();
					List<Double> verty = new ArrayList<Double>();
					List<Double> vertz = new ArrayList<Double>();

					List<Integer> v1 = new ArrayList<Integer>();
					List<Integer> v2 = new ArrayList<Integer>();
					List<Integer> v3 = new ArrayList<Integer>();

					List<Integer> p1 = new ArrayList<Integer>();
					List<Integer> p2 = new ArrayList<Integer>();
					List<Integer> p3 = new ArrayList<Integer>();
					List<Integer> tripid = new ArrayList<Integer>();

					Node objNode = node.getParentNode();
					Integer currid = Integer.parseInt(objNode.getAttributes().getNamedItem("id").getTextContent());
					if (objNode.getAttributes().getNamedItem("type") != null) {
						String currType = objNode.getAttributes().getNamedItem("type").getTextContent();
						meshModId.put(currid, currType);
					} else {
						meshModId.put(currid, "model");
					}
					if (objNode.getAttributes().getNamedItem("pid") != null) {
						pidChk = objNode.getAttributes().getNamedItem("pid").getTextContent().trim();
					}
					if (objNode.getAttributes().getNamedItem("pindex") != null) {
						pindexChk = objNode.getAttributes().getNamedItem("pindex").getTextContent().trim();
					}
					String objId = "";
					if (objNode.getAttributes().getNamedItem("id") != null)
						objId = objNode.getAttributes().getNamedItem("id").getTextContent().trim();
					// get a count of vertices, then validate the triangles
					// references
					int ivert = 0;
					int itrg = 0;
					NodeList childList = node.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node cnode = childList.item(j);
						if (cnode.getNodeName().equals("vertices")) {
							NodeList vList = cnode.getChildNodes();
							for (int jj = 0; jj < vList.getLength(); jj++) {
								Node vnode = vList.item(jj);
								if (vnode.getNodeName().equals("vertex")) {
									vertx.add(Double
											.parseDouble(vnode.getAttributes().getNamedItem("x").getTextContent()));
									verty.add(Double
											.parseDouble(vnode.getAttributes().getNamedItem("y").getTextContent()));
									vertz.add(Double
											.parseDouble(vnode.getAttributes().getNamedItem("z").getTextContent()));
									ivert++;
								}
							}
						}

						try {
							if (cnode.getNodeName().equals("triangles")) {
								NodeList vList = cnode.getChildNodes();
								for (int jj = 0; jj < vList.getLength(); jj++) {
									List<String> vertList = new ArrayList<String>();
									Node vnode = vList.item(jj);
									// <triangle p1="0" pid="4" v1="0" v2="2"
									// v3="1"/>
									if (vnode.getNodeName().equals("triangle")) {
										// If a triangle is present, checks that there are at least 3 vertices in the
										// object
										if (ivert < 3) {
											fErrorReturn += "<br>ER_114: Triangle defined in object but number of vertices: "
													+ ivert + " is less than the required 3 vertices" + " in file: "
													+ file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_114\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "<br>ER_114: Triangle defined in object but number of vertices: "
													+ ivert + " is less than the required 3 vertices" + " in file: "
													+ file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_114");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										itrg++;
										// Checks for
										if (vnode.getAttributes().getNamedItem("p1") != null) {
											String addToList = vnode.getAttributes().getNamedItem("p1").getTextContent()
													.trim();
											p1.add(Integer.parseInt(addToList));
										} else {
											p1.add(-1);
										}
										if (vnode.getAttributes().getNamedItem("p2") != null) {
											String addToList = vnode.getAttributes().getNamedItem("p2").getTextContent()
													.trim();
											p2.add(Integer.parseInt(addToList));
										} else {
											p2.add(-1);
										}
										if (vnode.getAttributes().getNamedItem("p3") != null) {
											String addToList = vnode.getAttributes().getNamedItem("p3").getTextContent()
													.trim();
											p3.add(Integer.parseInt(addToList));
										} else {
											p3.add(-1);
										}
										if (vnode.getAttributes().getNamedItem("pid") != null) {
											String addToList = vnode.getAttributes().getNamedItem("pid")
													.getTextContent().trim();
											tripid.add(Integer.parseInt(addToList));
										} else {
											tripid.add(-1);
										}

										String tst = vnode.getAttributes().getNamedItem("v1").getTextContent().trim();
										vertList.add(tst);
										v1.add(Integer.parseInt(tst));
										if (Integer.parseInt(tst) >= ivert) {
											fErrorReturn += "<br>ER_090: Triangle with v1=" + tst
													+ " is greater than the number of availalbe (zero based) vertices: "
													+ ivert + " in file: " + file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "Triangle with v1=" + tst
													+ " is greater than the number of availalbe (zero based) vertices: "
													+ ivert + " in file: " + file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_090");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}

										}
										tst = vnode.getAttributes().getNamedItem("v2").getTextContent().trim();
										v2.add(Integer.parseInt(tst));
										if (vertList.contains(tst)) {
											fWarningReturn += "<br>WR_016: Triangle: " + itrg + " in file: " + file
													+ " uses duplicate references to the same vertex " + tst + ".";
											fxmlWarningReturn += "<Issue  issueID=\"WR_014\" severity=\"info\" class=\"structure\" type=\"Vertex warning\">";
											fxmlWarningReturn += "Triangle: " + itrg + " in file: " + file
													+ " uses duplicate references to the same vertex " + tst + ".";
											fxmlWarningReturn += "</Issue>";
											errChk("WR_016");
											iwrCnt++;
											if (iwrCnt > iWrnMaxStr) {
												fWarningReturn += "<br>More than " + iWrnMaxStr
														+ " warnings found, stopping..<br>";
												return;
											}
										} else {
											vertList.add(tst);
										}
										if (Integer.parseInt(tst) >= ivert) {
											fErrorReturn += "<br>ER_091: Triangle with v2=" + tst
													+ " is greater than the number of availalbe (zero based) vertices: "
													+ ivert + " in file: " + file;

											fxmlErrorReturn += "<Issue  issueID=\"ER_091\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "Triangle with v2=" + tst
													+ " is greater than the number of availalbe (zero based) vertices: "
													+ ivert + " in file: " + file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_091");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										tst = vnode.getAttributes().getNamedItem("v3").getTextContent().trim();
										v3.add(Integer.parseInt(tst));
										if (vertList.contains(tst)) {
											fWarningReturn += "<br>WR_016: Triangle: " + itrg + " in file: " + file
													+ " uses duplicate references to the same vertex " + tst + ".";
											fxmlWarningReturn += "<Issue  issueID=\"WR_014\" severity=\"info\" class=\"structure\" type=\"Vertex warning\">";
											fxmlWarningReturn += "Triangle: " + itrg + " in file: " + file
													+ " uses duplicate references to the same vertex " + tst + ".";
											fxmlWarningReturn += "</Issue>";
											errChk("WR_016");
											iwrCnt++;
											if (iwrCnt > iWrnMaxStr) {
												fWarningReturn += "<br>More than " + iWrnMaxStr
														+ " warnings found, stopping..<br>";
												return;
											}
										} else {
											vertList.add(tst);
										}
										if (Integer.parseInt(tst) >= ivert) {
											fErrorReturn += "<br>ER_092: Triangle with v3=" + tst
													+ " is greater than the number of availalbe (zero based) vertices: "
													+ ivert + " in file: " + file;

											fxmlErrorReturn += "<Issue  issueID=\"ER_092\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "Triangle with v3=" + tst
													+ " is greater than the number of availalbe (zero based) vertices: "
													+ ivert + " in file: " + file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_092");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}

										if (vnode.getAttributes().getNamedItem("p1") != null
												&& vnode.getAttributes().getNamedItem("pid") != null
												&& pidChk.equals("") && pindexChk.equals("")) {
											fErrorReturn += "<br>ER_104: Triangle with p1="
													+ vnode.getAttributes().getNamedItem("p1").getTextContent()
													+ " and pid="
													+ vnode.getAttributes().getNamedItem("pid").getTextContent()
													+ "  in file: " + file + " with object id: " + objId
													+ " has p1 and pid defined but does not have the pid or pindex defined in the object.";
											fxmlErrorReturn += "<Issue  issueID=\"ER_104\" severity=\"moderate\" class=\"structure\" type=\"no default material\">";
											fxmlErrorReturn += "Triangle with p1="
													+ vnode.getAttributes().getNamedItem("p1").getTextContent()
													+ " and pid="
													+ vnode.getAttributes().getNamedItem("pid").getTextContent()
													+ "  in file: " + file + " with object id: " + objId
													+ " has p1 and pid defined but does not have the pid or pindex defined in the object.";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_104");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}

										} else if (vnode.getAttributes().getNamedItem("p1") != null
												&& vnode.getAttributes().getNamedItem("pid") != null
												&& pidChk.equals("")) {
											fErrorReturn += "<br>ER_105: Triangle with p1="
													+ vnode.getAttributes().getNamedItem("p1").getTextContent()
													+ " and pid="
													+ vnode.getAttributes().getNamedItem("pid").getTextContent()
													+ "  in file: " + file + " with object id: " + objId
													+ " has p1 and pid defined but does not have the pid defined in the object.";
											fxmlErrorReturn += "<Issue  issueID=\"ER_105\" severity=\"moderate\" class=\"structure\" type=\"no default material\">";
											fxmlErrorReturn += "Triangle with p1="
													+ vnode.getAttributes().getNamedItem("p1").getTextContent()
													+ " and pid="
													+ vnode.getAttributes().getNamedItem("pid").getTextContent()
													+ "  in file: " + file + " with object id: " + objId
													+ " has p1 and pid defined but does not have the pid defined in the object.";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_105");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										} else if (vnode.getAttributes().getNamedItem("p1") != null
												&& vnode.getAttributes().getNamedItem("pid") != null
												&& pindexChk.equals("")) {
											fErrorReturn += "<br>ER_106: Triangle with p1="
													+ vnode.getAttributes().getNamedItem("p1").getTextContent()
													+ " and pid="
													+ vnode.getAttributes().getNamedItem("pid").getTextContent()
													+ "  in file: " + file + " with object id: " + objId
													+ " has p1 and pid defined but does not have the pindex defined in the object.";
											fxmlErrorReturn += "<Issue  issueID=\"ER_106\" severity=\"moderate\" class=\"structure\" type=\"no default material\">";
											fxmlErrorReturn += "Triangle with p1="
													+ vnode.getAttributes().getNamedItem("p1").getTextContent()
													+ " and pid="
													+ vnode.getAttributes().getNamedItem("pid").getTextContent()
													+ "  in file: " + file + " with object id: " + objId
													+ " has p1 and pid defined but does not have the pindex defined in the object.";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_106");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										if (vnode.getAttributes().getNamedItem("p1") == null
												&& (vnode.getAttributes().getNamedItem("p2") != null
														|| vnode.getAttributes().getNamedItem("p3") != null)) {
											String sadd = "";
											if (vnode.getAttributes().getNamedItem("p2") != null) {
												sadd += " " + vnode.getAttributes().getNamedItem("p2") + ",";
											}
											if (vnode.getAttributes().getNamedItem("p3") != null) {
												sadd += " " + vnode.getAttributes().getNamedItem("p3");
											}
											fWarningReturn += "<br>WR_014: Triangle: " + sadd + " in file: " + file
													+ " does not define p1 while p2 and/or p3 have been set.";
											fxmlWarningReturn += "<Issue  issueID=\"WR_014\" severity=\"info\" class=\"structure\" type=\"ID warning\">";
											fxmlWarningReturn += "Triangle: " + sadd + " in file: " + file
													+ " does not define p1 while p2 and/or p3 have been set.";
											fxmlWarningReturn += "</Issue>";
											errChk("WR_014");
											iwrCnt++;
											if (iwrCnt > iWrnMaxStr) {
												fWarningReturn += "<br>More than " + iWrnMaxStr
														+ " warnings found, stopping..<br>";
												return;
											}
										}
										/*
										 * if (vnode.getAttributes().getNamedItem("p1") != null &&
										 * ((vnode.getAttributes().getNamedItem("p2") != null &&
										 * vnode.getAttributes().getNamedItem("p3") == null) ||
										 * (vnode.getAttributes().getNamedItem("p3") != null &&
										 * vnode.getAttributes().getNamedItem("p2") == null))) { String sadd = ""; sadd
										 * += " with " + vnode.getAttributes().getNamedItem("p1") + ","; if
										 * (vnode.getAttributes().getNamedItem("p2") != null) { sadd += " " +
										 * vnode.getAttributes().getNamedItem("p2") + ","; } if
										 * (vnode.getAttributes().getNamedItem("p3") != null) { sadd += " " +
										 * vnode.getAttributes().getNamedItem("p3"); } fWarningReturn +=
										 * "<br>WR_015: Triangle: " + sadd + " in file: " + file +
										 * " defines p1 while either p2 or p3 have not been set."; fxmlWarningReturn +=
										 * "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"ID warning\">"
										 * ; fxmlWarningReturn += "Triangle: " + sadd + " in file: " + file +
										 * " defines p1 while either p2 or p3 have not been set."; fxmlWarningReturn +=
										 * "</Issue>"; errChk("WR_015"); iwrCnt++; if (iwrCnt > iWrnMaxStr) {
										 * fWarningReturn += "<br>More than " + iWrnMaxStr +
										 * " warnings found, stopping..<br>"; return; } } if
										 * (vnode.getAttributes().getNamedItem("p1") == null &&
										 * vnode.getAttributes().getNamedItem("pid") != null) { tst =
										 * vnode.getAttributes().getNamedItem("pid").getTextContent().trim();
										 * fWarningReturn += "<br>WR_015: Triangle: with pid=" + tst + " in file: " +
										 * file + " has not defined p1."; fxmlWarningReturn +=
										 * "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"ID warning\">"
										 * ; fxmlWarningReturn += "Triangle: with pid=" + tst + " in file: " + file +
										 * " has not defined p1."; fxmlWarningReturn += "</Issue>"; errChk("WR_015");
										 * iwrCnt++; if (iwrCnt > iWrnMaxStr) { fWarningReturn += "<br>More than " +
										 * iWrnMaxStr + " warnings found, stopping..<br>"; return; } } if
										 * (vnode.getAttributes().getNamedItem("p2") == null &&
										 * vnode.getAttributes().getNamedItem("pid") != null) { tst =
										 * vnode.getAttributes().getNamedItem("pid").getTextContent().trim();
										 * fWarningReturn += "<br>WR_015: Triangle: with pid=" + tst + " in file: " +
										 * file + " has not defined p2."; fxmlWarningReturn +=
										 * "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"ID warning\">"
										 * ; fxmlWarningReturn += "Triangle: with pid=" + tst + " in file: " + file +
										 * " has not defined p2."; fxmlWarningReturn += "</Issue>"; errChk("WR_015");
										 * iwrCnt++; if (iwrCnt > iWrnMaxStr) { fWarningReturn += "<br>More than " +
										 * iWrnMaxStr + " warnings found, stopping..<br>"; return; } } if
										 * (vnode.getAttributes().getNamedItem("p3") == null &&
										 * vnode.getAttributes().getNamedItem("pid") != null) { tst =
										 * vnode.getAttributes().getNamedItem("pid").getTextContent().trim();
										 * fWarningReturn += "<br>WR_015: Triangle: with pid=" + tst + " in file: " +
										 * file + " has not defined p3."; fxmlWarningReturn +=
										 * "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"ID warning\">"
										 * ; fxmlWarningReturn += "Triangle: with pid=" + tst + " in file: " + file +
										 * " has not defined p3."; fxmlWarningReturn += "</Issue>"; errChk("WR_015");
										 * iwrCnt++; if (iwrCnt > iWrnMaxStr) { fWarningReturn += "<br>More than " +
										 * iWrnMaxStr + " warnings found, stopping..<br>"; return; } }
										 */
										if (vnode.getAttributes().getNamedItem("pid") != null) {
											tst = vnode.getAttributes().getNamedItem("pid").getTextContent().trim();
											Integer ichk = idWithCount.get(tst);
											if (ichk == null) {
												fErrorReturn += "<br>ER_097: Triangle with pid=" + tst + " in file: "
														+ file + " does not exist yet.";
												fxmlErrorReturn += "<Issue  issueID=\"ER_097\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
												fxmlErrorReturn += "Triangle with pid=" + tst + " in file: " + file
														+ " does not exist yet.";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_097");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											} else {
												idUsed.put(tst, true);
												if (vnode.getAttributes().getNamedItem("p1") != null) {
													String tst2 = vnode.getAttributes().getNamedItem("p1")
															.getTextContent().trim();
													if (Integer.parseInt(tst2) >= ichk) {
														fErrorReturn += "<br>ER_098: Triangle with pid=" + tst + " p1="
																+ tst2 + " in file: " + file
																+ " exceeds the number of items in "
																+ idWithName.get(tst) + ".";
														fxmlErrorReturn += "<Issue  issueID=\"ER_098\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
														fxmlErrorReturn += "Triangle with pid=" + tst + " p1=" + tst2
																+ " in file: " + file
																+ " exceeds the number of items in "
																+ idWithName.get(tst) + ".";
														fxmlErrorReturn += "</Issue>";
														errChk("ER_098");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
												}
												if (vnode.getAttributes().getNamedItem("p2") != null) {
													String tst2 = vnode.getAttributes().getNamedItem("p2")
															.getTextContent().trim();
													if (Integer.parseInt(tst2) >= ichk) {
														fErrorReturn += "<br>ER_099: Triangle with pid=" + tst + " p2="
																+ tst2 + " in file: " + file
																+ " exceeds the number of items in "
																+ idWithName.get(tst) + ".";
														fxmlErrorReturn += "<Issue  issueID=\"ER_099\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
														fxmlErrorReturn += "Triangle with pid=" + tst + " p2=" + tst2
																+ " in file: " + file
																+ " exceeds the number of items in "
																+ idWithName.get(tst) + ".";
														fxmlErrorReturn += "</Issue>";
														errChk("ER_099");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
												}
												if (vnode.getAttributes().getNamedItem("p3") != null) {
													String tst2 = vnode.getAttributes().getNamedItem("p3")
															.getTextContent().trim();
													if (Integer.parseInt(tst2) >= ichk) {
														fErrorReturn += "<br>ER_100: Triangle with pid=" + tst + " p3="
																+ tst2 + " in file: " + file
																+ " exceeds the number of items in "
																+ idWithName.get(tst) + ".";
														fxmlErrorReturn += "<Issue  issueID=\"ER_100\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
														fxmlErrorReturn += "Triangle with pid=" + tst + " p3=" + tst2
																+ " in file: " + file
																+ " exceeds the number of items in "
																+ idWithName.get(tst) + ".";
														fxmlErrorReturn += "</Issue>";
														errChk("ER_100");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
												}
											}
										}
									}
								}
							}
						} catch (Exception et) {
							fErrorReturn += "<br>ER_077: Caught an exception: " + et.getMessage() + "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
							fxmlErrorReturn += "Caught an exception: " + et.getMessage();
							fxmlErrorReturn += "</Issue>";
							errChk("ER_077");
							et.printStackTrace();
							ierCnt++;
						}

						// Check for Triangle Sets
						List<String> triangleSetList = new ArrayList<String>();
						NodeList triangleSetNodes = cnode.getChildNodes();
						try {
							if (cnode.getNodeName().endsWith("trianglesets")) {
								for (int jj = 0; jj < triangleSetNodes.getLength(); jj++) {
									Node vnode = triangleSetNodes.item(jj);
									if (vnode.getNodeName().endsWith("triangleset")) {
										NodeList xList = vnode.getChildNodes();
										String tste = vnode.getAttributes().getNamedItem("name").getTextContent()
												.trim();
										if (tste == "") {
											fErrorReturn += "<br>ER_115: Triangle Set name must not be empty"
													+ " in file: " + file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_115\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "Triangle Set name must not be empty" + " in file: "
													+ file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_115");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										triangleSetList.add(tste);
										for (int kk = 0; kk < xList.getLength(); kk++) {
											Node xnode = xList.item(kk);
											if (xnode.getNodeName().endsWith("ref")) {
												String refIndex = xnode.getAttributes().getNamedItem("index")
														.getTextContent().trim();
												if (Integer.parseInt(refIndex) > itrg - 1) {
													fErrorReturn += "<br>ER_116: Triangle Set with triangle=" + refIndex
															+ " is greater than the number of availalbe triangles: "
															+ itrg + " in file: " + file;
													fxmlErrorReturn += "<Issue  issueID=\"ER_116\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "Triangle Set with Triangle=" + refIndex
															+ " is greater than the number of availalbe triangles: "
															+ itrg + " in file: " + file;
													fxmlErrorReturn += "</Issue>";
													errChk("ER_116");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
											}
											if (xnode.getNodeName().endsWith("refrange")) {
												String startIndex = xnode.getAttributes().getNamedItem("startindex")
														.getTextContent().trim();
												String endIndex = xnode.getAttributes().getNamedItem("endindex")
														.getTextContent().trim();
												if (Integer.parseInt(endIndex) < Integer.parseInt(startIndex)) {
													fErrorReturn += "<br>ER_117: refrange with start index "
															+ startIndex + " and end index " + endIndex
															+ " has end index smaller than start index " + " in file: "
															+ file;
													fxmlErrorReturn += "<Issue  issueID=\"ER_117\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "refrange with start index " + startIndex
															+ " and endindex " + endIndex
															+ " has end index smaller than start index" + " in file: "
															+ file;
													fxmlErrorReturn += "</Issue>";
													errChk("ER_117");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (Integer.parseInt(endIndex) > itrg - 1) {
													fErrorReturn += "<br>ER_118: refrange with end index= " + endIndex
															+ " is greater than the number of availalbe triangles: "
															+ itrg + " in file: " + file;
													fxmlErrorReturn += "<Issue  issueID=\"ER_118\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "refrange with end inxex= " + endIndex
															+ " is greater than the number of availalbe triangles: "
															+ itrg + " in file: " + file;
													fxmlErrorReturn += "</Issue>";
													errChk("ER_118");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
											}
										}
									}
								}
							}
						} catch (Exception et) {
							fErrorReturn += "<br>ER_077: Caught an exception: " + et.getMessage() + "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
							fxmlErrorReturn += "Caught an exception: " + et.getMessage();
							fxmlErrorReturn += "</Issue>";
							errChk("ER_077");
							et.printStackTrace();
							ierCnt++;
						}

						// Check for mirror mesh
						try {
							if (cnode.getNodeName().endsWith("mirrormesh")) {
								// System.out.println("Found mirror mesh");
								// System.out.println("Object List length: " + objectNodeList.size());
								// creates lists to store the vertex coordinates of the original mesh and the
								// v1-3 for the triangles
								List<Double> origvertx = new ArrayList<Double>();
								List<Double> origverty = new ArrayList<Double>();
								List<Double> origvertz = new ArrayList<Double>();
								List<Integer> origv1 = new ArrayList<Integer>();
								List<Integer> origv2 = new ArrayList<Integer>();
								List<Integer> origv3 = new ArrayList<Integer>();
								List<Integer> origp1 = new ArrayList<Integer>();
								List<Integer> origp2 = new ArrayList<Integer>();
								List<Integer> origp3 = new ArrayList<Integer>();
								List<Integer> origtripid = new ArrayList<Integer>();

								boolean emptySets = false;
								boolean containsComponent = false;

								// count of the triangles and vertex in the original mesh
								int origVertCount = 0;
								int origTriCount = 0;

								// Checks to make sure the mirror mesh isn't pointing to itself
								String objID = objNode.getAttributes().getNamedItem("id").getTextContent();
								String orMesh = cnode.getAttributes().getNamedItem("originalmesh").getTextContent();
								if (Integer.parseInt(objID) == Integer.parseInt(orMesh)) {
									fErrorReturn += "<br>ER_119: mirrormesh with originalmesh: " + orMesh
											+ " cannot point to the same mesh it is inside: " + objID + " in file: "
											+ file;
									fxmlErrorReturn += "<Issue  issueID=\"ER_119\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
											+ " points to the mesh it is inside of " + objID + " in file: " + file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_119");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}

								// This section creates lists of all the triangles and vertices in the original
								// mesh
								Node resourceNode = cnode.getParentNode().getParentNode().getParentNode();
								Node origObjectNode = null;
								Node origMeshNode = null;

								// Navigates to the object node where id is equal to originalmesh value
								NodeList resourceNodeList = resourceNode.getChildNodes();
								for (int jj = 0; jj < resourceNodeList.getLength(); jj++) {
									if (resourceNodeList.item(jj).getNodeName() == "object"
											&& Integer.parseInt(resourceNodeList.item(jj).getAttributes()
													.getNamedItem("id").getTextContent()) == Integer.parseInt(orMesh)) {
										origObjectNode = resourceNodeList.item(jj);
										// System.out.println("Found original object node");
									}
								}
								// navigates to the mesh node within the correct object node
								boolean containsMesh = false;
								if (origObjectNode != null) {
									NodeList objNodeList = origObjectNode.getChildNodes();
									for (int jj = 0; jj < objNodeList.getLength(); jj++) {
										if (objNodeList.item(jj).getNodeName() == "mesh") {
											origMeshNode = objNodeList.item(jj);
											// System.out.println("Found original mesh node");
											containsMesh = true;
										}

										if (objNodeList.item(jj).getNodeName() == "components") {
											containsComponent = true;
											fErrorReturn += "<br>ER_131: mirrormesh with originalmesh: " + orMesh
													+ " has a component refrence in originalmesh" + " in file: " + file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_131\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
													+ " has a component refrence in original mesh" + " in file: "
													+ file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_131");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
								}
								// System.out.println(containsMesh);
								if (!containsMesh && !containsComponent) {
									fErrorReturn += "<br>ER_130: mirrormesh with originalmesh: " + orMesh
											+ " has no mesh element in original mesh" + " in file: " + file;
									fxmlErrorReturn += "<Issue  issueID=\"ER_130\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
											+ " has no mesh element in original mesh" + " in file: " + file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_130");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
								// creates lists of the vertices and triangles for the original mesh
								if (origMeshNode != null) {
									NodeList meshNodeList = origMeshNode.getChildNodes();
									for (int jj = 0; jj < meshNodeList.getLength(); jj++) {
										if (meshNodeList.item(jj).getNodeName().endsWith("mirrormesh")) {
											fErrorReturn += "<br>ER_126: mirrormesh with originalmesh: " + orMesh
													+ " has originalmesh that includes a mirror mesh" + " in file: "
													+ file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_126\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
													+ " has originalmesh that includes a mirror mesh" + " in file: "
													+ file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_126");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										if (meshNodeList.item(jj).getNodeName().equals("vertices")) {
											// System.out.println("Found original vertices node");
											NodeList vList = meshNodeList.item(jj).getChildNodes();
											for (int kk = 0; kk < vList.getLength(); kk++) {
												Node vnode = vList.item(kk);
												// If a vertex is found, adds x, y, and z coordinates to list
												if (vnode.getNodeName().equals("vertex")) {
													origvertx.add(Double.parseDouble(
															vnode.getAttributes().getNamedItem("x").getTextContent()));
													origverty.add(Double.parseDouble(
															vnode.getAttributes().getNamedItem("y").getTextContent()));
													origvertz.add(Double.parseDouble(
															vnode.getAttributes().getNamedItem("z").getTextContent()));
													origVertCount++;
												}
											}
										} else if (meshNodeList.item(jj).getNodeName().equals("triangles")) {
											// System.out.println("Found original triangles node");
											NodeList vList = meshNodeList.item(jj).getChildNodes();
											for (int kk = 0; kk < vList.getLength(); kk++) {
												Node vnode = vList.item(kk);
												// If a triangle is found, adds v1, v2, v3, p1, p2, p3, and pid to a
												// list
												if (vnode.getNodeName().equals("triangle")) {
													origv1.add(Integer.parseInt(
															vnode.getAttributes().getNamedItem("v1").getTextContent()));
													origv2.add(Integer.parseInt(
															vnode.getAttributes().getNamedItem("v2").getTextContent()));
													origv3.add(Integer.parseInt(
															vnode.getAttributes().getNamedItem("v3").getTextContent()));
													origTriCount++;
													if (vnode.getAttributes().getNamedItem("p1") != null) {
														String addToList = vnode.getAttributes().getNamedItem("p1")
																.getTextContent().trim();
														origp1.add(Integer.parseInt(addToList));
													} else {
														origp1.add(-1);
													}
													if (vnode.getAttributes().getNamedItem("p2") != null) {
														String addToList = vnode.getAttributes().getNamedItem("p2")
																.getTextContent().trim();
														origp2.add(Integer.parseInt(addToList));
													} else {
														origp2.add(-1);
													}
													if (vnode.getAttributes().getNamedItem("p3") != null) {
														String addToList = vnode.getAttributes().getNamedItem("p3")
																.getTextContent().trim();
														origp3.add(Integer.parseInt(addToList));
													} else {
														origp3.add(-1);
													}
													if (vnode.getAttributes().getNamedItem("pid") != null) {
														String addToList = vnode.getAttributes().getNamedItem("pid")
																.getTextContent().trim();
														origtripid.add(Integer.parseInt(addToList));
													} else {
														origtripid.add(-1);
													}
												}
											}
										}
									}
								}

								// System.out.println("Original vert count: " + origVertCount);
								// System.out.println("Original triangle count: " + origTriCount);

								// Time to check if the vertex and triangle count are same between original and
								// mirror
								if (origObjectNode != null && !containsComponent) {
									// First check if vertex, triangle are both zero
									if (itrg == 0 || ivert == 0) {
										if (ivert != 0) {
											fErrorReturn += "<br>ER_127: mirrormesh with originalmesh: " + orMesh
													+ " has vertex count " + ivert + " not equal to zero when"
													+ " triangle count " + itrg + " is equal to zero"
													+ " either make both zero, or make them the same as the original mesh"
													+ " in file: " + file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_127\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
													+ " has vertex count " + ivert + " not equal to zero when"
													+ " triangle count " + itrg + " is equal to zero"
													+ " Original mesh data will be used for object generation instead of mirror mesh data."
													+ " in file: " + file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_127");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										if (itrg != 0) {
											fErrorReturn += "<br>ER_128: mirrormesh with originalmesh: " + orMesh
													+ " has triangle count " + itrg + " not equal to zero when"
													+ " vertex count " + ivert + " is equal to zero"
													+ " either make both zero, or make them the same as the original mesh"
													+ " in file: " + file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_128\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
													+ " has triangle count " + itrg + " not equal to zero when"
													+ " vertex count " + ivert + " is equal to zero"
													+ " Original mesh data will be used for object generation instead of mirror mesh data."
													+ " in file: " + file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_128");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
									// Checks if vertex count is different between original and mirror
									if (origVertCount != ivert && origTriCount == itrg && itrg != 0) {
										fErrorReturn += "<br>ER_120: mirrormesh with originalmesh: " + orMesh
												+ " has vertex count " + ivert
												+ " different from original object vertex count " + origVertCount
												+ " this may be intentional for some test cases." + " in file: " + file
												+ ". Original mesh data will be used for object generation instead of mirror mesh data.";
										fxmlErrorReturn += "<Issue  issueID=\"ER_120\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
												+ " has vertex count " + ivert
												+ " different from original object vertex count " + origVertCount
												+ " in file: " + file
												+ ". Original mesh data will be used for object generation instead of mirror mesh data.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_120");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Checks if triangle count is different between original and mirror
									if (origVertCount == ivert && origTriCount != itrg && itrg != 0) {
										fErrorReturn += "<br>ER_121: mirrormesh with originalmesh: " + orMesh
												+ " has triangle count " + itrg
												+ " different from original object triangle count " + origTriCount
												+ " Original mesh data will be used for object generation instead of mirror mesh data."
												+ " in file: " + file;
										fxmlErrorReturn += "<Issue  issueID=\"ER_121\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
												+ " has triangle count " + itrg
												+ " different from original object triangle count " + origTriCount
												+ " Original mesh data will be used for object generation instead of mirror mesh data."
												+ " in file: " + file;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_121");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Checks if vertex count and triangle count are both different between original
									// and mirror
									if (origVertCount != ivert && origTriCount != itrg && itrg != 0) {
										fErrorReturn += "<br>ER_122: mirrormesh with originalmesh: " + orMesh
												+ " has triangle count " + itrg
												+ " different from original object triangle count " + origTriCount
												+ " and vertex count " + ivert
												+ " different from original object vertex count " + origVertCount
												+ " Original mesh data will be used for object generation instead of mirror mesh data."
												+ " in file: " + file;
										fxmlErrorReturn += "<Issue  issueID=\"ER_122\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "mirrormesh with originalmesh " + orMesh
												+ " has triangle count " + itrg
												+ " different from original object triangle count " + origTriCount
												+ " and vertex count " + ivert
												+ " different from original object vertex count " + origVertCount
												+ " Original mesh data will be used for object generation instead of mirror mesh data."
												+ " in file: " + file;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_122");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Checks if the triangles have the proper values for v1, v2, and v3
									if (origVertCount == ivert && origTriCount == itrg) {
										// System.out.println("Vertex and Triangle count match " + itrg);
										for (int jj = 0; jj < itrg; jj++) {
											// Comparing original v2 to mirror v2
											int originalv2 = origv2.get(jj);
											int newv2 = v2.get(jj);
											if (originalv2 != newv2) {
												fErrorReturn += "<br>ER_123: mirrored triangle " + jj + " with v2: "
														+ newv2 + " has different v2 from original mesh v2: "
														+ originalv2 + ". This may be intentional for some test cases."
														+ " in file: " + file;
												fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "<br>ER_123: mirrored triangle " + jj + " with v2: "
														+ newv2 + " has different v2 from original mesh v2: "
														+ originalv2
														+ ". Original mesh data will be used for object generation instead of mirror mesh data."
														+ " in file: " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_123");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
											// Comparing original v1 to mirror v3
											int originalv1 = origv1.get(jj);
											int newv3 = v3.get(jj);
											if (originalv1 != newv3) {
												fErrorReturn += "<br>ER_124: mirrored triangle " + jj + " with v3: "
														+ newv3 + " has different v3 from original mesh v1: "
														+ originalv1 + ". This may be intentional for some test cases."
														+ " in file: " + file;
												fxmlErrorReturn += "<Issue  issueID=\"ER_124\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "<br>ER_090: mirrored triangle " + jj + "with v3: "
														+ newv3 + " has different v3 from original mesh v1: "
														+ originalv1
														+ ". Original mesh data will be used for object generation instead of mirror mesh data."
														+ " in file: " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_124");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
											// Comparing original v3 to mirror v1
											int originalv3 = origv3.get(jj);
											int newv1 = v1.get(jj);
											if (originalv3 != newv1) {
												fErrorReturn += "<br>ER_125: mirrored triangle " + jj + " with v1: "
														+ newv1 + " has different v1 from original mesh v3: "
														+ originalv3 + ". This may be intentional for some test cases."
														+ " in file: " + file;
												fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "<br>ER_125: mirrored triangle " + jj + " with v1: "
														+ newv1 + " has different v1 from original mesh v3: "
														+ originalv3
														+ ". Original mesh data will be used for object generation instead of mirror mesh data."
														+ " in file: " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_125");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
									}
									if (origp1.size() != p1.size() || origp2.size() != p2.size()
											|| origp3.size() != p3.size() || tripid.size() != origtripid.size()) {
									} else {
										for (int jj = 0; jj < origp1.size(); jj++) {
											if (origp1.get(jj) != p3.get(jj)) {
												fErrorReturn += "<br>ER_132: mirrored triangle " + jj + " with p1: "
														+ origp1.get(jj).toString()
														+ " has different p1 from original mesh p3: "
														+ p3.get(jj).toString()
														+ ". This may be intentional for some test cases."
														+ " in file: " + file;
												fxmlErrorReturn += "<Issue  issueID=\"ER_132\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "<br>ER_125: mirrored triangle " + jj + " with p1: "
														+ origp1.get(jj).toString()
														+ " has different p1 from original mesh p3: "
														+ p3.get(jj).toString()
														+ ". Original mesh data will be used for object generation instead of mirror mesh data."
														+ " in file: " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_132");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
												// System.out.println("p1 does not match p3");
											}
											if (origp2.get(jj) != p2.get(jj)) {
												fErrorReturn += "<br>ER_132: mirrored triangle " + jj + " with p2: "
														+ origp2.get(jj).toString()
														+ " has different p2 from original mesh p2: "
														+ p2.get(jj).toString()
														+ ". This may be intentional for some test cases."
														+ " in file: " + file;
												fxmlErrorReturn += "<Issue  issueID=\"ER_132\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "<br>ER_125: mirrored triangle " + jj + " with p2: "
														+ origp2.get(jj).toString()
														+ " has different p2 from original mesh p2: "
														+ p2.get(jj).toString()
														+ ". Original mesh data will be used for object generation instead of mirror mesh data."
														+ " in file: " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_132");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
												// System.out.println("p2 do not match");
											}
											if (origp3.get(jj) != p1.get(jj)) {
												fErrorReturn += "<br>ER_132: mirrored triangle " + jj + " with p3: "
														+ origp3.get(jj).toString()
														+ " has different p3 from original mesh p1: "
														+ p1.get(jj).toString()
														+ ". This may be intentional for some test cases."
														+ " in file: " + file;
												fxmlErrorReturn += "<Issue  issueID=\"ER_132\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "<br>ER_125: mirrored triangle " + jj + " with p3: "
														+ origp3.get(jj).toString()
														+ " has different p3 from original mesh p1: "
														+ p1.get(jj).toString()
														+ ". Original mesh data will be used for object generation instead of mirror mesh data."
														+ " in file: " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_132");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
												// System.out.println("p3 does not match p1");
											}
											if (tripid.get(jj) != origtripid.get(jj)) {
												fErrorReturn += "<br>ER_132: mirrored triangle " + jj + " with pid: "
														+ origtripid.get(jj).toString()
														+ " has different pid from original mesh pid: "
														+ tripid.get(jj).toString()
														+ ". This may be intentional for some test cases."
														+ " in file: " + file;
												fxmlErrorReturn += "<Issue  issueID=\"ER_132\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "<br>ER_125: mirrored triangle " + jj + " with pid: "
														+ origtripid.get(jj).toString()
														+ " has different pid from original mesh pid: "
														+ tripid.get(jj).toString()
														+ ". Original mesh data will be used for object generation instead of mirror mesh data."
														+ " in file: " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_132");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
												// System.out.println("p3 does not match p1");
											}
										}
									}
								}
							}
						} catch (Exception et) {
							fErrorReturn += "<br>ER_077: Caught an exception: " + et.getMessage() + "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
							fxmlErrorReturn += "Caught an exception: " + et.getMessage();
							fxmlErrorReturn += "</Issue>";
							errChk("ER_077");
							et.printStackTrace();
							ierCnt++;
						}

						// now check for beamlattices
						try {
							if (cnode.getNodeName().endsWith("beamlattice")) {
								String objpidChk = "";
								Node pMesh = cnode.getParentNode();
								Node pObj = pMesh.getParentNode();
								if (pObj.getAttributes().getNamedItem("pid") != null)
									objpidChk = pObj.getAttributes().getNamedItem("pid").getTextContent().trim();

								String pidBeamLatChk = "";
								String pindexBeamLatChk = "";

								if (cnode.getAttributes().getNamedItem("pid") != null) {
									pidBeamLatChk = cnode.getAttributes().getNamedItem("pid").getTextContent().trim();
								}
								if (cnode.getAttributes().getNamedItem("pindex") != null) {
									pindexBeamLatChk = cnode.getAttributes().getNamedItem("pindex").getTextContent()
											.trim();
								}

								if (pidBeamLatChk.length() > 0 && objpidChk.equals("")) {
									fErrorReturn += "<br>ER_089: beamlattice with pid=" + pidBeamLatChk + "  in file: "
											+ file + " with object id: " + objId
											+ " has pid defined but does not have the pid defined in the object.";
									fxmlErrorReturn += "<Issue  issueID=\"ER_089\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "beamlattice with  pid=" + pidBeamLatChk + "  in file: " + file
											+ " with object id: " + objId
											+ " has p and pid defined but does not have the pid defined in the object.";
									fxmlErrorReturn += "</Issue>";
									errChk("ER_089");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}

								}
								meshBeamId.put(currid, true);
								// minlength is required, so should be picked up
								// by schema if missing. Will throw null error
								// if it is missing.
								Double minlength = Double.parseDouble(
										cnode.getAttributes().getNamedItem("minlength").getTextContent().trim());
								// if you have a clippingmesh
								if (cnode.getAttributes().getNamedItem("clippingmesh") == null
										&& (cnode.getAttributes().getNamedItem("clippingmode") != null
												&& !cnode.getAttributes().getNamedItem("clippingmode").getTextContent()
														.trim().equals("none"))) {
									fErrorReturn += "<br>ER_090: Beamlattice missing clippingmesh while clippingmode is not null and not equal to 'none' in file: "
											+ file;
									fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "Beamlattice missing clippingmesh while clippingmode is not null and not equal to 'none' in file: "
											+ file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_090");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
								if ((cnode.getAttributes().getNamedItem("clippingmode") != null
										&& !(cnode.getAttributes().getNamedItem("clippingmode").getTextContent().trim()
												.equals("none")
												|| cnode.getAttributes().getNamedItem("clippingmode").getTextContent()
														.trim().equals("inside")
												|| cnode.getAttributes().getNamedItem("clippingmode").getTextContent()
														.trim().equals("outside")))) {
									fErrorReturn += "<br>ER_090: Beamlattice clippingmode while clippingmode is not set to none, inside, or outside in file: "
											+ file;
									fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "Beamlattice clippingmode while clippingmode is not set to none, inside, or outside in file: "
											+ file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_090");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
								// a clipping mesh id must not reference the
								// currid and have a valid id available.
								if (cnode.getAttributes().getNamedItem("clippingmesh") != null) {
									Integer cid = Integer.parseInt(
											cnode.getAttributes().getNamedItem("clippingmesh").getTextContent());
									if (cid == currid) {
										fErrorReturn += "<br>ER_090: Beamlattice clippingmesh id matches the id of the container object in file: "
												+ file;
										fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "Beamlattice clippingmesh id matches the id of the container object in file: "
												+ file;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_090");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									if (!meshModId.containsKey(cid)) {
										fErrorReturn += "<br>ER_090: Beamlattice clippingmesh id " + cid
												+ " does not match the id of a container object in file: " + file;
										fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "Beamlattice clippingmesh id " + cid
												+ " does not match the id of a container object in file: " + file;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_090");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else {
										if (!meshModId.get(cid).equals("model")) {
											fErrorReturn += "<br>ER_090: Beamlattice clippingmesh id " + cid
													+ " does not map to a container object with type model in file: "
													+ file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "Beamlattice clippingmesh id " + cid
													+ " does not map to a container object with type model in file: "
													+ file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_090");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
									if (meshBeamId.containsKey(cid)) {
										fErrorReturn += "<br>ER_090: Beamlattice clippingmesh id " + cid
												+ " references a mesh that contains a beamlattice in file: " + file;
										fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "Beamlattice clippingmesh id " + cid
												+ " references a mesh that contains a beamlattice in file: " + file;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_090");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}

								// a representationmesh id must not reference the
								// currid and have a valid id available.
								if (cnode.getAttributes().getNamedItem("representationmesh") != null) {
									Integer cid = Integer.parseInt(
											cnode.getAttributes().getNamedItem("representationmesh").getTextContent());
									if (cid == currid) {
										fErrorReturn += "<br>ER_090: Beamlattice representationmesh id matches the id of the container object in file: "
												+ file;
										fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "Beamlattice representationmesh id matches the id of the container object in file: "
												+ file;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_090");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									if (!meshModId.containsKey(cid)) {
										fErrorReturn += "<br>ER_090: Beamlattice representationmesh id " + cid
												+ " does not match the id of a container object in file: " + file;
										fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
										fxmlErrorReturn += "Beamlattice representationmesh id " + cid
												+ " does not match the id of a container object in file: " + file;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_090");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else {
										if (!meshModId.get(cid).equals("model")) {
											fErrorReturn += "<br>ER_090: Beamlattice representationmesh id " + cid
													+ " does not map to a container object with type model in file: "
													+ file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
											fxmlErrorReturn += "Beamlattice representationmesh id " + cid
													+ " does not map to a container object with type model in file: "
													+ file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_090");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									}
								}
								if (cnode.getAttributes().getNamedItem("b2:ballradius") == null
										&& (cnode.getAttributes().getNamedItem("b2:ballmode") != null
												&& !cnode.getAttributes().getNamedItem("b2:ballmode").getTextContent()
														.trim().equals("none"))) {
									fErrorReturn += "<br>ER_090: Beamlattice missing ballradius while ballmode is not null and not equal to 'none' in file: "
											+ file;
									fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "Beamlattice missing ballradius while ballmode is not null and not equal to 'none' in file: "
											+ file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_090");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
								if ((cnode.getAttributes().getNamedItem("b2:ballmode") != null
										&& !(cnode.getAttributes().getNamedItem("b2:ballmode").getTextContent().trim()
												.equals("none")
												|| cnode.getAttributes().getNamedItem("b2:ballmode").getTextContent()
														.trim().equals("mixed")
												|| cnode.getAttributes().getNamedItem("b2:ballmode").getTextContent()
														.trim().equals("all")))) {
									fErrorReturn += "<br>ER_090: Beamlattice ballmode while ballmode is not set to none, mixed, or all in file: "
											+ file;
									fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
									fxmlErrorReturn += "Beamlattice ballmode while ballmode is not set to none, mixed, or all in file: "
											+ file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_090");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
								if (cnode.getAttributes().getNamedItem("pid") != null) {
									String pid = cnode.getAttributes().getNamedItem("pid").getTextContent().trim();
									if (cnode.getAttributes().getNamedItem("pindex") == null) {
										fWarningReturn += "<br>WR_015: Beamlattice: with pid=" + pid + " in file: "
												+ file + " has not defined pindex.";
										fxmlWarningReturn += "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"ID warning\">";
										fxmlWarningReturn += "Beamlattice: with pid=" + pid + " in file: " + file
												+ " has not defined pindex.";
										fxmlWarningReturn += "</Issue>";
										errChk("WR_015");
										iwrCnt++;
										if (iwrCnt > iWrnMaxStr) {
											fWarningReturn += "<br>More than " + iWrnMaxStr
													+ " warnings found, stopping..<br>";
											return;
										}
									}
									String name = idWithName.get(pid);
									if (name == null) {
										fErrorReturn += "<br>ER_088: Beamlattice with pid: " + pid + ", in file: "
												+ file + " does not yet exist.";
										fxmlErrorReturn += "<Issue  issueID=\"ER_088\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
										fxmlErrorReturn += "Beamlattice with pid: " + pid + ", in file: " + file
												+ " does not yet exist.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_088");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									} else {
										idUsed.put(pid, true);
										if (cnode.getAttributes().getNamedItem("pindex") != null) {
											Integer ichk = idWithCount.get(pid);
											String tst2 = cnode.getAttributes().getNamedItem("pindex").getTextContent()
													.trim();
											if (Integer.parseInt(tst2) >= ichk) {
												fErrorReturn += "<br>ER_089: Beamlattice with pid: " + pid
														+ ", in file: " + file + " has a pindex of " + tst2
														+ " which exceeds the index count of " + ichk + " for " + name
														+ ".";
												fxmlErrorReturn += "<Issue  issueID=\"ER_089\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
												fxmlErrorReturn += "Beamlattice with pid: " + pid + ", in file: " + file
														+ " has a pindex of " + tst2
														+ " which exceeds the index count of " + ichk + " for " + name
														+ ".";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_089");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
									}
								}

								// a beam is contained in beams, so we have
								// another level that triangle above does not.
								// And beamset in beamsets
								int ibeam = 0;
								NodeList beamLatSubList = cnode.getChildNodes();
								List<String> vertListBeam = new ArrayList<String>();
								int ballcnt = 0;
								for (int jjb = 0; jjb < beamLatSubList.getLength(); jjb++) {
									// find the count of the number of ball in balls
									for (int jjx = 0; jjx < beamLatSubList.getLength(); jjx++) {
										if (beamLatSubList.item(jjx).getNodeName().endsWith("balls")) {
											ballcnt = 0;
											NodeList ballList = beamLatSubList.item(jjx).getChildNodes();
											for (int jj = 0; jj < ballList.getLength(); jj++) {
												Node vnode = ballList.item(jj);
												// <b:balls>
												// <b:ball p="1" pid="6" r="6" vindex="0"/>
												if (vnode.getNodeName().endsWith("ball")) {
													ballcnt++;
												}
											}
										}
									}
									NodeList vList = beamLatSubList.item(jjb).getChildNodes();
									if (beamLatSubList.item(jjb).getNodeName().endsWith("beams")) {
										for (int jj = 0; jj < vList.getLength(); jj++) {
											List<String> vertList = new ArrayList<String>();
											Node vnode = vList.item(jj);
											// <triangle p1="0" pid="4" v1="0"
											// v2="2"
											// v3="1"/>
											if (vnode.getNodeName().endsWith("beam")) {
												ibeam++;
												String tst = vnode.getAttributes().getNamedItem("v1").getTextContent()
														.trim();
												Double x1 = 0.0;
												Double y1 = 0.0;
												Double z1 = 0.0;
												// checks if first vertex of beam is a valid vertex defined in the
												// object
												if (Integer.parseInt(tst) >= ivert) {
													fErrorReturn += "<br>ER_090: Beam with v1=" + tst
															+ " is greater than the number of availalbe (zero based) vertices: "
															+ ivert + " in file: " + file;
													fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "Beam with v1=" + tst
															+ " is greater than the number of availalbe (zero based) vertices: "
															+ ivert + " in file: " + file;
													fxmlErrorReturn += "</Issue>";
													errChk("ER_090");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}

												} else {
													vertList.add(tst);
													vertListBeam.add(tst);
													x1 = vertx.get(Integer.parseInt(tst));
													y1 = verty.get(Integer.parseInt(tst));
													z1 = vertz.get(Integer.parseInt(tst));
												}
												tst = vnode.getAttributes().getNamedItem("v2").getTextContent().trim();
												Double x2 = 0.0;
												Double y2 = 0.0;
												Double z2 = 0.0;
												// checks if second vertex of beam is a valid vertex defined in object
												if (Integer.parseInt(tst) >= ivert) {
													fErrorReturn += "<br>ER_090: Beam with v2=" + tst
															+ " is greater than the number of availalbe (zero based) vertices: "
															+ ivert + " in file: " + file;
													fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "Beam with v2=" + tst
															+ " is greater than the number of availalbe (zero based) vertices: "
															+ ivert + " in file: " + file;
													fxmlErrorReturn += "</Issue>";
													errChk("ER_090");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												} else {
													vertList.add(tst);
													vertListBeam.add(tst);
													x2 = vertx.get(Integer.parseInt(tst));
													y2 = verty.get(Integer.parseInt(tst));
													z2 = vertz.get(Integer.parseInt(tst));
												}
												// Consumers must ignore beams shorter than minlength.
												Double length = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
														+ (z2 - z1) * (z2 - z1));
												if (length < minlength) {
													fWarningReturn += "<br>WR_025: Beam " + (jj + 1)
															+ " has a length less than minlength and must be ignored by consumers; length value: "
															+ length.toString() + " in file: " + file;

													fxmlWarningReturn += "<Issue  issueID=\"WR_025\" severity=\"info\" class=\"structure\" type=\"beam length warning\">";
													fxmlWarningReturn += "Beam " + (jj + 1)
															+ " has a length less than minlength and must be ignored by consumers; length value: "
															+ length.toString() + " in file: " + file;
													fxmlWarningReturn += "</Issue>";
													errChk("WR_025");
													iwrCnt++;
													if (iwrCnt > iWrnMaxStr) {
														fWarningReturn += "<br>More than " + iWrnMaxStr
																+ " warnings found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("v1").getTextContent().trim()
														.equals(vnode.getAttributes().getNamedItem("v2")
																.getTextContent().trim())) {
													fErrorReturn += "<br>ER_091: Beam " + (jj + 1)
															+ " has a vertexes set to the same value in file: " + file;

													fxmlErrorReturn += "<Issue  issueID=\"ER_091\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "Beam " + (jj + 1)
															+ " has a vertexes set to the same value in file: " + file;
													fxmlErrorReturn += "</Issue>";
													errChk("ER_091");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("p1") == null
														&& vnode.getAttributes().getNamedItem("pid") != null) {
													tst = vnode.getAttributes().getNamedItem("pid").getTextContent()
															.trim();
													fWarningReturn += "<br>WR_015: Beam: with pid=" + tst + " in file: "
															+ file + " has not defined p1.";
													fxmlWarningReturn += "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"ID warning\">";
													fxmlWarningReturn += "Beam: with pid=" + tst + " in file: " + file
															+ " has not defined p1.";
													fxmlWarningReturn += "</Issue>";
													errChk("WR_015");
													iwrCnt++;
													if (iwrCnt > iWrnMaxStr) {
														fWarningReturn += "<br>More than " + iWrnMaxStr
																+ " warnings found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("pid") != null
														&& (pidBeamLatChk.equals("") || objpidChk.equals(""))) {
													fErrorReturn += "<br>ER_089: beam with pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
																	.trim()
															+ "  in file: " + file + " with object id: " + objId
															+ " has pid defined but does not have the pid defined in both the beamlattice and the object.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_089\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "beamlattice with  pid=" + pidBeamLatChk
															+ "  in file: " + file + " with object id: " + objId
															+ " has p and pid defined but does not have the pid defined in both the beamlattice and the object";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_089");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}

												}
												if (vnode.getAttributes().getNamedItem("p1") != null
														&& vnode.getAttributes().getNamedItem("pid") != null
														&& pidChk.equals("") && pindexChk.equals("")) {
													fErrorReturn += "<br>ER_104: Beam with p1="
															+ vnode.getAttributes().getNamedItem("p1").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p1 and pid defined but does not have the pid or pindex defined in the object.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_104\" severity=\"moderate\" class=\"structure\" type=\"no default material\">";
													fxmlErrorReturn += "Beam with p1="
															+ vnode.getAttributes().getNamedItem("p1").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p1 and pid defined but does not have the pid or pindex defined in the object.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_104");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}

												} else if (vnode.getAttributes().getNamedItem("p1") != null
														&& vnode.getAttributes().getNamedItem("pid") != null
														&& pidChk.equals("")) {
													fErrorReturn += "<br>ER_105: Beam with p1="
															+ vnode.getAttributes().getNamedItem("p1").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p1 and pid defined but does not have the pid defined in the object.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_105\" severity=\"moderate\" class=\"structure\" type=\"no default material\">";
													fxmlErrorReturn += "Beam with p1="
															+ vnode.getAttributes().getNamedItem("p1").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p1 and pid defined but does not have the pid defined in the object.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_105");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												} else if (vnode.getAttributes().getNamedItem("p1") != null
														&& vnode.getAttributes().getNamedItem("pid") != null
														&& pindexChk.equals("")) {
													fErrorReturn += "<br>ER_106: Beam with p1="
															+ vnode.getAttributes().getNamedItem("p1").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p1 and pid defined but does not have the pindex defined in the object.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_106\" severity=\"moderate\" class=\"structure\" type=\"no default material\">";
													fxmlErrorReturn += "Beam with p1="
															+ vnode.getAttributes().getNamedItem("p1").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p1 and pid defined but does not have the pindex defined in the object.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_106");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("cap1") != null
														&& !(vnode.getAttributes().getNamedItem("cap1").getTextContent()
																.equals("butt")
																|| vnode.getAttributes().getNamedItem("cap1")
																		.getTextContent().equals("sphere")
																|| vnode.getAttributes().getNamedItem("cap1")
																		.getTextContent().equals("hemisphere"))) {
													fErrorReturn += "<br>ER_095: Beam with "
															+ vnode.getAttributes().getNamedItem("cap1") + " in file: "
															+ file + " does not match butt, sphere, or hemisphere.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_095\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "Beam with "
															+ vnode.getAttributes().getNamedItem("cap1") + " in file: "
															+ file + " does not match butt, sphere, or hemisphere.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_095");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("cap2") != null
														&& !(vnode.getAttributes().getNamedItem("cap2").getTextContent()
																.equals("butt")
																|| vnode.getAttributes().getNamedItem("cap2")
																		.getTextContent().equals("sphere")
																|| vnode.getAttributes().getNamedItem("cap2")
																		.getTextContent().equals("hemisphere"))) {
													fErrorReturn += "<br>ER_095: Beam with "
															+ vnode.getAttributes().getNamedItem("cap2") + " in file: "
															+ file + " does not match butt, sphere, or hemisphere.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_095\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "Beam with "
															+ vnode.getAttributes().getNamedItem("cap2") + " in file: "
															+ file + " does not match butt, sphere, or hemisphere.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_095");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("r2") != null
														&& vnode.getAttributes().getNamedItem("r1") == null) {
													String sadd = "";
													sadd += " with " + vnode.getAttributes().getNamedItem("r2");
													fErrorReturn += "<br>ER_095: Beam: " + sadd + " in file: " + file
															+ " defines r2 while r1 has not been set.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_095\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "Bem: " + sadd + " in file: " + file
															+ " defines r2 while r1 has not been set.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_095");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("pid") != null) {
													tst = vnode.getAttributes().getNamedItem("pid").getTextContent()
															.trim();
													Integer ichk = idWithCount.get(tst);
													if (ichk == null) {
														fErrorReturn += "<br>ER_097: Beam with pid=" + tst
																+ " in file: " + file + " does not exist yet.";
														fxmlErrorReturn += "<Issue  issueID=\"ER_097\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
														fxmlErrorReturn += "Beam with pid=" + tst + " in file: " + file
																+ " does not exist yet.";
														fxmlErrorReturn += "</Issue>";
														errChk("ER_097");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													} else {
														idUsed.put(tst, true);
														if (vnode.getAttributes().getNamedItem("p1") != null) {
															String tst2 = vnode.getAttributes().getNamedItem("p1")
																	.getTextContent().trim();
															if (Integer.parseInt(tst2) >= ichk) {
																fErrorReturn += "<br>ER_098: Beam with pid=" + tst
																		+ " p1=" + tst2 + " in file: " + file
																		+ " exceeds the number of items in "
																		+ idWithName.get(tst) + ".";
																fxmlErrorReturn += "<Issue  issueID=\"ER_098\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
																fxmlErrorReturn += "Beam with pid=" + tst + " p1="
																		+ tst2 + " in file: " + file
																		+ " exceeds the number of items in "
																		+ idWithName.get(tst) + ".";
																fxmlErrorReturn += "</Issue>";
																errChk("ER_098");
																ierCnt++;
																if (ierCnt > iErrMaxStr) {
																	fErrorReturn += "<br>More than " + iErrMaxStr
																			+ " errors found, stopping..<br>";
																	return;
																}
															}
														}
														if (vnode.getAttributes().getNamedItem("p2") != null) {
															String tst2 = vnode.getAttributes().getNamedItem("p2")
																	.getTextContent().trim();
															if (Integer.parseInt(tst2) >= ichk) {
																fErrorReturn += "<br>ER_099: Beam with pid=" + tst
																		+ " p2=" + tst2 + " in file: " + file
																		+ " exceeds the number of items in "
																		+ idWithName.get(tst) + ".";
																fxmlErrorReturn += "<Issue  issueID=\"ER_099\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
																fxmlErrorReturn += "Beam with pid=" + tst + " p2="
																		+ tst2 + " in file: " + file
																		+ " exceeds the number of items in "
																		+ idWithName.get(tst) + ".";
																fxmlErrorReturn += "</Issue>";
																errChk("ER_099");
																ierCnt++;
																if (ierCnt > iErrMaxStr) {
																	fErrorReturn += "<br>More than " + iErrMaxStr
																			+ " errors found, stopping..<br>";
																	return;
																}
															}
														}
													}
												}

											}
										} // end of beams
									}
									// check if the beamset indexes are less than itrg.
									if (beamLatSubList.item(jjb).getNodeName().endsWith("beamsets")) {
										NodeList bssList = beamLatSubList.item(jjb).getChildNodes();
										for (int jj = 0; jj < bssList.getLength(); jj++) {
											Node vnode = bssList.item(jj);
											if (vnode.getNodeName().endsWith("beamset")) {
												NodeList bsList = vnode.getChildNodes();
												for (int jjr = 0; jjr < bsList.getLength(); jjr++) {
													Node rnode = bsList.item(jjr);
													if (rnode.getNodeName().endsWith("ballref")) {
														String tst = rnode.getAttributes().getNamedItem("index")
																.getTextContent().trim();
														if (Integer.parseInt(tst) >= ballcnt) {
															fErrorReturn += "<br>ER_091: Beamset ballref with index="
																	+ tst
																	+ " is greater than the number of availalbe (zero based) balls: "
																	+ ballcnt + " in file: " + file;

															fxmlErrorReturn += "<Issue  issueID=\"ER_091\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
															fxmlErrorReturn += "Beamset ballref with index=" + tst
																	+ " is greater than the number of availalbe (zero based) balls: "
																	+ ballcnt + " in file: " + file;
															fxmlErrorReturn += "</Issue>";
															errChk("ER_091");
															ierCnt++;
															if (ierCnt > iErrMaxStr) {
																fErrorReturn += "<br>More than " + iErrMaxStr
																		+ " errors found, stopping..<br>";
																return;
															}
														}
													} else if (rnode.getNodeName().endsWith("ref")) {
														String tst = rnode.getAttributes().getNamedItem("index")
																.getTextContent().trim();
														if (Integer.parseInt(tst) >= ibeam) {
															fErrorReturn += "<br>ER_091: Beamset ref with index=" + tst
																	+ " is greater than the number of availalbe (zero based) beams: "
																	+ ibeam + " in file: " + file;

															fxmlErrorReturn += "<Issue  issueID=\"ER_091\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
															fxmlErrorReturn += "Beamset ref with index=" + tst
																	+ " is greater than the number of availalbe (zero based) beams: "
																	+ ibeam + " in file: " + file;
															fxmlErrorReturn += "</Issue>";
															errChk("ER_091");
															ierCnt++;
															if (ierCnt > iErrMaxStr) {
																fErrorReturn += "<br>More than " + iErrMaxStr
																		+ " errors found, stopping..<br>";
																return;
															}
														}
													}
												}
											}
										}
									}
									if (beamLatSubList.item(jjb).getNodeName().endsWith("balls")) {
										for (int jj = 0; jj < vList.getLength(); jj++) {
											Node vnode = vList.item(jj);
											// <b:balls>
											// <b:ball p="1" pid="6" r="6" vindex="0"/>
											String tst;
											if (vnode.getNodeName().endsWith("ball")) {
												ibeam++;
												// Checks if ball has associated vindex value
												if (vnode.getAttributes().getNamedItem("vindex") != null) {
													tst = vnode.getAttributes().getNamedItem("vindex").getTextContent()
															.trim();
													if (Integer.parseInt(tst) >= ivert) {
														fErrorReturn += "<br>ER_090: ball with vindex=" + tst
																+ " is greater than the number of availalbe (zero based) vertices: "
																+ ivert + " in file: " + file;
														fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
														fxmlErrorReturn += "ball with vindex=" + tst
																+ " is greater than the number of availalbe (zero based) vertices: "
																+ ivert + " in file: " + file;
														fxmlErrorReturn += "</Issue>";
														errChk("ER_090");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}

													}
													// check ball vindex points to a vertex mapped to a vertex other
													// than a beam endpoint. (bad_vindex_beam_map)
													if (!vertListBeam.contains(tst)) {
														fErrorReturn += "<br>ER_090: ball with vindex=" + tst
																+ " points to a vertex mapped to a vertex other than a beam endpoint in file: "
																+ file;
														fxmlErrorReturn += "<Issue  issueID=\"ER_090\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
														fxmlErrorReturn += "ball with vindex=" + tst
																+ " points to a vertex mapped to a vertex other than a beam endpoint in file: "
																+ file;
														fxmlErrorReturn += "</Issue>";
														errChk("ER_090");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}

													}
												}
												// Checks if ball has a defined p value
												if (vnode.getAttributes().getNamedItem("p") == null
														&& vnode.getAttributes().getNamedItem("pid") != null) {
													tst = vnode.getAttributes().getNamedItem("pid").getTextContent()
															.trim();
													fWarningReturn += "<br>WR_015: Ball: with pid=" + tst + " in file: "
															+ file + " has not defined p.";
													fxmlWarningReturn += "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"ID warning\">";
													fxmlWarningReturn += "Ball: with pid=" + tst + " in file: " + file
															+ " has not defined p.";
													fxmlWarningReturn += "</Issue>";
													errChk("WR_015");
													iwrCnt++;
													if (iwrCnt > iWrnMaxStr) {
														fWarningReturn += "<br>More than " + iWrnMaxStr
																+ " warnings found, stopping..<br>";
														return;
													}
												}
												if (vnode.getAttributes().getNamedItem("p") != null
														&& vnode.getAttributes().getNamedItem("pid") != null
														&& pidBeamLatChk.equals("") && pindexBeamLatChk.equals("")) {
													fErrorReturn += "<br>ER_089: ball with p="
															+ vnode.getAttributes().getNamedItem("p").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p and pid defined but does not have the pid or pindex defined in the object.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_089\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "ball with p="
															+ vnode.getAttributes().getNamedItem("p").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p and pid defined but does not have the pid or pindex defined in the object.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_089");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}

												}
												// If ball has a defined p and pid, checks that beamlattice has defined
												// p and pid
												else if (vnode.getAttributes().getNamedItem("p") != null
														&& vnode.getAttributes().getNamedItem("pid") != null
														&& (pidBeamLatChk.equals("") || objpidChk.equals(""))) {
													fErrorReturn += "<br>ER_089: ball with p="
															+ vnode.getAttributes().getNamedItem("p").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p and pid defined but does not have the pid defined in both the beamlattice and the object.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_089\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
													fxmlErrorReturn += "ball with p="
															+ vnode.getAttributes().getNamedItem("p").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p and pid defined but does not have the pid defined in both the beamlattice and the object.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_089");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												} else if (vnode.getAttributes().getNamedItem("p") != null
														&& vnode.getAttributes().getNamedItem("pid") != null
														&& pindexBeamLatChk.equals("")) {
													fErrorReturn += "<br>ER_106: ball with p="
															+ vnode.getAttributes().getNamedItem("p").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p and pid defined but does not have the pindex defined in the object.";
													fxmlErrorReturn += "<Issue  issueID=\"ER_106\" severity=\"moderate\" class=\"structure\" type=\"no default material\">";
													fxmlErrorReturn += "ball with p="
															+ vnode.getAttributes().getNamedItem("p").getTextContent()
															+ " and pid="
															+ vnode.getAttributes().getNamedItem("pid").getTextContent()
															+ "  in file: " + file + " with object id: " + objId
															+ " has p and pid defined but does not have the pindex defined in the object.";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_106");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												} else {
												}
												// verifies that ball pid maps to an element with an equal id
												if (vnode.getAttributes().getNamedItem("pid") != null) {
													String pid = vnode.getAttributes().getNamedItem("pid")
															.getTextContent().trim();
													String name = idWithName.get(pid);
													if (name == null) {
														fErrorReturn += "<br>ER_088: ball with pid: " + pid
																+ ", in file: " + file + " does not yet exist.";
														fxmlErrorReturn += "<Issue  issueID=\"ER_088\" severity=\"moderate\" class=\"structure\" type=\"forward reference\">";
														fxmlErrorReturn += "ball with pid: " + pid + ", in file: "
																+ file + " does not yet exist.";
														fxmlErrorReturn += "</Issue>";
														errChk("ER_088");
														ierCnt++;

														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
													// verifies that ball's p value relates to an valid index of the
													// element with proper id
													else {
														idUsed.put(pid, true);
														if (vnode.getAttributes().getNamedItem("p") != null) {
															Integer ichk = idWithCount.get(pid);
															String tst2 = vnode.getAttributes().getNamedItem("p")
																	.getTextContent().trim();
															if (Integer.parseInt(tst2) >= ichk) {
																fErrorReturn += "<br>ER_089: ball with pid: " + pid
																		+ ", in file: " + file + " has a p value of "
																		+ tst2 + " which exceeds the index count of "
																		+ ichk + " for " + name + ".";
																fxmlErrorReturn += "<Issue  issueID=\"ER_089\" severity=\"high\" class=\"structure\" type=\"index out of range\">";
																fxmlErrorReturn += "object with pid: " + pid
																		+ ", in file: " + file + " has a p value of "
																		+ tst2 + " which exceeds the index count of "
																		+ ichk + " for " + name + ".";
																fxmlErrorReturn += "</Issue>";
																errChk("ER_089");
																ierCnt++;
																if (ierCnt > iErrMaxStr) {
																	fErrorReturn += "<br>More than " + iErrMaxStr
																			+ " errors found, stopping..<br>";
																	return;
																}
															}
														}
													}
												}
											}
										} // end of balls
									}
								} // end of nodelist for beamLatSubList as part of cnode, which is beamlattice.

							}
						} catch (Exception et) {
							fErrorReturn += "<br>ER_077: Caught an exception: " + et.getMessage() + "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
							fxmlErrorReturn += "Caught an exception: " + et.getMessage();
							fxmlErrorReturn += "</Issue>";
							errChk("ER_077");
							et.printStackTrace();
							ierCnt++;
						}

					}
				}
				if (node.getNodeName().contains("alternatives")) {
					NodeList childList = node.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node cnode = childList.item(j);
						if (cnode.getNodeName().contains("alternative")) {
							// Gets the UUID and checks if there is another same UUID that has been found
							String altUUID = cnode.getAttributes().getNamedItem("UUID").getTextContent();
							idUuid.add(altUUID);
							fileUuid.add(file);
							// Object IDs are local to the model selected by path (or this model).
							String objectid = cnode.getAttributes().getNamedItem("objectid").getTextContent().trim();
							String fileName = file;
							if (cnode.getAttributes().getNamedItem("path") != null) {
								fileName = cnode.getAttributes().getNamedItem("path").getTextContent();
							}
							String id = "";
							if (node.getParentNode().getAttributes().getNamedItem("id").getTextContent() != null) {
								id = node.getParentNode().getAttributes().getNamedItem("id").getTextContent();
							}
							try (ZipFile zfile = new ZipFile(fCurrentFile)) {
								boolean fndObj = false;
								Enumeration<? extends ZipEntry> entries = zfile.entries();
								while (entries.hasMoreElements()) {
									ZipEntry entry = entries.nextElement();
									if (!fileName.equals("/" + entry.getName())) {
										continue;
									}
									InputStream zin = zfile.getInputStream(entry);
									sContent = IOUtils.toString(zin, "UTF-8");
									if (sContent.trim().toLowerCase().startsWith("<?xml")) {
										DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
										dbFactory.setNamespaceAware(true);// needed to use
																			// getElementsByTagNameNS
										DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
										Document docy = dBuilder.parse(
												new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
										docy.getDocumentElement().normalize();
										NodeList nObjy = docy.getElementsByTagName("object");
										for (int ico = 0; ico < nObjy.getLength(); ico++) {
											if (objectid.contentEquals(nObjy.item(ico).getAttributes()
													.getNamedItem("id").getTextContent())) {
												fndObj = true;
												objIdListRef.add("/" + entry.getName() + "|" + objectid);
												NodeList objectChildren = nObjy.item(ico).getChildNodes();
												for (int k = 0; k < objectChildren.getLength(); k++) {
													if (objectChildren.item(k).getNodeName().contains("alternative")) {
														fErrorReturn += "<br>ER_140: Object " + objectid + " in "
																+ fileName
																+ " specified as an alternative also contains alternatives";
														fxmlErrorReturn += "<Issue  issueID=\"ER_140\" severity=\"high\" class=\"structure\" type=\"ID error\">";
														fxmlErrorReturn += "Object " + objectid + " in " + fileName
																+ " specified as an alternative also contains alternatives";
														fxmlErrorReturn += "</Issue>";
														errChk("ER_140");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
												}
											}
											if (id.contentEquals(nObjy.item(ico).getAttributes().getNamedItem("id")
													.getTextContent().trim()) && !fndObj
													&& file.equals(fileName)) {
												fErrorReturn += "<br>ER_141: Alternative with objectid = " + objectid
														+ " is positioned before the referenced object";
												fxmlErrorReturn += "<Issue  issueID=\"ER_141\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "Alternative with objectid = " + objectid
														+ " is positioned before the referenced object";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_141");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
									}
								}
								if (!fndObj) {
									fErrorReturn += "<br>ER_057: Did not find an object id for item with objectid="
											+ objectid + " in " + file + "<br>";
									fxmlErrorReturn += "<Issue  issueID=\"ER_057\" severity=\"high\" class=\"structure\" type=\"ID error\">";
									fxmlErrorReturn += "Did not find an object id for item with objectid=" + objectid
											+ " in " + file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_057");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}

							} catch (Exception e) {
								fErrorReturn += "<br>ER_077: Caught an exception: " + e.getMessage() + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
								fxmlErrorReturn += "Caught an exception: " + e.getMessage();
								fxmlErrorReturn += "</Issue>";
								errChk("ER_077");
								e.printStackTrace();
								ierCnt++;
							}
						}
					}
				}

				// Check booleanshape nodes
				if (node.getNodeName().contains("booleanshape")) {
					// Get the model node
					String booleanNS = node.getNamespaceURI();
					Node modelNode = node.getParentNode();
					while (!modelNode.getNodeName().equals("model")) {
						modelNode = modelNode.getParentNode();
					}
					for (int j = 0; j < modelNode.getAttributes().getLength(); j++) {
						if (modelNode.getAttributes().item(j).getTextContent().contains(booleanNS)) {
							booleanNS = modelNode.getAttributes().item(j).getNodeName().split(":")[1];
						}
					}
					// Check if boolean namespace is present in requiredextensions
					if (!modelNode.getAttributes().getNamedItem("requiredextensions").getTextContent()
							.contains(booleanNS)) {
						fErrorReturn += "<br>ER_133: booleanshape present without boolean operations as required extension";
						fxmlErrorReturn += "<Issue  issueID=\"ER_133\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "booleanshape present without boolean operations as required extension";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_133");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// Check if pid specified in parent object
					Node parentNode = node.getParentNode();
					String parentID = parentNode.getAttributes().getNamedItem("id").getTextContent();
					if (parentNode.getAttributes().getNamedItem("pid") != null) {
						fErrorReturn += "<br>ER_134: pid specified in booleanshape parent object " + parentID;
						fxmlErrorReturn += "<Issue  issueID=\"ER_134\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "pid specified in booleanshape parent object " + parentID;
						fxmlErrorReturn += "</Issue>";
						errChk("ER_134");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// Check if pindex specified in parent object
					if (parentNode.getAttributes().getNamedItem("pindex") != null) {
						fErrorReturn += "<br>ER_135: pindex specified in booleanshape parent object " + parentID;
						fxmlErrorReturn += "<Issue  issueID=\"ER_135\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "pindex specified in booleanshape parent object " + parentID;
						fxmlErrorReturn += "</Issue>";
						errChk("ER_135");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// Check if path contains prefix
					for (int j = 0; j < node.getAttributes().getLength(); j++) {
						if (node.getAttributes().item(j).getNodeName().contains("path")) {
							if (node.getAttributes().item(j).getNodeName().contains(":")) {
								fErrorReturn += "<br>ER_133: booleanshape path with a prefix in object " + parentID;
								fxmlErrorReturn += "<Issue  issueID=\"ER_133\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
								fxmlErrorReturn += "booleanshape path with a prefix in object " + parentID;
								fxmlErrorReturn += "</Issue>";
								errChk("ER_133");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
					}

					// Check if component in same object as boolean
					NodeList siblingNodes = parentNode.getChildNodes();
					for (int k = 0; k < siblingNodes.getLength(); k++) {
						if (siblingNodes.item(k).getNodeName() == "components") {
							fErrorReturn += "<br>ER_136: Found component and boolean in same object: "
									+ parentNode.getAttributes().getNamedItem("id").getTextContent();
							fxmlErrorReturn += "<Issue  issueID=\"ER_136\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += "Found component and boolean in same object: "
									+ parentNode.getAttributes().getNamedItem("id").getTextContent();
							fxmlErrorReturn += "</Issue>";
							errChk("ER_136");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
					}
					// Node cnode = refObjectChildList.item(j);
					// Check if path is a valid path
					if (node.getAttributes().getNamedItemNS(pns, "path") != null) {
						String booleanshapePathFile = node.getAttributes().getNamedItemNS(pns, "path").getTextContent()
								.trim().replaceAll("\\\\", "/");
						if (entryNames.indexOf(booleanshapePathFile) == -1) {
							fErrorReturn += "<br>ER_136: Could not find file " + booleanshapePathFile
									+ " as specified in 3D/3dmodel.model";
							fxmlErrorReturn += "<Issue  issueID=\"ER_136\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += "Could not find file " + booleanshapePathFile
									+ " as specified in 3D/3dmodel.model";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_136");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						} else {
							bentryNames.set(entryNames.indexOf(booleanshapePathFile), true);
						}
					}
					// Check if the object id references an existing object
					String objectid = node.getAttributes().getNamedItem("objectid").getTextContent().trim();
					String parentObjId = parentNode.getAttributes().getNamedItem("id").getTextContent().trim();
					try (ZipFile zfile = new ZipFile(fCurrentFile)) {
						boolean fndObj = false;
						Enumeration<? extends ZipEntry> entries = zfile.entries();
						while (entries.hasMoreElements()) {
							// entries.
							ZipEntry entry = entries.nextElement();
							InputStream zin = zfile.getInputStream(entry);
							sContent = IOUtils.toString(zin, "UTF-8");
							if (sContent.trim().toLowerCase().startsWith("<?xml")) {
								DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
								dbFactory.setNamespaceAware(true);// needed to use
																	// getElementsByTagNameNS
								DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
								Document docy = dBuilder
										.parse(new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
								docy.getDocumentElement().normalize();
								NodeList nObjy = docy.getElementsByTagName("object");
								boolean forwardReference = false;
								for (int ico = 0; ico < nObjy.getLength(); ico++) {
									// Check if object contains a forward reference
									if (parentObjId.contentEquals(
											nObjy.item(ico).getAttributes().getNamedItem("id").getTextContent())) {
										forwardReference = true;
									}
									if (objectid.contentEquals(
											nObjy.item(ico).getAttributes().getNamedItem("id").getTextContent())) {
										fndObj = true;
										if (forwardReference && file.contains(entry.getName())) {
											fErrorReturn += "<br>ER_143: booleanshape in object " + parentObjId
													+ " contains a forward reference objectid " + objectid + " in "
													+ file;
											fxmlErrorReturn += "<Issue  issueID=\"ER_143\" severity=\"high\" class=\"structure\" type=\"ID error\">";
											fxmlErrorReturn += "booleanshape in object " + parentObjId
													+ " contains a forward reference objectid " + objectid + " in "
													+ file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_143");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										objIdListRef.add("/" + entry.getName() + "|" + objectid);
										// Check if reference object is a model
										String objType = nObjy.item(ico).getAttributes().getNamedItem("type")
												.getTextContent();
										if (!objType.equalsIgnoreCase("model")) {
											fErrorReturn += "<br>ER_137: boolean reference object " + objectid
													+ " is not a model";
											fxmlErrorReturn += "<Issue  issueID=\"ER_137\" severity=\"high\" class=\"structure\" type=\"ID error\">";
											fxmlErrorReturn += "boolean reference object " + objectid
													+ " is not a model" + " in " + file;
											fxmlErrorReturn += "</Issue>";
											errChk("ER_137");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
										// Check if reference object contains forbidden items
										NodeList refObjectChildList = nObjy.item(ico).getChildNodes();
										for (int j = 0; j < refObjectChildList.getLength(); j++) {
											Node cnode = refObjectChildList.item(j);
											// Check if reference object contains components
											if (cnode.getNodeName().contains("components")) {
												fErrorReturn += "<br>ER_138: booleanshape reference object " + objectid
														+ " contains components";
												fxmlErrorReturn += "<Issue  issueID=\"ER_138\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "booleanshape reference object " + objectid
														+ " contains components" + " in " + file;
												fxmlErrorReturn += "</Issue>";
												errChk("ER_138");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
									}
								}
							}
						}
						if (!fndObj) {
							fErrorReturn += "<br>ER_057: Did not find an object id for item with objectid=" + objectid
									+ " in " + file + "<br>";
							fxmlErrorReturn += "<Issue  issueID=\"ER_057\" severity=\"high\" class=\"structure\" type=\"ID error\">";
							fxmlErrorReturn += "Did not find an object id for item with objectid=" + objectid + " in "
									+ file;
							fxmlErrorReturn += "</Issue>";
							errChk("ER_057");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
					} catch (Exception e) {
						fErrorReturn += "<br>ER_077: Caught an exception: " + e.getMessage() + "<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
						fxmlErrorReturn += "Caught an exception: " + e.getMessage();
						fxmlErrorReturn += "</Issue>";
						errChk("ER_077");
						e.printStackTrace();
						ierCnt++;
					}

					// Check boolean nodes
					NodeList childList = node.getChildNodes();
					for (int j = 0; j < childList.getLength(); j++) {
						Node cnode = childList.item(j);
						if (cnode.getNodeName().contains("boolean")) {
							// Check if path is a valid path
							if (cnode.getAttributes().getNamedItemNS(pns, "path") != null) {
								String booleanPathFile = cnode.getAttributes().getNamedItemNS(pns, "path")
										.getTextContent().trim().replaceAll("\\\\", "/");
								if (entryNames.indexOf(booleanPathFile) == -1) {
									fErrorReturn += "<br>ER_136: Could not find file " + booleanPathFile
											+ " as specified in 3D/3dmodel.model";
									fxmlErrorReturn += "<Issue  issueID=\"ER_136\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
									fxmlErrorReturn += "Could not find file " + booleanPathFile
											+ " as specified in 3D/3dmodel.model";
									fxmlErrorReturn += "</Issue>";
									errChk("ER_136");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								} else {
									bentryNames.set(entryNames.indexOf(booleanPathFile), true);
								}
							}
							// Check if path contains prefix
							for (int k = 0; k < cnode.getAttributes().getLength(); k++) {
								if (cnode.getAttributes().item(k).getNodeName().contains("path")) {
									if (cnode.getAttributes().item(k).getNodeName().contains(":")) {
										fErrorReturn += "<br>ER_133: boolean path with a prefix in object " + parentID;
										fxmlErrorReturn += "<Issue  issueID=\"ER_133\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
										fxmlErrorReturn += "boolean path with a prefix in object " + parentID;
										fxmlErrorReturn += "</Issue>";
										errChk("ER_133");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
							}

							// Check if the objectid references an existing object
							objectid = cnode.getAttributes().getNamedItem("objectid").getTextContent().trim();
							String booleanID = parentNode.getAttributes().getNamedItem("id").getTextContent();
							boolean forwardReference = false;
							try (ZipFile zfile = new ZipFile(fCurrentFile)) {
								boolean fndObj = false;
								Enumeration<? extends ZipEntry> entries = zfile.entries();
								while (entries.hasMoreElements()) {
									ZipEntry entry = entries.nextElement();
									InputStream zin = zfile.getInputStream(entry);
									sContent = IOUtils.toString(zin, "UTF-8");
									if (sContent.trim().toLowerCase().startsWith("<?xml")) {
										DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
										dbFactory.setNamespaceAware(true);// needed to use
																			// getElementsByTagNameNS
										DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
										Document docy = dBuilder.parse(
												new InputSource(new ByteArrayInputStream(sContent.getBytes("utf-8"))));
										docy.getDocumentElement().normalize();
										NodeList nObjy = docy.getElementsByTagName("object");
										for (int ico = 0; ico < nObjy.getLength(); ico++) {
											// If the boolean object is found before the reference object and the
											// boolean object has no path, reference object would be a forward reference
											if (parentObjId.contentEquals(nObjy.item(ico).getAttributes()
													.getNamedItem("id").getTextContent())) {
												forwardReference = true;
											}
											if (objectid.contentEquals(nObjy.item(ico).getAttributes()
													.getNamedItem("id").getTextContent())) {
												fndObj = true;
												if (forwardReference && file.contains(entry.getName())) {
													fErrorReturn += "<br>ER_143: boolean in object " + booleanID
															+ " contains a forward refrence objectid " + objectid
															+ " in " + file;
													fxmlErrorReturn += "<Issue  issueID=\"ER_143\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "boolean in object " + booleanID
															+ " contains a forward refrence objectid " + objectid
															+ " in " + file;
													fxmlErrorReturn += "</Issue>";
													errChk("ER_143");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												objIdListRef.add("/" + entry.getName() + "|" + objectid);

												// Check if reference object contains forbidden items
												NodeList refObjectChildList = nObjy.item(ico).getChildNodes();
												for (int k = 0; k < refObjectChildList.getLength(); k++) {
													Node currentNode = refObjectChildList.item(k);
													// Check if reference object contains components
													if (currentNode.getNodeName().contains("components")) {
														fErrorReturn += "<br>ER_138: boolean reference object "
																+ objectid + " contains components";
														fxmlErrorReturn += "<Issue  issueID=\"ER_138\" severity=\"high\" class=\"structure\" type=\"ID error\">";
														fxmlErrorReturn += "boolean reference object " + objectid
																+ " contains components" + " in " + file;
														fxmlErrorReturn += "</Issue>";
														errChk("ER_138");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
													// Check if reference object contains booleans
													if (currentNode.getNodeName().contains("booleanshape")) {
														fErrorReturn += "<br>ER_139: boolean reference object "
																+ objectid + " contains booleans";
														fxmlErrorReturn += "<Issue  issueID=\"ER_139\" severity=\"high\" class=\"structure\" type=\"ID error\">";
														fxmlErrorReturn += "boolean reference object " + objectid
																+ " contains booleans" + " in " + file;
														fxmlErrorReturn += "</Issue>";
														errChk("ER_139");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
													// Check if reference object contains displacements
													if (currentNode.getNodeName().contains("displacement")) {
														fErrorReturn += "<br>ER_176: boolean reference object "
																+ objectid + " contains displacements";
														fxmlErrorReturn += "<Issue  issueID=\"ER_176\" severity=\"high\" class=\"structure\" type=\"ID error\">";
														fxmlErrorReturn += "boolean reference object " + objectid
																+ " contains displacements" + " in " + file;
														fxmlErrorReturn += "</Issue>";
														errChk("ER_176");
														ierCnt++;
														if (ierCnt > iErrMaxStr) {
															fErrorReturn += "<br>More than " + iErrMaxStr
																	+ " errors found, stopping..<br>";
															return;
														}
													}
												}
											}
										}
									}
								}
								if (!fndObj) {
									fErrorReturn += "<br>ER_057: Did not find an object id for item with objectid="
											+ objectid + " in " + file + "<br>";
									fxmlErrorReturn += "<Issue  issueID=\"ER_057\" severity=\"high\" class=\"structure\" type=\"ID error\">";
									fxmlErrorReturn += "Did not find an object id for item with objectid=" + objectid
											+ " in " + file;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_057");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
							} catch (Exception e) {
								fErrorReturn += "<br>ER_077: Caught an exception: " + e.getMessage() + "<br>";
								fxmlErrorReturn += "<Issue  issueID=\"ER_077\" severity=\"moderate\" class=\"structure\" type=\"unexpected exception\">";
								fxmlErrorReturn += "Caught an exception: " + e.getMessage();
								fxmlErrorReturn += "</Issue>";
								errChk("ER_077");
								e.printStackTrace();
								ierCnt++;
							}
						}
					}
				}

				// Check normvectornodes
				if (node.getNodeName().contains("normvectorgroup")) {
					NodeList childList = node.getChildNodes();
					String normVectorID = node.getAttributes().getNamedItem("id").getTextContent();
					int vectorCount = 0;
					for (int j = 0; j < childList.getLength(); j++) {
						if (childList.item(j).getNodeName().contains("normvector")) {
							double module;
							double x = Double
									.parseDouble(childList.item(j).getAttributes().getNamedItem("x").getTextContent());
							double y = Double
									.parseDouble(childList.item(j).getAttributes().getNamedItem("y").getTextContent());
							double z = Double
									.parseDouble(childList.item(j).getAttributes().getNamedItem("z").getTextContent());
							// Add data to table
							normVectorGroupTable.addRow(new Object[] { normVectorID, vectorCount, x, y, z });
							vectorCount++;
							module = Math.sqrt((x * x) + (y * y) + (z * z));
							if (module < .999999 || module > 1.000001) {
								fWarningReturn += "<br>WR_023: X, Y, Z normvector whose module is above or below one (module = " + module + ")";
								fxmlWarningReturn += "<Issue  issueID=\"WR_023\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
								fxmlWarningReturn += "X, Y, Z normvector whose module is above or below one (module = " + module + ")";
								fxmlWarningReturn += "</Issue>";
								errChk("WR_023");
								iwrCnt++;
								if (iwrCnt > iWrnMaxStr) {
									fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
									return;
								}
							}
						}
					}

				}
				// Check displacementmesh nodes
				if (node.getNodeName().contains("displacementmesh")) {
					// Check if node is in a model
					Node objectNode = node.getParentNode();
					while (!objectNode.getNodeName().contains("object")) {
						objectNode = objectNode.getParentNode();
					}
					String objectId = objectNode.getAttributes().getNamedItem("id").getTextContent();

					// Get the model node
					Node modelNode = node.getParentNode();
					while (!modelNode.getNodeName().equals("model")) {
						modelNode = modelNode.getParentNode();
					}
					// Get the characters for the displacement namespace
					String displacementNS = "";
					for (int ico = 0; ico < modelNode.getAttributes().getLength(); ico++) {
						if (modelNode.getAttributes().item(ico).getTextContent().contains("displacement")) {
							displacementNS = modelNode.getAttributes().item(ico).getNodeName();
							displacementNS = displacementNS.split(":")[1];
						}
					}
					// Check if displacement namespace is present in requiredextensions
					if (modelNode.getAttributes().getNamedItem("requiredextensions") != null) {
						if (!modelNode.getAttributes().getNamedItem("requiredextensions").getTextContent()
								.contains(displacementNS)) {
							fErrorReturn += "<br>ER_144: displacement present without displacements as required extension";
							fxmlErrorReturn += "<Issue  issueID=\"ER_144\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
							fxmlErrorReturn += "displacement present without displacements as required extension";
							fxmlErrorReturn += "</Issue>";
							errChk("ER_144");
							ierCnt++;
							if (ierCnt > iErrMaxStr) {
								fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
								return;
							}
						}
					} else {
						fErrorReturn += "<br>ER_144: displacement present without displacements as required extension";
						fxmlErrorReturn += "<Issue  issueID=\"ER_144\" severity=\"high\" class=\"structure\" type=\"invalid relationship\">";
						fxmlErrorReturn += "displacement present without displacements as required extension";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_144");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					displacementNS = displacementNS + ":";
					// Check if object containing displacementmesh has the type 'model'
					if ("model".compareTo(objectNode.getAttributes().getNamedItem("type").getTextContent()) != 0) {
						fErrorReturn += "<br>ER_145: object "
								+ objectNode.getAttributes().getNamedItem("id").getTextContent()
								+ " containing displacmentmesh has a type other than model";
						fxmlErrorReturn += "<Issue  issueID=\"ER_145\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "object " + objectNode.getAttributes().getNamedItem("id").getTextContent()
								+ " containing displacmentmesh has a type other than model";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_145");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}

					NodeList childList = node.getChildNodes();
					int vertexCount = 0;
					for (int j = 0; j < childList.getLength(); j++) {
						if (childList.item(j).getNodeName().contains("vertices")) {
							// Check if vertices contains the extension namespace prefix
							if (!childList.item(j).getNodeName().contains(displacementNS)) {
								fErrorReturn += "<br>ER_146: displacementmesh vertices fails to use the displacement extension";
								fxmlErrorReturn += "<Issue  issueID=\"ER_146\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "displacementmesh vertices fails to use the displacement extension";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_146");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
							NodeList verticesList = childList.item(j).getChildNodes();
							for (int n = 0; n < verticesList.getLength(); n++) {
								if (verticesList.item(n).getNodeName().contains("vertex")) {
									verticesTable.addRow(new Object[] { objectId, vertexCount,
											Double.parseDouble(verticesList.item(n).getAttributes().getNamedItem("x")
													.getTextContent()),
											Double.parseDouble(verticesList.item(n).getAttributes().getNamedItem("y")
													.getTextContent()),
											Double.parseDouble(verticesList.item(n).getAttributes().getNamedItem("z")
													.getTextContent()) });
									vertexCount++;
									// Check if vertex contains the extension namespace prefix
									if (!verticesList.item(n).getNodeName().contains(displacementNS)) {
										fErrorReturn += "<br>ER_147: displacementmesh vertex fails to use the displacement extension";
										fxmlErrorReturn += "<Issue  issueID=\"ER_147\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "displacementmesh vertex fails to use the displacement extension";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_147");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
								}
							}
						}
						if (childList.item(j).getNodeName().contains("triangles")) {
							// Check if node contains displacement ns
							if (!childList.item(j).getNodeName().contains(displacementNS)) {
								fErrorReturn += "<br>ER_148: displacementmesh triangles fails to use the displacement extension";
								fxmlErrorReturn += "<Issue  issueID=\"ER_148\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "displacementmesh triangles fails to use the displacement extension";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_148");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
							Node disp2dNode = null;
							String trianglesDid = "";
							int triangleCount = 0;
							if (childList.item(j).getAttributes().getNamedItem("did") != null) {
								trianglesDid = childList.item(j).getAttributes().getNamedItem("did").getTextContent();
								for (int k = 0; k < nodeList.getLength(); k++) {
									if (nodeList.item(k).getNodeName().contains("disp2dgroup")) {
										if (trianglesDid.compareTo(nodeList.item(k).getAttributes().getNamedItem("id")
												.getTextContent()) == 0) {
											disp2dNode = nodeList.item(k);
										}
									}
								}
								// Check if the 'did' points to a valid resource
								if (disp2dNode == null) {
									fErrorReturn += "<br>ER_149: triangles with did " + trianglesDid
											+ " points to a non-existent disp2dgroup resource";
									fxmlErrorReturn += "<Issue  issueID=\"ER_149\" severity=\"high\" class=\"structure\" type=\"ID error\">";
									fxmlErrorReturn += "triangles with did " + trianglesDid
											+ " points to a non-existent disp2dgroup resource";
									fxmlErrorReturn += "</Issue>";
									errChk("ER_149");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
							}
							NodeList trianglesChildList = childList.item(j).getChildNodes();
							for (int k = 0; k < trianglesChildList.getLength(); k++) {
								Node currentNode = trianglesChildList.item(k);
								String triangleDid = "";
								if (currentNode.getNodeName().contains("triangle")) {
									if (!currentNode.getNodeName().contains(displacementNS)) {
										fErrorReturn += "<br>ER_150: displacementmesh triangle fails to use the displacement extension";
										fxmlErrorReturn += "<Issue  issueID=\"ER_150\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "displacementmesh triangle fails to use the displacement extension";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_150");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Check if triangle specifies a did, and if so get the group for that did
									if (currentNode.getAttributes().getNamedItemNS(dns, "did") != null) {
										triangleDid = currentNode.getAttributes().getNamedItemNS(dns, "did").getTextContent();
									} else if(currentNode.getAttributes().getNamedItem("did") != null) {
										triangleDid = currentNode.getAttributes().getNamedItem("did").getTextContent();
									}
									if(triangleDid.compareTo("") != 0) {
										disp2dNode = null;
										for (int m = 0; m < nodeList.getLength(); m++) {
											if (nodeList.item(m).getNodeName().contains("disp2dgroup")) {
												if (triangleDid.compareTo(nodeList.item(m).getAttributes()
														.getNamedItem("id").getTextContent()) == 0) {
													disp2dNode = nodeList.item(m);
												}
											}
										}
										if (disp2dNode == null) {
											fErrorReturn += "<br>ER_151: triangle with did " + triangleDid
													+ " points to a non-existent disp2dgroup resource";
											fxmlErrorReturn += "<Issue  issueID=\"ER_151\" severity=\"high\" class=\"structure\" type=\"ID error\">";
											fxmlErrorReturn += "triangle with did " + triangleDid
													+ " points to a non-existent disp2dgroup resource";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_151");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										}
									} else {
										triangleDid = trianglesDid;
									}
									// Checks if triangle has d1 but no 'did' in either triangle or triangles
									if (trianglesDid.isEmpty() && triangleDid.isEmpty()
											&& (currentNode.getAttributes().getNamedItem("d1") != null || currentNode.getAttributes().getNamedItemNS(dns, "d1") != null)) {
										fErrorReturn += "<br>ER_152: triangle with 'd1' specified, but no 'did' in triangle or in triangles.";
										fxmlErrorReturn += "<Issue  issueID=\"ER_152\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "triangle with 'd1' specified, but no 'did' in triangle nor in triangles.";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_152");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Check if vertices are unique
									String v1 = currentNode.getAttributes().getNamedItem("v1").getTextContent();
									String v2 = currentNode.getAttributes().getNamedItem("v2").getTextContent();
									String v3 = currentNode.getAttributes().getNamedItem("v3").getTextContent();
									if (v1.compareTo(v2) == 0 || v1.compareTo(v3) == 0 || v2.compareTo(v3) == 0) {
										fErrorReturn += "<br>ER_153: triangle indices with vertex references that are not unique";
										fxmlErrorReturn += "<Issue  issueID=\"ER_153\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "triangle indices with vertex references that are not unique";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_153");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Check if v1 is in range of the vertex count
									if (Integer.parseInt(v1) >= vertexCount) {
										fErrorReturn += "<br>ER_154: triangle v1 attribute points to a vertex index that is out of range";
										fxmlErrorReturn += "<Issue  issueID=\"ER_154\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "triangle v1 attribute points to a vertex index that is out of range";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_154");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Check if v2 is in range of the vertex count
									if (Integer.parseInt(v2) >= vertexCount) {
										fErrorReturn += "<br>ER_155: triangle v2 attribute points to a vertex index that is out of range";
										fxmlErrorReturn += "<Issue  issueID=\"ER_155\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "triangle v2 attribute points to a vertex index that is out of range";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_155");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Check if v3 is in range of the vertex count
									if (Integer.parseInt(v3) >= vertexCount) {
										fErrorReturn += "<br>ER_156: triangle v3 attribute points to a vertex index that is out of range";
										fxmlErrorReturn += "<Issue  issueID=\"ER_156\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "triangle v3 attribute points to a vertex index that is out of range";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_156");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									// Check if pid points to a valid resource
									if (currentNode.getAttributes().getNamedItem("pid") != null) {
										Node pidNode = null;
										String pid = currentNode.getAttributes().getNamedItem("pid").getTextContent();
										System.out.println(pid);
										int nodeCount = 0;
										for (int m = 0; m < nodeList.getLength(); m++) {
											if (nodeList.item(m).getAttributes().getNamedItem("id") != null) {
												if (pid.contentEquals(nodeList.item(m).getAttributes()
														.getNamedItem("id").getTextContent())) {
													pidNode = nodeList.item(m);
													System.out.println("Found pid refrence object: " + pidNode
															.getAttributes().getNamedItem("id").getTextContent());
													for (int n = 0; n < pidNode.getChildNodes().getLength(); n++) {
														if (!pidNode.getChildNodes().item(n).getNodeName()
																.contains("#")) {
															nodeCount++;
														}
													}
													System.out.println(nodeCount);
												}
											}
										}
										if (pidNode == null) {
											fErrorReturn += "<br>ER_157: triange with pid attribute " + pid
													+ " points to a non-existent resource";
											fxmlErrorReturn += "<Issue  issueID=\"ER_157\" severity=\"high\" class=\"structure\" type=\"ID error\">";
											fxmlErrorReturn += "triange with pid attribute " + pid
													+ " points to a non-existent resource";
											fxmlErrorReturn += "</Issue>";
											errChk("ER_157");
											ierCnt++;
											if (ierCnt > iErrMaxStr) {
												fErrorReturn += "<br>More than " + iErrMaxStr
														+ " errors found, stopping..<br>";
												return;
											}
										} else {
											// check that p1 is a resource index that is in range
											if (currentNode.getAttributes().getNamedItem("p1") != null) {
												if (Integer.parseInt(currentNode.getAttributes().getNamedItem("p1")
														.getTextContent()) >= nodeCount) {
													fErrorReturn += "<br>ER_158: triangle p1 attribute points to a resource index that is out of range";
													fxmlErrorReturn += "<Issue  issueID=\"ER_158\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "triangle p1 attribute points to a resource index that is out of range";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_158");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
											}
											// check that p2 is a resource index that is in range
											if (currentNode.getAttributes().getNamedItem("p2") != null) {
												if (Integer.parseInt(currentNode.getAttributes().getNamedItem("p2")
														.getTextContent()) >= nodeCount) {
													fErrorReturn += "<br>ER_159: triangle p2 attribute points to a resource index that is out of range";
													fxmlErrorReturn += "<Issue  issueID=\"ER_159\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "triangle p2 attribute points to a resource index that is out of range";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_159");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (currentNode.getAttributes().getNamedItem("p1") == null) {
													fErrorReturn += "<br>ER_160: triangle with p2 but not p1";
													fxmlErrorReturn += "<Issue  issueID=\"ER_160\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "triangle with p2 but not p1";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_160");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
											}
											// check that p3 is a resource index that is in range
											if (currentNode.getAttributes().getNamedItem("p3") != null) {
												if (Integer.parseInt(currentNode.getAttributes().getNamedItem("p3")
														.getTextContent()) >= nodeCount) {
													fErrorReturn += "<br>ER_161: triangle p3 attribute points to a resource index that is out of range";
													fxmlErrorReturn += "<Issue  issueID=\"ER_161\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "triangle p3 attribute points to a resource index that is out of range";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_161");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
												if (currentNode.getAttributes().getNamedItem("p1") == null) {
													fErrorReturn += "<br>ER_162: triangle with p3 but not p1";
													fxmlErrorReturn += "<Issue  issueID=\"ER_162\" severity=\"high\" class=\"structure\" type=\"ID error\">";
													fxmlErrorReturn += "triangle with p3 but not p1";
													fxmlErrorReturn += "</Issue>";
													errChk("ER_162");
													ierCnt++;
													if (ierCnt > iErrMaxStr) {
														fErrorReturn += "<br>More than " + iErrMaxStr
																+ " errors found, stopping..<br>";
														return;
													}
												}
											}
										}
									} else if (objectNode.getAttributes().getNamedItem("pid") == null
											&& currentNode.getAttributes().getNamedItem("p1") != null) {
										fErrorReturn += "<br>ER_163: triangle with \"p1\" specified, but no \"pid\" in triangle nor in object";
										fxmlErrorReturn += "<Issue  issueID=\"ER_163\" severity=\"high\" class=\"structure\" type=\"ID error\">";
										fxmlErrorReturn += "triangle with \"p1\" specified, but no \"pid\" in triangle nor in object";
										fxmlErrorReturn += "</Issue>";
										errChk("ER_163");
										ierCnt++;
										if (ierCnt > iErrMaxStr) {
											fErrorReturn += "<br>More than " + iErrMaxStr
													+ " errors found, stopping..<br>";
											return;
										}
									}
									if (disp2dNode != null) {
										// Get the number of elements in the disp2d group
										NodeList disp2dNodeChildList = disp2dNode.getChildNodes();
										int dispCordCount = 0;
										for (int m = 0; m < disp2dNodeChildList.getLength(); m++) {
											if (disp2dNodeChildList.item(m).getNodeName().contains("disp2dcoord")) {
												dispCordCount++;
											}
										}
										// check if d1 points to a valid coordinate
										if (currentNode.getAttributes().getNamedItem("d1") != null) {
											if (dispCordCount < Integer.parseInt(
													currentNode.getAttributes().getNamedItem("d1").getTextContent())) {
												fErrorReturn += "<br>ER_164: triangle d1 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "<Issue  issueID=\"ER_164\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d1 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_164");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										} else if(currentNode.getAttributes().getNamedItemNS(dns, "d1") != null) {
											if (dispCordCount < Integer.parseInt(
													currentNode.getAttributes().getNamedItemNS(dns, "d1").getTextContent())) {
												fErrorReturn += "<br>ER_164: triangle d1 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "<Issue  issueID=\"ER_164\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d1 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_164");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
										//Checks for d2 without namespace prefix
										if (currentNode.getAttributes().getNamedItem("d2") != null) {
											// Check if d1 is specified
											if (currentNode.getAttributes().getNamedItem("d1") == null
													&& currentNode.getAttributes().getNamedItemNS(dns, "d1") == null) {
												fErrorReturn += "<br>ER_165: triangle with d2 specified, but not d1";
												fxmlErrorReturn += "<Issue  issueID=\"ER_165\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d1 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_165");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
											// check if d2 points to a valid coordinate
											if (dispCordCount < Integer.parseInt(
													currentNode.getAttributes().getNamedItem("d2").getTextContent())) {
												fErrorReturn += "<br>ER_166: triangle d2 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "<Issue  issueID=\"ER_166\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d2 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_166");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
										//Checks for d2 with namespace prefix
										if (currentNode.getAttributes().getNamedItemNS(dns, "d2") != null) {
											// Check if d1 is specified
											if (currentNode.getAttributes().getNamedItem("d1") == null
													&& currentNode.getAttributes().getNamedItemNS(dns, "d1") == null) {
												fErrorReturn += "<br>ER_165: triangle with d2 specified, but not d1";
												fxmlErrorReturn += "<Issue  issueID=\"ER_165\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d1 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_165");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
											// check if d2 points to a valid coordinate
											if (dispCordCount < Integer.parseInt(
													currentNode.getAttributes().getNamedItemNS(dns, "d2").getTextContent())) {
												fErrorReturn += "<br>ER_166: triangle d2 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "<Issue  issueID=\"ER_166\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d2 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_166");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
										//Checks for d3 without namespace prefix
										if (currentNode.getAttributes().getNamedItem("d3") != null) {
											// Check if d1 is specified
											if (currentNode.getAttributes().getNamedItem("d1") == null
													&& currentNode.getAttributes().getNamedItemNS(dns, "d1") == null) {
												fErrorReturn += "<br>ER_167: triangle with d3 specified, but not d1";
												fxmlErrorReturn += "<Issue  issueID=\"ER_167\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d3 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_167");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
											// check if d3 points to a valid coordinate
											if (dispCordCount < Integer.parseInt(
													currentNode.getAttributes().getNamedItem("d3").getTextContent())) {
												fErrorReturn += "<br>ER_168: triangle d3 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "<Issue  issueID=\"ER_168\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d3 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_168");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
										//Checks for d3 with namespace prefix
										if (currentNode.getAttributes().getNamedItemNS(dns, "d3") != null) {
											// Check if d1 is specified
											if (currentNode.getAttributes().getNamedItem("d1") == null
													&& currentNode.getAttributes().getNamedItemNS(dns, "d1") == null) {
												fErrorReturn += "<br>ER_167: triangle with d3 specified, but not d1";
												fxmlErrorReturn += "<Issue  issueID=\"ER_167\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d3 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_167");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
											// check if d3 points to a valid coordinate
											if (dispCordCount < Integer.parseInt(
													currentNode.getAttributes().getNamedItemNS(dns, "d3").getTextContent())) {
												fErrorReturn += "<br>ER_168: triangle d3 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "<Issue  issueID=\"ER_168\" severity=\"high\" class=\"structure\" type=\"ID error\">";
												fxmlErrorReturn += "triangle d3 attribute points to a displacement map index that is out of range";
												fxmlErrorReturn += "</Issue>";
												errChk("ER_168");
												ierCnt++;
												if (ierCnt > iErrMaxStr) {
													fErrorReturn += "<br>More than " + iErrMaxStr
															+ " errors found, stopping..<br>";
													return;
												}
											}
										}
									}
									if (!triangleDid.isEmpty()
											&& (currentNode.getAttributes().getNamedItem("d1") != null || currentNode.getAttributes().getNamedItemNS(dns, "d1") != null)) {
										String d1;
										if (currentNode.getAttributes().getNamedItem("d1") != null) {
											d1 = currentNode.getAttributes().getNamedItem("d1").getTextContent();
										} else {
											d1 = currentNode.getAttributes().getNamedItemNS(dns, "d1").getTextContent();
										}
										String d2;
										String d3;
										if (currentNode.getAttributes().getNamedItem("d2") != null) {
											d2 = currentNode.getAttributes().getNamedItem("d2").getTextContent();
										} else if (currentNode.getAttributes().getNamedItemNS(dns, "d2") != null) {
											d2 = currentNode.getAttributes().getNamedItemNS(dns, "d2").getTextContent();
										} else {
											d2 = d1;
										}
										if (currentNode.getAttributes().getNamedItem("d3") != null) {
											d3 = currentNode.getAttributes().getNamedItem("d3").getTextContent();
										} else if (currentNode.getAttributes().getNamedItemNS(dns, "d3") != null) {
											d3 = currentNode.getAttributes().getNamedItemNS(dns, "d3").getTextContent();
										} else {
											d3 = d1;
										}
										triangleTable.addRow(new Object[] { objectId, triangleDid, triangleCount, d1,
												d2, d3, v1, v2, v3 });
									}

									triangleCount++;
								}
							}
							// Check if there are more than three triangles
							if (triangleCount < 4 && triangleCount > 0) {
								fErrorReturn += "<br>ER_169: Invalid mesh object with less than 4 triangles";
								fxmlErrorReturn += "<Issue  issueID=\"ER_169\" severity=\"high\" class=\"structure\" type=\"ID error\">";
								fxmlErrorReturn += "Invalid mesh object with only 3 triangles";
								fxmlErrorReturn += "</Issue>";
								errChk("ER_169");
								ierCnt++;
								if (ierCnt > iErrMaxStr) {
									fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
									return;
								}
							}
						}
					}
				}

				// Check displacement2d nodes
				if (node.getNodeName().contains("disp2dgroup")) {
					String dispId = node.getAttributes().getNamedItem("dispid").getTextContent().trim();
					String nId = node.getAttributes().getNamedItem("nid").getTextContent().trim();
					String id = node.getAttributes().getNamedItem("id").getTextContent().trim();
					Node disp2dNode = null;
					Node normVectorGroupNode = null;
					boolean forwardD2N = false;
					boolean forwardNVGN = false;
					for (int m = 0; m < nodeList.getLength(); m++) {
						// Finds the object the 'dispid' points to
						if (nodeList.item(m).getNodeName().contains("displacement2d")) {
							if (dispId.compareTo(
									nodeList.item(m).getAttributes().getNamedItem("id").getTextContent()) == 0) {
								disp2dNode = nodeList.item(m);
							}
						}
						// Finds the object the 'nid' points to
						if (nodeList.item(m).getNodeName().contains("normvectorgroup")) {
							if (nId.compareTo(
									nodeList.item(m).getAttributes().getNamedItem("id").getTextContent()) == 0) {
								normVectorGroupNode = nodeList.item(m);
							}
						}
						// Checks if the node is found before the referenced nodes
						if (nodeList.item(m).isSameNode(node)) {
							if (disp2dNode == null) {
								forwardD2N = true;
							}

							if (normVectorGroupNode == null) {
								forwardNVGN = true;
							}
						}
					}
					// Checks if the 'dispid' points to a valid object
					if (disp2dNode == null) {
						fErrorReturn += "<br>ER_170: disp2dgroup "
								+ node.getAttributes().getNamedItem("dispid").getTextContent()
								+ " has an invalid dispid attribute pointing to a non-existent resource in " + file
								+ "<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_170\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "disp2dgroup " + node.getAttributes().getNamedItem("dispid").getTextContent()
								+ " has an invalid dispid attribute pointing to a non-existent resource in " + file
								+ "<br>";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_170");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					} else if (forwardD2N) {
						fErrorReturn += "<br>ER_171: Reference in disp2dgroup with dispid " + dispId
								+ " attribute before it is defined";
						fxmlErrorReturn += "<Issue  issueID=\"ER_171\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Reference in disp2dgroup with dispid " + dispId
								+ " attribute before it is defined";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_171");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// Checks if the 'nid' points to a valid object
					if (normVectorGroupNode == null) {
						fErrorReturn += "<br>ER_172: disp2dgroup "
								+ node.getAttributes().getNamedItem("dispid").getTextContent()
								+ " has an invalid nid attribute pointing to a non-existent resource in " + file
								+ "<br>";
						fxmlErrorReturn += "<Issue  issueID=\"ER_172\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "disp2dgroup " + node.getAttributes().getNamedItem("dispid").getTextContent()
								+ " has an invalid nid attribute pointing to a non-existent resource in " + file
								+ "<br>";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_172");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					} else if (forwardNVGN) {
						fErrorReturn += "<br>ER_173: Reference in disp2dgroup with nid " + nId
								+ " attribute before it is defined";
						fxmlErrorReturn += "<Issue  issueID=\"ER_173\" severity=\"high\" class=\"structure\" type=\"ID error\">";
						fxmlErrorReturn += "Reference in disp2dgroup with nid " + nId
								+ " attribute before it is defined";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_173");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// Checks if the disp2dcoords have valid 'n' attributes
					if (normVectorGroupNode != null) {
						NodeList childList = node.getChildNodes();
						int dispCordCount = 0;
						for (int j = 0; j < childList.getLength(); j++) {
							if (childList.item(j).getNodeName().contains("disp2dcoord")) {
								String nValue = childList.item(j).getAttributes().getNamedItem("n").getTextContent();
								disp2dGroupTable.addRow(new Object[] { id, dispCordCount, nId, nValue });
								dispCordCount++;
								NodeList nvgChildList = normVectorGroupNode.getChildNodes();
								int vectorCount = 0;
								for (int k = 0; k < nvgChildList.getLength(); k++) {
									if (nvgChildList.item(k).getNodeName().contains("normvector")) {
										vectorCount++;
									}
								}
								if (vectorCount <= Integer.parseInt(nValue)) {
									fErrorReturn += "<br>ER_174: A disp2dcoord in disp2dgroup "
											+ node.getAttributes().getNamedItem("dispid").getTextContent()
											+ " has an n attribute " + nValue
											+ " outside the valid range for normvectors in normvectorgroup " + nId;
									fxmlErrorReturn += "<Issue  issueID=\"ER_174\" severity=\"high\" class=\"structure\" type=\"ID error\">";
									fxmlErrorReturn += "A disp2dcoord in disp2dgroup "
											+ node.getAttributes().getNamedItem("dispid").getTextContent()
											+ " has an n attribute " + nValue
											+ " outside the valid range for normvectors in normvectorgroup " + nId;
									fxmlErrorReturn += "</Issue>";
									errChk("ER_174");
									ierCnt++;
									if (ierCnt > iErrMaxStr) {
										fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
										return;
									}
								}
							}
						}
					}
				}
			}

			// check resources that basematerials is first and object last.
			if (node.getNodeName().equals("resources")) {
				NodeList childList = node.getChildNodes();
				Boolean startBase = false;
				Boolean endBase = false;
				Boolean endObject = false;
				Boolean doOnceObj = false;

				for (int j = 0; j < childList.getLength(); j++) {
					Node cnode = childList.item(j);
					String name = cnode.getNodeName();
					if (name.equals("#text") || name.equals("#comment"))
						continue;
					// there are base materials here
					if (name.equals("basematerials"))
						startBase = true;

					// check if we have set endBase by finding something else
					// and then found basematerials again.
					if (name.equals("basematerials") && endBase) {
						fErrorReturn += "<br>ER_102: basematerials appeared again after other resources were defined "
								+ " in file: " + file + ". The basematerials should appear first in resources.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_102\" severity=\"high\" class=\"structure\" type=\"resource ordering\">";
						fxmlErrorReturn += "basematerials appeared again after other resources were defined "
								+ " in file: " + file + ". The basematerials should appear first in resources.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_102");
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// Something other than basematerials after a basematerials
					// was found, set endBase.
					if (!name.equals("basematerials") && !endBase && startBase) {
						endBase = true;
					}

					// if we have set endObject, but find something else, that
					// is bad
					if (!name.equals("object") && endObject && !doOnceObj) {
						fErrorReturn += "<br>ER_103: Other resources appeared after object was defined " + " in file: "
								+ file + ". The object elements should appear last in resources.";
						fxmlErrorReturn += "<Issue  issueID=\"ER_103\" severity=\"high\" class=\"structure\" type=\"resource ordering\">";
						fxmlErrorReturn += "ther resources appeared after object was defined " + " in file: " + file
								+ ". The object elements should appear last in resources.";
						fxmlErrorReturn += "</Issue>";
						errChk("ER_103");
						doOnceObj = true;
						ierCnt++;
						if (ierCnt > iErrMaxStr) {
							fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
							return;
						}
					}
					// have we found object yet
					if (name.equals("object") && !endObject) {
						endObject = true;
					}

				}

			}

		}
		// Normalize vertices
//		Double maxX = 0.0;
//		Double maxY = 0.0;
//		Double maxZ = 0.0;
//		int firstObjIndex = 0;
//		for (int i = 0; i < verticesTable.getRowCount(); i++) {
//			if (Integer.parseInt(verticesTable.getValueAt(i, 1).toString()) != 0) {
//				Double currentX = (Double) verticesTable.getValueAt(i, 2);
//				Double currentY = (Double) verticesTable.getValueAt(i, 3);
//				Double currentZ = (Double) verticesTable.getValueAt(i, 4);
//				if (currentX > maxX) {
//					maxX = currentX;
//					System.out.println("Max x: " + maxX);
//				}
//				if (currentY > maxY) {
//					maxY = currentY;
//					System.out.println("Max y: " + maxY);
//				}
//				if (currentZ > maxZ) {
//					maxZ = currentZ;
//					System.out.println("Max z: " + maxZ);
//				}
//			} else {
//				System.out.println("Got to else");
//				for (int j = firstObjIndex; j < i; j++) {
//					verticesTable.setValueAt((Double) verticesTable.getValueAt(j, 2) / maxX, j, 2);
//					verticesTable.setValueAt((Double) verticesTable.getValueAt(j, 3) / maxY, j, 3);
//					verticesTable.setValueAt((Double) verticesTable.getValueAt(j, 4) / maxZ, j, 4);
//				}
//				firstObjIndex = i;
//			}
//			// if(verticesTable.getValueAt(i, i) maxX)
//		}
//		for (int j = firstObjIndex; j < verticesTable.getRowCount(); j++) {
//			verticesTable.setValueAt((Double) verticesTable.getValueAt(j, 2) / maxX, j, 2);
//			verticesTable.setValueAt((Double) verticesTable.getValueAt(j, 3) / maxY, j, 3);
//			verticesTable.setValueAt((Double) verticesTable.getValueAt(j, 4) / maxZ, j, 4);
//		}
		// Check displacement scalar product
		for (int i = 0; i < triangleTable.getRowCount(); i++) {
			String objectId = triangleTable.getValueAt(i, 0).toString();
			String dId = triangleTable.getValueAt(i, 1).toString();
			String normVectorId[] = { null, null, null };
			String n[] = { null, null, null };
			Double[] v0 = { 0.0, 0.0, 0.0 };
			Double[] v1 = { 0.0, 0.0, 0.0 };
			Double[] v2 = { 0.0, 0.0, 0.0 };
			Double[] vector = { 0.0, 0.0, 0.0 };
			Double vectorMagnitude = 0.0;
			Double[] normalizedVector = { 0.0, 0.0, 0.0 };
			Double[] normV1 = { 0.0, 0.0, 0.0 };
			Double[] normV2 = { 0.0, 0.0, 0.0 };
			Double[] normV3 = { 0.0, 0.0, 0.0 };
			Double[] scalarProducts = { 0.0, 0.0, 0.0 };
			boolean found0 = false;
			boolean found1 = false;
			boolean found2 = false;
			boolean missingValues = false;
			// Get the x, y, and z coordinates for the three vertexes of the triangle
			for (int j = 0; j < verticesTable.getRowCount(); j++) {
				if (verticesTable.getValueAt(j, 0).toString().contentEquals(objectId) && verticesTable.getValueAt(j, 1)
						.toString().contentEquals(triangleTable.getValueAt(i, 6).toString())) {
					found0 = true;
					v0[0] = (Double) verticesTable.getValueAt(j, 2);
					v0[1] = (Double) verticesTable.getValueAt(j, 3);
					v0[2] = (Double) verticesTable.getValueAt(j, 4);
				}
				if (verticesTable.getValueAt(j, 0).toString().contentEquals(objectId) && verticesTable.getValueAt(j, 1)
						.toString().contentEquals(triangleTable.getValueAt(i, 7).toString())) {
					found1 = true;
					v1[0] = (Double) verticesTable.getValueAt(j, 2);
					v1[1] = (Double) verticesTable.getValueAt(j, 3);
					v1[2] = (Double) verticesTable.getValueAt(j, 4);
				}
				if (verticesTable.getValueAt(j, 0).toString().contentEquals(objectId) && verticesTable.getValueAt(j, 1)
						.toString().contentEquals(triangleTable.getValueAt(i, 8).toString())) {
					found2 = true;
					v2[0] = (Double) verticesTable.getValueAt(j, 2);
					v2[1] = (Double) verticesTable.getValueAt(j, 3);
					v2[2] = (Double) verticesTable.getValueAt(j, 4);
				}
			}
			//If any values were not found, another error will be generated and scalar product calc will fail
			if(!found0 || !found1 || !found2) {
				missingValues = true;
			}
			found0 = false;
			found1 = false;
			found2 = false;
			vector[0] = (((v1[1] - v0[1]) * (v2[2] - v0[2])) - ((v1[2] - v0[2]) * (v2[1] - v0[1])));
			vector[1] = (((v1[2] - v0[2]) * (v2[0] - v0[0])) - ((v1[0] - v0[0]) * (v2[2] - v0[2])));
			vector[2] = (((v1[0] - v0[0]) * (v2[1] - v0[1])) - ((v1[1] - v0[1]) * (v2[0] - v0[0])));

			// Normalize the triangle's normal vector
			vectorMagnitude = Math.sqrt((vector[0] * vector[0]) + (vector[1] * vector[1]) + (vector[2] * vector[2]));
			normalizedVector[0] = vector[0] / vectorMagnitude;
			normalizedVector[1] = vector[1] / vectorMagnitude;
			normalizedVector[2] = vector[2] / vectorMagnitude;
			// Get the normvectorID from the disp2dGroupTable
			for (int j = 0; j < disp2dGroupTable.getRowCount(); j++) {
				if (disp2dGroupTable.getValueAt(j, 0).toString().contentEquals(dId) && disp2dGroupTable.getValueAt(j, 1)
						.toString().contentEquals(triangleTable.getValueAt(i, 3).toString())) {
					found0 = true;
					normVectorId[0] = disp2dGroupTable.getValueAt(j, 2).toString();
					n[0] = disp2dGroupTable.getValueAt(j, 3).toString();
				}
				if (disp2dGroupTable.getValueAt(j, 0).toString().contentEquals(dId) && disp2dGroupTable.getValueAt(j, 1)
						.toString().contentEquals(triangleTable.getValueAt(i, 4).toString())) {
					found1 = true;
					normVectorId[1] = disp2dGroupTable.getValueAt(j, 2).toString();
					n[1] = disp2dGroupTable.getValueAt(j, 3).toString();
				}
				if (disp2dGroupTable.getValueAt(j, 0).toString().contentEquals(dId) && disp2dGroupTable.getValueAt(j, 1)
						.toString().contentEquals(triangleTable.getValueAt(i, 5).toString())) {
					found2 = true;
					normVectorId[2] = disp2dGroupTable.getValueAt(j, 2).toString();
					n[2] = disp2dGroupTable.getValueAt(j, 3).toString();
				}
			}
			//If any values were not found, another error will be generated and scalar product calc will fail
			if(!found0 || !found1 || !found2) {
				missingValues = true;
			}
			found0 = false;
			found1 = false;
			found2 = false;
			// Get the x, y, and z values for the three d vectors
			for (int j = 0; j < normVectorGroupTable.getRowCount(); j++) {
				if (normVectorId[0] != null && n[0] != null && n[1] != null && n[2] != null) {
					if (normVectorGroupTable.getValueAt(j, 0).toString().contentEquals(normVectorId[0])
							&& normVectorGroupTable.getValueAt(j, 1).toString().contentEquals(n[0])) {
						found0 = true;
						normV1[0] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 2).toString());
						normV1[1] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 3).toString());
						normV1[2] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 4).toString());
					}
					if (normVectorGroupTable.getValueAt(j, 0).toString().contentEquals(normVectorId[0])
							&& normVectorGroupTable.getValueAt(j, 1).toString().contentEquals(n[1])) {
						found1 = true;
						normV2[0] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 2).toString());
						normV2[1] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 3).toString());
						normV2[2] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 4).toString());
					}
					if (normVectorGroupTable.getValueAt(j, 0).toString().contentEquals(normVectorId[0])
							&& normVectorGroupTable.getValueAt(j, 1).toString().contentEquals(n[2])) {
						found2 = true;
						normV3[0] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 2).toString());
						normV3[1] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 3).toString());
						normV3[2] = Double.parseDouble(normVectorGroupTable.getValueAt(j, 4).toString());
					}
				}
			}
			
			//If any values were not found, another error will be generated and scalar product calc will fail
			if(!found0 || !found1 || !found2) {
				missingValues = true;
			}
			found0 = false;
			found1 = false;
			found2 = false;

			System.out.println("norm vector 0: " + normalizedVector[0]);
			System.out.println("norm vector 1: " + normalizedVector[1]);
			System.out.println("norm vector 2: " + normalizedVector[2]);
			System.out.println("");
			System.out.println("norm v1 0: " + normV1[0]);
			System.out.println("norm v1 1: " + normV1[1]);
			System.out.println("norm v1 2: " + normV1[2]);
			System.out.println("");
			System.out.println("norm v2 0: " + normV2[0]);
			System.out.println("norm v2 1: " + normV2[1]);
			System.out.println("norm v2 2: " + normV2[2]);
			System.out.println("");
			System.out.println("norm v3 0: " + normV3[0]);
			System.out.println("norm v3 1: " + normV3[1]);
			System.out.println("norm v3 2: " + normV3[2]);
			System.out.println("");
			
			//Calculate scalar products if no values are missing
			if(!missingValues) {
				scalarProducts[0] = ((normalizedVector[0] * normV1[0]) + (normalizedVector[1] * normV1[1])
						+ (normalizedVector[2] * normV1[2]));
				scalarProducts[1] = ((normalizedVector[0] * normV2[0]) + (normalizedVector[1] * normV2[1])
						+ (normalizedVector[2] * normV2[2]));
				scalarProducts[2] = ((normalizedVector[0] * normV3[0]) + (normalizedVector[1] * normV3[1])
						+ (normalizedVector[2] * normV3[2]));
				
				if (scalarProducts[0] <= 0) {
					fErrorReturn += "<br>ER_175: Scalar product (" + scalarProducts[0] + ") of normal vector (" + normalizedVector[0] + ", "
							+ normalizedVector[1] + ", " + normalizedVector[2] + ") and normvector (" + normV1[0]
							+ ", " + normV1[1] + ", " + normV1[2] + ") is 0 or less.<br>" + "Normal vector from Object "
							+ objectId + " triangle index " + triangleTable.getValueAt(i, 2)
							+ " , displacement normvector from d1";
					fxmlErrorReturn += "<Issue  issueID=\"ER_175\" severity=\"high\" class=\"structure\" type=\"resource ordering\">";
					fxmlErrorReturn += "Scalar product (" + scalarProducts[0] + ") of normal vector (" + normalizedVector[0] + ", "
							+ normalizedVector[1] + ", " + normalizedVector[2] + ") and normvector (" + normV1[0]
							+ ", " + normV1[1] + ", " + normV1[2] + ") is 0 or less.<br>" + "Normal vector from Object "
							+ objectId + " triangle index " + triangleTable.getValueAt(i, 2)
							+ " , displacement normvector from d1";
					fxmlErrorReturn += "</Issue>";
					errChk("ER_175");
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				}
				if (scalarProducts[1] <= 0) {
					fErrorReturn += "<br>ER_175: Scalar product (" + scalarProducts[1] + ") of normal vector (" + normalizedVector[0] + ", "
							+ normalizedVector[1] + ", " + normalizedVector[2] + ") and normvector (" + normV2[0]
							+ ", " + normV2[1] + ", " + normV2[2] + ") is 0 or less.<br>" + "Normal vector from Object "
							+ objectId + " triangle index " + triangleTable.getValueAt(i, 2)
							+ " , displacement normvector from d2";
					fxmlErrorReturn += "<Issue  issueID=\"ER_175\" severity=\"high\" class=\"structure\" type=\"resource ordering\">";
					fxmlErrorReturn += "<Issue  issueID=\"ER_175\" severity=\"high\" class=\"structure\" type=\"resource ordering\">";
					fxmlErrorReturn += "Scalar product (" + scalarProducts[1] + ") of normal vector (" + normalizedVector[0] + ", "
							+ normalizedVector[1] + ", " + normalizedVector[2] + ") and normvector (" + normV2[0]
							+ ", " + normV2[1] + ", " + normV2[2] + ") is 0 or less.<br>" + "Normal vector from Object "
							+ objectId + " triangle index " + triangleTable.getValueAt(i, 2)
							+ " , displacement normvector from d2";
					fxmlErrorReturn += "</Issue>";
					errChk("ER_175");
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				}
				if (scalarProducts[2] <= 0) {
					fErrorReturn += "<br>ER_175: Scalar product (" + scalarProducts[2] + ") of normal vector (" + normalizedVector[0] + ", "
							+ normalizedVector[1] + ", " + normalizedVector[2] + ") and normvector (" + normV3[0]
							+ ", " + normV3[1] + ", " + normV3[2] + ")" + " is 0 or less.<br>" + "Normal vector from Object "
							+ objectId + " triangle index " + triangleTable.getValueAt(i, 2)
							+ " , displacement normvector from d3";
					fxmlErrorReturn += "<Issue  issueID=\"ER_175\" severity=\"high\" class=\"structure\" type=\"resource ordering\">";
					fxmlErrorReturn += "Scalar product (" + scalarProducts[2] + ") of normal vector (" + normalizedVector[0] + ", "
							+ normalizedVector[1] + ", " + normalizedVector[2] + ") and normvector (" + normV3[0]
							+ ", " + normV3[1] + ", " + normV3[2] + ")" + " is 0 or less.<br>" + "Normal vector from Object "
							+ objectId + " triangle index " + triangleTable.getValueAt(i, 2)
							+ " , displacement normvector from d3";
					fxmlErrorReturn += "</Issue>";
					errChk("ER_175");
					ierCnt++;
					if (ierCnt > iErrMaxStr) {
						fErrorReturn += "<br>More than " + iErrMaxStr + " errors found, stopping..<br>";
						return;
					}
				}
			}
		}

		// check for unused ids
		for (

		Map.Entry<String, Boolean> entry : idUsed.entrySet()) {
			String id = entry.getKey();
			Boolean bused = entry.getValue();
			if (idWithName.get(id).equals("object") || idWithName.get(id).equals("slicestack"))
				continue;
			if (!bused) {
				fWarningReturn += "<br>WR_007: " + idWithName.get(id) + " with id: " + id + ", in file: " + file
						+ ", does not appear to have been used.";
				fxmlWarningReturn += "<Issue  issueID=\"WR_007\" severity=\"info\" class=\"structure\" type=\"orphaned item\">";
				fxmlWarningReturn += idWithName.get(id) + " with id: " + id + ", in file: " + file
						+ ", does not appear to have been used.";
				fxmlWarningReturn += "</Issue>";
				errChk("WR_007");
				iwrCnt++;
				if (iwrCnt > iWrnMaxStr) {
					fWarningReturn += "<br>More than " + iWrnMaxStr + " warnings found, stopping..<br>";
					return;
				}

			}
		}
	}

	private void errChk(String err) {
		if (errList.indexOf(err) < iErr) {
			sErr = err;
			iErr = errList.indexOf(err);
		}

	}

	private boolean overRideFileCheck(String file) {
		boolean bRes = false;
		if (sOverRide.size() > 0 && sOverRide.indexOf(file.toLowerCase()) > -1)
			bRes = true;
		return bRes;
	}

	// check the inbound list and return a set of the duplicate entries, if any
	public static Set<String> findDuplicates(List<String> listContainingDuplicates) {

		final Set<String> setToReturn = new HashSet<String>();
		final Set<String> set1 = new HashSet<String>();

		for (String yourInt : listContainingDuplicates) {
			if (!set1.add(yourInt)) {
				setToReturn.add(yourInt);
			}
		}
		return setToReturn;
	}

	public static Set<String> findPrefix(Document doc) {

		Set<String> setToReturn = new HashSet<String>();
		NodeList n1 = doc.getElementsByTagName("model");
		Node n2 = n1.item(0);
		for (int j = 0; j < n2.getAttributes().getLength(); j++) {
			if (n2.getAttributes().item(j).getNodeValue() != null
					&& n2.getAttributes().item(j).getNodeValue().contentEquals(pns)) {
				setToReturn.add(n2.getAttributes().item(j).getNodeName().substring(6));
				pnsc = n2.getAttributes().item(j).getNodeName().substring(6);
			}
			if (n2.getAttributes().item(j).getNodeValue() != null
					&& n2.getAttributes().item(j).getNodeValue().contentEquals(sns)) {
				setToReturn.add(n2.getAttributes().item(j).getNodeName().substring(6));
				snsc = n2.getAttributes().item(j).getNodeName().substring(6);
			}
		}

		return setToReturn;
	}

	// Check to see if the id has already been assigned to a resource item.
	private Boolean chkResourcesId(String idIn, String idName, String file) {
		Boolean fnd = false;
		Integer ichk = idWithCount.get(idIn);
		if (ichk != null) {
			fnd = true;
			String sfnd = idWithName.get(idIn);
			fErrorReturn += "<br>ER_107: " + idName + " with id=" + idIn + " in file: " + file + " conflicts with "
					+ sfnd + " which has the same id.";
			fxmlErrorReturn += "<Issue  issueID=\"ER_107\" severity=\"critical\" class=\"structure\" type=\"id reference\">";
			fxmlErrorReturn += idName + " with id=" + idIn + " in file: " + file + " conflicts with " + sfnd
					+ " which has the same id.";
			fxmlErrorReturn += "</Issue>";
			errChk("ER_107");
			ierCnt++;
		}
		return fnd;
	}

	private void backSlashWarn(String loc, String file) {
		if (!loc.contains("\\"))
			return;

		fWarningReturn += "<br>WR_015: Found backslash rather than forwardslash for path " + loc + " in " + file
				+ "<br>";
		fxmlWarningReturn += "<Issue  issueID=\"WR_015\" severity=\"info\" class=\"structure\" type=\"convention\">";
		fxmlWarningReturn += "Found backslash rather than forwardslash for path " + loc + " in " + file;
		fxmlWarningReturn += "</Issue>";
		errChk("WR_015");
		iwrCnt++;

	}

	private void checkRel0() {
		try {
			// We are not trying to debug the structure here. If we do not find
			// something, just return.
			File file = new File("ztmp/_rels/.rels");
			if (!file.exists())
				return;

			String xfile;
			xfile = FileUtils.readFileToString(file, "UTF-8");
			if (!xfile.trim().toLowerCase().startsWith("<?xml"))
				return;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);// needed to use
												// getElementsByTagNameNS
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xfile.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if (node.getNodeName().equals("Relationship")) {
						String type = node.getAttributes().getNamedItem("Type").getTextContent();
						if (type.contentEquals("http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel")) {
							srel0 = node.getAttributes().getNamedItem("Id").getTextContent().trim();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			fWarningReturn += "Failed to locate an id for \"rel0\" in _rels/.rels<br>";
		}
	}

	private KeyInfo CheckKeyInfo(String floc) {
		String lfloc = floc.replaceAll("\\\\", "/");
		KeyInfo key = null;
		KeyInfo keychk = null;
		for (int i = 0; i < keyinfo.size(); i++) {
			keychk = keyinfo.get(i);
			if (lfloc.equals(keychk.getFile())) {
				key = keychk;
				break;
			}
		}

		return key;
	}
}
