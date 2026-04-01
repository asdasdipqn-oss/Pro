package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "考勤申诉VO")
public class AttAppealVO {

    @Schema(description = "申诉ID")
    private Long id;

    @Schema(description = "申诉单号")
    private String appealCode;

    @Schema(description = "申诉人ID")
    private Long employeeId;

    @Schema(description = "申诉人姓名")
    private String employeeName;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "申诉日期")
    private LocalDate appealDate;

    @Schema(description = "申诉类型：1-漏打卡 2-定位异常 3-其他")
    private Integer appealType;

    @Schema(description = "申诉类型名称")
    private String appealTypeName;

    @Schema(description = "申诉原因")
    private String appealReason;

    @Schema(description = "证明材料URL")
    private String proofUrl;

    @Schema(description = "状态：0-待审批 1-审批中 2-已通过 3-已驳回 4-已撤回")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "审批意见")
    private String approveComment;

    @Schema(description = "审批人姓名")
    private String approverName;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "申诉时间")
    private LocalDateTime createTime;
}
