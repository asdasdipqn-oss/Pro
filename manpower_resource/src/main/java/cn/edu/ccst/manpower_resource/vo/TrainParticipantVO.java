package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "培训参与者VO")
public class TrainParticipantVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "培训计划ID")
    private Long planId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "员工工号")
    private String empCode;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "签到时间")
    private LocalDateTime signInTime;

    @Schema(description = "签退时间")
    private LocalDateTime signOutTime;

    @Schema(description = "出勤状态：0-未签到 1-已签到 2-缺席")
    private Integer attendanceStatus;

    @Schema(description = "出勤状态名称")
    private String attendanceStatusName;

    @Schema(description = "考核成绩")
    private Integer score;

    @Schema(description = "培训评价")
    private String evaluation;
}
