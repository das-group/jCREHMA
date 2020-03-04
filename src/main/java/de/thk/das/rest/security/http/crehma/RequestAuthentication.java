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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;

import de.thk.das.rest.security.http.crehma.client.request.HttpBasicRequest;
import de.thk.das.rest.security.http.crehma.hash.BodyHasher;

public abstract class RequestAuthentication extends MessageAuthentication {
	protected List<String> readHeaders;
	protected List<String> createUpdateHeaders;

	public RequestAuthentication() {
		super();
		readHeaders = new ArrayList<>();
		createUpdateHeaders = new ArrayList<>();
		getBasicHeaders().add("Host");
		readHeaders.add("Accept");
		createUpdateHeaders.add("Content-Type");
		createUpdateHeaders.add("Content-Length");
		createUpdateHeaders.add("Transfer-Encoding");
	}
	
	public void checkRequestSyntax(HttpBasicRequest req) throws IncorrectHttpMessage{
		
		String method = req.getRequestLine().getMethod().toUpperCase();	
		
		if((method.equals("GET") || method.equals("HEAD") || method.equals("OPTIONS") || method.equals("DELETE")) && req.getEntity() == null){
			throw new IncorrectHttpMessage("A request containing this method must include an empty body");
		}
		
		if((method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) && req.getEntity() == null){
			throw new IncorrectHttpMessage("A request containing this method must include a resource representation");
		}
	}
	
	public List<String> getTbsHeaders(List<String> tbsHeaders, String method){
		
		tbsHeaders.addAll(getBasicHeaders());
		tbsHeaders.addAll(this.readHeaders);
		tbsHeaders.addAll(createUpdateHeaders);
//		if (method.equals("GET") || method.equals("HEAD")) {
//			tbsHeaders.addAll(readHeaders);
//		}
//
//		else if (method.equals("POST") || method.equals("PUT")
//				|| method.equals("PATCH")) {
//			tbsHeaders.addAll(createUpdateHeaders);
//		}
		
		Collections.sort(tbsHeaders);
		return tbsHeaders;
	}
	

	
	public void getTbsHeaders(List<String> tbsHeaders, List<String> additionalHeaders){
		if (additionalHeaders != null)
			tbsHeaders.addAll(additionalHeaders);
	}

	public final String buildTbs(HttpBasicRequest req, String hash, String tvp,
			List<String> additionalHeaders) throws IncorrectHttpMessage, IllegalStateException, IOException  {
		
		checkRequestSyntax(req);
		
		String method = req.getRequestLine().getMethod().toUpperCase();	
		List<String> tbsHeaders = new ArrayList<String>();
		
		getTbsHeaders(tbsHeaders, method);
		getTbsHeaders(tbsHeaders, additionalHeaders);

		String tbs = tvp + "\n";
		tbs += method + "\n";
		tbs += req.getRequestLine().getUri() + "\n";
		tbs += req.getRequestLine().getProtocolVersion().toString()
				.toUpperCase()
				+ "\n";
		
		for (String header : tbsHeaders) {
			if(req.containsHeader(header))
				tbs += req.getFirstHeader(header).getValue().toLowerCase() + "\n";
			else
				tbs +="\n";
		}

		BodyHasher bodyHasher = getBodyHashersHashMap().get(hash);
		if (bodyHasher == null) {
			throw new UnsupportedHashAlgorithm();
		}
		String bodyHash = "";

		if (method.equals("POST")||method.equals("PATCH")||method.equals("PUT")) {
			bodyHash = Base64.encodeBase64URLSafeString(bodyHasher.hash(IOUtils
					.toByteArray(((HttpEntityEnclosingRequest) req).getEntity().getContent())));
		} else if (method.equals("GET") || method.equals("HEAD") || method.equals("OPTIONS") || method.equals("DELETE")) {
			bodyHash = Base64.encodeBase64URLSafeString(bodyHasher.getHashOfEmptyBody());
		} else {
			throw new IncorrectHttpMessage("Unknown Method");
		}

		tbs += bodyHash;
//		System.out.println(tbs);
		return tbs;
	}
}
