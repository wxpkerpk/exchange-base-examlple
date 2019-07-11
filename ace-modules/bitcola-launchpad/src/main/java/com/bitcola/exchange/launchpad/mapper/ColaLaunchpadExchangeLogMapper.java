package com.bitcola.exchange.launchpad.mapper;

import com.bitcola.exchange.launchpad.entity.ColaLaunchpadExchangeLog;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaLaunchpadExchangeLogMapper extends Mapper<ColaLaunchpadExchangeLog> {
    List<ColaLaunchpadExchangeLog> list(AdminQuery query);

    Long total(AdminQuery query);

    List<ColaLaunchpadExchangeLog> listIssue(@Param("projectId") String projectId);

    void updateStatus(@Param("projectId")String projectId, @Param("issued")String issued);
}
