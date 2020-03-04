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

public class UnsupportedHashAlgorithm extends UnsupportedAlgorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 853585923934245851L;
	
	public UnsupportedHashAlgorithm(String message) {
		super(message);
	}
	
	public UnsupportedHashAlgorithm(){
		super("Unsupported Hash Algorithm");
	}

}
