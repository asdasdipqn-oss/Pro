package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 员工信息表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("emp_employee")
@ApiModel(value="EmpEmployee对象", description="员工信息表")
public class EmpEmployee implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "员工ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "员工工号")
    private String empCode;

    @ApiModelProperty(value = "员工姓名")
    private String empName;

    @ApiModelProperty(value = "性别：0-女 1-男")
    private Integer gender;

    @ApiModelProperty(value = "身份证号")
    private String idCard;

    @ApiModelProperty(value = "出生日期")
    private LocalDate birthday;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @ApiModelProperty(value = "所属部门ID")
    private Long deptId;

    @ApiModelProperty(value = "部门名称（非数据库字段，仅用于返回）")
    @TableField(exist = false)
    private String deptName;

    @ApiModelProperty(value = "岗位ID")
    private Long positionId;

    @ApiModelProperty(value = "直属领导ID")
    private Long directLeaderId;

    @ApiModelProperty(value = "员工状态：1-在职 2-试用期 3-离职")
    private Integer empStatus;

    @ApiModelProperty(value = "员工类型：1-正式 2-实习 3-外包")
    private Integer empType;

    @ApiModelProperty(value = "入职日期")
    private LocalDate hireDate;

    @ApiModelProperty(value = "转正日期")
    private LocalDate regularizationDate;

    @ApiModelProperty(value = "离职日期")
    private LocalDate leaveDate;

    @ApiModelProperty(value = "现居住地址")
    private String address;

    @ApiModelProperty(value = "籍贯")
    private String nativePlace;

    @ApiModelProperty(value = "紧急联系人")
    private String emergencyContact;

    @ApiModelProperty(value = "紧急联系人电话")
    private String emergencyPhone;

    @ApiModelProperty(value = "紧急联系人关系")
    private String emergencyRelation;

    @ApiModelProperty(value = "学历：1-高中 2-大专 3-本科 4-硕士 5-博士")
    private Integer education;

    @ApiModelProperty(value = "毕业院校")
    private String graduateSchool;

    @ApiModelProperty(value = "专业")
    private String major;

    @ApiModelProperty(value = "毕业日期")
    private LocalDate graduateDate;

    @ApiModelProperty(value = "开户银行")
    private String bankName;

    @ApiModelProperty(value = "银行卡号")
    private String bankAccount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    @TableLogic
    private Integer deleted;


}
