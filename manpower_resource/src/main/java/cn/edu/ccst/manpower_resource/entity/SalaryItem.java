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
 * 薪资项目表
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("salary_item")
@ApiModel(value="SalaryItem对象", description="薪资项目表")
public class SalaryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "项目ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "项目编码")
    private String itemCode;

    @ApiModelProperty(value = "项目名称")
    private String itemName;

    @ApiModelProperty(value = "项目类型：1-固定收入 2-浮动收入 3-扣款 4-社保 5-公积金")
    private Integer itemType;

    @ApiModelProperty(value = "计算方式：1-固定金额 2-按比例 3-按公式")
    private Integer calcType;

    @ApiModelProperty(value = "计算公式")
    private String calcFormula;

    @ApiModelProperty(value = "是否计税：0-否 1-是")
    private Integer isTax;

    @ApiModelProperty(value = "是否必填：0-否 1-是")
    private Integer isRequired;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "状态：0-禁用 1-启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除")
    private Integer deleted;


}
