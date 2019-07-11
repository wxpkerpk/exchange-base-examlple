package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.entity.ResourceAuthority;
import com.bitcola.exchange.security.admin.mapper.ResourceAuthorityMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by wx on 2017/6/19.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ResourceAuthorityBiz extends BaseBiz<ResourceAuthorityMapper,ResourceAuthority> {
}
