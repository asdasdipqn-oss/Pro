package cn.edu.ccst.manpower_resource.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * 请假申请表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("leave_application")
@ApiModel(value="LeaveApplication对象", description="请假申请表")
public class LeaveApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "申请ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "申请单号")
    private String applyCode;

    @ApiModelProperty(value = "申请人ID")
    private Long employeeId;

    @ApiModelProperty(value = "假期类型ID")
    private Long leaveTypeId;

    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "请假时长(天)")
    private BigDecimal duration;

    @ApiModelProperty(value = "请假原因")
    private String reason;

    @ApiModelProperty(value = "证明材料URL")
    private String proofUrl;

    @ApiModelProperty(value = "状态：0-待审批 1-审批中 2-已通过 3-已驳回 4-已撤回")
    private Integer status;

    @ApiModelProperty(value = "申请时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "申请人姓名(非数据库字段)")
    @TableField(exist = false)
    private String employeeName;

}
