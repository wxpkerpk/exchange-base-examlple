package com.bitcola.ctc;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-08 10:57
 **/
@Data
@Table(name = "ag_admin_v1.cola_ctc_order")
public class ColaCtcOrder {
    /**
     * 订单号
     */
    @Id
    String id;
    String direction;
    @Column(name = "coin_code")
    String coinCode;
    Long timestamp;
    @Column(name = "customer_user_id")
    String customerUserId;
    BigDecimal price;
    BigDecimal number;
    BigDecimal fee;
    /**
     * 待付款,已付款,已到账,已完成 | 待处理,处理中,已汇出,已完成 | 失败
     */
    String status;
    @Column(name = "from_card_id")
    String fromCardId;
    @Column(name = "to_card_id")
    String toCardId;

    /**
     * 审核人
     *
     *   审核人包含 4 个操作
     *   接受任务: 避免任务重复处理
     *   填写进出账单: 只能由接收任务的人填写
     *
     *   完成:可能是另一个人
     *   拒绝:可能是另一个人,也可以是审核人
     */
    String auditor;
    /**
     * 审核状态: 未处理,处理中,确认资金情况(已经填写出账进账单),已完成  | 已拒绝
     */
    @Column(name = "audit_status")
    String auditStatus;

    /**
     * 最终确认人 (点击已完成按钮的那个人,此时加币,扣币)
     */
    @Column(name = "confirm_user_id")
    String confirmUserId;
    @Column(name = "audit_timestamp")
    Long auditTimestamp;
    @Column(name = "confirm_timestamp")
    Long confirmTimestamp;
}
