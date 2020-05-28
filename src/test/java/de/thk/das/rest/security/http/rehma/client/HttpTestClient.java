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
package de.thk.das.rest.security.http.rehma.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

import de.thk.das.rest.security.http.crehma.NotAuthenticatedExpection;
import de.thk.das.rest.security.http.crehma.ahc.RequestPlaceboInterceptor;
import de.thk.das.rest.security.http.crehma.ahc.ResponsePlaceboInterceptor;
import de.thk.das.rest.security.http.crehma.ahc.SigningRequestInterceptor;
import de.thk.das.rest.security.http.crehma.ahc.VerifyingResponseInterceptor;
import de.thk.das.rest.security.http.crehma.hash.Sha256Hasher;
import de.thk.das.rest.security.http.crehma.sig.HmacSha256Authenticator;
/**
 * jREHMA HTTP test client
 */
public class HttpTestClient {
	public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException, URISyntaxException {
		
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
	    
	    Sha256Hasher  sha256Hasher = new Sha256Hasher();
	    HmacSha256Authenticator hmacSha256Authenticator = new HmacSha256Authenticator();
	    hmacSha256Authenticator.getHmacKeyStore().put("jCREHMAKey", key);
	    
	    SigningRequestInterceptor sri = new SigningRequestInterceptor("jCREHMAKey",hmacSha256Authenticator, sha256Hasher);
	    VerifyingResponseInterceptor vri = new VerifyingResponseInterceptor(hmacSha256Authenticator, sha256Hasher);
		
	    HttpClient client = HttpClients.custom().addInterceptorLast(sri).addInterceptorFirst(vri).build();
	    
	    sri.setSignRequest(true);
	    
		
		long requestId = new Date().getTime();
		
		String host = "139.6.102.29";

		host = "d5esw3092ua6w.cloudfront.net";

		host = "ec2-34-241-245-90.eu-west-1.compute.amazonaws.com";

		
		HttpGet getRequest = new HttpGet("http://"+host);
		getRequest = new HttpGet("http://"+host+"/rsc/"+requestId);
		HttpCoreContext ctx = new HttpCoreContext();
		getRequest.setHeader("Accept", "application/json");
		getRequest.setHeader("X-Response","cc:max-age=0");
		getRequest.setHeader("Create-Signature","true");
		
		
		
		long start = new Date().getTime();
		HttpResponse response = client.execute(getRequest,ctx);
		long end = new Date().getTime();
		long delta = end - start;
		System.out.println(delta);
		System.out.println(response);
		System.out.println(EntityUtils.toString(response.getEntity()));
		
		

		Thread.sleep(1000);
//		start = new Date().getTime();
//		response = client.execute(getRequest,ctx);
//		end = new Date().getTime();
//		delta = end - start;
//		System.out.println(delta);
//		System.out.println(response);
//		System.out.println(EntityUtils.toString(response.getEntity()));
//		
//		Thread.sleep(1000);
//		start = new Date().getTime();
//		response = client.execute(getRequest,ctx);
//		end = new Date().getTime();
//		delta = end - start;
//		System.out.println(delta);
//		System.out.println(response);
//		System.out.println(EntityUtils.toString(response.getEntity()));
		
		System.out.println("------");
		int numberOfValidTest = 1000;
		int numberOfOmittedTests = 0;
		int numberOfTest = numberOfValidTest + numberOfOmittedTests;
		double totalTimes = 0;
		String times = "";
		for (int i = 0; i < numberOfTest; i++) {

//			client = HttpClients.custom().build();
//			client = HttpClients.custom().addInterceptorLast(sri).build();
			client = HttpClients.custom().addInterceptorLast(sri).addInterceptorFirst(vri).build();
			if (i >= numberOfOmittedTests) {
				start = new Date().getTime();
				response = client.execute(getRequest, ctx);
				end = new Date().getTime();

				delta = end - start;
				times += delta + "\n";
				totalTimes += delta;
				System.out.println(delta);
//				System.out.println(response);
			} else {
				response = client.execute(getRequest, ctx);
				System.out.println(delta);
			}
			
			Thread.sleep(1000);

		}
		
		System.out.println(totalTimes/numberOfValidTest);
		PrintWriter out = new PrintWriter("crehma_"+new Date().getTime()+".txt");
		out.close();
		
	}
}
