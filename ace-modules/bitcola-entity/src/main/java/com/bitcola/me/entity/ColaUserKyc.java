package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2018-12-06 13:00
 **/
@Data
@Table(name = "ag_admin_v1.cola_user_kyc")
public class ColaUserKyc {

    @Id
    @Column(name = "user_id")
    private String userId;
    @Column(name = "kyc_status")
    private Integer kycStatus;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "document_number")
    private String documentNumber;
    @Column(name = "front_side")
    private String frontSide;
    @Column(name = "back_side")
    private String backSide;
    @Column(name = "document_type")
    private String documentType;
    @Column(name = "document_and_face")
    private String documentAndFace;
    private Long timestamp;
    private String reason;
    @Column(name = "area_code")
    private String areaCode;
    private String telephone;
    private String email;

}
