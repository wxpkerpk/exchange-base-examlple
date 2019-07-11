package com.bitcola.exchange.script.data;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-03-29 10:07
 **/
@Data
@Table(name = "ag_admin_v1.cola_exchange_script_config")
public class Config {

    @Id
    String script;
    String config;
}
