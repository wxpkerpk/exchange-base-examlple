package com.bitcola.chain.chain.tera.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-08 09:50
 **/
@Data
public class HistoryTransaction {
    int result;
    List<History> History = new ArrayList<>();
}
