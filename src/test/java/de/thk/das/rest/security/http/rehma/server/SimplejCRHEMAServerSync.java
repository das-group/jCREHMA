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
package de.thk.das.rest.security.http.rehma.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;

import de.thk.das.rest.security.http.crehma.ahc.SigningResponseInterceptor;
import de.thk.das.rest.security.http.crehma.ahc.VerifyingRequestInterceptor;
import de.thk.das.rest.security.http.crehma.hash.Sha256Hasher;
import de.thk.das.rest.security.http.crehma.sig.HmacSha256Authenticator;

/**
 * jREHMA HTTP test server 
 */
public class SimplejCRHEMAServerSync {

	private int port;
	private HttpServer server;
	
    public static void main(String[] args) throws Exception {
       SimplejCRHEMAServerSync server = new SimplejCRHEMAServerSync(8088);
       server.start();
    }
    
    public SimplejCRHEMAServerSync(int port){
    	this.port = port;
    }
    
    public SimplejCRHEMAServerSync(int port, HttpServer server){
    	this(port);
    	this.server = server;
    }
    
    public void start() throws IOException, InterruptedException{
    	if(server == null){
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
    	    Sha256Hasher sha256Hasher= new Sha256Hasher();
    	    HashMap<String, byte[]> hmacKeyStore = new HashMap<String, byte[]>();
    	    
    	    hmacKeyStore.put("jCREHMAKey", key);
    	    HmacSha256Authenticator hmacSha256Authenticator = new HmacSha256Authenticator(hmacKeyStore);

            SocketConfig socketConfig = SocketConfig.custom()
                    .setSoTimeout(15000)
                    .setTcpNoDelay(true)
                    .build();

            server = ServerBootstrap.bootstrap()
                    .setListenerPort(port)
                    .setServerInfo("Test/1.1")
                    .setSocketConfig(socketConfig)
                    .registerHandler("*", new RequestHandlerSync())
                    .addInterceptorFirst(new VerifyingRequestInterceptor(hmacSha256Authenticator, sha256Hasher))
                    .addInterceptorLast(new SigningResponseInterceptor("jCREHMAKey", hmacSha256Authenticator, sha256Hasher))
                    .create();
    	}
    	server.start();
    	server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    	System.out.println("Server running at port "+port);
    	
    	Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown(5, TimeUnit.SECONDS);
            }
        });
    }
    
    public void stop(){
    	server.stop();
    	System.out.println("Server stopped");
    }


}