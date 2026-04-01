package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "审批流程节点DTO")
public class ApprovalFlowNodeDTO {

    @Schema(description = "节点ID（更新时传入）")
    private Long id;

    @NotBlank(message = "节点名称不能为空")
    @Schema(description = "节点名称")
    private String nodeName;

    @NotNull(message = "节点顺序不能为空")
    @Schema(description = "节点顺序")
    private Integer nodeOrder;

    @NotNull(message = "审批人类型不能为空")
    @Schema(description = "审批人类型：1-指定人员 2-指定角色 3-部门负责人 4-直属领导")
    private Integer approverType;

    @Schema(description = "审批人/角色ID（当类型为1或2时）")
    private Long approverId;
}
