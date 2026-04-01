package cn.edu.ccst.manpower_resource.entity;

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
 * 培训参与记录表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("train_participant")
@ApiModel(value="TrainParticipant对象", description="培训参与记录表")
public class TrainParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "培训计划ID")
    private Long planId;

    @ApiModelProperty(value = "员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "签到时间")
    private LocalDateTime signInTime;

    @ApiModelProperty(value = "签退时间")
    private LocalDateTime signOutTime;

    @ApiModelProperty(value = "出勤状态：0-未签到 1-已签到 2-缺席")
    private Integer attendanceStatus;

    @ApiModelProperty(value = "考核成绩")
    private Integer score;

    @ApiModelProperty(value = "培训评价")
    private String evaluation;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
