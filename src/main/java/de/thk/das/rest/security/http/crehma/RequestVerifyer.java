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

import java.util.List;

import de.thk.das.rest.security.http.crehma.client.request.HttpBasicRequest;
import de.thk.das.rest.security.http.crehma.hash.BodyHasher;
import de.thk.das.rest.security.http.crehma.sig.TbsAuthenticator;
import de.thk.das.rest.security.http.crehma.utils.SignatureHeaderParser;

public class RequestVerifyer extends RequestAuthentication {

	public RequestVerifyer() {
		super();
	}

	public RequestVerifyer(TbsAuthenticator tbsAuthenticator,
			BodyHasher bodyHasher) {
		this();
		addBodyHasher(bodyHasher);
		addTbsAuthenticator(tbsAuthenticator);
	}

	public RequestVerifyer(List<TbsAuthenticator> tbsAuthenticators,List<BodyHasher> bodyHashers) {
		this();
		addTbsAuthenticators(tbsAuthenticators);
		addBodyHashers(bodyHashers);
	}

	public void verify(HttpBasicRequest req) throws Exception {
		if (req.containsHeader("Signature")) {

			SignatureHeaderParser parser = new SignatureHeaderParser(req
					.getFirstHeader("Signature").getValue());
			parser.parse();
			String tvp = parser.getTvp();
			if (!getTimeVariantParameter().verify(tvp)) {
				throw new NotAuthenticatedExpection("Invalid TVP");
			}
			String kid = parser.getKid();
			String sv = parser.getSigValue();
			String hash = parser.getHash();
			String sig = parser.getSig();
			List<String> additionalHeaders = parser.getAddHeaders();

			String tbs = buildTbs(req, hash, tvp, additionalHeaders);
		
			TbsAuthenticator tbsAuthenticator = getTbsAuthenticatorsHashMap().get(sig);

			if(tbsAuthenticator == null){
				throw new UnsupportedSignatureAlgorithm();
			}
			
			if (!tbsAuthenticator.verify(kid, tbs, sv)) {
				throw new NotAuthenticatedExpection("Invalid Signature");
			}
		}

		else {

			throw new NotAuthenticatedExpection("No Signature Header");
		}
	}
}
