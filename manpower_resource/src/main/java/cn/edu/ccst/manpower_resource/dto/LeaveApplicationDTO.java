package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "请假申请DTO")
public class LeaveApplicationDTO {

    @Schema(description = "申请ID(更新时必填)")
    private Long id;

    @NotNull(message = "假期类型不能为空")
    @Schema(description = "假期类型ID", required = true)
    private Long leaveTypeId;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(description = "请假时长(天)")
    private BigDecimal duration;

    @Schema(description = "请假原因")
    private String reason;

    @Schema(description = "证明材料URL")
    private String proofUrl;
}
