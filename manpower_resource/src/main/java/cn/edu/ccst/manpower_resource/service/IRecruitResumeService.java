package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.dto.RecruitResumeDTO;
import cn.edu.ccst.manpower_resource.entity.RecruitResume;
import cn.edu.ccst.manpower_resource.vo.RecruitResumeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 简历/候选人表 服务类
 */
public interface IRecruitResumeService extends IService<RecruitResume> {

    void submitResume(RecruitResumeDTO dto, Long jobSeekerId);

    PageResult<RecruitResumeVO> pageList(PageQuery query, Long jobId, Integer status);

    RecruitResumeVO getDetail(Long id);

    void screenResume(Long id, Integer status);

    void updateStatus(Long id, Integer status);

    Map<String, Object> getStatistics();
}
