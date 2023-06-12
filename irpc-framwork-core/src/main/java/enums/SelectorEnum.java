package enums;

import lombok.Getter;

@Getter
public enum SelectorEnum {
    RANDOM_SELECTOR(0, "random");

    int code;
    String desc;

    SelectorEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
