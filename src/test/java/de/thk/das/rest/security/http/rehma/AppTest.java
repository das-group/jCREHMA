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
package de.thk.das.rest.security.http.rehma;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.thk.das.rest.security.http.crehma.ahc.SigningRequestInterceptor;
import de.thk.das.rest.security.http.crehma.ahc.VerifyingResponseInterceptor;
import de.thk.das.rest.security.http.crehma.hash.Sha256Hasher;
import de.thk.das.rest.security.http.crehma.sig.HmacSha256Authenticator;
import de.thk.das.rest.security.http.rehma.server.SimplejCRHEMAServerSync;

/**
 * Unit test for jREHMA.
 */
public class AppTest
{
	private static String host = "http://localhost";
	private static int port = 8088;
	private static HttpClient client;
	private static SimplejCRHEMAServerSync server;
	
    @Test
    public void testGET() throws ClientProtocolException, IOException
    {
    	HttpGet request = new HttpGet(host+":"+port+"/courses");
    	request.setHeader("Accept", "application/json");
    	HttpResponse response = client.execute(request);
    	System.out.println(response);
        assertTrue( response.getStatusLine().getStatusCode() == 200 );
    }
    
    @Test
    public void testPUT() throws ClientProtocolException, IOException
    {
    	HttpPut request = new HttpPut(host+":"+port+":/courses/1");
    	String course = "{\"name\":\"Computer Science\",\"term\":\"summer\"}";
    	request.setEntity(new StringEntity(course));
    	request.setHeader("Content-Type", "application/json");
    	HttpResponse response = client.execute(request);
    	System.out.println(response);
        assertTrue( response.getStatusLine().getStatusCode() == 204);
    }
    
//    This will cause an exception as the Apache HTTPComponents server does not support the PATCH method
//    @Test
    public void testPATCH() throws ClientProtocolException, IOException
    {
    	HttpPatch request = new HttpPatch(host+":"+port+":/courses/1");
    	String course = "{\"name\":\"Computer Science\",\"term\":\"summer\"}";
    	request.setEntity(new StringEntity(course));
    	request.setHeader("Content-Type", "application/json");
    	HttpResponse response = client.execute(request);
    	System.out.println(response);
        assertTrue( response.getStatusLine().getStatusCode() == 204);
    }
    
    @Test
    public void testPOST() throws ClientProtocolException, IOException
    {
    	HttpPost request = new HttpPost(host+":"+port+":/courses");
    	String course = "{\"name\":\"Computer Science\",\"term\":\"summer\"}";
    	request.setEntity(new StringEntity(course));
    	request.setHeader("Content-Type", "application/json");
    	HttpResponse response = client.execute(request);
    	System.out.println(response);
        assertTrue( response.getStatusLine().getStatusCode() == 201);
    }
    
    @Test
    public void testOPTIONS() throws ClientProtocolException, IOException
    {
    	HttpOptions request = new HttpOptions(host+":"+port+":/courses/1");
    	HttpResponse response = client.execute(request);
    	System.out.println(response);
    	assertTrue( response.getStatusLine().getStatusCode() == 200);
    }
    
    @Test
    public void testDELETE() throws ClientProtocolException, IOException
    {
    	HttpDelete request = new HttpDelete(host+":"+port+":/courses/1");
    	HttpResponse response = client.execute(request);
    	System.out.println(response);
    	assertTrue( response.getStatusLine().getStatusCode() == 204);
    }
    
    @BeforeClass
    public static void init() throws Exception{
    	//Insert Key
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
	
	    final Sha256Hasher  sha256Hasher = new Sha256Hasher();
	    final HmacSha256Authenticator hmacSha256Authenticator = new HmacSha256Authenticator();
	    hmacSha256Authenticator.getHmacKeyStore().put("jREHMAKey", key);
	    
	    SigningRequestInterceptor sri = new SigningRequestInterceptor("jREHMAKey",hmacSha256Authenticator, sha256Hasher);
	    VerifyingResponseInterceptor vri = new VerifyingResponseInterceptor(hmacSha256Authenticator, sha256Hasher);
	    
	    //Start server
		Thread serverThread = new Thread(new Runnable() {

			@Override
			public void run() {
				server = new SimplejCRHEMAServerSync(port);
				try {
					server.start();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		serverThread.start();

	    //Create Client
	    client = HttpClients.custom().addInterceptorLast(sri).addInterceptorFirst(vri).build();
//	    client = HttpClients.custom().addInterceptorFirst(vri).build();
  
 
    }
    @AfterClass
    public static void finish() throws IOException{
    	server.stop();
    }
    
}
