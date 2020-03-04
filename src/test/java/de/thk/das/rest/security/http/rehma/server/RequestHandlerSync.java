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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
public class RequestHandlerSync implements HttpRequestHandler {
	private static List<String> supportedMethods;
	private static String course1;
	private static String course2;
	private static String courses;
	private static String jsonMediaType;
	private static String textMediaType;
	public RequestHandlerSync() {
		super();
		supportedMethods = new ArrayList<String>();
		supportedMethods.add("GET");
		supportedMethods.add("POST");
		supportedMethods.add("PUT");
		supportedMethods.add("PATCH");
		supportedMethods.add("OPTIONS");
		supportedMethods.add("DELETE");
		course1 = "{\"id\":1,\"name\":\"Computer Science\",\"term\":\"summer\"}";
		course2 = "{\"id\":2,\"name\":\"Mathematics\",\"term\":\"winter\"}";
		courses = "[{\"id\":1,\"name\":\"Computer Science\",\"term\":\"summer\"},{\"id\":2,\"name\":\"Mathematics\",\"term\":\"winter\"}]";
		jsonMediaType = "application/json";
		textMediaType = "text/plain";
		// / this.docRoot = docRoot;
	}
	public HttpAsyncRequestConsumer<HttpRequest> processRequest(
			final HttpRequest request, final HttpContext context) {
			return new BasicAsyncRequestConsumer();
	}
	public void handle(HttpRequest request,
			HttpResponse response, final HttpContext context)
			throws HttpException, IOException {
		handleInternal(request, response, context);
	}
	private void handleInternal(final HttpRequest request,
			final HttpResponse response, final HttpContext context)
			throws HttpException, IOException {	
		if(context.getAttribute("exception")==null){	
			String method = request.getRequestLine().getMethod().toUpperCase();
			if (!supportedMethods.contains(method.toUpperCase())) {
				throw new MethodNotSupportedException(method + " method not supported");
			}		
			NStringEntity body = new NStringEntity("Not Found");
			response.setHeader(HttpHeaders.CONTENT_TYPE,textMediaType);
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			String uri = request.getRequestLine().getUri();
			if (method.equals("GET")) {
				if (uri.equals("/courses") || uri.equals("/courses/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					body = new NStringEntity(courses,ContentType.create(jsonMediaType));
					response.setEntity(body);
				}
				else if (uri.equals("/courses/1") || uri.equals("/courses/1/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					body = new NStringEntity(course1,ContentType.create(jsonMediaType));
					response.setEntity(body);
				}	
				else if (uri.equals("/courses/2") || uri.equals("/courses/2/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					body = new NStringEntity(course2,ContentType.create(jsonMediaType));
					response.setEntity(body);
				}
				else {
					response.setEntity(body);
				}
			}	
			else if (method.equals("HEAD")) {
				if (uri.equals("/courses") || uri.equals("/courses/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					body = new NStringEntity("", ContentType.create(jsonMediaType));
					response.setEntity(body);
				}	
				else if (uri.equals("/courses/1") || uri.equals("/courses/1/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					response.setHeader("Content-Length", course1.length() + "");
					response.setHeader("Content-Type", jsonMediaType);
				}
				else if (uri.equals("/courses/2") || uri.equals("/courses/2/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					response.setHeader("Content-Length", course1.length() + "");
					response.setHeader("Content-Type", jsonMediaType);
				} else {
					response.setEntity(body);
				}
			}
			else if (method.equals("PUT") || method.equals("PATCH")) {
				if (uri.equals("/courses") || uri.equals("/courses/")) {
					response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
				}
				else if (uri.equals("/courses/1") || uri.equals("/courses/1/")) {
					response.setStatusCode(HttpStatus.SC_NO_CONTENT);
				}
				else if (uri.equals("/courses/2") || uri.equals("/courses/2/")) {
					response.setStatusCode(HttpStatus.SC_NO_CONTENT);
				} else {
					response.setEntity(body);
				}
			}
			else if (method.equals("POST")) {
				if (uri.equals("/courses") || uri.equals("/courses/")) {
					response.setStatusCode(HttpStatus.SC_CREATED);
					response.setHeader("Location", "https://example.org/courses/3");
				}
				else if (uri.equals("/courses/1") || uri.equals("/courses/1/")) {
					response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
				}
				else if (uri.equals("/courses/2") || uri.equals("/courses/2/")) {
					response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
				}
			}
			else if (method.equals("OPTIONS")) {
				if (uri.equals("/courses") || uri.equals("/courses/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					response.setHeader("Allow", "GET,HEAD,POST,OPTIONS");
				}
				else if (uri.equals("/courses/1") || uri.equals("/courses/1/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					response.setHeader("Allow", "GET,HEAD,PUT,PATCH,OPTIONS");
				}
				else if (uri.equals("/courses/2") || uri.equals("/courses/2/")) {
					response.setStatusCode(HttpStatus.SC_OK);
					response.setHeader("Allow", "GET,HEAD,PUT,PATCH,OPTIONS");
				} else {
					response.setEntity(body);
				}
			}
			else if (method.equals("DELETE")) {
				if (uri.equals("/courses") || uri.equals("/courses/")) {
					response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
				}
				else if (uri.equals("/courses/1") || uri.equals("/courses/1/")) {
					response.setStatusCode(HttpStatus.SC_NO_CONTENT);
				}
				else if (uri.equals("/courses/2") || uri.equals("/courses/2/")) {
					response.setStatusCode(HttpStatus.SC_NO_CONTENT);
				} 
			}
		} else {
			response.setStatusCode(HttpStatus.SC_FORBIDDEN);
			response.setEntity(new NStringEntity(context.getAttribute("exception").toString()));
		}
	}

}
