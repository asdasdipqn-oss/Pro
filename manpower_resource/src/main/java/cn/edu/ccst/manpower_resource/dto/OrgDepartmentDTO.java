package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "部门DTO")
public class OrgDepartmentDTO {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "父部门ID")
    private Long parentId;

    @NotBlank(message = "部门编码不能为空")
    @Schema(description = "部门编码", required = true)
    private String deptCode;

    @NotBlank(message = "部门名称不能为空")
    @Schema(description = "部门名称", required = true)
    private String deptName;

    @Schema(description = "部门负责人ID")
    private Long leaderId;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态:0-禁用 1-启用")
    private Integer status;
}
