package common;

import lombok.Data;

import java.util.concurrent.Semaphore;

@Data
public class ServerServiceSemaphoreWrapper {
    public ServerServiceSemaphoreWrapper(int maxNums) {
        this.maxNums = maxNums;
        this.semaphore = new Semaphore(maxNums);
    }

    private Semaphore semaphore;

    private int maxNums;

}
