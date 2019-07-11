package com.bitcola.exchange.launchpad.mapper;

import com.bitcola.exchange.launchpad.dto.ColaLaunchpadProjectDto;
import com.bitcola.exchange.launchpad.dto.ColaLaunchpadProjectList;
import com.bitcola.exchange.launchpad.dto.ColaUserBalance;
import com.bitcola.exchange.launchpad.dto.ColaUserStatus;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadApply;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadExchangeLog;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadProjectIeo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ColaLaunchpadProjectIeoMapper extends Mapper<ColaLaunchpadProjectIeo> {
    Long total();

    List<ColaLaunchpadProjectList> list(@Param("page")Integer page, @Param("size") Integer size);

    void updateProjectStatus(@Param("id")String id, @Param("status")Integer status);

    ColaLaunchpadProjectDto detail(@Param("id")String id);

    String getUserPin(@Param("userId")String userId);

    int frozenUserBalance(@Param("id")String id, @Param("number")BigDecimal frozenNumber,@Param("key") String key);

    List<String> selectBatch(@Param("list")List<ColaUserBalance> balanceList);

    void batchInsertExchangeLog(@Param("list")List<ColaLaunchpadExchangeLog> exchangeLogs);

    void updateProjectRemainAndStatus(@Param("id")String id, @Param("sellNumber")BigDecimal sellNumber,@Param("status") Integer status);

    List<Map<String,Object>> selectIeoProject();

    ColaLaunchpadProjectIeo selectProjectById(@Param("id")String id);

    void batchUpdateUserBalance(@Param("list")List<ColaUserBalance> balanceList, @Param("balanceKey")String balanceKey);

    List<ColaUserStatus> getUserPinAndKycByIds(@Param("list")List<String> ids);

    List<ColaLaunchpadProjectIeo> selectProjectByIds(@Param("list")List<String> projectIds);
}
