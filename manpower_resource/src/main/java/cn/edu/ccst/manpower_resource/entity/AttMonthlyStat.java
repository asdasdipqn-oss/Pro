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
 * 考勤月度汇总表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("att_monthly_stat")
@ApiModel(value="AttMonthlyStat对象", description="考勤月度汇总表")
public class AttMonthlyStat implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "统计年份")
    private Integer statYear;

    @ApiModelProperty(value = "统计月份")
    private Integer statMonth;

    @ApiModelProperty(value = "应出勤天数")
    private Integer workDays;

    @ApiModelProperty(value = "实际出勤天数")
    private Integer actualDays;

    @ApiModelProperty(value = "迟到次数")
    private Integer lateCount;

    @ApiModelProperty(value = "早退次数")
    private Integer earlyCount;

    @ApiModelProperty(value = "缺勤天数")
    private Integer absentCount;

    @ApiModelProperty(value = "请假天数")
    private BigDecimal leaveDays;

    @ApiModelProperty(value = "加班时长(小时)")
    private BigDecimal overtimeHours;

    @ApiModelProperty(value = "出差天数")
    private Integer travelDays;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
