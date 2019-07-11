package com.bitcola.exchange.launchpad.biz;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.launchpad.constant.ProjectIeoStatus;
import com.bitcola.exchange.launchpad.dto.ColaLaunchpadProjectDto;
import com.bitcola.exchange.launchpad.dto.ColaLaunchpadProjectList;
import com.bitcola.exchange.launchpad.dto.ColaUserStatus;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadProjectIeoMapper;
import com.bitcola.exchange.launchpad.util.ParamsUtil;
import com.bitcola.exchange.launchpad.vo.ColaLaunchpadProjectVo;
import com.bitcola.exchange.launchpad.vo.Community;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-13 18:54
 **/
@Service
public class ColaLaunchpadProjectBiz {

    @Autowired
    ColaLaunchpadProjectIeoMapper mapper;

    Integer size = 8;

    public List<ColaLaunchpadProjectList> list(Integer page) {
        List<ColaLaunchpadProjectList> list = mapper.list(page,size);
        for (ColaLaunchpadProjectList project : list) {
            Integer status = project.getDbStatus();
            Long start = project.getStart();
            Long end = project.getEnd();
            long current = System.currentTimeMillis();
            if (status == 0 && start < current) { // 开始了
                mapper.updateProjectStatus(project.getId(),1);
                project.setDbStatus(1);
            }
            if (end < current){ // 结束了
                if (status < 2){
                    mapper.updateProjectStatus(project.getId(),2);
                    project.setDbStatus(2);
                }
            }
            if (ColaLanguage.getCurrentLanguage().equals(ColaLanguage.LANGUAGE_CN)){
                project.setIntroduction(project.getIntroductionCn());
                project.setTitle(project.getTitleCn());
            }
            project.setStatus(getStatus(project.getDbStatus()));
        }
        return list;
    }


    public long total() {
        Long total = mapper.total();
        return total % size == 0 ? total/size : total/size + 1;
    }


    public ColaLaunchpadProjectVo detail(String id) {
        ColaLaunchpadProjectDto detail = mapper.detail(id);
        Integer status = detail.getStatus();
        Long start = detail.getStart();
        Long end = detail.getEnd();
        if (status == 0 && start < System.currentTimeMillis()) { // 开始了
            mapper.updateProjectStatus(detail.getId(),1);
            detail.setStatus(1);
        }
        if (end < System.currentTimeMillis()){ // 结束了
            if (status < 2){
                mapper.updateProjectStatus(detail.getId(),2);
                detail.setStatus(2);
            }
        }
        ColaLaunchpadProjectVo vo = new ColaLaunchpadProjectVo();
        vo.setId(detail.getId());
        vo.setTitle(detail.getTitle());
        vo.setTitleImg(detail.getTitleImg());
        if (ColaLanguage.LANGUAGE_CN.equals(ColaLanguage.getCurrentLanguage())){
            vo.setIntroduction(detail.getIntroductionCn());
        } else {
            vo.setIntroduction(detail.getIntroduction());
        }
        vo.setStatus(getStatus(detail.getStatus()));
        vo.setPrice(detail.getPrice());
        vo.setCurrentSupply(detail.getCurrentSupply());
        vo.setStart(detail.getStart());
        vo.setEnd(detail.getEnd());
        vo.setReward(detail.getReward());
        vo.setSymbol(getSymbol(detail.getSymbol()));
        vo.setCoinCode(detail.getCoinCode());
        vo.setTotalSupply(detail.getTotalSupply());
        vo.setApplication(detail.getApplication());
        vo.setWebsite(detail.getWebsite());
        vo.setWhitePaper(detail.getWhitePaper());
        vo.setPlatform(detail.getPlatform());
        vo.setCommunity(getCommunity(detail.getCommunity()));
        vo.setIssueTime(detail.getIssueTime());
        vo.setCurrentTime(System.currentTimeMillis());
        vo.setRemain(detail.getRemain());
        vo.setAllowMinNumber(detail.getAllowMinNumber());
        vo.setAllowMaxNumber(detail.getAllowMaxNumber());
        if (ColaLanguage.LANGUAGE_CN.equals(ColaLanguage.getCurrentLanguage())){
            vo.setDetail(getDetailImage(detail.getDetailCn()));
            vo.setTitle(detail.getTitleCn());
        } else {
            vo.setDetail(getDetailImage(detail.getDetail()));
        }
        return vo;
    }

    private String getStatus(Integer dbStatus){
        if (dbStatus == 1) return ProjectIeoStatus.START;
        if (dbStatus == 2) return ProjectIeoStatus.END;
        if (dbStatus == 3) return ProjectIeoStatus.END;
        return ProjectIeoStatus.PENDING;
    }
    private List<String> getSymbol(String dbSymbol){
        List<String> symbol = new ArrayList<>();
        symbol.addAll(Arrays.asList(dbSymbol.split(ParamsUtil.COMMA_SPLIT)));
        return symbol;
    }
    private List<Community> getCommunity(String dbCommunity){
        List<Community> communities = JSONObject.parseArray(dbCommunity, Community.class);
        if (communities == null){
            communities = new ArrayList<>();
        }
        return communities;
    }
    private List<String> getDetailImage(String dbDetail){
        List<String> detail = new ArrayList<>();
        detail.addAll(Arrays.asList(dbDetail.split(ParamsUtil.COMMA_5_SPLIT)));
        return detail;
    }

    public String getUserPin(String userId){
        return mapper.getUserPin(userId);
    }

    public int frozenUserBalance(String userID, String symbol, BigDecimal frozenNumber) {
        return mapper.frozenUserBalance(userID+symbol,frozenNumber, EncoderUtil.BALANCE_KEY);
    }

    public List<ColaUserStatus> getUserPinAndKycByIds(List<String> ids) {
        return mapper.getUserPinAndKycByIds(ids);
    }
}
