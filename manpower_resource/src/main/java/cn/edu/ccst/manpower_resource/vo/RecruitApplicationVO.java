package cn.edu.ccst.manpower_resource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 招聘投递记录VO
 */
@Data
@ApiModel(value="RecruitApplicationVO", description="投递申请记录VO")
public class RecruitApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "投递ID")
    private Long id;

    @ApiModelProperty(value = "岗位ID")
    private Long jobId;

    @ApiModelProperty(value = "求职者ID")
    private Long jobSeekerId;

    @ApiModelProperty(value = "岗位名称")
    private String jobName;

    @ApiModelProperty(value = "岗位代码")
    private String jobCode;

    @ApiModelProperty(value = "投递状态：0-待处理 1-已查看 2-面试 3-已录用")
    private Integer status;

    @ApiModelProperty(value = "状态描述")
    private String statusText;

    @ApiModelProperty(value = "求职信")
    private Integer coverLetter;

    @ApiModelProperty(value = "个人说明")
    private String selfIntro;

    @ApiModelProperty(value = "申请时间")
    private LocalDateTime applyTime;

    @ApiModelProperty(value = "HR备注")
    private String hrComment;

    @ApiModelProperty(value = "HR审核时间")
    private LocalDateTime hrReviewTime;
}
