package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.dto.JobApplicationRequest;
import cn.edu.ccst.manpower_resource.entity.RecruitApplication;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import cn.edu.ccst.manpower_resource.mapper.RecruitApplicationMapper;
import cn.edu.ccst.manpower_resource.mapper.RecruitJobMapper;
import cn.edu.ccst.manpower_resource.service.IRecruitCandidateService;
import cn.edu.ccst.manpower_resource.vo.RecruitApplicationVO;
import cn.edu.ccst.manpower_resource.vo.RecruitJobVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public void applyJob(JobApplicationRequest request, Long jobSeekerId) {
        // 检查是否已投递
        Long count = applicationMapper.selectCount(
                new LambdaQueryWrapper<RecruitApplication>()
                        .eq(RecruitApplication::getJobId, request.getJobId())
                        .eq(RecruitApplication::getJobSeekerId, jobSeekerId)
                        .eq(RecruitApplication::getStatus, 0)
        );

        if (count > 0) {
            throw new RuntimeException("您已经投递过该岗位");
        }

        // 使用mapper插入投递记录
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
}
