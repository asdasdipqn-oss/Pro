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
 * 考勤异常申诉表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("att_appeal")
@ApiModel(value="AttAppeal对象", description="考勤异常申诉表")
public class AttAppeal implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "申诉ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "申诉单号")
    private String appealCode;

    @ApiModelProperty(value = "申诉人ID")
    private Long employeeId;

    @ApiModelProperty(value = "申诉日期")
    private LocalDate appealDate;

    @ApiModelProperty(value = "申诉类型：1-漏打卡 2-定位异常 3-其他")
    private Integer appealType;

    @ApiModelProperty(value = "打卡类型：1-上班卡 2-下班卡")
    private Integer clockType;

    @ApiModelProperty(value = "申诉原因")
    private String appealReason;

    @ApiModelProperty(value = "证明材料URL")
    private String proofUrl;

    @ApiModelProperty(value = "状态：0-待审批 1-审批中 2-已通过 3-已驳回 4-已撤回")
    private Integer status;

    @ApiModelProperty(value = "申诉时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
