package com.bitcola.exchange.chat.comm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Constants
 * 
 * @author Lynch 2014-09-15
 *
 */
@Component
public class Constants {

	public static String API_HTTP_SCHEMA = "https";
	public static String DEFAULT_PASSWORD = "bitcola2018";

	public static String API_SERVER_HOST;
	public static String APPKEY;
	public static String APP_CLIENT_ID;
	public static String APP_CLIENT_SECRET;


	@Value("${easemob.api_server_host}")
	public void setAPI_SERVER_HOST(String api_server_host){
		API_SERVER_HOST = api_server_host;
	}
	@Value("${easemob.appkey}")
	public void setAPPKEY(String appkey){
		APPKEY = appkey;
	}
	@Value("${easemob.app_client_id}")
	public void setAPP_CLIENT_ID(String app_client_id){
		APP_CLIENT_ID = app_client_id;
	}
	@Value("${easemob.app_client_secret}")
	public void setAPP_CLIENT_SECRET(String app_client_secret){
		APP_CLIENT_SECRET = app_client_secret;
	}
}
