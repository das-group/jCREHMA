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
package de.thk.das.rest.security.http.crehma.tvp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import de.thk.das.rest.security.http.crehma.NotAuthenticatedExpection;

public class ISO8601TVP implements TimeVariantParameter {
	private long delta;
	
	public ISO8601TVP() {
		delta = 5000;
	}
	
	public ISO8601TVP(long delta){
		this.delta = delta;
	}
	
	public long getDelta() {
		return delta;
	}

	public void setDelta(long delta) {
		this.delta = delta;
	}

	@Override
	public String generate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(new Date());
	}

	@Override
	public boolean verify(String tvp) {
		
		Date date;
		try {
			date = parseTvp(tvp);
			
			long now = new Date().getTime();
			long end = new Date(now+delta).getTime();
			long start = new Date(now-delta).getTime();
			if(date.getTime()<=end  && date.getTime()>=start ){
				return true;
			}
			
			else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
		
		
		
	}
	
	@Override
	public Date parseTvp(String tvp) throws Exception{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date date = null;
		try {
			date = dateFormat.parse(tvp);
		} catch (ParseException e) {
			//e.printStackTrace();
			throw new Exception("Dublicate Signature is not fresh");
		}
		
		return date;
	}

	@Override
	public boolean verifyFrehness(String tvp) {
		// TODO Auto-generated method stub
		return false;
	}

}
