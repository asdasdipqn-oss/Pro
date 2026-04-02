package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "岗位信息（含部门名称）")
public class PositionVO {

    @Schema(description = "岗位ID")
    private Long id;

    @Schema(description = "岗位编码")
    private String positionCode;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "岗位级别：1-初级 2-中级 3-高级 4-专家")
    private Integer positionLevel;

    @Schema(description = "岗位描述")
    private String description;

    @Schema(description = "任职要求")
    private String requirements;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
