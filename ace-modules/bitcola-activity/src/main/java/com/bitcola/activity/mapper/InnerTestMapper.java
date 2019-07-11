package com.bitcola.activity.mapper;

import com.bitcola.activity.entity.InnerTest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-29 21:27
 **/
@Repository
public interface InnerTestMapper extends Mapper<InnerTest> {

    List<Map<String, Object>> innerTest(@Param("startTime") Long startTime,@Param("endTime") Long endTime);

    BigDecimal total();

}
