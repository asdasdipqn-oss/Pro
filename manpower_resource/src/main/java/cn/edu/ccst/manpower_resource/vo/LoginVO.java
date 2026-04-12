package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "令牌类型")
    private String tokenType;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "用户类型: EMPLOYEE-员工 CANDIDATE-求职者")
    private String userType;
}
