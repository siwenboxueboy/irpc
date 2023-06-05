package common.event;

/**
 * 具体的事件实现类
 * 节点更新事件
 * */
public class IRpcUpdateEvent implements IRpcEvent{

    private Object data;

    public IRpcUpdateEvent(Object data) {
        this.data = data;
    }

    /**
     * @return
     */
    @Override
    public Object getData() {
        return data;
    }

    /**
     * @param data
     * @return
     */
    @Override
    public IRpcEvent setData(Object data) {
        return this;
    }
}
