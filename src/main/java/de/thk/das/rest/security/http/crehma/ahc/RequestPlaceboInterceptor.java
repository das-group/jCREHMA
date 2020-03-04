package de.thk.das.rest.security.http.crehma.ahc;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class RequestPlaceboInterceptor implements HttpRequestInterceptor {

	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		// TODO Auto-generated method stub

	}

}
