/*******************************************************************************
 * Copyright 2015 Hoai Viet Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.thk.das.rest.security.http.crehma.sig;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import de.thk.das.rest.security.http.crehma.NotAuthenticatedExpection;

public class HmacSha256Authenticator implements TbsAuthenticator {
	
	private ArrayList<String> verifiedSignatures;
	
	private HashMap<String,byte[]> hmacKeyStore;
	
	public HmacSha256Authenticator() {
		hmacKeyStore = new HashMap<String, byte[]>();
		this.verifiedSignatures = new ArrayList<>();
	}
	
	public HmacSha256Authenticator(HashMap<String,byte[]> hmacKeyStore){
		this.hmacKeyStore = hmacKeyStore;
		this.verifiedSignatures = new ArrayList<>();
	}
	
	public HashMap<String, byte[]> getHmacKeyStore() {
		return hmacKeyStore;
	}

	public void setHmacKeyStore(HashMap<String, byte[]> hmacKeyStore) {
		this.hmacKeyStore = hmacKeyStore;
	}

	@Override
	public byte[] sign(String kid, String tbs) throws Exception {
		if(hmacKeyStore.size() == 0){
			throw new Exception();
		}
		
		byte[] key = hmacKeyStore.get(kid);
		if(key == null){
			throw new NullPointerException("Unkown Key id");
		}
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
		  return sha256_HMAC.doFinal(tbs.getBytes());
	}

	@Override
	public boolean verify(String kid, String tbs, String sv) throws NotAuthenticatedExpection {
		Mac sha256_HMAC = null;
		byte[] key = hmacKeyStore.get(kid);
		if(key == null){
			throw new NotAuthenticatedExpection("Unknown Key id");
			
		}
		try {
			sha256_HMAC = Mac.getInstance("HmacSHA256");
		} catch (NoSuchAlgorithmException e) {
			
			e.printStackTrace();
			return false;
		}
		  SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
		  try {
			sha256_HMAC.init(secret_key);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			String svTemp = Base64.encodeBase64URLSafeString(sha256_HMAC.doFinal(tbs.getBytes("UTF-8")));
			if(svTemp.equals(sv)){
				
				return true;
			} else {			
				return false;
			}
		} catch (Exception e) {
			
			return false;
		} 
		
	}

	@Override
	public String getName() {
		return "HMAC/SHA256";
	}

	@Override
	public boolean isDublicateSignature(String sv) {
		
		return verifiedSignatures.contains(sv) ? true : false;
	}

	@Override
	public void addSignature(String sv) {
		// TODO Auto-generated method stub
		verifiedSignatures.add(sv);
	}
	
	

}
