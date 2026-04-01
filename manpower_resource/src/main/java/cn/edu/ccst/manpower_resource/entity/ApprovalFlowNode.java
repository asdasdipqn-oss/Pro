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
 * 审批流程节点表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("approval_flow_node")
@ApiModel(value="ApprovalFlowNode对象", description="审批流程节点表")
public class ApprovalFlowNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "节点ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "流程ID")
    private Long flowId;

    @ApiModelProperty(value = "节点名称")
    private String nodeName;

    @ApiModelProperty(value = "节点顺序")
    private Integer nodeOrder;

    @ApiModelProperty(value = "审批人类型：1-指定人员 2-指定角色 3-部门负责人 4-直属领导")
    private Integer approverType;

    @ApiModelProperty(value = "审批人/角色ID（当类型为1或2时）")
    private Long approverId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
