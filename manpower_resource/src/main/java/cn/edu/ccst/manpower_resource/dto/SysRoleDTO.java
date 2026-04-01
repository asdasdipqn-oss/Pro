package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色DTO")
public class SysRoleDTO {

    @Schema(description = "角色ID")
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", required = true)
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", required = true)
    private String roleName;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "数据权限范围:1-全部 2-本部门及子部门 3-仅本部门 4-仅本人")
    private Integer dataScope;

    @Schema(description = "状态:0-禁用 1-启用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;
}
