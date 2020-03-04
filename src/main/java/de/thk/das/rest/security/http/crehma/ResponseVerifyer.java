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
package de.thk.das.rest.security.http.crehma;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpResponse;

import de.thk.das.rest.security.http.crehma.hash.BodyHasher;
import de.thk.das.rest.security.http.crehma.sig.TbsAuthenticator;
import de.thk.das.rest.security.http.crehma.utils.HttpUtils;
import de.thk.das.rest.security.http.crehma.utils.SignatureHeaderParser;

public class ResponseVerifyer extends ResponseAuthentication {
	
	public ResponseVerifyer(){
		super();
	}
	
	public ResponseVerifyer(TbsAuthenticator tbsAuthenticator, BodyHasher bodyHasher) {
		this();
		addTbsAuthenticator(tbsAuthenticator);
		addBodyHasher(bodyHasher);
	}
	
	protected ResponseVerifyer(List<TbsAuthenticator> tbsAuthenticators, List<BodyHasher> bodyHashers){
		this();
		addTbsAuthenticators(tbsAuthenticators);
		addBodyHashers(bodyHashers);
	}
	
	public void verify(HttpResponse res, String requestMethod, String uri) throws Exception{
		
		if(res.containsHeader("Signature")){
			
			SignatureHeaderParser parser = new SignatureHeaderParser(res.getFirstHeader("Signature").getValue());
			parser.parse();
			
			String tvp = parser.getTvp();
			
			
			String kid = parser.getKid();
			String sv = parser.getSigValue();
			String hash = parser.getHash();
			String sig = parser.getSig();
			List<String> additionalHeaders = parser.getAddHeaders();
			
			String tbs = buildTbs(res, requestMethod, uri,hash,tvp,additionalHeaders);
			
			TbsAuthenticator tbsAuthenticator = getTbsAuthenticatorsHashMap().get(sig);
			
			if(tbsAuthenticator == null){
				throw new UnsupportedSignatureAlgorithm();
			}
			
			if(!tbsAuthenticator.verify(kid, tbs, sv)){
				throw new NotAuthenticatedExpection("Signature invalid");
			}
			
			if(tbsAuthenticator.isDublicateSignature(sv)){
				if(res.containsHeader("Cache-Control")){
					String cacheControlHeader = res.getFirstHeader("Cache-Control").getValue();
					long maxAge = 0;
					maxAge = HttpUtils.getMaxAge(cacheControlHeader);
					//maxAge = HttpUtils.getSMaxAge(cacheControlHeader);
					if (maxAge <= 0) {
						throw new NotAuthenticatedExpection("Dublicate Signature is not fresh");
					}
					
					if(!verifySignatureFreshness(res, tvp)) {
						throw new NotAuthenticatedExpection("Dublicate Signature is not fresh");
					}
					
				} else if(res.containsHeader("Expires")){
					String expiresHeader = res.getFirstHeader("Expires").getValue();
					long tvpDate = getTimeVariantParameter().parseTvp(tvp).getTime();
					
					SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
					long expiresHeaderDate = format.parse(expiresHeader).getTime();
					if(tvpDate <= expiresHeaderDate + getTimeVariantParameter().getDelta()) {
						
					}
					
				}
				
				
			} else {
				if(!getTimeVariantParameter().verify(tvp)){
					throw new NotAuthenticatedExpection("Invalid TVP");
				}
				tbsAuthenticator.addSignature(sv);
			}
			
			
			
		} else {
			throw new NotAuthenticatedExpection("No Signature Header");
		}
		
		
	}
	
	public boolean verifySignatureFreshness(HttpResponse res, String tvp) throws Exception{
		String cacheControlHeader = res.getFirstHeader("Cache-Control").getValue();
		String[] cacheControlHeaderEntries = cacheControlHeader.split(",");
		long maxAge = 0;
		long delta = getTimeVariantParameter().getDelta();
		long tvpDate = getTimeVariantParameter().parseTvp(tvp).getTime();
		
		for (String entry : cacheControlHeaderEntries) {
			if(entry.startsWith("s-maxage=")){
				maxAge = Long.parseLong(entry.split("=")[1]);
			} else if(entry.startsWith("max-age=")){
				maxAge = Long.parseLong(entry.split("=")[1]);
			}
		}

		long signatureExpirationDate = tvpDate + delta + maxAge * 1000;
		
		long now = new Date().getTime();
		
		if(now < signatureExpirationDate){
			return true;
		}
		
		return false;
	}

}
