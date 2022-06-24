package com.nutech.priceloader.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class Http {
	
	private String username;
	private String password;
	
	public Http() {}
	public Http(String user, String pass) {}

	private static final String USER_AGENT = "Mozilla/5.0";

	public String sendGET(String GET_URL) throws IOException {
		URL obj = new URL(GET_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		String encoded = Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));  //Java 8
		con.setRequestProperty("Authorization", "Basic "+encoded);
		int responseCode = con.getResponseCode();
		
		String responseText="";
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
			responseText=response.toString();
		} else {
			System.out.println("GET request not worked");
		}
		return responseText;
	}

	public String sendPOST(String POST_URL, String POST_PARAMS) throws IOException {
		
		byte[] PARAMS_BYTE = POST_PARAMS.getBytes();
        String encodedString = new String(PARAMS_BYTE, StandardCharsets.UTF_8);
        
        System.out.println(encodedString);
		URL obj = new URL(POST_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
		con.setRequestProperty( "charset", "utf-8");
		con.setRequestProperty( "Content-Length", Integer.toString( encodedString.length() ));
		con.setRequestProperty( "Cache-Control", "no-cache");
		con.setRequestProperty( "Connection", "keep-alive");
		con.setRequestProperty( "Upgrade-Insecure-Requests", "1");
		String encoded = Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));  //Java 8
		con.setRequestProperty("Authorization", "Basic "+encoded);
		
		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(encodedString.getBytes());
		os.flush();
		os.close();
		// For POST only - END

		String responseText="";
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);
		
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
			responseText=response.toString();
		} else {
			System.out.println("POST request not worked");
		}
		return responseText;
	}

}