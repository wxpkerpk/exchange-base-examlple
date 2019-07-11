package com.bitcola.chain.chain.btc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/*
 * @author:wx
 * @description:
 * @create:2018-10-21  16:02
 */
@Configuration
public class JsonRpcHttpCilenConfig {
    @Value("${btc.rpc_user}")
    String rpc_user;
    @Value("${btc.rpc_password}")
    String rpc_password;
    @Value("${btc.rpc_url}")
    String rpc_url;
    @Bean("btcClient")
    public JsonRpcHttpClient jsonRpcHttpClient() throws MalformedURLException {
        // 身份认证
        String cred = Base64.getEncoder().encodeToString((rpc_user + ":" + rpc_password).getBytes());
        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("Authorization", "Basic " + cred);
        return new JsonRpcHttpClient(new URL(rpc_url), headers);

    }
}
