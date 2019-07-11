package com.bitcola.exchange.security.gate.v2.filter;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.security.api.vo.authority.PermissionInfo;
import com.bitcola.exchange.security.api.vo.log.LogInfo;
import com.bitcola.exchange.security.auth.client.config.ServiceAuthConfig;
import com.bitcola.exchange.security.auth.client.config.UserAuthConfig;
import com.bitcola.exchange.security.auth.client.jwt.ServiceAuthUtil;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.BaseResponse;
import com.bitcola.exchange.security.common.msg.auth.TokenForbiddenResponse;
import com.bitcola.exchange.security.gate.v2.feign.ILogService;
import com.bitcola.exchange.security.gate.v2.service.MaintainService;
import com.bitcola.exchange.security.gate.v2.service.UserService;
import com.bitcola.exchange.security.gate.v2.utils.DBLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wx
 * @create 2018/3/12.
 */
@Configuration
@Slf4j
public class AccessGatewayFilter implements GlobalFilter {

    @Autowired
    @Lazy
    private ILogService logService;

    @Autowired
    @Lazy
    private UserService userService;

    @Value("${gate.ignore.startWith}")
    private String startWith;

    //    @Value("${zuul.prefix}")
//    private String zuulPrefix;

    private static final String GATE_WAY_PREFIX = "/api";
    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    private ServiceAuthConfig serviceAuthConfig;

    @Autowired
    private UserAuthConfig userAuthConfig;

