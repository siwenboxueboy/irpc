package common.event;

public class IRpcNodeChangeEvent implements IRpcEvent {

    private Object data;

    public IRpcNodeChangeEvent(Object data) {
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
