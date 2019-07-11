package com.bitcola.exchange.security.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * @author lky
 * @create 2019-04-28 12:29
 **/
@Data
public class NewsBannerEntity {

    @Id
    String id;

    @Indexed
    String index;

    String image;

    String url;
}
