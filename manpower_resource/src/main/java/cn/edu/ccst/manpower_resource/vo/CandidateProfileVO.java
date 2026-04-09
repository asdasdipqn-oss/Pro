package cn.edu.ccst.manpower_resource.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 求职者个人信息VO
 */
@Data
@ApiModel(value="CandidateProfileVO", description="求职者个人信息响应")
public class CandidateProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户名")
    private String username;

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
    private String graduateDate;

    @ApiModelProperty(value = "工作年限（年）")
    private Integer workExperience;

    @ApiModelProperty(value = "期望薪资")
    private BigDecimal expectedSalary;

    @ApiModelProperty(value = "期望岗位")
    private String expectedPosition;

    @ApiModelProperty(value = "简历地址")
    private String resumeUrl;
}
