package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author lky
 * @create 2019-04-18 12:48
 **/
@Data
@Table(name = "ag_admin_v1.cola_chat_consumer")
public class Consumer {

    @Id
    String id;
    @Column(name = "description")
    String description;
}
