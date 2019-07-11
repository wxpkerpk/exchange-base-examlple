package com.bitcola.exchange.security.community.biz;

import com.bitcola.exchange.security.community.entity.LiveEntity;
import com.bitcola.exchange.security.community.repostory.LiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author lky
 * @create 2019-04-28 14:50
 **/
@Service
public class ColaLiveBiz {
    @Autowired
    LiveRepository liveRepository;
    @Autowired
    MongoTemplate mongoTemplate;
    public void addLive(LiveEntity liveEntity){
        String id = UUID.randomUUID().toString();
        liveEntity.setId(id);
        liveEntity.setTime(System.currentTimeMillis());
        liveRepository.insert(liveEntity);
    }

    public void changeLive(LiveEntity liveEntity){
        liveRepository.save(liveEntity);
    }

    public void deleteLive(String id){
        liveRepository.deleteById(id);
    }

    public List<LiveEntity> liveList(Long timestamp, int limit){
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "time"));
        query.addCriteria(Criteria.where("time").lt(timestamp)).limit(limit);
        List<LiveEntity> list = mongoTemplate.find(query, LiveEntity.class);
        return list;
    }
}
