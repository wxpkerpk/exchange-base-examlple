package com.bitcola.exchange.security.community.util;

import com.bitcola.exchange.security.community.entity.*;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.me.entity.ColaUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加用户信息
 *
 * @author zkq
 * @create 2018-10-30 15:10
 **/
@Component
public class AddUserInfoUtil {

    @Autowired
    IDataServiceFeign userFeign;

    /**
     * 列表
     * @param list
     */
    public void articleItems(List<ArticleItemEntity> list){
        if (list.size() == 0){
            return;
        }
        ArrayList<String> ids = new ArrayList<>();
        for (ArticleItemEntity entity : list) {
            ids.add(entity.getFromUser());
        }
        List<ColaUserEntity> colaUserEntities = userFeign.infoByIds(ids);
        for (ArticleItemEntity entity : list) {
            for (ColaUserEntity user : colaUserEntities) {
                if (user.getSysUserID().equals(entity.getFromUser())){
                    entity.setFromNickName(user.getNickName());
                    entity.setFromUsername(user.getUsername());
                    entity.setFromUserAvatar(user.getAvatar());
                    entity.setFromUserSign(user.getSign());
                }
            }
        }
    }

    public void likeList(List<LikeEntity> list){
        ArrayList<String> ids = new ArrayList<>();
        for (LikeEntity entity : list) {
            ids.add(entity.getFromUser());
        }
        List<ColaUserEntity> colaUserEntities = userFeign.infoByIds(ids);
        for (LikeEntity entity : list) {
            for (ColaUserEntity user : colaUserEntities) {
                if (user.getSysUserID().equals(entity.getFromUser())){
                    entity.setFromNickName(user.getNickName());
                    entity.setFromUsername(user.getUsername());
                    entity.setFromUserAvatar(user.getAvatar());
                    entity.setFromUserSign(user.getSign());
                }
            }
        }
    }
    public void commentList(List<CommentEntity> list){
        if (list.size() == 0){
            return;
        }
        ArrayList<String> ids = new ArrayList<>();
        for (CommentEntity entity : list) {
            ids.add(entity.getFromUser());
        }
        List<ColaUserEntity> colaUserEntities = userFeign.infoByIds(ids);
        for (CommentEntity entity : list) {
            for (ColaUserEntity user : colaUserEntities) {
                if (user.getSysUserID().equals(entity.getFromUser())){
                    entity.setFromNickName(user.getNickName());
                    entity.setFromUsername(user.getUsername());
                    entity.setFromUserAvatar(user.getAvatar());
                    entity.setFromUserSign(user.getSign());
                }
            }
        }
    }
    public void donateList(List<DonateEntity> list){
        if (list.size() == 0){
            return;
        }
        ArrayList<String> ids = new ArrayList<>();
        for (DonateEntity entity : list) {
            ids.add(entity.getFromUser());
        }
        List<ColaUserEntity> colaUserEntities = userFeign.infoByIds(ids);
        for (DonateEntity entity : list) {
            for (ColaUserEntity user : colaUserEntities) {
                if (user.getSysUserID().equals(entity.getFromUser())){
                    entity.setFromNickName(user.getNickName());
                    entity.setFromUsername(user.getUsername());
                    entity.setFromUserAvatar(user.getAvatar());
                    entity.setFromUserSign(user.getSign());
                }
            }
        }
    }

    /**
     * 单个
     * @param entity
     */
    public void articleItem(ArticleItemEntity entity){
        ColaUserEntity user = userFeign.info(entity.getFromUser());
        entity.setFromNickName(user.getNickName());
        entity.setFromUsername(user.getUsername());
        entity.setFromUserAvatar(user.getAvatar());
        entity.setFromUserSign(user.getSign());
    }
    /**
     * 单个
     * @param entity
     */
    public void articleDetail(ArticleEntity entity){
        ColaUserEntity user = userFeign.info(entity.getFromUser());
        entity.setFromNickName(user.getNickName());
        entity.setFromUsername(user.getUsername());
        entity.setFromUserAvatar(user.getAvatar());
        entity.setFromUserSign(user.getSign());
    }
    /**
     * 单个
     * @param entity
     */
    public void shortArticleDetail(ShortArticleEntity entity){
        ColaUserEntity user = userFeign.info(entity.getFromUser());
        entity.setFromNickName(user.getNickName());
        entity.setFromUsername(user.getUsername());
        entity.setFromUserAvatar(user.getAvatar());
        entity.setFromUserSign(user.getSign());
    }


}
