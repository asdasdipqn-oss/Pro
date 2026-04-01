package cn.edu.ccst.manpower_resource.entity;

import java.math.BigDecimal;
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
 * 打卡记录表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("att_clock_record")
@ApiModel(value="AttClockRecord对象", description="打卡记录表")
public class AttClockRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "员工ID")
    private Long employeeId;

    @ApiModelProperty(value = "打卡日期")
    private LocalDate clockDate;

    @ApiModelProperty(value = "打卡类型：1-上班打卡 2-下班打卡")
    private Integer clockType;

    @ApiModelProperty(value = "打卡时间")
    private LocalDateTime clockTime;

    @ApiModelProperty(value = "打卡纬度")
    private BigDecimal clockLatitude;

    @ApiModelProperty(value = "打卡经度")
    private BigDecimal clockLongitude;

    @ApiModelProperty(value = "打卡地址")
    private String clockAddress;

    @ApiModelProperty(value = "定位状态：0-异常 1-正常")
    private Integer locationStatus;

    @ApiModelProperty(value = "打卡设备信息")
    private String deviceInfo;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
