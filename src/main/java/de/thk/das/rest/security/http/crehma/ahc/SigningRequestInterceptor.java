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
package de.thk.das.rest.security.http.crehma.ahc;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;

import de.thk.das.rest.security.http.crehma.RequestSigner;
import de.thk.das.rest.security.http.crehma.client.request.HttpBasicRequest;
import de.thk.das.rest.security.http.crehma.hash.BodyHasher;
import de.thk.das.rest.security.http.crehma.sig.TbsAuthenticator;

public class SigningRequestInterceptor extends RequestSigner implements HttpRequestInterceptor {
	private String kid;
	private String hash;
	private String sig;
	private ArrayList<String> additionalHeaders;
	private boolean signRequest;
	
	public boolean isSignRequest() {
		return signRequest;
	}

	public void setSignRequest(boolean signRequest) {
		this.signRequest = signRequest;
	}

	public SigningRequestInterceptor(){
		super();
	}
	
	public SigningRequestInterceptor(String kid, TbsAuthenticator tbsAuthenticator,
			BodyHasher bodyHasher) {
		super(tbsAuthenticator, bodyHasher);
		this.sig = tbsAuthenticator.getName();
		this.hash = bodyHasher.getName();
		this.kid = kid;
		this.additionalHeaders = new ArrayList<String>();
		this.signRequest = true;
		
	}

	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		//Set HTTP-Method for VerifyingResponseInterceptor
		context.setAttribute("Method", request.getRequestLine().getMethod());
		context.setAttribute("Uri", request.getFirstHeader("Host").getValue()+request.getRequestLine().getUri());
		
		HttpBasicRequest basicRequest;
		basicRequest = new HttpBasicRequest(request.getRequestLine().getUri(),
				request.getRequestLine().getMethod());
		basicRequest.setHeaders(request.getAllHeaders());
		if (signRequest) {
			
			
			byte[] body = new byte[0];
			if (request instanceof HttpEntityEnclosingRequest) {
				body = IOUtils.toByteArray(((HttpEntityEnclosingRequest) request).getEntity().getContent());
			}
			basicRequest.setEntity(new ByteArrayEntity(body));
			try {
				sign(basicRequest, this.sig, this.kid, this.hash, this.additionalHeaders);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		request.setHeaders(basicRequest.getAllHeaders());
	}
	
	public String getKid() {
		return kid;
	}

	public void setKid(String kid) {
		this.kid = kid;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}
	
	public ArrayList<String> getAdditionalHeaders() {
		return additionalHeaders;
	}

	public void setAdditionalHeaders(ArrayList<String> additionalHeaders) {
		this.additionalHeaders = additionalHeaders;
	}
}
