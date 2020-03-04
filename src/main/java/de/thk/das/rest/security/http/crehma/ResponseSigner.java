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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;

import de.thk.das.rest.security.http.crehma.hash.BodyHasher;
import de.thk.das.rest.security.http.crehma.sig.TbsAuthenticator;


public class ResponseSigner extends ResponseAuthentication{
	
	public ResponseSigner(TbsAuthenticator signer, BodyHasher hasher){
		this();
		addTbsAuthenticator(signer);	
		addBodyHasher(hasher);
	}
	
	protected ResponseSigner(List<TbsAuthenticator> tbsAuthenticators, List<BodyHasher> bodyHashers){
		this();
		addTbsAuthenticators(tbsAuthenticators);
		addBodyHashers(bodyHashers);
	}
	
	public ResponseSigner() {
		super();
	
	}

	
	protected void sign(HttpResponse res, String sig, String kid, String hash, ArrayList<String> additionalHeaders,String requestMethod, String uri) throws Exception{
		
		String tvp = getTimeVariantParameter().generate();
		String tbs = "";
		
		tbs = buildTbs(res, requestMethod, uri , hash, tvp, additionalHeaders);
		
		TbsAuthenticator tbsAuthenticator = getTbsAuthenticatorsHashMap().get(sig);
		if(tbsAuthenticator == null){
			throw new UnsupportedSignatureAlgorithm();
		}
		
		String signature = Base64.encodeBase64URLSafeString(tbsAuthenticator.sign(kid, tbs));
		
		String addHeaders = "null";
		if(!additionalHeaders.isEmpty()){			
			addHeaders = StringUtils.join(additionalHeaders,";");
		}
		
		res.setHeader("Signature", String.format(SIGNATURE_HEADER, sig,hash,kid,tvp,addHeaders,signature));
	}	
	
}
