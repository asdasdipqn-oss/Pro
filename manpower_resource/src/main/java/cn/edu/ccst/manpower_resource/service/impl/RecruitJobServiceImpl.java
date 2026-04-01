package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.RecruitJobMapper;
import cn.edu.ccst.manpower_resource.service.IRecruitJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class RecruitJobServiceImpl extends ServiceImpl<RecruitJobMapper, RecruitJob> implements IRecruitJobService {

    @Override
    public List<RecruitJob> listPublished() {
        return baseMapper.selectList(new LambdaQueryWrapper<RecruitJob>()
                .eq(RecruitJob::getStatus, 1)
                .eq(RecruitJob::getDeleted, 0)
                .orderByDesc(RecruitJob::getCreateTime));
    }

    @Override
    public PageResult<RecruitJob> pageJobs(PageQuery query, Integer status) {
        Page<RecruitJob> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RecruitJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, RecruitJob::getStatus, status)
                .eq(RecruitJob::getDeleted, 0)
                .orderByDesc(RecruitJob::getCreateTime);
        Page<RecruitJob> result = baseMapper.selectPage(page, wrapper);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public void publishJob(RecruitJob job) {
        // 自动生成岗位编号
        String jobCode = "JOB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        job.setJobCode(jobCode);
        job.setStatus(1);
        job.setDeleted(0);
        job.setPublishTime(LocalDateTime.now());
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(job);
    }

    @Override
    @Transactional
    public void updateJob(RecruitJob job) {
        RecruitJob old = baseMapper.selectById(job.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        job.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(job);
    }

    @Override
    @Transactional
    public void closeJob(Long id) {
        RecruitJob job = baseMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        job.setStatus(2); // 关闭
        job.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(job);
    }
}
