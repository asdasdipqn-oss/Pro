package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 面试记录表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("recruit_interview")
@ApiModel(value="RecruitInterview对象", description="面试记录表")
public class RecruitInterview implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "面试ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "简历ID")
    private Long resumeId;

    @ApiModelProperty(value = "面试轮次")
    private Integer interviewRound;

    @ApiModelProperty(value = "面试官ID")
    private Long interviewerId;

    @ApiModelProperty(value = "面试时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime interviewTime;

    @ApiModelProperty(value = "面试方式：1-现场 2-电话 3-视频")
    private Integer interviewType;

    @ApiModelProperty(value = "面试地点")
    private String interviewAddress;

    @ApiModelProperty(value = "面试评价")
    private String evaluation;

    @ApiModelProperty(value = "评分(1-100)")
    private Integer score;

    @ApiModelProperty(value = "面试结果：1-通过 2-待定 3-不通过")
    private Integer result;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "状态：0-待面试 1-已完成 2-已取消")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
