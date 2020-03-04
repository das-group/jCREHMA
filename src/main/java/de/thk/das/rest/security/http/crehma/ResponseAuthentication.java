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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;

import de.thk.das.rest.security.http.crehma.hash.BodyHasher;

public abstract class ResponseAuthentication extends MessageAuthentication {

	protected List<String> contentHeaders;
	protected List<String> optionsHeaders;
	protected List<String> redirectHeaders;
	protected List<String> cacheHeaders;

	public ResponseAuthentication() {
		super();
		contentHeaders = new ArrayList<String>();
		optionsHeaders = new ArrayList<String>();
		redirectHeaders = new ArrayList<String>();
		cacheHeaders = new ArrayList<String>();
		contentHeaders.add("Content-Type");
		contentHeaders.add("Content-Length");
		contentHeaders.add("Transfer-Encoding");
		optionsHeaders.add("Allow");
		redirectHeaders.add("Location");
		cacheHeaders.add("Cache-Control");
		cacheHeaders.add("Expires");
		cacheHeaders.add("ETag");
		cacheHeaders.add("Last-Modified");
		
	}

	public void checkResponseSyntax(HttpResponse res, String requestMethod)
			throws IncorrectHttpMessage {
		
		int statusCode = res.getStatusLine().getStatusCode();

		if (requestMethod.equals("OPTIONS") && !res.containsHeader("Allow")) {
			throw new IncorrectHttpMessage(
					"A response of an OPTIONS request must contain an Allow Header-Field");
		}

		else if (((requestMethod.equals("POST") && statusCode == 201) || statusCode == 301
				|| statusCode == 302 || statusCode == 303 || statusCode == 307)
				&& !res.containsHeader("Location")) {
			throw new IncorrectHttpMessage(
					"This response must contain a Location Header-Field");
		}

		else if (statusCode == 204 && res.getEntity() != null) {
			throw new IncorrectHttpMessage(
					"This response containing the status code 204 must include an empty body");
		}
		
		else if (requestMethod.equals("HEAD") && res.getEntity().getContentLength() > 0){
			throw new IncorrectHttpMessage(
					"A response to HEAD request  must include an empty body");
		}
		
		
	}

	public void getTbsHeaders(List<String> tbsHeaders, int statusCode,
			String requestMethod, long contentLength) {
		tbsHeaders.clear();
		tbsHeaders.addAll(getBasicHeaders());
		tbsHeaders.addAll(this.cacheHeaders);
		
		
		if (requestMethod.equals("OPTIONS")) {
			tbsHeaders.addAll(optionsHeaders);
		}

		else if (requestMethod.equals("POST") || statusCode == 301
				|| statusCode == 302 || statusCode == 303 || statusCode == 307) {
			tbsHeaders.addAll(redirectHeaders);
		}

		if (contentLength > 0) {
			tbsHeaders.addAll(contentHeaders);
		}

		Collections.sort(tbsHeaders);
	}

	public void getTbsHeaders(List<String> tbsHeaders,
			List<String> additionalHeaders) {
		if (additionalHeaders != null) {
			tbsHeaders.addAll(additionalHeaders);
		}
	}

	public final String buildTbs(HttpResponse res, String requestMethod, String uri,
			String hash, String tvp, List<String> additionalHeaders)
			throws IncorrectHttpMessage, NoSuchAlgorithmException,
			IllegalStateException, IOException {

		checkResponseSyntax(res, requestMethod);

		int statusCode = res.getStatusLine().getStatusCode();
		List<String> tbsHeaders = new ArrayList<>();
		if(res.getEntity() == null){			
			getTbsHeaders(tbsHeaders, statusCode, requestMethod,0);
		} else {
			getTbsHeaders(tbsHeaders, statusCode, requestMethod,res.getEntity().getContentLength());
		}
		getTbsHeaders(tbsHeaders, additionalHeaders);

		String tbs = tvp + "\n";
		tbs += requestMethod + "\n";
		tbs += uri + "\n";
		tbs += res.getStatusLine().getProtocolVersion().toString() + "\n";
		tbs += statusCode + "\n";

		for (String header : tbsHeaders) {
			if(res.containsHeader(header))
				tbs += res.getFirstHeader(header).getValue().toLowerCase() + "\n";
			else
				tbs +="\n";
		}

		BodyHasher bodyHasher = getBodyHashersHashMap().get(hash);
		if (bodyHasher == null) {
			throw new UnsupportedSignatureAlgorithm();
		}

		String bodyHash = "";
		byte[] body = null;
		if (res.getEntity() != null) {
			body = IOUtils.toByteArray(res.getEntity().getContent());
			bodyHash = Base64.encodeBase64URLSafeString(bodyHasher.hash(body));
		} else {
			bodyHash = Base64.encodeBase64URLSafeString(bodyHasher.getHashOfEmptyBody());
		}

		tbs += bodyHash;
		res.setEntity(new ByteArrayEntity(body));
		return tbs;
	}

}
