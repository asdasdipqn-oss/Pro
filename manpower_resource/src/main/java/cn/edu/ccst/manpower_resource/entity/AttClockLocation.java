package cn.edu.ccst.manpower_resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打卡地点实体
 */
@Data
@TableName("att_clock_location")
@Schema(description = "打卡地点")
public class AttClockLocation {

    @TableId(type = IdType.AUTO)
    @Schema(description = "地点ID")
    private Long id;

    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "地点地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "打卡范围(米)")
    private Integer radius;

    @Schema(description = "状态:0-禁用 1-启用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;
}
