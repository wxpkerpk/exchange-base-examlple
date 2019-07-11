package com.bitcola.exchange.security.me.config;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/*
 * @author:wx
 * @description:
 * @create:2018-08-22  16:22
 */
@Configuration
public class MongodbConfig {
    @Value(value = "${spring.data.mongodb.host}")

    String host;

    @Value(value = "${spring.data.mongodb.username}")
    String username;
    @Value(value = "${spring.data.mongodb.database}")

    String database;
    @Value(value = "${spring.data.mongodb.password}")

    String password;
    @Value(value = "${spring.data.mongodb.port}")
    String port;


    @Bean
    public GridFS mongoClient()
    {

        MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(host, Integer.parseInt(port)), Collections.singletonList(credential));
        DB db=mongoClient.getDB(database);
        GridFS gfsPhoto = new GridFS(db, "photo");

        return gfsPhoto;


    }
}
