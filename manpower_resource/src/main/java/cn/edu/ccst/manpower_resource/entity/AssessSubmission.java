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

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("assess_submission")
@ApiModel(value = "AssessSubmission对象", description = "考核提交表")
public class AssessSubmission implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "提交ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "关联任务ID")
    private Long taskId;

    @ApiModelProperty(value = "提交员工ID")
    private Long employeeId;

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

    @ApiModelProperty(value = "等级(A/B/C/D/E)")
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

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
