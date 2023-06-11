package enums;

import serialize.SerializeFactory;
import serialize.fastjson.FastJsonSerializeFactory;
import serialize.hessian.HessianSerializeFactory;
import serialize.jdk.JdkSerializeFactory;
import serialize.kryo.KryoSerializeFactory;

public enum SerializeEnum {

    HESSION_2, KRYO, JDK, FAST_JSON;

    SerializeEnum() {
    }

    public static SerializeFactory getSerializeFactory(SerializeEnum serializeEnum) {
        if (serializeEnum == null) {
            return new JdkSerializeFactory();
        }
        switch (serializeEnum) {
            case JDK:
                return new JdkSerializeFactory();
            case FAST_JSON:
                return new FastJsonSerializeFactory();
            case HESSION_2:
                return new HessianSerializeFactory();
            case KRYO:
                return new KryoSerializeFactory();
            default:
                throw new RuntimeException("no match serialize type for " + serializeEnum.name());
        }
    }
}
