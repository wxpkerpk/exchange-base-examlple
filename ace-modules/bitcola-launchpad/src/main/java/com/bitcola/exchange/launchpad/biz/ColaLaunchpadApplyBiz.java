package com.bitcola.exchange.launchpad.biz;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadApply;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadApplyMapper;
import com.bitcola.exchange.launchpad.service.ClearService;
import com.bitcola.exchange.launchpad.util.ParamsUtil;
import com.bitcola.exchange.launchpad.vo.ColaLaunchpadApplyVo;
import com.bitcola.exchange.launchpad.vo.Community;
import com.bitcola.exchange.launchpad.vo.IeoParams;
import com.bitcola.exchange.launchpad.vo.ProjectParams;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.exchange.security.common.util.Query;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-03-13 15:47
 **/
@Service
public class ColaLaunchpadApplyBiz extends BaseBiz<ColaLaunchpadApplyMapper, ColaLaunchpadApply> {

    @Autowired
    ClearService clearService;

    public TableResultResponse<ColaLaunchpadApplyVo> page(Query query) {
        Example example = new Example(ColaLaunchpadApply.class);
        if(query.entrySet().size()>0) {
            Example.Criteria criteria = example.createCriteria();
            for (Map.Entry<String, Object> entry : query.entrySet()) {
                criteria.andLike(entry.getKey(), "%" + entry.getValue().toString() + "%");
            }
        }
        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
        List<ColaLaunchpadApply> list = mapper.selectByExample(example);
        List<ColaLaunchpadApplyVo> voList = new ArrayList<>();
        for (ColaLaunchpadApply apply : list) {
            ColaLaunchpadApplyVo vo = new ColaLaunchpadApplyVo();
            vo.setUserId(apply.getUserId());
            vo.setStatus(apply.getStatus());
            vo.setReason(apply.getReason());
            vo.setDetail(ParamsUtil.toMap(apply.getDetail()));
            voList.add(vo);
        }
        return new TableResultResponse<ColaLaunchpadApplyVo>(result.getTotal(), voList);
    }

    public void saveProject(ProjectParams params) {
        mapper.saveProject(params);
    }

    public TableResultResponse projectList(AdminQuery query) {
        Long total = mapper.projectTotal();
        List<ProjectParams> list = mapper.projectList(query);
        for (ProjectParams project : list) {
            project.setCommunity(JSONObject.parseArray(project.getCommunityStr(), Community.class));
            project.setDetail(ParamsUtil.toList(project.getDetailStr(),ParamsUtil.COMMA_5_SPLIT));
            project.setDetailCn(ParamsUtil.toList(project.getDetailCnStr(),ParamsUtil.COMMA_5_SPLIT));
            project.setCommunityStr(null);
            project.setDetailStr(null);
            project.setDetailCnStr(null);
        }
        return new TableResultResponse(total,list);
    }

    public List<IeoParams> ieoList(String id) {
        List<IeoParams> list = mapper.ieoList(id);
        for (IeoParams ieo : list) {
            ieo.setSymbols(ParamsUtil.toList(ieo.getSymbolStr(),ParamsUtil.COMMA_SPLIT));
            ieo.setSymbolStr(null);
        }
        return list;
    }

    @Transactional
    public void startIeo(IeoParams params) {
        mapper.saveIeo(params);
        //  冻结项目方资金
        mapper.frozenProjectBalance(params.getUserId()+params.getCoinCode(),params.getNumber(), EncoderUtil.BALANCE_KEY);
        //  开启交易线程
        clearService.start(params.getId(),params.getNumber(),params.getEnd());
    }
}
