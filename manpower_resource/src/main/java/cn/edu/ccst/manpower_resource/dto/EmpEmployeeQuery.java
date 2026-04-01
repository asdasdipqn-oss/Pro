package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "员工查询条件")
public class EmpEmployeeQuery extends PageQuery {

    @Schema(description = "工号(模糊)")
    private String empCode;

    @Schema(description = "姓名(模糊)")
    private String empName;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "岗位ID")
    private Long positionId;

    @Schema(description = "员工状态")
    private Integer empStatus;

    @Schema(description = "员工类型")
    private Integer empType;
}
