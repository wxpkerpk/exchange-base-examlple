package com.bitcola.dataservice.mapper;

import com.bitcola.community.entity.NotificationsEntity;
import com.bitcola.community.entity.NotificationsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaCommunityNotificationsMapper extends Mapper<NotificationsEntity> {
    List<NotificationsVo> list(@Param("size") Integer size, @Param("timestamp") Long timestamp, @Param("userId") String userId);

    Long notReadNumber(@Param("userId") String userId);

    Integer read(@Param("id") String id);

    Integer readAll(@Param("userId") String userId);
}
