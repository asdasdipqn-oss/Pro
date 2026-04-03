package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "培训需求VO")
public class TrainRequestVO {

    @Schema(description = "需求ID")
    private Long id;

    @Schema(description = "需求编号")
    private String requestCode;

    @Schema(description = "培训主题")
    private String trainTitle;

    @Schema(description = "培训类型")
    private Integer trainType;

    @Schema(description = "培训类型名称")
    private String trainTypeName;

    @Schema(description = "期望培训时间")
    private LocalDateTime expectedDate;

    @Schema(description = "期望培训时长（小时）")
    private Integer expectedHours;

    @Schema(description = "培训需求描述")
    private String requestReason;

    @Schema(description = "参与人数")
    private Integer participantCount;

    @Schema(description = "审批状态")
    private Integer status;

    @Schema(description = "审批状态名称")
    private String statusName;

    @Schema(description = "审批意见")
    private String approvalComment;

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "申请人部门")
    private String deptName;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
