package com.bitcola.chain.chain.usdt;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.java_websocket.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-01-10 17:49
 **/
@Configuration
public class UsdtConfig {
    @Value("${usdt.rpc_user}")
    String rpc_user;
    @Value("${usdt.rpc_password}")
    String rpc_password;
    @Value("${usdt.rpc_url}")
    String rpc_url;
    @Bean("usdtClient")
    public JsonRpcHttpClient usdtClient() throws MalformedURLException {
        // 身份认证
        String cred = Base64.encodeBytes((rpc_user + ":" + rpc_password).getBytes());
        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("Authorization", "Basic " + cred);
        return new JsonRpcHttpClient(new URL(rpc_url), headers);

    }
}
