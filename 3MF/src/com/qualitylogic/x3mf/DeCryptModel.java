package com.qualitylogic.x3mf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;

public class DeCryptModel {
    static String plainText = "This is a plain text which need to be encrypted by Java AES 256 GCM Encryption Algorithm";
    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
    public String sIV;
    public String sAAD;
    public String sTAG;
    public String sCT;
    public String FileLoc;
    public boolean bSha256;
    public boolean bNoComp;

    public DeCryptModel() {
	}

    public String DoDecrypt() throws Exception
    {
		File file = new File(FileLoc);
		if (!file.exists()){
			return "";
		}
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
       
        // Generate Key

        byte[] IV = Base64.getDecoder().decode(sIV);
        byte[] AAD = Base64.getDecoder().decode(sAAD);
        byte[] TAG = Base64.getDecoder().decode(sTAG);
        byte[] ct = Base64.getDecoder().decode(sCT);
        byte[] combined =new byte[IV.length + TAG.length];
        System.arraycopy(IV,0,combined,0,IV.length);
        System.arraycopy(TAG,0,combined,IV.length,TAG.length);
        
        //decrypt with pem file.
        PrivateKey pkey = getKey();
        //need to look for  <kekparams digestmethod="http://www.w3.org/2001/04/xmlenc#sha256" for sha256
        //the true is for sha256, false sha1
        byte[] skey = pemdecrypt(pkey, ct, bSha256);
        
        SecretKey key = new SecretKeySpec(skey, 0, skey.length, "AES");

        InputStream is= new FileInputStream(FileLoc);;
        byte[] cipherText1 = IOUtils.toByteArray(is);
        is.close();
        int bstart = cipherText1[8];
        byte[] cipherText2 = Arrays.copyOfRange(cipherText1, bstart, cipherText1.length); 
        byte[] cipherText =new byte[cipherText2.length + TAG.length];
        System.arraycopy(cipherText2,0,cipherText,0,cipherText2.length);
        System.arraycopy(TAG,0,cipherText,cipherText2.length,TAG.length);
       
       //if the 8th (0 based) is x0C, then strip 3 more bytes, if x0E, then 5 more bytes.
        
        byte[] decryptedText = decrypt(cipherText, key, combined, IV.length, TAG.length, AAD);
        String txt = new String(decryptedText);
        if(txt.toLowerCase().startsWith("<?xml") || txt.length() < 2 || bNoComp)
        	return txt;
        txt = unzipString(decryptedText);
        return txt;
    }

