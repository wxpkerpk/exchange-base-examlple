package com.bitcola.exchange.security.me.mapper;

import com.bitcola.me.entity.ColaMeFeedback;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 意见反馈表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Repository
public interface ColaMeFeedbackMapper extends Mapper<ColaMeFeedback> {

    /**
     * 列表
     * @param userID
     * @return
     */
    List<ColaMeFeedback> list(@Param("userid") String userID);
	
}
