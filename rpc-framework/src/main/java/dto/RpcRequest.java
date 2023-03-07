package dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class RpcRequest {
    private String interfaceName;
    private String methodName;

}
