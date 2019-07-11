//package com.bitcola.exchange.security.gate.v2.filter;
//
//import com.bitcola.exchange.security.gate.v2.xss.HTMLFilter;
//import io.netty.buffer.ByteBufAllocator;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.core.io.buffer.DataBufferUtils;
//import org.springframework.core.io.buffer.NettyDataBufferFactory;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.net.*;
//import java.nio.CharBuffer;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * @author zkq
// * @create 2019-04-10 17:43
// **/
//@Configuration
//public class XssFilter implements GlobalFilter {
//
//    private final static HTMLFilter htmlFilter = new HTMLFilter();
//
//    private DataBuffer stringBuffer(String value) {
//        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
//
//        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
//        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
//        buffer.write(bytes);
//        return buffer;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        URI uri = exchange.getRequest().getURI();
//        ServerHttpRequest request= exchange.getRequest();
//        if(!"GET".equalsIgnoreCase(request.getMethodValue())) {//判断是否为POST请求
//            Flux<DataBuffer> body = request.getBody();
//            AtomicReference<String> bodyRef = new AtomicReference<>();//缓存读取的request body信息
//            body.subscribe(dataBuffer -> {
//                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
//                DataBufferUtils.release(dataBuffer);
//                bodyRef.set(charBuffer.toString());
//            });//读取request body到缓存
//            String bodyStr = bodyRef.get();//获取request body
//            if (StringUtils.isNotBlank(bodyStr)){
//                bodyStr = htmlFilter.filter(bodyStr);
//            } else {
//                bodyStr = "";
//            }
//            DataBuffer bodyDataBuffer = stringBuffer(bodyStr);
//            Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);
//
//            request = new ServerHttpRequestDecorator(request) {
//                @Override
//                public Flux<DataBuffer> getBody() {
//                    return bodyFlux;
//                }
//            };
//        } else {
//            String path = htmlFilter.filter(URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8));
//            request = new ServerHttpRequestDecorator(request) {
//                @Override
//                public URI getURI(){
//                    try {
//                        return new URI(path);
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    }
//                    throw new RuntimeException("地址有误");
//                }
//            };
//        }
//        return chain.filter(exchange.mutate().request(request).build());
//    }
//}
