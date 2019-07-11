package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.me.mapper.ColaAppBannerMapper;
import com.bitcola.me.entity.ColaAppBanner;
import tk.mybatis.mapper.entity.Example;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zkq
 * @create 2019-03-20 11:04
 **/
@Service
public class ColaAppBannerBiz extends BaseBiz<ColaAppBannerMapper, ColaAppBanner> {
    public List<ColaAppBanner> banner() {
        Example example = new Example(ColaAppBanner.class);
        example.orderBy("id").asc();
        List<ColaAppBanner> colaAppBanners = mapper.selectByExample(example);
        for (ColaAppBanner colaAppBanner : colaAppBanners) {
            if (ColaLanguage.LANGUAGE_CN.equals(ColaLanguage.getCurrentLanguage())){
                colaAppBanner.setBanner(colaAppBanner.getBannerCn());
                colaAppBanner.setUrl(colaAppBanner.getUrlCn());
                colaAppBanner.setBannerCn(null);
                colaAppBanner.setUrlCn(null);
            }
        }
        return colaAppBanners;
    }
}
