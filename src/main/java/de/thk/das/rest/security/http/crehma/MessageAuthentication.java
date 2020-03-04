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
package de.thk.das.rest.security.http.crehma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.thk.das.rest.security.http.crehma.hash.BodyHasher;
import de.thk.das.rest.security.http.crehma.sig.TbsAuthenticator;
import de.thk.das.rest.security.http.crehma.tvp.ISO8601TVP;
import de.thk.das.rest.security.http.crehma.tvp.TimeVariantParameter;

public abstract class MessageAuthentication{
	public static final String SIGNATURE_HEADER = "sig=%s,hash=%s,kid=%s,tvp=%s,addHeaders=%s,sv=%s";
	private List<String> basicHeaders;
	public List<String> getBasicHeaders() {
		return basicHeaders;
	}

	private HashMap<String, TbsAuthenticator> tbsAuthenticatorsHashMap;
	private HashMap<String, BodyHasher> bodyHashersHashMap;
	private TimeVariantParameter timeVariantParameter;
	
	protected MessageAuthentication() {
		bodyHashersHashMap = new HashMap<String, BodyHasher>();
		tbsAuthenticatorsHashMap = new HashMap<String, TbsAuthenticator>();
		basicHeaders = new ArrayList<String>();
		
		this.timeVariantParameter = new ISO8601TVP();
	}
	
	public TimeVariantParameter getTimeVariantParameter() {
		return timeVariantParameter;
	}

	public void setTimeVariantParameter(TimeVariantParameter timeVariantParameter) {
		this.timeVariantParameter = timeVariantParameter;
	}

	public void addTbsAuthenticator(TbsAuthenticator tbsAuthenticator){
		tbsAuthenticatorsHashMap.put(tbsAuthenticator.getName(), tbsAuthenticator);
	}
	
	public void addTbsAuthenticators(List<TbsAuthenticator> tbsAuthenticators) {
		for (TbsAuthenticator tbsAuthenticator : tbsAuthenticators) {
			tbsAuthenticatorsHashMap.put(tbsAuthenticator.getName(), tbsAuthenticator);
		}
	}

	public void addBodyHasher(BodyHasher bodyHasher){
		bodyHashersHashMap.put(bodyHasher.getName(), bodyHasher);
	}
	
	public void addBodyHashers(List<BodyHasher> bodyHashers) {
		for(BodyHasher bodyHasher: bodyHashers){
			bodyHashersHashMap.put(bodyHasher.getName(), bodyHasher);
		}
	}
	
	public void setBasicHeaders(List<String> basicHeaders) {
		this.basicHeaders = basicHeaders;
	}

	public HashMap<String, TbsAuthenticator> getTbsAuthenticatorsHashMap() {
		return tbsAuthenticatorsHashMap;
	}

	public void setTbsAuthenticatorsHashMap(
			HashMap<String, TbsAuthenticator> tbsAuthenticatorsHashMap) {
		this.tbsAuthenticatorsHashMap = tbsAuthenticatorsHashMap;
	}

	public HashMap<String, BodyHasher> getBodyHashersHashMap() {
		return bodyHashersHashMap;
	}

	public void setBodyHashersHashMap(HashMap<String, BodyHasher> bodyHashersHashMap) {
		this.bodyHashersHashMap = bodyHashersHashMap;
	}
	

}
