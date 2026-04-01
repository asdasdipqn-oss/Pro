package cn.edu.ccst.manpower_resource.entity;

import java.math.BigDecimal;
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
 * 招聘岗位表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("recruit_job")
@ApiModel(value="RecruitJob对象", description="招聘岗位表")
public class RecruitJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "岗位ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "岗位编号")
    private String jobCode;

    @ApiModelProperty(value = "岗位名称")
    private String jobName;

    @ApiModelProperty(value = "招聘部门ID")
    private Long deptId;

    @ApiModelProperty(value = "对应岗位ID")
    private Long positionId;

    @ApiModelProperty(value = "招聘人数")
    private Integer headcount;

    @ApiModelProperty(value = "已录用人数")
    private Integer hiredCount;

    @ApiModelProperty(value = "薪资下限")
    private BigDecimal salaryMin;

    @ApiModelProperty(value = "薪资上限")
    private BigDecimal salaryMax;

    @ApiModelProperty(value = "学历要求：1-不限 2-大专 3-本科 4-硕士 5-博士")
    private Integer educationReq;

    @ApiModelProperty(value = "经验要求")
    private String experienceReq;

    @ApiModelProperty(value = "岗位描述")
    private String jobDescription;

    @ApiModelProperty(value = "任职要求")
    private String requirements;

    @ApiModelProperty(value = "福利待遇")
    private String benefits;

    @ApiModelProperty(value = "发布人ID")
    private Long publisherId;

    @ApiModelProperty(value = "发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty(value = "截止日期")
    private LocalDate deadline;

    @ApiModelProperty(value = "状态：0-草稿 1-招聘中 2-已暂停 3-已结束")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;


}
