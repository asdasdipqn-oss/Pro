package cn.edu.ccst.manpower_resource.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 求职者注册请求DTO
 */
@Data
@ApiModel(value="CandidateRegisterRequest", description="求职者注册请求")
public class CandidateRegisterRequest {

    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    private String username;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20个字符")
    private String password;

    @ApiModelProperty(value = "确认密码", required = true)
    @NotBlank(message = "请确认密码")
    private String confirmPassword;
}
