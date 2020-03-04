package de.thk.das.rest.security.http.crehma.utils;

public class HttpUtils {
	public static long getMaxAge(String maxAgeString){

		long maxAge = 0l;
		if(maxAgeString.contains("max-age=")){

			String[] paramsArray = maxAgeString.split(",");
			for (String param : paramsArray) {
				String[] keyValueArray = param.split("=");
				if(keyValueArray[0].equals("max-age")){
					maxAge = Long.parseLong(keyValueArray[1]);


				}

			}
		}


		return maxAge;
	};

	public static long getSMaxAge(String maxAgeString){
		long sMaxAge = 0l;
		if(maxAgeString.contains("s-maxage=")){

			String[] paramsArray = maxAgeString.split(",");
			for (String param : paramsArray) {
				String[] keyValueArray = param.split("=");
				if(keyValueArray[0].equals("s-maxage")){
					sMaxAge = Long.parseLong(keyValueArray[1]);


				}

			}
		}

		return sMaxAge;
	};
}
