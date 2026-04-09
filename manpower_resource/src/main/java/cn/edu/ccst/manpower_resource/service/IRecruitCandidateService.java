package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.dto.JobApplicationRequest;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import cn.edu.ccst.manpower_resource.vo.RecruitJobVO;
import cn.edu.ccst.manpower_resource.vo.RecruitApplicationVO;
import cn.edu.ccst.manpower_resource.entity.RecruitApplication;

import java.util.List;

/**
 * 求职者招聘Service接口
 */
public interface IRecruitCandidateService {

    /**
     * 获取岗位列表
     */
    List<RecruitJobVO> getJobList();

    /**
     * 根据ID获取岗位
     */
    RecruitJobVO getJobById(Long id);

    /**
     * 投递简历
     */
    void applyJob(JobApplicationRequest request, Long jobSeekerId);

    /**
     * 获取我的投递记录
     */
    List<RecruitApplicationVO> getMyApplications(Long jobSeekerId);

    /**
     * 获取投递申请详情
     */
    RecruitApplication getApplicationDetail(Long applicationId);
}
