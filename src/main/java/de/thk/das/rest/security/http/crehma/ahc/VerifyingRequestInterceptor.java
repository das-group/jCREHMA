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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;

import de.thk.das.rest.security.http.crehma.RequestVerifyer;
import de.thk.das.rest.security.http.crehma.client.request.HttpBasicRequest;
import de.thk.das.rest.security.http.crehma.hash.BodyHasher;
import de.thk.das.rest.security.http.crehma.sig.TbsAuthenticator;

public class VerifyingRequestInterceptor extends RequestVerifyer implements
		HttpRequestInterceptor {

	public VerifyingRequestInterceptor(){
		super();
	}
	
	public VerifyingRequestInterceptor(
			TbsAuthenticator tbsAuthenticator,
			BodyHasher bodyHasher) {
		super(tbsAuthenticator, bodyHasher);
	}

	@Override
	public void process(HttpRequest request, HttpContext context)
			throws HttpException, IOException {
		context.setAttribute("Method", request.getRequestLine().getMethod());
		context.setAttribute("Uri", request.getFirstHeader("Host").getValue()+request.getRequestLine().getUri());
		HttpBasicRequest basicRequest = new HttpBasicRequest(request.getRequestLine().getUri(), request.getRequestLine().getMethod());
		basicRequest.setHeaders(request.getAllHeaders());
		byte[] body = new byte[0];
		
		if(request instanceof HttpEntityEnclosingRequest){
			body = IOUtils.toByteArray(((HttpEntityEnclosingRequest) request).getEntity().getContent());
		}
		basicRequest.setEntity(new ByteArrayEntity(body));
		try {
			verify(basicRequest);
		} catch (Exception e) {
			context.setAttribute("exception", e.getMessage());
			e.printStackTrace();
		}
	}

}
