package com.cimpa.x3dexperience;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;

public class X3dServerAuthenticationNew {
	
	public JSONObject authenticate(HttpServletRequest req, HttpServletResponse res ) throws Exception {
		
		HttpSession session = req.getSession(true);
		
        Properties prop = new Properties();
        InputStream input = null;
        String x3d_server_name = "";
        String sResponseObject = "";
        InputStreamReader in = null;
        BufferedReader br = null;
        String sLine = "";
        PrintWriter out;
        JSONParser jsonParser = new JSONParser();
        
        JSONObject returnJson = new JSONObject(); 
        
        //get Json POST data
    	JSONObject jReconnectPostBody = (JSONObject) session.getAttribute("ReconnectPostReqBody");
    	String strJsonReconnectPostBody = jReconnectPostBody.toString();
    	//System.out.println("strJsonReconnectPostBody = "+strJsonReconnectPostBody);
        
    	//out = res.getWriter();
        input = getClass().getClassLoader().getResourceAsStream("config.properties");
        
        // load a properties file
        prop.load(input);
        
        // get the property value and print it out	        
        x3d_server_name = prop.getProperty("x3d_server_name");       
        String sUserName = prop.getProperty("login_username");
        String sUserPwd = prop.getProperty("login_userpwd");
        String sUserRole = prop.getProperty("login_role");
        
        String sLoginTicketUrl = "https://"+x3d_server_name+"/3dpassport/login?action=get_auth_params";
        
		Map hmConn1HeaderFields = null;
		List<String> lCookie = null;
		boolean bSkipCSRF = false;
		String sCSRFTokenString = null;
		
        // Create a CookieManager with default cookie policy
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        // Set the default CookieManager
        CookieHandler.setDefault(cookieManager);

        //HttpClient httpClient = HttpClient.newBuilder().build();
        HttpClient httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();
        
        // First HTTP request
        HttpRequest firstRequest = HttpRequest.newBuilder()
                .uri(new URI(sLoginTicketUrl))
                .GET()
                .build();
        
        // Send the first request and retrieve the response
        HttpResponse<String> firstResponse = httpClient.send(firstRequest, HttpResponse.BodyHandlers.ofString());
        
        // Get cookies from the first response
        Map<String, List<String>> firstResponseHeaders = firstResponse.headers().map();
        //System.out.println("firstResponseHeaders = "+firstResponseHeaders);
        
        /*
        List<String> cookies1 = firstResponseHeaders.getOrDefault("Set-Cookie", List.of());
        System.out.println("cookies1 = "+cookies1);
        */
        
        // Get cookies from the CookieManager
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        
        //Prepare cookies for the second request
        StringBuilder cookieHeaderBuilder = new StringBuilder();
        for (HttpCookie cookie : cookies) {
            cookieHeaderBuilder.append(cookie.toString()).append("; ");
        }
        String cookieHeader = cookieHeaderBuilder.toString();
        //System.out.println("initially cookieHeader = "+cookieHeader);

        String sGetLoginTktResp = firstResponse.body();
        //System.out.println("sGetLoginTktResp = "+sGetLoginTktResp);
        
        if( sGetLoginTktResp.contains("lt") && sGetLoginTktResp.contains("LT-") ) {
        	sGetLoginTktResp = sGetLoginTktResp.substring(sGetLoginTktResp.indexOf(","), sGetLoginTktResp.length()-1);
        	sGetLoginTktResp = sGetLoginTktResp.substring(sGetLoginTktResp.indexOf(":")+1, sGetLoginTktResp.length());
            
            String sLoginTicket = sGetLoginTktResp.replaceAll("\"", "");
            System.out.println("sLoginTicket = "+sLoginTicket); 
            
            
            //STEP 2:: CAS Auth
            String sCASAuthPOSTBody = "lt="+sLoginTicket+"&username="+sUserName+"&password="+sUserPwd;
            String sCasAuthUrl = "https://"+x3d_server_name+"/3dpassport/login";
            String sCSRFTokenURL = "https://"+x3d_server_name+"/3dspace/resources/v1/application/CSRF";
            
            String sImportReqsURL = "https://"+x3d_server_name+"/3dspace/reconnect/PostRequirementsCAS";
            //String s3DSpaceURL = "https://"+x3d_server_name+"/3dspace";
            
            //String cookieHeader2 = String.join("; ", cookies1);
            
            // Second HTTP request with cookies from the first response
            HttpRequest secondRequest = HttpRequest.newBuilder()
                    .uri(new URI(sCasAuthUrl))
                    .header("Cookie", cookieHeader) // Attach cookies to the second request
                    //.header("Accept", "*/*")  // Add Accept header
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8") // Modified Content-Type header
                    //.header("Content-Type", "text/plain") // Add Content-Type header
                    .POST(HttpRequest.BodyPublishers.ofString(sCASAuthPOSTBody))
                    .build();
            
            // Send the second request and retrieve the response
            HttpResponse<String> secondResponse = httpClient.send(secondRequest, HttpResponse.BodyHandlers.ofString());
            
            int iSecondRespCode = secondResponse.statusCode();
            System.out.println("iSecondRespCode = "+iSecondRespCode);
            
            String sCASAuthResp = secondResponse.body();
            System.out.println("sCASAuthResp = "+sCASAuthResp);
            
            // Get cookies from the first response
            Map<String, List<String>> secondResponseHeaders = secondResponse.headers().map();
            System.out.println("secondResponseHeaders = "+secondResponseHeaders);

            // Check if the response is a redirection (HTTP 302)
            if (secondResponse.statusCode() == 302) {
            	
                // Retrieve the redirection URL from the Location header
                String redirectionUrl = secondResponse.headers().firstValue("Location").orElse(null);                
                redirectionUrl = "https://"+x3d_server_name+redirectionUrl;
                //System.out.println("fully constructed :: redirectionUrl = "+redirectionUrl);
                
                /*
                redirectionUrl += "?service=";
                String encodedCSRFUrl = URLEncoder.encode(sCSRFTokenURL, "UTF-8");
                redirectionUrl += encodedCSRFUrl;
                */
                
                cookies = cookieManager.getCookieStore().getCookies();                
                for (HttpCookie cookie : cookies) {
                    cookieHeaderBuilder.append(cookie.toString()).append("; ");
                }                
                cookieHeader = cookieHeaderBuilder.toString();
                System.out.println("after CAS Auth redirect:: cookies list = "+cookieHeader);
                
                
                if (redirectionUrl != null) {
                    // Create a new request for the redirection URL
                    HttpRequest redirectedRequest = HttpRequest.newBuilder()
                            .uri(new URI(redirectionUrl))
                            .GET()
                            .header("Cookie", cookieHeader)
                            .header("Accept", "Application/*")
                            .build();

                    // Send the redirected request and retrieve the final response
                    secondResponse = httpClient.send(redirectedRequest, HttpResponse.BodyHandlers.ofString());
                }
            }

            // Print the final response body
            int iCASAuthStatus = secondResponse.statusCode();
            System.out.println("post handling redirect CAS Auth resp body :::: "+secondResponse.body());  
            System.out.println("post handling redirect CAS Auth resp code:::: "+iCASAuthStatus);
            
            if(iCASAuthStatus == 200) {
            	
                cookies = cookieManager.getCookieStore().getCookies();               
                for (HttpCookie cookie : cookies) {
                    cookieHeaderBuilder.append(cookie.toString()).append("; ");
                }
                cookieHeader = cookieHeaderBuilder.toString();
                System.out.println("after successful CAS Auth :: cookies list = "+cookieHeader);
                
            	String encodedCSRFUrl = URLEncoder.encode(sCSRFTokenURL, "UTF-8");
            	String sNewURL = sCasAuthUrl+"?service="+encodedCSRFUrl;
         	          	
                // third HTTP request with cookies to generate CSRF token           	
                HttpRequest csrfRequest = HttpRequest.newBuilder()
                        .uri(new URI(sNewURL))
                        .GET()
                        .header("Cookie", cookieHeader) // Attach cookies to the second request
                        //.header("Accept", "Application/J")  // Add Accept header
                        .build();
                
                // Send the second request and retrieve the response           	
                HttpResponse<String> csrfResponse = httpClient.send(csrfRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("CSRF resp body :::: "+csrfResponse.body());  
                               
                int iCSRFStatus = csrfResponse.statusCode();
                System.out.println("CSRF resp code:::: "+iCSRFStatus);

                if(iCSRFStatus == 302) {
                	
                    Map<String, List<String>> csrfResponseHeaders = csrfResponse.headers().map();
                    System.out.println("csrfResponseHeaders = "+csrfResponseHeaders);
                    
                    String sCSRFRedirectURL = csrfResponse.headers().firstValue("Location").orElse(null);
                    System.out.println("sCSRFRedirectURL = "+sCSRFRedirectURL);
                    
                    cookies = cookieManager.getCookieStore().getCookies();
                    for (HttpCookie cookie : cookies) {
                        cookieHeaderBuilder.append(cookie.toString()).append("; ");
                    }                                      
                    cookieHeader = cookieHeaderBuilder.toString();
                    System.out.println("CSRF redirect:: :: cookies list = "+cookieHeader);

                    if (sCSRFRedirectURL != null) {
                        // Create a new request for the redirection URL
                        HttpRequest redirectedCSRFRequest = HttpRequest.newBuilder()
                                .uri(new URI(sCSRFRedirectURL))
                                .GET()
                                .header("Cookie", cookieHeader)
                                .header("Accept", "*/*")
                                .build();
                        
                        // Send the redirected request for CSRF and retrieve the final response
                        csrfResponse = httpClient.send(redirectedCSRFRequest, HttpResponse.BodyHandlers.ofString());
                        
                        String sCSRFRespBody = csrfResponse.body();
                        System.out.println("Redirected :: CSRF resp body :::: "+csrfResponse.body()); 
                        System.out.println("Redirected :: CSRF resp code :::: "+csrfResponse.statusCode());
                        
                        iCSRFStatus = csrfResponse.statusCode();
                        
                        if( iCSRFStatus == 200 ) {
                        	if(sCSRFRespBody.contains("csrf")) {
                        		
				            	JSONObject csrfJSON = (JSONObject) jsonParser.parse(sCSRFRespBody);
				            	
				            	JSONObject csrf = (JSONObject) csrfJSON.get("csrf");
				            	
				            	sCSRFTokenString = (String) csrf.get("value");
				            	System.out.println("csrf token = "+sCSRFTokenString);
				            	
			                    cookies = cookieManager.getCookieStore().getCookies();
			                    for (HttpCookie cookie : cookies) {
			                        cookieHeaderBuilder.append(cookie.toString()).append("; ");
			                    }                                      
			                    cookieHeader = cookieHeaderBuilder.toString();
			                    System.out.println("CSRF Success:: :: cookies list = "+cookieHeader);
			                    
			                    //STEP4: Import Requirements into 3Dx
		                        HttpRequest importReqsRequest = HttpRequest.newBuilder()
		                                .uri(new URI(sImportReqsURL))
		                                .header("Cookie", cookieHeader) // Attach cookies to the second request
		                                .header("ENO_CSRF_TOKEN", sCSRFTokenString)  // Add CSRF Token
		                                .header("SecurityContext", sUserRole)  // Security Context
		                                .header("Accept", "application/json") 
		                                .header("Content-Type", "application/json;charset=UTF-8") // Modified Content-Type header		                                
		                                .POST(HttpRequest.BodyPublishers.ofString(strJsonReconnectPostBody))
		                                .build();
		                        
		                        HttpResponse importReqsResp  = httpClient.send(importReqsRequest, HttpResponse.BodyHandlers.ofString()); 
		                        
		                        String sImportReqsRespBody = (String) importReqsResp.body();
		                        int iImportReqsRespStatus =  importReqsResp.statusCode();
		                        
		                        System.out.println("iImportReqsRespStatus = "+iImportReqsRespStatus);
		                        System.out.println("sImportReqsRespBody = "+sImportReqsRespBody);
		                        
		                        // Using Gson
		                        Gson gson = new Gson();
		                        // Parse JSON string to JSON object
		                        returnJson = gson.fromJson(sImportReqsRespBody, JSONObject.class);
		                        
		                        System.out.println("returnJson = "+returnJson);		                        
                        	}
                        }
                    }                                        
                }
            }
        }        
        return returnJson;
	}

}
