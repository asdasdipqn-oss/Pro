package cn.edu.ccst.manpower_resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "简历VO")
public class RecruitResumeVO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String name;
    private Integer gender;
    private String phone;
    private String email;
    private LocalDate birthday;
    private Integer education;
    private String educationName;
    private String graduateSchool;
    private String major;
    private Integer workYears;
    private String currentCompany;
    private String currentPosition;
    private String expectedSalary;
    private String resumeUrl;
    private String selfIntro;
    private String source;
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
}
