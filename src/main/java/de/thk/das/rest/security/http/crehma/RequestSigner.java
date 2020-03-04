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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import de.thk.das.rest.security.http.crehma.client.request.HttpBasicRequest;
import de.thk.das.rest.security.http.crehma.hash.BodyHasher;
import de.thk.das.rest.security.http.crehma.sig.TbsAuthenticator;

public class RequestSigner extends RequestAuthentication {
	public RequestSigner(TbsAuthenticator tbsSigner, BodyHasher bodyHasher){
		this();
		addTbsAuthenticator(tbsSigner);
		addBodyHasher(bodyHasher);
	}
	
	protected RequestSigner() {
		super();
	}
	
	protected RequestSigner(List<TbsAuthenticator> tbsAuthenticators, List<BodyHasher> bodyHashers){
		this();
		addTbsAuthenticators(tbsAuthenticators);
		addBodyHashers(bodyHashers);
	}
	
	public void sign(HttpBasicRequest req, String sig, String kid, String hash, ArrayList<String> additionalHeaders) throws Exception{
		
		if(!req.containsHeader(HttpHeaders.HOST)){
			req.addHeader(HttpHeaders.HOST, new URI(req.getRequestLine().getUri()).getHost());
		}
		
		if(req.getRequestLine().getMethod().equals("GET") && !req.containsHeader(HttpHeaders.ACCEPT)){
			req.addHeader(HttpHeaders.ACCEPT, "*/*");
		}
		
		String tvp = getTimeVariantParameter().generate();
		
		String tbs = "";
		
		tbs = buildTbs(req, hash, tvp, additionalHeaders);
		
		String addHeaders = "null";
		if(!additionalHeaders.isEmpty()){			
			addHeaders = StringUtils.join(additionalHeaders,";");
		}
		
		TbsAuthenticator tbsAuthenticator = getTbsAuthenticatorsHashMap().get(sig);
		
		if(tbsAuthenticator == null){
			throw new UnsupportedSignatureAlgorithm();
		}
	
		String signature = Base64.encodeBase64URLSafeString(tbsAuthenticator.sign(kid, tbs));
	
		req.setHeader("Signature", String.format(SIGNATURE_HEADER, sig,hash,kid,tvp,addHeaders,signature));
	}
}
