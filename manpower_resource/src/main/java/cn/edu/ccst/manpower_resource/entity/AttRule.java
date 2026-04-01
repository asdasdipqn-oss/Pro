package cn.edu.ccst.manpower_resource.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 考勤规则表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("att_rule")
@ApiModel(value="AttRule对象", description="考勤规则表")
public class AttRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "规则ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "上班时间")
    private LocalTime workStartTime;

    @ApiModelProperty(value = "下班时间")
    private LocalTime workEndTime;

    @ApiModelProperty(value = "迟到阈值(分钟)，超过视为迟到")
    private Integer lateThreshold;

    @ApiModelProperty(value = "早退阈值(分钟)，提前多久下班视为早退")
    private Integer earlyThreshold;

    @ApiModelProperty(value = "缺勤阈值(分钟)，迟到超过多久视为缺勤")
    private Integer absentThreshold;

    @ApiModelProperty(value = "是否需要定位打卡：0-否 1-是")
    private Integer needLocation;

    @ApiModelProperty(value = "打卡有效范围(米)")
    private Integer locationRange;

    @ApiModelProperty(value = "工作地点纬度")
    private BigDecimal workLatitude;

    @ApiModelProperty(value = "工作地点经度")
    private BigDecimal workLongitude;

    @ApiModelProperty(value = "工作地点地址")
    private String workAddress;

    @ApiModelProperty(value = "是否默认规则：0-否 1-是")
    private Integer isDefault;

    @ApiModelProperty(value = "状态：0-禁用 1-启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;


}
