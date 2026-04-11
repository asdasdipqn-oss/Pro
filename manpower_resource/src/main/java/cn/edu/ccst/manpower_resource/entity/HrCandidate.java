package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 求职者表
 * </p>
 *
 * @author
 * @since 2026-04-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hr_candidate")
@ApiModel(value="HrCandidate对象", description="求职者表")
public class HrCandidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "求职者ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码（加密存储）")
    private String password;

    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "性别：0-女 1-男")
    private Integer gender;

    @ApiModelProperty(value = "身份证号")
    private String idCard;

    @ApiModelProperty(value = "学历：1-高中 2-大专 3-本科 4-硕士 5-博士")
    private Integer education;

    @ApiModelProperty(value = "毕业院校")
    private String graduateSchool;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "毕业日期")
    private LocalDate graduateDate;

    @ApiModelProperty(value = "工作年限（年）")
    private Integer workExperience;

    @ApiModelProperty(value = "期望薪资")
    private java.math.BigDecimal expectedSalary;

    @ApiModelProperty(value = "期望岗位")
    private String expectedPosition;

    @ApiModelProperty(value = "简历URL")
    private String resumeUrl;

    @ApiModelProperty(value = "状态：0-禁用 1-正常")
    private Integer status;

    @ApiModelProperty(value = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "最后登录IP")
    private String lastLoginIp;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除：0-未删除 1-已删除")
    private Integer deleted;
}
