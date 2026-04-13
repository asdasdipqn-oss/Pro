package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询条件")
public class SysUserQuery extends PageQuery {

    @Schema(description = "用户名(模糊)")
    private String username;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "关联员工ID")
    private Long employeeId;
}
