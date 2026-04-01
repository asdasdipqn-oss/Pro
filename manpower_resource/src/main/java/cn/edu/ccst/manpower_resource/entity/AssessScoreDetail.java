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
 * 考核指标得分明细表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("assess_score_detail")
@ApiModel(value="AssessScoreDetail对象", description="考核指标得分明细表")
public class AssessScoreDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "考核结果ID")
    private Long resultId;

    @ApiModelProperty(value = "指标ID")
    private Long indicatorId;

    @ApiModelProperty(value = "自评得分")
    private BigDecimal selfScore;

    @ApiModelProperty(value = "考核人评分")
    private BigDecimal assessorScore;

    @ApiModelProperty(value = "最终得分")
    private BigDecimal finalScore;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
