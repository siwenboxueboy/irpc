package common.constants;

public class RpcConstants {
    public static final short MAGIC_NUMBER = 1231;

    public static final Integer DEFAULT_TIMEOUT = 3000;

    public static final Integer DEFAULT_THREAD_NUMS = 256;

    public static final Integer DEFAULT_QUEUE_SIZE = 512;

    public static final Integer DEFAULT_MAX_CONNECTION_NUMS = DEFAULT_THREAD_NUMS + DEFAULT_QUEUE_SIZE;

    // 默认的分隔符
    public static final String DEFAULT_DECODE_CHAR = "$_i0#Xsop1_$";

    public static final int SERVER_DEFAULT_MSG_LENGTH = 1024 * 10;

    public static final int CLIENT_DEFAULT_MSG_LENGTH = 1024 * 10;
}
