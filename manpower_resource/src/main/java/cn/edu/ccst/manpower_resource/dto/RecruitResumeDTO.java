package cn.edu.ccst.manpower_resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "简历投递DTO")
public class RecruitResumeDTO {

    @NotNull(message = "应聘岗位不能为空")
    @Schema(description = "应聘岗位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long jobId;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "性别：0-女 1-男")
    private Integer gender;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "出生日期")
    private LocalDate birthday;

    @Schema(description = "学历：1-高中及以下 2-大专 3-本科 4-硕士 5-博士")
    private Integer education;

    @Schema(description = "毕业院校")
    private String graduateSchool;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "工作年限")
    private Integer workYears;

    @Schema(description = "当前公司")
    private String currentCompany;

    @Schema(description = "当前职位")
    private String currentPosition;

    @Schema(description = "期望薪资")
    private String expectedSalary;

    @Schema(description = "简历附件URL")
    private String resumeUrl;

    @Schema(description = "自我介绍")
    private String selfIntro;

    @Schema(description = "简历来源")
    private String source;
}
