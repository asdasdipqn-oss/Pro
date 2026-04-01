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
 * 简历/候选人表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("recruit_resume")
@ApiModel(value="RecruitResume对象", description="简历/候选人表")
public class RecruitResume implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "简历ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "应聘岗位ID")
    private Long jobId;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "性别：0-女 1-男")
    private Integer gender;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "出生日期")
    private LocalDate birthday;

    @ApiModelProperty(value = "学历")
    private Integer education;

    @ApiModelProperty(value = "毕业院校")
    private String graduateSchool;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "工作年限")
    private Integer workYears;

    @ApiModelProperty(value = "当前公司")
    private String currentCompany;

    @ApiModelProperty(value = "当前职位")
    private String currentPosition;

    @ApiModelProperty(value = "期望薪资")
    private String expectedSalary;

    @ApiModelProperty(value = "简历附件URL")
    private String resumeUrl;

    @ApiModelProperty(value = "自我介绍")
    private String selfIntro;

    @ApiModelProperty(value = "简历来源")
    private String source;

    @ApiModelProperty(value = "状态：1-待筛选 2-筛选通过 3-面试中 4-已录用 5-未通过 6-已放弃")
    private Integer status;

    @ApiModelProperty(value = "投递时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
