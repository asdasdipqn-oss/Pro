package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.dto.JobApplicationRequest;
import cn.edu.ccst.manpower_resource.entity.HrCandidate;
import cn.edu.ccst.manpower_resource.entity.RecruitApplication;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import cn.edu.ccst.manpower_resource.entity.RecruitResume;
import cn.edu.ccst.manpower_resource.mapper.HrCandidateMapper;
import cn.edu.ccst.manpower_resource.mapper.RecruitApplicationMapper;
import cn.edu.ccst.manpower_resource.mapper.RecruitJobMapper;
import cn.edu.ccst.manpower_resource.mapper.RecruitResumeMapper;
import cn.edu.ccst.manpower_resource.service.IRecruitCandidateService;
import cn.edu.ccst.manpower_resource.vo.RecruitApplicationVO;
import cn.edu.ccst.manpower_resource.vo.RecruitJobVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 求职者招聘Service实现类
 */
@Service
@RequiredArgsConstructor
public class RecruitCandidateServiceImpl implements IRecruitCandidateService {

    private final RecruitJobMapper jobMapper;
    private final RecruitApplicationMapper applicationMapper;
    private final HrCandidateMapper candidateMapper;
    private final RecruitResumeMapper resumeMapper;

    @Override
    public List<cn.edu.ccst.manpower_resource.vo.RecruitJobVO> getJobList() {
        LambdaQueryWrapper<RecruitJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitJob::getStatus, 1);
        wrapper.orderByDesc(RecruitJob::getPublishTime);
        wrapper.orderByDesc(RecruitJob::getCreateTime);

        return jobMapper.selectList(wrapper)
                .stream()
                .map(job -> {
                    cn.edu.ccst.manpower_resource.vo.RecruitJobVO vo = new cn.edu.ccst.manpower_resource.vo.RecruitJobVO();
                    vo.setId(job.getId());
                    vo.setJobCode(job.getJobCode());
                    vo.setJobName(job.getJobName());
                    vo.setDeptId(job.getDeptId());
                    vo.setPositionId(job.getPositionId());
                    vo.setHeadcount(job.getHeadcount());
                    vo.setHiredCount(job.getHiredCount());
                    vo.setSalaryMin(job.getSalaryMin());
                    vo.setSalaryMax(job.getSalaryMax());
                    vo.setEducationReq(job.getEducationReq());
                    vo.setExperienceReq(job.getExperienceReq());
                    vo.setJobDescription(job.getJobDescription());
                    vo.setRequirements(job.getRequirements());
                    vo.setBenefits(job.getBenefits());
                    vo.setLocation(job.getLocation());
                    vo.setPublisherId(job.getPublisherId());
                    vo.setPublishTime(job.getPublishTime());
                    if (job.getDeadline() != null) {
                        vo.setDeadline(job.getDeadline().atStartOfDay());
                    }
                    return vo;
                })
                .toList();
    }

    @Override
    public cn.edu.ccst.manpower_resource.vo.RecruitJobVO getJobById(Long id) {
        RecruitJob job = jobMapper.selectById(id);
        if (job == null || job.getDeleted() == 1) {
            throw new RuntimeException("岗位不存在");
        }

        cn.edu.ccst.manpower_resource.vo.RecruitJobVO vo = new cn.edu.ccst.manpower_resource.vo.RecruitJobVO();
        vo.setId(job.getId());
        vo.setJobCode(job.getJobCode());
        vo.setJobName(job.getJobName());
        vo.setDeptId(job.getDeptId());
        vo.setPositionId(job.getPositionId());
        vo.setHeadcount(job.getHeadcount());
        vo.setHiredCount(job.getHiredCount());
        vo.setSalaryMin(job.getSalaryMin());
        vo.setSalaryMax(job.getSalaryMax());
        vo.setEducationReq(job.getEducationReq());
        vo.setExperienceReq(job.getExperienceReq());
        vo.setJobDescription(job.getJobDescription());
        vo.setRequirements(job.getRequirements());
        vo.setBenefits(job.getBenefits());
        vo.setLocation(job.getLocation());
        vo.setPublisherId(job.getPublisherId());
        vo.setPublishTime(job.getPublishTime());
        if (job.getDeadline() != null) {
            vo.setDeadline(job.getDeadline().atStartOfDay());
        }
        return vo;
    }

    @Override
    @Transactional
    public void applyJob(JobApplicationRequest request, Long jobSeekerId) {
        // 检查是否已投递
        Long count = applicationMapper.selectCount(
                new LambdaQueryWrapper<RecruitApplication>()
                        .eq(RecruitApplication::getJobId, request.getJobId())
                        .eq(RecruitApplication::getJobSeekerId, jobSeekerId)
        );

        if (count > 0) {
            throw new RuntimeException("您已经投递过该岗位");
        }

        // 查询求职者信息
        HrCandidate candidate = candidateMapper.selectById(jobSeekerId);

        // 同时写入 recruit_resume 表，让HR在简历管理中可见
        RecruitResume resume = new RecruitResume();
        resume.setJobId(request.getJobId());
        resume.setName(request.getName() != null ? request.getName() :
                (candidate != null ? candidate.getRealName() : ""));
        resume.setPhone(request.getPhone() != null ? request.getPhone() :
                (candidate != null ? candidate.getPhone() : ""));
        resume.setEmail(request.getEmail() != null ? request.getEmail() :
                (candidate != null ? candidate.getEmail() : ""));
        resume.setGender(candidate != null ? candidate.getGender() : null);
        resume.setEducation(request.getEducation() != null ? request.getEducation() :
                (candidate != null ? candidate.getEducation() : null));
        resume.setWorkYears(request.getWorkYears() != null ? request.getWorkYears() :
                (candidate != null ? candidate.getWorkExperience() : null));
        resume.setGraduateSchool(candidate != null ? candidate.getGraduateSchool() : null);
        resume.setMajor(candidate != null ? candidate.getMajor() : null);
        resume.setSelfIntro(request.getSelfIntro());
        resume.setSource("在线投递");
        resume.setStatus(1); // 待筛选
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());
        resumeMapper.insert(resume);

        // 设置 resumeId，写入 recruit_application
        request.setResumeId(resume.getId());
        applicationMapper.insertApplication(request, jobSeekerId);
    }

    @Override
    public List<RecruitApplicationVO> getMyApplications(Long jobSeekerId) {
        return applicationMapper.selectByJobSeeker(jobSeekerId);
    }

    @Override
    public RecruitApplication getApplicationDetail(Long applicationId) {
        return applicationMapper.selectById(applicationId);
    }

    @Override
    public Long getCandidateIdByUsername(String username) {
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
                        .eq(HrCandidate::getDeleted, 0)
        );

        if (candidate == null) {
            throw new RuntimeException("求职者不存在: " + username);
        }

        return candidate.getId();
    }
}
