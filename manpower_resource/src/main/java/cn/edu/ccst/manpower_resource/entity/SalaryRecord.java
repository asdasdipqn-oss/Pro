package cn.edu.ccst.manpower_resource.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 薪资发放记录表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("salary_record")
@ApiModel(value="SalaryRecord对象", description="薪资发放记录表")
public class SalaryRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "薪资年份")
    private Integer salaryYear;

    @ApiModelProperty(value = "薪资月份")
    private Integer salaryMonth;

    @ApiModelProperty(value = "基本工资")
    private BigDecimal baseSalary;

    @ApiModelProperty(value = "岗位工资")
    private BigDecimal positionSalary;

    @ApiModelProperty(value = "绩效工资")
    private BigDecimal performanceSalary;

    @ApiModelProperty(value = "加班费")
    private BigDecimal overtimePay;

    @ApiModelProperty(value = "补贴")
    private BigDecimal allowance;

    @ApiModelProperty(value = "奖金")
    private BigDecimal bonus;

    @ApiModelProperty(value = "应发工资")
    private BigDecimal grossSalary;

    @ApiModelProperty(value = "社保扣款")
    private BigDecimal socialInsurance;

    @ApiModelProperty(value = "公积金扣款")
    private BigDecimal housingFund;

    @ApiModelProperty(value = "个人所得税")
    private BigDecimal personalTax;

    @ApiModelProperty(value = "其他扣款")
    private BigDecimal otherDeduction;

    @ApiModelProperty(value = "扣款合计")
    private BigDecimal totalDeduction;

    @ApiModelProperty(value = "实发工资")
    private BigDecimal netSalary;

    @ApiModelProperty(value = "应出勤天数")
    private Integer workDays;

    @ApiModelProperty(value = "实际出勤天数")
    private Integer actualDays;

    @ApiModelProperty(value = "请假天数")
    private BigDecimal leaveDays;

    @ApiModelProperty(value = "迟到次数")
    private Integer lateCount;

    @ApiModelProperty(value = "状态：0-草稿 1-已确认 2-已发放")
    private Integer status;

    @ApiModelProperty(value = "确认时间")
    private LocalDateTime confirmTime;

    @ApiModelProperty(value = "发放时间")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "全勤奖")
    private BigDecimal fullAttendanceBonus;

    @ApiModelProperty(value = "个税")
    private BigDecimal incomeTax;

    @ApiModelProperty(value = "是否已推送通知:0-未推送 1-已推送")
    private Integer notified;

    @ApiModelProperty(value = "推送时间")
    private LocalDateTime notifyTime;

    @ApiModelProperty(value = "是否已读:0-未读 1-已读")
    private Integer readStatus;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
