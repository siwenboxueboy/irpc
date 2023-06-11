package serialize.fastjson;

import com.alibaba.fastjson.JSON;
import serialize.SerializeFactory;

public class FastJsonSerializeFactory implements SerializeFactory {
    /**
     * 序列化
     *
     * @param t
     * @return
     */
    @Override
    public <T> byte[] serialize(T t) {
        String jsonStr = JSON.toJSONString(t);
        return jsonStr.getBytes();
    }

    /**
     * 反序列化
     *
     * @param data
     * @param clazz
     * @return
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(new String(data), clazz);
    }
}
