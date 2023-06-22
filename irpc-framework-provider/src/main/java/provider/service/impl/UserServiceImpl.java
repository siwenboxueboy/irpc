package provider.service.impl;

import cn.hutool.core.util.IdUtil;
import common.IRpcService;
import interfaces.user.UserRpcService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@IRpcService
public class UserServiceImpl implements UserRpcService {

    /**
     * @return
     */
    @Override
    public String getUserId() {
        String simpleUUID = IdUtil.fastSimpleUUID();
        log.info("即将返回的数据是 [{}]", simpleUUID);
        return simpleUUID;
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public List<Map<String, String>> findMyGoods(String userId) {
        log.info("入参是 [{}]", userId);
        List<Map<String, String>> maps = Collections.singletonList(Collections.singletonMap(IdUtil.fastSimpleUUID(), IdUtil.fastUUID()));
        log.info("即将返回的数据是 [{}]", maps);
        return maps;
    }
}
