package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "审批流程DTO")
public class ApprovalFlowDTO {

    @Schema(description = "流程ID（更新时传入）")
    private Long id;

    @NotBlank(message = "流程编码不能为空")
    @Schema(description = "流程编码")
    private String flowCode;

    @NotBlank(message = "流程名称不能为空")
    @Schema(description = "流程名称")
    private String flowName;

    @NotNull(message = "流程类型不能为空")
    @Schema(description = "流程类型：1-请假 2-考勤异常 3-调岗 4-离职")
    private Integer flowType;

    @Schema(description = "流程描述")
    private String description;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status = 1;

    @Schema(description = "流程节点列表")
    private List<ApprovalFlowNodeDTO> nodes;
}
