package com.bitcola.exchange.security.me.service;

/*
 * @author:wx
 * @description:
 * @create:2018-08-22  15:30
 */

import com.bitcola.exchange.security.me.util.ExpireUploadImageUtil;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Service
public class MongoFsService {


   @Autowired
   GridFS gridFS;

   @Autowired
   ExpireUploadImageUtil expireUploadImageUtil;

   public void uploadToMongodb(InputStream inputStream,String name)
   {


       GridFSInputFile gfsFile = null;
       gfsFile = gridFS.createFile(inputStream);
       gfsFile.setFilename(name);
       gfsFile.save();

   }


   public boolean delete(String name)
   {
      gridFS.remove(gridFS.findOne(name));
      return true;
   }





   public void getImageFromMongodb(String name, HttpServletResponse response)
   {
      GridFSDBFile imageForOutput = gridFS.findOne(name);
      //if (imageForOutput == null){
      //   imageForOutput = expireUploadImageUtil.getChatPhoto(name);
      //}
      try {
         if (imageForOutput != null){
            imageForOutput.writeTo(response.getOutputStream());

         }
      } catch (IOException e) {
         e.printStackTrace();
      }


   }
}
