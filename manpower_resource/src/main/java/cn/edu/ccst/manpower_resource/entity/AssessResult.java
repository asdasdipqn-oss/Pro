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
 * 考核结果表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("assess_result")
@ApiModel(value="AssessResult对象", description="考核结果表")
public class AssessResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "结果ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "考核方案ID")
    private Long planId;

    @ApiModelProperty(value = "被考核员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "考核人ID")
    private Long assessorId;

    @ApiModelProperty(value = "总得分")
    private BigDecimal totalScore;

    @ApiModelProperty(value = "考核等级（A/B/C/D/E）")
    private String grade;

    @ApiModelProperty(value = "自我评价")
    private String selfEvaluation;

    @ApiModelProperty(value = "考核评语")
    private String assessorComment;

    @ApiModelProperty(value = "状态：0-待考核 1-已自评 2-已考核 3-已确认")
    private Integer status;

    @ApiModelProperty(value = "考核时间")
    private LocalDateTime assessTime;

    @ApiModelProperty(value = "确认时间")
    private LocalDateTime confirmTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
