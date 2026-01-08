package com.qualitylogic.x3mf;

//Data structures for Secure Content Encryption

public class KeyInfo {
    private String sIV;
    private String sAAD;
    private String sTAG;
    private String sCT;
    private String sFileLoc;
    private boolean bSha256;
    private boolean bNoComp;

	public KeyInfo() {
	}
	public String getIV() {return sIV;}
	public String getAAD() {return sAAD;}
	public String getTAG() {return sTAG;}
	public String getCT() {return sCT;}
	public String getFile() {return sFileLoc;}
	public boolean getSha256() {return bSha256;}
	public boolean getNoComp() {return bNoComp;}
	
	public void setIV(String in) { this.sIV = in; }
	public void setAAD(String in) { this.sAAD = in; }
	public void setTAG(String in) { this.sTAG = in; }
	public void setCT(String in) { this.sCT = in; }
	public void setFile(String in) { this.sFileLoc = in; }
	public void setSha256(boolean bSha) { this.bSha256 = bSha; }
	public void setNoComp(boolean bNC) { this.bNoComp = bNC; }

}
