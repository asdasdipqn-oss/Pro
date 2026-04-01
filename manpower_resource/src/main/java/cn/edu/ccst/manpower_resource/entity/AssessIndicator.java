package cn.edu.ccst.manpower_resource.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * 考核指标表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("assess_indicator")
@ApiModel(value="AssessIndicator对象", description="考核指标表")
public class AssessIndicator implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "指标ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "考核方案ID")
    private Long planId;

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "指标类型：1-定量 2-定性")
    private Integer indicatorType;

    @ApiModelProperty(value = "权重(%)")
    private BigDecimal weight;

    @ApiModelProperty(value = "满分")
    private Integer maxScore;

    @ApiModelProperty(value = "指标说明")
    private String description;

    @ApiModelProperty(value = "评分标准")
    private String scoringStandard;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
