package firefly.models;

import lombok.Data;

@Data
public class JobRequest {

    private String jobName;
    private int warehouseId;
    private String data;
    private String email;
}
