//package com.bitcola.exchange.security.admin.config;
//
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.alibaba.fastjson.support.config.FastJsonConfig;
//import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
//import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.HttpMessageConverter;
//
//
///**
// * 处理返回为空
// *
// * @author zkq
// * @create 2018-08-23 14:54
// **/
//@Configuration
//public class FastJsonMessage {
//
//    @Bean
//    public HttpMessageConverters fastJsonHttpMessageConverters() {
//        //1、定义一个convert转换消息的对象
//        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
//        //2、添加fastjson的配置信息
//        FastJsonConfig fastJsonConfig = new FastJsonConfig();
//        fastJsonConfig.setSerializerFeatures(
//                SerializerFeature.WriteEnumUsingToString,
//                SerializerFeature.WriteNullStringAsEmpty,
//                SerializerFeature.WriteMapNullValue,
//                SerializerFeature.WriteNullListAsEmpty,
//                SerializerFeature.WriteDateUseDateFormat);
//        ////2-1 处理中文乱码问题
//        //List<MediaType> fastMediaTypes = new ArrayList<>();
//        //fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
//        //fastConverter.setSupportedMediaTypes(fastMediaTypes);
//        //3、在convert中添加配置信息
//        fastConverter.setFastJsonConfig(fastJsonConfig);
//        //4、将convert添加到converters中
//        HttpMessageConverter<?> converter = fastConverter;
//        return new HttpMessageConverters(converter);
//    }
//}
//
