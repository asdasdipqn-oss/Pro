package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "考勤申诉DTO")
public class AppealDTO {

    @Schema(description = "申诉日期")
    @NotNull(message = "申诉日期不能为空")
    private LocalDate appealDate;

    @Schema(description = "申诉类型：1-漏打卡 2-定位异常 3-其他")
    @NotNull(message = "申诉类型不能为空")
    private Integer appealType;

    @Schema(description = "打卡类型：1-上班卡 2-下班卡（漏打卡申诉时必填）")
    private Integer clockType;

    @Schema(description = "申诉原因")
    @NotNull(message = "申诉原因不能为空")
    private String appealReason;

    @Schema(description = "证明材料URL")
    private String proofUrl;
}
