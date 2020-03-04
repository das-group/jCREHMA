package de.thk.das.rest.security.http.rehma;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class Base64Test {
	public static void main(String[] args) throws IOException {
		String test = "";
		byte[] test2 = new byte[8];
		
		String base64Key = "";
		BufferedReader br = new BufferedReader(new FileReader("key.txt"));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        base64Key = sb.toString();
	    } finally {
	        br.close();
	    }
	    
	    byte[] key = Base64.decodeBase64(base64Key); 
	    
	    String tbs = "Hello World";
	    
	    Mac sha256_HMAC = null;
	    
	    try {
			sha256_HMAC = Mac.getInstance("HmacSHA256");
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
			
		}
		  SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
		  try {
			sha256_HMAC.init(secret_key);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			
		}
		
		try {
			String svTemp = Base64.encodeBase64URLSafeString(sha256_HMAC.doFinal(tbs.getBytes("UTF-8")));
			System.out.println(svTemp);
			
		} catch (Exception e) {
			
			
		} 
		
		
	}
}
