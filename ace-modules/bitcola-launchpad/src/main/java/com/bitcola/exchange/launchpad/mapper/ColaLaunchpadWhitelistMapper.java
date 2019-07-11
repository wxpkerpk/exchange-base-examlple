package com.bitcola.exchange.launchpad.mapper;

import com.bitcola.exchange.launchpad.entity.ColaLaunchpadApply;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadWhitelist;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaLaunchpadWhitelistMapper extends Mapper<ColaLaunchpadWhitelist> {
}
