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
 * 培训需求表
 * </p>
 *
 * @author
 * @since 2026-04-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("train_request")
@ApiModel(value="TrainRequest对象", description="培训需求表")
public class TrainRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "需求ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "需求编号")
    private String requestCode;

    @ApiModelProperty(value = "培训主题")
    private String trainTitle;

    @ApiModelProperty(value = "培训类型：1-入职培训 2-技能培训 3-管理培训 4-安全培训 5-其他")
    private Integer trainType;

    @ApiModelProperty(value = "期望培训时间")
    private LocalDateTime expectedDate;

    @ApiModelProperty(value = "期望培训时长（小时）")
    private Integer expectedHours;

    @ApiModelProperty(value = "培训需求描述")
    private String requestReason;

    @ApiModelProperty(value = "参与人数")
    private Integer participantCount;

    @ApiModelProperty(value = "审批状态：0-待审批 1-已通过 2-已拒绝 3-已取消")
    private Integer status;

    @ApiModelProperty(value = "审批意见")
    private String approvalComment;

    @ApiModelProperty(value = "审批人ID")
    private Long approverId;

    @ApiModelProperty(value = "审批时间")
    private LocalDateTime approvalTime;

    @ApiModelProperty(value = "申请人ID")
    private Long applicantId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;

}
