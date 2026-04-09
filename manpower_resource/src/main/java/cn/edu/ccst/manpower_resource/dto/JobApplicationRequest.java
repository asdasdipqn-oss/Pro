package cn.edu.ccst.manpower_resource.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 投递申请请求DTO
 */
@Data
@ApiModel(value="JobApplicationRequest", description="投递申请请求")
public class JobApplicationRequest {

    @ApiModelProperty(value = "岗位ID", required = true)
    @NotNull(message = "岗位ID不能为空")
    private Long jobId;

    @ApiModelProperty(value = "简历ID")
    private Long resumeId;

    @ApiModelProperty(value = "求职信")
    private Integer coverLetter;

    @ApiModelProperty(value = "个人说明")
    @Size(max = 500, message = "个人说明不能超过500个字符")
    private String selfIntro;
}
