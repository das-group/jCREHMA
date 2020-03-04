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
package de.thk.das.rest.security.http.crehma.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class SignatureHeaderParser {

	private HashMap<String, String> map = new HashMap<String, String>();
	public String header;

	public SignatureHeaderParser(String header) {
		this.header = header;
	}

	public void parse() {
		StringTokenizer st = new StringTokenizer(header, ",");
		while (st.hasMoreTokens()) {
			String thisToken = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(thisToken, "=");
			map.put(st2.nextToken(), st2.nextToken());
		}
	}

	public String getValue(String key) {
		return map.get(key).toString();
	}
	
	public String getTvp(){
		return map.get("tvp");
	}
	
	public String getSig(){
		return map.get("sig");
	}
	
	public String getHash(){
		return map.get("hash");
	}
	
	public String getSigValue(){
		return map.get("sv");
	}
	
	public String getKid(){
		return map.get("kid");
	}
	
	public List<String> getAddHeaders(){
		String addHeaders = map.get("addHeaders");
		List<String> additionalHeaders = null;
		if(!addHeaders.equals("null")){
			additionalHeaders = Arrays.asList(addHeaders.split(";"));
		}
		
		return additionalHeaders;
	}
}
