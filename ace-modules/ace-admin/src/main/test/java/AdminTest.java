import com.bitcola.exchange.security.admin.AdminBootstrap;
import com.bitcola.exchange.security.admin.biz.MenuBiz;
import com.bitcola.exchange.security.admin.biz.UserBiz;
import com.bitcola.exchange.security.admin.entity.Menu;
import com.bitcola.exchange.security.admin.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author zkq
 * @create 2018-10-10 18:54
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdminBootstrap.class)
public class AdminTest {

    @Autowired
    MenuBiz biz;


    @Test
    public void test1(){
        List<Menu> menus = biz.selectListAll();
        for (Menu menu : menus) {
            System.out.println(menu.getId());
        }
    }
}
