package com.bitcola.exchange.security.me.util;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * 聊天图片 (3个月过期)
 * @author zkq
 * @create 2018-10-09 11:56
 **/
@Component
public class ExpireUploadImageUtil {

    @Autowired
    MongoTemplate mongoTemplate;

    GridFS gfsPhoto;

    public void saveChatPhoto(InputStream inputStream, String name){
        if (gfsPhoto == null){
            init();
        }
        GridFSInputFile gfsFile = gfsPhoto.createFile(inputStream);
        gfsFile.setFilename(name);
        gfsFile.save();
    }

    public GridFSDBFile getChatPhoto(String name){
        if (gfsPhoto == null){
            init();
        }
        return gfsPhoto.findOne(name);
    }


    private void init(){
        DB db = mongoTemplate.getMongoDbFactory().getLegacyDb();
        gfsPhoto = new GridFS(db, "chatPhoto");
    }

    /**
     * 手动去掉过期的图片
     *  每天半夜4点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void expire(){
        long currentTime = System.currentTimeMillis();
        long threeMonthsAgo = currentTime-7776000000L;  //三个月前的时间
        DB db = mongoTemplate.getMongoDbFactory().getLegacyDb();
        if (gfsPhoto == null){
            init();
        }
        DBCollection collection = db.getCollection("chatPhoto" + ".files");
        DBCursor sort = collection.find().sort(new BasicDBObject("uploadDate", 1));
        while (sort.hasNext()){
            DBObject next = sort.next();
            Date uploadDate = (Date)next.get("uploadDate");
            if (uploadDate.getTime() < threeMonthsAgo){
                String filename = next.get("filename").toString();
                // 删除这个文件
                gfsPhoto.remove(filename);
            } else {
                return;
            }
        }

    }


}