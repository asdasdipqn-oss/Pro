package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "离职预测结果")
public class ResignationPredictVO {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "所属部门")
    private String departmentName;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "离职风险等级：LOW-低风险 MEDIUM-中风险 HIGH-高风险")
    private String riskLevel;

    @Schema(description = "离职概率（0-100）")
    private BigDecimal resignationProbability;

    @Schema(description = "应出勤天数")
    private Integer totalWorkDays;

    @Schema(description = "实际出勤天数")
    private Integer actualAttendDays;

    @Schema(description = "考勤出勤率（%）")
    private BigDecimal attendanceRate;

    @Schema(description = "迟到次数")
    private Integer lateCount;

    @Schema(description = "早退次数")
    private Integer earlyLeaveCount;

    @Schema(description = "缺勤次数")
    private Integer absentCount;

    @Schema(description = "请假天数")
    private BigDecimal leaveDays;

    @Schema(description = "AI分析结果")
    private String aiAnalysis;

    @Schema(description = "建议措施")
    private String suggestions;

    @Schema(description = "分析月数")
    private Integer analysisMonths;
}
