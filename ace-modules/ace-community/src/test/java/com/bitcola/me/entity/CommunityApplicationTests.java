package com.bitcola.me.entity;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.CommunityApplication;
import com.bitcola.exchange.security.community.biz.ColaArticleListBiz;
import com.bitcola.exchange.security.community.biz.ColaArticleSearchBiz;
import com.bitcola.exchange.security.community.biz.ColaPublishArticleBiz;
import com.bitcola.exchange.security.community.entity.*;
import com.bitcola.exchange.security.community.repostory.*;
import com.bitcola.exchange.security.community.rest.ColaFeedController;
import com.bitcola.exchange.security.community.rest.ColaFollowController;
import com.netflix.discovery.converters.Auto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest(classes =  CommunityApplication.class)
public class CommunityApplicationTests {


	@Autowired
	private ShortArticleRepository shortArticleRepository;

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private ArticleItemRepository articleItemRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private LikeRepository likeRepository;

	@Autowired
	private DonateRepository donateRepository;

	@Autowired
	private ColaPublishArticleBiz articleBiz;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
    ColaArticleSearchBiz searchBiz;

	@Autowired
	ColaArticleListBiz articleListBiz;

	@Autowired
	ColaFollowController followController;

	@Autowired
	FollowRepository followRepository;

	@Autowired
	ColaFeedController feedController;


	@Test
	public void contextLoads() throws  Exception{
		// 查询出热门文章
		String id = "7be997da-8c34-4fbb-8731-ff8c1088cd7f";
		//查询出下面的评论
		Query query = new Query();
		query.addCriteria(Criteria.where("parentId").is(id));
		List<CommentEntity> commentEntities = mongoTemplate.find(query, CommentEntity.class);
		for (CommentEntity commentEntity : commentEntities) {
			System.out.println(commentEntity.getId());
		}
		//删除其中一个评论
		BaseContextHandler.setUserID("210080");
		feedController.deleteItem("comment","4bd36152-b1f6-4f34-b143-4525e46684c2",false);

		//再次查看下面的评论
		commentEntities = mongoTemplate.find(query, CommentEntity.class);
		for (CommentEntity commentEntity : commentEntities) {
			System.out.println(commentEntity.getId());
		}
	}

	/**
	 * 重置所有社区数据
	 */
	@Test
	public void contextLoads2() {
		articleItemRepository.deleteAll();
		shortArticleRepository.deleteAll();
		articleRepository.deleteAll();
		commentRepository.deleteAll();
		likeRepository.deleteAll();
		donateRepository.deleteAll();
	}

	@Test
	public void contextLoads5() {
		ShortArticleEntity shortArticleEntity = new ShortArticleEntity();
		for (int i = 0; i < 20; i++) {
			shortArticleEntity.setContent("我随便发一下内容"+i);
			BaseContextHandler.setUserID("100002");
			BaseContextHandler.setUsername("qiuqiu");
			articleBiz.publishShortArticle(shortArticleEntity);
		}

	}

	@Test
	public void contextLoads3() {
		Query query = new Query();
		query.with(new Sort(Sort.Direction.DESC, "time"));
		query.limit(5);
		List<ArticleItemEntity> all = mongoTemplate.find(query,ArticleItemEntity.class);
		for (ArticleItemEntity shortArticleEntity : all) {
			System.out.println(shortArticleEntity.getContent());
		}
	}



	@Test
	public void test8() throws Exception{
		List<ArticleItemEntity> test = searchBiz.search("test", null, 1, 10);
		System.out.println(test.size()+"    ===============================");
	}

	@Test
	public void delete(){
		followRepository.deleteAll();
	}

	@Test
    public void testFollow(){
        Map<String,String> map = new HashMap<>();
        map.put("userId","100012");
        BaseContextHandler.setUserID("100002");
		AppResponse follow = followController.follow(map);
		System.out.println(follow.getMessage());

	}

	@Test
    public void testFollow2(){
		//BaseContextHandler.setUserID("101");
		//AppResponse follow = followController.followList(0,10,"","");
		//System.out.println();
		//System.out.println();
		//System.out.println();
		//System.out.println(JSONObject.toJSONString(follow));
		//System.out.println();
		//System.out.println();
		//System.out.println();


		FollowEntity entity = new FollowEntity();
		entity.setUserId("101");
		Example example = Example.of(entity, ExampleMatcher.matching().withMatcher("followUserId",ExampleMatcher.GenericPropertyMatchers.caseSensitive()).withIgnorePaths("userId","time"));
		Page<FollowEntity> all = followRepository.findAll(example, PageRequest.of(1, 10));
		for (FollowEntity followEntity : all) {

		}

	}




	@Test
    public void testFollow3(){
		LookupOperation lookupOperation = LookupOperation.newLookup().
				from("likeEntity").//关联表名
				localField("_id").//关联字段
				foreignField("parentId").//主表关联字段对应的次表字段
				as("like");
		Criteria like = Criteria.where("like.fromUser").is("100014");
		Aggregation aggregation = Aggregation.newAggregation(lookupOperation,Aggregation.match(like));
		List<ArticleItemEntity> manEntry = mongoTemplate.aggregate(aggregation, "articleItemEntity", ArticleItemEntity.class).getMappedResults();

		for (ArticleItemEntity entity : manEntry) {

		}
	}





}
