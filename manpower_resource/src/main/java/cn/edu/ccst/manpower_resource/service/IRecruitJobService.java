package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IRecruitJobService extends IService<RecruitJob> {

    List<RecruitJob> listPublished();

    PageResult<RecruitJob> pageJobs(PageQuery query, Integer status);

    void publishJob(RecruitJob job);

    void updateJob(RecruitJob job);

    void closeJob(Long id);
}
