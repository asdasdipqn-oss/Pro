package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "审批请求DTO")
public class ApproveDTO {

    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型：1-请假 2-考勤异常 3-调岗 4-离职")
    private Integer businessType;

    @NotNull(message = "业务ID不能为空")
    @Schema(description = "业务ID")
    private Long businessId;

    @NotNull(message = "审批结果不能为空")
    @Schema(description = "审批状态：1-通过 2-驳回")
    private Integer status;

    @Schema(description = "审批意见")
    private String opinion;
}
