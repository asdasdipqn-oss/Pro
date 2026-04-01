package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "员工VO")
public class EmpEmployeeVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "员工工号")
    private String empCode;

    @Schema(description = "员工姓名")
    private String empName;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "岗位ID")
    private Long positionId;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "入职日期")
    private LocalDate hireDate;

    @Schema(description = "员工状态")
    private Integer empStatus;

    @Schema(description = "员工类型")
    private Integer empType;

    @Schema(description = "现住址")
    private String address;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
