package com.bitcola.exchange.bitcolapush.oss;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.*;
import com.aliyun.oss.common.utils.DateUtil;
import com.aliyun.oss.model.ObjectMetadata;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-12-29 21:20
 **/
public class OssUtil {
    private static String accessKeyId = "LTAIsCYUscZ3p2TB";
    private static String accessKeySecret = "Nf4cRBwnrUWKUF3pdwoTANf00CBw5n";

    private static String bucketName = "bitcolachina";
    private static String endpoint = "https://oss-cn-shanghai.aliyuncs.com";
    private static String domain = "https://bitcolachina.oss-cn-shanghai.aliyuncs.com";

    private static OSS client;

    static{
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        client = new OSSClientBuilder().build(endpoint,accessKeyId,accessKeySecret,conf);
    }

    public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        map.put("test","我修改了222文件内容");
        pushApi(map,"test");
    }

    public static boolean pushApi(Object object,String apiName){
        String json = JSONObject.toJSONString(object);
        InputStream is;
        try {
            is = new ByteArrayInputStream(json.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("application/json;charset=UTF-8");
        client.putObject(bucketName, "api/"+apiName+".json", is,meta);
        return true;
    }







    public static String uploadSuffix(InputStream inputStream, String suffix) {
        String path = getPath(suffix);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setCacheControl("Public");
        try {
            meta.setExpirationTime(DateUtil.parseIso8601Date("2022-10-12T00:00:00.000Z"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        meta.setHeader("Pragma","Pragma");
        meta.setLastModified(new Date());
        client.putObject(bucketName,path,inputStream,meta);
        return domain+"/"+path;
    }


    public static String getPath(String suffix) {
        //生成uuid
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //文件路径
        String path = DateTime.now().toString("yyyyMMdd") + "/" + uuid;
        path = "image/" + path;
        return path + suffix;
    }

}
