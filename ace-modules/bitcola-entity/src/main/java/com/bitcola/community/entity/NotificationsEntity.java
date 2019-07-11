package com.bitcola.community.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 通知
 *
 * @author zkq
 * @create 2018-11-05 10:46
 **/
@Data
@Table(name = "ag_admin_v1.cola_community_notifications")
public class NotificationsEntity implements Serializable {

    @Id
    String id;

    String type;

    @Column(name = "from_user")
    String fromUser;

    @Column(name = "to_user")
    String toUser;

    @Column(name = "action_id")
    String actionId;

    @Column(name = "action_info")
    String actionInfo;
    Long time;

    @Column(name = "is_read")
    Integer isRead;

    String info;

    @Column(name = "action_type")
    String actionType;


}
