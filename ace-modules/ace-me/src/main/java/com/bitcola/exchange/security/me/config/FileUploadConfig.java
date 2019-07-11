package com.bitcola.exchange.security.me.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;


/**
 * 文件上传配置
 *
 * @author zkq
 * @create 2018-09-13 16:35
 **/
@Configuration
public class FileUploadConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //  单个数据大小
        factory.setMaxFileSize("30240KB"); // KB,MB
        /// 总上传数据大小
        factory.setMaxRequestSize("302400KB");
        return factory.createMultipartConfig();
    }
}
