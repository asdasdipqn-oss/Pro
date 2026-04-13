package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "审批日志VO")
public class ApprovalLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "操作动作")
    private String action;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "审批状态：0-待审批 1-已通过 2-已驳回")
    private Integer status;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
