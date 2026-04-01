package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户DTO")
public class SysUserDTO {

    @Schema(description = "用户ID(更新时必填)")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", required = true)
    private String username;

    @Schema(description = "密码(新增时必填)")
    private String password;

    @Schema(description = "关联员工ID")
    private Long employeeId;

    @Schema(description = "状态：0-冻结 1-正常")
    private Integer status;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}
