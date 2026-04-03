package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "培训需求DTO")
public class TrainRequestDTO {

    @Schema(description = "培训主题")
    @NotBlank(message = "培训主题不能为空")
    private String trainTitle;

    @Schema(description = "培训类型：1-入职培训 2-技能培训 3-管理培训 4-安全培训 5-其他")
    @NotNull(message = "培训类型不能为空")
    private Integer trainType;

    @Schema(description = "期望培训时间")
    private LocalDateTime expectedDate;

    @Schema(description = "期望培训时长（小时）")
    private Integer expectedHours;

    @Schema(description = "培训需求描述")
    @NotBlank(message = "培训需求描述不能为空")
    private String requestReason;

    @Schema(description = "参与人数")
    private Integer participantCount;
}
