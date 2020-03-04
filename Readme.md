# jCREHMA
Web applications are nowadays mostly secured by Transport Layer Security (TLS). Protecting messages on transport layer only is, however, not a sufficient safeguard, since the communication flow in web application is often layered by intermediate components such as proxies, caches and load balancers. In layered systems the protection of TLS does not reach from end to end, as messages are unprotected inside an intermediate system. CREHMA aims to ensure end-to-end authenticity and integrity in modern web applications. Unlike other authentication scheme for the web, CREHMA ensure end-to-end security while considering caches. 
jCREHMA is the Java reference implementation of CREHMA. It can be used in any Java-based web framework or library to provide end-to-end security in layered systems. 

# How does CREHMA works
To provide end-to-end authenticity and integrity, CREHMA generates a digital signature over the whole HTTP messages. 

Assuming that this example HTTP message require to be authenticated.
```
GET /courses HTTP/1.1
Accept: application/json
Host: localhost:8088
Connection: Keep-Alive
```
Then the string to be signed is built to:
```
2015-12-01T10:00:13.740Z
GET
/courses
HTTP/1.1
application/json

keep-alive
localhost:8088

47DEQpj8HBSa-_TImW-5JCeuQeRkm5NMpJWZG3hSuFU
```
After signing this string, the signed HTTP message has the following shape.
```
GET /courses HTTP/1.1
Accept: application/json
Host: localhost:8088
Connection: Keep-Alive
Signature: sig=HMAC/SHA256,hash=SHA256,kid=jREHMAKey,tvp=2015-12-01T10:00:13.740Z,addHeaders=null,sv=cbEpt_jMEhA88xtvcnwpeXTA2RdBxbYm2yYiWCXY238
```
## Dependencies
* [Apache HttpComponents 4.5.1](https://hc.apache.org/downloads.cgi)
* [Apache Commons IO 2.4](https://commons.apache.org/proper/commons-io/download_io.cgi)
* [Apache Commons Lang 3.4](https://commons.apache.org/proper/commons-lang/download_lang.cgi)
* [Junit 4.12](https://github.com/junit-team/junit/wiki/Download-and-Install)

# Requirements
Java 1.7 or higher