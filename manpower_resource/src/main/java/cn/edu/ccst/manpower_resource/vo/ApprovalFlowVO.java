package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "审批流程VO")
public class ApprovalFlowVO {

    @Schema(description = "流程ID")
    private Long id;

    @Schema(description = "流程编码")
    private String flowCode;

    @Schema(description = "流程名称")
    private String flowName;

    @Schema(description = "流程类型：1-请假 2-考勤异常 3-调岗 4-离职")
    private Integer flowType;

    @Schema(description = "流程类型名称")
    private String flowTypeName;

    @Schema(description = "流程描述")
    private String description;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "流程节点列表")
    private List<ApprovalFlowNodeVO> nodes;

    public String getFlowTypeName() {
        if (flowType == null) return "";
        switch (flowType) {
            case 1: return "请假";
            case 2: return "考勤异常";
            case 3: return "调岗";
            case 4: return "离职";
            default: return "";
        }
    }
}
