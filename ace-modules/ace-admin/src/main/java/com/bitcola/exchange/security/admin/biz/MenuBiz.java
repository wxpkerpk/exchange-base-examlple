package com.bitcola.exchange.security.admin.biz;

import com.ace.cache.annotation.Cache;
import com.ace.cache.annotation.CacheClear;
import com.bitcola.exchange.security.admin.constant.AdminCommonConstant;
import com.bitcola.exchange.security.admin.mapper.MenuMapper;
import com.bitcola.exchange.security.admin.entity.Menu;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-06-12 8:48
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MenuBiz extends BaseBiz<MenuMapper, Menu> {
    @Override
    @Cache(key="permission:menu")
    public List<Menu> selectListAll() {
        return super.selectListAll();
    }

    @Override
    @CacheClear(keys={"permission:menu","permission"})
    public void insertSelective(Menu entity) {
        if (AdminCommonConstant.ROOT.equals(entity.getParentId())) {
            entity.setPath("/" + entity.getCode());
        } else {
            Menu parent = this.selectById(entity.getParentId());
            entity.setPath(parent.getPath() + "/" + entity.getCode());
        }
        entity.setId(UUID.randomUUID().toString());
        super.insertSelective(entity);
    }

    @Override
    @CacheClear(keys={"permission:menu","permission"})
    public void updateById(Menu entity) {
        if (AdminCommonConstant.ROOT.equals(entity.getParentId())) {
            entity.setPath("/" + entity.getCode());
        } else {
            Menu parent = this.selectById(entity.getParentId());
            entity.setPath(parent.getPath() + "/" + entity.getCode());
        }
        super.updateById(entity);
    }

    @Override
    @CacheClear(keys={"permission:menu","permission"})
    public void updateSelectiveById(Menu entity) {
        super.updateSelectiveById(entity);
    }

    /**
     * 获取用户可以访问的菜单
     *
     * @param id
     * @return
     */
    @Cache(key = "permission:menu:u{1}")
    public List<Menu> getUserAuthorityMenuByUserId(String id) {
        return mapper.selectAuthorityMenuByUserId(id);
    }

    /**
     * 根据用户获取可以访问的系统
     *
     * @param id
     * @return
     */
    public List<Menu> getUserAuthoritySystemByUserId(String id) {
        return mapper.selectAuthoritySystemByUserId(id);
    }
}
