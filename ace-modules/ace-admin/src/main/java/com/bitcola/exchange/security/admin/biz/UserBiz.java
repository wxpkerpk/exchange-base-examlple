package com.bitcola.exchange.security.admin.biz;

import com.ace.cache.annotation.Cache;
import com.ace.cache.annotation.CacheClear;
import com.bitcola.exchange.security.admin.entity.User;
import com.bitcola.exchange.security.admin.mapper.ColaUserLimitMapper;
import com.bitcola.exchange.security.admin.mapper.ColaUserMapper;
import com.bitcola.exchange.security.admin.mapper.MenuMapper;
import com.bitcola.exchange.security.admin.mapper.UserMapper;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.me.entity.ColaUserLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-06-08 16:23
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserBiz extends BaseBiz<UserMapper,User> {

    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private UserAuthUtil userAuthUtil;
    @Autowired
    ColaUserMapper userMapper;
    @Autowired
    ColaUserLimitMapper userLimitMapper;
    @Override
    public void insertSelective(User entity) {
        String password = new BCryptPasswordEncoder(UserConstant.PW_ENCORDER_SALT).encode(entity.getPassword());
        entity.setPassword(password);
        Long id = mapper.getMaxId()+1;
        if (id == 100) throw new RuntimeException("管理系统人数已达上限");
        entity.setId(id.toString());
        super.insertSelective(entity);
        // 创建客户账号(id = 邀请人,昵称,头像,时间)
        String nickName = "客服"+entity.getId();
        userMapper.insertCustomer(entity.getId(),nickName,UserConstant.DEFAULT_AVATAR,System.currentTimeMillis());
        // 禁止该用户冲提和交易
        ColaUserLimit userLimit = new ColaUserLimit();
        userLimit.setId(UUID.randomUUID().toString());
        userLimit.setUserId(entity.getId());
        userLimit.setReason("初始化禁用");
        userLimit.setTime(0L);
        userLimit.setType(1);
        userLimit.setModule("community");
        userLimitMapper.insertSelective(userLimit);
        userLimit = new ColaUserLimit();
        userLimit.setId(UUID.randomUUID().toString());
        userLimit.setUserId(entity.getId());
        userLimit.setReason("初始化禁用");
        userLimit.setTime(0L);
        userLimit.setType(1);
        userLimit.setModule("withdraw");
        userLimitMapper.insertSelective(userLimit);
        userLimit = new ColaUserLimit();
        userLimit.setId(UUID.randomUUID().toString());
        userLimit.setUserId(entity.getId());
        userLimit.setReason("初始化禁用");
        userLimit.setTime(0L);
        userLimit.setType(1);
        userLimit.setModule("makeOrder");
        userLimitMapper.insertSelective(userLimit);
    }

    
    @Override
    @CacheClear(pre="user{1.username}")
    public void updateSelectiveById(User entity) {
        super.updateSelectiveById(entity);
    }

    /**
     * 根据用户名获取用户信息
     * @param username
     * @return
     */
    @Cache(key="user{1}")
    public User getUserByUsername(String username){
        User user = new User();
        user.setUsername(username);
        return mapper.selectOne(user);
    }


    public TableResultResponse<User> page(AdminQuery query) {
        List<User> list = mapper.page(query);
        Long total = mapper.count(query);
        return new TableResultResponse<>(total,list);
    }
}
