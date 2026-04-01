package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 离职申请表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("resignation_application")
@ApiModel(value = "ResignationApplication对象", description = "离职申请表")
public class ResignationApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "申请ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "离职类型:1-主动离职 2-协商离职 3-合同到期")
    private Integer resignationType;

    @ApiModelProperty(value = "期望离职日期")
    private LocalDate expectedDate;

    @ApiModelProperty(value = "离职原因")
    private String reason;

    @ApiModelProperty(value = "工作交接人ID")
    private Long handoverTo;

    @ApiModelProperty(value = "状态:0-待审批 1-已批准 2-已拒绝 3-已撤销")
    private Integer status;

    @ApiModelProperty(value = "审批人ID")
    private Long approverId;

    @ApiModelProperty(value = "审批时间")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "审批备注")
    private String approveRemark;

    @ApiModelProperty(value = "实际离职日期")
    private LocalDate actualLeaveDate;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
