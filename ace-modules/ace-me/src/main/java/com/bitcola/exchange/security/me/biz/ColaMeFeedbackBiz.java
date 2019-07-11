package com.bitcola.exchange.security.me.biz;

import com.bitcola.me.entity.ColaMeFeedback;
import com.bitcola.exchange.security.me.mapper.ColaMeFeedbackMapper;
import org.springframework.stereotype.Service;

import com.bitcola.exchange.security.common.biz.BaseBiz;

import java.util.List;

/**
 * 意见反馈表
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Service
public class ColaMeFeedbackBiz extends BaseBiz<ColaMeFeedbackMapper,ColaMeFeedback> {


    /**
     * 意见列表
     *
     * @author zkq
     * @date 2018/7/15 16:15
     * @param userID
     * @return java.util.List<ColaFeedbackEntity>
     */
    public List<ColaMeFeedback> list(String userID) {
        return  mapper.list(userID);
    }
}