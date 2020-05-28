# jCREHMA
CREHMA stands for Cache-ware REST-ful HTTP Message Authentication. It is an end-to-end authenticity and integrity scheme for HTTP messages that can be used in conjunction with web caches.  To ensure end-to-end authenticity and integrity, CREHMA builds a signature over the whole HTTP message. Response messages can also be cached and modified by intermediate systems without running into the risk that the signature value is classified as invalid or a replay attack.  To do so, each signed response message  includes a signature freshness. With this value clients can verify whether a response is reused by a legitimate cache.
CREHMA.js is designed to be used in any Javascript-based environemt such as Node, vert.x or in the Webbrowser. 

# How does it works?
CREHMA creates a digital signature over the whole HTTP message by concatenating the security-critical headers and the body to a string. This concatenated string is then signed by a given key. The signature value in conjunction with signature meta data is then included the Signature header. The other endpoint, i.e client or server, verifies the message's authenticity and integrity by validating the signature value in the Signature header with a given key. 

The figure below shows an example message flow which protected by CREHMA. 
<!-- ![CREHMA protected message flow](https://github.com/hvnguyen86/crehma.js/blob/master/images/message_flow_fl.png "CREHMA protected message flow") -->
![CREHMA protected message flow](https://github.com/hvnguyen86/crehma.js/blob/master/images/CREHMA_message_flow_fl.png "CREHMA protected message flow")
<!-- <img src="https://github.com/hvnguyen86/crehma.js/blob/master/images/CREHMA_message_flow_fl.png" alt="Kitten"
	width="50%" />
<img src="https://github.com/hvnguyen86/crehma.js/blob/master/images/CREHMA_message_flow_fl.png" alt="Kitten"
	width="50%" /> -->
The appended Signature header is marked in bold. CREHMA are compatible  with caches. Signed response messages can be stored and reused by caches as shown be the figure. If client received a reused response message from the cache then it needs to verify signature freshness in addition to the signature value. In this example, the signature freshness is calculated from the time stamp of tvp parameter and the max-age value of the Cache-Control header. The seconds in max-age (360) are then added to the tvp value (). 

# How to integrate CREHMA in your web application?

```
2019-06-13T15:45:10.494Z + 360 <= <current_time>
```
The signature freshness is valid, if the sum is lower or equal then/to the current time.

For more information please read our corresponding paper: https://doi.org/10.1145/3374664.3375750

# How does it works?

Let's assume that the following HTTP request message 

```
GET / HTTP/1.1
Accept: application/json
Host: localhost:3000
```
Then the concatenated string to be signed is built to:
```
2020-03-30T08:58:18.939Z
GET
/
HTTP/1.1
application/json


localhost:3000


47DEQpj8HBSa-_TImW-5JCeuQeRkm5NMpJWZG3hSuFU
```
After signing this string, the signed HTTP request message has the following shape and will be sent to the origin server.
```
GET / HTTP/1.1
Accept: application/json
Host: localhost:3000
Signature: sig=HMAC/SHA256,hash=SHA256,kid=jCREHMAKey,tvp=2020-03-30T08:58:18.939Z,addHeaders=null,sv=-BISmytcqhowT27-iaE9jUKahCzG95n96z8vsI5MI0E
```

The origin server verifies this request message based on the information included in the Signature header. It  builds a string according to same concatenation order as the client. This string will verified by a corresponding verification key.  
In case the signature is valid, the origin server will generate the following response message:

```
HTTP/1.1 200 OK
Cache-Control: max-age=3600
Content-Length: 38
Content-Type: text/plain
ETag: BOpGqmfVxx

Valid Request Signature 1585558698960

```

The concatenated string to be signed is then build as follows. Note that the 


```
2020-03-30T08:58:18.960Z
GET localhost:3000/
HTTP/1.1
200
max-age=3600
38
text/plain
BOpGqmfVxx




Ogsxz6JAfkmeZ_AcITK-KxDbBgVel9deG9XBH8YMORg
```

After generating the signature value, the origin server appends a Signature header to the returned response message which is sent to the client.

```
HTTP/1.1 200 OK
Cache-Control: max-age=3600
Content-Length: 38
Content-Type: text/plain
ETag: BOpGqmfVxx
Signature: sig=HMAC/SHA256,hash=SHA256,kid=jCREHMAKey,tvp=2020-03-30T08:58:18.960Z,addHeaders=null,sv=HHeADsjjh_LGG0VLbrbyQ5Gu4-UKnkGa9rkkib4uCHE

Valid Request Signature 1585558698960

```

Based on the Signature header, the client validates the request message's authenticity and integrity. 

jREHMA includes 4 main classes for signing and verifying HTTP messages: RequestSigner, ResponseVerifyer, ResponseSigner and RequestVerifyer. As the name implies, RequestSigner is considered to be used in HTTP clients to sign HTTP requests and ResponseVerifyer can be used to verify incoming HTTP responses. The other two classes, ResponseSigner and RequestVerifyer can be used by Webservers or REST-based Web Services to authenticate HTTP requests  and protect HTTP responses against man-in-the-middle attacks. 

These four classes are considered to be used in interceptors (in some frameworks also called as filters) to sign and verify traversing HTTP messages. Note, that the interceptor must have access to the whole HTTP message in order the sign all security-relevant header fields and the body. If your framework does not support interceptors or its interceptors can not access the whole HTTP message, jREHMA can also be used in a proxy. 
Using jREHMA in proxies should only be taken into consideration, if interceptors are not provided, as proxies do not ensure end-to-end security. 

## Using jREHMA in interceptors
Many frameworks for developing Web Applications and Web Services provide interceptors for pre- or post-processing HTTP messages. Usually interceptor are java interfaces which can be implemented by any class.

So, one way of using jREHMA in interceptors is to extend the four main classes of jREHMA and simultaneously implement the interceptor interface. 
We have already implemented 4 interceptors for Apache HttpComponents: SigningRequestInterceptor, VerifyingResponseInterceptor, SigningResponseInterceptor and VerifyingRequestInterceptor.

In the following we demonstrate the integration of jREHMA in Apache HTTPComponents interceptors. By means of this example, the integration of jREHMA in other interceptors of other Web frameworks and proxies can be adopted likewise. 
### Using jREHMA in Apache HTTPComponents
Apache HTTPComponents is a Java library offering a toolset for implementing low level HTTP components. The following example shows how to incorporate the RequestSigner into an Apache HTTPComponents HttpRequestInterceptor.
```java
public class SigningRequestInterceptor extends RequestSigner implements HttpRequestInterceptor {
  private String kid;
  private String hash;
  private String sig;
  private ArrayList<String> additionalHeaders;
	
  public SigningRequestInterceptor(){
    super();
  }
	
  public SigningRequestInterceptor(String kid, TbsAuthenticator tbsAuthenticator, BodyHasher bodyHasher) {
    super(tbsAuthenticator, bodyHasher);
    this.sig = tbsAuthenticator.getName();
    this.hash = bodyHasher.getName();
    this.kid = kid;
    this.additionalHeaders = new ArrayList<String>();
  }

  @Override
  public void process(HttpRequest request, HttpContext context)  throws HttpException, IOException {
    //Set HTTP-Method for VerifyingResponseInterceptor
    context.setAttribute("Method",request.getRequestLine().getMethod());
		
    HttpBasicRequest basicRequest = new HttpBasicRequest(request.getRequestLine().getUri(),
    request.getRequestLine().getMethod());
    basicRequest.setHeaders(request.getAllHeaders());
    byte[] body = new byte[0];		
    
    if(request instanceof HttpEntityEnclosingRequest){
      body = IOUtils.toByteArray(((HttpEntityEnclosingRequest) request).getEntity().getContent());
    }
    basicRequest.setEntity(new ByteArrayEntity(body));
		
    try {
      sign(basicRequest, this.sig, this.kid, this.hash, this.additionalHeaders);
    } catch (Exception e) {
      e.printStackTrace();
    }
    request.setHeaders(basicRequest.getAllHeaders());
  }
  // Getters and Setters
  ...
}
```
The 4 instance variables will be used later by the client to define and describe the signature generation process: kid specifies the key id, hash defines the hash algorithm name, sig describes the signature algorithm name and additionalHeaders informs the signature generation process whether and which additional security-relevant header fields need to be signed.

The first constructor has not any arguments. Here, only the super method is invoked. The second constructor has three arguments which encompass the key id, a TbsAuthenticator and a BodyHasher.
TbsAuthenticator is an interface where one can define and implement the signature algorithm which signs or verifies HTTP messages. Bodyhasher represents an interface which is used to define the hash algorithm to hash the body. Further information on defining and including signature and hash algorithm can be found here:

[Defining and signature and hash algorithm](https://gitlab.com/thk.das/jREHMA/wikis/defining_signature_and_hash_algorithm)

The process method is the to be implemented function of the HttpRequestInterceptor interface. Here we can process the request, e.g, sign it. The first thing to do is to store the request method in the HttpContext object. This object enables sharing information among various related components such as other interceptors. Later, the VerifyingResponseInterceptor will use the stored method of the HttpContext object to check whether the response contains the required header fields which is needed by the corresponding request. 

The next step is to map the incoming request into HttpBasicRequest object which is the format used by jREHMA to process the request.

This HttpBasicRequest object is given together with sig, kid, hash and additionHeaders to the sign method. As name implies this method signs the request according to information provided by the arguments. It also appends mandatory header fields in case they were absent. 

After signing the request, the request header fields are reset. Now, the header fields also include the new header field Signature which includes the signature value of the request along with the signature description including the key id, signature as well as hash algorithm name, a time stamp, and the additional header fields to be signed, if any. 

The implementation of three other classes VerifyingResponseInterceptor, SigningResponseInterceptor and VerifyingRequestInterceptor are similar. For details please take a look at these classes:

[SigningRequestInterceptor](https://github.com/das-th-koeln/jCREHMA/blob/master/src/main/java/de/thk/das/rest/security/http/crehma/ahc/SigningRequestInterceptor.java)

[VerifyingResponseInterceptor](https://github.com/das-th-koeln/jCREHMA/blob/master/src/main/java/de/thk/das/rest/security/http/crehma/ahc/VerifyingResponseInterceptor.java)

[SigningResponseInterceptor](https://github.com/das-th-koeln/jCREHMA/blob/master/src/main/java/de/thk/das/rest/security/http/crehma/ahc/SigningResponseInterceptor.java)

[VerifyingRequestInterceptor](https://github.com/das-th-koeln/jCREHMA/blob/master/src/main/java/de/thk/das/rest/security/http/crehma/ahc/VerifyingRequestInterceptor.java)

The following Listing depicts how to include SigningRequestInterceptor and VerifyingResposeInterceptor in an Apache HttpComponents client. 
```java
String base64Key = "fJW7ebII2E4RU3...";
byte[] key = Base64.decodeBase64(base64Key);

Sha256Hasher  sha256Hasher = new Sha256Hasher();
HmacSha256Authenticator hmacSha256Authenticator = new HmacSha256Authenticator();
hmacSha256Authenticator.getHmacKeyStore().put("jREHMAKey", key);

SigningRequestInterceptor sri = new SigningRequestInterceptor("jREHMAKey",hmacSha256Authenticator, sha256Hasher);
VerifyingResponseInterceptor vri = new VerifyingResponseInterceptor(hmacSha256Authenticator, sha256Hasher);

HttpClient client = HttpClients.custom().addInterceptorLast(sri).addInterceptorFirst(vri).build();
HttpGet request = new HttpGet("http://localhost:8081/courses");
request.setHeader("Accept", "application/json");
HttpResponse response = client.execute(request);
```
In this example, the client utilizes Sha256Hasher and HmacSha256Authenticator. These two classes are instances of BodyHasher and TbsAuthenticator providing a SHA-256 cryptographic hash for the body and an HMAC-SHA256 signature for the HTTP request. The complete code of this test client can be found here:

[HttpTestClient](https://github.com/das-th-koeln/jCREHMA/blob/master/src/test/java/de/thk/das/rest/security/http/rehma/client/HttpTestClient.java)

A test server using SigningResponseInterceptor and VerifyingRequestInterceptor is also included in jRHEMA as well. The complete code of the server is available here:

[SimplejCRHEMAServerSync](https://github.com/das-th-koeln/jCREHMA/blob/master/src/test/java/de/thk/das/rest/security/http/rehma/server/SimplejCRHEMAServerSync.java)

## Dependencies
* [Apache HttpComponents 4.5.1](https://hc.apache.org/downloads.cgi)
* [Apache Commons IO 2.4](https://commons.apache.org/proper/commons-io/download_io.cgi)
* [Apache Commons Lang 3.4](https://commons.apache.org/proper/commons-lang/download_lang.cgi)
* [Junit 4.12](https://github.com/junit-team/junit/wiki/Download-and-Install)

# Requirements
Java 1.7 or higher
