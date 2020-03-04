package de.thk.das.rest.security.http.crehma.sig;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.HashMap;


public class Rsa512Authenticator implements TbsAuthenticator {
	
	private HashMap<String,KeyPair> rsaKeyStore;
	
	public  Rsa512Authenticator() {
		// TODO Auto-generated constructor stub
		rsaKeyStore = new HashMap<>();
	}
	
	public  Rsa512Authenticator(HashMap<String,KeyPair> rsaKeyStore) {
		// TODO Auto-generated constructor stub
		this.rsaKeyStore = rsaKeyStore;
	}

	@Override
	public byte[] sign(String kid, String tbs) throws Exception {
		// TODO Auto-generated method stub
		KeyPair keyPair = rsaKeyStore.get(kid);
		PrivateKey privateKey = keyPair.getPrivate();
		Signature privateSignature = Signature.getInstance("SHA512withRSA");
	    privateSignature.initSign(privateKey);
	    privateSignature.update(tbs.getBytes(UTF_8));

	    byte[] signature = privateSignature.sign();
	    
	    return signature;
	
	}

	@Override
	public boolean verify(String kid, String tbs, String sv) {
		Signature publicSignature;
		try {
			publicSignature = Signature.getInstance("SHA512withRSA");
			KeyPair keyPair = rsaKeyStore.get(kid);
			PublicKey publicKey = keyPair.getPublic();
	        publicSignature.initVerify(publicKey);
	        publicSignature.update(tbs.getBytes(UTF_8));

	        return publicSignature.verify(sv.getBytes(UTF_8));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return false;
		}
		
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "RSA/SHA512";
	}

	@Override
	public boolean isDublicateSignature(String sv) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addSignature(String sv) {
		// TODO Auto-generated method stub
		
	}

}
