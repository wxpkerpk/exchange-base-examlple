package com.bitcola.exchange.security.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * @author lky
 * @create 2019-04-28 14:43
 **/
@Data
public class LiveEntity {

    @Id
    String id;

    String title;

    String content;

    @Indexed
    Long time;

    String image;

    String type;

    String from;
}
