package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "审批流程节点VO")
public class ApprovalFlowNodeVO {

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点顺序")
    private Integer nodeOrder;

    @Schema(description = "审批人类型：1-指定人员 2-指定角色 3-部门负责人 4-直属领导")
    private Integer approverType;

    @Schema(description = "审批人类型名称")
    private String approverTypeName;

    @Schema(description = "审批人/角色ID")
    private Long approverId;

    @Schema(description = "审批人/角色名称")
    private String approverName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public String getApproverTypeName() {
        if (approverType == null) return "";
        switch (approverType) {
            case 1: return "指定人员";
            case 2: return "指定角色";
            case 3: return "部门负责人";
            case 4: return "直属领导";
            default: return "";
        }
    }
}
