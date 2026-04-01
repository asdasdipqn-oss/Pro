package cn.edu.ccst.manpower_resource.entity;

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
 * 培训计划表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("train_plan")
@ApiModel(value="TrainPlan对象", description="培训计划表")
public class TrainPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "计划ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "计划编号")
    private String planCode;

    @ApiModelProperty(value = "计划名称")
    private String planName;

    @ApiModelProperty(value = "培训类型：1-入职培训 2-技能培训 3-管理培训 4-其他")
    private Integer trainType;

    @ApiModelProperty(value = "所属部门（为空表示公司级）")
    private Long deptId;

    @ApiModelProperty(value = "培训讲师")
    private String trainer;

    @ApiModelProperty(value = "培训地点")
    private String trainLocation;

    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "最大参与人数")
    private Integer maxParticipants;

    @ApiModelProperty(value = "培训内容描述")
    private String description;

    @ApiModelProperty(value = "培训资料URL")
    private String materialsUrl;

    @ApiModelProperty(value = "状态：0-草稿 1-已发布 2-进行中 3-已结束 4-已取消")
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
