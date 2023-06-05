package common.event;

/**
 * 抽象的事件
 * 该事件会用于装载需要传递的数据信息
 * */
public interface IRpcEvent {
    Object getData();

    IRpcEvent setData(Object data);

}
