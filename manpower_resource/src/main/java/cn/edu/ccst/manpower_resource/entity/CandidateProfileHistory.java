package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * 求职者个人信息修改历史表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("candidate_profile_history")
@ApiModel(value="CandidateProfileHistory对象", description="求职者个人信息修改历史表")
public class CandidateProfileHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "历史ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "求职者ID")
    @TableField("candidate_id")
    private Long candidateId;

    @ApiModelProperty(value = "真实姓名")
    @TableField("real_name")
    private String realName;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "性别：0-女 1-男")
    private Integer gender;

    @ApiModelProperty(value = "身份证号")
    @TableField("id_card")
    private String idCard;

    @ApiModelProperty(value = "学历：1-高中 2-大专 3-本科 4-硕士 5-博士")
    private Integer education;

    @ApiModelProperty(value = "毕业院校")
    @TableField("graduate_school")
    private String graduateSchool;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "毕业日期")
    @TableField("graduate_date")
    private String graduateDate;

    @ApiModelProperty(value = "工作年限（年）")
    @TableField("work_experience")
    private Integer workExperience;

    @ApiModelProperty(value = "期望薪资")
    @TableField("expected_salary")
    private java.math.BigDecimal expectedSalary;

    @ApiModelProperty(value = "期望岗位")
    @TableField("expected_position")
    private String expectedPosition;

    @ApiModelProperty(value = "简历地址")
    @TableField("resume_url")
    private String resumeUrl;

    @ApiModelProperty(value = "提交时间")
    @TableField("submit_time")
    private LocalDateTime submitTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;
}
