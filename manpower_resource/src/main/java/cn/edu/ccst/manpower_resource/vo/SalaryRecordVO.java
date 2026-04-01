package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "薪资记录VO")
public class SalaryRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工工号")
    private String empCode;

    @Schema(description = "员工姓名")
    private String empName;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "薪资年份")
    private Integer salaryYear;

    @Schema(description = "薪资月份")
    private Integer salaryMonth;

    @Schema(description = "基本工资")
    private BigDecimal baseSalary;

    @Schema(description = "岗位工资")
    private BigDecimal positionSalary;

    @Schema(description = "绩效工资/全勤奖")
    private BigDecimal performanceSalary;

    @Schema(description = "加班费")
    private BigDecimal overtimePay;

    @Schema(description = "补贴")
    private BigDecimal allowance;

    @Schema(description = "奖金")
    private BigDecimal bonus;

    @Schema(description = "应发工资")
    private BigDecimal grossSalary;

    @Schema(description = "社保扣款")
    private BigDecimal socialInsurance;

    @Schema(description = "公积金扣款")
    private BigDecimal housingFund;

    @Schema(description = "个人所得税")
    private BigDecimal personalTax;

    @Schema(description = "其他扣款")
    private BigDecimal otherDeduction;

    @Schema(description = "扣款合计")
    private BigDecimal totalDeduction;

    @Schema(description = "实发工资")
    private BigDecimal netSalary;

    @Schema(description = "应出勤天数")
    private Integer workDays;

    @Schema(description = "实际出勤天数")
    private Integer actualDays;

    @Schema(description = "请假天数")
    private BigDecimal leaveDays;

    @Schema(description = "迟到次数")
    private Integer lateCount;

    @Schema(description = "状态：0-草稿 1-已确认 2-已发放")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "发放时间")
    private LocalDateTime payTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已确认";
            case 2: return "已发放";
            default: return "";
        }
    }
}