    public static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] IV) throws Exception
    {
        // Get Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        
        // Create SecretKeySpec
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        
        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);
        
        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
        
        // Perform Encryption
        byte[] cipherText = cipher.doFinal(plaintext);
        
        return cipherText;
    }

    public static byte[] decrypt(byte[] cipherText, SecretKey key, byte[] IV, int ivsize, int tagsize, byte[] AAD ) throws Exception
    {
    	try{
	        // Get Cipher Instance
	        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
	        
	        // Create SecretKeySpec
	        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
	        
	        // Create GCMParameterSpec
	        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(tagsize * Byte.SIZE, IV, 0, ivsize);
	        if (gcmParameterSpec.getTLen() != tagsize * Byte.SIZE) {
	            throw new Exception("tLen's not equal");
	        }
	      
	        // Initialize Cipher for DECRYPT_MODE
	        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
	        
	        if(AAD.length > 1)
	        	cipher.updateAAD(AAD);
	        
	        // Perform Decryption
	        byte[] decryptedText = cipher.doFinal(cipherText);
	        
	        return decryptedText;
    	} catch (Exception e){
    		byte[] oops = new byte[1];
    		return oops;
    	}
    }
    public PrivateKey getKey(){
        PrivateKey key = null;
        //now a pkcs8 key, not #1
        String pem = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5t2XllX337AD/W9Rl54zwpBpJDt1XRb1XDs82VezNF2hq/BbgP97vRxqOh4ndMl9qsD3N0TekFXNeyNrlUzVrHOs8JCfvtW8UVMuwxrWUHPdRlyQort0BW6hERAIfrTjb/hQaG8qZWgar2O3TExXQIEiBY2B++ITq26KJ7MaSg8QlcFuGDRZc17olLFgjd9GSOM+Hx8dKVrkmKXov5etP+fp6FKGUs33rS2fyy7G7UBpe5G92KZk3FWArtVApxhBt8oN+nMSLkl93ZQrGa4qf/zueut4VhpkJ5OMVtp0/sWQHzB1IYYJy2x1F+SX+kIGR0usay/sizqNGVbszRmgFAgMBAAECggEAAcfw+bkrypdgURKb4FhOoTYprF6wn7gi/zU8pnw/d54MWc5OF9t0811+szQ4jb8AbEEBbrpdarzr7NJHSyC3p+3QkbSVD3bxeEPx6iGVtzRp6MXmyl8W7n7nB7SRQqWCIJX4smFm2X6kBrzfVwFdWbAdkeQWllEi0S/HCMSVCO2nVyj6SYK517Dh9+k8E9TDtNK45a5BWOj53kyHTFpnMyOPFKiue/gtECbUPg2gJpu4LHuNFjaA6CXf2fAJaL3ba+yxsSMBSDj7zrYmp7+153jEgYKTwM/5vDGPdZuguOQzjKn/kBwsaRqi+GO6kteu7jAOqxavzBuDBXUGbNTJ1wKBgQC8No7q3ny6TSRZUSaUYS56WfgB8hSOCOjnWpttvbdaw9BNm2Wns3T+o00qxaS2vRk9Eh0gVzFOmt36DLksajNAbPjUnaVP70Mayajd0HZ7juv64oOc3CYh6Cn7gtgWJcYsYM2iefsRJJ5zhePzPxiixUeySvrim08I7NjOYMKRbwKBgQD8mqPAso5iAFWhRMRlB8x7b3lUpoqvG2qmtgJMcX0n2XKzGDZZ8hUkHKhWemSbLYQj4zD65u0+vHvULHjXd16b9LVo44iyok+p0WRz4ARFXpl5PU1YsKDNahzNb83ll2d5SRZrSLQHSiUCIL1vnPH0cH6nVubvEQvfMvo5QM+7ywKBgF+WRdc6z5W+f+tsr29EO9qfvrkePRo1pDN7bNy7MVfDgxMnDOWy2oRIY3SJlDHKr/q4LFq0bbJCLOJFe/X2qbjanzU8Ky0Bks8UA9NDgCkYWPi9+aG8nREzW78EoMH+xTrcnxMZaE04j9CT+0UakmR4VQVT1z8nKWibATEvKyG5AoGAV3aj5IDu0/gRrjlFV0YQA11hwXG461Cu6PJ0cUuKblXYZTKo/xPXRh22G9tdIm3sJxDvULDMzMAukBSl8tUeKRneBmdiWZfhxYb/JTAXyn0q3tPfCCqAQx/OwUNJI4n9Bf3MiXEvBKcjkU0MyqypCl45PG/Wnkhkt+/KvDA4vWkCgYAz9mgvupZ6dkDMPUZ3Oj0eWvGXEEk+ev5l1iqsll2j9PABFjwtm7TtWy5pjVkCouWQEBFG3KmYaPTg8ZtZZFnc+f8zn8WCMZrkz2FWe8slIGSUTu18osHgovl4wAMjsm56U6p8oHFeunwiXNWVTwyTRVBHDqGOrXM0osm0PgQWrg==";
        try {
        	byte[] encoded = Base64.getDecoder().decode(pem);
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        key = kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
        	
        }
        return key;
    }
    public static PublicKey getPKey(){
    	PublicKey key = null;
    	String pub = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw53q4y2KB2WcoOBUE9OEXI0OCzUf4SI1J6fDx6XeDJ8PzqxN4pPRtXgtKfp/RiSL0invf7ASfkBMcXuhD8XP0uki3JIzvsxTH+Jnnz/PrYnS9DFa6c9MYciTIV8vC4u03vkZH6OuGq4rWeSZuNCTCgT59q67Ly6OytNsQgsDHL2QO8xhpYdQ4bx7F0uNn5LAxFyA0ymsFsgSSLONJWzaVtsq9jvkIOEdTzYq52PAXMUIpegbyqSheNlmedcss8teqiZGnCOxpBxL3z+ogcFenX1S8kq2UhzOjXLEjPs9B0SchwXSadephL89shJwra+30NS3R3frwfCz+a3H6wTVBwIDAQAB";
    	byte[] encoded = Base64.getDecoder().decode(pub);
    	try {
			key =KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return key;
    }
    public byte[] pemdecrypt(PrivateKey key, byte[] inputData, boolean bdoSha256)
            throws Exception {
    	if(bdoSha256){
        	String ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, key, oaepParams);
            byte[] decryptedBytes = cipher.doFinal(inputData);

            return decryptedBytes;
    	} else {
        	String ALGORITHM = "RSA/ECB/OAEPPadding";
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(inputData);

            return decryptedBytes;
    	}
    }
    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater(true);
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = 0;
            try {
                count = inflater.inflate(buffer);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
            inflater.end();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] output = outputStream.toByteArray();
        outputStream.close();
        return output;
    }
    public static String unzipString(byte[] zbytes) throws Exception {
        String charsetName = "ISO-8859-1";
        String unzipped = null;
        Inflater inflater = new Inflater(true);
        try {
            // Add extra byte to array when Inflater is set to true
            byte[] input = new byte[zbytes.length + 1];
            System.arraycopy(zbytes, 0, input, 0, zbytes.length);
            input[zbytes.length] = 0;
            ByteArrayInputStream bin = new ByteArrayInputStream(input);
            InflaterInputStream in = new InflaterInputStream(bin, inflater);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(512);
            int b;
            try{
	            while ((b = in.read()) != -1) {
	                bout.write(b); 
	            }
	            } catch (IOException e) {
	            	e.printStackTrace();
	            }
            bout.close();
            unzipped = bout.toString(charsetName);
        } catch (IOException e) { 
            e.printStackTrace();
        }
        if(unzipped.length() < 2){
        	throw new Exception("Failure to deflate input.");
        }
        return unzipped;
     }
}
