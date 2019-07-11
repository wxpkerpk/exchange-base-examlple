package com.bitcola.exchange.security.me.util;

import com.bitcola.exchange.security.me.service.MongoFsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2018-11-20 17:42
 **/
@Component
public class ImageCaptchaUtil {

    private static int MAX_X = 500;
    private static int MAX_Y = 250;
    private static int W = 60;
    private static int OFFSET = 15;


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MongoFsService fsService;

    public boolean verification(String key,String x){
        try {
            Object o = redisTemplate.opsForValue().get(key);
            if (o == null){
                return false;
            }
            Integer i = Integer.valueOf(o.toString());
            Integer j = Integer.valueOf(x);
            if (Math.abs(i-j)<OFFSET){
                return true;
            }
        } catch (Exception e){
            return false;
        }
        return false;
    }

    public Map<String,Object> generate() throws Exception{
        String key = UUID.randomUUID().toString();
        int x = new Random().nextInt(MAX_X-100)+50;
        int y = new Random().nextInt(MAX_Y-100)+50;
        Map<String,String> images = generateImage(x,y);
        redisTemplate.opsForValue().set(key,x,10, TimeUnit.MINUTES);
        Map map = new HashMap<>();
        map.put("key",key);
        map.put("y",y);
        map.put("images",images);
        return map;
    }

    /**
     * 生成验证图片 3 张
     * @return
     */
    private Map<String,String> generateImage(int x,int y) throws Exception{
        BufferedImage original = ImageIO.read(ImageCaptchaUtil.class.getClassLoader().getResourceAsStream("captcha_1.jpg"));
        BufferedImage image = ImageIO.read(ImageCaptchaUtil.class.getClassLoader().getResourceAsStream("captcha_1.jpg"));
        BufferedImage result = new BufferedImage(W, W, BufferedImage.TYPE_INT_ARGB);
        for (int x1 = x; x1 < x+W; ++x1) {
            for (int y1 = y; y1 < y+W; ++y1) {
                int rgb = image.getRGB(x1, y1);
                Color color = new Color(rgb);
                Color newColor = new Color(color.getRed(), color.getGreen(),color.getBlue(), 100);
                result.setRGB(x1 - x, y1 - y, rgb);
                image.setRGB(x1,y1, newColor.getRGB());
            }
        }
        String name = UUID.randomUUID().toString();
        String originalName = name+"_original.png";
        String subtractName = name+"_subtract.png";
        String deductionName = name+"_deduction.png";
        fsService.uploadToMongodb(getInputStream(original),originalName);
        fsService.uploadToMongodb(getInputStream(image),subtractName);
        fsService.uploadToMongodb(getInputStream(result),deductionName);
        Map<String,String> map = new HashMap<>();
        map.put("origin",originalName);
        map.put("subtract",subtractName);
        map.put("deduction",deductionName);
        return map;
    }

    private InputStream getInputStream(BufferedImage imgBar) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
        ImageIO.write(imgBar,"png", imOut);
        InputStream is = new ByteArrayInputStream(bs.toByteArray());
        return is;
    }


    /**
     * 当前点距离中心点20像素之类,就为 true
     * @param x
     * @param y
     * @param centerX
     * @param centerY
     * @return
     */
    private static boolean isContain(int x,int y,int centerX,int centerY){
        double _x = Math.abs(centerX - x);
        double _y = Math.abs(centerY - y);
        double sqrt = Math.sqrt(_x * _x + _y * _y);
        if (sqrt<=20){
            return true;
        }
        return false;
    }

    private static boolean isBorder(int x,int y,int centerX,int centerY){
        double _x = Math.abs(centerX - x);
        double _y = Math.abs(centerY - y);
        double sqrt = Math.sqrt(_x * _x + _y * _y);
        if (20-sqrt<1){
            return true;
        }
        return false;
    }



    public static void main(String[] args) throws Exception {
        BufferedImage result = new BufferedImage(W, W, BufferedImage.TYPE_INT_ARGB);
        for (int x = 1; x < 60; ++x) {
            for (int y = 1; y < 60; ++y) {
                if (isContain(x,y,30,30)){
                    if (isBorder(x,y,30,30)){
                        result.setRGB(x,y,new Color(211, 189, 17).getRGB());
                    } else {
                        result.setRGB(x,y,new Color(171, 149, 17).getRGB());
                    }
                }else {
                    result.setRGB(x,y,new Color(255,255,255).getRGB());
                }
            }
        }
        ImageIO.write(result, "png", new File("/Users/qiuqiu/Desktop/ccc.png"));
    }




}
