//package com.bitcola.exchange.security.me.rest;
//
//import com.bitcola.exchange.security.common.msg.AppResponse;
//import com.bitcola.exchange.security.me.service.MongoFsService;
//import com.bitcola.exchange.security.me.util.ExpireUploadImageUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.commons.CommonsMultipartFile;
//
//import javax.imageio.ImageIO;
//import javax.imageio.stream.ImageOutputStream;
//import javax.servlet.http.HttpServletResponse;
//import java.awt.*;
//import java.awt.geom.AffineTransform;
//import java.awt.image.AffineTransformOp;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.util.*;
//import java.util.List;
//
///**
// * 图片
// *
// * @author zkq
// * @create 2018-08-27 00:59
// **/
//@RequestMapping("/colaImage")
//@RestController
//public class ColaImageController {
//
//    public static final String IMAGE_TYPE_NORMAL = "normal";
//    public static final String IMAGE_TYPE_GIF = "gif";
//    public static final String IMAGE_TYPE_LONG = "long";
//
//    /**
//     * 压缩图片大小
//     */
//    public static final int SIZE = 500;
//
//    @Autowired
//    ExpireUploadImageUtil imageUtil;
//
//
//    public static String getFileType(String name)
//    {
//
//        String []s=name.split("\\.");
//        if(s.length>1) return s[s.length-1];
//        return null;
//
//
//    }
//    @Autowired
//    MongoFsService mongoFsService;
//
//
//    /**
//     * 社区图片上传
//     * @param files
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(value = "/communityUploadImage",method = RequestMethod.POST)
//    public AppResponse communityUploadImage(@RequestParam("files") MultipartFile[] files) throws IOException {
//
//        List<Map<String,String>> list = new ArrayList<>();
//        if (files == null || files.length == 0){
//            return AppResponse.paramsError();
//        }
//
//        for (MultipartFile file : files) {
//            Map<String,String> map = new HashMap<>();
//            String type = IMAGE_TYPE_NORMAL;
//            String originalFilename=file.getOriginalFilename();
//            assert originalFilename != null;
//            String suffix=getFileType(originalFilename);
//            if(suffix==null) return null;
//
//            String s = UUID.randomUUID().toString();
//            String name= s+"."+suffix;
//            String smallName = s+"_small."+suffix;
//            InputStream inputStream = file.getInputStream();
//            mongoFsService.uploadToMongodb(inputStream,name);
//            BufferedImage image = ImageIO.read(file.getInputStream());
//
//            InputStream smallInputStream = null;
//            int width = image.getWidth();
//            int height = image.getHeight();
//            if (height > width*2){
//                type = IMAGE_TYPE_LONG;
//            }
//            if (suffix.equalsIgnoreCase(IMAGE_TYPE_GIF)){
//                type = IMAGE_TYPE_GIF;
//            }
//
//            if (type.equalsIgnoreCase(IMAGE_TYPE_GIF)){
//                //截取第一帧,压缩
//                InputStream png = getInputStream(image, "png");
//                BufferedImage bf = ImageIO.read(png);
//                BufferedImage smallPic = smallPic(bf, SIZE);
//                smallInputStream = getInputStream(smallPic, "png");
//            } else if (type.equalsIgnoreCase(IMAGE_TYPE_LONG)){
//                //长图 压缩,截取最上面
//                BufferedImage smallPic = smallPic(image, SIZE);
//                BufferedImage cutImg = cutImg(smallPic, 0, 0, SIZE, SIZE);
//                smallInputStream = getInputStream(cutImg,suffix);
//            } else {
//                // 普通图片
//                if (files.length == 1){
//                    // 只1张图片就压缩下,不裁剪比例
//                    BufferedImage smallPic = smallPic(image, SIZE);
//                    smallInputStream = getInputStream(smallPic,suffix);
//                } else {
//                    // 压缩,裁剪正中的为正方形
//                    BufferedImage smallPic = smallPic(image, SIZE);
//                    int x = 0;
//                    int y = 0;
//                    width = smallPic.getWidth();
//                    height = smallPic.getHeight();
//                    if (width > height){
//                        x = (width - SIZE) / 2;
//                    } else {
//                        y = (height - SIZE) / 2;
//                    }
//                    BufferedImage cutImg = cutImg(smallPic, x, y, x + SIZE, y + SIZE);
//                    smallInputStream = getInputStream(cutImg,suffix);
//                }
//            }
//            mongoFsService.uploadToMongodb(smallInputStream,smallName);
//            //记录返回前台
//            map.put("source",name);
//            map.put("small",smallName);
//            map.put("type",type);
//            list.add(map);
//        }
//        return AppResponse.ok().data(list);
//
//    }
//
//    /**
//     * 聊天图片上传
//     * @param file
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping("chatUploadImage")
//    public AppResponse chatUploadImage(@RequestParam("file")CommonsMultipartFile file) throws IOException{
//        String originalFilename=file.getOriginalFilename();
//        assert originalFilename != null;
//        String type=getFileType(originalFilename);
//        if(type==null) return null;
//        String uuid = UUID.randomUUID().toString();
//        String name= uuid+"."+type;
//        String smallName= uuid+"_small."+type;
//        imageUtil.saveChatPhoto(file.getInputStream(),name);
//        BufferedImage image = ImageIO.read(file.getInputStream());
//        BufferedImage smallPic = smallPic(image, SIZE);
//        InputStream inputStream = getInputStream(smallPic, type);
//        imageUtil.saveChatPhoto(inputStream,smallName);
//        return AppResponse.ok().data(smallName);
//    }
//
//
//
//
//    @RequestMapping(value = "/upload",method = RequestMethod.POST)
//    public AppResponse upload(@RequestParam("files") MultipartFile[] files) throws IOException {
//        List<String> resp = new ArrayList<>();
//        for (MultipartFile file : files) {
//            String originalFilename=file.getOriginalFilename();
//            assert originalFilename != null;
//            String type=getFileType(originalFilename);
//            if(type==null) return null;
//
//            String name= UUID.randomUUID().toString()+"."+type;
//            mongoFsService.uploadToMongodb(file.getInputStream(),name);
//            resp.add(name);
//        }
//        return AppResponse.ok().data(resp);
//
//
//    }
//
//    @RequestMapping(value = "/delete",method = RequestMethod.POST)
//    public AppResponse delete(String name)
//    {
//        mongoFsService.delete(name);
//        return AppResponse.ok();
//    }
//
//
//    @RequestMapping(value = "/{name}",method = RequestMethod.GET)
//    public void getRestful(@PathVariable String name, HttpServletResponse response) throws IOException {
//        response.setContentType("image/jpeg");
//        response.setDateHeader("Last-Modified",new Date().getTime());
//        response.setDateHeader("Expires", System.currentTimeMillis()+1000l*3600*24*30);
//        response.setHeader("Cache-Control", "Public");
//        response.setHeader("Pragma", "Pragma");
//        mongoFsService.getImageFromMongodb(name,response);
//    }
//
//
//    /**
//     * BufferedImage 转 inputStream
//     * @param imgBar
//     * @param suffix
//     * @return
//     * @throws IOException
//     */
//    private InputStream getInputStream(BufferedImage imgBar,String suffix) throws IOException{
//        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
//        ImageIO.write(imgBar, suffix, imOut);
//        InputStream is = new ByteArrayInputStream(bs.toByteArray());
//        return is;
//    }
//
//
//
//
//    /**
//     * 压缩图片为制定宽度,高度
//     */
//    private BufferedImage smallPic( BufferedImage bufImg,int index) throws IOException{
//        int width = bufImg.getWidth();
//        int height = bufImg.getHeight();
//        int w,h;
//        if (width<height){
//            w = index;
//            h = index * height / width;
//        } else {
//            h = index;
//            w = index * width / height;
//        }
//        Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);//设置缩放目标图片模板
//
//        double wr=w*1.0/ width ;   //获取缩放比例
//        double hr=h*1.0 / height;
//
//        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
//        Itemp = ato.filter(bufImg, null);
//        return (BufferedImage)Itemp;
//
//    }
//
//
//    /**
//     * 裁剪图片
//     * @param bufferedImage
//     * @param bufferedImage 图像源
//     * @param startX 裁剪开始x坐标
//     * @param startY 裁剪开始y坐标
//     * @param endX 裁剪结束x坐标
//     * @param endY 裁剪结束y坐标
//     * @return
//     * @throws IOException
//     */
//    private BufferedImage cutImg(BufferedImage bufferedImage, int startX, int startY, int endX, int endY) throws IOException{
//            int width = bufferedImage.getWidth();
//            int height = bufferedImage.getHeight();
//            if (startX == -1) {
//                startX = 0;
//            }
//            if (startY == -1) {
//                startY = 0;
//            }
//            if (endX == -1) {
//                endX = width - 1;
//            }
//            if (endY == -1) {
//                endY = height - 1;
//            }
//            BufferedImage result = new BufferedImage(endX - startX, endY - startY, 4);
//            for (int x = startX; x < endX; ++x) {
//                for (int y = startY; y < endY; ++y) {
//                    int rgb = bufferedImage.getRGB(x, y);
//                    result.setRGB(x - startX, y - startY, rgb);
//                }
//            }
//            return result;
//    }
//
//
//
//}
