package com.bitcola.caculate.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
 * @author:wx
 * @description:
 * @create:2018-10-06  18:29
 */
@Data
public class CaculateParams {
    String type;
    List<ColaOrder> completed=new ArrayList<>();
    ColaOrder unCompleted;
    public static void main(String []s){

        System.out.println(System.currentTimeMillis()-3*60*60*1000);



    }
}
