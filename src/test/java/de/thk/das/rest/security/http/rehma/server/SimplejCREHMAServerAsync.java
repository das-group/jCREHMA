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

/**
 * jREHMA test dummy server.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseServer;

import de.thk.das.rest.security.http.crehma.ahc.SigningResponseInterceptor;
import de.thk.das.rest.security.http.crehma.ahc.VerifyingRequestInterceptor;
import de.thk.das.rest.security.http.crehma.hash.Sha256Hasher;
import de.thk.das.rest.security.http.crehma.sig.HmacSha256Authenticator;
public class SimplejCREHMAServerAsync {
	private ListeningIOReactor ioReactor;
	public static void main(String[] args) throws Exception {
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
	    
	    hmacKeyStore.put("jREHMAKey", key);
	    HmacSha256Authenticator hmacSha256Authenticator = new HmacSha256Authenticator(hmacKeyStore);
	    
        HttpProcessor httpProcessor = HttpProcessorBuilder.create()
        		.addFirst(new VerifyingRequestInterceptor(hmacSha256Authenticator,sha256Hasher))
                .add(new ResponseServer("jREHMA-Server"))
                .add(new ResponseContent())
                .add(new RequestContent())
                .add(new ResponseConnControl())
                .addLast(new SigningResponseInterceptor("jREHMAKey",hmacSha256Authenticator,sha256Hasher)).build();
        
		new SimplejCREHMAServerAsync(httpProcessor, 8088).start();
	}
	
	private HttpProcessor httpProcessor;
	private int port;
	public SimplejCREHMAServerAsync(HttpProcessor httpProcessor, int port){
		this.httpProcessor = httpProcessor;
		this.port = port;
	}
	
	public void start() throws Exception{
		UriHttpAsyncRequestHandlerMapper reqistry = new UriHttpAsyncRequestHandlerMapper();
        // Register the default handler for all URIs
        reqistry.register("*", new RequestHandler());
//          reqistry.register("*", new HttpRequestHandler() );
        // Create server-side HTTP protocol handler
        
        HttpAsyncService protocolHandler = new HttpAsyncService(httpProcessor, reqistry) {

            @Override
            public void connected(final NHttpServerConnection conn) {
//                System.out.println(conn + ": connection open");
                super.connected(conn);
            }

            @Override
            public void closed(final NHttpServerConnection conn) {
//                System.out.println(conn + ": connection closed");
                super.closed(conn);
            }

        };
        // Create HTTP connection factory
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory = new DefaultNHttpServerConnectionFactory(ConnectionConfig.DEFAULT);
       
        
        // Create server-side I/O event dispatch
        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        // Set I/O reactor defaults
        IOReactorConfig config = IOReactorConfig.custom()
            .setIoThreadCount(10)
            .setSoTimeout(5000)
            .setConnectTimeout(5000)
            .setTcpNoDelay(true)      
            .build();
        // Create server-side I/O reactor
        ioReactor = new DefaultListeningIOReactor(config);
        try {
            // Listen of the given port
            ioReactor.listen(new InetSocketAddress(port));
            // Ready to go!
            System.out.println("Server running on port "+port);
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex) {
            System.err.println("Interrupted");
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (Exception e) {
        	System.err.println("Signature wrong");
        	
        }
   
        System.out.println("Shutdown");
	}
	
	public void stop() throws IOException{
		ioReactor.shutdown();
	}
}
