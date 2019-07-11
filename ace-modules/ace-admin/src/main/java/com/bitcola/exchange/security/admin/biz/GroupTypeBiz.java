package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.entity.GroupType;
import com.bitcola.exchange.security.admin.mapper.GroupTypeMapper;
import org.springframework.stereotype.Service;

import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.transaction.annotation.Transactional;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-06-12 8:48
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GroupTypeBiz extends BaseBiz<GroupTypeMapper,GroupType> {
}
