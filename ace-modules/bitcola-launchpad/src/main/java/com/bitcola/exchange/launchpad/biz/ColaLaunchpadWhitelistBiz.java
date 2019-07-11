package com.bitcola.exchange.launchpad.biz;

import com.bitcola.exchange.launchpad.entity.ColaLaunchpadApply;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadWhitelist;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadApplyMapper;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadWhitelistMapper;
import com.bitcola.exchange.launchpad.util.ParamsUtil;
import com.bitcola.exchange.launchpad.vo.ColaLaunchpadApplyVo;
import com.bitcola.exchange.launchpad.vo.ColaLaunchpadWhitelistVo;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.Query;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-03-13 15:47
 **/
@Service
public class ColaLaunchpadWhitelistBiz extends BaseBiz<ColaLaunchpadWhitelistMapper, ColaLaunchpadWhitelist> {
    public TableResultResponse<ColaLaunchpadWhitelistVo> page(Query query) {
        Example example = new Example(ColaLaunchpadWhitelist.class);
        if(query.entrySet().size()>0) {
            Example.Criteria criteria = example.createCriteria();
            for (Map.Entry<String, Object> entry : query.entrySet()) {
                criteria.andLike(entry.getKey(), "%" + entry.getValue().toString() + "%");
            }
        }
        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
        List<ColaLaunchpadWhitelist> list = mapper.selectByExample(example);
        List<ColaLaunchpadWhitelistVo> voList = new ArrayList<>();
        for (ColaLaunchpadWhitelist apply : list) {
            ColaLaunchpadWhitelistVo vo = new ColaLaunchpadWhitelistVo();
            vo.setUserId(apply.getUserId());
            vo.setStatus(apply.getStatus());
            vo.setReason(apply.getReason());
            vo.setDetail(ParamsUtil.toMap(apply.getDetail()));
            voList.add(vo);
        }
        return new TableResultResponse<ColaLaunchpadWhitelistVo>(result.getTotal(), voList);
    }
}
