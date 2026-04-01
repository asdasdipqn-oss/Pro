package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
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
 * 考勤日统计表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("att_daily_stat")
@ApiModel(value="AttDailyStat对象", description="考勤日统计表")
public class AttDailyStat implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "统计日期")
    private LocalDate statDate;

    @ApiModelProperty(value = "实际上班打卡时间")
    private LocalDateTime clockInTime;

    @ApiModelProperty(value = "实际下班打卡时间")
    private LocalDateTime clockOutTime;

    @ApiModelProperty(value = "工作时长(分钟)")
    private Integer workDuration;

    @ApiModelProperty(value = "考勤状态：0-未打卡 1-正常 2-迟到 3-早退 4-迟到+早退 5-缺勤 6-请假 7-出差")
    private Integer status;

    @ApiModelProperty(value = "迟到分钟数")
    private Integer lateMinutes;

    @ApiModelProperty(value = "早退分钟数")
    private Integer earlyMinutes;

    @ApiModelProperty(value = "是否工作日：0-否 1-是")
    private Integer isWorkday;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
