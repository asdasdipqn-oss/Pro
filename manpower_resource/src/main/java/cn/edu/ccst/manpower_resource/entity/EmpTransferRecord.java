package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 员工异动记录表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("emp_transfer_record")
@ApiModel(value="EmpTransferRecord对象", description="员工异动记录表")
public class EmpTransferRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "异动类型：1-入职 2-转正 3-调岗 4-晋升 5-降级 6-离职")
    private Integer transferType;

    @ApiModelProperty(value = "原部门ID")
    private Long oldDeptId;

    @ApiModelProperty(value = "原岗位ID")
    private Long oldPositionId;

    @ApiModelProperty(value = "新部门ID")
    private Long newDeptId;

    @ApiModelProperty(value = "新岗位ID")
    private Long newPositionId;

    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;

    @ApiModelProperty(value = "异动原因")
    private String reason;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "创建人ID")
    private Long createBy;


}
