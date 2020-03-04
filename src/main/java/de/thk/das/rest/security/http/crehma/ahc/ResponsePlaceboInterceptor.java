package de.thk.das.rest.security.http.crehma.ahc;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

public class ResponsePlaceboInterceptor implements HttpResponseInterceptor {

	@Override
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		// TODO Auto-generated method stub

	}

}
