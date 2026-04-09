package cn.edu.ccst.manpower_resource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 招聘岗位VO
 */
@Data
@ApiModel(value="RecruitJobVO", description="招聘岗位VO")
public class RecruitJobVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "岗位ID")
    private Long id;

    @ApiModelProperty(value = "岗位代码")
    private String jobCode;

    @ApiModelProperty(value = "岗位名称")
    private String jobName;

    @ApiModelProperty(value = "部门ID")
    private Long deptId;

    @ApiModelProperty(value = "岗位ID")
    private Long positionId;

    @ApiModelProperty(value = "招聘人数")
    private Integer headcount;

    @ApiModelProperty(value = "已录用人数")
    private Integer hiredCount;

    @ApiModelProperty(value = "最低薪资")
    private BigDecimal salaryMin;

    @ApiModelProperty(value = "最高薪资")
    private BigDecimal salaryMax;

    @ApiModelProperty(value = "学历要求")
    private Integer educationReq;

    @ApiModelProperty(value = "工作经验要求（年）")
    private String experienceReq;

    @ApiModelProperty(value = "岗位描述")
    private String jobDescription;

    @ApiModelProperty(value = "任职要求")
    private String requirements;

    @ApiModelProperty(value = "福利待遇")
    private String benefits;

    @ApiModelProperty(value = "工作地点")
    private String location;

    @ApiModelProperty(value = "发布人ID")
    private Long publisherId;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty(value = "截止时间")
    private LocalDateTime deadline;

    @ApiModelProperty(value = "状态：0-草稿 1-已发布 2-已关闭 3-已下架")
    private Integer status;

    @ApiModelProperty(value = "发布人姓名")
    private String publisherName;

    @ApiModelProperty(value = "学历要求文本")
    private String educationReqText;

    @ApiModelProperty(value = "经验要求文本")
    private String experienceReqText;

    @ApiModelProperty(value = "薪资范围文本")
    private String salaryRange;

    @ApiModelProperty(value = "发布时间文本")
    private String publishTimeText;

    @ApiModelProperty(value = "截止时间文本")
    private String deadlineText;
}
