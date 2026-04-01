package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "审批记录VO")
public class ApprovalRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "业务类型：1-请假 2-考勤异常 3-调岗 4-离职")
    private Integer businessType;

    @Schema(description = "业务类型名称")
    private String businessTypeName;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "流程ID")
    private Long flowId;

    @Schema(description = "流程名称")
    private String flowName;

    @Schema(description = "当前节点ID")
    private Long nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批状态：0-待审批 1-已通过 2-已驳回")
    private Integer status;

    @Schema(description = "审批状态名称")
    private String statusName;

    @Schema(description = "审批意见")
    private String opinion;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请人部门")
    private String deptName;

    public String getBusinessTypeName() {
        if (businessType == null) return "";
        switch (businessType) {
            case 1: return "请假";
            case 2: return "考勤异常";
            case 3: return "调岗";
            case 4: return "离职";
            default: return "";
        }
    }

    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "";
        }
    }
}
