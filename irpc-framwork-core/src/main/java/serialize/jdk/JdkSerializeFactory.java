package serialize.jdk;

import serialize.SerializeFactory;

import java.io.*;
/**
 * 生成的字节码流方面并不是那么精简
 * */
public class JdkSerializeFactory implements SerializeFactory {

    /**
     * 序列化
     *
     * @param t
     * @return
     */
    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try (// 字节流
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             // 对象流 将字节流包装
             ObjectOutputStream output = new ObjectOutputStream(os);) {
            output.writeObject(t);
            output.flush();
            output.close();
            data = os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return data;
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
        try (ByteArrayInputStream is = new ByteArrayInputStream(data); ObjectInputStream input = new ObjectInputStream(is);) {
            Object result = input.readObject();
            return ((T) result);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
