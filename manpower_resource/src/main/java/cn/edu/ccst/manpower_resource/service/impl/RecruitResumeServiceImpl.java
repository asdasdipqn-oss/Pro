package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.dto.RecruitResumeDTO;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import cn.edu.ccst.manpower_resource.entity.RecruitResume;
import cn.edu.ccst.manpower_resource.entity.RecruitApplication;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.RecruitJobMapper;
import cn.edu.ccst.manpower_resource.mapper.RecruitResumeMapper;
import cn.edu.ccst.manpower_resource.mapper.RecruitApplicationMapper;
import cn.edu.ccst.manpower_resource.service.IRecruitResumeService;
import cn.edu.ccst.manpower_resource.vo.RecruitResumeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简历/候选人表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class RecruitResumeServiceImpl extends ServiceImpl<RecruitResumeMapper, RecruitResume> implements IRecruitResumeService {

    private final RecruitJobMapper jobMapper;
    private final RecruitApplicationMapper applicationMapper;

    @Override
    public void submitResume(RecruitResumeDTO dto, Long jobSeekerId) {
        System.out.println("[RecruitResumeServiceImpl] submitResume called, dto: " + dto);
        System.out.println("[RecruitResumeServiceImpl] jobSeekerId: " + jobSeekerId);

        RecruitResume resume = new RecruitResume();
        BeanUtils.copyProperties(dto, resume);
        resume.setStatus(1); // 待筛选
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());

        System.out.println("[RecruitResumeServiceImpl] Creating resume in recruit_resume table");

        // 先插入简历
        baseMapper.insert(resume);

        System.out.println("[RecruitResumeServiceImpl] Resume inserted with ID: " + resume.getId());

        // 同时创建投递申请记录
        RecruitApplication application = new RecruitApplication();
        application.setJobId(dto.getJobId());
        application.setJobSeekerId(jobSeekerId);
        application.setResumeId(resume.getId());
        application.setStatus(0); // 待处理
        application.setSelfIntro(dto.getSelfIntro());
        application.setCoverLetter(null);
        application.setApplyTime(LocalDateTime.now());
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        System.out.println("[RecruitResumeServiceImpl] Creating application in recruit_application table");
        System.out.println("[RecruitResumeServiceImpl] Application data - jobId: " + application.getJobId() +
                    ", jobSeekerId: " + application.getJobSeekerId() +
                    ", resumeId: " + application.getResumeId() +
                    ", status: " + application.getStatus());

        applicationMapper.insert(application);

        System.out.println("[RecruitResumeServiceImpl] Application inserted successfully");
    }

    @Override
    public PageResult<RecruitResumeVO> pageList(PageQuery query, Long jobId, Integer status) {
        Page<RecruitResume> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RecruitResume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(jobId != null, RecruitResume::getJobId, jobId)
                .eq(status != null, RecruitResume::getStatus, status)
                .orderByDesc(RecruitResume::getCreateTime);
        Page<RecruitResume> result = baseMapper.selectPage(page, wrapper);
        List<RecruitResumeVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public RecruitResumeVO getDetail(Long id) {
        RecruitResume resume = baseMapper.selectById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return toVO(resume);
    }

    @Override
    public void screenResume(Long id, Integer status) {
        RecruitResume resume = baseMapper.selectById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        resume.setStatus(status);
        resume.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(resume);

        // 同步更新 recruit_application 表的状态
        syncApplicationStatus(resume.getId(), status);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        screenResume(id, status);
    }

    /**
     * 同步更新 recruit_application 表的状态
     * 根据简历ID查找对应的投递记录并更新状态
     */
    private void syncApplicationStatus(Long resumeId, Integer newStatus) {
        // 根据简历ID查找对应的投递记录
        LambdaQueryWrapper<RecruitApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitApplication::getResumeId, resumeId);
        List<RecruitApplication> applications = applicationMapper.selectList(wrapper);

        if (applications != null && !applications.isEmpty()) {
            for (RecruitApplication app : applications) {
                app.setStatus(newStatus);
                app.setUpdateTime(LocalDateTime.now());
                applicationMapper.updateById(app);
            }
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总简历数
        stats.put("totalResumes", baseMapper.selectCount(null));
        
        // 各状态统计
        stats.put("pending", baseMapper.selectCount(new LambdaQueryWrapper<RecruitResume>().eq(RecruitResume::getStatus, 1)));
        stats.put("screened", baseMapper.selectCount(new LambdaQueryWrapper<RecruitResume>().eq(RecruitResume::getStatus, 2)));
        stats.put("interviewing", baseMapper.selectCount(new LambdaQueryWrapper<RecruitResume>().eq(RecruitResume::getStatus, 3)));
        stats.put("hired", baseMapper.selectCount(new LambdaQueryWrapper<RecruitResume>().eq(RecruitResume::getStatus, 4)));
        stats.put("rejected", baseMapper.selectCount(new LambdaQueryWrapper<RecruitResume>().eq(RecruitResume::getStatus, 5)));
        
        // 发布岗位数
        stats.put("activeJobs", jobMapper.selectCount(new LambdaQueryWrapper<RecruitJob>().eq(RecruitJob::getStatus, 1)));
        
        return stats;
    }

    private RecruitResumeVO toVO(RecruitResume resume) {
        RecruitResumeVO vo = new RecruitResumeVO();
        BeanUtils.copyProperties(resume, vo);
        
        if (resume.getJobId() != null) {
            RecruitJob job = jobMapper.selectById(resume.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getJobName());
            }
        }
        
        vo.setEducationName(getEducationName(resume.getEducation()));
        vo.setStatusName(getStatusName(resume.getStatus()));
        return vo;
    }

    private String getEducationName(Integer edu) {
        if (edu == null) return "";
        switch (edu) {
            case 1: return "高中及以下";
            case 2: return "大专";
            case 3: return "本科";
            case 4: return "硕士";
            case 5: return "博士";
            default: return "";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "待筛选";
            case 2: return "筛选通过";
            case 3: return "面试中";
            case 4: return "已录用";
            case 5: return "未通过";
            case 6: return "已放弃";
            default: return "";
        }
    }
}
