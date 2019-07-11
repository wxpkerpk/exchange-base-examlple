package com.bitcola.exchange.bitcolapush.rest;

import com.bitcola.exchange.bitcolapush.oss.OssUtil;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2018-12-29 21:55
 **/
@RequestMapping("image")
@RestController
public class ImageController {

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public AppResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()){
            return AppResponse.error("error");
        }
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String url = OssUtil.uploadSuffix(file.getInputStream(), suffix);
        return AppResponse.ok().data(url);
    }

    @RequestMapping(value = "/multipleUpload",method = RequestMethod.POST)
    public AppResponse multipleUpload(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()){
                return AppResponse.error("error");
            }
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String url = OssUtil.uploadSuffix(file.getInputStream(), suffix);
            urls.add(url);
        }
        return AppResponse.ok().data(urls);
    }





}
