package com.bitcola.exchange.security.auth.biz;

import com.bitcola.exchange.security.auth.entity.Client;
import com.bitcola.exchange.security.auth.entity.ClientService;
import com.bitcola.exchange.security.auth.mapper.ClientMapper;
import com.bitcola.exchange.security.auth.mapper.ClientServiceMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * 
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2017-12-26 19:43:46
 */
@Service
public class ClientBiz extends BaseBiz<ClientMapper,Client> {
    @Autowired
    private ClientServiceMapper clientServiceMapper;
    @Autowired
    private ClientServiceBiz clientServiceBiz;

    public List<Client> getClientServices(String id) {
        return mapper.selectAuthorityServiceInfo(id);
    }

    public void modifyClientServices(String id, String clients) {
        clientServiceMapper.deleteByServiceId(id);
        if (!StringUtils.isEmpty(clients)) {
            String[] mem = clients.split(",");
            for (String m : mem) {
                ClientService clientService = new ClientService();
                clientService.setServiceId(m);
                clientService.setClientId(id);
                clientService.setId(UUID.randomUUID().toString());
                clientServiceBiz.insertSelective(clientService);
            }
        }
    }
}