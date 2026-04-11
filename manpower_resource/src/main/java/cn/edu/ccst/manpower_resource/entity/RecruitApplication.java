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
 * 招聘投递申请记录表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("recruit_application")
@ApiModel(value="RecruitApplication对象", description="投递申请记录表")
public class RecruitApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "投递ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "岗位ID")
    private Long jobId;

    @ApiModelProperty(value = "求职者ID")
    private Long jobSeekerId;

    @ApiModelProperty(value = "简历ID（可为空）")
    private Long resumeId;

    @ApiModelProperty(value = "投递状态：0-待处理 1-已查看 2-面试 3-已录用")
    private Integer status;

    @ApiModelProperty(value = "求职信")
    private Integer coverLetter;

    @ApiModelProperty(value = "个人说明")
    private String selfIntro;

    @ApiModelProperty(value = "HR备注")
    private String hrComment;

    @ApiModelProperty(value = "HR审核时间")
    private LocalDateTime hrReviewTime;

    @ApiModelProperty(value = "申请时间")
    private LocalDateTime applyTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
