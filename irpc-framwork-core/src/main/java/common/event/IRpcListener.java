package common.event;

/**
 * 监听器接口
 * 监听器用于捕获对应事件并对事件进行处理
 * */
public interface IRpcListener<T> {

    void callBack(Object t);

}
