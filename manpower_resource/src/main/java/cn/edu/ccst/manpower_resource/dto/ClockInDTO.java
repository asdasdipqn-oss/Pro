package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "打卡请求")
public class ClockInDTO {

    @NotNull(message = "打卡类型不能为空")
    @Schema(description = "打卡类型:1-上班打卡 2-下班打卡", required = true)
    private Integer clockType;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "打卡地址")
    private String address;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "备注")
    private String remark;
}
