package com.bitcola.exchange.launchpad.mapper;

import com.bitcola.exchange.launchpad.entity.ColaLaunchpadApply;
import com.bitcola.exchange.launchpad.vo.IeoParams;
import com.bitcola.exchange.launchpad.vo.ProjectParams;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ColaLaunchpadApplyMapper extends Mapper<ColaLaunchpadApply> {
    int saveProject(ProjectParams params);

    int saveIeo(IeoParams params);

    Long projectTotal();

    List<ProjectParams> projectList(AdminQuery query);

    List<IeoParams> ieoList(@Param("id") String id);

    void frozenProjectBalance(@Param("id")String id, @Param("number")BigDecimal number,@Param("key")String key);
}
