package com.bitcola.exchange.security.community.entity;

import lombok.Data;

/**
 * 文章内容
 *
 * @author zkq
 * @create 2018-08-22 14:17
 **/
@Data
public class ArticleContent {

    public ArticleContent(){}

    public ArticleContent(String type,String content){
        this.type = type;
        this.content = content;
    }

    /**
     * "text" or "image"
     */
    private String type;

    /**
     * 具体内容
     */
    private String content;

    private ImageEntity image;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
