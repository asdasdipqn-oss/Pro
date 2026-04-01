package cn.edu.ccst.manpower_resource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "AssessSubmissionVO", description = "考核提交VO（含员工信息）")
public class AssessSubmissionVO {

    @ApiModelProperty(value = "提交ID")
    private Long id;

    @ApiModelProperty(value = "关联任务ID")
    private Long taskId;

    @ApiModelProperty(value = "任务标题")
    private String taskTitle;

    @ApiModelProperty(value = "提交员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "员工工号")
    private String empCode;

    @ApiModelProperty(value = "员工姓名")
    private String empName;

    @ApiModelProperty(value = "提交用户ID")
    private Long userId;

    @ApiModelProperty(value = "文件原始名称")
    private String fileName;

    @ApiModelProperty(value = "文件存储路径")
    private String filePath;

    @ApiModelProperty(value = "文件大小(字节)")
    private Long fileSize;

    @ApiModelProperty(value = "提交备注")
    private String remark;

    @ApiModelProperty(value = "得分")
    private BigDecimal score;

    @ApiModelProperty(value = "等级")
    private String grade;

    @ApiModelProperty(value = "评分人用户ID")
    private Long scorerId;

    @ApiModelProperty(value = "评分评语")
    private String scorerComment;

    @ApiModelProperty(value = "状态：0-待评分 1-已评分 2-已退回")
    private Integer status;

    @ApiModelProperty(value = "提交时间")
    private LocalDateTime submitTime;

    @ApiModelProperty(value = "评分时间")
    private LocalDateTime scoreTime;

    @ApiModelProperty(value = "修订版本号（同一任务同一用户从1开始递增）")
    private Integer revisionNo;
}
