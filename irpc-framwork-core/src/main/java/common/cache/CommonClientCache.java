package common.cache;

import common.RpcInvocation;
import common.RpcProtocol;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class CommonClientCache {

    public static ArrayBlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(16);

    public static Map<String,Object> RESP_MAP = new HashMap<>();
}
