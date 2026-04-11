package cn.edu.ccst.manpower_resource.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 求职者个人信息更新DTO
 */
@Data
@ApiModel(value="CandidateProfileDTO", description="求职者个人信息更新请求")
public class CandidateProfileDTO {

    @ApiModelProperty(value = "真实姓名")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;

    @ApiModelProperty(value = "手机号码")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    @Email(message = "请输入正确的邮箱格式")
    private String email;

    @ApiModelProperty(value = "性别：0-女 1-男")
    private Integer gender;

    @ApiModelProperty(value = "身份证号")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])\\d{3}[0-9Xx]$", message = "请输入正确的身份证号")
    private String idCard;

    @ApiModelProperty(value = "学历：1-高中 2-大专 3-本科 4-硕士 5-博士")
    @Min(value = 1, message = "请选择学历")
    private Integer education;

    @ApiModelProperty(value = "毕业院校")
    @Size(max = 100, message = "毕业院校不能超过100个字符")
    private String graduateSchool;

    @ApiModelProperty(value = "专业")
    @Size(max = 100, message = "专业不能超过100个字符")
    private String major;

    @ApiModelProperty(value = "毕业日期")
    private String graduateDate;

    @ApiModelProperty(value = "工作年限（年）")
    @Min(value = 0, message = "工作年限不能为负数")
    @Max(value = 50, message = "工作年限不能超过50年")
    private Integer workExperience;

    @ApiModelProperty(value = "期望薪资")
    @Min(value = 0, message = "期望薪资不能为负数")
    private BigDecimal expectedSalary;

    @ApiModelProperty(value = "期望岗位")
    @Size(max = 100, message = "期望岗位不能超过100个字符")
    private String expectedPosition;

    @ApiModelProperty(value = "简历地址")
    @Size(max = 500, message = "简历地址不能超过500个字符")
    private String resumeUrl;
}
