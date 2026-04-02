package cn.edu.ccst.manpower_resource.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 打卡记录视图对象（包含迟到早退时间）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AttClockRecordVO对象", description = "打卡记录视图对象")
public class AttClockRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
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

    @ApiModelProperty(value = "迟到分钟数")
    private Integer lateMinutes;

    @ApiModelProperty(value = "早退分钟数")
    private Integer earlyMinutes;
}
