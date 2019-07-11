package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.MenuBiz;
import com.bitcola.exchange.security.admin.biz.UserBiz;
import com.bitcola.exchange.security.admin.entity.Menu;
import com.bitcola.exchange.security.admin.entity.User;
import com.bitcola.exchange.security.admin.rpc.service.PermissionService;
import com.bitcola.exchange.security.admin.vo.FrontUser;
import com.bitcola.exchange.security.admin.vo.MenuTree;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-06-08 11:51
 */
@RestController
@RequestMapping("user")
public class UserController extends BaseController<UserBiz,User> {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private MenuBiz menuBiz;

    @RequestMapping(value = "/front/info", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getUserInfo(String token) throws Exception {
        FrontUser userInfo = permissionService.getUserInfo(token);
        if(userInfo==null) {
            return ResponseEntity.status(401).body(false);
        } else {
            return ResponseEntity.ok(userInfo);
        }
    }

    @RequestMapping(value = "/front/menus", method = RequestMethod.GET)
    public @ResponseBody
    List<MenuTree> getMenusByUsername(String token) throws Exception {
        return permissionService.getMenusByUsername(token);
    }

    @RequestMapping(value = "/front/menu/all", method = RequestMethod.GET)
    public @ResponseBody
    List<Menu> getAllMenus() throws Exception {
        return menuBiz.selectListAll();
    }


    @RequestMapping(value = "/page",method = RequestMethod.GET)
    @ResponseBody
    public TableResultResponse<User> list(@RequestParam Map<String, Object> params){
        //查询列表数据
        AdminQuery query = new AdminQuery(params);
        return baseBiz.page(query);
    }



}
