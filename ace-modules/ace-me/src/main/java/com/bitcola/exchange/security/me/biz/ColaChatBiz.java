package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.me.mapper.ColaChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-13 11:26
 **/
@Service
public class ColaChatBiz {

    @Autowired
    ColaChatMapper mapper;

    public void saveOrUpdateGroup(String id, String avatar, String name) {
        List<String> ids = new ArrayList<>();
        ids.add(id);
        List<Map<String, String>> maps = mapper.groupInfo(ids);
        if (maps!=null && maps.size()!=0){
            mapper.updateGroup(id,avatar,name);
        } else {
            mapper.newGroup(id,avatar,name);
        }

    }

    public List<Map<String, String>> groupInfo(List<String> ids) {
        return mapper.groupInfo(ids);
    }

    public boolean checkGroupNameRepeat(String groupName) {
        int count = mapper.checkGroupNameRepeat(groupName);
        return count >= 1;
    }
}
