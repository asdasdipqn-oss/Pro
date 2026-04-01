package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "离职预测请求")
public class ResignationPredictRequest {

    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", required = true)
    private Long employeeId;

    @Schema(description = "分析月数（默认6个月）", example = "6")
    private Integer months = 6;
}
