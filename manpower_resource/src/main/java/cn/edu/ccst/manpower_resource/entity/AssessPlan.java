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
 * 考核方案表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("assess_plan")
@ApiModel(value="AssessPlan对象", description="考核方案表")
public class AssessPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "方案ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "方案编号")
    private String planCode;

    @ApiModelProperty(value = "方案名称")
    private String planName;

    @ApiModelProperty(value = "考核类型：1-月度考核 2-季度考核 3-年度考核 4-试用期考核")
    private Integer assessType;

    @ApiModelProperty(value = "考核年份")
    private Integer assessYear;

    @ApiModelProperty(value = "考核周期（如：2024Q1、202401）")
    private String assessPeriod;

    @ApiModelProperty(value = "适用部门（为空表示全公司）")
    private Long deptId;

    @ApiModelProperty(value = "考核开始日期")
    private LocalDate startDate;

    @ApiModelProperty(value = "考核结束日期")
    private LocalDate endDate;

    @ApiModelProperty(value = "考核说明")
    private String description;

    @ApiModelProperty(value = "状态：0-草稿 1-已发布 2-考核中 3-已结束")
    private Integer status;

    @ApiModelProperty(value = "创建人ID")
    private Long createBy;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;


}
