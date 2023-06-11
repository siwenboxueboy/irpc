package example;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class User implements Serializable {
    private static final long serialVersionUID = -1728196331321496561L;
    private Integer id;
    private Long tel;
}
