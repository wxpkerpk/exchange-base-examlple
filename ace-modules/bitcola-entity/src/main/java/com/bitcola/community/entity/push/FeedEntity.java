package com.bitcola.community.entity.push;

import com.bitcola.community.entity.NotificationsEntity;
import lombok.Data;

/**
 * @author zkq
 * @create 2018-11-15 10:19
 **/
@Data
public class FeedEntity extends NotificationsEntity {
    String fromUsername;
    String fromUserNickName;
    String fromUserAvatar;
}
