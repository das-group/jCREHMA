package de.thk.das.rest.security.http.rehma.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

import de.thk.das.rest.security.http.crehma.ahc.SigningRequestInterceptor;
import de.thk.das.rest.security.http.crehma.ahc.VerifyingResponseInterceptor;
import de.thk.das.rest.security.http.crehma.hash.Sha256Hasher;
import de.thk.das.rest.security.http.crehma.sig.HmacSha256Authenticator;

public class SizeTest {
	public static void main(String[] args) throws IOException, InterruptedException {
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
	    hmacSha256Authenticator.getHmacKeyStore().put("jREHMAKey", key);
	    
	    SigningRequestInterceptor sri = new SigningRequestInterceptor("jREHMAKey",hmacSha256Authenticator, sha256Hasher);
	    VerifyingResponseInterceptor vri = new VerifyingResponseInterceptor(hmacSha256Authenticator, sha256Hasher);
		
	    HttpClient client = HttpClients.custom().addInterceptorLast(sri).addInterceptorFirst(vri).build();
	    
	    String host = "139.6.102.29";

		host = "d5esw3092ua6w.cloudfront.net";
//		host = "ec2-34-252-120-148.eu-west-1.compute.amazonaws.com";
		
		long abl = 5;
		long numberRequestPerStep = 20;
		long numberOfSteps = 10;
		long stepSize = 100000;
		long start = 0;
		long end = 0;
		long delta = 0;
		long contentLengths[] = new long[(int)numberOfSteps]; 
		
		HttpResponse response = null;
		Map<Long, long[]> map = new HashMap<Long, long[]>();
		Long contentLength = 0l;
		
		
		for (int i = 0; i < numberOfSteps; i++) {
			long requestId = new Date().getTime();
			long[] times = new long[(int) (numberRequestPerStep-1)];
			for (int j = 0; j < numberRequestPerStep; j++) {
				HttpGet getRequest =  new HttpGet("http://"+host+"/rsc/"+requestId);
				//HttpCoreContext ctx = new HttpCoreContext();
				getRequest.setHeader("Accept", "application/json");
				getRequest.setHeader("X-Response","cc:max-age=3600;abl:"+abl);
				getRequest.setHeader("Create-Signature","false");
				if(j == 0) {
					//client = HttpClients.custom().addInterceptorLast(sri).addInterceptorFirst(vri).build();
					client = HttpClients.custom().build();
					start = new Date().getTime();
					response = client.execute(getRequest);
					end = new Date().getTime();
					delta = end - start;
//					System.out.println(delta);
					contentLength = response.getEntity().getContentLength();
//					System.out.println(response);
//					System.out.println(contentLength);
//					System.out.println(EntityUtils.toString(response.getEntity()));
					contentLengths[i] = contentLength;
				} else {
					
					//client = HttpClients.custom().addInterceptorLast(sri).addInterceptorFirst(vri).build();
					client = HttpClients.custom().build();
					start = new Date().getTime();
					response = client.execute(getRequest);
					end = new Date().getTime();
					delta = end - start;
					//System.out.println(delta);
					times[j-1] = delta;
				}
				Thread.sleep(1000);
				map.put(contentLength, times);
				
			}
			abl+=stepSize;
		}
		
		String csvString = "";
		
		
		for (int i = 0; i < numberOfSteps; i++) {
			csvString += contentLengths[i] + "\t";
		}
		csvString+="\n";
		for (int j = 0; j < (numberRequestPerStep - 1); j++) {
			for (int i = 0; i < numberOfSteps; i++) {
				csvString +=map.get(contentLengths[i])[j]+ "\t";
			}
			csvString+="\n";
		}
		
		System.out.println(csvString);
		PrintWriter out = new PrintWriter("size_test"+new Date().getTime()+".csv");
		out.print(csvString);
		out.close();
		
	}
}