    @Autowired
    private ServiceAuthUtil serviceAuthUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, GatewayFilterChain gatewayFilterChain) {
        // log.info("check token and user permission....");
        ServerHttpRequest request = serverWebExchange.getRequest();
        String requestUri = request.getPath().pathWithinApplication().value();
        LinkedHashSet requiredAttribute = serverWebExchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        ServerHttpRequest.Builder mutate = request.mutate();

        if(requiredAttribute!=null) {
            Iterator<URI> iterator = requiredAttribute.iterator();
            while (iterator.hasNext()) {
                URI next = iterator.next();
                if (next.getPath().startsWith(GATE_WAY_PREFIX)) {
                    requestUri = next.getPath().substring(GATE_WAY_PREFIX.length());
                }
            }
        }
        final String method = request.getMethod().toString();
        BaseContextHandler.setToken(null);
        // 服务器正在维护 maintainTime 非 0 表示在维护,需要返回 maintainTime
        long maintainTime = isMaintain(requestUri);
        if (maintainTime != 0) {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
            serverWebExchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            byte[] bytes = ("{\"status\":500,\"message\":\"system maintain\",\"data\":" + maintainTime + "}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(bytes);
            return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
        }

        // 不进行拦截的地址
        if (isStartWith(requestUri)) {
            mutate.header(serviceAuthConfig.getTokenHeader(), serviceAuthUtil.getClientToken());
            ServerHttpRequest build = mutate.build();

            return gatewayFilterChain.filter(serverWebExchange.mutate().request(build).build());
        }
        IJWTInfo user = null;
        try {
            user = getJWTUser(request, mutate);
        } catch (Exception e) {
            log.error("用户Token过期异常", e);
            return getVoidMono(serverWebExchange, new TokenForbiddenResponse("User Token Forbidden or Expired!"));
        }
        List<PermissionInfo> permissionIfs = userService.getAllPermissionInfo();
        // 判断资源是否启用权限约束
        Stream<PermissionInfo> stream = getPermissionIfs(requestUri, method, permissionIfs);
        List<PermissionInfo> result = stream.collect(Collectors.toList());
        PermissionInfo[] permissions = result.toArray(new PermissionInfo[]{});
        if (permissions.length > 0) {
            if (checkUserPermission(permissions, serverWebExchange, user)) {
                return getVoidMono(serverWebExchange, new TokenForbiddenResponse("User Forbidden!Does not has Permission!"));
            }
        }
        // 申请客户端密钥头
        mutate.header(serviceAuthConfig.getTokenHeader(), serviceAuthUtil.getClientToken());
        ServerHttpRequest build = mutate.build();
        return gatewayFilterChain.filter(serverWebExchange.mutate().request(build).build());

    }


    /**
     * 网关抛异常
     *
     * @param body
     */
    @NotNull
    private Mono<Void> getVoidMono(ServerWebExchange serverWebExchange, BaseResponse body) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        byte[] bytes = JSONObject.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(bytes);
        return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
    }


    /**
     * 获取目标权限资源
     *
     * @param requestUri
     * @param method
     * @param serviceInfo
     * @return
     */
    private Stream<PermissionInfo> getPermissionIfs(final String requestUri, final String method, List<PermissionInfo> serviceInfo) {
        return serviceInfo.stream().filter(permissionInfo -> {
            String uri = permissionInfo.getUri();
            if (uri.indexOf("{") > 0) {
                uri = uri.replaceAll("\\{\\*\\}", "[a-zA-Z\\\\d]+");
            }
            String regEx = "^" + uri + "$";
            return (Pattern.compile(regEx).matcher(requestUri).find())
                    && method.equals(permissionInfo.getMethod());
        });
    }

    private void setCurrentUserInfoAndLog(ServerWebExchange serverWebExchange, IJWTInfo user, PermissionInfo pm) {
        String host = serverWebExchange.getRequest().getRemoteAddress().toString();
        LogInfo logInfo = new LogInfo(pm.getMenu(), pm.getName(), pm.getUri(), new Date(), user.getId(), user.getName(), host);
        DBLog.getInstance().setLogService(logService).offerQueue(logInfo);
    }

    /**
     * 返回session中的用户信息
     *
     * @param request
     * @param ctx
     * @return
     */
    private IJWTInfo getJWTUser(ServerHttpRequest request, ServerHttpRequest.Builder ctx) throws Exception {
        List<String> strings = request.getHeaders().get(userAuthConfig.getTokenHeader());
        String authToken = null;
        if (strings != null) {
            authToken = strings.get(0);
        }
        if (StringUtils.isBlank(authToken)) {
            strings = request.getQueryParams().get("token");
            if (strings != null) {
                authToken = strings.get(0);
            }
        }
        ctx.header(userAuthConfig.getTokenHeader(), authToken);
        BaseContextHandler.setToken(authToken);
        return userAuthUtil.getInfoFromToken(authToken);
    }


    private boolean checkUserPermission(PermissionInfo[] permissions, ServerWebExchange ctx, IJWTInfo user) {
        List<PermissionInfo> permissionInfos = userService.getPermissionByUsername(user.getUniqueName());

        PermissionInfo current = null;
        for (PermissionInfo info : permissions) {
            boolean anyMatch = permissionInfos.stream().anyMatch(permissionInfo -> permissionInfo.getCode().equals(info.getCode()));
            if (anyMatch) {
                current = info;
                break;
            }
        }
        if (current == null) {
            return true;
        } else {
            if (!RequestMethod.GET.toString().equals(current.getMethod())) {
                setCurrentUserInfoAndLog(ctx, user, current);
            }
            return false;
        }
    }


    /**
     * URI是否以什么打头
     *
     * @param requestUri
     * @return
     */
    private boolean isStartWith(String requestUri) {
        boolean flag = false;
        for (String s : startWith.split(",")) {
            if (requestUri.startsWith(s)) {
                return true;
            }
        }
        return flag;
    }

    /**
     * 网关抛异常
     *
     * @param body
     * @param code
     */
    private Mono<Void> setFailedRequest(ServerWebExchange serverWebExchange, String body, int code) {
        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        return serverWebExchange.getResponse().setComplete();
    }


    /**
     * 正在维护返回 预计维护时间,否则返回0
     *
     * @param requestUri
     * @return
     */
    private long isMaintain(String requestUri) {
        String module = getModuleFromUri(requestUri);
        Map<String, Object> map = MaintainService.modules.get(module);
        if (map == null) {
            return 0;
        }
        if (!"0".equals(map.get("status"))) {//0 表示维护
            return 0;
        } else {
            return (Long) map.get("timestamp");
        }
    }

    /**
     * 获得请求模块
     *
     * @param requestUri
     * @return
     */
    private String getModuleFromUri(String requestUri) {
        return requestUri.split("/")[1];
    }

}
