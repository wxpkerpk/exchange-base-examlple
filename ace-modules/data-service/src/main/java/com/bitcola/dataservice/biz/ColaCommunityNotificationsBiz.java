package com.bitcola.dataservice.biz;

import com.bitcola.community.entity.NotificationsEntity;
import com.bitcola.community.entity.NotificationsVo;
import com.bitcola.dataservice.mapper.ColaCommunityNotificationsMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zkq
 * @create 2018-11-05 10:54
 **/
@Service
public class ColaCommunityNotificationsBiz extends BaseBiz<ColaCommunityNotificationsMapper, NotificationsEntity> {
    public Integer add(NotificationsEntity entity) {
        return mapper.insert(entity);
    }

    public List<NotificationsVo> list(Integer size, Long timestamp, String userId) {
        return mapper.list(size,timestamp,userId);
    }

    public Long notReadNumber(String userId) {
        return mapper.notReadNumber(userId);
    }

    public Integer read(String id) {
        return mapper.read(id);
    }

    public Integer readAll(String userId) {
        return mapper.readAll(userId);
    }
}
