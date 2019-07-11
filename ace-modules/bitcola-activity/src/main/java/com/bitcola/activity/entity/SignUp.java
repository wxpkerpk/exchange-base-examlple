package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-11-29 12:28
 **/
@Data
@Table(name = "ag_admin_v1.cola_activity_sign_up")
public class SignUp extends Activity{


    @Column(name = "area_code")
    String areaCode;
    String telephone;
    @Column(name = "is_inviter_reward")
    Integer isInviterReward = 0;
}
