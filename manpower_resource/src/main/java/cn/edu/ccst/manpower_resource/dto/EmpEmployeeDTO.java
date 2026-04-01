package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "员工DTO")
public class EmpEmployeeDTO {

    @Schema(description = "员工ID")
    private Long id;

    @NotBlank(message = "员工工号不能为空")
    @Schema(description = "员工工号", required = true)
    private String empCode;

    @NotBlank(message = "员工姓名不能为空")
    @Schema(description = "员工姓名", required = true)
    private String empName;

    @Schema(description = "性别:1-男 2-女")
    private Integer gender;

    @Schema(description = "出生日期")
    private LocalDate birthDate;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "岗位ID")
    private Long positionId;

    @Schema(description = "直属上级ID")
    private Long directLeaderId;

    @Schema(description = "入职日期")
    private LocalDate hireDate;

    @Schema(description = "员工状态:1-在职 2-离职 3-试用期")
    private Integer empStatus;

    @Schema(description = "员工类型:1-正式 2-实习 3-兼职 4-外包")
    private Integer empType;

    @Schema(description = "学历:1-高中 2-大专 3-本科 4-硕士 5-博士")
    private Integer education;

    @Schema(description = "现住址")
    private String address;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系人电话")
    private String emergencyPhone;

    @Schema(description = "备注")
    private String remark;
}
